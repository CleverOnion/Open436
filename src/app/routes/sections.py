"""
板块管理路由
实现所有板块相关的API端点
"""
from fastapi import APIRouter, Depends, HTTPException, Query, status
from sqlalchemy.orm import Session
from typing import List

from src.app.models.database import get_db
from src.app.models.schemas import (
    SectionCreate,
    SectionUpdate,
    SectionResponse,
    SectionListResponse,
    MessageResponse
)
from src.app.services.section_service import SectionService

router = APIRouter()


@router.get("/", response_model=SectionListResponse, summary="获取板块列表")
async def get_sections(
    page: int = Query(1, ge=1, description="页码"),
    page_size: int = Query(20, ge=1, le=100, description="每页数量"),
    enabled_only: bool = Query(True, description="仅显示启用的板块"),
    db: Session = Depends(get_db)
):
    """
    获取板块列表
    
    - 支持分页
    - 可选择仅显示启用的板块
    - 按sort_order排序
    """
    service = SectionService(db)
    sections, total = service.get_sections(
        page=page,
        page_size=page_size,
        enabled_only=enabled_only
    )
    
    return SectionListResponse(
        total=total,
        items=sections,
        page=page,
        page_size=page_size
    )


@router.get("/{section_id}", response_model=SectionResponse, summary="获取板块详情")
async def get_section(
    section_id: int,
    db: Session = Depends(get_db)
):
    """根据ID获取板块详细信息"""
    service = SectionService(db)
    section = service.get_section_by_id(section_id)
    
    if not section:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"板块ID {section_id} 不存在"
        )
    
    return section


@router.get("/slug/{slug}", response_model=SectionResponse, summary="通过slug获取板块")
async def get_section_by_slug(
    slug: str,
    db: Session = Depends(get_db)
):
    """根据slug获取板块信息"""
    service = SectionService(db)
    section = service.get_section_by_slug(slug)
    
    if not section:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"板块标识 '{slug}' 不存在"
        )
    
    return section


@router.post("/", response_model=SectionResponse, status_code=status.HTTP_201_CREATED, summary="创建板块")
async def create_section(
    section_data: SectionCreate,
    db: Session = Depends(get_db)
):
    """
    创建新板块（管理员功能）
    
    - 板块名称和slug必须唯一
    - slug只能包含小写字母、数字、下划线
    - 颜色必须是HEX格式
    """
    service = SectionService(db)
    
    # 检查名称是否已存在
    if service.get_section_by_name(section_data.name):
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"板块名称 '{section_data.name}' 已存在"
        )
    
    # 检查slug是否已存在
    if service.get_section_by_slug(section_data.slug):
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"板块标识 '{section_data.slug}' 已存在"
        )
    
    section = service.create_section(section_data)
    return section


@router.put("/{section_id}", response_model=SectionResponse, summary="更新板块")
async def update_section(
    section_id: int,
    section_data: SectionUpdate,
    db: Session = Depends(get_db)
):
    """
    更新板块信息（管理员功能）
    
    - 不能修改slug
    - 修改名称时需要检查唯一性
    """
    service = SectionService(db)
    
    # 检查板块是否存在
    section = service.get_section_by_id(section_id)
    if not section:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"板块ID {section_id} 不存在"
        )
    
    # 如果修改了名称，检查新名称是否已被其他板块使用
    if section_data.name and section_data.name != section.name:
        existing = service.get_section_by_name(section_data.name)
        if existing:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=f"板块名称 '{section_data.name}' 已被使用"
            )
    
    updated_section = service.update_section(section_id, section_data)
    return updated_section


@router.delete("/{section_id}", response_model=MessageResponse, summary="删除板块")
async def delete_section(
    section_id: int,
    force: bool = Query(False, description="强制删除（即使有帖子）"),
    db: Session = Depends(get_db)
):
    """
    删除板块（管理员功能）
    
    - 默认仅软删除（禁用）
    - 如果板块内有帖子，建议使用禁用而非删除
    - force=True时执行硬删除
    """
    service = SectionService(db)
    
    section = service.get_section_by_id(section_id)
    if not section:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"板块ID {section_id} 不存在"
        )
    
    # 检查是否有帖子
    if section.posts_count > 0 and not force:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"板块内有 {section.posts_count} 篇帖子，建议使用禁用功能"
        )
    
    service.delete_section(section_id, soft_delete=not force)
    
    return MessageResponse(
        message=f"板块 '{section.name}' 已{'删除' if force else '禁用'}",
        code=200
    )


@router.patch("/{section_id}/toggle", response_model=SectionResponse, summary="切换板块启用状态")
async def toggle_section(
    section_id: int,
    db: Session = Depends(get_db)
):
    """
    启用/禁用板块（管理员功能）
    
    - 至少保留一个启用的板块
    """
    service = SectionService(db)
    
    section = service.get_section_by_id(section_id)
    if not section:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"板块ID {section_id} 不存在"
        )
    
    # 如果要禁用，检查是否是最后一个启用的板块
    if section.is_enabled:
        enabled_count = service.count_enabled_sections()
        if enabled_count <= 1:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="至少需要保留一个启用的板块"
            )
    
    updated_section = service.toggle_section(section_id)
    return updated_section


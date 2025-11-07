"""
板块业务逻辑服务
封装所有板块相关的业务操作
"""
from sqlalchemy.orm import Session
from sqlalchemy import func
from typing import List, Tuple, Optional

from src.app.models.section import Section
from src.app.models.schemas import SectionCreate, SectionUpdate


class SectionService:
    """板块服务类"""
    
    def __init__(self, db: Session):
        self.db = db
    
    def get_sections(
        self,
        page: int = 1,
        page_size: int = 20,
        enabled_only: bool = True
    ) -> Tuple[List[Section], int]:
        """
        获取板块列表（分页）
        
        Args:
            page: 页码
            page_size: 每页数量
            enabled_only: 是否仅显示启用的板块
            
        Returns:
            (板块列表, 总数)
        """
        query = self.db.query(Section)
        
        if enabled_only:
            query = query.filter(Section.is_enabled == True)
        
        # 获取总数
        total = query.count()
        
        # 分页查询
        sections = query.order_by(
            Section.sort_order.asc(),
            Section.id.asc()
        ).offset((page - 1) * page_size).limit(page_size).all()
        
        return sections, total
    
    def get_section_by_id(self, section_id: int) -> Optional[Section]:
        """根据ID获取板块"""
        return self.db.query(Section).filter(Section.id == section_id).first()
    
    def get_section_by_slug(self, slug: str) -> Optional[Section]:
        """根据slug获取板块"""
        return self.db.query(Section).filter(Section.slug == slug).first()
    
    def get_section_by_name(self, name: str) -> Optional[Section]:
        """根据名称获取板块"""
        return self.db.query(Section).filter(Section.name == name).first()
    
    def create_section(self, section_data: SectionCreate) -> Section:
        """
        创建新板块
        
        Args:
            section_data: 板块创建数据
            
        Returns:
            创建的板块对象
        """
        section = Section(
            slug=section_data.slug,
            name=section_data.name,
            description=section_data.description,
            color=section_data.color,
            sort_order=section_data.sort_order,
            is_enabled=True,
            posts_count=0
        )
        
        self.db.add(section)
        self.db.commit()
        self.db.refresh(section)
        
        return section
    
    def update_section(
        self,
        section_id: int,
        section_data: SectionUpdate
    ) -> Optional[Section]:
        """
        更新板块信息
        
        Args:
            section_id: 板块ID
            section_data: 更新数据
            
        Returns:
            更新后的板块对象
        """
        section = self.get_section_by_id(section_id)
        if not section:
            return None
        
        # 更新字段（仅更新提供的字段）
        update_data = section_data.model_dump(exclude_unset=True)
        for field, value in update_data.items():
            setattr(section, field, value)
        
        self.db.commit()
        self.db.refresh(section)
        
        return section
    
    def delete_section(self, section_id: int, soft_delete: bool = True) -> bool:
        """
        删除板块
        
        Args:
            section_id: 板块ID
            soft_delete: 是否软删除（True=禁用，False=硬删除）
            
        Returns:
            是否成功
        """
        section = self.get_section_by_id(section_id)
        if not section:
            return False
        
        if soft_delete:
            # 软删除：设置为禁用状态
            section.is_enabled = False
            self.db.commit()
        else:
            # 硬删除：从数据库删除
            self.db.delete(section)
            self.db.commit()
        
        return True
    
    def toggle_section(self, section_id: int) -> Optional[Section]:
        """
        切换板块启用状态
        
        Args:
            section_id: 板块ID
            
        Returns:
            更新后的板块对象
        """
        section = self.get_section_by_id(section_id)
        if not section:
            return None
        
        section.is_enabled = not section.is_enabled
        self.db.commit()
        self.db.refresh(section)
        
        return section
    
    def count_enabled_sections(self) -> int:
        """统计启用的板块数量"""
        return self.db.query(func.count(Section.id)).filter(
            Section.is_enabled == True
        ).scalar()
    
    def increment_posts_count(self, section_id: int) -> bool:
        """
        增加板块帖子数量
        当M3创建帖子时调用
        
        Args:
            section_id: 板块ID
            
        Returns:
            是否成功
        """
        section = self.get_section_by_id(section_id)
        if not section:
            return False
        
        section.posts_count += 1
        self.db.commit()
        
        return True
    
    def decrement_posts_count(self, section_id: int) -> bool:
        """
        减少板块帖子数量
        当M3删除帖子时调用
        
        Args:
            section_id: 板块ID
            
        Returns:
            是否成功
        """
        section = self.get_section_by_id(section_id)
        if not section:
            return False
        
        if section.posts_count > 0:
            section.posts_count -= 1
            self.db.commit()
        
        return True


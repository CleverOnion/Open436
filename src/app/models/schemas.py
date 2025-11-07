"""
Pydantic 数据模式（用于请求/响应验证）
"""
from pydantic import BaseModel, Field, field_validator
from typing import Optional
from datetime import datetime
import re


class SectionBase(BaseModel):
    """板块基础模式"""
    name: str = Field(..., min_length=2, max_length=50, description="板块名称")
    slug: str = Field(..., min_length=3, max_length=20, description="板块标识")
    description: Optional[str] = Field(None, max_length=500, description="板块描述")
    color: str = Field(..., pattern=r"^#[0-9A-Fa-f]{6}$", description="板块颜色（HEX格式）")
    sort_order: int = Field(default=100, ge=1, le=999, description="排序号")
    
    @field_validator('slug')
    @classmethod
    def validate_slug(cls, v: str) -> str:
        """验证slug格式"""
        if not re.match(r'^[a-z0-9_]+$', v):
            raise ValueError('slug只能包含小写字母、数字和下划线')
        return v


class SectionCreate(SectionBase):
    """创建板块请求模式"""
    pass


class SectionUpdate(BaseModel):
    """更新板块请求模式"""
    name: Optional[str] = Field(None, min_length=2, max_length=50)
    description: Optional[str] = Field(None, max_length=500)
    color: Optional[str] = Field(None, pattern=r"^#[0-9A-Fa-f]{6}$")
    sort_order: Optional[int] = Field(None, ge=1, le=999)
    is_enabled: Optional[bool] = None


class SectionResponse(SectionBase):
    """板块响应模式"""
    id: int
    icon_file_id: Optional[str] = None
    is_enabled: bool
    posts_count: int
    created_at: datetime
    updated_at: datetime
    
    class Config:
        from_attributes = True


class SectionListResponse(BaseModel):
    """板块列表响应模式"""
    total: int
    items: list[SectionResponse]
    page: int
    page_size: int


class MessageResponse(BaseModel):
    """通用消息响应"""
    message: str
    code: int = 200


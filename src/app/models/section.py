"""
板块数据模型（SQLAlchemy ORM）
"""
from sqlalchemy import Column, Integer, String, Text, Boolean, TIMESTAMP, CheckConstraint
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.sql import func
from datetime import datetime

from src.app.models.database import Base


class Section(Base):
    """板块模型"""
    
    __tablename__ = "sections"
    __table_args__ = (
        CheckConstraint('sort_order >= 1 AND sort_order <= 999', name='chk_sort_order'),
        CheckConstraint('posts_count >= 0', name='chk_posts_count'),
        CheckConstraint("color ~ '^#[0-9A-Fa-f]{6}$'", name='chk_color_format'),
        CheckConstraint("slug ~ '^[a-z0-9_]+$'", name='chk_slug_format'),
    )
    
    # 主键
    id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    
    # 板块标识（用于URL）
    slug = Column(String(20), unique=True, nullable=False, index=True)
    
    # 板块名称
    name = Column(String(50), unique=True, nullable=False, index=True)
    
    # 板块描述
    description = Column(Text, nullable=True)
    
    # 板块图标文件ID（外键关联files表）
    icon_file_id = Column(UUID(as_uuid=True), nullable=True)
    
    # 板块颜色（HEX格式）
    color = Column(String(7), nullable=False)
    
    # 排序号
    sort_order = Column(Integer, nullable=False, default=100, index=True)
    
    # 启用状态
    is_enabled = Column(Boolean, nullable=False, default=True, index=True)
    
    # 帖子数量（冗余字段）
    posts_count = Column(Integer, nullable=False, default=0)
    
    # 时间戳
    created_at = Column(
        TIMESTAMP(timezone=True),
        nullable=False,
        server_default=func.now()
    )
    updated_at = Column(
        TIMESTAMP(timezone=True),
        nullable=False,
        server_default=func.now(),
        onupdate=func.now()
    )
    
    def __repr__(self):
        return f"<Section(id={self.id}, slug='{self.slug}', name='{self.name}')>"
    
    def to_dict(self):
        """转换为字典"""
        return {
            "id": self.id,
            "slug": self.slug,
            "name": self.name,
            "description": self.description,
            "icon_file_id": str(self.icon_file_id) if self.icon_file_id else None,
            "color": self.color,
            "sort_order": self.sort_order,
            "is_enabled": self.is_enabled,
            "posts_count": self.posts_count,
            "created_at": self.created_at.isoformat() if self.created_at else None,
            "updated_at": self.updated_at.isoformat() if self.updated_at else None,
        }


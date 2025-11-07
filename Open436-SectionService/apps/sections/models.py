"""
Section models
"""
from django.db import models
from django.core.validators import RegexValidator, MinValueValidator, MaxValueValidator


class Section(models.Model):
    """
    板块模型
    
    注意：此表由 SQL 直接创建，Django 不管理迁移（managed=False）
    """
    
    # 主键
    id = models.AutoField(primary_key=True)
    
    # 板块标识（用于 URL）
    slug = models.SlugField(
        max_length=20,
        unique=True,
        db_index=True,
        validators=[
            RegexValidator(
                regex=r'^[a-z0-9_]+$',
                message='板块标识只能包含小写字母、数字和下划线'
            )
        ],
        help_text='板块标识，用于 URL（3-20个字符）'
    )
    
    # 板块名称
    name = models.CharField(
        max_length=50,
        unique=True,
        help_text='板块名称（2-50个字符）'
    )
    
    # 板块描述
    description = models.TextField(
        blank=True,
        null=True,
        help_text='板块描述，支持 Markdown'
    )
    
    # 板块图标文件 ID（外键关联 files 表）
    icon_file_id = models.UUIDField(
        blank=True,
        null=True,
        db_column='icon_file_id',
        help_text='板块图标文件 ID（M7 文件服务）'
    )
    
    # 板块颜色
    color = models.CharField(
        max_length=7,
        validators=[
            RegexValidator(
                regex=r'^#[0-9A-Fa-f]{6}$',
                message='颜色必须是 HEX 格式（如 #1976D2）'
            )
        ],
        help_text='板块颜色（HEX 格式）'
    )
    
    # 排序号
    sort_order = models.IntegerField(
        default=100,
        validators=[
            MinValueValidator(1),
            MaxValueValidator(999)
        ],
        help_text='排序号（1-999，数字越小越靠前）'
    )
    
    # 启用状态
    is_enabled = models.BooleanField(
        default=True,
        db_index=True,
        help_text='是否启用（False 为禁用）'
    )
    
    # 帖子数量（冗余字段）
    posts_count = models.IntegerField(
        default=0,
        validators=[MinValueValidator(0)],
        help_text='板块内帖子数量'
    )
    
    # 时间戳
    created_at = models.DateTimeField(
        auto_now_add=True,
        help_text='创建时间'
    )
    updated_at = models.DateTimeField(
        auto_now=True,
        help_text='最后更新时间'
    )
    
    class Meta:
        db_table = 'sections'
        managed = False  # 不由 Django 管理迁移
        ordering = ['sort_order', 'id']  # 默认排序
        verbose_name = '板块'
        verbose_name_plural = '板块'
    
    def __str__(self):
        return f'{self.name} ({self.slug})'
    
    def __repr__(self):
        return f'<Section: {self.slug}>'
    
    @classmethod
    def get_enabled_sections(cls):
        """获取所有启用的板块"""
        return cls.objects.filter(is_enabled=True).order_by('sort_order', 'id')
    
    @classmethod
    def get_by_slug(cls, slug):
        """根据 slug 获取板块"""
        try:
            return cls.objects.get(slug=slug, is_enabled=True)
        except cls.DoesNotExist:
            return None
    
    def increment_posts_count(self, value=1):
        """
        更新帖子数量
        
        Args:
            value: 增量值（+1 或 -1）
        """
        self.posts_count = models.F('posts_count') + value
        self.save(update_fields=['posts_count'])
        self.refresh_from_db()  # 刷新以获取实际值
    
    def can_be_deleted(self):
        """
        检查板块是否可以被删除
        
        Returns:
            tuple: (可删除, 原因)
        """
        # 检查是否有帖子
        if self.posts_count > 0:
            return False, f'板块内有 {self.posts_count} 篇帖子，无法删除'
        
        # 检查是否是最后一个启用的板块
        enabled_count = Section.objects.filter(is_enabled=True).count()
        if self.is_enabled and enabled_count <= 1:
            return False, '至少需要保留一个启用的板块'
        
        return True, None
    
    def disable(self):
        """
        禁用板块（软删除）
        
        Raises:
            ValueError: 如果是最后一个启用的板块
        """
        enabled_count = Section.objects.filter(is_enabled=True).count()
        if self.is_enabled and enabled_count <= 1:
            raise ValueError('至少需要保留一个启用的板块')
        
        self.is_enabled = False
        self.save(update_fields=['is_enabled'])
    
    def enable(self):
        """启用板块"""
        self.is_enabled = True
        self.save(update_fields=['is_enabled'])


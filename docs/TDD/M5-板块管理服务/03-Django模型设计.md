# M5 板块管理服务 - Django 模型设计

## 文档信息

**服务名称**: 板块管理服务 (section-service)  
**框架**: Django 4.2+ + Django REST Framework 3.14+  
**版本**: v1.0

---

## 目录

1. [模型层设计](#模型层设计)
2. [序列化器设计](#序列化器设计)
3. [视图集设计](#视图集设计)
4. [权限控制](#权限控制)
5. [URL 路由配置](#url-路由配置)
6. [完整代码示例](#完整代码示例)

---

## 模型层设计

### Section 模型

板块模型使用 `managed=False`，因为表结构由 SQL 直接创建，Django 不管理迁移。

#### apps/sections/models.py

```python
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
        indexes = [
            models.Index(fields=['slug'], name='idx_sections_slug'),
            models.Index(fields=['is_enabled'], name='idx_sections_is_enabled'),
            models.Index(fields=['sort_order', 'id'], name='idx_sections_sort_order'),
        ]
    
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
```

---

## 序列化器设计

### 基础序列化器

#### apps/sections/serializers.py

```python
from rest_framework import serializers
from .models import Section
from apps.core.service_client import FileServiceClient


class SectionListSerializer(serializers.ModelSerializer):
    """
    板块列表序列化器（用于列表展示）
    """
    
    icon_url = serializers.SerializerMethodField()
    
    class Meta:
        model = Section
        fields = [
            'id',
            'slug',
            'name',
            'description',
            'icon_url',
            'color',
            'sort_order',
            'posts_count',
            'created_at',
            'updated_at'
        ]
        read_only_fields = ['id', 'created_at', 'updated_at', 'posts_count']
    
    def get_icon_url(self, obj):
        """
        获取图标 URL
        
        如果有 icon_file_id，则调用 M7 文件服务获取 URL
        """
        if not obj.icon_file_id:
            return None
        
        # 调用 M7 文件服务获取 URL
        try:
            file_client = FileServiceClient()
            file_info = file_client.get_file_url(obj.icon_file_id)
            return file_info.get('url')
        except Exception as e:
            # 日志记录错误
            print(f'Error getting icon URL: {e}')
            return None


class SectionDetailSerializer(serializers.ModelSerializer):
    """
    板块详情序列化器（包含更多信息）
    """
    
    icon_url = serializers.SerializerMethodField()
    
    class Meta:
        model = Section
        fields = [
            'id',
            'slug',
            'name',
            'description',
            'icon_file_id',
            'icon_url',
            'color',
            'sort_order',
            'is_enabled',
            'posts_count',
            'created_at',
            'updated_at'
        ]
        read_only_fields = ['id', 'created_at', 'updated_at', 'posts_count']
    
    def get_icon_url(self, obj):
        """获取图标 URL"""
        if not obj.icon_file_id:
            return None
        
        try:
            file_client = FileServiceClient()
            file_info = file_client.get_file_url(obj.icon_file_id)
            return file_info.get('url')
        except Exception:
            return None


class SectionCreateSerializer(serializers.ModelSerializer):
    """
    创建板块序列化器
    """
    
    class Meta:
        model = Section
        fields = [
            'slug',
            'name',
            'description',
            'icon_file_id',
            'color',
            'sort_order'
        ]
    
    def validate_slug(self, value):
        """
        验证 slug 格式和唯一性
        """
        # 长度验证
        if len(value) < 3 or len(value) > 20:
            raise serializers.ValidationError('板块标识长度必须在 3-20 个字符之间')
        
        # 格式验证（仅小写字母、数字、下划线）
        import re
        if not re.match(r'^[a-z0-9_]+$', value):
            raise serializers.ValidationError('板块标识只能包含小写字母、数字和下划线')
        
        # 唯一性验证
        if Section.objects.filter(slug=value).exists():
            raise serializers.ValidationError('板块标识已存在')
        
        return value
    
    def validate_name(self, value):
        """
        验证板块名称
        """
        # 长度验证
        if len(value) < 2 or len(value) > 50:
            raise serializers.ValidationError('板块名称长度必须在 2-50 个字符之间')
        
        # 唯一性验证
        if Section.objects.filter(name=value).exists():
            raise serializers.ValidationError('板块名称已存在')
        
        return value
    
    def validate_color(self, value):
        """
        验证颜色格式
        """
        import re
        if not re.match(r'^#[0-9A-Fa-f]{6}$', value):
            raise serializers.ValidationError('颜色必须是 HEX 格式（如 #1976D2）')
        
        return value
    
    def validate_sort_order(self, value):
        """
        验证排序号范围
        """
        if value < 1 or value > 999:
            raise serializers.ValidationError('排序号必须在 1-999 之间')
        
        return value


class SectionUpdateSerializer(serializers.ModelSerializer):
    """
    更新板块序列化器（不允许修改 slug）
    """
    
    slug = serializers.SlugField(read_only=True)  # 不可修改
    
    class Meta:
        model = Section
        fields = [
            'slug',
            'name',
            'description',
            'icon_file_id',
            'color',
            'sort_order'
        ]
    
    def validate_name(self, value):
        """
        验证板块名称（排除自己）
        """
        if len(value) < 2 or len(value) > 50:
            raise serializers.ValidationError('板块名称长度必须在 2-50 个字符之间')
        
        # 唯一性验证（排除当前对象）
        instance = self.instance
        if Section.objects.filter(name=value).exclude(id=instance.id).exists():
            raise serializers.ValidationError('板块名称已存在')
        
        return value
    
    def validate_color(self, value):
        """验证颜色格式"""
        import re
        if not re.match(r'^#[0-9A-Fa-f]{6}$', value):
            raise serializers.ValidationError('颜色必须是 HEX 格式（如 #1976D2）')
        
        return value


class SectionStatusSerializer(serializers.Serializer):
    """
    板块状态序列化器（启用/禁用）
    """
    
    is_enabled = serializers.BooleanField(required=True)
    
    def validate_is_enabled(self, value):
        """
        验证状态变更
        """
        instance = self.context.get('instance')
        
        # 如果要禁用板块
        if not value and instance.is_enabled:
            # 检查是否是最后一个启用的板块
            enabled_count = Section.objects.filter(is_enabled=True).count()
            if enabled_count <= 1:
                raise serializers.ValidationError('至少需要保留一个启用的板块')
        
        return value


class SectionReorderSerializer(serializers.Serializer):
    """
    板块排序序列化器
    """
    
    order = serializers.ListField(
        child=serializers.IntegerField(min_value=1),
        min_length=1,
        max_length=100,
        help_text='板块 ID 数组，按新顺序排列'
    )
    
    def validate_order(self, value):
        """
        验证排序数组
        """
        # 检查是否有重复
        if len(value) != len(set(value)):
            raise serializers.ValidationError('排序数组中不能有重复的 ID')
        
        # 检查所有 ID 是否有效
        existing_ids = set(Section.objects.filter(id__in=value).values_list('id', flat=True))
        invalid_ids = set(value) - existing_ids
        
        if invalid_ids:
            raise serializers.ValidationError(f'无效的板块 ID: {list(invalid_ids)}')
        
        return value


class SectionStatisticsSerializer(serializers.Serializer):
    """
    板块统计序列化器
    """
    
    total_sections = serializers.IntegerField()
    enabled_sections = serializers.IntegerField()
    disabled_sections = serializers.IntegerField()
    total_posts = serializers.IntegerField()
    sections = SectionDetailSerializer(many=True)
```

---

## 视图集设计

### SectionViewSet

#### apps/sections/views.py

```python
from rest_framework import viewsets, status
from rest_framework.decorators import action
from rest_framework.response import Response
from rest_framework.permissions import IsAuthenticated, AllowAny
from django.shortcuts import get_object_or_404
from django.db import transaction
from django.db.models import Sum

from .models import Section
from .serializers import (
    SectionListSerializer,
    SectionDetailSerializer,
    SectionCreateSerializer,
    SectionUpdateSerializer,
    SectionStatusSerializer,
    SectionReorderSerializer,
    SectionStatisticsSerializer
)
from apps.core.permissions import IsAdminUser
from apps.core.responses import success_response, error_response


class SectionViewSet(viewsets.ModelViewSet):
    """
    板块视图集
    
    提供板块的 CRUD 操作和管理功能
    """
    
    queryset = Section.objects.all()
    
    def get_queryset(self):
        """
        根据用户权限过滤查询集
        """
        queryset = super().get_queryset()
        
        # 普通用户只能看到启用的板块
        if not self.request.user or not self.request.user.is_staff:
            queryset = queryset.filter(is_enabled=True)
        
        return queryset.order_by('sort_order', 'id')
    
    def get_serializer_class(self):
        """
        根据操作选择序列化器
        """
        if self.action == 'list':
            return SectionListSerializer
        elif self.action == 'retrieve':
            return SectionDetailSerializer
        elif self.action == 'create':
            return SectionCreateSerializer
        elif self.action in ['update', 'partial_update']:
            return SectionUpdateSerializer
        else:
            return SectionDetailSerializer
    
    def get_permissions(self):
        """
        根据操作设置权限
        """
        if self.action in ['list', 'retrieve']:
            # 公开接口
            return [AllowAny()]
        else:
            # 管理员接口
            return [IsAuthenticated(), IsAdminUser()]
    
    def retrieve(self, request, *args, **kwargs):
        """
        获取板块详情（支持 ID 或 slug）
        """
        lookup_value = kwargs.get('pk')
        
        # 尝试通过 ID 查询
        if lookup_value.isdigit():
            instance = get_object_or_404(Section, id=lookup_value)
        else:
            # 通过 slug 查询
            instance = get_object_or_404(Section, slug=lookup_value)
        
        serializer = self.get_serializer(instance)
        return success_response(data=serializer.data)
    
    def list(self, request, *args, **kwargs):
        """
        获取板块列表
        """
        queryset = self.get_queryset()
        page = self.paginate_queryset(queryset)
        
        if page is not None:
            serializer = self.get_serializer(page, many=True)
            return self.get_paginated_response(serializer.data)
        
        serializer = self.get_serializer(queryset, many=True)
        return success_response(data=serializer.data)
    
    def create(self, request, *args, **kwargs):
        """
        创建板块（管理员）
        """
        serializer = self.get_serializer(data=request.data)
        serializer.is_valid(raise_exception=True)
        
        # 保存板块
        section = serializer.save()
        
        # 返回详细信息
        detail_serializer = SectionDetailSerializer(section)
        return success_response(
            data=detail_serializer.data,
            message='板块创建成功',
            status_code=status.HTTP_201_CREATED
        )
    
    def update(self, request, *args, **kwargs):
        """
        更新板块（管理员）
        """
        instance = self.get_object()
        serializer = self.get_serializer(instance, data=request.data, partial=True)
        serializer.is_valid(raise_exception=True)
        
        # 保存更新
        section = serializer.save()
        
        # 返回详细信息
        detail_serializer = SectionDetailSerializer(section)
        return success_response(
            data=detail_serializer.data,
            message='板块更新成功'
        )
    
    def destroy(self, request, *args, **kwargs):
        """
        删除板块（软删除，管理员）
        """
        instance = self.get_object()
        
        # 检查是否可以删除
        can_delete, reason = instance.can_be_deleted()
        if not can_delete:
            return error_response(
                message=reason,
                status_code=status.HTTP_400_BAD_REQUEST
            )
        
        # 软删除（禁用）
        instance.disable()
        
        return success_response(
            data={'id': instance.id, 'status': 'disabled'},
            message='板块已删除'
        )
    
    @action(detail=True, methods=['put'], permission_classes=[IsAuthenticated, IsAdminUser])
    def status(self, request, pk=None):
        """
        启用/禁用板块（管理员）
        
        PUT /api/sections/{id}/status
        Body: {"is_enabled": true/false}
        """
        instance = self.get_object()
        serializer = SectionStatusSerializer(
            data=request.data,
            context={'instance': instance}
        )
        serializer.is_valid(raise_exception=True)
        
        # 更新状态
        is_enabled = serializer.validated_data['is_enabled']
        
        if is_enabled:
            instance.enable()
            message = '板块已启用'
        else:
            try:
                instance.disable()
                message = '板块已禁用'
            except ValueError as e:
                return error_response(
                    message=str(e),
                    status_code=status.HTTP_400_BAD_REQUEST
                )
        
        # 返回更新后的信息
        detail_serializer = SectionDetailSerializer(instance)
        return success_response(
            data=detail_serializer.data,
            message=message
        )
    
    @action(detail=False, methods=['put'], permission_classes=[IsAuthenticated, IsAdminUser])
    def reorder(self, request):
        """
        批量调整板块排序（管理员）
        
        PUT /api/sections/reorder
        Body: {"order": [3, 1, 2, 4, 5, 6]}
        """
        serializer = SectionReorderSerializer(data=request.data)
        serializer.is_valid(raise_exception=True)
        
        order = serializer.validated_data['order']
        
        # 批量更新排序号
        with transaction.atomic():
            sections_to_update = []
            for index, section_id in enumerate(order, start=1):
                section = Section.objects.get(id=section_id)
                section.sort_order = index
                sections_to_update.append(section)
            
            # 批量更新
            Section.objects.bulk_update(sections_to_update, ['sort_order'])
        
        # 返回更新后的列表
        updated_sections = Section.objects.filter(id__in=order).order_by('sort_order')
        result = [
            {'id': s.id, 'slug': s.slug, 'sort_order': s.sort_order}
            for s in updated_sections
        ]
        
        return success_response(
            data={'updated_count': len(order), 'sections': result},
            message='板块排序已更新'
        )
    
    @action(detail=False, methods=['get'], permission_classes=[IsAuthenticated, IsAdminUser])
    def statistics(self, request):
        """
        获取板块统计（管理员）
        
        GET /api/sections/statistics
        """
        # 统计数据
        total_sections = Section.objects.count()
        enabled_sections = Section.objects.filter(is_enabled=True).count()
        disabled_sections = total_sections - enabled_sections
        total_posts = Section.objects.aggregate(Sum('posts_count'))['posts_count__sum'] or 0
        
        # 板块列表（带统计）
        sections = Section.objects.all().order_by('-posts_count')
        
        data = {
            'total_sections': total_sections,
            'enabled_sections': enabled_sections,
            'disabled_sections': disabled_sections,
            'total_posts': total_posts,
            'sections': SectionDetailSerializer(sections, many=True).data
        }
        
        return success_response(data=data)


class InternalSectionViewSet(viewsets.ViewSet):
    """
    内部接口视图集（供其他服务调用）
    """
    
    permission_classes = []  # 内部接口不需要认证
    
    @action(detail=True, methods=['post'])
    def increment_posts(self, request, pk=None):
        """
        更新板块帖子数（M3 内容服务调用）
        
        POST /internal/sections/{id}/increment-posts
        Body: {"value": 1}  # +1 或 -1
        """
        section = get_object_or_404(Section, id=pk)
        
        value = request.data.get('value', 1)
        
        # 验证 value
        if not isinstance(value, int) or value not in [-1, 1]:
            return error_response(
                message='value 必须是 1 或 -1',
                status_code=status.HTTP_400_BAD_REQUEST
            )
        
        # 更新帖子数
        section.increment_posts_count(value)
        
        return success_response(
            data={'id': section.id, 'posts_count': section.posts_count}
        )
```

---

## 权限控制

### 自定义权限类

#### apps/core/permissions.py

```python
from rest_framework.permissions import BasePermission


class IsAdminUser(BasePermission):
    """
    仅管理员可访问
    """
    
    def has_permission(self, request, view):
        """
        检查用户是否为管理员
        """
        # 从 Token 验证中间件获取用户角色
        user_role = getattr(request, 'user_role', None)
        return user_role == 'admin'
```

### Token 验证中间件

#### apps/core/middleware.py

```python
from django.utils.deprecation import MiddlewareMixin
from .service_client import AuthServiceClient
from .responses import error_response


class TokenAuthMiddleware(MiddlewareMixin):
    """
    Token 验证中间件
    
    从请求头中提取 Token，调用 M1 认证服务验证
    """
    
    def process_request(self, request):
        """
        处理请求，验证 Token
        """
        # 跳过不需要认证的路径
        if self._should_skip(request.path):
            return None
        
        # 提取 Token
        auth_header = request.META.get('HTTP_AUTHORIZATION', '')
        
        if not auth_header.startswith('Bearer '):
            return None
        
        token = auth_header.replace('Bearer ', '')
        
        # 调用 M1 认证服务验证
        try:
            auth_client = AuthServiceClient()
            result = auth_client.verify_token(token)
            
            if result['valid']:
                # 将用户信息附加到 request
                request.user_id = result['userId']
                request.username = result['username']
                request.user_role = result.get('role', 'user')
                request.user_token = token
            else:
                # Token 无效
                return error_response(
                    message='Token 无效或已过期',
                    status_code=401
                )
        
        except Exception as e:
            # 验证失败
            return error_response(
                message=f'Token 验证失败: {str(e)}',
                status_code=401
            )
        
        return None
    
    def _should_skip(self, path):
        """
        判断是否跳过验证
        """
        skip_paths = [
            '/api/sections',  # 公开接口
            '/health/',       # 健康检查
            '/internal/',     # 内部接口
        ]
        
        for skip_path in skip_paths:
            if path.startswith(skip_path):
                return True
        
        return False
```

---

## URL 路由配置

### apps/sections/urls.py

```python
from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import SectionViewSet, InternalSectionViewSet

# 创建路由器
router = DefaultRouter()
router.register(r'sections', SectionViewSet, basename='section')

# 内部接口路由器
internal_router = DefaultRouter()
internal_router.register(r'sections', InternalSectionViewSet, basename='internal-section')

urlpatterns = [
    # API 路由
    path('', include(router.urls)),
    
    # 内部接口路由
    path('internal/', include(internal_router.urls)),
]
```

### 生成的路由

| URL | 方法 | 视图方法 | 说明 |
|-----|------|----------|------|
| `/api/sections` | GET | list | 获取板块列表 |
| `/api/sections` | POST | create | 创建板块 |
| `/api/sections/{id}` | GET | retrieve | 获取板块详情 |
| `/api/sections/{id}` | PUT | update | 更新板块 |
| `/api/sections/{id}` | DELETE | destroy | 删除板块 |
| `/api/sections/{id}/status` | PUT | status | 启用/禁用板块 |
| `/api/sections/reorder` | PUT | reorder | 批量调整排序 |
| `/api/sections/statistics` | GET | statistics | 获取统计数据 |
| `/internal/sections/{id}/increment-posts` | POST | increment_posts | 更新帖子数 |

---

## 完整代码示例

### 创建板块完整流程

```python
# 1. 序列化器验证
serializer = SectionCreateSerializer(data={
    'slug': 'mobile',
    'name': '移动开发',
    'description': 'iOS、Android、Flutter',
    'color': '#4CAF50',
    'sort_order': 10
})

if serializer.is_valid():
    # 2. 保存到数据库
    section = serializer.save()
    
    # 3. 返回详细信息
    detail_serializer = SectionDetailSerializer(section)
    return Response(detail_serializer.data, status=201)
```

### 更新板块排序

```python
# 批量更新排序
with transaction.atomic():
    sections_to_update = []
    for index, section_id in enumerate([3, 1, 2, 4, 5, 6], start=1):
        section = Section.objects.get(id=section_id)
        section.sort_order = index
        sections_to_update.append(section)
    
    Section.objects.bulk_update(sections_to_update, ['sort_order'])
```

---

## 总结

### Django 模型设计亮点

- ✅ 使用 `managed=False` 避免 Django 管理已有表
- ✅ 模型方法封装业务逻辑（`can_be_deleted()`, `disable()`）
- ✅ 类方法提供常用查询（`get_enabled_sections()`）
- ✅ 完善的字段验证器

### 序列化器设计亮点

- ✅ 分离不同场景的序列化器（List/Detail/Create/Update）
- ✅ 自定义验证方法（`validate_slug()`, `validate_name()`）
- ✅ `SerializerMethodField` 动态生成字段（`icon_url`）

### 视图集设计亮点

- ✅ `get_queryset()` 根据权限过滤
- ✅ `get_serializer_class()` 根据操作选择序列化器
- ✅ `get_permissions()` 灵活的权限控制
- ✅ 自定义 action（`@action`）扩展功能
- ✅ 支持 ID 和 slug 双重查询

---

**文档版本**: v1.0  
**创建日期**: 2025-11-04  
**最后更新**: 2025-11-04


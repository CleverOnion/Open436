# M2 用户管理服务 - Django 模型设计

## 文档信息

**服务名称**: 用户管理服务 (user-service)  
**框架**: Django 4.2+ / Django REST Framework 3.14+  
**版本**: v1.0

---

## 目录

1. [Models 设计](#models-设计)
2. [Serializers 设计](#serializers-设计)
3. [ViewSets 设计](#viewsets-设计)
4. [URL 路由配置](#url-路由配置)
5. [权限控制](#权限控制)
6. [过滤器和分页](#过滤器和分页)

---

## Models 设计

### 1. 基础抽象模型

```python
# apps/core/models.py
from django.db import models

class TimeStampedModel(models.Model):
    """带时间戳的抽象基类"""
    created_at = models.DateTimeField(auto_now_add=True, verbose_name="创建时间")
    updated_at = models.DateTimeField(auto_now=True, verbose_name="更新时间")

    class Meta:
        abstract = True
        ordering = ['-created_at']
```

### 2. UserProfile 模型

```python
# apps/users/models.py
from django.db import models
from apps.core.models import TimeStampedModel

class UserProfile(TimeStampedModel):
    """用户资料模型"""
    user_id = models.IntegerField(
        primary_key=True,
        verbose_name="用户ID",
        help_text="关联 M1 认证服务的 users_auth.id"
    )
    nickname = models.CharField(
        max_length=20,
        verbose_name="昵称",
        help_text="用户昵称，2-20字符"
    )
    avatar_url = models.URLField(
        max_length=500,
        blank=True,
        null=True,
        verbose_name="头像URL",
        help_text="头像图片URL，指向 M7 文件服务"
    )
    bio = models.TextField(
        max_length=200,
        blank=True,
        null=True,
        verbose_name="个人简介",
        help_text="个人简介，最大200字符"
    )
    nickname_updated_at = models.DateTimeField(
        blank=True,
        null=True,
        verbose_name="昵称修改时间",
        help_text="昵称最后一次修改的时间，用于30天限制"
    )

    class Meta:
        db_table = 'users_profile'
        verbose_name = '用户资料'
        verbose_name_plural = '用户资料'
        ordering = ['-created_at']
        indexes = [
            models.Index(fields=['nickname'], name='idx_users_profile_nickname'),
            models.Index(fields=['-created_at'], name='idx_users_profile_created_at'),
        ]

    def __str__(self):
        return f"{self.nickname} ({self.user_id})"

    def __repr__(self):
        return f"<UserProfile: {self.user_id} - {self.nickname}>"

    @property
    def can_update_nickname(self):
        """检查是否可以修改昵称（30天限制）"""
        if self.nickname_updated_at is None:
            return True
        from django.utils import timezone
        days_since_update = (timezone.now() - self.nickname_updated_at).days
        return days_since_update >= 30
```

### 3. UserStatistics 模型

```python
# apps/users/models.py
class UserStatistics(models.Model):
    """用户统计模型"""
    user_id = models.OneToOneField(
        UserProfile,
        on_delete=models.CASCADE,
        primary_key=True,
        related_name='statistics',
        verbose_name="用户ID"
    )
    posts_count = models.IntegerField(
        default=0,
        verbose_name="发帖数",
        help_text="用户发布的帖子总数"
    )
    replies_count = models.IntegerField(
        default=0,
        verbose_name="回复数",
        help_text="用户发布的回复总数"
    )
    likes_received = models.IntegerField(
        default=0,
        verbose_name="获赞数",
        help_text="用户获得的点赞总数"
    )
    favorites_received = models.IntegerField(
        default=0,
        verbose_name="获收藏数",
        help_text="用户获得的收藏总数"
    )
    updated_at = models.DateTimeField(
        auto_now=True,
        verbose_name="更新时间"
    )

    class Meta:
        db_table = 'user_statistics'
        verbose_name = '用户统计'
        verbose_name_plural = '用户统计'
        indexes = [
            models.Index(fields=['-posts_count'], name='idx_user_statistics_posts_count'),
            models.Index(fields=['-likes_received'], name='idx_user_statistics_likes_received'),
        ]

    def __str__(self):
        return f"统计 - {self.user_id.nickname}"

    def __repr__(self):
        return f"<UserStatistics: {self.user_id.user_id} - posts:{self.posts_count}>"

    def increment_field(self, field_name, value=1):
        """原子性增加字段值"""
        from django.db.models import F
        UserStatistics.objects.filter(user_id=self.user_id).update(
            **{field_name: F(field_name) + value}
        )
        self.refresh_from_db()
```

### 4. Model Managers（自定义查询管理器）

```python
# apps/users/managers.py
from django.db import models

class UserProfileManager(models.Manager):
    """用户资料查询管理器"""

    def with_statistics(self):
        """预加载统计数据"""
        return self.select_related('statistics')

    def active_users(self):
        """获取活跃用户（发帖数 > 0）"""
        return self.filter(statistics__posts_count__gt=0)

    def search(self, keyword):
        """搜索用户（按昵称）"""
        return self.filter(nickname__icontains=keyword)


class UserStatisticsManager(models.Manager):
    """用户统计查询管理器"""

    def top_posters(self, limit=10):
        """发帖排行榜"""
        return self.order_by('-posts_count')[:limit]

    def top_liked(self, limit=10):
        """获赞排行榜"""
        return self.order_by('-likes_received')[:limit]


# 在模型中使用
class UserProfile(TimeStampedModel):
    # ... 字段定义 ...

    objects = UserProfileManager()

class UserStatistics(models.Model):
    # ... 字段定义 ...

    objects = UserStatisticsManager()
```

---

## Serializers 设计

### 1. 用户资料序列化器

```python
# apps/users/serializers.py
from rest_framework import serializers
from .models import UserProfile, UserStatistics

class UserStatisticsSerializer(serializers.ModelSerializer):
    """用户统计序列化器"""

    class Meta:
        model = UserStatistics
        fields = ['posts_count', 'replies_count', 'likes_received', 'favorites_received', 'updated_at']
        read_only_fields = fields


class UserProfileSerializer(serializers.ModelSerializer):
    """用户资料基础序列化器"""

    class Meta:
        model = UserProfile
        fields = ['user_id', 'nickname', 'avatar_url', 'bio', 'created_at', 'updated_at']
        read_only_fields = ['user_id', 'created_at', 'updated_at']

    def validate_nickname(self, value):
        """验证昵称"""
        if len(value) < 2:
            raise serializers.ValidationError("昵称长度至少为2个字符")
        if len(value) > 20:
            raise serializers.ValidationError("昵称长度不能超过20个字符")
        # 检查是否包含特殊字符
        import re
        if not re.match(r'^[\w\u4e00-\u9fa5_-]+$', value):
            raise serializers.ValidationError("昵称只能包含中文、英文、数字、下划线和连字符")
        return value

    def validate_bio(self, value):
        """验证个人简介"""
        if value and len(value) > 200:
            raise serializers.ValidationError("个人简介不能超过200个字符")
        return value


class UserProfileDetailSerializer(serializers.ModelSerializer):
    """用户资料详细序列化器（包含统计数据）"""
    statistics = UserStatisticsSerializer(read_only=True)

    class Meta:
        model = UserProfile
        fields = ['user_id', 'nickname', 'avatar_url', 'bio', 'created_at', 'updated_at', 'statistics']
        read_only_fields = ['user_id', 'created_at', 'updated_at', 'statistics']


class UserProfileUpdateSerializer(serializers.ModelSerializer):
    """用户资料更新序列化器"""

    class Meta:
        model = UserProfile
        fields = ['nickname', 'avatar_url', 'bio']

    def validate_nickname(self, value):
        """验证昵称修改频率"""
        instance = self.instance
        if instance and 'nickname' in self.initial_data:
            if instance.nickname != value and not instance.can_update_nickname:
                raise serializers.ValidationError("昵称每30天只能修改一次")

        # 调用基础验证
        if len(value) < 2 or len(value) > 20:
            raise serializers.ValidationError("昵称长度必须在2-20个字符之间")

        return value

    def update(self, instance, validated_data):
        """更新用户资料"""
        if 'nickname' in validated_data and instance.nickname != validated_data['nickname']:
            from django.utils import timezone
            instance.nickname_updated_at = timezone.now()

        return super().update(instance, validated_data)


class UserProfileListSerializer(serializers.ModelSerializer):
    """用户列表序列化器（管理员使用）"""
    posts_count = serializers.IntegerField(source='statistics.posts_count', read_only=True)
    replies_count = serializers.IntegerField(source='statistics.replies_count', read_only=True)

    class Meta:
        model = UserProfile
        fields = ['user_id', 'nickname', 'avatar_url', 'created_at', 'posts_count', 'replies_count']
```

### 2. 用户创建序列化器（管理员）

```python
# apps/users/serializers.py
class UserCreateSerializer(serializers.Serializer):
    """用户创建序列化器（管理员）"""
    username = serializers.CharField(min_length=3, max_length=20, required=True)
    password = serializers.CharField(min_length=6, max_length=32, required=True, write_only=True)
    role = serializers.ChoiceField(choices=['user', 'admin'], default='user')
    nickname = serializers.CharField(min_length=2, max_length=20, required=True)
    avatar_url = serializers.URLField(required=False, allow_blank=True)
    bio = serializers.CharField(max_length=200, required=False, allow_blank=True)

    def create(self, validated_data):
        """创建用户（调用 M1 服务）"""
        import requests
        from django.db import transaction

        # 1. 调用 M1 创建认证账号
        auth_data = {
            'username': validated_data['username'],
            'password': validated_data['password'],
            'role': validated_data.get('role', 'user')
        }

        response = requests.post(
            'http://auth-service:8001/api/auth/users',
            json=auth_data,
            timeout=5
        )

        if response.status_code != 201:
            raise serializers.ValidationError({'error': '创建认证账号失败', 'details': response.json()})

        user_id = response.json()['data']['user_id']

        # 2. 创建用户资料和统计记录
        with transaction.atomic():
            profile = UserProfile.objects.create(
                user_id=user_id,
                nickname=validated_data['nickname'],
                avatar_url=validated_data.get('avatar_url', ''),
                bio=validated_data.get('bio', '')
            )
            UserStatistics.objects.create(user_id=profile.user_id)

        return profile
```

---

## ViewSets 设计

### 1. 用户资料 ViewSet

```python
# apps/users/views.py
from rest_framework import viewsets, status
from rest_framework.decorators import action
from rest_framework.response import Response
from rest_framework.permissions import IsAuthenticatedOrReadOnly
from django.utils import timezone
from .models import UserProfile, UserStatistics
from .serializers import (
    UserProfileDetailSerializer,
    UserProfileUpdateSerializer,
    UserStatisticsSerializer
)
from .permissions import IsOwnerOrAdmin

class UserViewSet(viewsets.ModelViewSet):
    """用户资料 ViewSet"""
    queryset = UserProfile.objects.select_related('statistics').all()
    serializer_class = UserProfileDetailSerializer
    lookup_field = 'user_id'
    permission_classes = [IsAuthenticatedOrReadOnly]

    def get_serializer_class(self):
        """根据操作选择序列化器"""
        if self.action in ['update', 'partial_update']:
            return UserProfileUpdateSerializer
        return UserProfileDetailSerializer

    def get_permissions(self):
        """根据操作设置权限"""
        if self.action in ['update', 'partial_update', 'destroy']:
            return [IsOwnerOrAdmin()]
        return super().get_permissions()

    def retrieve(self, request, user_id=None):
        """获取用户信息"""
        user = self.get_object()
        serializer = self.get_serializer(user)
        return Response({
            'code': 200,
            'message': 'success',
            'data': serializer.data,
            'timestamp': int(timezone.now().timestamp() * 1000)
        })

    @action(detail=True, methods=['put', 'patch'], url_path='profile')
    def update_profile(self, request, user_id=None):
        """更新用户资料"""
        user = self.get_object()
        serializer = UserProfileUpdateSerializer(user, data=request.data, partial=True)
        serializer.is_valid(raise_exception=True)
        serializer.save()

        return Response({
            'code': 200,
            'message': '用户资料已更新',
            'data': UserProfileDetailSerializer(user).data,
            'timestamp': int(timezone.now().timestamp() * 1000)
        })

    @action(detail=True, methods=['get'], url_path='statistics')
    def statistics(self, request, user_id=None):
        """获取用户统计数据"""
        user = self.get_object()
        if not hasattr(user, 'statistics'):
            UserStatistics.objects.create(user_id=user.user_id)
            user.refresh_from_db()

        serializer = UserStatisticsSerializer(user.statistics)
        return Response({
            'code': 200,
            'message': 'success',
            'data': serializer.data,
            'timestamp': int(timezone.now().timestamp() * 1000)
        })

    @action(detail=True, methods=['get'], url_path='posts')
    def posts(self, request, user_id=None):
        """获取用户发帖历史（调用 M3 服务）"""
        import requests

        response = requests.get(
            f'http://content-service:8003/api/posts',
            params={'author_id': user_id, **request.query_params.dict()},
            timeout=5
        )

        if response.status_code == 200:
            return Response(response.json())
        else:
            return Response({
                'code': 500,
                'message': '获取发帖历史失败'
            }, status=500)

    @action(detail=True, methods=['get'], url_path='replies')
    def replies(self, request, user_id=None):
        """获取用户回复历史（调用 M4 服务）"""
        import requests

        response = requests.get(
            f'http://interaction-service:8004/api/replies',
            params={'author_id': user_id, **request.query_params.dict()},
            timeout=5
        )

        if response.status_code == 200:
            return Response(response.json())
        else:
            return Response({
                'code': 500,
                'message': '获取回复历史失败'
            }, status=500)

    @action(detail=True, methods=['post'], url_path='avatar')
    def upload_avatar(self, request, user_id=None):
        """上传用户头像（调用 M7 文件服务）"""
        user = self.get_object()

        avatar_file = request.FILES.get('avatar')
        if not avatar_file:
            return Response({
                'code': 400,
                'message': '请上传头像文件'
            }, status=400)

        import requests
        files = {'file': avatar_file}
        data = {'file_type': 'avatar', 'user_id': user_id}

        response = requests.post(
            'http://file-service:8007/api/files/upload',
            files=files,
            data=data,
            timeout=10
        )

        if response.status_code == 200:
            file_data = response.json()['data']
            user.avatar_url = file_data['file_url']
            user.save()

            return Response({
                'code': 200,
                'message': '头像上传成功',
                'data': {'avatar_url': user.avatar_url},
                'timestamp': int(timezone.now().timestamp() * 1000)
            })
        else:
            return Response({
                'code': 500,
                'message': '文件上传失败'
            }, status=500)
```

### 2. 管理员 ViewSet

```python
# apps/users/views.py
from rest_framework.permissions import IsAdminUser
from django_filters.rest_framework import DjangoFilterBackend
from rest_framework.filters import SearchFilter, OrderingFilter
from .serializers import UserProfileListSerializer, UserCreateSerializer

class AdminUserViewSet(viewsets.ModelViewSet):
    """管理员用户管理 ViewSet"""
    queryset = UserProfile.objects.select_related('statistics').all()
    serializer_class = UserProfileListSerializer
    permission_classes = [IsAdminUser]
    filter_backends = [DjangoFilterBackend, SearchFilter, OrderingFilter]
    search_fields = ['nickname', 'user_id']
    ordering_fields = ['created_at', 'statistics__posts_count', 'statistics__replies_count']
    ordering = ['-created_at']

    def get_serializer_class(self):
        if self.action == 'create':
            return UserCreateSerializer
        return UserProfileListSerializer

    def create(self, request):
        """创建用户"""
        serializer = UserCreateSerializer(data=request.data)
        serializer.is_valid(raise_exception=True)
        profile = serializer.save()

        return Response({
            'code': 201,
            'message': '用户创建成功',
            'data': UserProfileDetailSerializer(profile).data,
            'timestamp': int(timezone.now().timestamp() * 1000)
        }, status=201)
```

### 3. 内部服务 ViewSet

```python
# apps/users/views.py
from rest_framework.decorators import api_view, permission_classes
from apps.core.permissions import IsInternalService
from django.db.models import F

@api_view(['POST'])
@permission_classes([IsInternalService])
def batch_get_users(request):
    """批量获取用户信息（内部服务）"""
    user_ids = request.data.get('user_ids', [])

    if len(user_ids) > 100:
        return Response({
            'code': 400,
            'message': '最多支持100个用户ID'
        }, status=400)

    users = UserProfile.objects.filter(user_id__in=user_ids)
    serializer = UserProfileSerializer(users, many=True)

    return Response({
        'code': 200,
        'message': 'success',
        'data': serializer.data,
        'timestamp': int(timezone.now().timestamp() * 1000)
    })


@api_view(['POST'])
@permission_classes([IsInternalService])
def increment_statistics(request, user_id):
    """更新用户统计数据（内部服务）"""
    field = request.data.get('field')
    value = request.data.get('value', 0)

    allowed_fields = ['posts_count', 'replies_count', 'likes_received', 'favorites_received']
    if field not in allowed_fields:
        return Response({
            'code': 400,
            'message': f'无效的字段名，允许的字段：{allowed_fields}'
        }, status=400)

    stats, created = UserStatistics.objects.get_or_create(user_id=user_id)

    # 使用 F 表达式原子性更新
    UserStatistics.objects.filter(user_id=user_id).update(
        **{field: F(field) + value}
    )

    stats.refresh_from_db()
    serializer = UserStatisticsSerializer(stats)

    return Response({
        'code': 200,
        'message': '统计数据已更新',
        'data': serializer.data,
        'timestamp': int(timezone.now().timestamp() * 1000)
    })
```

---

## URL 路由配置

```python
# apps/users/urls.py
from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import UserViewSet, AdminUserViewSet, batch_get_users, increment_statistics

app_name = 'users'

# 公开路由
router = DefaultRouter()
router.register(r'users', UserViewSet, basename='user')

# 管理员路由
admin_router = DefaultRouter()
admin_router.register(r'users', AdminUserViewSet, basename='admin-user')

urlpatterns = [
    # 公开接口
    path('api/', include(router.urls)),

    # 管理员接口
    path('api/admin/', include(admin_router.urls)),

    # 内部服务接口
    path('internal/users/batch', batch_get_users, name='batch-get-users'),
    path('internal/users/<int:user_id>/statistics/increment', increment_statistics, name='increment-statistics'),
]
```

---

## 权限控制

```python
# apps/users/permissions.py
from rest_framework import permissions

class IsOwnerOrAdmin(permissions.BasePermission):
    """仅本人或管理员可以操作"""

    def has_object_permission(self, request, view, obj):
        # 读取权限允许所有人
        if request.method in permissions.SAFE_METHODS:
            return True

        # 写入权限仅本人或管理员
        user_data = getattr(request, 'user', {})
        user_id = user_data.get('user_id')
        is_admin = user_data.get('is_admin', False)

        return obj.user_id == user_id or is_admin


# apps/core/permissions.py
class IsInternalService(permissions.BasePermission):
    """内部服务权限（通过 API Key 验证）"""

    def has_permission(self, request, view):
        api_key = request.META.get('HTTP_X_INTERNAL_API_KEY', '')
        expected_key = settings.INTERNAL_API_KEY
        return api_key == expected_key
```

---

## 过滤器和分页

### 自定义过滤器

```python
# apps/users/filters.py
import django_filters
from .models import UserProfile

class UserProfileFilter(django_filters.FilterSet):
    """用户资料过滤器"""
    nickname = django_filters.CharFilter(lookup_expr='icontains')
    created_after = django_filters.DateTimeFilter(field_name='created_at', lookup_expr='gte')
    created_before = django_filters.DateTimeFilter(field_name='created_at', lookup_expr='lte')
    min_posts = django_filters.NumberFilter(field_name='statistics__posts_count', lookup_expr='gte')

    class Meta:
        model = UserProfile
        fields = ['nickname', 'created_after', 'created_before', 'min_posts']
```

### 自定义分页器

```python
# apps/users/pagination.py
from rest_framework.pagination import PageNumberPagination

class UserPagination(PageNumberPagination):
    """用户列表分页器"""
    page_size = 20
    page_size_query_param = 'page_size'
    max_page_size = 100
```

---

**文档版本**: v1.0  
**创建日期**: 2025-10-27  
**最后更新**: 2025-10-27

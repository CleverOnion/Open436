# M2 用户管理服务 - API 接口设计

## 文档信息

**服务名称**: 用户管理服务 (user-service)  
**Base URL**: `http://localhost:8002/api`  
**API 版本**: v1  
**认证方式**: JWT Token (Bearer)

---

## 目录

1. [接口概述](#接口概述)
2. [认证与鉴权](#认证与鉴权)
3. [通用响应格式](#通用响应格式)
4. [用户资料接口](#用户资料接口)
5. [用户统计接口](#用户统计接口)
6. [用户活动历史接口](#用户活动历史接口)
7. [管理员接口](#管理员接口)
8. [内部服务接口](#内部服务接口)
9. [错误码说明](#错误码说明)

---

## 接口概述

### 接口分类

| 分类     | 路径前缀                | 说明                 |
| -------- | ----------------------- | -------------------- |
| 用户资料 | `/api/users`            | 用户信息的 CRUD 操作 |
| 用户活动 | `/api/users/{id}/posts` | 用户发帖、回复历史   |
| 管理员   | `/api/admin/users`      | 管理员管理用户       |
| 内部服务 | `/internal/users`       | 仅供其他服务调用     |

### 技术规范

- **协议**: HTTP/HTTPS
- **数据格式**: JSON
- **字符编码**: UTF-8
- **分页**: 使用 `page` 和 `page_size` 参数
- **排序**: 使用 `ordering` 参数
- **过滤**: 使用查询参数

---

## 认证与鉴权

### JWT Token 认证

#### Header 格式

```http
Authorization: Bearer <JWT_TOKEN>
```

#### Token 获取

从 M1 认证服务获取:

```http
POST http://auth-service:8001/api/auth/login
Content-Type: application/json

{
  "username": "alice",
  "password": "password123"
}

Response:
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user_id": 1,
  "expires_in": 7200
}
```

#### Token 验证（Django 实现）

```python
# apps/core/authentication.py
from rest_framework.authentication import BaseAuthentication
from rest_framework.exceptions import AuthenticationFailed
import jwt
import requests

class JWTAuthentication(BaseAuthentication):
    def authenticate(self, request):
        auth_header = request.META.get('HTTP_AUTHORIZATION', '')

        if not auth_header.startswith('Bearer '):
            return None

        token = auth_header.split(' ')[1]

        try:
            # 调用 M1 服务验证 Token
            response = requests.post(
                'http://auth-service:8001/api/auth/verify',
                json={'token': token},
                timeout=2
            )

            if response.status_code == 200:
                user_data = response.json()
                return (user_data, token)
            else:
                raise AuthenticationFailed('Invalid token')
        except Exception as e:
            raise AuthenticationFailed(f'Token verification failed: {str(e)}')
```

---

## 通用响应格式

### 成功响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    // 具体数据
  },
  "timestamp": 1698384000000
}
```

### 错误响应

```json
{
  "code": 400,
  "message": "Nickname length must be between 2 and 20 characters",
  "errors": {
    "nickname": ["长度必须在 2-20 个字符之间"]
  },
  "timestamp": 1698384000000
}
```

### 分页响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "count": 100,
    "next": "http://localhost:8002/api/users?page=2",
    "previous": null,
    "results": [
      // 数据列表
    ]
  },
  "timestamp": 1698384000000
}
```

---

## 用户资料接口

### 1. 获取用户信息

获取用户的完整信息（资料 + 统计数据）

**接口**: `GET /api/users/{user_id}`

**权限**: 公开（无需登录）

**Path 参数**:

| 参数    | 类型    | 必填 | 说明    |
| ------- | ------- | ---- | ------- |
| user_id | integer | 是   | 用户 ID |

**请求示例**:

```http
GET /api/users/1 HTTP/1.1
Host: localhost:8002
```

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "user_id": 1,
    "nickname": "Alice",
    "avatar_url": "https://cdn.example.com/avatars/alice.jpg",
    "bio": "热爱编程的开发者",
    "created_at": "2025-01-01T10:00:00Z",
    "updated_at": "2025-10-27T15:30:00Z",
    "statistics": {
      "posts_count": 15,
      "replies_count": 42,
      "likes_received": 128,
      "favorites_received": 35
    }
  },
  "timestamp": 1698384000000
}
```

**Django 实现**:

```python
# apps/users/views.py
from rest_framework import viewsets
from rest_framework.response import Response
from .models import UserProfile
from .serializers import UserProfileDetailSerializer

class UserViewSet(viewsets.ReadOnlyModelViewSet):
    queryset = UserProfile.objects.select_related('statistics').all()
    serializer_class = UserProfileDetailSerializer
    lookup_field = 'user_id'

    def retrieve(self, request, user_id=None):
        user = self.get_object()
        serializer = self.get_serializer(user)
        return Response({
            'code': 200,
            'message': 'success',
            'data': serializer.data,
            'timestamp': int(timezone.now().timestamp() * 1000)
        })
```

---

### 2. 更新用户资料

更新用户的昵称、头像或简介

**接口**: `PUT /api/users/{user_id}/profile`

**权限**: 仅本人或管理员

**Path 参数**:

| 参数    | 类型    | 必填 | 说明    |
| ------- | ------- | ---- | ------- |
| user_id | integer | 是   | 用户 ID |

**请求 Body**:

| 字段       | 类型   | 必填 | 说明                      |
| ---------- | ------ | ---- | ------------------------- |
| nickname   | string | 否   | 昵称（2-20 字符）         |
| avatar_url | string | 否   | 头像 URL                  |
| bio        | string | 否   | 个人简介（最大 200 字符） |

**请求示例**:

```http
PUT /api/users/1/profile HTTP/1.1
Host: localhost:8002
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "nickname": "Alice Chen",
  "bio": "Full Stack Developer | Python & Django"
}
```

**响应示例**:

```json
{
  "code": 200,
  "message": "用户资料已更新",
  "data": {
    "user_id": 1,
    "nickname": "Alice Chen",
    "avatar_url": "https://cdn.example.com/avatars/alice.jpg",
    "bio": "Full Stack Developer | Python & Django",
    "updated_at": "2025-10-27T16:00:00Z"
  },
  "timestamp": 1698384000000
}
```

**业务规则**:

- 昵称 30 天内只能修改一次（管理员除外）
- 昵称长度：2-20 字符
- 个人简介最大 200 字符

**Django 实现**:

```python
# apps/users/views.py
from rest_framework.decorators import action
from rest_framework.exceptions import ValidationError
from django.utils import timezone
from datetime import timedelta

class UserViewSet(viewsets.ModelViewSet):
    @action(detail=True, methods=['put'], url_path='profile')
    def update_profile(self, request, user_id=None):
        user = self.get_object()

        # 权限检查
        if request.user['user_id'] != user.user_id and not request.user.get('is_admin'):
            return Response({
                'code': 403,
                'message': '权限不足'
            }, status=403)

        # 昵称修改频率限制
        if 'nickname' in request.data:
            if user.nickname_updated_at:
                days_since_update = (timezone.now() - user.nickname_updated_at).days
                if days_since_update < 30 and not request.user.get('is_admin'):
                    return Response({
                        'code': 400,
                        'message': '昵称每 30 天只能修改一次'
                    }, status=400)
            user.nickname_updated_at = timezone.now()

        serializer = UserProfileUpdateSerializer(user, data=request.data, partial=True)
        serializer.is_valid(raise_exception=True)
        serializer.save()

        return Response({
            'code': 200,
            'message': '用户资料已更新',
            'data': serializer.data,
            'timestamp': int(timezone.now().timestamp() * 1000)
        })
```

---

### 3. 上传用户头像

上传用户头像图片（调用 M7 文件服务）

**接口**: `POST /api/users/{user_id}/avatar`

**权限**: 仅本人或管理员

**Path 参数**:

| 参数    | 类型    | 必填 | 说明    |
| ------- | ------- | ---- | ------- |
| user_id | integer | 是   | 用户 ID |

**请求 Body**: `multipart/form-data`

| 字段   | 类型 | 必填 | 说明                                  |
| ------ | ---- | ---- | ------------------------------------- |
| avatar | file | 是   | 头像图片文件（JPG/PNG/GIF，最大 2MB） |

**请求示例**:

```http
POST /api/users/1/avatar HTTP/1.1
Host: localhost:8002
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: multipart/form-data; boundary=----WebKitFormBoundary

------WebKitFormBoundary
Content-Disposition: form-data; name="avatar"; filename="avatar.jpg"
Content-Type: image/jpeg

[binary data]
------WebKitFormBoundary--
```

**响应示例**:

```json
{
  "code": 200,
  "message": "头像上传成功",
  "data": {
    "avatar_url": "https://cdn.example.com/avatars/abc123.jpg"
  },
  "timestamp": 1698384000000
}
```

**Django 实现**:

```python
# apps/users/views.py
import requests

class UserViewSet(viewsets.ModelViewSet):
    @action(detail=True, methods=['post'], url_path='avatar')
    def upload_avatar(self, request, user_id=None):
        user = self.get_object()

        # 权限检查
        if request.user['user_id'] != user.user_id and not request.user.get('is_admin'):
            return Response({'code': 403, 'message': '权限不足'}, status=403)

        avatar_file = request.FILES.get('avatar')
        if not avatar_file:
            return Response({'code': 400, 'message': '请上传头像文件'}, status=400)

        # 调用 M7 文件服务上传
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

---

## 用户统计接口

### 4. 获取用户统计数据

获取用户的详细统计信息

**接口**: `GET /api/users/{user_id}/statistics`

**权限**: 公开

**Path 参数**:

| 参数    | 类型    | 必填 | 说明    |
| ------- | ------- | ---- | ------- |
| user_id | integer | 是   | 用户 ID |

**请求示例**:

```http
GET /api/users/1/statistics HTTP/1.1
Host: localhost:8002
```

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "user_id": 1,
    "posts_count": 15,
    "replies_count": 42,
    "likes_received": 128,
    "favorites_received": 35,
    "updated_at": "2025-10-27T15:30:00Z"
  },
  "timestamp": 1698384000000
}
```

---

## 用户活动历史接口

### 5. 获取用户发帖历史

获取用户发布的所有帖子列表

**接口**: `GET /api/users/{user_id}/posts`

**权限**: 公开

**Path 参数**:

| 参数    | 类型    | 必填 | 说明    |
| ------- | ------- | ---- | ------- |
| user_id | integer | 是   | 用户 ID |

**Query 参数**:

| 参数      | 类型    | 必填 | 默认值      | 说明                                               |
| --------- | ------- | ---- | ----------- | -------------------------------------------------- |
| page      | integer | 否   | 1           | 页码                                               |
| page_size | integer | 否   | 20          | 每页数量（最大 50）                                |
| ordering  | string  | 否   | -created_at | 排序字段（created_at, likes_count, replies_count） |

**请求示例**:

```http
GET /api/users/1/posts?page=1&page_size=20&ordering=-likes_count HTTP/1.1
Host: localhost:8002
```

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "count": 15,
    "next": null,
    "previous": null,
    "results": [
      {
        "post_id": 101,
        "title": "Django REST Framework 最佳实践",
        "content_preview": "本文介绍 DRF 的最佳实践...",
        "section_name": "技术交流",
        "created_at": "2025-10-25T10:00:00Z",
        "views_count": 256,
        "replies_count": 12,
        "likes_count": 45
      }
    ]
  },
  "timestamp": 1698384000000
}
```

**说明**: 此接口需要调用 M3 内容管理服务获取帖子数据

---

### 6. 获取用户回复历史

获取用户发布的所有回复列表

**接口**: `GET /api/users/{user_id}/replies`

**权限**: 公开

**Path 参数**:

| 参数    | 类型    | 必填 | 说明    |
| ------- | ------- | ---- | ------- |
| user_id | integer | 是   | 用户 ID |

**Query 参数**:

| 参数      | 类型    | 必填 | 默认值 | 说明     |
| --------- | ------- | ---- | ------ | -------- |
| page      | integer | 否   | 1      | 页码     |
| page_size | integer | 否   | 20     | 每页数量 |

**请求示例**:

```http
GET /api/users/1/replies?page=1&page_size=20 HTTP/1.1
Host: localhost:8002
```

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "count": 42,
    "next": "http://localhost:8002/api/users/1/replies?page=2",
    "previous": null,
    "results": [
      {
        "reply_id": 501,
        "content": "很棒的分享，学到了很多！",
        "post_id": 101,
        "post_title": "Django REST Framework 最佳实践",
        "created_at": "2025-10-26T14:30:00Z",
        "likes_count": 8
      }
    ]
  },
  "timestamp": 1698384000000
}
```

**说明**: 此接口需要调用 M4 互动评论服务获取回复数据

---

## 管理员接口

### 7. 查看用户列表（管理员）

管理员查看所有用户列表，支持搜索和筛选

**接口**: `GET /api/admin/users`

**权限**: 仅管理员

**Query 参数**:

| 参数      | 类型    | 必填 | 默认值      | 说明                       |
| --------- | ------- | ---- | ----------- | -------------------------- |
| page      | integer | 否   | 1           | 页码                       |
| page_size | integer | 否   | 50          | 每页数量                   |
| search    | string  | 否   | -           | 搜索关键词（昵称或用户名） |
| ordering  | string  | 否   | -created_at | 排序字段                   |

**请求示例**:

```http
GET /api/admin/users?search=Alice&page=1&page_size=50 HTTP/1.1
Host: localhost:8002
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "count": 1,
    "next": null,
    "previous": null,
    "results": [
      {
        "user_id": 1,
        "username": "alice",
        "nickname": "Alice",
        "avatar_url": "https://cdn.example.com/avatars/alice.jpg",
        "status": "active",
        "created_at": "2025-01-01T10:00:00Z",
        "posts_count": 15,
        "replies_count": 42
      }
    ]
  },
  "timestamp": 1698384000000
}
```

---

### 8. 创建用户（管理员）

管理员创建新用户（包括认证账号和用户资料）

**接口**: `POST /api/admin/users`

**权限**: 仅管理员

**请求 Body**:

| 字段       | 类型   | 必填 | 说明                      |
| ---------- | ------ | ---- | ------------------------- |
| username   | string | 是   | 用户名（3-20 字符，唯一） |
| password   | string | 是   | 初始密码（6-32 字符）     |
| role       | string | 是   | 用户角色（user/admin）    |
| nickname   | string | 是   | 昵称（2-20 字符）         |
| avatar_url | string | 否   | 头像 URL                  |
| bio        | string | 否   | 个人简介                  |

**请求示例**:

```http
POST /api/admin/users HTTP/1.1
Host: localhost:8002
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "username": "bob",
  "password": "password123",
  "role": "user",
  "nickname": "Bob",
  "bio": "Python Developer"
}
```

**响应示例**:

```json
{
  "code": 201,
  "message": "用户创建成功",
  "data": {
    "user_id": 2,
    "username": "bob",
    "nickname": "Bob",
    "avatar_url": null,
    "bio": "Python Developer",
    "created_at": "2025-10-27T16:30:00Z"
  },
  "timestamp": 1698384000000
}
```

**流程说明**:

1. M2 先调用 M1 服务创建认证账号
2. M1 返回 `user_id`
3. M2 使用该 `user_id` 创建用户资料和统计记录

**Django 实现**:

```python
# apps/users/views.py
import requests
from django.db import transaction

class AdminUserViewSet(viewsets.ModelViewSet):
    @transaction.atomic
    def create(self, request):
        # 1. 调用 M1 创建认证账号
        auth_data = {
            'username': request.data['username'],
            'password': request.data['password'],
            'role': request.data.get('role', 'user')
        }

        response = requests.post(
            'http://auth-service:8001/api/auth/users',
            json=auth_data,
            timeout=5
        )

        if response.status_code != 201:
            return Response({
                'code': 400,
                'message': '创建认证账号失败',
                'errors': response.json()
            }, status=400)

        user_id = response.json()['data']['user_id']

        # 2. 创建用户资料
        profile = UserProfile.objects.create(
            user_id=user_id,
            nickname=request.data['nickname'],
            avatar_url=request.data.get('avatar_url'),
            bio=request.data.get('bio')
        )

        # 3. 创建统计记录
        UserStatistics.objects.create(user_id=user_id)

        serializer = UserProfileDetailSerializer(profile)
        return Response({
            'code': 201,
            'message': '用户创建成功',
            'data': serializer.data,
            'timestamp': int(timezone.now().timestamp() * 1000)
        }, status=201)
```

---

## 内部服务接口

### 9. 批量获取用户信息（内部）

供其他服务批量获取用户信息（M3、M4 调用）

**接口**: `POST /internal/users/batch`

**权限**: 内部服务（通过内部 API Key 认证）

**请求 Body**:

| 字段     | 类型  | 必填 | 说明                        |
| -------- | ----- | ---- | --------------------------- |
| user_ids | array | 是   | 用户 ID 列表（最多 100 个） |

**请求示例**:

```http
POST /internal/users/batch HTTP/1.1
Host: localhost:8002
X-Internal-API-Key: your-internal-api-key
Content-Type: application/json

{
  "user_ids": [1, 2, 3]
}
```

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "user_id": 1,
      "nickname": "Alice",
      "avatar_url": "https://cdn.example.com/avatars/alice.jpg"
    },
    {
      "user_id": 2,
      "nickname": "Bob",
      "avatar_url": "https://cdn.example.com/avatars/bob.jpg"
    }
  ],
  "timestamp": 1698384000000
}
```

---

### 10. 更新用户统计数据（内部）

供其他服务更新用户统计数据（M3、M4 调用）

**接口**: `POST /internal/users/{user_id}/statistics/increment`

**权限**: 内部服务

**Path 参数**:

| 参数    | 类型    | 必填 | 说明    |
| ------- | ------- | ---- | ------- |
| user_id | integer | 是   | 用户 ID |

**请求 Body**:

| 字段  | 类型    | 必填 | 说明                                                                     |
| ----- | ------- | ---- | ------------------------------------------------------------------------ |
| field | string  | 是   | 字段名（posts_count, replies_count, likes_received, favorites_received） |
| value | integer | 是   | 增量值（可为负数）                                                       |

**请求示例**:

```http
POST /internal/users/1/statistics/increment HTTP/1.1
Host: localhost:8002
X-Internal-API-Key: your-internal-api-key
Content-Type: application/json

{
  "field": "posts_count",
  "value": 1
}
```

**响应示例**:

```json
{
  "code": 200,
  "message": "统计数据已更新",
  "data": {
    "user_id": 1,
    "posts_count": 16,
    "replies_count": 42,
    "likes_received": 128,
    "favorites_received": 35
  },
  "timestamp": 1698384000000
}
```

**Django 实现**:

```python
# apps/users/views.py
from django.db.models import F

class InternalUserViewSet(viewsets.ViewSet):
    @action(detail=True, methods=['post'], url_path='statistics/increment')
    def increment_statistics(self, request, user_id=None):
        field = request.data.get('field')
        value = request.data.get('value', 0)

        if field not in ['posts_count', 'replies_count', 'likes_received', 'favorites_received']:
            return Response({'code': 400, 'message': '无效的字段名'}, status=400)

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

## 错误码说明

### HTTP 状态码

| 状态码 | 说明           |
| ------ | -------------- |
| 200    | 请求成功       |
| 201    | 创建成功       |
| 400    | 请求参数错误   |
| 401    | 未认证         |
| 403    | 权限不足       |
| 404    | 资源不存在     |
| 500    | 服务器内部错误 |

### 业务错误码

| 错误码 | 说明                 |
| ------ | -------------------- |
| 40001  | 昵称长度不符合要求   |
| 40002  | 昵称修改频率超限     |
| 40003  | 个人简介超过长度限制 |
| 40004  | 头像文件格式不支持   |
| 40005  | 头像文件过大         |
| 40401  | 用户不存在           |
| 40301  | 权限不足             |

---

## 接口测试示例

### Postman Collection

```json
{
  "info": {
    "name": "M2 User Service API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Get User Profile",
      "request": {
        "method": "GET",
        "header": [],
        "url": {
          "raw": "{{base_url}}/api/users/1",
          "host": ["{{base_url}}"],
          "path": ["api", "users", "1"]
        }
      }
    },
    {
      "name": "Update User Profile",
      "request": {
        "method": "PUT",
        "header": [
          {
            "key": "Authorization",
            "value": "Bearer {{token}}"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"nickname\": \"Alice Chen\",\n  \"bio\": \"Full Stack Developer\"\n}"
        },
        "url": {
          "raw": "{{base_url}}/api/users/1/profile",
          "host": ["{{base_url}}"],
          "path": ["api", "users", "1", "profile"]
        }
      }
    }
  ]
}
```

---

**文档版本**: v1.0  
**创建日期**: 2025-10-27  
**最后更新**: 2025-10-27

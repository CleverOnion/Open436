# API 设计规范

## 文档信息

**文档版本**: v1.0  
**创建日期**: 2025-10-23  
**文档类型**: 技术设计文档（TDD）  
**适用范围**: 全体后端开发团队

---

## 目录

1. [RESTful API 设计原则](#restful-api-设计原则)
2. [URL 设计规范](#url-设计规范)
3. [HTTP 方法使用](#http-方法使用)
4. [请求与响应格式](#请求与响应格式)
5. [错误处理规范](#错误处理规范)
6. [分页与排序](#分页与排序)
7. [版本控制](#版本控制)
8. [API 文档规范](#api-文档规范)

---

## RESTful API 设计原则

### 1.1 核心原则

| 原则 | 说明 | 示例 |
|------|------|------|
| **资源导向** | URL 表示资源，而非动作 | ✅ `/api/posts` ❌ `/api/getPosts` |
| **名词复数** | 使用复数名词表示资源集合 | ✅ `/api/users` ❌ `/api/user` |
| **层级关系** | 使用路径表示资源关系 | `/api/posts/123/replies` |
| **无状态** | 每个请求独立，包含完整信息 | 通过 Token 而非 Session |
| **统一接口** | 使用标准 HTTP 方法 | GET, POST, PUT, DELETE |

### 1.2 设计检查清单

- [ ] URL 是否使用名词而非动词？
- [ ] 是否使用复数形式？
- [ ] 是否正确使用 HTTP 方法？
- [ ] 响应格式是否统一？
- [ ] 错误信息是否清晰？
- [ ] 是否支持分页？
- [ ] 是否有 API 文档？

---

## URL 设计规范

### 2.1 基础格式

```
https://{domain}/{version}/{resource}/{id}/{sub-resource}
```

**示例**：
```
https://api.open436.com/v1/posts/123/replies
```

### 2.2 命名规范

| 规则 | 说明 | 示例 |
|------|------|------|
| **小写字母** | 全部使用小写 | `/api/users` |
| **连字符分隔** | 多个单词用 `-` 连接 | `/api/user-profiles` |
| **复数名词** | 资源集合用复数 | `/api/posts` |
| **避免动词** | URL 不包含动作 | ❌ `/api/createPost` |

### 2.3 资源路径设计

#### 单一资源

```
GET    /api/users           # 获取用户列表
POST   /api/users           # 创建用户
GET    /api/users/:id       # 获取单个用户
PUT    /api/users/:id       # 更新用户
DELETE /api/users/:id       # 删除用户
```

#### 嵌套资源

```
GET    /api/posts/:id/replies        # 获取帖子的回复列表
POST   /api/posts/:id/replies        # 为帖子添加回复
GET    /api/posts/:id/replies/:rid   # 获取特定回复
DELETE /api/posts/:id/replies/:rid   # 删除回复
```

#### 资源操作（非 CRUD）

```
POST   /api/posts/:id/like           # 点赞
DELETE /api/posts/:id/like           # 取消点赞
POST   /api/posts/:id/favorite       # 收藏
PUT    /api/posts/:id/pin            # 置顶
POST   /api/auth/login               # 登录
POST   /api/auth/logout              # 登出
```

### 2.4 查询参数

```
GET /api/posts?page=1&pageSize=20&sort=created_at&order=desc&author=123
```

| 参数 | 说明 | 示例 |
|------|------|------|
| `page` | 页码 | `page=1` |
| `pageSize` | 每页数量 | `pageSize=20` |
| `sort` | 排序字段 | `sort=created_at` |
| `order` | 排序方向 | `order=desc` |
| `filter` | 筛选条件 | `author=123` |
| `search` | 搜索关键词 | `search=keyword` |

---

## HTTP 方法使用

### 3.1 方法定义

| 方法 | 用途 | 幂等性 | 安全性 | 示例 |
|------|------|--------|--------|------|
| **GET** | 获取资源 | ✅ | ✅ | 获取用户列表 |
| **POST** | 创建资源 | ❌ | ❌ | 创建新帖子 |
| **PUT** | 完整更新资源 | ✅ | ❌ | 更新用户信息 |
| **PATCH** | 部分更新资源 | ❌ | ❌ | 修改昵称 |
| **DELETE** | 删除资源 | ✅ | ❌ | 删除帖子 |

**幂等性**：多次执行结果相同  
**安全性**：不修改服务器状态

### 3.2 使用示例

#### GET - 获取资源

```http
GET /api/posts/123
```

```json
{
  "code": 200,
  "data": {
    "id": 123,
    "title": "Hello World",
    "content": "...",
    "author": { "id": 1, "name": "Alice" }
  }
}
```

#### POST - 创建资源

```http
POST /api/posts
Content-Type: application/json

{
  "title": "New Post",
  "content": "Post content..."
}
```

```json
{
  "code": 201,
  "message": "Created successfully",
  "data": {
    "id": 124,
    "title": "New Post",
    "created_at": "2025-10-23T10:30:00Z"
  }
}
```

#### PUT - 完整更新

```http
PUT /api/users/123
Content-Type: application/json

{
  "nickname": "New Name",
  "avatar": "/avatars/new.jpg",
  "bio": "New bio"
}
```

#### PATCH - 部分更新

```http
PATCH /api/users/123
Content-Type: application/json

{
  "nickname": "New Name"
}
```

#### DELETE - 删除资源

```http
DELETE /api/posts/123
```

```json
{
  "code": 200,
  "message": "Deleted successfully"
}
```

---

## 请求与响应格式

### 4.1 统一响应格式

#### 成功响应

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    // 业务数据
  },
  "timestamp": "2025-10-23T10:30:00Z"
}
```

#### 列表响应（带分页）

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "items": [
      { "id": 1, "title": "Post 1" },
      { "id": 2, "title": "Post 2" }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 100,
      "totalPages": 5
    }
  },
  "timestamp": "2025-10-23T10:30:00Z"
}
```

#### 错误响应

```json
{
  "code": 400,
  "message": "Validation failed",
  "errors": [
    {
      "field": "title",
      "message": "Title is required"
    },
    {
      "field": "content",
      "message": "Content must be at least 10 characters"
    }
  ],
  "timestamp": "2025-10-23T10:30:00Z"
}
```

### 4.2 HTTP 状态码

| 状态码 | 说明 | 使用场景 |
|--------|------|---------|
| **200 OK** | 成功 | GET, PUT, PATCH, DELETE 成功 |
| **201 Created** | 已创建 | POST 创建资源成功 |
| **204 No Content** | 无内容 | DELETE 成功且无返回数据 |
| **400 Bad Request** | 请求错误 | 参数验证失败 |
| **401 Unauthorized** | 未认证 | Token 无效或过期 |
| **403 Forbidden** | 权限不足 | 无权访问资源 |
| **404 Not Found** | 资源不存在 | 请求的资源不存在 |
| **409 Conflict** | 冲突 | 资源已存在（如用户名重复） |
| **422 Unprocessable Entity** | 无法处理 | 业务逻辑错误 |
| **429 Too Many Requests** | 请求过多 | 触发限流 |
| **500 Internal Server Error** | 服务器错误 | 服务器内部错误 |
| **503 Service Unavailable** | 服务不可用 | 服务维护或过载 |

### 4.3 请求头规范

#### 必需的请求头

```http
Content-Type: application/json
Authorization: Bearer {token}
```

#### 可选的请求头

```http
Accept-Language: zh-CN
X-Request-ID: uuid-1234-5678
User-Agent: Open436-Client/1.0
```

### 4.4 响应头规范

```http
Content-Type: application/json; charset=utf-8
X-Request-ID: uuid-1234-5678
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1698000000
```

---

## 错误处理规范

### 5.1 错误码体系

#### 业务错误码格式

```
{HTTP状态码}{模块编号}{错误序号}
```

**示例**：
- `40001001` = 400（参数错误）+ 01（认证模块）+ 001（用户名为空）
- `40301002` = 403（权限不足）+ 01（认证模块）+ 002（非管理员）

#### 错误码定义

**认证模块 (01)**：

| 错误码 | HTTP | 说明 |
|--------|------|------|
| 40001001 | 400 | 用户名不能为空 |
| 40001002 | 400 | 密码不能为空 |
| 40101001 | 401 | Token 无效 |
| 40101002 | 401 | Token 已过期 |
| 40301001 | 403 | 权限不足 |
| 40901001 | 409 | 用户名已存在 |

**用户模块 (02)**：

| 错误码 | HTTP | 说明 |
|--------|------|------|
| 40002001 | 400 | 昵称长度不符 |
| 40402001 | 404 | 用户不存在 |

**内容模块 (03)**：

| 错误码 | HTTP | 说明 |
|--------|------|------|
| 40003001 | 400 | 标题不能为空 |
| 40003002 | 400 | 内容过短 |
| 40403001 | 404 | 帖子不存在 |

### 5.2 错误响应示例

#### 单个错误

```json
{
  "code": 40001001,
  "message": "Username is required",
  "timestamp": "2025-10-23T10:30:00Z"
}
```

#### 多个验证错误

```json
{
  "code": 40000000,
  "message": "Validation failed",
  "errors": [
    {
      "field": "username",
      "code": 40001001,
      "message": "Username is required"
    },
    {
      "field": "password",
      "code": 40001002,
      "message": "Password must be at least 6 characters"
    }
  ],
  "timestamp": "2025-10-23T10:30:00Z"
}
```

#### 服务器错误

```json
{
  "code": 50000000,
  "message": "Internal server error",
  "requestId": "uuid-1234-5678",
  "timestamp": "2025-10-23T10:30:00Z"
}
```

---

## 分页与排序

### 6.1 分页参数

```
GET /api/posts?page=1&pageSize=20
```

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `page` | integer | 1 | 页码（从 1 开始） |
| `pageSize` | integer | 20 | 每页数量 |
| `limit` | integer | 20 | 每页数量（别名） |
| `offset` | integer | 0 | 偏移量 |

### 6.2 分页响应

```json
{
  "code": 200,
  "data": {
    "items": [...],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 100,
      "totalPages": 5,
      "hasNext": true,
      "hasPrev": false
    }
  }
}
```

### 6.3 排序参数

```
GET /api/posts?sort=created_at&order=desc
```

| 参数 | 值 | 说明 |
|------|-----|------|
| `sort` | 字段名 | 排序字段 |
| `order` | `asc` / `desc` | 升序/降序 |

**多字段排序**：

```
GET /api/posts?sort=pinned,created_at&order=desc,desc
```

### 6.4 游标分页（可选）

适用于实时数据流：

```
GET /api/posts?cursor=eyJpZCI6MTIzfQ==&limit=20
```

```json
{
  "code": 200,
  "data": {
    "items": [...],
    "cursor": {
      "next": "eyJpZCI6MTQzfQ==",
      "hasMore": true
    }
  }
}
```

---

## 版本控制

### 7.1 版本策略

**推荐方式**：URL 路径版本

```
https://api.open436.com/v1/posts
https://api.open436.com/v2/posts
```

**可选方式**：Header 版本

```http
GET /api/posts
Accept: application/vnd.open436.v1+json
```

### 7.2 版本兼容性

| 变更类型 | 是否需要新版本 | 示例 |
|---------|---------------|------|
| 添加新字段 | ❌ | 响应中添加 `view_count` |
| 添加新接口 | ❌ | 新增 `/api/posts/:id/share` |
| 修改字段类型 | ✅ | `id` 从 int 改为 string |
| 删除字段 | ✅ | 移除 `deprecated_field` |
| 修改接口行为 | ✅ | 修改排序逻辑 |

### 7.3 版本废弃

```json
{
  "code": 200,
  "data": {...},
  "deprecated": {
    "version": "v1",
    "sunset": "2026-01-01",
    "message": "This API version will be deprecated on 2026-01-01. Please migrate to v2."
  }
}
```

---

## API 文档规范

### 8.1 OpenAPI (Swagger) 规范

**示例**：

```yaml
openapi: 3.0.0
info:
  title: Open436 API
  version: 1.0.0
  description: Open436 论坛系统 API 文档

servers:
  - url: https://api.open436.com/v1
    description: 生产环境
  - url: http://localhost:8000/v1
    description: 开发环境

paths:
  /posts:
    get:
      summary: 获取帖子列表
      tags:
        - Posts
      parameters:
        - name: page
          in: query
          schema:
            type: integer
            default: 1
        - name: pageSize
          in: query
          schema:
            type: integer
            default: 20
      responses:
        '200':
          description: 成功
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/PostListResponse'
    
    post:
      summary: 创建帖子
      tags:
        - Posts
      security:
        - BearerAuth: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CreatePostRequest'
      responses:
        '201':
          description: 创建成功

components:
  schemas:
    Post:
      type: object
      properties:
        id:
          type: integer
        title:
          type: string
        content:
          type: string
        author:
          $ref: '#/components/schemas/User'
        created_at:
          type: string
          format: date-time
    
    CreatePostRequest:
      type: object
      required:
        - title
        - content
      properties:
        title:
          type: string
          minLength: 1
          maxLength: 200
        content:
          type: string
          minLength: 10
  
  securitySchemes:
    BearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT
```

### 8.2 接口文档模板

#### 接口基本信息

| 项目 | 内容 |
|------|------|
| **接口名称** | 获取帖子列表 |
| **接口路径** | `GET /api/posts` |
| **接口描述** | 分页获取帖子列表，支持排序和筛选 |
| **是否需要认证** | 否 |
| **权限要求** | 无 |

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| page | integer | 否 | 1 | 页码 |
| pageSize | integer | 否 | 20 | 每页数量 |
| sort | string | 否 | created_at | 排序字段 |
| order | string | 否 | desc | 排序方向 |

#### 响应示例

```json
{
  "code": 200,
  "data": {
    "items": [
      {
        "id": 1,
        "title": "Hello World",
        "author": {
          "id": 1,
          "nickname": "Alice"
        }
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 100
    }
  }
}
```

#### 错误码

| 错误码 | HTTP | 说明 |
|--------|------|------|
| 40000001 | 400 | 参数错误 |
| 50000000 | 500 | 服务器错误 |

---

## 最佳实践总结

### 9.1 DO（推荐做法）

✅ 使用名词表示资源  
✅ 使用复数形式  
✅ 使用标准 HTTP 方法  
✅ 提供清晰的错误信息  
✅ 支持分页和排序  
✅ 编写完整的 API 文档  
✅ 使用版本控制  
✅ 返回统一的响应格式

### 9.2 DON'T（避免做法）

❌ URL 中使用动词  
❌ 在 URL 中暴露实现细节  
❌ 返回不一致的数据格式  
❌ 忽略 HTTP 状态码  
❌ 缺少错误信息  
❌ 不支持分页（大数据集）  
❌ 硬编码业务逻辑到 URL

---

**文档维护**: API 设计组  
**最后更新**: 2025-10-23

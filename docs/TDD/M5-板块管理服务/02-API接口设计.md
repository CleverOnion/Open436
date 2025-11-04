# M5 板块管理服务 - API 接口设计

## 文档信息

**服务名称**: 板块管理服务 (section-service)  
**Base URL**: `http://localhost:8005/api` (开发环境)  
**通过 Kong**: `http://localhost:8000/api` (生产环境)  
**版本**: v1.0

---

## 目录

1. [API 设计规范](#api-设计规范)
2. [公开接口](#公开接口)
3. [管理员接口](#管理员接口)
4. [内部接口](#内部接口)
5. [错误处理](#错误处理)
6. [测试用例](#测试用例)

---

## API 设计规范

### 1. 统一响应格式

#### 成功响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    // 具体数据
  },
  "timestamp": 1699027200000
}
```

#### 错误响应

```json
{
  "code": 400,
  "message": "板块名称已存在",
  "error": "ValidationError",
  "timestamp": 1699027200000
}
```

### 2. HTTP 状态码

| 状态码 | 说明           | 使用场景                 |
|--------|----------------|--------------------------|
| 200    | 成功           | GET、PUT、DELETE 成功    |
| 201    | 创建成功       | POST 创建资源成功        |
| 400    | 请求错误       | 参数验证失败             |
| 401    | 未认证         | Token 缺失或无效         |
| 403    | 权限不足       | 非管理员访问管理接口     |
| 404    | 资源不存在     | 板块不存在               |
| 409    | 冲突           | 板块 slug/name 重复      |
| 500    | 服务器错误     | 系统内部错误             |

### 3. 认证方式

```http
Authorization: Bearer {sa-token}
```

- 公开接口：无需认证
- 管理员接口：需要 Sa-Token 认证 + admin 角色

### 4. 分页参数

```
?page=1&page_size=20
```

- `page`: 页码（从 1 开始，默认 1）
- `page_size`: 每页数量（默认 20，最大 100）

### 5. 排序参数

```
?ordering=sort_order,-created_at
```

- 支持多字段排序
- `-` 前缀表示降序

---

## 公开接口

### 1. 获取板块列表

获取所有启用的板块列表，按排序号升序排列。

#### 接口信息

- **URL**: `GET /api/sections`
- **认证**: 不需要
- **权限**: 所有用户

#### 请求参数

**Query Parameters**:

| 参数       | 类型    | 必填 | 说明                              | 默认值   |
|------------|---------|------|-----------------------------------|----------|
| page       | integer | 否   | 页码                              | 1        |
| page_size  | integer | 否   | 每页数量                          | 20       |
| is_enabled | boolean | 否   | 筛选启用状态（仅管理员可用 false）| true     |
| ordering   | string  | 否   | 排序字段                          | sort_order,id |

#### 响应示例

**成功响应 (200 OK)**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "count": 6,
    "next": null,
    "previous": null,
    "results": [
      {
        "id": 1,
        "slug": "tech",
        "name": "技术交流",
        "description": "分享编程技术和开发经验，讨论最新技术趋势",
        "icon_url": "http://minio:9000/open436-icons/tech-icon.png",
        "color": "#1976D2",
        "sort_order": 1,
        "posts_count": 156,
        "created_at": "2025-01-01T00:00:00Z",
        "updated_at": "2025-01-15T10:30:00Z"
      },
      {
        "id": 2,
        "slug": "design",
        "name": "设计分享",
        "description": "UI/UX 设计作品展示、设计心得分享",
        "icon_url": "http://minio:9000/open436-icons/design-icon.png",
        "color": "#9C27B0",
        "sort_order": 2,
        "posts_count": 89,
        "created_at": "2025-01-01T00:00:00Z",
        "updated_at": "2025-01-15T10:30:00Z"
      }
    ]
  },
  "timestamp": 1699027200000
}
```

#### cURL 示例

```bash
# 获取启用的板块列表
curl -X GET http://localhost:8005/api/sections

# 通过 Kong 访问
curl -X GET http://localhost:8000/api/sections

# 分页查询
curl -X GET "http://localhost:8005/api/sections?page=1&page_size=10"
```

---

### 2. 获取板块详情

根据 ID 或 slug 获取单个板块的详细信息。

#### 接口信息

- **URL**: `GET /api/sections/{id_or_slug}`
- **认证**: 不需要
- **权限**: 所有用户

#### 请求参数

**Path Parameters**:

| 参数        | 类型   | 必填 | 说明                    |
|-------------|--------|------|-------------------------|
| id_or_slug  | mixed  | 是   | 板块 ID（数字）或 slug  |

#### 响应示例

**成功响应 (200 OK)**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "slug": "tech",
    "name": "技术交流",
    "description": "分享编程技术和开发经验，讨论最新技术趋势",
    "icon_url": "http://minio:9000/open436-icons/tech-icon.png",
    "color": "#1976D2",
    "sort_order": 1,
    "is_enabled": true,
    "posts_count": 156,
    "created_at": "2025-01-01T00:00:00Z",
    "updated_at": "2025-01-15T10:30:00Z"
  },
  "timestamp": 1699027200000
}
```

**错误响应 (404 Not Found)**:

```json
{
  "code": 404,
  "message": "板块不存在",
  "error": "NotFound",
  "timestamp": 1699027200000
}
```

#### cURL 示例

```bash
# 通过 ID 查询
curl -X GET http://localhost:8005/api/sections/1

# 通过 slug 查询
curl -X GET http://localhost:8005/api/sections/tech

# 通过 Kong 访问
curl -X GET http://localhost:8000/api/sections/tech
```

---

## 管理员接口

所有管理员接口需要 Sa-Token 认证且用户角色为 `admin`。

### 3. 创建板块

管理员创建新板块。

#### 接口信息

- **URL**: `POST /api/sections`
- **认证**: 需要 Sa-Token
- **权限**: 仅管理员

#### 请求参数

**Headers**:
```
Authorization: Bearer {sa-token}
Content-Type: application/json
```

**Body Parameters**:

| 参数         | 类型    | 必填 | 说明                            | 示例        |
|--------------|---------|------|---------------------------------|-------------|
| slug         | string  | 是   | 板块标识（3-20字符，小写字母、数字、下划线） | "frontend" |
| name         | string  | 是   | 板块名称（2-50字符，唯一）      | "前端开发"  |
| description  | string  | 否   | 板块描述（最大500字符）         | "前端技术讨论" |
| icon_file_id | uuid    | 否   | 图标文件ID（M7文件服务返回）    | "uuid"      |
| color        | string  | 是   | 板块颜色（HEX格式）             | "#1976D2"   |
| sort_order   | integer | 否   | 排序号（1-999）                 | 10          |

#### 请求示例

```json
{
  "slug": "frontend",
  "name": "前端开发",
  "description": "HTML、CSS、JavaScript、Vue、React 等前端技术讨论",
  "icon_file_id": "550e8400-e29b-41d4-a716-446655440000",
  "color": "#42A5F5",
  "sort_order": 7
}
```

#### 响应示例

**成功响应 (201 Created)**:

```json
{
  "code": 201,
  "message": "板块创建成功",
  "data": {
    "id": 7,
    "slug": "frontend",
    "name": "前端开发",
    "description": "HTML、CSS、JavaScript、Vue、React 等前端技术讨论",
    "icon_url": "http://minio:9000/open436-icons/frontend-icon.png",
    "color": "#42A5F5",
    "sort_order": 7,
    "is_enabled": true,
    "posts_count": 0,
    "created_at": "2025-11-04T10:30:00Z",
    "updated_at": "2025-11-04T10:30:00Z"
  },
  "timestamp": 1699027200000
}
```

**错误响应 (400 Bad Request)**:

```json
{
  "code": 400,
  "message": "参数验证失败",
  "error": "ValidationError",
  "details": {
    "slug": ["板块标识已存在"],
    "color": ["颜色格式不正确，应为 HEX 格式（如 #1976D2）"]
  },
  "timestamp": 1699027200000
}
```

**错误响应 (409 Conflict)**:

```json
{
  "code": 409,
  "message": "板块名称已存在",
  "error": "Conflict",
  "timestamp": 1699027200000
}
```

#### cURL 示例

```bash
# 创建板块
curl -X POST http://localhost:8005/api/sections \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "slug": "frontend",
    "name": "前端开发",
    "description": "前端技术讨论",
    "color": "#42A5F5",
    "sort_order": 7
  }'

# 通过 Kong 访问
curl -X POST http://localhost:8000/api/sections \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{...}'
```

---

### 4. 编辑板块

管理员编辑板块信息（不包括 slug）。

#### 接口信息

- **URL**: `PUT /api/sections/{id}`
- **认证**: 需要 Sa-Token
- **权限**: 仅管理员

#### 请求参数

**Path Parameters**:

| 参数 | 类型    | 必填 | 说明    |
|------|---------|------|---------|
| id   | integer | 是   | 板块 ID |

**Body Parameters**:

| 参数         | 类型    | 必填 | 说明                        |
|--------------|---------|------|-----------------------------|
| name         | string  | 否   | 板块名称（2-50字符）        |
| description  | string  | 否   | 板块描述                    |
| icon_file_id | uuid    | 否   | 图标文件ID                  |
| color        | string  | 否   | 板块颜色（HEX格式）         |
| sort_order   | integer | 否   | 排序号（1-999）             |

**注意**: 
- `slug` 字段创建后不可修改
- 至少提供一个字段

#### 请求示例

```json
{
  "name": "前端技术",
  "description": "现代前端技术讨论：Vue、React、TypeScript",
  "color": "#2196F3",
  "sort_order": 5
}
```

#### 响应示例

**成功响应 (200 OK)**:

```json
{
  "code": 200,
  "message": "板块更新成功",
  "data": {
    "id": 7,
    "slug": "frontend",
    "name": "前端技术",
    "description": "现代前端技术讨论：Vue、React、TypeScript",
    "icon_url": "http://minio:9000/open436-icons/frontend-icon.png",
    "color": "#2196F3",
    "sort_order": 5,
    "is_enabled": true,
    "posts_count": 23,
    "created_at": "2025-11-04T10:30:00Z",
    "updated_at": "2025-11-04T15:45:00Z"
  },
  "timestamp": 1699027200000
}
```

#### cURL 示例

```bash
curl -X PUT http://localhost:8005/api/sections/7 \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "前端技术",
    "color": "#2196F3",
    "sort_order": 5
  }'
```

---

### 5. 删除板块

管理员删除板块（软删除，实际上是禁用）。

#### 接口信息

- **URL**: `DELETE /api/sections/{id}`
- **认证**: 需要 Sa-Token
- **权限**: 仅管理员

#### 请求参数

**Path Parameters**:

| 参数 | 类型    | 必填 | 说明    |
|------|---------|------|---------|
| id   | integer | 是   | 板块 ID |

**Query Parameters**:

| 参数  | 类型    | 必填 | 说明                          | 默认值 |
|-------|---------|------|-------------------------------|--------|
| force | boolean | 否   | 强制删除（即使有帖子）        | false  |

#### 响应示例

**成功响应 (200 OK)**:

```json
{
  "code": 200,
  "message": "板块已删除",
  "data": {
    "id": 7,
    "status": "disabled"
  },
  "timestamp": 1699027200000
}
```

**错误响应 (400 Bad Request)** - 板块有帖子时:

```json
{
  "code": 400,
  "message": "无法删除包含帖子的板块，请先清空帖子或禁用该板块",
  "error": "ValidationError",
  "details": {
    "posts_count": 23
  },
  "timestamp": 1699027200000
}
```

**错误响应 (400 Bad Request)** - 最后一个启用板块:

```json
{
  "code": 400,
  "message": "无法删除最后一个启用的板块",
  "error": "ValidationError",
  "timestamp": 1699027200000
}
```

#### cURL 示例

```bash
# 删除板块（软删除）
curl -X DELETE http://localhost:8005/api/sections/7 \
  -H "Authorization: Bearer {token}"

# 强制删除（即使有帖子）
curl -X DELETE "http://localhost:8005/api/sections/7?force=true" \
  -H "Authorization: Bearer {token}"
```

---

### 6. 启用/禁用板块

管理员切换板块的启用状态。

#### 接口信息

- **URL**: `PUT /api/sections/{id}/status`
- **认证**: 需要 Sa-Token
- **权限**: 仅管理员

#### 请求参数

**Path Parameters**:

| 参数 | 类型    | 必填 | 说明    |
|------|---------|------|---------|
| id   | integer | 是   | 板块 ID |

**Body Parameters**:

| 参数       | 类型    | 必填 | 说明           |
|------------|---------|------|----------------|
| is_enabled | boolean | 是   | 启用状态       |

#### 请求示例

```json
{
  "is_enabled": false
}
```

#### 响应示例

**成功响应 (200 OK)**:

```json
{
  "code": 200,
  "message": "板块已禁用",
  "data": {
    "id": 7,
    "slug": "frontend",
    "name": "前端技术",
    "is_enabled": false,
    "updated_at": "2025-11-04T16:00:00Z"
  },
  "timestamp": 1699027200000
}
```

**错误响应 (400 Bad Request)** - 最后一个启用板块:

```json
{
  "code": 400,
  "message": "至少需要保留一个启用的板块",
  "error": "ValidationError",
  "timestamp": 1699027200000
}
```

#### cURL 示例

```bash
# 禁用板块
curl -X PUT http://localhost:8005/api/sections/7/status \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{"is_enabled": false}'

# 启用板块
curl -X PUT http://localhost:8005/api/sections/7/status \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{"is_enabled": true}'
```

---

### 7. 批量调整板块排序

管理员批量调整板块的显示顺序。

#### 接口信息

- **URL**: `PUT /api/sections/reorder`
- **认证**: 需要 Sa-Token
- **权限**: 仅管理员

#### 请求参数

**Body Parameters**:

| 参数  | 类型  | 必填 | 说明                                |
|-------|-------|------|-------------------------------------|
| order | array | 是   | 板块ID数组，按新顺序排列            |

#### 请求示例

```json
{
  "order": [3, 1, 2, 4, 5, 6]
}
```

说明：数组中的第一个 ID 排序号为 1，第二个为 2，以此类推。

#### 响应示例

**成功响应 (200 OK)**:

```json
{
  "code": 200,
  "message": "板块排序已更新",
  "data": {
    "updated_count": 6,
    "sections": [
      {"id": 3, "slug": "discuss", "sort_order": 1},
      {"id": 1, "slug": "tech", "sort_order": 2},
      {"id": 2, "slug": "design", "sort_order": 3},
      {"id": 4, "slug": "question", "sort_order": 4},
      {"id": 5, "slug": "share", "sort_order": 5},
      {"id": 6, "slug": "announce", "sort_order": 6}
    ]
  },
  "timestamp": 1699027200000
}
```

**错误响应 (400 Bad Request)**:

```json
{
  "code": 400,
  "message": "无效的板块ID",
  "error": "ValidationError",
  "details": {
    "invalid_ids": [999]
  },
  "timestamp": 1699027200000
}
```

#### cURL 示例

```bash
curl -X PUT http://localhost:8005/api/sections/reorder \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{"order": [3, 1, 2, 4, 5, 6]}'
```

---

### 8. 获取板块统计

管理员查看板块的详细统计数据。

#### 接口信息

- **URL**: `GET /api/sections/statistics`
- **认证**: 需要 Sa-Token
- **权限**: 仅管理员

#### 响应示例

**成功响应 (200 OK)**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total_sections": 6,
    "enabled_sections": 6,
    "disabled_sections": 0,
    "total_posts": 534,
    "sections": [
      {
        "id": 1,
        "slug": "tech",
        "name": "技术交流",
        "posts_count": 156,
        "is_enabled": true,
        "last_post_at": "2025-11-04T15:30:00Z"
      },
      {
        "id": 2,
        "slug": "design",
        "name": "设计分享",
        "posts_count": 89,
        "is_enabled": true,
        "last_post_at": "2025-11-04T14:20:00Z"
      }
    ]
  },
  "timestamp": 1699027200000
}
```

#### cURL 示例

```bash
curl -X GET http://localhost:8005/api/sections/statistics \
  -H "Authorization: Bearer {token}"
```

---

## 内部接口

### 9. 更新板块帖子数（内部接口）

供 M3 内容服务调用，更新板块的帖子数量。

#### 接口信息

- **URL**: `POST /internal/sections/{id}/increment-posts`
- **认证**: 内部服务（通过内网访问）
- **权限**: 仅服务间调用

#### 请求参数

**Path Parameters**:

| 参数 | 类型    | 必填 | 说明    |
|------|---------|------|---------|
| id   | integer | 是   | 板块 ID |

**Body Parameters**:

| 参数  | 类型    | 必填 | 说明                      |
|-------|---------|------|---------------------------|
| value | integer | 是   | 增量值（+1 或 -1）        |

#### 请求示例

```json
{
  "value": 1
}
```

#### 响应示例

**成功响应 (200 OK)**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "posts_count": 157
  },
  "timestamp": 1699027200000
}
```

#### cURL 示例

```bash
# M3 创建帖子时调用（+1）
curl -X POST http://section-service:8005/internal/sections/1/increment-posts \
  -H "Content-Type: application/json" \
  -d '{"value": 1}'

# M3 删除帖子时调用（-1）
curl -X POST http://section-service:8005/internal/sections/1/increment-posts \
  -H "Content-Type: application/json" \
  -d '{"value": -1}'
```

---

## 错误处理

### 统一错误格式

```json
{
  "code": 400,
  "message": "错误描述",
  "error": "错误类型",
  "details": {},
  "timestamp": 1699027200000
}
```

### 常见错误码

| HTTP 状态码 | code | error            | 说明             |
|-------------|------|------------------|------------------|
| 400         | 400  | ValidationError  | 参数验证失败     |
| 401         | 401  | Unauthorized     | 未认证           |
| 403         | 403  | Forbidden        | 权限不足         |
| 404         | 404  | NotFound         | 资源不存在       |
| 409         | 409  | Conflict         | 数据冲突         |
| 500         | 500  | InternalError    | 服务器内部错误   |

### 错误示例

#### 1. 参数验证失败 (400)

```json
{
  "code": 400,
  "message": "参数验证失败",
  "error": "ValidationError",
  "details": {
    "slug": ["板块标识只能包含小写字母、数字和下划线"],
    "name": ["板块名称长度必须在 2-50 个字符之间"],
    "color": ["颜色格式不正确，应为 HEX 格式（如 #1976D2）"]
  },
  "timestamp": 1699027200000
}
```

#### 2. Token 验证失败 (401)

```json
{
  "code": 401,
  "message": "Token 无效或已过期",
  "error": "Unauthorized",
  "timestamp": 1699027200000
}
```

#### 3. 权限不足 (403)

```json
{
  "code": 403,
  "message": "需要管理员权限",
  "error": "Forbidden",
  "timestamp": 1699027200000
}
```

#### 4. 板块不存在 (404)

```json
{
  "code": 404,
  "message": "板块不存在",
  "error": "NotFound",
  "timestamp": 1699027200000
}
```

#### 5. 板块名称冲突 (409)

```json
{
  "code": 409,
  "message": "板块名称已存在",
  "error": "Conflict",
  "details": {
    "field": "name",
    "value": "技术交流"
  },
  "timestamp": 1699027200000
}
```

---

## 测试用例

### 1. 公开接口测试

#### 测试1: 获取板块列表

```bash
# 请求
curl -X GET http://localhost:8005/api/sections

# 预期结果
# - 状态码: 200
# - 返回启用的板块列表
# - 按 sort_order 升序排列
```

#### 测试2: 获取板块详情（通过slug）

```bash
# 请求
curl -X GET http://localhost:8005/api/sections/tech

# 预期结果
# - 状态码: 200
# - 返回 tech 板块的详细信息
```

#### 测试3: 获取不存在的板块

```bash
# 请求
curl -X GET http://localhost:8005/api/sections/nonexistent

# 预期结果
# - 状态码: 404
# - 错误消息: "板块不存在"
```

### 2. 管理员接口测试

#### 测试4: 创建板块（成功）

```bash
# 1. 先登录获取 Token
TOKEN=$(curl -X POST http://localhost:8001/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"test123"}' \
  | jq -r '.data.token')

# 2. 创建板块
curl -X POST http://localhost:8005/api/sections \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "slug": "mobile",
    "name": "移动开发",
    "description": "iOS、Android、Flutter 等移动开发",
    "color": "#4CAF50",
    "sort_order": 10
  }'

# 预期结果
# - 状态码: 201
# - 返回创建的板块信息
```

#### 测试5: 创建板块（slug 重复）

```bash
curl -X POST http://localhost:8005/api/sections \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "slug": "tech",
    "name": "新板块",
    "color": "#000000",
    "sort_order": 10
  }'

# 预期结果
# - 状态码: 409
# - 错误消息: "板块标识已存在"
```

#### 测试6: 编辑板块

```bash
curl -X PUT http://localhost:8005/api/sections/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "技术讨论",
    "sort_order": 2
  }'

# 预期结果
# - 状态码: 200
# - 返回更新后的板块信息
```

#### 测试7: 禁用板块

```bash
curl -X PUT http://localhost:8005/api/sections/7/status \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"is_enabled": false}'

# 预期结果
# - 状态码: 200
# - 板块被禁用
```

#### 测试8: 调整排序

```bash
curl -X PUT http://localhost:8005/api/sections/reorder \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"order": [2, 1, 3, 4, 5, 6]}'

# 预期结果
# - 状态码: 200
# - 排序已更新
```

#### 测试9: 未认证访问管理接口

```bash
curl -X POST http://localhost:8005/api/sections \
  -H "Content-Type: application/json" \
  -d '{...}'

# 预期结果
# - 状态码: 401
# - 错误消息: "Token 无效或已过期"
```

#### 测试10: 普通用户访问管理接口

```bash
# 1. 以普通用户登录
USER_TOKEN=$(curl -X POST http://localhost:8001/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"password123"}' \
  | jq -r '.data.token')

# 2. 尝试创建板块
curl -X POST http://localhost:8005/api/sections \
  -H "Authorization: Bearer $USER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{...}'

# 预期结果
# - 状态码: 403
# - 错误消息: "需要管理员权限"
```

---

## API 使用流程

### 1. 用户浏览板块

```
1. 前端加载页面
2. GET /api/sections
3. 显示板块列表（侧边栏）
4. 用户点击板块
5. 跳转到 /sections/{slug}
6. GET /api/sections/{slug}
7. 显示板块详情和帖子列表
```

### 2. 管理员创建板块

```
1. 管理员登录（M1）
2. 获取 Sa-Token
3. 上传板块图标（M7）
4. 获取 icon_file_id
5. POST /api/sections（带 icon_file_id）
6. 创建成功
7. 板块显示在列表中
```

### 3. M3 创建帖子时更新板块计数

```
1. 用户在前端选择板块
2. M3 验证板块是否启用
   GET http://section-service:8005/api/sections/{slug}
3. 验证通过，创建帖子
4. M3 更新板块帖子数
   POST http://section-service:8005/internal/sections/{id}/increment-posts
   {"value": 1}
5. 板块 posts_count += 1
```

---

## 性能优化建议

### 1. 缓存策略

```python
# 板块列表缓存 5 分钟
cache_key = 'sections:enabled'
cache.set(cache_key, sections, 300)

# 单个板块缓存 10 分钟
cache_key = f'section:{slug}'
cache.set(cache_key, section, 600)
```

### 2. 数据库查询优化

```python
# 使用 select_related 预加载关联数据
sections = Section.objects.select_related('icon_file').all()

# 使用 only() 只查询需要的字段
sections = Section.objects.only('id', 'slug', 'name', 'color').all()
```

### 3. 批量操作

```python
# 批量更新排序号
Section.objects.bulk_update(sections, ['sort_order'])
```

---

## 总结

### API 接口总览

| 接口                                   | 方法   | 认证 | 权限     | 说明           |
|----------------------------------------|--------|------|----------|----------------|
| GET /api/sections                      | GET    | 否   | 公开     | 获取板块列表   |
| GET /api/sections/{id_or_slug}         | GET    | 否   | 公开     | 获取板块详情   |
| POST /api/sections                     | POST   | 是   | 管理员   | 创建板块       |
| PUT /api/sections/{id}                 | PUT    | 是   | 管理员   | 编辑板块       |
| DELETE /api/sections/{id}              | DELETE | 是   | 管理员   | 删除板块       |
| PUT /api/sections/{id}/status          | PUT    | 是   | 管理员   | 启用/禁用板块  |
| PUT /api/sections/reorder              | PUT    | 是   | 管理员   | 批量调整排序   |
| GET /api/sections/statistics           | GET    | 是   | 管理员   | 获取统计数据   |
| POST /internal/sections/{id}/increment-posts | POST | 否 | 内部服务 | 更新帖子数     |

### 设计亮点

- ✅ RESTful 风格，语义清晰
- ✅ 统一响应格式
- ✅ 详细的错误处理
- ✅ 支持 ID 和 slug 双重查询
- ✅ 公开/管理员接口分离
- ✅ 内部接口支持服务间调用

---

**文档版本**: v1.0  
**创建日期**: 2025-11-04  
**最后更新**: 2025-11-04


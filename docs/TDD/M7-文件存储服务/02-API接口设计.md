# M7 文件存储服务 - API 接口设计

## 文档信息

**服务名称**: 文件存储服务 (file-service)  
**Base URL**: `http://file-service:8007` (内部) / `https://api.open436.com/api/files` (通过 Kong)  
**协议**: HTTP/1.1 + JSON  
**认证方式**: Kong 网关 JWT 验证  
**版本**: v1.0

---

## 目录

1. [接口概览](#接口概览)
2. [通用规范](#通用规范)
3. [文件上传](#文件上传)
4. [文件信息查询](#文件信息查询)
5. [文件使用管理](#文件使用管理)
6. [文件删除](#文件删除)
7. [批量操作](#批量操作)
8. [统计接口](#统计接口)
9. [错误码定义](#错误码定义)

---

## 接口概览

### 接口列表

| 接口                         | 方法   | 认证 | 说明                   | 调用方           |
| ---------------------------- | ------ | ---- | ---------------------- | ---------------- |
| `/api/files/upload`          | POST   | 是   | 上传文件               | M2/M3/M4/M5      |
| `/api/files/:id`             | GET    | 否   | 获取文件信息           | M2/M3/M4/M5      |
| `/api/files/:id/url`         | GET    | 否   | 获取文件访问 URL       | M2/M3/M4/M5      |
| `/api/files/:id/mark-used`   | POST   | 是   | 标记文件已使用         | M2/M3/M4/M5      |
| `/api/files/:id/mark-unused` | POST   | 是   | 标记文件未使用         | M2/M3/M4/M5      |
| `/api/files/:id`             | DELETE | 是   | 删除文件（管理员）     | M8（系统管理）   |
| `/api/files/batch-info`      | POST   | 否   | 批量获取文件信息       | M2/M3            |
| `/api/files/statistics`      | GET    | 是   | 获取存储统计（管理员） | M8（系统管理）   |
| `/api/files/cleanup`         | POST   | 是   | 手动触发清理（管理员） | M8（系统管理）   |
| `/health`                    | GET    | 否   | 健康检查               | Kong / 运维      |

### 认证说明

- **需要认证**: 请求头携带 `Authorization: Bearer {token}`
- **认证流程**: Kong 网关验证 JWT → 转发请求到 M7 → M7 从请求头获取用户 ID (`X-User-Id`)
- **公开接口**: 文件信息查询和 URL 获取无需认证（文件本身公开可访问）

---

## 通用规范

### 请求头

```http
Content-Type: multipart/form-data  # 上传文件时
Content-Type: application/json     # 其他接口

Authorization: Bearer {jwt_token}  # 需要认证的接口
```

### 响应格式

#### 成功响应

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    // 业务数据
  },
  "timestamp": "2025-10-28T10:30:00Z"
}
```

#### 错误响应

```json
{
  "code": 40001001,
  "message": "File type not supported",
  "data": null,
  "timestamp": "2025-10-28T10:30:00Z"
}
```

### HTTP 状态码

| 状态码 | 说明                       | 使用场景               |
| ------ | -------------------------- | ---------------------- |
| 200    | OK                         | 成功                   |
| 201    | Created                    | 上传成功               |
| 400    | Bad Request                | 参数错误、验证失败     |
| 401    | Unauthorized               | 未认证                 |
| 403    | Forbidden                  | 无权限（管理员接口）   |
| 404    | Not Found                  | 文件不存在             |
| 413    | Payload Too Large          | 文件过大               |
| 500    | Internal Server Error      | 服务器错误             |

---

## 文件上传

### 1. 上传文件

**接口**: `POST /api/files/upload`

**描述**: 上传文件到存储服务

**是否需要认证**: 是

**请求格式**: `multipart/form-data`

**请求参数**:

| 参数        | 类型   | 必填 | 说明                                            |
| ----------- | ------ | ---- | ----------------------------------------------- |
| `file`      | File   | 是   | 文件数据（二进制）                              |
| `file_type` | String | 是   | 文件类型：`avatar` / `post` / `reply` / `section_icon` |

**请求示例**:

```http
POST /api/files/upload HTTP/1.1
Host: api.open436.com
Authorization: Bearer eyJhbGci...
Content-Type: multipart/form-data; boundary=----WebKitFormBoundary

------WebKitFormBoundary
Content-Disposition: form-data; name="file"; filename="avatar.jpg"
Content-Type: image/jpeg

<binary data>
------WebKitFormBoundary
Content-Disposition: form-data; name="file_type"

avatar
------WebKitFormBoundary--
```

**成功响应** (201):

```json
{
  "code": 201,
  "message": "File uploaded successfully",
  "data": {
    "file_id": "a1b2c3d4-e5f6-4789-a1b2-c3d4e5f67890",
    "filename": "avatar.jpg",
    "storage_key": "2025/10/28/a1b2c3d4-e5f6-4789-a1b2-c3d4e5f67890.jpg",
    "file_type": "avatar",
    "mime_type": "image/jpeg",
    "file_size": 153600,
    "url": "http://localhost:9000/open436-files/2025/10/28/a1b2c3d4-e5f6-4789-a1b2-c3d4e5f67890.jpg",
    "status": "unused",
    "created_at": "2025-10-28T10:30:00Z"
  },
  "timestamp": "2025-10-28T10:30:00Z"
}
```

**错误响应**:

| HTTP 状态码 | 错误码   | 说明                                |
| ----------- | -------- | ----------------------------------- |
| 400         | 70001001 | 文件类型不支持                      |
| 400         | 70001002 | 文件类型参数无效（不是枚举值）      |
| 413         | 70001003 | 文件过大                            |
| 400         | 70001004 | 文件为空                            |
| 400         | 70001005 | 文件名非法                          |
| 401         | 70001006 | 未认证                              |
| 500         | 70001007 | 上传失败（存储后端错误）            |

**业务逻辑**:

1. 从 Kong 网关获取用户 ID (`X-User-Id`)
2. 验证 `file_type` 参数是否有效
3. 读取文件数据（流式读取，避免内存溢出）
4. 验证文件大小（根据 `file_type`）
5. 验证文件类型（魔数检测）
6. 生成存储路径（按日期分目录 + UUID 文件名）
7. 上传到存储后端（S3/Local）
8. 保存文件元数据到数据库
9. 返回文件信息和访问 URL

**实现示例**:

```rust
use actix_multipart::Multipart;
use futures_util::StreamExt;

pub async fn upload_handler(
    storage: web::Data<Arc<dyn StorageBackend>>,
    db: web::Data<PgPool>,
    req: HttpRequest,
    mut payload: Multipart,
) -> Result<HttpResponse, FileError> {
    // 1. 获取用户 ID
    let user_id = get_current_user_id(&req)?;

    let mut file_data = Vec::new();
    let mut file_type_str = String::new();
    let mut filename = String::new();

    // 2. 解析 multipart
    while let Some(item) = payload.next().await {
        let mut field = item?;
        let field_name = field.name().to_string();

        match field_name.as_str() {
            "file" => {
                filename = field.content_disposition()
                    .get_filename()
                    .unwrap_or("unknown")
                    .to_string();

                while let Some(chunk) = field.next().await {
                    file_data.extend_from_slice(&chunk?);
                }
            }
            "file_type" => {
                while let Some(chunk) = field.next().await {
                    file_type_str.push_str(&String::from_utf8_lossy(&chunk?));
                }
            }
            _ => {}
        }
    }

    // 3. 解析文件类型
    let file_type = FileType::from_str(&file_type_str)?;

    // 4. 验证文件
    FileValidator::validate_file_size(&file_data, file_type)?;
    let mime_type = FileValidator::validate_file_type(&file_data)?;

    // 5. 生成存储路径
    let storage_key = PathGenerator::generate_storage_key(&filename)?;

    // 6. 上传到存储后端
    let url = storage.upload(&storage_key, file_data.clone(), &mime_type).await?;

    // 7. 保存到数据库
    let file_id = save_file_metadata(&db, &filename, &storage_key, file_type, &mime_type, file_data.len(), user_id).await?;

    // 8. 返回响应
    Ok(HttpResponse::Created().json(/* ... */))
}
```

---

## 文件信息查询

### 2. 获取文件信息

**接口**: `GET /api/files/:id`

**描述**: 根据文件 ID 获取文件元数据

**是否需要认证**: 否（公开接口）

**路径参数**:

| 参数 | 类型 | 说明    |
| ---- | ---- | ------- |
| `id` | UUID | 文件 ID |

**请求示例**:

```http
GET /api/files/a1b2c3d4-e5f6-4789-a1b2-c3d4e5f67890 HTTP/1.1
Host: api.open436.com
```

**成功响应** (200):

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "file_id": "a1b2c3d4-e5f6-4789-a1b2-c3d4e5f67890",
    "filename": "avatar.jpg",
    "storage_key": "2025/10/28/a1b2c3d4-e5f6-4789-a1b2-c3d4e5f67890.jpg",
    "file_type": "avatar",
    "mime_type": "image/jpeg",
    "file_size": 153600,
    "url": "http://localhost:9000/open436-files/2025/10/28/a1b2c3d4-e5f6-4789-a1b2-c3d4e5f67890.jpg",
    "status": "used",
    "uploader_id": 1,
    "created_at": "2025-10-28T10:30:00Z",
    "updated_at": "2025-10-28T10:35:00Z"
  },
  "timestamp": "2025-10-28T10:40:00Z"
}
```

**错误响应**:

| HTTP 状态码 | 错误码   | 说明       |
| ----------- | -------- | ---------- |
| 404         | 70002001 | 文件不存在 |
| 400         | 70002002 | 文件 ID 格式错误（不是有效的 UUID） |

---

### 3. 获取文件访问 URL

**接口**: `GET /api/files/:id/url`

**描述**: 快速获取文件访问 URL（不返回完整元数据）

**是否需要认证**: 否

**路径参数**:

| 参数 | 类型 | 说明    |
| ---- | ---- | ------- |
| `id` | UUID | 文件 ID |

**请求示例**:

```http
GET /api/files/a1b2c3d4-e5f6-4789-a1b2-c3d4e5f67890/url HTTP/1.1
Host: api.open436.com
```

**成功响应** (200):

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "file_id": "a1b2c3d4-e5f6-4789-a1b2-c3d4e5f67890",
    "url": "http://localhost:9000/open436-files/2025/10/28/a1b2c3d4-e5f6-4789-a1b2-c3d4e5f67890.jpg"
  },
  "timestamp": "2025-10-28T10:40:00Z"
}
```

**错误响应**:

| HTTP 状态码 | 错误码   | 说明       |
| ----------- | -------- | ---------- |
| 404         | 70002001 | 文件不存在 |

---

## 文件使用管理

### 4. 标记文件已使用

**接口**: `POST /api/files/:id/mark-used`

**描述**: 标记文件被业务实体使用（如帖子、回复、头像等）

**是否需要认证**: 是

**路径参数**:

| 参数 | 类型 | 说明    |
| ---- | ---- | ------- |
| `id` | UUID | 文件 ID |

**请求体** (JSON):

```json
{
  "usage_type": "post",   // post / reply / avatar / section_icon
  "usage_id": 101         // 帖子ID / 回复ID / 用户ID / 板块ID
}
```

| 参数         | 类型    | 必填 | 说明                                            |
| ------------ | ------- | ---- | ----------------------------------------------- |
| `usage_type` | String  | 是   | 使用类型：`post` / `reply` / `avatar` / `section_icon` |
| `usage_id`   | Integer | 是   | 使用位置 ID（正整数）                           |

**请求示例**:

```http
POST /api/files/a1b2c3d4-e5f6-4789-a1b2-c3d4e5f67890/mark-used HTTP/1.1
Host: api.open436.com
Authorization: Bearer eyJhbGci...
Content-Type: application/json

{
  "usage_type": "post",
  "usage_id": 101
}
```

**成功响应** (200):

```json
{
  "code": 200,
  "message": "File marked as used",
  "data": {
    "file_id": "a1b2c3d4-e5f6-4789-a1b2-c3d4e5f67890",
    "status": "used",
    "usage": {
      "usage_type": "post",
      "usage_id": 101,
      "created_at": "2025-10-28T10:35:00Z"
    }
  },
  "timestamp": "2025-10-28T10:35:00Z"
}
```

**错误响应**:

| HTTP 状态码 | 错误码   | 说明                                |
| ----------- | -------- | ----------------------------------- |
| 404         | 70003001 | 文件不存在                          |
| 400         | 70003002 | 使用类型无效                        |
| 400         | 70003003 | 使用 ID 无效（必须为正整数）        |
| 409         | 70003004 | 已经标记为此使用（重复标记）        |

**业务逻辑**:

1. 验证文件是否存在
2. 验证 `usage_type` 和 `usage_id` 参数
3. 检查是否已经关联（防止重复）
4. 插入 `file_usages` 表
5. 更新 `files` 表的 `status` 为 `used`
6. 返回成功响应

---

### 5. 标记文件未使用

**接口**: `POST /api/files/:id/mark-unused`

**描述**: 取消文件的使用标记（如删除帖子时）

**是否需要认证**: 是

**路径参数**:

| 参数 | 类型 | 说明    |
| ---- | ---- | ------- |
| `id` | UUID | 文件 ID |

**请求体** (JSON):

```json
{
  "usage_type": "post",
  "usage_id": 101
}
```

**请求示例**:

```http
POST /api/files/a1b2c3d4-e5f6-4789-a1b2-c3d4e5f67890/mark-unused HTTP/1.1
Host: api.open436.com
Authorization: Bearer eyJhbGci...
Content-Type: application/json

{
  "usage_type": "post",
  "usage_id": 101
}
```

**成功响应** (200):

```json
{
  "code": 200,
  "message": "File marked as unused",
  "data": {
    "file_id": "a1b2c3d4-e5f6-4789-a1b2-c3d4e5f67890",
    "status": "unused",  // 如果没有其他使用位置
    "remaining_usages": 0
  },
  "timestamp": "2025-10-28T10:45:00Z"
}
```

**错误响应**:

| HTTP 状态码 | 错误码   | 说明             |
| ----------- | -------- | ---------------- |
| 404         | 70004001 | 文件不存在       |
| 404         | 70004002 | 使用关联不存在   |

**业务逻辑**:

1. 验证文件是否存在
2. 查找并删除对应的 `file_usages` 记录
3. 检查文件是否还有其他使用位置
4. 如果没有其他使用，更新 `files.status` 为 `unused`
5. 返回成功响应

---

## 文件删除

### 6. 删除文件（管理员）

**接口**: `DELETE /api/files/:id`

**描述**: 删除文件（仅管理员）

**是否需要认证**: 是（管理员）

**路径参数**:

| 参数 | 类型 | 说明    |
| ---- | ---- | ------- |
| `id` | UUID | 文件 ID |

**查询参数**:

| 参数     | 类型   | 必填 | 说明                       |
| -------- | ------ | ---- | -------------------------- |
| `reason` | String | 否   | 删除原因（记录到日志）     |

**请求示例**:

```http
DELETE /api/files/a1b2c3d4-e5f6-4789-a1b2-c3d4e5f67890?reason=违规内容 HTTP/1.1
Host: api.open436.com
Authorization: Bearer eyJhbGci...
```

**成功响应** (200):

```json
{
  "code": 200,
  "message": "File deleted successfully",
  "data": {
    "file_id": "a1b2c3d4-e5f6-4789-a1b2-c3d4e5f67890",
    "deleted_at": "2025-10-28T10:50:00Z"
  },
  "timestamp": "2025-10-28T10:50:00Z"
}
```

**错误响应**:

| HTTP 状态码 | 错误码   | 说明             |
| ----------- | -------- | ---------------- |
| 404         | 70005001 | 文件不存在       |
| 403         | 70005002 | 需要管理员权限   |
| 500         | 70005003 | 删除失败         |

**业务逻辑**:

1. 验证管理员权限（从 Kong 传递的 `X-User-Role`）
2. 验证文件是否存在
3. 从存储后端删除物理文件
4. 更新数据库 `files.status` 为 `deleted`
5. 删除所有 `file_usages` 关联
6. 记录删除日志（操作者、时间、原因）
7. 返回成功响应

---

## 批量操作

### 7. 批量获取文件信息

**接口**: `POST /api/files/batch-info`

**描述**: 批量获取多个文件的信息（用于帖子列表等场景）

**是否需要认证**: 否

**请求体** (JSON):

```json
{
  "file_ids": [
    "a1b2c3d4-e5f6-4789-a1b2-c3d4e5f67890",
    "b2c3d4e5-f6a7-4890-b2c3-d4e5f6a78901",
    "c3d4e5f6-a7b8-4901-c3d4-e5f6a7b89012"
  ]
}
```

| 参数       | 类型          | 必填 | 说明                   |
| ---------- | ------------- | ---- | ---------------------- |
| `file_ids` | Array<String> | 是   | 文件 ID 列表（最多 100 个） |

**请求示例**:

```http
POST /api/files/batch-info HTTP/1.1
Host: api.open436.com
Content-Type: application/json

{
  "file_ids": [
    "a1b2c3d4-e5f6-4789-a1b2-c3d4e5f67890",
    "b2c3d4e5-f6a7-4890-b2c3-d4e5f6a78901"
  ]
}
```

**成功响应** (200):

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "files": [
      {
        "file_id": "a1b2c3d4-e5f6-4789-a1b2-c3d4e5f67890",
        "filename": "avatar.jpg",
        "file_type": "avatar",
        "mime_type": "image/jpeg",
        "file_size": 153600,
        "url": "http://localhost:9000/open436-files/2025/10/28/a1b2c3d4-e5f6-4789-a1b2-c3d4e5f67890.jpg",
        "status": "used",
        "created_at": "2025-10-28T10:30:00Z"
      },
      {
        "file_id": "b2c3d4e5-f6a7-4890-b2c3-d4e5f6a78901",
        "filename": "post-image.png",
        "file_type": "post",
        "mime_type": "image/png",
        "file_size": 524288,
        "url": "http://localhost:9000/open436-files/2025/10/28/b2c3d4e5-f6a7-4890-b2c3-d4e5f6a78901.png",
        "status": "used",
        "created_at": "2025-10-28T11:00:00Z"
      }
    ],
    "total": 2
  },
  "timestamp": "2025-10-28T12:00:00Z"
}
```

**错误响应**:

| HTTP 状态码 | 错误码   | 说明                       |
| ----------- | -------- | -------------------------- |
| 400         | 70006001 | file_ids 为空              |
| 400         | 70006002 | file_ids 超过限制（最多 100）|

**业务逻辑**:

1. 验证 `file_ids` 数组不为空且不超过 100 个
2. 使用 `WHERE id = ANY($1)` 批量查询
3. 返回文件列表（不存在的文件会被忽略）

---

## 统计接口

### 8. 获取存储统计（管理员）

**接口**: `GET /api/files/statistics`

**描述**: 获取存储空间统计数据（仅管理员）

**是否需要认证**: 是（管理员）

**查询参数**:

| 参数        | 类型    | 必填 | 说明                           |
| ----------- | ------- | ---- | ------------------------------ |
| `user_id`   | Integer | 否   | 指定用户 ID（查询该用户的统计）|
| `file_type` | String  | 否   | 指定文件类型（查询该类型的统计）|

**请求示例**:

```http
GET /api/files/statistics HTTP/1.1
Host: api.open436.com
Authorization: Bearer eyJhbGci...
```

**成功响应** (200):

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "total_files": 1523,
    "total_size": 78643200,  // 字节
    "total_size_pretty": "75.0 MB",
    "by_status": {
      "unused": {
        "count": 15,
        "size": 786432,
        "size_pretty": "768.0 KB"
      },
      "used": {
        "count": 1500,
        "size": 77594368,
        "size_pretty": "74.0 MB"
      },
      "deleted": {
        "count": 8,
        "size": 262400,
        "size_pretty": "256.3 KB"
      }
    },
    "by_type": {
      "avatar": {
        "count": 500,
        "size": 10240000,
        "size_pretty": "9.8 MB"
      },
      "post": {
        "count": 800,
        "size": 62914560,
        "size_pretty": "60.0 MB"
      },
      "reply": {
        "count": 200,
        "size": 5242880,
        "size_pretty": "5.0 MB"
      },
      "section_icon": {
        "count": 23,
        "size": 245760,
        "size_pretty": "240.0 KB"
      }
    }
  },
  "timestamp": "2025-10-28T12:00:00Z"
}
```

**错误响应**:

| HTTP 状态码 | 错误码   | 说明           |
| ----------- | -------- | -------------- |
| 403         | 70007001 | 需要管理员权限 |

---

### 9. 手动触发清理（管理员）

**接口**: `POST /api/files/cleanup`

**描述**: 手动触发清理任务（仅管理员）

**是否需要认证**: 是（管理员）

**请求体** (JSON):

```json
{
  "dry_run": false  // true: 仅列出待删除文件，不实际删除
}
```

**请求示例**:

```http
POST /api/files/cleanup HTTP/1.1
Host: api.open436.com
Authorization: Bearer eyJhbGci...
Content-Type: application/json

{
  "dry_run": false
}
```

**成功响应** (200):

```json
{
  "code": 200,
  "message": "Cleanup completed",
  "data": {
    "files_deleted": 15,
    "space_freed": 7864320,  // 字节
    "space_freed_pretty": "7.5 MB",
    "duration_ms": 1250,
    "status": "success"
  },
  "timestamp": "2025-10-28T12:05:00Z"
}
```

**错误响应**:

| HTTP 状态码 | 错误码   | 说明           |
| ----------- | -------- | -------------- |
| 403         | 70008001 | 需要管理员权限 |
| 500         | 70008002 | 清理失败       |

---

## 错误码定义

### 文件上传 (7000 1xxx)

| 错误码   | HTTP 状态码 | 说明                   |
| -------- | ----------- | ---------------------- |
| 70001001 | 400         | 文件类型不支持         |
| 70001002 | 400         | 文件类型参数无效       |
| 70001003 | 413         | 文件过大               |
| 70001004 | 400         | 文件为空               |
| 70001005 | 400         | 文件名非法             |
| 70001006 | 401         | 未认证                 |
| 70001007 | 500         | 上传失败               |

### 文件查询 (7000 2xxx)

| 错误码   | HTTP 状态码 | 说明             |
| -------- | ----------- | ---------------- |
| 70002001 | 404         | 文件不存在       |
| 70002002 | 400         | 文件 ID 格式错误 |

### 标记已使用 (7000 3xxx)

| 错误码   | HTTP 状态码 | 说明                 |
| -------- | ----------- | -------------------- |
| 70003001 | 404         | 文件不存在           |
| 70003002 | 400         | 使用类型无效         |
| 70003003 | 400         | 使用 ID 无效         |
| 70003004 | 409         | 已经标记为此使用     |

### 标记未使用 (7000 4xxx)

| 错误码   | HTTP 状态码 | 说明           |
| -------- | ----------- | -------------- |
| 70004001 | 404         | 文件不存在     |
| 70004002 | 404         | 使用关联不存在 |

### 文件删除 (7000 5xxx)

| 错误码   | HTTP 状态码 | 说明           |
| -------- | ----------- | -------------- |
| 70005001 | 404         | 文件不存在     |
| 70005002 | 403         | 需要管理员权限 |
| 70005003 | 500         | 删除失败       |

### 批量操作 (7000 6xxx)

| 错误码   | HTTP 状态码 | 说明                     |
| -------- | ----------- | ------------------------ |
| 70006001 | 400         | file_ids 为空            |
| 70006002 | 400         | file_ids 超过限制        |

### 统计接口 (7000 7xxx)

| 错误码   | HTTP 状态码 | 说明           |
| -------- | ----------- | -------------- |
| 70007001 | 403         | 需要管理员权限 |

### 清理接口 (7000 8xxx)

| 错误码   | HTTP 状态码 | 说明           |
| -------- | ----------- | -------------- |
| 70008001 | 403         | 需要管理员权限 |
| 70008002 | 500         | 清理失败       |

---

## 健康检查

### 健康检查接口

**接口**: `GET /health`

**描述**: 检查服务健康状态

**是否需要认证**: 否

**请求示例**:

```http
GET /health HTTP/1.1
Host: file-service:8007
```

**成功响应** (200):

```json
{
  "status": "ok",
  "service": "file-service",
  "version": "1.0.0",
  "timestamp": "2025-10-28T12:00:00Z",
  "checks": {
    "database": "ok",
    "storage": "ok"
  }
}
```

**失败响应** (503):

```json
{
  "status": "error",
  "service": "file-service",
  "version": "1.0.0",
  "timestamp": "2025-10-28T12:00:00Z",
  "checks": {
    "database": "error",
    "storage": "ok"
  },
  "message": "Database connection failed"
}
```

---

**文档版本**: v1.0  
**创建日期**: 2025-10-28  
**最后更新**: 2025-10-28


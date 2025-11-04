# M3 - 内容管理模块 API 文档

## 文档信息

**模块名称**：内容管理模块  
**模块编号**：M3  
**API版本**：v1.0.0  
**基础路径**：`/api/content`  
**文档创建日期**：2025-11-03
**文档最后更新**：2025-11-03

---

## 概述

内容管理模块负责论坛帖子的完整生命周期管理，包括发布、浏览、编辑、删除、置顶等功能。

### 功能范围

- 发布新帖
- 浏览帖子列表（支持分页、筛选、排序）
- 查看帖子详情
- 编辑帖子
- 删除帖子
- 查看用户的帖子列表
- 置顶/取消置顶（管理员）
- 恢复已删除的帖子（管理员）
- 永久删除帖子（管理员）
- 查看编辑历史（管理员）

### 在线文档

启动应用后，可访问以下地址查看Swagger UI：

```
http://localhost:8003/swagger-ui/index.html
```

---

## 通用说明

### 统一响应格式

所有API接口均采用统一的响应格式：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1699012345678
}
```

**字段说明**：

| 字段 | 类型 | 说明 |
|------|------|------|
| code | Integer | 状态码，200表示成功 |
| message | String | 响应消息 |
| data | Object | 响应数据，可能为null |
| timestamp | Long | 时间戳（毫秒） |

### 分页响应格式

分页查询接口的`data`字段格式：

```json
{
  "page": 1,
  "pageSize": 20,
  "total": 100,
  "totalPages": 5,
  "hasNext": true,
  "hasPrevious": false,
  "records": []
}
```

**字段说明**：

| 字段 | 类型 | 说明 |
|------|------|------|
| page | Integer | 当前页码 |
| pageSize | Integer | 每页大小 |
| total | Long | 总记录数 |
| totalPages | Integer | 总页数 |
| hasNext | Boolean | 是否有下一页 |
| hasPrevious | Boolean | 是否有上一页 |
| records | Array | 数据列表 |

### 认证说明

**当前版本**：使用临时的请求头方式传递用户信息（框架搭建阶段）

| 请求头 | 说明 | 示例 |
|--------|------|------|
| X-User-Id | 当前登录用户ID | 1001 |
| X-Is-Admin | 是否为管理员 | true/false |

**未来版本**：将使用JWT Token进行身份认证

```
Authorization: Bearer {JWT_TOKEN}
```

### 错误码定义

| 状态码 | 说明 |
|--------|------|
| 200 | 操作成功 |
| 400 | 请求参数错误 |
| 401 | 未登录或Token过期 |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## API接口列表

### 1. 发布新帖

**接口描述**：用户发布一篇新帖子

**请求方式**：`POST`

**接口路径**：`/api/content/posts`

**请求头**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| X-User-Id | Long | 是 | 当前登录用户ID |

**请求体**：

```json
{
  "title": "如何学习Spring Boot框架？",
  "content": "最近在学习Spring Boot，有哪些好的学习资源推荐？",
  "boardId": 1
}
```

**请求参数说明**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| title | String | 是 | 帖子标题，5-100个字符 |
| content | String | 是 | 帖子内容，10-50000个字符，支持富文本 |
| boardId | Long | 是 | 所属板块ID |

**响应示例**：

```json
{
  "code": 200,
  "message": "发布成功",
  "data": 1001,
  "timestamp": 1699012345678
}
```

**业务规则**：

- 用户必须登录
- 标题长度：5-100个字符
- 内容长度：10-50000个字符
- 板块ID必须有效且启用状态
- 发布成功后自动更新用户发帖数

---

### 2. 查询帖子列表

**接口描述**：分页查询帖子列表，支持板块筛选和排序

**请求方式**：`GET`

**接口路径**：`/api/content/posts`

**请求参数**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| boardId | Long | 否 | - | 板块ID，不传则查询所有板块 |
| authorId | Long | 否 | - | 作者用户ID |
| sortBy | String | 否 | latest | 排序方式：latest-最新发布，reply-最新回复，hot-热度 |
| pinnedOnly | Boolean | 否 | false | 是否只查询置顶帖子 |
| page | Integer | 否 | 1 | 页码，从1开始 |
| pageSize | Integer | 否 | 20 | 每页大小，最大50 |

**响应示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "page": 1,
    "pageSize": 20,
    "total": 100,
    "totalPages": 5,
    "hasNext": true,
    "hasPrevious": false,
    "records": [
      {
        "id": 1001,
        "title": "如何学习Spring Boot框架？",
        "contentPreview": "最近在学习Spring Boot，有哪些好的学习资源推荐？...",
        "authorId": 1,
        "authorName": "张三",
        "authorAvatar": "https://example.com/avatar.jpg",
        "boardId": 1,
        "boardName": "Java技术",
        "pinType": 0,
        "viewCount": 1250,
        "replyCount": 15,
        "likeCount": 28,
        "createdAt": "2025-11-03T10:30:00",
        "lastEditedAt": null,
        "isEdited": false
      }
    ]
  },
  "timestamp": 1699012345678
}
```

**排序方式说明**：

- `latest`：按发布时间倒序（最新在前），置顶帖子优先
- `reply`：按最后回复时间倒序，置顶帖子优先
- `hot`：按热度排序（综合点赞数、回复数、浏览数），置顶帖子优先

---

### 3. 查看帖子详情

**接口描述**：查看指定帖子的完整信息，自动记录浏览量

**请求方式**：`GET`

**接口路径**：`/api/content/posts/{id}`

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 帖子ID |

**请求头**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| X-User-Id | Long | 否 | 当前登录用户ID（未登录可不传） |

**响应示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1001,
    "title": "如何学习Spring Boot框架？",
    "content": "最近在学习Spring Boot，有哪些好的学习资源推荐？...",
    "authorId": 1,
    "authorName": "张三",
    "authorAvatar": "https://example.com/avatar.jpg",
    "boardId": 1,
    "boardName": "Java技术",
    "pinType": 0,
    "isDeleted": false,
    "deleteReason": null,
    "viewCount": 1251,
    "replyCount": 15,
    "likeCount": 28,
    "editCount": 2,
    "createdAt": "2025-11-03T10:30:00",
    "updatedAt": "2025-11-03T15:45:00",
    "lastEditedAt": "2025-11-03T15:45:00",
    "lastEditedBy": 1,
    "isEdited": true
  },
  "timestamp": 1699012345678
}
```

**业务规则**：

- 每次访问浏览量+1
- 同一用户10分钟内多次访问不重复计数
- 作者本人访问不计入浏览量
- 已删除的帖子仅管理员可见

---

### 4. 编辑帖子

**接口描述**：编辑指定帖子的内容，仅作者本人或管理员可操作

**请求方式**：`PUT`

**接口路径**：`/api/content/posts/{id}`

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 帖子ID |

**请求头**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| X-User-Id | Long | 是 | 当前登录用户ID |
| X-Is-Admin | Boolean | 否 | 是否为管理员 |

**请求体**：

```json
{
  "title": "如何学习Spring Boot框架（修改版）",
  "content": "更新后的内容...",
  "boardId": 1,
  "editReason": "修正错别字"
}
```

**请求参数说明**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| title | String | 否 | 帖子标题，5-100个字符 |
| content | String | 否 | 帖子内容，10-50000个字符 |
| boardId | Long | 否 | 所属板块ID |
| editReason | String | 否 | 编辑原因 |

**响应示例**：

```json
{
  "code": 200,
  "message": "编辑成功",
  "data": null,
  "timestamp": 1699012345678
}
```

**业务规则**：

- 仅作者本人或管理员可编辑
- 发布后24小时内可无限制编辑
- 24小时后每篇帖子最多编辑5次（管理员无限制）
- 每次编辑保存历史版本
- 编辑后显示"已编辑"标识

---

### 5. 删除帖子

**接口描述**：删除指定帖子（软删除），仅作者本人或管理员可操作

**请求方式**：`DELETE`

**接口路径**：`/api/content/posts/{id}`

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 帖子ID |

**请求头**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| X-User-Id | Long | 是 | 当前登录用户ID |
| X-Is-Admin | Boolean | 否 | 是否为管理员 |

**响应示例**：

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null,
  "timestamp": 1699012345678
}
```

**业务规则**：

- 仅作者本人或管理员可删除
- 默认执行软删除（数据保留）
- 删除后用户发帖数-1
- 软删除的帖子可由管理员恢复

---

### 6. 查询用户的帖子列表

**接口描述**：查询指定用户发布的所有帖子

**请求方式**：`GET`

**接口路径**：`/api/content/posts/user/{userId}`

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | Long | 是 | 用户ID |

**请求参数**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 20 | 每页大小 |

**响应示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "page": 1,
    "pageSize": 20,
    "total": 50,
    "totalPages": 3,
    "hasNext": true,
    "hasPrevious": false,
    "records": [
      {
        "id": 1001,
        "title": "如何学习Spring Boot框架？",
        "contentPreview": "最近在学习Spring Boot...",
        "authorId": 1,
        "authorName": "张三",
        "authorAvatar": "https://example.com/avatar.jpg",
        "boardId": 1,
        "boardName": "Java技术",
        "pinType": 0,
        "viewCount": 1250,
        "replyCount": 15,
        "likeCount": 28,
        "createdAt": "2025-11-03T10:30:00",
        "lastEditedAt": null,
        "isEdited": false
      }
    ]
  },
  "timestamp": 1699012345678
}
```

**业务规则**：

- 仅显示该用户发布的帖子
- 不显示已删除的帖子（本人查看自己的可以看到）
- 按发布时间倒序排列

---

## 管理员API

### 7. 置顶帖子

**接口描述**：将帖子设置为置顶状态，仅管理员可操作

**请求方式**：`PUT`

**接口路径**：`/api/content/posts/manage/{id}/pin`

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 帖子ID |

**请求头**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| X-User-Id | Long | 是 | 管理员用户ID |

**请求体**：

```json
{
  "pinType": 1
}
```

**请求参数说明**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pinType | Integer | 是 | 置顶类型：1-板块置顶，2-全局置顶 |

**响应示例**：

```json
{
  "code": 200,
  "message": "置顶成功",
  "data": null,
  "timestamp": 1699012345678
}
```

**业务规则**：

- 仅管理员可操作
- 全局置顶最多3篇
- 每个板块置顶最多5篇
- 已删除的帖子无法置顶

---

### 8. 取消置顶

**接口描述**：取消帖子的置顶状态，仅管理员可操作

**请求方式**：`DELETE`

**接口路径**：`/api/content/posts/manage/{id}/pin`

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 帖子ID |

**请求头**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| X-User-Id | Long | 是 | 管理员用户ID |

**响应示例**：

```json
{
  "code": 200,
  "message": "已取消置顶",
  "data": null,
  "timestamp": 1699012345678
}
```

---

### 9. 恢复已删除的帖子

**接口描述**：恢复软删除的帖子，仅管理员可操作

**请求方式**：`POST`

**接口路径**：`/api/content/posts/manage/{id}/restore`

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 帖子ID |

**请求头**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| X-User-Id | Long | 是 | 管理员用户ID |

**响应示例**：

```json
{
  "code": 200,
  "message": "恢复成功",
  "data": null,
  "timestamp": 1699012345678
}
```

**业务规则**：

- 仅管理员可操作
- 仅软删除的帖子可恢复
- 恢复后帖子重新显示在列表中

---

### 10. 永久删除帖子

**接口描述**：硬删除帖子，数据无法恢复，仅管理员可操作

**请求方式**：`DELETE`

**接口路径**：`/api/content/posts/manage/{id}/permanent`

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 帖子ID |

**请求头**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| X-User-Id | Long | 是 | 管理员用户ID |

**请求体**：

```json
{
  "reason": "违反社区规定",
  "permanent": true
}
```

**请求参数说明**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| reason | String | 是 | 删除原因 |
| permanent | Boolean | 否 | 是否硬删除，默认false |

**响应示例**：

```json
{
  "code": 200,
  "message": "永久删除成功",
  "data": null,
  "timestamp": 1699012345678
}
```

**业务规则**：

- 仅管理员可操作
- 硬删除后数据不可恢复
- 同时删除帖子的回复、点赞、收藏等相关数据

---

### 11. 查看帖子编辑历史

**接口描述**：查看帖子的所有编辑记录，仅管理员可操作

**请求方式**：`GET`

**接口路径**：`/api/content/posts/manage/{id}/edit-history`

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 帖子ID |

**响应示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "postId": 1001,
      "version": 1,
      "oldTitle": "如何学习Spring Boot？",
      "oldContent": "原始内容...",
      "oldBoardId": 1,
      "oldBoardName": "Java技术",
      "editedBy": 1,
      "editorName": "张三",
      "editedAt": "2025-11-03T15:45:00",
      "editReason": "修正错别字"
    }
  ],
  "timestamp": 1699012345678
}
```

**业务规则**：

- 仅管理员可查看
- 按编辑时间倒序排列
- 显示每个版本的修改内容

---

## 数据模型

### 帖子列表项（PostListVO）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 帖子ID |
| title | String | 帖子标题 |
| contentPreview | String | 内容预览（前200字） |
| authorId | Long | 作者用户ID |
| authorName | String | 作者昵称 |
| authorAvatar | String | 作者头像URL |
| boardId | Long | 所属板块ID |
| boardName | String | 所属板块名称 |
| pinType | Integer | 置顶类型：0-不置顶，1-板块置顶，2-全局置顶 |
| viewCount | Long | 浏览量 |
| replyCount | Integer | 回复数 |
| likeCount | Integer | 点赞数 |
| createdAt | LocalDateTime | 发布时间 |
| lastEditedAt | LocalDateTime | 最后编辑时间 |
| isEdited | Boolean | 是否已编辑 |

### 帖子详情（PostDetailVO）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 帖子ID |
| title | String | 帖子标题 |
| content | String | 帖子完整内容 |
| authorId | Long | 作者用户ID |
| authorName | String | 作者昵称 |
| authorAvatar | String | 作者头像URL |
| boardId | Long | 所属板块ID |
| boardName | String | 所属板块名称 |
| pinType | Integer | 置顶类型 |
| isDeleted | Boolean | 是否已删除 |
| deleteReason | String | 删除原因 |
| viewCount | Long | 浏览量 |
| replyCount | Integer | 回复数 |
| likeCount | Integer | 点赞数 |
| editCount | Integer | 编辑次数 |
| createdAt | LocalDateTime | 发布时间 |
| updatedAt | LocalDateTime | 更新时间 |
| lastEditedAt | LocalDateTime | 最后编辑时间 |
| lastEditedBy | Long | 最后编辑者ID |
| isEdited | Boolean | 是否已编辑 |

---

## 错误处理

### 常见错误响应

#### 400 Bad Request - 参数错误

```json
{
  "code": 400,
  "message": "标题长度必须在5-100个字符之间",
  "data": null,
  "timestamp": 1699012345678
}
```

#### 401 Unauthorized - 未登录

```json
{
  "code": 401,
  "message": "用户未登录，请先登录",
  "data": null,
  "timestamp": 1699012345678
}
```

#### 403 Forbidden - 权限不足

```json
{
  "code": 403,
  "message": "权限不足，仅作者本人或管理员可操作",
  "data": null,
  "timestamp": 1699012345678
}
```

#### 404 Not Found - 资源不存在

```json
{
  "code": 404,
  "message": "帖子 [id=1001] 不存在",
  "data": null,
  "timestamp": 1699012345678
}
```

#### 500 Internal Server Error - 服务器错误

```json
{
  "code": 500,
  "message": "系统内部错误，请联系管理员",
  "data": null,
  "timestamp": 1699012345678
}
```

---

## 附录

### A. 业务规则汇总

#### 发布帖子

- 标题长度：5-100个字符
- 内容长度：10-50000个字符
- 图片：最多10张，每张不超过5MB，支持JPG/PNG/GIF

#### 编辑帖子

- 发布后24小时内可无限制编辑
- 24小时后每篇最多编辑5次（管理员无限制）
- 每次编辑保存历史版本

#### 删除帖子

- 普通用户执行软删除
- 管理员可选择软删除或硬删除
- 软删除可恢复，硬删除不可恢复

#### 置顶帖子

- 全局置顶最多3篇
- 每个板块置顶最多5篇
- 置顶帖子在列表中优先显示

#### 浏览量统计

- 每次访问浏览量+1
- 同一用户10分钟内多次访问不重复计数
- 作者本人访问不计入浏览量

### B. 技术栈

- **后端框架**：Spring Boot 3.5.7
- **数据库**：PostgreSQL
- **ORM框架**：Spring Data JPA / Hibernate
- **API文档**：Springdoc OpenAPI 3.0 (Swagger)
- **构建工具**：Maven
- **JDK版本**：Java 17

### C. 数据库表结构

详见：`Open436-Content/src/main/resources/db/schema.sql`

---

**文档版本**：v1.0.0  
**最后更新**：2025-11-03  
**维护人员**：Open436 开发团队


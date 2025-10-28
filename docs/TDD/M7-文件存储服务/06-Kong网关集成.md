# M7 文件存储服务 - Kong 网关集成

## 文档信息

**服务名称**: 文件存储服务 (file-service)  
**网关**: Kong Gateway 3.x  
**认证方式**: Sa-Token (M1) + Kong 请求转发  
**版本**: v1.0

---

## 目录

1. [集成概述](#集成概述)
2. [Kong 配置](#kong-配置)
3. [路由规则](#路由规则)
4. [认证流程](#认证流程)
5. [请求头传递](#请求头传递)
6. [限流策略](#限流策略)
7. [测试验证](#测试验证)
8. [故障排查](#故障排查)

---

## 集成概述

### 架构图

```
客户端
  ↓ Authorization: Bearer {sa-token}
┌─────────────────────────────────────┐
│      Kong API Gateway (8000)         │
│  ┌────────────────────────────────┐  │
│  │  1. 接收请求                    │  │
│  │  2. 路由匹配（/api/files/*）   │  │
│  │  3. 调用 M1 验证 Token          │  │
│  │  4. 注入用户信息到请求头        │  │
│  │  5. 转发到 M7 服务              │  │
│  └────────────────────────────────┘  │
└─────────────┬───────────────────────┘
              │
     ┌────────┼────────┐
     ↓        ↓        ↓
  ┌─────┐ ┌─────┐ ┌──────┐
  │ M1  │ │ M7  │ │其他服务│
  │认证 │ │文件 │ │      │
  └─────┘ └─────┘ └──────┘
```

### 集成方式

| 功能 | 实现方式 |
|------|---------|
| **路由转发** | Kong 路由匹配 `/api/files/*` → 转发到 M7 (8007端口) |
| **认证验证** | Kong 转发 Token 到 M1 `/api/auth/verify` 接口验证 |
| **用户信息** | Kong 从 M1 响应中提取用户信息，注入到请求头 |
| **权限控制** | M7 从请求头获取用户角色，判断是否为管理员 |

---

## Kong 配置

### 服务注册

#### 使用 Admin API

```bash
# 注册 M7 文件存储服务
curl -X POST http://localhost:8001/services \
  --data name=file-service \
  --data protocol=http \
  --data host=file-service \
  --data port=8007 \
  --data path=/ \
  --data retries=2 \
  --data connect_timeout=5000 \
  --data write_timeout=60000 \
  --data read_timeout=60000

# 响应
{
  "id": "...",
  "name": "file-service",
  "host": "file-service",
  "port": 8007,
  "protocol": "http"
}
```

#### 使用声明式配置

```yaml
# kong/kong.yml
services:
  - name: file-service
    url: http://file-service:8007
    tags:
      - file
      - storage
      - m7
    connect_timeout: 5000
    write_timeout: 60000   # 文件上传可能耗时较长
    read_timeout: 60000
    retries: 2
```

---

## 路由规则

### 路由分类

M7 的路由分为 **3 类**：

| 类别 | 路由名称 | 路径 | 方法 | 认证 | 说明 |
|------|---------|------|------|------|------|
| **公开** | `file-public` | `/api/files/:id`<br>`/api/files/:id/url`<br>`/api/files/batch-info` | GET<br>POST | ❌ 否 | 文件信息查询 |
| **受保护** | `file-protected` | `/api/files/upload`<br>`/api/files/:id/mark-used`<br>`/api/files/:id/mark-unused` | POST | ✅ 是 | 文件上传和标记 |
| **管理员** | `file-admin` | `/api/files/:id` (DELETE)<br>`/api/files/statistics`<br>`/api/files/cleanup` | DELETE<br>GET<br>POST | ✅ 管理员 | 管理接口 |

### 路由配置详情

#### 1. 公开路由（文件查询）

```yaml
routes:
  - name: file-public
    paths:
      - /api/files/[a-f0-9-]{36}$        # GET /api/files/:id
      - /api/files/[a-f0-9-]{36}/url$    # GET /api/files/:id/url
      - /api/files/batch-info            # POST /api/files/batch-info
    strip_path: false
    methods:
      - GET
      - POST
    tags:
      - public
      - no-auth
    regex_priority: 10
```

**说明**：
- 使用正则表达式匹配 UUID 格式 (`[a-f0-9-]{36}`)
- 无需认证，任何人都可以访问
- 适用于前端展示文件（如帖子中的图片）

#### 2. 受保护路由（需要登录）

```yaml
routes:
  - name: file-protected
    paths:
      - /api/files/upload
      - /api/files/[a-f0-9-]{36}/mark-used
      - /api/files/[a-f0-9-]{36}/mark-unused
    strip_path: false
    methods:
      - POST
    tags:
      - protected
      - requires-auth
    regex_priority: 20
    plugins:
      # 请求转换（添加 Kong 标识）
      - name: request-transformer
        config:
          add:
            headers:
              - "X-Kong-Gateway: true"
      
      # 限流（防止上传滥用）
      - name: rate-limiting
        config:
          minute: 20
          hour: 100
          policy: local
```

**说明**：
- 需要携带 Sa-Token
- Kong 调用 M1 验证 Token
- 添加限流保护（20次/分钟）

#### 3. 管理员路由（需要管理员权限）

```yaml
routes:
  - name: file-admin
    paths:
      - /api/files/statistics
      - /api/files/cleanup
    strip_path: false
    methods:
      - GET
      - POST
      - DELETE
    tags:
      - admin
      - requires-admin
    regex_priority: 30
    plugins:
      - name: request-transformer
        config:
          add:
            headers:
              - "X-Kong-Gateway: true"
              - "X-Requires-Admin: true"
```

**说明**：
- 需要管理员角色
- Kong 转发请求头 `X-User-Role: admin`
- M7 服务验证角色

---

## 认证流程

### M1 Sa-Token 验证流程

```
┌────────┐                 ┌──────┐                 ┌────┐     ┌────┐
│ 客户端 │                 │ Kong │                 │ M1 │     │ M7 │
└───┬────┘                 └──┬───┘                 └─┬──┘     └─┬──┘
    │                         │                       │          │
    │ 1. POST /api/files/upload                      │          │
    │    Authorization: Bearer {sa-token}             │          │
    ├────────────────────────>│                       │          │
    │                         │                       │          │
    │                         │ 2. GET /api/auth/verify         │
    │                         │    Authorization: Bearer {token} │
    │                         ├──────────────────────>│          │
    │                         │                       │          │
    │                         │ 3. 返回用户信息        │          │
    │                         │    {userId:1, role:"user"}       │
    │                         │<──────────────────────┤          │
    │                         │                       │          │
    │                         │ 4. POST /api/files/upload        │
    │                         │    X-User-Id: 1                  │
    │                         │    X-User-Role: user             │
    │                         ├─────────────────────────────────>│
    │                         │                       │          │
    │                         │ 5. 返回上传结果                  │
    │                         │<─────────────────────────────────┤
    │                         │                       │          │
    │ 6. 返回结果              │                       │          │
    │<────────────────────────┤                       │          │
```

### 实现方式

由于 M1 使用 Sa-Token 而非标准 JWT，Kong 无法直接验证。需要使用以下方式：

#### 方案1：自定义插件（推荐）

创建自定义 Kong 插件，调用 M1 验证接口：

```lua
-- kong/plugins/sa-token-auth/handler.lua
local http = require "resty.http"

local function verify_sa_token(token)
  local httpc = http.new()
  local res, err = httpc:request_uri("http://auth-service:8001/api/auth/verify", {
    method = "GET",
    headers = {
      ["Authorization"] = "Bearer " .. token
    }
  })
  
  if not res then
    return nil, err
  end
  
  if res.status == 200 then
    local cjson = require "cjson"
    return cjson.decode(res.body)
  else
    return nil, "Invalid token"
  end
end

function plugin:access(conf)
  local token = kong.request.get_header("Authorization")
  
  if not token then
    return kong.response.exit(401, {message = "No token provided"})
  end
  
  token = string.gsub(token, "Bearer ", "")
  
  local user_info, err = verify_sa_token(token)
  
  if not user_info then
    return kong.response.exit(401, {message = "Invalid token"})
  end
  
  -- 注入用户信息到请求头
  kong.service.request.set_header("X-User-Id", user_info.data.user.id)
  kong.service.request.set_header("X-User-Role", user_info.data.user.role)
end
```

#### 方案2：使用 Kong 函数插件（简化版）

```yaml
plugins:
  - name: pre-function
    route: file-protected
    config:
      access:
        - |
          local http = require "resty.http"
          local httpc = http.new()
          
          local token = kong.request.get_header("Authorization")
          if not token then
            return kong.response.exit(401, {message = "No token"})
          end
          
          -- 调用 M1 验证
          local res = httpc:request_uri("http://auth-service:8001/api/auth/verify", {
            headers = {["Authorization"] = token}
          })
          
          if res.status == 200 then
            local cjson = require "cjson"
            local data = cjson.decode(res.body)
            kong.service.request.set_header("X-User-Id", tostring(data.data.userId))
            kong.service.request.set_header("X-User-Role", data.data.role)
          else
            return kong.response.exit(401, {message = "Invalid token"})
          end
```

#### 方案3：简化方案（开发环境）

直接转发 Authorization header 到 M7，由 M7 调用 M1 验证（不推荐生产环境）。

---

## 请求头传递

### Kong → M7 传递的请求头

| Header | 来源 | 说明 | 示例 |
|--------|------|------|------|
| `X-User-Id` | M1 验证响应 | 当前用户 ID | `1` |
| `X-User-Role` | M1 验证响应 | 用户角色 | `admin` / `user` |
| `X-Kong-Gateway` | Kong 添加 | Kong 网关标识 | `true` |
| `X-Requires-Admin` | Kong 添加（管理员路由） | 管理员权限要求 | `true` |
| `Authorization` | 客户端 | 原始 Token（透传） | `Bearer {token}` |

### M7 服务如何使用

```rust
// src/middleware/auth.rs

/// 从 Kong 传递的请求头中提取用户 ID
pub fn get_current_user_id(req: &HttpRequest) -> Result<i32, FileError> {
    req.headers()
        .get("X-User-Id")
        .and_then(|v| v.to_str().ok())
        .and_then(|s| s.parse::<i32>().ok())
        .ok_or(FileError::Unauthorized)
}

/// 检查是否为管理员
pub fn is_admin(req: &HttpRequest) -> Result<bool, FileError> {
    let role = req
        .headers()
        .get("X-User-Role")
        .and_then(|v| v.to_str().ok())
        .unwrap_or("");
    
    Ok(role == "admin")
}

// 使用示例
pub async fn upload_handler(req: HttpRequest, ...) -> Result<HttpResponse> {
    // 获取当前用户 ID
    let user_id = get_current_user_id(&req)?;
    
    // 处理上传逻辑...
}

pub async fn statistics_handler(req: HttpRequest, ...) -> Result<HttpResponse> {
    // 验证管理员权限
    if !is_admin(&req)? {
        return Err(FileError::Forbidden);
    }
    
    // 返回统计数据...
}
```

---

## 限流策略

### 不同接口的限流配置

| 接口类别 | 限流规则 | 说明 |
|---------|---------|------|
| **全局** | 100次/分钟 | 保护所有接口 |
| **文件上传** | 20次/分钟 | 防止上传滥用 |
| **批量查询** | 60次/分钟 | 防止批量爬取 |
| **管理员接口** | 无限制 | 内部使用 |

### 配置示例

```yaml
# 文件上传路由限流
- name: file-protected
  plugins:
    - name: rate-limiting
      config:
        minute: 20
        hour: 100
        policy: local
        fault_tolerant: true
        hide_client_headers: false
```

### 限流响应

当触发限流时，Kong 返回：

```http
HTTP/1.1 429 Too Many Requests
X-RateLimit-Limit-Minute: 20
X-RateLimit-Remaining-Minute: 0
Content-Type: application/json

{
  "message": "API rate limit exceeded"
}
```

---

## 测试验证

### 1. 验证服务注册

```bash
# 查看所有服务
curl http://localhost:8001/services | jq '.data[] | {name, host, port}'

# 预期输出包含:
{
  "name": "auth-service",
  "host": "auth-service",
  "port": 8001
}
{
  "name": "file-service",
  "host": "file-service",
  "port": 8007
}
```

### 2. 验证路由配置

```bash
# 查看 M7 相关路由
curl http://localhost:8001/routes | jq '.data[] | select(.service.name=="file-service") | {name, paths, methods}'

# 预期输出:
{
  "name": "file-public",
  "paths": ["/api/files/[a-f0-9-]{36}", ...],
  "methods": ["GET", "POST"]
}
```

### 3. 测试公开接口（无需认证）

```bash
# 测试文件信息查询
curl http://localhost:8000/api/files/a1b2c3d4-e5f6-4789-a1b2-c3d4e5f67890

# 预期: 返回 200 或 404（如果文件不存在）

# 测试批量查询
curl -X POST http://localhost:8000/api/files/batch-info \
  -H "Content-Type: application/json" \
  -d '{
    "file_ids": ["a1b2c3d4-e5f6-4789-a1b2-c3d4e5f67890"]
  }'

# 预期: 返回 200 和文件列表
```

### 4. 测试受保护接口（需要认证）

```bash
# 先登录获取 Token
TOKEN=$(curl -X POST http://localhost:8000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }' | jq -r '.data.token')

# 测试文件上传
curl -X POST http://localhost:8000/api/files/upload \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@test.jpg" \
  -F "file_type=avatar"

# 预期: 返回 201 和文件信息
```

### 5. 测试管理员接口

```bash
# 使用管理员账号登录
ADMIN_TOKEN=$(curl -X POST http://localhost:8000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }' | jq -r '.data.token')

# 测试统计接口
curl http://localhost:8000/api/files/statistics \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# 预期: 返回 200 和统计数据

# 测试清理接口
curl -X POST http://localhost:8000/api/files/cleanup \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"dry_run": true}'

# 预期: 返回 200 和清理结果
```

### 6. 测试限流

```bash
# 快速连续上传 25 次
for i in {1..25}; do
  curl -X POST http://localhost:8000/api/files/upload \
    -H "Authorization: Bearer $TOKEN" \
    -F "file=@test.jpg" \
    -F "file_type=post"
done

# 预期: 前 20 次成功，后 5 次返回 429 Too Many Requests
```

---

## 故障排查

### 问题1: 路由不匹配

**症状**: 访问 `/api/files/upload` 返回 404

**排查**:

```bash
# 1. 检查路由是否存在
curl http://localhost:8001/routes | jq '.data[] | select(.name | contains("file"))'

# 2. 测试路径匹配
curl -v http://localhost:8000/api/files/upload

# 3. 查看 Kong 日志
docker logs kong -f
```

**解决**: 确认路由配置正确，regex_priority 设置合理

### 问题2: 认证失败

**症状**: 上传文件返回 401 Unauthorized

**排查**:

```bash
# 1. 直接测试 M1 验证接口
curl -X GET http://localhost:8001/api/auth/verify \
  -H "Authorization: Bearer $TOKEN"

# 2. 检查 Kong 是否正确转发请求头
docker logs kong | grep "Authorization"

# 3. 验证 Token 是否有效
```

**解决**: 确保 Token 有效，M1 服务正常运行

### 问题3: 文件上传失败

**症状**: 上传文件返回 413 Request Entity Too Large

**排查**:

```bash
# 检查请求大小限制插件
curl http://localhost:8001/plugins | jq '.data[] | select(.name=="request-size-limiting")'
```

**解决**:

```bash
# 调整限制为 20 MB
curl -X PATCH http://localhost:8001/plugins/{plugin-id} \
  --data config.allowed_payload_size=20
```

### 问题4: 跨域错误

**症状**: 浏览器 Console 显示 CORS 错误

**排查**:

```bash
# 检查 CORS 插件
curl http://localhost:8001/plugins | jq '.data[] | select(.name=="cors")'
```

**解决**:

```bash
# 添加允许的源
curl -X POST http://localhost:8001/plugins \
  --data name=cors \
  --data 'config.origins=http://your-frontend.com'
```

---

## 配置脚本使用

### 初始化配置

```bash
# 1. 启动 Kong
cd kong
docker-compose up -d

# 2. 等待 Kong 就绪
sleep 15

# 3. 运行配置脚本
chmod +x kong-config.sh
./kong-config.sh
```

### 使用声明式配置

```bash
# 1. 安装 deck 工具
brew install deck  # macOS
# 或下载二进制: https://github.com/Kong/deck/releases

# 2. 验证配置文件
deck validate -s kong.yml

# 3. 查看差异（不应用）
deck diff -s kong.yml

# 4. 同步配置
deck sync -s kong.yml

# 5. 导出当前配置（备份）
deck dump -o kong-backup.yml
```

### 更新配置

```bash
# 修改 kong.yml 后重新同步
deck sync -s kong.yml

# 查看变更
deck diff -s kong.yml
```

---

## 生产环境注意事项

### 1. 使用 Redis 策略限流

```yaml
plugins:
  - name: rate-limiting
    config:
      minute: 100
      policy: redis       # 使用 Redis（多实例共享）
      redis_host: redis
      redis_port: 6379
      redis_password: your-redis-password
      redis_database: 0
```

### 2. 启用健康检查

```yaml
services:
  - name: file-service
    url: http://file-service:8007
    health_checks:
      active:
        healthy:
          interval: 10
          successes: 2
        unhealthy:
          interval: 5
          tcp_failures: 3
          http_failures: 3
```

### 3. 配置负载均衡（多实例）

```yaml
upstreams:
  - name: file-service-upstream
    algorithm: round-robin
    targets:
      - target: file-service-1:8007
        weight: 100
      - target: file-service-2:8007
        weight: 100

services:
  - name: file-service
    host: file-service-upstream
```

### 4. 启用日志插件

```yaml
plugins:
  - name: file-log
    config:
      path: /var/log/kong/file-service-access.log
      reopen: true
  
  - name: http-log
    service: file-service
    config:
      http_endpoint: http://logstash:8080
      method: POST
      content_type: application/json
```

---

## 完整配置检查清单

### 配置前

- [ ] Kong Gateway 已启动
- [ ] M1 认证服务已部署
- [ ] M7 文件存储服务已部署
- [ ] PostgreSQL 已配置
- [ ] Minio 已配置

### 配置中

- [ ] M7 服务已注册到 Kong
- [ ] 公开路由已配置（文件查询）
- [ ] 受保护路由已配置（上传、标记）
- [ ] 管理员路由已配置（删除、统计）
- [ ] 认证插件已启用
- [ ] 限流插件已配置
- [ ] CORS 插件已配置

### 配置后

- [ ] 健康检查通过
- [ ] 公开接口可访问
- [ ] 受保护接口需要 Token
- [ ] 管理员接口需要管理员权限
- [ ] 限流正常工作
- [ ] 跨域请求正常

---

**文档版本**: v1.0  
**创建日期**: 2025-10-28  
**最后更新**: 2025-10-28


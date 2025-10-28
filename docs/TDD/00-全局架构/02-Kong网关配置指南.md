# Kong 网关配置指南

## 文档信息

**文档版本**: v1.0  
**创建日期**: 2025-10-23  
**文档类型**: 技术设计文档（TDD）  
**适用范围**: 运维团队、后端开发团队

---

## 目录

1. [Kong 简介](#kong-简介)
2. [Kong 核心概念](#kong-核心概念)
3. [Kong 安装部署](#kong-安装部署)
4. [服务注册配置](#服务注册配置)
5. [路由配置](#路由配置)
6. [JWT 认证插件](#jwt-认证插件)
7. [限流插件](#限流插件)
8. [其他常用插件](#其他常用插件)
9. [配置管理最佳实践](#配置管理最佳实践)

---

## Kong 简介

### 1.1 什么是 Kong

Kong 是一个**云原生、高性能、可扩展的 API 网关**，基于 Nginx 和 OpenResty 构建。

**核心功能**：
- **流量管理**：路由、负载均衡、限流
- **安全认证**：JWT、OAuth2、API Key
- **可观测性**：日志、监控、追踪
- **插件生态**：丰富的官方和社区插件

### 1.2 在 Open436 中的作用

```
客户端请求
    ↓
┌───────────────────────────────────────┐
│          Kong API Gateway             │
│  ┌─────────────────────────────────┐  │
│  │  1. 路由分发                     │  │
│  │  2. JWT 认证验证                 │  │
│  │  3. 限流熔断                     │  │
│  │  4. 日志记录                     │  │
│  │  5. 负载均衡                     │  │
│  └─────────────────────────────────┘  │
└───────────────┬───────────────────────┘
                │
    ┌───────────┼───────────┐
    ↓           ↓           ↓
 auth-service user-service post-service
```

---

## Kong 核心概念

### 2.1 核心对象关系

```
Service (服务)
    ↓ 关联
Route (路由)
    ↓ 应用
Plugin (插件)
    ↓ 作用于
Consumer (消费者)
```

### 2.2 对象说明

| 对象 | 说明 | 示例 |
|------|------|------|
| **Service** | 后端微服务的抽象，定义服务的协议、主机、端口 | `auth-service` → `http://auth-service:8001` |
| **Route** | 请求路由规则，定义如何将请求转发到 Service | `/api/auth/*` → `auth-service` |
| **Plugin** | 功能插件，提供认证、限流、日志等功能 | JWT 插件、Rate Limiting 插件 |
| **Consumer** | API 消费者（用户），用于认证和授权 | 用户 ID、API Key |
| **Upstream** | 上游服务集群，用于负载均衡 | `auth-service-upstream` |
| **Target** | Upstream 中的具体服务实例 | `192.168.1.10:8001` |

---

## Kong 安装部署

### 3.1 使用 Docker Compose 部署（推荐）

**docker-compose.yml**:

```yaml
version: '3.8'

services:
  # Kong 数据库
  kong-database:
    image: postgres:14-alpine
    container_name: kong-database
    environment:
      POSTGRES_USER: kong
      POSTGRES_DB: kong
      POSTGRES_PASSWORD: kong_password
    volumes:
      - kong-db-data:/var/lib/postgresql/data
    networks:
      - kong-net
    healthcheck:
      test: ["CMD", "pg_isready", "-U", "kong"]
      interval: 10s
      timeout: 5s
      retries: 5

  # Kong 数据库初始化
  kong-migration:
    image: kong:3.4
    container_name: kong-migration
    command: kong migrations bootstrap
    environment:
      KONG_DATABASE: postgres
      KONG_PG_HOST: kong-database
      KONG_PG_USER: kong
      KONG_PG_PASSWORD: kong_password
      KONG_PG_DATABASE: kong
    networks:
      - kong-net
    depends_on:
      kong-database:
        condition: service_healthy
    restart: on-failure

  # Kong Gateway
  kong:
    image: kong:3.4
    container_name: kong
    environment:
      KONG_DATABASE: postgres
      KONG_PG_HOST: kong-database
      KONG_PG_USER: kong
      KONG_PG_PASSWORD: kong_password
      KONG_PG_DATABASE: kong
      KONG_PROXY_ACCESS_LOG: /dev/stdout
      KONG_ADMIN_ACCESS_LOG: /dev/stdout
      KONG_PROXY_ERROR_LOG: /dev/stderr
      KONG_ADMIN_ERROR_LOG: /dev/stderr
      KONG_ADMIN_LISTEN: 0.0.0.0:8001
      KONG_ADMIN_GUI_URL: http://localhost:8002
    ports:
      - "8000:8000"  # HTTP 代理端口
      - "8443:8443"  # HTTPS 代理端口
      - "8001:8001"  # Admin API
      - "8444:8444"  # Admin API HTTPS
    networks:
      - kong-net
    depends_on:
      kong-database:
        condition: service_healthy
      kong-migration:
        condition: service_completed_successfully
    healthcheck:
      test: ["CMD", "kong", "health"]
      interval: 10s
      timeout: 5s
      retries: 5

  # Kong Manager (可选，可视化管理界面)
  kong-manager:
    image: pantsel/konga:latest
    container_name: kong-manager
    environment:
      NODE_ENV: production
      DB_ADAPTER: postgres
      DB_HOST: kong-database
      DB_USER: kong
      DB_PASSWORD: kong_password
      DB_DATABASE: konga
    ports:
      - "1337:1337"
    networks:
      - kong-net
    depends_on:
      - kong-database

networks:
  kong-net:
    driver: bridge

volumes:
  kong-db-data:
```

### 3.2 启动 Kong

```bash
# 启动所有服务
docker-compose up -d

# 检查 Kong 状态
curl -i http://localhost:8001/

# 预期响应
HTTP/1.1 200 OK
```

### 3.3 访问地址

| 服务 | 地址 | 说明 |
|------|------|------|
| Kong Proxy | http://localhost:8000 | 客户端请求入口 |
| Kong Admin API | http://localhost:8001 | 管理接口 |
| Kong Manager | http://localhost:1337 | 可视化管理界面 |

---

## 服务注册配置

### 4.1 注册认证服务 (S1)

#### 方式一：使用 Admin API

```bash
# 1. 创建 Service
curl -X POST http://localhost:8001/services \
  --data name=auth-service \
  --data protocol=http \
  --data host=auth-service \
  --data port=8001 \
  --data path=/

# 响应示例
{
  "id": "4e13f54a-bbf1-47a8-8777-255fed7116f2",
  "name": "auth-service",
  "protocol": "http",
  "host": "auth-service",
  "port": 8001,
  "path": "/",
  "created_at": 1698000000
}
```

#### 方式二：使用声明式配置（推荐）

**kong.yml**:

```yaml
_format_version: "3.0"

services:
  - name: auth-service
    url: http://auth-service:8001
    tags:
      - auth
      - core
    
  - name: user-service
    url: http://user-service:8002
    tags:
      - user
      - core
    
  - name: post-service
    url: http://post-service:8003
    tags:
      - post
      - core
    
  - name: interaction-service
    url: http://interaction-service:8004
    tags:
      - interaction
    
  - name: file-service
    url: http://file-service:8007
    tags:
      - file
```

加载配置：

```bash
# 使用 deck 工具同步配置
deck sync -s kong.yml
```

### 4.2 配置负载均衡（多实例场景）

```bash
# 1. 创建 Upstream
curl -X POST http://localhost:8001/upstreams \
  --data name=auth-service-upstream \
  --data algorithm=round-robin

# 2. 添加 Target（服务实例）
curl -X POST http://localhost:8001/upstreams/auth-service-upstream/targets \
  --data target=auth-service-1:8001 \
  --data weight=100

curl -X POST http://localhost:8001/upstreams/auth-service-upstream/targets \
  --data target=auth-service-2:8001 \
  --data weight=100

# 3. 更新 Service 指向 Upstream
curl -X PATCH http://localhost:8001/services/auth-service \
  --data host=auth-service-upstream
```

---

## 路由配置

### 5.1 基础路由配置

#### 认证服务路由

```bash
curl -X POST http://localhost:8001/services/auth-service/routes \
  --data name=auth-route \
  --data 'paths[]=/api/auth' \
  --data strip_path=false \
  --data 'methods[]=GET' \
  --data 'methods[]=POST' \
  --data 'methods[]=PUT' \
  --data 'methods[]=DELETE'
```

**参数说明**：
- `paths[]=/api/auth`：匹配 `/api/auth/*` 的请求
- `strip_path=false`：不去除路径前缀，保持原始路径
- `methods[]`：允许的 HTTP 方法

#### 用户服务路由

```bash
curl -X POST http://localhost:8001/services/user-service/routes \
  --data name=user-route \
  --data 'paths[]=/api/users' \
  --data strip_path=false
```

#### 内容服务路由

```bash
curl -X POST http://localhost:8001/services/post-service/routes \
  --data name=post-route \
  --data 'paths[]=/api/posts' \
  --data strip_path=false
```

### 5.2 声明式路由配置

**kong.yml**:

```yaml
_format_version: "3.0"

services:
  - name: auth-service
    url: http://auth-service:8001
    routes:
      - name: auth-route
        paths:
          - /api/auth
        strip_path: false
        methods:
          - GET
          - POST
          - PUT
          - DELETE
        
  - name: user-service
    url: http://user-service:8002
    routes:
      - name: user-route
        paths:
          - /api/users
        strip_path: false
        
  - name: post-service
    url: http://post-service:8003
    routes:
      - name: post-route
        paths:
          - /api/posts
        strip_path: false
        
  - name: interaction-service
    url: http://interaction-service:8004
    routes:
      - name: interaction-route
        paths:
          - /api/posts/\d+/replies
          - /api/posts/\d+/like
          - /api/posts/\d+/favorite
        strip_path: false
        regex_priority: 1
```

### 5.3 路由优先级

Kong 按以下顺序匹配路由：
1. **正则表达式路由**（`regex_priority` 高的优先）
2. **精确路径匹配**
3. **前缀路径匹配**

**示例**：

```yaml
routes:
  # 优先级 1：精确匹配登录接口
  - name: login-route
    paths:
      - /api/auth/login
    strip_path: false
    regex_priority: 10
    
  # 优先级 2：其他认证接口
  - name: auth-route
    paths:
      - /api/auth
    strip_path: false
    regex_priority: 5
```

---

## JWT 认证插件

### 6.1 JWT 认证流程

```
1. 用户登录 → S1 认证服务
2. S1 生成 JWT Token → 返回给客户端
3. 客户端携带 Token 请求 → Kong 网关
4. Kong JWT 插件验证 Token → 通过则转发到后端服务
5. 后端服务从 Header 获取用户信息 → 处理业务逻辑
```

### 6.2 配置 JWT 插件

#### 全局启用 JWT（推荐）

```bash
# 为所有路由启用 JWT 验证（除登录接口外）
curl -X POST http://localhost:8001/plugins \
  --data name=jwt \
  --data config.key_claim_name=kid \
  --data config.secret_is_base64=false \
  --data config.claims_to_verify=exp
```

#### 为特定服务启用 JWT

```bash
# 仅为用户服务启用 JWT
curl -X POST http://localhost:8001/services/user-service/plugins \
  --data name=jwt
```

#### 排除登录接口

```bash
# 为登录路由禁用 JWT
curl -X POST http://localhost:8001/routes/login-route/plugins \
  --data name=request-termination \
  --data config.status_code=200 \
  --data config.message='Login endpoint, no JWT required'
```

**更优雅的方式**：使用路由分组

```yaml
# kong.yml
services:
  - name: auth-service
    url: http://auth-service:8001
    routes:
      # 公开路由（无需认证）
      - name: auth-public-route
        paths:
          - /api/auth/login
          - /api/auth/register
        strip_path: false
        tags:
          - public
        
      # 受保护路由（需要认证）
      - name: auth-protected-route
        paths:
          - /api/auth/logout
          - /api/auth/refresh
          - /api/auth/password
        strip_path: false
        tags:
          - protected
        plugins:
          - name: jwt

plugins:
  # 为所有 protected 标签的路由启用 JWT
  - name: jwt
    route: auth-protected-route
    config:
      key_claim_name: kid
      claims_to_verify:
        - exp
```

### 6.3 JWT Consumer 配置

#### 创建 Consumer

```bash
# 创建消费者（代表一个用户）
curl -X POST http://localhost:8001/consumers \
  --data username=user_12345 \
  --data custom_id=12345
```

#### 为 Consumer 配置 JWT 凭证

```bash
curl -X POST http://localhost:8001/consumers/user_12345/jwt \
  --data key=user_12345_key \
  --data secret=your-secret-key-here \
  --data algorithm=HS256
```

**注意**：在实际应用中，JWT 凭证由 S1 认证服务动态生成，Kong 仅验证签名。

### 6.4 S1 认证服务生成 JWT

**Node.js 示例**：

```javascript
const jwt = require('jsonwebtoken');

// JWT 密钥（与 Kong 配置一致）
const JWT_SECRET = process.env.JWT_SECRET || 'your-secret-key-here';

// 生成 Token
function generateToken(user) {
  const payload = {
    sub: user.id,              // 用户 ID
    username: user.username,   // 用户名
    role: user.role,           // 角色
    iat: Math.floor(Date.now() / 1000),  // 签发时间
    exp: Math.floor(Date.now() / 1000) + (2 * 60 * 60)  // 过期时间（2小时）
  };
  
  return jwt.sign(payload, JWT_SECRET, {
    algorithm: 'HS256',
    header: {
      kid: `user_${user.id}_key`  // Key ID（对应 Kong Consumer）
    }
  });
}

// 登录接口
app.post('/api/auth/login', async (req, res) => {
  const { username, password } = req.body;
  
  // 验证用户名密码
  const user = await authenticateUser(username, password);
  
  if (!user) {
    return res.status(401).json({ error: 'Invalid credentials' });
  }
  
  // 生成 Token
  const token = generateToken(user);
  
  res.json({
    token,
    user: {
      id: user.id,
      username: user.username,
      role: user.role
    }
  });
});
```

### 6.5 客户端使用 Token

```javascript
// 前端请求示例
fetch('http://localhost:8000/api/users/123', {
  headers: {
    'Authorization': 'Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...'
  }
})
.then(res => res.json())
.then(data => console.log(data));
```

### 6.6 后端服务获取用户信息

Kong 验证 JWT 后，会将用户信息注入到请求头中：

```javascript
// Node.js 后端服务
app.get('/api/users/:id', (req, res) => {
  // Kong 注入的用户信息
  const userId = req.headers['x-consumer-custom-id'];  // 用户 ID
  const username = req.headers['x-consumer-username']; // 用户名
  
  // 或者从 JWT Payload 中获取
  const jwtPayload = JSON.parse(req.headers['x-credential-identifier'] || '{}');
  const role = jwtPayload.role;
  
  // 业务逻辑
  if (userId !== req.params.id && role !== 'admin') {
    return res.status(403).json({ error: 'Forbidden' });
  }
  
  // 返回用户信息
  res.json({ id: userId, username });
});
```

**Kong 注入的 Header**：

| Header | 说明 | 示例值 |
|--------|------|--------|
| `X-Consumer-ID` | Kong Consumer ID | `4e13f54a-bbf1-47a8-8777-255fed7116f2` |
| `X-Consumer-Custom-ID` | 自定义用户 ID | `12345` |
| `X-Consumer-Username` | 用户名 | `user_12345` |
| `X-Credential-Identifier` | JWT Payload（Base64） | `eyJzdWIiOiIxMjM0NSIsInJvbGUiOiJ1c2VyIn0=` |

---

## 限流插件

### 7.1 全局限流

```bash
# 每个 IP 每分钟最多 100 次请求
curl -X POST http://localhost:8001/plugins \
  --data name=rate-limiting \
  --data config.minute=100 \
  --data config.policy=local
```

### 7.2 按服务限流

```bash
# 认证服务：每分钟 20 次（防暴力破解）
curl -X POST http://localhost:8001/services/auth-service/plugins \
  --data name=rate-limiting \
  --data config.minute=20 \
  --data config.hour=100 \
  --data config.policy=local
```

### 7.3 按用户限流

```bash
# 每个用户每分钟 60 次请求
curl -X POST http://localhost:8001/plugins \
  --data name=rate-limiting \
  --data config.minute=60 \
  --data config.limit_by=consumer \
  --data config.policy=redis \
  --data config.redis_host=redis \
  --data config.redis_port=6379
```

### 7.4 声明式配置

```yaml
plugins:
  # 全局限流
  - name: rate-limiting
    config:
      minute: 100
      hour: 1000
      policy: redis
      redis_host: redis
      redis_port: 6379
      fault_tolerant: true
      
  # 认证服务限流
  - name: rate-limiting
    service: auth-service
    config:
      minute: 20
      hour: 100
      policy: local
```

---

## 其他常用插件

### 8.1 CORS 插件

```bash
curl -X POST http://localhost:8001/plugins \
  --data name=cors \
  --data config.origins=http://localhost:3000 \
  --data config.origins=https://open436.com \
  --data config.methods=GET,POST,PUT,DELETE,OPTIONS \
  --data config.headers=Authorization,Content-Type \
  --data config.exposed_headers=X-Auth-Token \
  --data config.credentials=true \
  --data config.max_age=3600
```

### 8.2 请求日志插件

```bash
curl -X POST http://localhost:8001/plugins \
  --data name=file-log \
  --data config.path=/var/log/kong/access.log
```

### 8.3 响应转换插件

```bash
# 添加自定义响应头
curl -X POST http://localhost:8001/plugins \
  --data name=response-transformer \
  --data config.add.headers=X-Server:Open436 \
  --data config.add.headers=X-Version:1.0
```

### 8.4 IP 限制插件

```bash
# 仅允许特定 IP 访问管理接口
curl -X POST http://localhost:8001/routes/admin-route/plugins \
  --data name=ip-restriction \
  --data config.allow=192.168.1.0/24 \
  --data config.allow=10.0.0.0/8
```

### 8.5 请求大小限制

```bash
# 限制请求体大小为 10MB
curl -X POST http://localhost:8001/plugins \
  --data name=request-size-limiting \
  --data config.allowed_payload_size=10
```

---

## 配置管理最佳实践

### 9.1 使用声明式配置

**优势**：
- 版本控制（Git）
- 环境一致性
- 快速恢复
- 团队协作

**完整配置示例 (kong.yml)**：

```yaml
_format_version: "3.0"

# 服务定义
services:
  - name: auth-service
    url: http://auth-service:8001
    tags: [auth, core]
    routes:
      - name: auth-public
        paths: [/api/auth/login, /api/auth/register]
        strip_path: false
        tags: [public]
        
      - name: auth-protected
        paths: [/api/auth]
        strip_path: false
        tags: [protected]
        plugins:
          - name: jwt
          - name: rate-limiting
            config:
              minute: 20

  - name: user-service
    url: http://user-service:8002
    tags: [user, core]
    routes:
      - name: user-route
        paths: [/api/users]
        strip_path: false
        plugins:
          - name: jwt

# 全局插件
plugins:
  - name: cors
    config:
      origins: ["*"]
      methods: [GET, POST, PUT, DELETE, OPTIONS]
      headers: [Authorization, Content-Type]
      credentials: true
      
  - name: rate-limiting
    config:
      minute: 100
      policy: redis
      redis_host: redis
      redis_port: 6379
      
  - name: request-size-limiting
    config:
      allowed_payload_size: 10

# 消费者（可选，通常由认证服务动态创建）
consumers:
  - username: admin
    custom_id: "1"
    jwt_secrets:
      - key: admin_key
        secret: admin_secret_key
        algorithm: HS256
```

### 9.2 环境配置分离

```bash
# 开发环境
deck sync -s kong.dev.yml

# 测试环境
deck sync -s kong.test.yml

# 生产环境
deck sync -s kong.prod.yml
```

### 9.3 配置备份

```bash
# 导出当前配置
deck dump -o kong-backup-$(date +%Y%m%d).yml

# 恢复配置
deck sync -s kong-backup-20251023.yml
```

### 9.4 配置验证

```bash
# 验证配置文件
deck validate -s kong.yml

# 查看差异（不应用）
deck diff -s kong.yml

# 应用配置
deck sync -s kong.yml
```

---

## 监控与调试

### 10.1 查看 Kong 状态

```bash
# 健康检查
curl http://localhost:8001/status

# 查看所有服务
curl http://localhost:8001/services

# 查看所有路由
curl http://localhost:8001/routes

# 查看所有插件
curl http://localhost:8001/plugins
```

### 10.2 查看日志

```bash
# Kong 访问日志
docker logs kong -f

# 查看特定服务的请求
docker logs kong | grep "auth-service"
```

### 10.3 Prometheus 监控

```bash
# 启用 Prometheus 插件
curl -X POST http://localhost:8001/plugins \
  --data name=prometheus

# 访问指标
curl http://localhost:8001/metrics
```

---

## 常见问题

### Q1: JWT 验证失败

**错误**：`{"message":"Unauthorized"}`

**排查步骤**：
1. 检查 Token 格式：`Bearer <token>`
2. 验证 Token 是否过期
3. 确认密钥一致性
4. 查看 Kong 日志

### Q2: 路由不生效

**排查步骤**：
1. 检查路由优先级
2. 确认 `strip_path` 配置
3. 测试路由匹配：`curl -v http://localhost:8000/api/auth/login`

### Q3: 服务无法访问

**排查步骤**：
1. 检查服务健康状态：`curl http://localhost:8001/services/auth-service/health`
2. 确认网络连通性
3. 查看 Kong 错误日志

---

## 下一步阅读

- [服务间通信规范](./03-服务间通信规范.md) - 如何在后端服务中调用其他服务
- [API 设计规范](./04-API设计规范.md) - 统一的 API 标准

---

**文档维护**: 运维组  
**最后更新**: 2025-10-23

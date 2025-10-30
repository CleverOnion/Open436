# Open436 开发环境配置指南（Windows + IDE）

本指南适用于在 Windows 环境下，使用 IDE 运行后端服务，Docker 运行基础设施的开发模式。

## 📋 架构说明

```
开发模式架构：
- IDE 运行: M1 认证服务 (localhost:8081)、M7 文件服务 (localhost:8007)
- Docker 运行: Consul、Kong、PostgreSQL、Redis、Minio
```

## 🔧 前置准备

### 必需软件

- **Docker Desktop for Windows** (推荐 4.20+)
- **Java 21** (M1 服务)
  - 下载: https://adoptium.net/
  - 验证: `java -version`
- **Maven 3.8+** (M1 服务)
  - 下载: https://maven.apache.org/download.cgi
  - 验证: `mvn -version`
- **Rust 1.70+** (M7 服务)
  - 下载: https://rustup.rs/
  - 验证: `rustc --version`
- **IDE**
  - IntelliJ IDEA (推荐，用于 M1)
  - VS Code 或 CLion (用于 M7)

### 可选工具

- **Git Bash** - 执行 shell 脚本
- **Postman** - API 测试
- **DBeaver** - 数据库管理
- **Redis Desktop Manager** - Redis 可视化

## 📂 项目准备

### 1. 克隆项目

```bash
git clone <your-repo>
cd Open436
```

### 2. 构建 M1 认证服务（首次）

```bash
cd Open436-Auth
mvn clean install -DskipTests
cd ..
```

## 🐳 步骤 1: 启动 Docker 基础设施

### 1.1 创建开发环境 Docker Compose 文件

**文件**: `docker-compose-dev.yml`

```yaml
version: '3.8'

services:
  # Consul 服务注册中心
  consul:
    image: consul:1.17
    container_name: open436-consul
    ports:
      - "8500:8500"    # HTTP API + UI
      - "8600:8600/udp" # DNS
    command: agent -server -ui -bootstrap-expect=1 -client=0.0.0.0
    networks:
      - open436-dev-network
    volumes:
      - consul-dev-data:/consul/data

  # PostgreSQL 数据库
  postgres:
    image: postgres:14-alpine
    container_name: open436-postgres
    environment:
      POSTGRES_USER: open436
      POSTGRES_PASSWORD: open436
      POSTGRES_DB: open436
    ports:
      - "5432:5432"
    networks:
      - open436-dev-network
    volumes:
      - postgres-dev-data:/var/lib/postgresql/data
      - ./Open436-Auth/init-database.sql:/docker-entrypoint-initdb.d/init.sql

  # Redis 缓存
  redis:
    image: redis:7-alpine
    container_name: open436-redis
    ports:
      - "6379:6379"
    networks:
      - open436-dev-network

  # Minio 对象存储
  minio:
    image: minio/minio:latest
    container_name: open436-minio
    ports:
      - "9000:9000"   # API
      - "9001:9001"   # Console
    environment:
      MINIO_ROOT_USER: minioadmin
      MINIO_ROOT_PASSWORD: minioadmin
    command: server /data --console-address ":9001"
    networks:
      - open436-dev-network
    volumes:
      - minio-dev-data:/data

  # Kong 数据库
  kong-database:
    image: postgres:14-alpine
    container_name: kong-database
    environment:
      POSTGRES_USER: kong
      POSTGRES_PASSWORD: kong
      POSTGRES_DB: kong
    ports:
      - "5433:5432"  # 避免与主数据库端口冲突
    networks:
      - open436-dev-network
    volumes:
      - kong-dev-data:/var/lib/postgresql/data

  # Kong 迁移
  kong-migration:
    image: kong:3.4
    command: kong migrations bootstrap
    environment:
      KONG_DATABASE: postgres
      KONG_PG_HOST: kong-database
      KONG_PG_USER: kong
      KONG_PG_PASSWORD: kong
    depends_on:
      - kong-database
    networks:
      - open436-dev-network

  # Kong Gateway
  kong:
    image: kong:3.4
    container_name: open436-kong
    environment:
      KONG_DATABASE: postgres
      KONG_PG_HOST: kong-database
      KONG_PG_USER: kong
      KONG_PG_PASSWORD: kong
      KONG_PROXY_ACCESS_LOG: /dev/stdout
      KONG_ADMIN_ACCESS_LOG: /dev/stdout
      KONG_PROXY_ERROR_LOG: /dev/stderr
      KONG_ADMIN_ERROR_LOG: /dev/stderr
      KONG_ADMIN_LISTEN: 0.0.0.0:8001
      # 允许 Kong 访问宿主机服务（Windows Docker Desktop）
      KONG_DNS_RESOLVER: 8.8.8.8
    ports:
      - "8000:8000"  # Proxy
      - "8443:8443"  # Proxy SSL
      - "8001:8001"  # Admin API
    depends_on:
      - kong-migration
      - consul
    networks:
      - open436-dev-network
    extra_hosts:
      - "host.docker.internal:host-gateway"  # Windows Docker Desktop 特性
    volumes:
      - ./kong/plugins:/usr/local/share/lua/5.1/kong/plugins/custom

volumes:
  consul-dev-data:
  postgres-dev-data:
  kong-dev-data:
  minio-dev-data:

networks:
  open436-dev-network:
    driver: bridge
```

### 1.2 启动基础设施

在项目根目录打开 PowerShell 或 Git Bash：

```bash
# 启动所有基础设施
docker-compose -f docker-compose-dev.yml up -d

# 查看服务状态
docker-compose -f docker-compose-dev.yml ps

# 查看日志
docker-compose -f docker-compose-dev.yml logs -f
```

### 1.3 初始化 Minio

访问 Minio Console: http://localhost:9001

- 用户名: `minioadmin`
- 密码: `minioadmin`

创建存储桶：

1. 点击 "Buckets" → "Create Bucket"
2. 创建 3 个 bucket:
   - `open436-avatars`
   - `open436-posts`
   - `open436-icons`
3. 设置为 Public（可选，开发环境）

### 1.4 验证基础设施

```bash
# 验证 Consul
curl http://localhost:8500/v1/status/leader

# 验证 PostgreSQL
docker exec -it open436-postgres psql -U open436 -c "SELECT version();"

# 验证 Redis
docker exec -it open436-redis redis-cli ping

# 验证 Minio
curl http://localhost:9000/minio/health/live

# 验证 Kong
curl http://localhost:8001/status
```

## 🔧 步骤 2: 配置并运行 M1 认证服务

### 2.1 配置文件

编辑 `Open436-Auth/src/main/resources/application-dev.yml`：

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/open436
    username: open436
    password: open436
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.PostgreSQLDialect
  
  redis:
    host: localhost
    port: 6379
    database: 0

  cloud:
    consul:
      host: localhost
      port: 8500
      discovery:
        enabled: true
        service-name: auth-service
        instance-id: ${spring.application.name}:${random.value}
        health-check-path: /actuator/health
        health-check-interval: 10s
        prefer-ip-address: true
        # Windows 环境使用 localhost
        hostname: localhost
        # 重要：告诉 Consul 服务运行在宿主机
        ip-address: host.docker.internal

server:
  port: 8081

# Sa-Token 配置
sa-token:
  token-name: token
  timeout: 2592000
  active-timeout: -1
  is-concurrent: true
  is-share: true
  token-style: uuid
  is-log: true
  auto-renew: true

# Actuator 健康检查
management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: always

# 日志配置
logging:
  level:
    com.open436: DEBUG
    org.springframework.cloud.consul: DEBUG
```

### 2.2 在 IntelliJ IDEA 中运行

1. **打开项目**
   - File → Open → 选择 `Open436-Auth` 目录
   - 等待 Maven 导入依赖

2. **配置运行配置**
   - Run → Edit Configurations
   - 点击 "+" → Spring Boot
   - 设置：
     - Name: `Open436-Auth-Dev`
     - Main class: `com.open436.auth.Open436AuthApplication`
     - Active profiles: `dev`
     - Environment variables: `CONSUL_HOST=localhost;CONSUL_PORT=8500`

3. **运行服务**
   - 点击运行按钮或按 `Shift+F10`
   - 查看控制台输出，确认：
     ```
     Service registered to Consul: auth-service
     Started Open436AuthApplication in X.XXX seconds
     ```

4. **验证服务**
   ```bash
   # 健康检查
   curl http://localhost:8081/actuator/health
   
   # 查看 Consul UI
   # 访问 http://localhost:8500
   # 应该看到 auth-service (绿色)
   ```

### 2.3 测试登录

```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"alice\",\"password\":\"password123\"}"
```

## 🦀 步骤 3: 配置并运行 M7 文件服务

### 3.1 配置环境变量

创建 `Open436-FileService/.env` 文件：

```env
# 数据库配置
DATABASE_URL=postgresql://open436:open436@localhost:5432/open436

# Consul 配置
CONSUL_URL=http://localhost:8500
SERVICE_PORT=8007

# S3/Minio 配置
S3_ENDPOINT=http://localhost:9000
S3_ACCESS_KEY=minioadmin
S3_SECRET_KEY=minioadmin
S3_BUCKET_AVATARS=open436-avatars
S3_BUCKET_POSTS=open436-posts
S3_BUCKET_ICONS=open436-icons
S3_REGION=us-east-1

# 服务器配置
SERVER_HOST=127.0.0.1
SERVER_PORT=8007

# 清理任务配置
CLEANUP_ENABLED=true
CLEANUP_RETENTION_DAYS=30
CLEANUP_CRON=0 0 2 * * *

# 日志级别
RUST_LOG=info,file_service=debug
```

### 3.2 在 VS Code 中运行

**创建 `.vscode/launch.json`**:

```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "lldb",
      "request": "launch",
      "name": "Debug File Service",
      "cargo": {
        "args": [
          "build",
          "--bin=file-service",
          "--package=file-service"
        ],
        "filter": {
          "name": "file-service",
          "kind": "bin"
        }
      },
      "args": [],
      "cwd": "${workspaceFolder}/Open436-FileService",
      "env": {
        "RUST_LOG": "debug"
      }
    }
  ]
}
```

### 3.3 运行服务

**方法 1: 使用 VS Code**
1. 打开 `Open436-FileService` 文件夹
2. 按 F5 启动调试

**方法 2: 使用终端**

```bash
cd Open436-FileService

# 运行服务（开发模式）
cargo run

# 或者以 release 模式运行（更快）
cargo run --release
```

### 3.4 验证服务

```bash
# 健康检查
curl http://localhost:8007/health

# 查看 Consul UI
# 访问 http://localhost:8500
# 应该看到 file-service (绿色)
```

## 🌉 步骤 4: 配置 Kong 路由到本地服务

### 4.1 创建开发环境 Kong 配置脚本

**文件**: `kong/kong-config-dev.sh`

```bash
#!/bin/bash
# Kong 开发环境配置脚本（路由到宿主机服务）

KONG_ADMIN="http://localhost:8001"

# Windows Docker Desktop 使用 host.docker.internal 访问宿主机
HOST_ADDR="host.docker.internal"

echo "Configuring Kong Gateway for Development..."

# 1. 创建 M1 认证服务（指向宿主机）
echo "Creating auth-service..."
curl -i -X POST $KONG_ADMIN/services \
  --data name=auth-service \
  --data url=http://${HOST_ADDR}:8081

curl -i -X POST $KONG_ADMIN/services/auth-service/routes \
  --data paths[]=/api/auth \
  --data strip_path=false

# 2. 创建 M7 文件服务（指向宿主机）
echo "Creating file-service..."
curl -i -X POST $KONG_ADMIN/services \
  --data name=file-service \
  --data url=http://${HOST_ADDR}:8007

curl -i -X POST $KONG_ADMIN/services/file-service/routes \
  --data paths[]=/api/files \
  --data strip_path=false

# 3. 启用 Sa-Token 认证插件（文件服务需要鉴权）
echo "Enabling satoken-auth plugin..."
curl -i -X POST $KONG_ADMIN/services/file-service/plugins \
  --data name=satoken-auth \
  --data config.auth_service_url=http://${HOST_ADDR}:8081

echo ""
echo "Kong configuration complete!"
echo ""
echo "Services registered:"
echo "- auth-service: http://${HOST_ADDR}:8081"
echo "- file-service: http://${HOST_ADDR}:8007"
```

### 4.2 执行配置

在 Git Bash 中执行：

```bash
chmod +x kong/kong-config-dev.sh
bash kong/kong-config-dev.sh
```

或在 PowerShell 中：

```powershell
# 手动执行命令
$KONG_ADMIN = "http://localhost:8001"
$HOST_ADDR = "host.docker.internal"

# 创建认证服务
Invoke-RestMethod -Uri "$KONG_ADMIN/services" -Method Post -Body @{
    name = "auth-service"
    url = "http://${HOST_ADDR}:8081"
}

Invoke-RestMethod -Uri "$KONG_ADMIN/services/auth-service/routes" -Method Post -Body @{
    "paths[]" = "/api/auth"
    strip_path = $false
}

# 创建文件服务
Invoke-RestMethod -Uri "$KONG_ADMIN/services" -Method Post -Body @{
    name = "file-service"
    url = "http://${HOST_ADDR}:8007"
}

Invoke-RestMethod -Uri "$KONG_ADMIN/services/file-service/routes" -Method Post -Body @{
    "paths[]" = "/api/files"
    strip_path = $false
}
```

### 4.3 验证 Kong 配置

```bash
# 查看服务列表
curl http://localhost:8001/services

# 查看路由列表
curl http://localhost:8001/routes

# 测试通过 Kong 访问认证服务
curl -X POST http://localhost:8000/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"alice\",\"password\":\"password123\"}"
```

## 🧪 步骤 5: 完整测试

### 5.1 测试流程

```bash
# 1. 通过 Kong 登录
curl -X POST http://localhost:8000/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"alice\",\"password\":\"password123\"}"

# 保存返回的 token

# 2. 测试 Token 验证
curl -X POST http://localhost:8000/api/auth/verify \
  -H "Content-Type: application/json" \
  -d "{\"token\":\"YOUR_TOKEN_HERE\"}"

# 3. 通过 Kong 上传文件（需要鉴权）
curl -X POST http://localhost:8000/api/files/upload \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -F "file=@test-image.jpg" \
  -F "file_type=POST_IMAGE"
```

### 5.2 查看服务状态

访问以下 URL：

- **Consul UI**: http://localhost:8500
  - 查看 auth-service 和 file-service 状态
  - 查看健康检查详情

- **Kong Admin**: http://localhost:8001
  - 查看服务配置: http://localhost:8001/services
  - 查看路由配置: http://localhost:8001/routes

- **Minio Console**: http://localhost:9001
  - 查看上传的文件
  - 管理存储桶

## 🐛 调试技巧

### M1 Java 服务调试

在 IntelliJ IDEA 中：
1. 在代码行号左侧点击设置断点
2. 点击调试按钮（虫子图标）
3. 发送请求触发断点

### M7 Rust 服务调试

在 VS Code 中：
1. 安装 `CodeLLDB` 扩展
2. 在代码左侧设置断点
3. 按 F5 启动调试
4. 发送请求触发断点

### 查看日志

**M1 日志**:
- IntelliJ IDEA 控制台输出
- 日志级别在 `application-dev.yml` 中配置

**M7 日志**:
- VS Code 终端输出
- 通过 `RUST_LOG=debug` 环境变量控制

**Docker 服务日志**:
```bash
# 查看所有日志
docker-compose -f docker-compose-dev.yml logs -f

# 查看特定服务
docker-compose -f docker-compose-dev.yml logs -f consul
docker-compose -f docker-compose-dev.yml logs -f kong
docker-compose -f docker-compose-dev.yml logs -f postgres
```

## 🔄 常用开发操作

### 重启服务

**M1**: 在 IDE 中点击停止按钮，然后再次运行

**M7**: 
```bash
# Ctrl+C 停止
# 然后重新运行
cargo run
```

### 重置数据库

```bash
# 停止服务
docker-compose -f docker-compose-dev.yml down

# 删除数据卷
docker volume rm open436_postgres-dev-data

# 重新启动
docker-compose -f docker-compose-dev.yml up -d postgres

# 等待数据库就绪
sleep 5

# 重新运行服务
```

### 清理并重启所有基础设施

```bash
# 停止并删除所有容器和数据
docker-compose -f docker-compose-dev.yml down -v

# 重新启动
docker-compose -f docker-compose-dev.yml up -d

# 等待服务就绪
sleep 10

# 重新配置 Kong
bash kong/kong-config-dev.sh
```

## 📊 开发环境端口一览

| 服务 | 端口 | 访问地址 | 说明 |
|------|------|---------|------|
| M1 认证服务 | 8081 | http://localhost:8081 | IDE 运行 |
| M7 文件服务 | 8007 | http://localhost:8007 | IDE 运行 |
| Consul UI | 8500 | http://localhost:8500 | Docker |
| Kong Proxy | 8000 | http://localhost:8000 | Docker |
| Kong Admin | 8001 | http://localhost:8001 | Docker |
| PostgreSQL | 5432 | localhost:5432 | Docker |
| Kong Database | 5433 | localhost:5433 | Docker |
| Redis | 6379 | localhost:6379 | Docker |
| Minio API | 9000 | http://localhost:9000 | Docker |
| Minio Console | 9001 | http://localhost:9001 | Docker |

## ⚠️ 常见问题

### 问题 1: Consul 无法注册服务

**症状**: 服务启动，但 Consul UI 看不到

**解决方案**:
```yaml
# 检查 application-dev.yml 中的配置
spring:
  cloud:
    consul:
      host: localhost  # 确保是 localhost
      port: 8500
      discovery:
        ip-address: host.docker.internal  # 重要：告诉 Consul 服务在宿主机
```

### 问题 2: Kong 无法访问本地服务

**症状**: 通过 Kong 访问返回 502 Bad Gateway

**解决方案**:
```bash
# 检查 Kong 服务配置
curl http://localhost:8001/services

# 确认 URL 使用 host.docker.internal
# 正确: http://host.docker.internal:8081
# 错误: http://localhost:8081
```

### 问题 3: M7 无法连接 Minio

**症状**: 文件上传失败

**解决方案**:
```env
# 检查 .env 配置
S3_ENDPOINT=http://localhost:9000  # 使用 localhost，不是 127.0.0.1

# 确认 Minio 正在运行
curl http://localhost:9000/minio/health/live
```

### 问题 4: Windows 防火墙阻止连接

**症状**: 服务间无法通信

**解决方案**:
1. 打开 Windows 防火墙设置
2. 允许 Java、Rust 应用通过防火墙
3. 或者临时关闭防火墙测试

## 🎓 开发流程建议

### 每日开发流程

1. **启动基础设施** (只需一次)
   ```bash
   docker-compose -f docker-compose-dev.yml up -d
   ```

2. **启动 M1 服务**
   - 在 IntelliJ IDEA 中运行

3. **启动 M7 服务**
   - 在 VS Code 或终端中运行

4. **开发和测试**
   - 修改代码
   - IDE 自动重新编译（M1）或手动重启（M7）
   - 测试 API

5. **结束开发**
   - 停止 IDE 中的服务
   - 可选：停止 Docker
     ```bash
     docker-compose -f docker-compose-dev.yml stop
     ```

### 代码热重载

**M1 (Spring Boot DevTools)**:
- 已包含在依赖中
- 修改代码后自动重新加载（部分情况）

**M7 (Rust)**:
- 使用 `cargo watch`:
  ```bash
  cargo install cargo-watch
  cargo watch -x run
  ```

## 📝 快速参考

### 启动开发环境

```bash
# 1. 启动 Docker 基础设施
docker-compose -f docker-compose-dev.yml up -d

# 2. 配置 Kong（首次或重置后）
bash kong/kong-config-dev.sh

# 3. 在 IDE 中启动 M1 和 M7
```

### 停止开发环境

```bash
# 停止 IDE 中的服务

# 停止 Docker（可选）
docker-compose -f docker-compose-dev.yml stop
```

### 重置环境

```bash
# 完全清理
docker-compose -f docker-compose-dev.yml down -v

# 重新开始
docker-compose -f docker-compose-dev.yml up -d
bash kong/kong-config-dev.sh
```

---

**祝开发顺利！** 🚀

如有问题，请查看日志或访问 Consul UI 检查服务状态。




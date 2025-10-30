# 🚀 Open436 开发环境快速启动（Windows）

这是精简版的快速启动指南，帮助您在 5 分钟内启动开发环境。

## ✅ 前置检查

确保已安装：
- [ ] Docker Desktop
- [ ] Java 21
- [ ] Rust 1.70+
- [ ] Maven 3.8+
- [ ] IntelliJ IDEA 或 VS Code

## 📝 快速启动步骤

### 1. 启动 Docker 基础设施（2 分钟）

打开 PowerShell（管理员模式），执行：

```powershell
# 进入项目目录
cd Open436

# 启动基础设施
.\deploy\dev\start.ps1
```

或使用 Git Bash：

```bash
chmod +x deploy/dev/start.sh
./deploy/dev/start.sh
```

✅ **验证**: 访问 http://localhost:8500 应该看到 Consul UI

### 2. 配置 Minio（1 分钟）

1. 访问 http://localhost:9001
2. 登录：`minioadmin` / `minioadmin`
3. 创建 3 个 bucket:
   - `open436-avatars`
   - `open436-posts`
   - `open436-icons`

### 3. 启动 M1 认证服务（1 分钟）

在 IntelliJ IDEA 中：

1. **Open Project** → 选择 `Open436-Auth` 目录
2. 等待 Maven 导入完成
3. **Run → Edit Configurations** → 添加 Spring Boot 配置
   - Main class: `com.open436.auth.Open436AuthApplication`
   - Active profiles: `dev`
4. **点击运行** ▶️

✅ **验证**: 
```bash
curl http://localhost:8081/actuator/health
```

### 4. 启动 M7 文件服务（1 分钟）

**创建配置文件**:
```bash
cd Open436-FileService
cp env.template .env
```

**启动服务**:
```bash
cargo run
```

或在 VS Code 中按 F5

✅ **验证**:
```bash
curl http://localhost:8007/health
```

### 5. 测试完整流程（1 分钟）

```bash
# 通过 Kong 登录
curl -X POST http://localhost:8000/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"alice\",\"password\":\"password123\"}"
```

✅ **成功**: 返回包含 token 的 JSON

## 🎯 开发流程

### 日常开发

1. **启动基础设施** (只需一次)
   ```powershell
   .\deploy\dev\start.ps1
   ```

2. **在 IDE 中启动服务**
   - IntelliJ IDEA: 运行 M1
   - VS Code/Terminal: 运行 M7

3. **开发和调试**
   - 修改代码
   - 测试 API
   - 查看日志

### 停止开发

```bash
# 停止 IDE 中的服务

# 可选：停止 Docker
docker-compose -f deploy/dev/docker-compose.yml stop
```

## 🔗 常用链接

| 服务 | 地址 | 说明 |
|------|------|------|
| **Consul UI** | http://localhost:8500 | 服务注册状态 |
| **Minio Console** | http://localhost:9001 | 文件管理 |
| **Kong Admin** | http://localhost:8001 | 网关配置 |
| **M1 认证服务** | http://localhost:8081 | 直接访问 |
| **M7 文件服务** | http://localhost:8007 | 直接访问 |
| **Kong Proxy** | http://localhost:8000 | 统一入口 |

## 🐛 常见问题

### 端口被占用

```bash
# 检查端口占用
netstat -ano | findstr :8081
netstat -ano | findstr :8007

# 关闭占用端口的进程
taskkill /PID <PID> /F
```

### 服务未注册到 Consul

检查 `application-dev.yml` 中的配置：
```yaml
spring:
  cloud:
    consul:
      host: localhost
      discovery:
        ip-address: host.docker.internal  # 重要
```

### Kong 无法访问本地服务

确认 Kong 配置使用了 `host.docker.internal`：
```bash
curl http://localhost:8001/services | grep host.docker.internal
```

## 📚 详细文档

- [完整开发指南](./DEVELOPMENT-GUIDE.md)
- [部署指南](./DEPLOYMENT-GUIDE.md)
- [Consul + Kong 架构](./README-CONSUL-KONG.md)

---

**Happy Coding!** 🎉




# Open436-Auth 快速启动指南

## 🎉 核心开发已完成

**完成时间**: 2025-10-27  
**完成进度**: ████████████████████ 100% (28/28 任务)

---

## 快速开始

### 1. 前置条件

确保以下服务已启动：

- ✅ PostgreSQL（端口 5432）
- ✅ Redis（端口 16379）

### 2. 初始化数据库

```bash
# 连接 PostgreSQL
psql -U postgres

# 创建数据库（如果还没创建）
CREATE DATABASE open436;

# 连接到数据库
\c open436

# 执行初始化脚本
\i init-database.sql
```

**或者直接使用命令行**:

```bash
psql -U postgres -d open436 -f init-database.sql
```

### 3. 启动应用

```bash
# 使用 Maven 启动
mvn spring-boot:run

# 或者使用 IDE 运行 Open436AuthApplication
```

**启动成功标志**:

```
Started Open436AuthApplication in X.XXX seconds
```

### 4. 测试 API

#### 4.1 用户登录

```bash
curl -X POST http://localhost:8001/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

**响应示例**:

```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "satoken:satoken:xyz123...",
    "expiresIn": 2592000,
    "user": {
      "id": 1,
      "username": "admin",
      "role": "admin",
      "status": "active"
    }
  },
  "timestamp": 1698000000000
}
```

#### 4.2 获取当前用户

```bash
curl -X GET http://localhost:8001/api/auth/current \
  -H "Authorization: Bearer {你的Token}"
```

#### 4.3 获取我的权限

```bash
curl -X GET http://localhost:8001/api/auth/permissions/my \
  -H "Authorization: Bearer {你的Token}"
```

#### 4.4 创建用户（管理员）

```bash
curl -X POST http://localhost:8001/api/auth/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {管理员Token}" \
  -d '{
    "username": "alice",
    "password": "password123",
    "role": "user"
  }'
```

---

## 项目结构

```
Open436-Auth/
├── src/main/java/com/open436/auth/
│   ├── config/                      # 配置类
│   │   ├── CacheConfig.java         ✅ Redis 缓存配置
│   │   ├── JpaConfig.java           ✅ JPA 审计配置
│   │   ├── SaTokenConfig.java       ✅ Sa-Token 配置
│   │   └── SecurityConfig.java      ✅ 密码加密配置
│   ├── controller/                  # 控制器
│   │   ├── AuthController.java      ✅ 认证控制器
│   │   ├── UserController.java      ✅ 用户管理控制器
│   │   └── PermissionController.java ✅ 权限控制器
│   ├── dto/                         # 数据传输对象
│   │   ├── ApiResponse.java         ✅ 统一响应格式
│   │   ├── LoginRequest.java        ✅ 登录请求
│   │   ├── LoginResponse.java       ✅ 登录响应
│   │   ├── UserInfoResponse.java    ✅ 用户信息响应
│   │   ├── CreateUserRequest.java   ✅ 创建用户请求
│   │   ├── UpdatePasswordRequest.java ✅ 修改密码请求
│   │   ├── UpdateUserStatusRequest.java ✅ 更新状态请求
│   │   └── ResetPasswordRequest.java ✅ 重置密码请求
│   ├── entity/                      # 实体类
│   │   ├── UserAuth.java            ✅ 用户认证实体
│   │   ├── Role.java                ✅ 角色实体
│   │   └── Permission.java          ✅ 权限实体
│   ├── exception/                   # 异常处理
│   │   ├── BusinessException.java   ✅ 业务异常
│   │   └── GlobalExceptionHandler.java ✅ 全局异常处理器
│   ├── repository/                  # 数据访问层
│   │   ├── UserAuthRepository.java  ✅ 用户 Repository
│   │   ├── RoleRepository.java      ✅ 角色 Repository
│   │   └── PermissionRepository.java ✅ 权限 Repository
│   └── service/                     # 业务逻辑层
│       ├── AuthService.java         ✅ 认证服务接口
│       ├── UserService.java         ✅ 用户服务接口
│       ├── PermissionService.java   ✅ 权限服务接口
│       └── impl/
│           ├── AuthServiceImpl.java ✅ 认证服务实现
│           ├── UserServiceImpl.java ✅ 用户服务实现
│           └── PermissionServiceImpl.java ✅ 权限服务实现
├── src/main/resources/
│   ├── application.yml              ✅ 主配置
│   ├── application-dev.yml          ✅ 开发环境
│   ├── application-test.yml         ✅ 测试环境
│   ├── application-prod.yml         ✅ 生产环境
│   └── db/migration/
│       ├── V1__initial_schema.sql   ✅ 建表脚本
│       └── V2__initial_data.sql     ✅ 初始化数据
├── init-database.sql                ✅ 快速初始化脚本
├── README.md                        ✅ 项目说明
└── pom.xml                          ✅ Maven 配置
```

---

## 默认账号

**管理员账号**:

- 用户名: `admin`
- 密码: `admin123`
- 角色: admin
- 权限: 所有权限（8 个）

⚠️ **重要**: 生产环境请立即修改默认密码！

---

## 已实现的权限

M1 认证授权模块权限（8 个）:

| 权限代码      | 权限名称 | 资源   | 操作   | 普通用户 | 管理员 |
| ------------- | -------- | ------ | ------ | -------- | ------ |
| user:read     | 查看用户 | user   | read   | ✅       | ✅     |
| user:create   | 创建用户 | user   | create | ❌       | ✅     |
| user:update   | 编辑用户 | user   | update | ❌       | ✅     |
| user:delete   | 删除用户 | user   | delete | ❌       | ✅     |
| user:manage   | 管理用户 | user   | manage | ❌       | ✅     |
| role:read     | 查看角色 | role   | read   | ❌       | ✅     |
| role:manage   | 管理角色 | role   | manage | ❌       | ✅     |
| system:manage | 系统配置 | system | manage | ❌       | ✅     |

**说明**: 其他模块（帖子、回复、互动、板块）的权限待相关模块开发时添加。

---

## 环境切换

### 开发环境（默认）

```bash
mvn spring-boot:run
```

### 测试环境

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=test
```

### 生产环境

```bash
# 设置环境变量
export SPRING_PROFILES_ACTIVE=prod
export DB_URL=jdbc:postgresql://db.example.com:5432/auth_db
export DB_USERNAME=auth_user
export DB_PASSWORD=secure_password
export REDIS_HOST=redis.example.com
export REDIS_PORT=6379
export REDIS_PASSWORD=redis_password

# 启动应用
java -jar target/Open436-Auth-0.0.1-SNAPSHOT.jar
```

---

## 核心技术栈

- **Java**: 21
- **Spring Boot**: 3.5.7
- **Sa-Token**: 1.37.0（自动续签）
- **PostgreSQL**: 14+
- **Redis**: 7+
- **JPA**: Spring Data JPA
- **密码加密**: BCrypt (cost=10)

---

## 技术特性

- ✅ **Sa-Token 自动续签**: 30 天有效期，自动延长
- ✅ **无需手动刷新**: 用户持续使用会自动续期
- ✅ **RBAC 权限控制**: 基于角色的访问控制
- ✅ **Redis 缓存**: Session 存储 + 权限缓存
- ✅ **多环境配置**: dev、test、prod
- ✅ **统一异常处理**: 全局异常拦截
- ✅ **参数自动验证**: @Valid + @NotBlank
- ✅ **日志记录**: 完整的操作日志

---

## 常见问题

### Q1: 启动失败，无法连接数据库

**A**: 检查 `application-dev.yml` 中的数据库配置是否正确。

### Q2: 启动失败，无法连接 Redis

**A**: 检查 Redis 是否启动，端口是否为 16379（或修改配置）。

### Q3: 登录失败

**A**: 确保已执行 `init-database.sql` 初始化数据。

### Q4: Token 自动续签如何工作？

**A**:

- 用户登录时 Token 有效期 30 天
- 用户每次访问 API 时，Sa-Token 自动检查 Token 剩余有效期
- 如果剩余有效期不足 15 天，自动延长至 30 天
- 用户无感知，持续使用会自动续期

---

## 相关文档

- [开发任务清单](../docs/TDD/M1-认证授权服务/05-开发任务清单.md)
- [API 接口设计](../docs/TDD/M1-认证授权服务/02-API接口设计.md)
- [数据库设计](../docs/TDD/M1-认证授权服务/01-数据库设计.md)
- [Sa-Token 自动续签方案](../docs/TDD/M1-认证授权服务/03-JWT实现方案.md)
- [RBAC 权限模型](../docs/TDD/M1-认证授权服务/04-RBAC权限模型.md)

---

**祝开发顺利！** 🚀


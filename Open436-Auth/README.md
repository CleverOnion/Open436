# Open436-Auth 认证授权服务

## 项目简介

Open436 认证授权服务，基于 Sa-Token 实现自动续签的用户认证和 RBAC 权限管理。

**技术栈**: Java 21 + Spring Boot 3.5.7 + Sa-Token 1.37.0 + PostgreSQL + Redis

**认证方案**: Sa-Token 自动续签（单Token + 自动续期）

## 快速开始

### 1. 环境要求

- JDK 21
- Maven 3.8+
- PostgreSQL 14+
- Redis 7+

### 2. 数据库初始化

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

### 3. 配置环境

默认使用开发环境配置，可以通过以下方式切换：

```bash
# 修改 application.yml
spring.profiles.active: dev  # dev, test, prod

# 或使用启动参数
mvn spring-boot:run -Dspring-boot.run.profiles=test

# 或使用环境变量
export SPRING_PROFILES_ACTIVE=prod
```

### 4. 启动服务

```bash
# 使用 Maven 启动
mvn spring-boot:run

# 或打包后运行
mvn clean package
java -jar target/Open436-Auth-0.0.1-SNAPSHOT.jar
```

### 5. 验证服务

服务启动后访问：http://localhost:8001

## 配置文件

```
src/main/resources/
├── application.yml          # 主配置（Sa-Token、Kong、CORS等）
├── application-dev.yml      # 开发环境（本地数据库和Redis）
├── application-test.yml     # 测试环境
└── application-prod.yml     # 生产环境（使用环境变量）
```

## 核心功能

- ✅ 用户登录（用户名/密码）
- ✅ 自动续签（30天有效期，自动延长）
- ✅ 用户登出
- ✅ RBAC 权限控制
- ✅ 用户管理（管理员）
- ✅ 密码管理

## 默认账号

**管理员账号**:
- 用户名: `admin`
- 密码: `admin123`
- 角色: 管理员（拥有所有权限）

⚠️ **生产环境请立即修改默认密码！**

## 项目结构

```
Open436-Auth/
├── src/
│   ├── main/
│   │   ├── java/com/open436/auth/
│   │   │   ├── config/          # 配置类
│   │   │   ├── controller/      # 控制器
│   │   │   ├── dto/             # 数据传输对象
│   │   │   ├── entity/          # 实体类
│   │   │   ├── repository/      # 数据访问层
│   │   │   ├── service/         # 业务逻辑层
│   │   │   └── exception/       # 异常处理
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-test.yml
│   │       ├── application-prod.yml
│   │       └── db/migration/    # 数据库迁移脚本
│   └── test/
├── init-database.sql            # 快速初始化脚本
└── pom.xml
```

## 相关文档

- [技术设计文档](../docs/TDD/M1-认证授权服务/)
- [开发任务清单](../docs/TDD/M1-认证授权服务/05-开发任务清单.md)
- [API 接口设计](../docs/TDD/M1-认证授权服务/02-API接口设计.md)
- [数据库设计](../docs/TDD/M1-认证授权服务/01-数据库设计.md)

## 许可证

MIT License


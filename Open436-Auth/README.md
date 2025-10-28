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

#### 3.1 环境变量配置

参考 `env.example` 文件配置环境变量：

```bash
# 查看环境变量示例
cat env.example
```

编辑 `.env` 文件，填写实际配置：

```properties
# 数据库配置
DB_URL=jdbc:postgresql://localhost:5432/open436
DB_USERNAME=postgres
DB_PASSWORD=your_database_password_here

# Redis配置
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# 应用配置
SPRING_PROFILES_ACTIVE=dev
```

**重要说明**：
- 开发环境：配置文件已改用环境变量 `${DB_PASSWORD:postgres}`，默认密码为 `postgres`
- 测试环境：使用独立的测试数据库配置
- 生产环境：**必须**通过环境变量配置敏感信息，不要在配置文件中硬编码

#### 3.2 切换环境

可以通过以下方式切换运行环境：

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

## 最近更新

### 代码优化 (2024)

最近完成了全面的代码优化，主要改进包括：

**安全性提升**:
- ✅ 移除硬编码密码，改用环境变量
- ✅ 使用枚举统一管理错误码和状态值
- ✅ 增强参数验证，添加 `@PasswordMatch` 注解

**架构优化**:
- ✅ 修复事务边界问题，拆分数据库操作与外部状态操作
- ✅ 统一角色权限获取逻辑，创建 `RoleService`
- ✅ 实现权限和角色缓存自动失效机制
- ✅ 使用 `@ConfigurationProperties` 统一Token配置

**性能优化**:
- ✅ 修复N+1查询问题，改用LAZY加载 + `@EntityGraph`
- ✅ 优化实体类，避免循环依赖（`@Data` → `@Getter/@Setter`）
- ✅ 添加批量清理缓存方法

**代码质量**:
- ✅ 抽取魔法值为枚举常量（`UserStatus`, `ErrorCode`, `TokenConstants`）
- ✅ 重构重复代码，添加 `getPrimaryRoleCode()` 辅助方法
- ✅ 清理未使用的Repository方法
- ✅ 增强异常处理，支持ErrorCode枚举

## 相关文档

- [技术设计文档](../docs/TDD/M1-认证授权服务/)
- [开发任务清单](../docs/TDD/M1-认证授权服务/05-开发任务清单.md)
- [API 接口设计](../docs/TDD/M1-认证授权服务/02-API接口设计.md)
- [数据库设计](../docs/TDD/M1-认证授权服务/01-数据库设计.md)

## 许可证

MIT License


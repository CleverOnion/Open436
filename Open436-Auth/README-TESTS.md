# Auth 模块测试指南

## 测试概览

已为 Open436-Auth 模块创建了全面的测试套件，包括：

- **单元测试**：8 个 Service 层单元测试类
- **集成测试**：5 个 Repository 和 Service 集成测试类
- **API 测试**：3 个 Controller API 测试类
- **E2E 测试**：1 个端到端场景测试类
- **异常测试**：1 个全局异常处理测试类

**总计**: 约 110+个测试用例

## 测试结构

```
src/test/java/com/open436/auth/
├── base/                           # 测试基类
│   ├── BaseUnitTest.java          # 单元测试基类（Mockito）
│   ├── BaseIntegrationTest.java   # 集成测试基类（完整Spring上下文）
│   └── BaseApiTest.java           # API测试基类（MockMvc）
├── repository/                     # Repository集成测试
│   ├── UserAuthRepositoryTest.java
│   ├── RoleRepositoryTest.java
│   └── PermissionRepositoryTest.java
├── service/                        # Service单元测试
│   ├── AuthServiceTest.java
│   ├── UserServiceTest.java
│   ├── PermissionServiceTest.java
│   └── integration/               # Service集成测试
│       ├── AuthServiceIntegrationTest.java
│       └── PermissionServiceIntegrationTest.java
├── controller/                     # Controller API测试
│   ├── AuthControllerTest.java
│   ├── UserControllerTest.java
│   └── PermissionControllerTest.java
├── exception/                      # 异常处理测试
│   └── GlobalExceptionHandlerTest.java
└── e2e/                           # 端到端场景测试
    └── E2EAuthFlowTest.java
```

## 运行测试

### 前置条件

**必须启动以下服务**：

1. **PostgreSQL 数据库** (端口 5432)

   - 数据库名：open436
   - 确保已运行初始化脚本，包含 roles 和 permissions 表

2. **Redis 服务** (端口 16379)
   - 集成测试需要 Redis 用于缓存和 Session

### 运行所有测试

```powershell
# 使用Maven包装器
cd Open436-Auth
.\mvnw.cmd clean test

# 或使用Maven
mvn clean test
```

### 运行特定测试类

```powershell
# 运行单个测试类
.\mvnw.cmd test -Dtest=AuthServiceTest

# 运行特定包下的所有测试
.\mvnw.cmd test -Dtest="com.open436.auth.controller.*Test"

# 运行特定测试方法
.\mvnw.cmd test -Dtest=AuthServiceTest#testLogin_Success
```

### 跳过测试

```powershell
.\mvnw.cmd clean package -DskipTests
```

## 测试分类

### 1. 单元测试（不需要外部依赖）

测试 Service 层业务逻辑，使用 Mockito 模拟所有依赖：

```powershell
.\mvnw.cmd test -Dtest="com.open436.auth.service.*Test,com.open436.auth.exception.*Test"
```

**特点**：

- 执行速度快
- 不需要数据库和 Redis
- 完全隔离测试

### 2. 集成测试（需要数据库和 Redis）

测试 Repository 和真实的 Spring 集成：

```powershell
.\mvnw.cmd test -Dtest="com.open436.auth.repository.*Test,com.open436.auth.service.integration.*Test"
```

**特点**：

- 需要 PostgreSQL 和 Redis
- 测试真实的数据库交互
- 验证缓存机制

### 3. API 测试（需要数据库和 Redis）

测试 Controller 层 HTTP 端点：

```powershell
.\mvnw.cmd test -Dtest="com.open436.auth.controller.*Test"
```

**特点**：

- 使用 MockMvc 模拟 HTTP 请求
- 验证完整的请求/响应流程
- 测试权限控制

### 4. E2E 测试（需要数据库和 Redis）

测试完整的业务流程：

```powershell
.\mvnw.cmd test -Dtest="E2EAuthFlowTest"
```

**特点**：

- 测试真实用户场景
- 验证多个端点的协作
- 包含完整的认证流程

## 测试数据

测试使用程序化方式创建测试用户，不依赖 SQL 脚本：

### 测试账号

| 用户名        | 密码    | 角色  | 状态     |
| ------------- | ------- | ----- | -------- |
| test_admin    | test123 | admin | active   |
| test_user     | test123 | user  | active   |
| test_disabled | test123 | user  | disabled |

### 数据隔离

- 每个测试方法使用`@Transactional`注解
- 测试完成后自动回滚数据库更改
- 确保测试之间互不影响

## 常见问题

### 1. Redis 连接失败

**错误**: `RedisConnectionFailureException: Unable to connect to Redis`

**解决方案**：

```powershell
# 启动Redis服务（Windows）
redis-server --port 16379

# 或检查Redis服务状态
redis-cli -p 16379 ping
```

### 2. 数据库连接失败

**错误**: `Connection refused` 或 `Could not open JDBC Connection`

**解决方案**：

- 确保 PostgreSQL 运行在端口 5432
- 检查数据库 open436 是否存在
- 验证用户名密码是否正确（postgres/Shicong666）

### 3. 测试用户登录失败

**错误**: `用户名或密码错误` 在集成测试中

**解决方案**：

- 确保数据库中有 admin 和 user 角色
- 检查 BaseIntegrationTest 的 setUpTestUsers 方法是否正常执行
- 验证 PasswordEncoder bean 是否正确配置

### 4. 权限测试失败

**错误**: `需要管理员权限` 或 `403 Forbidden`

**解决方案**：

- 确保测试用户的角色正确分配
- 检查 Sa-Token 配置是否正确
- 验证@SaCheckRole 注解是否生效

## 测试覆盖率

预期覆盖率目标：

- **Controller 层**: ~85%+ （部分 401/403 错误路径难以覆盖）
- **Service 层**: ~90%+
- **Repository 层**: ~95%+
- **异常处理**: 100%

### 查看覆盖率报告

如需生成覆盖率报告，可以添加 JaCoCo 插件到`pom.xml`：

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

然后运行：

```powershell
.\mvnw.cmd clean test jacoco:report
```

报告位置：`target/site/jacoco/index.html`

## 持续集成

建议在 CI/CD 流水线中：

1. **快速反馈**：先运行单元测试
2. **完整验证**：再运行集成和 API 测试
3. **场景测试**：最后运行 E2E 测试

示例 CI 配置：

```yaml
test:
  stage: test
  services:
    - postgres:15
    - redis:7
  script:
    - mvn test
```

## 维护建议

1. **新增功能时**：同时编写对应的单元测试和集成测试
2. **修复 Bug 时**：先写一个失败的测试用例，然后修复代码
3. **重构代码时**：确保所有测试通过，保证功能不变
4. **定期 review**：检查测试覆盖率，补充缺失的测试

## 参考资料

- [JUnit 5 文档](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito 文档](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/reference/testing/index.html)
- [Sa-Token 文档](https://sa-token.cc/)

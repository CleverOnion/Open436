# M1 认证授权服务 - API 接口设计

## 文档信息

**服务名称**: 认证授权服务 (auth-service)  
**Base URL**: `http://auth-service:8001` (内部) / `https://api.open436.com/api/auth` (通过 Kong)  
**版本**: v3.0  
**认证方案**: Sa-Token 自动续签（单 Token + 自动续期）

---

## 🔐 自动续签认证说明

### Token 说明

| Token     | 有效期            | 用途                                  | 存储位置       |
| --------- | ----------------- | ------------------------------------- | -------------- |
| **Token** | 30 天（自动续签） | API 调用，放在 `Authorization` header | 客户端 + Redis |

### 工作流程

1. **登录**: 返回 `token`（自动续签 Token）
2. **API 调用**: 使用 `token`（Header: `Authorization: Bearer {token}`）
3. **自动续签**: Token 在有效期内每次访问自动延长 30 天有效期
4. **登出**: 清除 Session 和 Token

### 安全优势

- ✅ Token 长期有效，自动续签，用户体验好
- ✅ Token 存储在 Redis，可随时撤销
- ✅ 登出后清除所有会话数据
- ✅ Sa-Token 框架成熟稳定，安全可靠

---

## 目录

1. [接口概览](#接口概览)
2. [认证接口](#认证接口)
3. [密码管理接口](#密码管理接口)
4. [用户管理接口](#用户管理接口)
5. [权限查询接口](#权限查询接口)
6. [错误码定义](#错误码定义)

---

## 接口概览

### 接口分类

| 分类     | 接口数量 | 是否需要认证 | 权限要求    |
| -------- | -------- | ------------ | ----------- |
| 认证接口 | 3        | 部分需要     | 无          |
| 密码管理 | 2        | 是           | 本人/管理员 |
| 用户管理 | 3        | 是           | 管理员      |
| 权限查询 | 2        | 是           | 本人/管理员 |

### 公开接口（无需 Token）

- `POST /api/auth/login` - 用户登录（返回自动续签 Token）

### 受保护接口（需要 Token）

- `POST /api/auth/logout` - 用户登出（清除 Session）
- `GET /api/auth/verify` - 验证 Token
- `GET /api/auth/current` - 获取当前用户信息
- `PUT /api/auth/password` - 修改密码（本人或管理员）
- `PUT /api/auth/users/:id/password` - 重置密码（管理员）
- `POST /api/auth/users` - 创建用户（管理员）
- `PUT /api/auth/users/:id/status` - 启用/禁用用户（管理员）
- `GET /api/auth/users/:id/permissions` - 获取用户权限

**说明**:

- 公开接口通过 Kong 直接转发到 M1 服务
- 受保护接口先经过 Kong JWT 验证，再转发到 M1 服务
- 权限检查（如管理员）在业务代码中使用 `@SaCheckRole` 注解
- Token 在有效期内会自动续签，无需手动刷新

---

## 认证接口

### 1. 用户登录

**接口**: `POST /api/auth/login`

**描述**: 用户使用用户名和密码登录系统

**是否需要认证**: 否

**请求参数**:

```json
{
  "username": "alice",
  "password": "password123",
  "rememberMe": false
}
```

| 参数       | 类型    | 必填 | 说明                   |
| ---------- | ------- | ---- | ---------------------- |
| username   | string  | 是   | 用户名，3-20 字符      |
| password   | string  | 是   | 密码，6-32 字符        |
| rememberMe | boolean | 否   | 是否记住我，默认 false |

**成功响应** (200):

```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "satoken:satoken:xyz123...",
    "expiresIn": 2592000,
    "user": {
      "id": 1,
      "username": "alice",
      "role": "user",
      "status": "active"
    }
  },
  "timestamp": "2025-10-23T10:30:00Z"
}
```

**响应字段说明**:

| 字段      | 类型   | 说明                                            |
| --------- | ------ | ----------------------------------------------- |
| token     | string | 自动续签 Token（30 天），每次访问自动延长有效期 |
| expiresIn | number | Token 过期时间（秒），30 天                     |
| user      | object | 用户基本信息                                    |

**错误响应**:

| HTTP 状态码 | 错误码   | 说明                         |
| ----------- | -------- | ---------------------------- |
| 400         | 40001001 | 用户名不能为空               |
| 400         | 40001002 | 密码不能为空                 |
| 401         | 40101001 | 用户名或密码错误             |
| 403         | 40301001 | 账号已被禁用                 |
| 429         | 42900001 | 登录尝试次数过多，请稍后再试 |

**业务逻辑**:

1. 验证参数格式
2. 查询用户是否存在
3. 检查账号状态（是否被禁用）
4. 验证密码（BCrypt compare）
5. 获取用户角色
6. 使用 Sa-Token 登录（自动生成 Token）
7. 设置 Session 信息到 Redis
8. 更新最后登录时间
9. 返回 Token 和用户信息

**实现示例**（使用 Sa-Token）:

```java
// AuthServiceImpl.java
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserAuthRepository userAuthRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public LoginResponse login(LoginRequest request) {
        // 1. 参数验证（通过 @Valid 注解自动验证）

        // 2. 查询用户
        UserAuth user = userAuthRepository
            .findByUsername(request.getUsername())
            .orElseThrow(() -> new BusinessException(40101001, "用户名或密码错误"));

        // 3. 检查账号状态
        if ("disabled".equals(user.getStatus())) {
            throw new BusinessException(40301001, "账号已被禁用，请联系管理员");
        }

        // 4. 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(40101001, "用户名或密码错误");
        }

        // 5. 获取用户角色
        String role = user.getRoles().stream()
            .findFirst()
            .map(Role::getCode)
            .orElse("user");

        // 6. 使用Sa-Token登录（自动生成Token并开启自动续签）
        StpUtil.login(user.getId(), new SaLoginModel()
            .setDevice("web")
            .setIsLastingCookie(true)
            .setTimeout(2592000)  // 30天
        );

        // 7. 设置Session信息
        StpUtil.getSession().set("username", user.getUsername());
        StpUtil.getSession().set("role", role);

        // 8. 获取Token值
        String token = StpUtil.getTokenValue();

        // 9. 更新最后登录时间
        user.setLastLoginAt(LocalDateTime.now());
        userAuthRepository.save(user);

        // 10. 返回Token和用户信息
        return LoginResponse.builder()
            .token(token)
            .expiresIn(2592000)  // 30天
            .user(UserInfoResponse.from(user))
            .build();
    }
}
```

**Sa-Token 优势**:

- ✅ 无需手写 JWT 生成逻辑
- ✅ 自动管理 Session 和 Redis
- ✅ 自动续签机制，无需手动刷新
- ✅ 代码量减少 60%+

---

### 2. 用户登出

**接口**: `POST /api/auth/logout`

**描述**: 用户退出登录

**是否需要认证**: 是

**请求参数**: 无

**请求头**:

```
Authorization: Bearer {accessToken}
```

**成功响应** (200):

```json
{
  "code": 200,
  "message": "已成功退出登录",
  "data": null,
  "timestamp": "2025-10-23T10:30:00Z"
}
```

**业务逻辑**:

1. 从 Sa-Token 获取当前用户 ID
2. Sa-Token 登出（自动清除 Session 和 Token）
3. 返回成功消息

**实现示例**（使用 Sa-Token）:

```java
// AuthServiceImpl.java
    @Override
public void logout() {
    // Sa-Token 登出（自动清除 Session 和 Token）
    StpUtil.logout();
}
```

**登出说明**:

- 清除 Redis 中的 Session 数据
- 清除 Token 缓存
- 标记 Token 为已失效
- 安全性高：一次性清除所有会话数据

---

### 3. 验证 Token

**接口**: `GET /api/auth/verify`

**描述**: 验证 Token 是否有效（供其他服务调用）

**是否需要认证**: 否（服务间调用）

**请求参数**:

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**成功响应** (200):

```json
{
  "code": 200,
  "message": "Token 有效",
  "data": {
    "valid": true,
    "userId": 1,
    "username": "alice",
    "role": "user"
  },
  "timestamp": "2025-10-23T10:30:00Z"
}
```

**失败响应** (401):

```json
{
  "code": 40101003,
  "message": "Token 无效",
  "data": {
    "valid": false
  },
  "timestamp": "2025-10-23T10:30:00Z"
}
```

---

## 密码管理接口

### 5. 修改密码

**接口**: `PUT /api/auth/password`

**描述**: 用户修改自己的密码

**是否需要认证**: 是

**权限要求**: 本人

**请求参数**:

```json
{
  "oldPassword": "oldpass123",
  "newPassword": "newpass456",
  "confirmPassword": "newpass456"
}
```

| 参数            | 类型   | 必填 | 说明              |
| --------------- | ------ | ---- | ----------------- |
| oldPassword     | string | 是   | 原密码            |
| newPassword     | string | 是   | 新密码，6-32 字符 |
| confirmPassword | string | 是   | 确认新密码        |

**成功响应** (200):

```json
{
  "code": 200,
  "message": "密码修改成功，请重新登录",
  "timestamp": "2025-10-23T10:30:00Z"
}
```

**错误响应**:

| HTTP 状态码 | 错误码   | 说明                   |
| ----------- | -------- | ---------------------- |
| 400         | 40001003 | 新密码长度不符合要求   |
| 400         | 40001004 | 两次输入的密码不一致   |
| 401         | 40101004 | 原密码错误             |
| 400         | 40001005 | 新密码不能与原密码相同 |

**业务逻辑**:

1. 从 Token 获取当前用户 ID
2. 验证原密码
3. 验证新密码格式
4. 验证两次密码一致性
5. 加密新密码（BCrypt）
6. 更新数据库
7. 清除所有 Token（强制重新登录）

---

### 6. 重置用户密码（管理员）

**接口**: `PUT /api/auth/users/:id/password`

**描述**: 管理员为用户重置密码

**是否需要认证**: 是

**权限要求**: 管理员

**路径参数**:

| 参数 | 类型    | 说明    |
| ---- | ------- | ------- |
| id   | integer | 用户 ID |

**请求参数**:

```json
{
  "newPassword": "resetpass123"
}
```

**成功响应** (200):

```json
{
  "code": 200,
  "message": "密码重置成功",
  "timestamp": "2025-10-23T10:30:00Z"
}
```

**错误响应**:

| HTTP 状态码 | 错误码   | 说明           |
| ----------- | -------- | -------------- |
| 403         | 40301002 | 需要管理员权限 |
| 404         | 40401001 | 用户不存在     |

---

## 用户管理接口

### 7. 创建用户（管理员）

**接口**: `POST /api/auth/users`

**描述**: 管理员创建新用户账号

**是否需要认证**: 是

**权限要求**: 管理员

**请求参数**:

```json
{
  "username": "bob",
  "password": "initialpass123",
  "role": "user"
}
```

| 参数     | 类型   | 必填 | 说明                    |
| -------- | ------ | ---- | ----------------------- |
| username | string | 是   | 用户名，3-20 字符，唯一 |
| password | string | 是   | 初始密码，6-32 字符     |
| role     | string | 是   | 角色：user/admin        |

**成功响应** (201):

```json
{
  "code": 201,
  "message": "用户创建成功",
  "data": {
    "id": 10,
    "username": "bob",
    "role": "user",
    "status": "active",
    "createdAt": "2025-10-23T10:30:00Z"
  },
  "timestamp": "2025-10-23T10:30:00Z"
}
```

**错误响应**:

| HTTP 状态码 | 错误码   | 说明                 |
| ----------- | -------- | -------------------- |
| 403         | 40301002 | 需要管理员权限       |
| 409         | 40901001 | 用户名已存在         |
| 400         | 40001006 | 用户名长度不符合要求 |

**业务逻辑**:

1. 验证管理员权限
2. 验证用户名唯一性
3. 加密密码（BCrypt）
4. 插入 users_auth 表
5. 分配角色（插入 user_roles 表）
6. 返回用户信息

**实现示例**:

```java
// UserService.java
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserAuthRepository userAuthRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public CreateUserResponse createUser(CreateUserRequest request, UserAuth currentUser) {
        String username = request.getUsername();
        String password = request.getPassword();
        String roleCode = request.getRole() != null ? request.getRole() : "user";

        // 1. 验证管理员权限（通过 @PreAuthorize 注解验证）

        // 2. 验证用户名长度（通过 @Valid 注解验证）

        // 3. 检查用户名是否存在
        if (userAuthRepository.existsByUsername(username)) {
            throw new BusinessException(40901001, "用户名已存在");
        }

        // 4. 加密密码
        String passwordHash = passwordEncoder.encode(password);

        // 5. 插入用户
        UserAuth newUser = new UserAuth();
        newUser.setUsername(username);
        newUser.setPasswordHash(passwordHash);
        newUser.setStatus("active");
        newUser = userAuthRepository.save(newUser);

        // 6. 分配角色
        Role role = roleRepository.findByCode(roleCode)
            .orElseThrow(() -> new BusinessException(40401002, "角色不存在"));

        UserRole userRole = new UserRole();
        userRole.setUserId(newUser.getId());
        userRole.setRoleId(role.getId());
        userRoleRepository.save(userRole);

        // 7. 返回结果
        return CreateUserResponse.builder()
            .id(newUser.getId())
            .username(newUser.getUsername())
            .role(roleCode)
            .status(newUser.getStatus())
            .createdAt(newUser.getCreatedAt())
            .build();
    }
}
```

---

### 8. 启用/禁用用户（管理员）

**接口**: `PUT /api/auth/users/:id/status`

**描述**: 管理员启用或禁用用户账号

**是否需要认证**: 是

**权限要求**: 管理员

**路径参数**:

| 参数 | 类型    | 说明    |
| ---- | ------- | ------- |
| id   | integer | 用户 ID |

**请求参数**:

```json
{
  "status": "disabled"
}
```

| 参数   | 类型   | 必填 | 说明                  |
| ------ | ------ | ---- | --------------------- |
| status | string | 是   | 状态：active/disabled |

**成功响应** (200):

```json
{
  "code": 200,
  "message": "用户状态已更新",
  "data": {
    "id": 10,
    "username": "bob",
    "status": "disabled"
  },
  "timestamp": "2025-10-23T10:30:00Z"
}
```

**错误响应**:

| HTTP 状态码 | 错误码   | 说明           |
| ----------- | -------- | -------------- |
| 403         | 40301002 | 需要管理员权限 |
| 404         | 40401001 | 用户不存在     |
| 400         | 40001007 | 状态值无效     |

**业务逻辑**:

1. 验证管理员权限
2. 验证用户是否存在
3. 更新用户状态
4. 如果是禁用操作，清除该用户所有 Token
5. 返回更新后的用户信息

---

## 权限查询接口

### 9. 获取用户权限

**接口**: `GET /api/auth/users/:id/permissions`

**描述**: 获取指定用户的所有权限

**是否需要认证**: 是

**权限要求**: 本人或管理员

**路径参数**:

| 参数 | 类型    | 说明    |
| ---- | ------- | ------- |
| id   | integer | 用户 ID |

**成功响应** (200):

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "userId": 1,
    "username": "alice",
    "roles": ["user"],
    "permissions": [
      {
        "code": "post:create",
        "name": "创建帖子",
        "resource": "post",
        "action": "create"
      },
      {
        "code": "post:update_own",
        "name": "编辑自己的帖子",
        "resource": "post",
        "action": "update"
      }
    ]
  },
  "timestamp": "2025-10-23T10:30:00Z"
}
```

**SQL 查询**:

```sql
SELECT DISTINCT p.code, p.name, p.resource, p.action
FROM permissions p
JOIN role_permissions rp ON p.id = rp.permission_id
JOIN user_roles ur ON rp.role_id = ur.role_id
WHERE ur.user_id = $1
ORDER BY p.resource, p.action;
```

---

## 错误码定义

### 认证模块错误码 (401xx)

| 错误码   | HTTP 状态码 | 说明                   |
| -------- | ----------- | ---------------------- |
| 40001001 | 400         | 用户名不能为空         |
| 40001002 | 400         | 密码不能为空           |
| 40001003 | 400         | 新密码长度不符合要求   |
| 40001004 | 400         | 两次输入的密码不一致   |
| 40001005 | 400         | 新密码不能与原密码相同 |
| 40001006 | 400         | 用户名长度不符合要求   |
| 40001007 | 400         | 状态值无效             |
| 40101001 | 401         | 用户名或密码错误       |
| 40101002 | 401         | Token 已过期           |
| 40101003 | 401         | Token 无效             |
| 40101004 | 401         | 原密码错误             |
| 40301001 | 403         | 账号已被禁用           |
| 40301002 | 403         | 需要管理员权限         |
| 40401001 | 404         | 用户不存在             |
| 40901001 | 409         | 用户名已存在           |
| 42900001 | 429         | 登录尝试次数过多       |
| 50000000 | 500         | 服务器内部错误         |

---

## 接口测试

### Postman 集合示例

```json
{
  "info": {
    "name": "Auth Service API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Login",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"username\": \"alice\",\n  \"password\": \"password123\"\n}"
        },
        "url": {
          "raw": "http://localhost:8001/api/auth/login",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8001",
          "path": ["api", "auth", "login"]
        }
      }
    }
  ]
}
```

---

**文档版本**: v1.0  
**创建日期**: 2025-10-23  
**最后更新**: 2025-10-23

# M1 认证授权服务 - RBAC 权限模型

## 文档信息

**服务名称**: 认证授权服务 (auth-service)  
**权限模型**: RBAC (Role-Based Access Control)  
**版本**: v1.0

---

## 目录

1. [RBAC 概述](#rbac-概述)
2. [权限模型设计](#权限模型设计)
3. [权限定义](#权限定义)
4. [权限验证实现](#权限验证实现)
5. [权限查询](#权限查询)
6. [权限缓存策略](#权限缓存策略)

---

## RBAC 概述

### 1.1 什么是 RBAC

RBAC (Role-Based Access Control) 是一种基于角色的访问控制模型。

**核心概念**:
- **用户 (User)**: 系统的使用者
- **角色 (Role)**: 用户的身份标识
- **权限 (Permission)**: 对资源的操作许可
- **资源 (Resource)**: 系统中的实体（帖子、用户、板块等）

**关系**:
```
用户 → 角色 → 权限 → 资源
```

### 1.2 RBAC 优势

| 优势 | 说明 |
|------|------|
| **简化管理** | 通过角色批量分配权限 |
| **职责分离** | 不同角色有不同的权限范围 |
| **易于扩展** | 新增角色或权限不影响现有结构 |
| **符合直觉** | 角色概念易于理解 |

---

## 权限模型设计

### 2.1 模型结构

```
┌─────────────┐
│    User     │
│   (用户)     │
└──────┬──────┘
       │ N:M
       ↓
┌─────────────┐
│  User_Role  │
│ (用户角色)   │
└──────┬──────┘
       │ N:1
       ↓
┌─────────────┐
│    Role     │
│   (角色)     │
└──────┬──────┘
       │ N:M
       ↓
┌──────────────┐
│Role_Permission│
│ (角色权限)    │
└──────┬───────┘
       │ N:1
       ↓
┌─────────────┐
│ Permission  │
│   (权限)     │
└─────────────┘
```

### 2.2 角色定义

| 角色ID | 角色代码 | 角色名称 | 说明 |
|--------|---------|---------|------|
| 1 | `user` | 普通用户 | 可以发帖、回复、点赞、收藏 |
| 2 | `admin` | 管理员 | 拥有全站管理权限 |

**未来可扩展角色**:
- `moderator` - 版主（管理特定板块）
- `vip` - VIP用户（更多权限）
- `guest` - 访客（只读权限）

### 2.3 权限命名规范

**格式**: `{resource}:{action}`

| 部分 | 说明 | 示例 |
|------|------|------|
| resource | 资源类型 | post, user, section, system |
| action | 操作类型 | create, read, update, delete, manage |

**特殊操作**:
- `manage`: 完全管理权限（包含 CRUD）
- `update_own`: 只能更新自己的资源
- `delete_own`: 只能删除自己的资源

---

## 权限定义

### 3.1 内容权限 (Post)

| 权限代码 | 权限名称 | 普通用户 | 管理员 | 说明 |
|---------|---------|---------|--------|------|
| `post:create` | 创建帖子 | ✅ | ✅ | 发布新帖子 |
| `post:read` | 查看帖子 | ✅ | ✅ | 查看帖子内容 |
| `post:update_own` | 编辑自己的帖子 | ✅ | ✅ | 编辑自己发布的帖子 |
| `post:delete_own` | 删除自己的帖子 | ✅ | ✅ | 删除自己发布的帖子 |
| `post:manage` | 管理所有帖子 | ❌ | ✅ | 编辑、删除任意帖子，置顶 |

### 3.2 回复权限 (Reply)

| 权限代码 | 权限名称 | 普通用户 | 管理员 | 说明 |
|---------|---------|---------|--------|------|
| `reply:create` | 创建回复 | ✅ | ✅ | 回复帖子 |
| `reply:update_own` | 编辑自己的回复 | ✅ | ✅ | 编辑自己的回复 |
| `reply:delete_own` | 删除自己的回复 | ✅ | ✅ | 删除自己的回复 |
| `reply:manage` | 管理所有回复 | ❌ | ✅ | 删除任意回复 |

### 3.3 互动权限 (Interaction)

| 权限代码 | 权限名称 | 普通用户 | 管理员 | 说明 |
|---------|---------|---------|--------|------|
| `interaction:like` | 点赞 | ✅ | ✅ | 点赞帖子 |
| `interaction:favorite` | 收藏 | ✅ | ✅ | 收藏帖子 |

### 3.4 用户管理权限 (User)

| 权限代码 | 权限名称 | 普通用户 | 管理员 | 说明 |
|---------|---------|---------|--------|------|
| `user:manage` | 管理用户 | ❌ | ✅ | 创建、编辑、禁用用户 |

### 3.5 板块管理权限 (Section)

| 权限代码 | 权限名称 | 普通用户 | 管理员 | 说明 |
|---------|---------|---------|--------|------|
| `section:manage` | 管理板块 | ❌ | ✅ | 创建、编辑、删除板块 |

### 3.6 系统管理权限 (System)

| 权限代码 | 权限名称 | 普通用户 | 管理员 | 说明 |
|---------|---------|---------|--------|------|
| `system:manage` | 系统配置 | ❌ | ✅ | 修改系统配置 |

---

## 权限验证实现

### 4.1 权限验证实现

**文件**: `PermissionService.java`

```java
package com.open436.auth.service;

import com.open436.auth.entity.Permission;
import com.open436.auth.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionService {
    
    private final PermissionRepository permissionRepository;
    
    /**
     * 获取用户权限（带缓存）
     * @param userId 用户ID
     * @return 权限列表
     */
    @Cacheable(value = "userPermissions", key = "#userId")
    public List<Permission> getUserPermissions(Long userId) {
        return permissionRepository.findByUserId(userId);
    }
    
    /**
     * 检查用户是否拥有指定权限
     * @param userId 用户ID
     * @param permissionCode 权限代码
     * @return 是否拥有权限
     */
    public boolean hasPermission(Long userId, String permissionCode) {
        List<Permission> permissions = getUserPermissions(userId);
        return permissions.stream()
            .anyMatch(p -> p.getCode().equals(permissionCode));
    }
}
```

**Repository**:

```java
package com.open436.auth.repository;

import com.open436.auth.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    
    @Query("SELECT DISTINCT p FROM Permission p " +
           "JOIN RolePermission rp ON p.id = rp.permissionId " +
           "JOIN UserRole ur ON rp.roleId = ur.roleId " +
           "WHERE ur.userId = :userId")
    List<Permission> findByUserId(@Param("userId") Long userId);
}
```

**使用 Spring Security 注解**:

```java
// 方式1：使用 @PreAuthorize 注解
@PostMapping("/posts")
@PreAuthorize("hasAuthority('post:create')")
public ResponseEntity<?> createPost(@RequestBody CreatePostRequest request) {
    // 创建帖子逻辑
}

// 方式2：自定义权限检查
@Component("permissionChecker")
public class PermissionChecker {
    
    @Autowired
    private PermissionService permissionService;
    
    public boolean check(Long userId, String permission) {
        return permissionService.hasPermission(userId, permission);
    }
}

@PostMapping("/posts")
@PreAuthorize("@permissionChecker.check(authentication.principal, 'post:create')")
public ResponseEntity<?> createPost(@RequestBody CreatePostRequest request) {
    // 创建帖子逻辑
}
```

### 4.2 使用权限验证

**Controller 示例**:

```java
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
    
    private final PostService postService;
    
    // 创建帖子（需要 post:create 权限）
    @PostMapping
    @PreAuthorize("hasAuthority('post:create')")
    public ResponseEntity<ApiResponse<Post>> createPost(
            @Valid @RequestBody CreatePostRequest request,
            @AuthenticationPrincipal Long userId) {
        Post post = postService.createPost(request, userId);
        return ResponseEntity.ok(ApiResponse.success(post));
    }
    
    // 删除任意帖子（需要 post:manage 权限）
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('post:manage')")
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}

@RestController
@RequestMapping("/api/auth/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    // 管理用户（需要 user:manage 权限）
    @PostMapping
    @PreAuthorize("hasAuthority('user:manage')")
    public ResponseEntity<ApiResponse<User>> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        User user = userService.createUser(request);
        return ResponseEntity.ok(ApiResponse.success(user));
    }
}
```

### 4.3 资源所有权验证

**场景**: 用户只能编辑/删除自己的帖子

```java
/**
 * 资源所有权验证
 */
@Component
@RequiredArgsConstructor
public class ResourceOwnershipChecker {
    
    private final PostRepository postRepository;
    private final ReplyRepository replyRepository;
    
    /**
     * 检查帖子所有权
     */
    public boolean isPostOwner(Long postId, Long userId, String role) {
        // 管理员跳过所有权检查
        if ("admin".equals(role)) {
            return true;
        }
        
        return postRepository.findById(postId)
            .map(post -> post.getAuthorId().equals(userId))
            .orElseThrow(() -> new BusinessException(40401001, "资源不存在"));
    }
    
    /**
     * 检查回复所有权
     */
    public boolean isReplyOwner(Long replyId, Long userId, String role) {
        if ("admin".equals(role)) {
            return true;
        }
        
        return replyRepository.findById(replyId)
            .map(reply -> reply.getUserId().equals(userId))
            .orElseThrow(() -> new BusinessException(40401001, "资源不存在"));
    }
}

// 使用示例
@PutMapping("/posts/{id}")
@PreAuthorize("hasAuthority('post:update_own') and @resourceOwnershipChecker.isPostOwner(#id, authentication.principal, authentication.authorities[0].authority)")
public ResponseEntity<ApiResponse<Post>> updatePost(
        @PathVariable Long id,
        @Valid @RequestBody UpdatePostRequest request,
        @AuthenticationPrincipal Long userId) {
    Post post = postService.updatePost(id, request, userId);
    return ResponseEntity.ok(ApiResponse.success(post));
}
```

### 4.4 组合权限验证

**场景**: 编辑帖子需要满足以下条件之一：
- 拥有 `post:manage` 权限（管理员）
- 拥有 `post:update_own` 权限且是帖子作者

```java
/**
 * 组合权限验证
 */
@Component("postPermissionEvaluator")
@RequiredArgsConstructor
public class PostPermissionEvaluator {
    
    private final PermissionService permissionService;
    private final PostRepository postRepository;
    
    /**
     * 检查是否可以更新帖子
     */
    public boolean canUpdate(Long postId, Long userId) {
        // 检查是否有管理权限
        if (permissionService.hasPermission(userId, "post:manage")) {
            return true;
        }
        
        // 检查是否有更新自己帖子的权限
        if (permissionService.hasPermission(userId, "post:update_own")) {
            // 检查是否是帖子作者
            return postRepository.findById(postId)
                .map(post -> post.getAuthorId().equals(userId))
                .orElse(false);
        }
        
        return false;
    }
}

// 使用示例
@PutMapping("/posts/{id}")
@PreAuthorize("@postPermissionEvaluator.canUpdate(#id, authentication.principal)")
public ResponseEntity<ApiResponse<Post>> updatePost(
        @PathVariable Long id,
        @Valid @RequestBody UpdatePostRequest request,
        @AuthenticationPrincipal Long userId) {
    Post post = postService.updatePost(id, request, userId);
    return ResponseEntity.ok(ApiResponse.success(post));
}
```

---

## 权限查询

### 5.1 获取用户权限列表

**接口**: `GET /api/auth/users/:id/permissions`

```java
// PermissionController.java
@RestController
@RequestMapping("/api/auth/users")
@RequiredArgsConstructor
public class PermissionController {
    
    private final PermissionService permissionService;
    private final UserAuthRepository userAuthRepository;
    private final RoleRepository roleRepository;
    
    @GetMapping("/{id}/permissions")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal")
    public ResponseEntity<ApiResponse<UserPermissionsResponse>> getUserPermissions(
            @PathVariable Long id,
            @AuthenticationPrincipal Long currentUserId) {
        
        // 查询用户信息
        UserAuth user = userAuthRepository.findById(id)
            .orElseThrow(() -> new BusinessException(40401001, "用户不存在"));
        
        // 查询用户角色
        List<Role> roles = roleRepository.findByUserId(id);
        
        // 查询用户权限
        List<Permission> permissions = permissionService.getUserPermissions(id);
        
        // 按资源分组
        Map<String, List<PermissionDto>> groupedPermissions = permissions.stream()
            .collect(Collectors.groupingBy(
                Permission::getResource,
                Collectors.mapping(
                    p -> PermissionDto.builder()
                        .code(p.getCode())
                        .name(p.getName())
                        .action(p.getAction())
                        .build(),
                    Collectors.toList()
                )
            ));
        
        UserPermissionsResponse response = UserPermissionsResponse.builder()
            .userId(user.getId())
            .username(user.getUsername())
            .status(user.getStatus())
            .roles(roles.stream()
                .map(r -> RoleDto.builder()
                    .code(r.getCode())
                    .name(r.getName())
                    .build())
                .collect(Collectors.toList()))
            .permissions(groupedPermissions)
            .totalPermissions(permissions.size())
            .build();
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
```

**响应示例**:

```json
{
  "code": 200,
  "data": {
    "userId": 1,
    "username": "alice",
    "status": "active",
    "roles": [
      {
        "code": "user",
        "name": "普通用户"
      }
    ],
    "permissions": {
      "post": [
        {
          "code": "post:create",
          "name": "创建帖子",
          "action": "create"
        },
        {
          "code": "post:update_own",
          "name": "编辑自己的帖子",
          "action": "update"
        }
      ],
      "reply": [
        {
          "code": "reply:create",
          "name": "创建回复",
          "action": "create"
        }
      ],
      "interaction": [
        {
          "code": "interaction:like",
          "name": "点赞",
          "action": "create"
        }
      ]
    },
    "totalPermissions": 8
  }
}
```

### 5.2 检查特定权限

**接口**: `GET /api/auth/check-permission`

```java
// PermissionController.java
@GetMapping("/check-permission")
public ResponseEntity<ApiResponse<CheckPermissionResponse>> checkPermission(
        @RequestParam String permission,
        @AuthenticationPrincipal Long userId) {
    
    if (permission == null || permission.isEmpty()) {
        throw new BusinessException(40001001, "权限代码不能为空");
    }
    
    boolean hasPermission = permissionService.hasPermission(userId, permission);
    
    CheckPermissionResponse response = CheckPermissionResponse.builder()
        .permission(permission)
        .hasPermission(hasPermission)
        .build();
    
    return ResponseEntity.ok(ApiResponse.success(response));
}
```

---

## 权限缓存策略

### 6.1 缓存设计

**缓存 Key 格式**:
```
user_permissions:{userId}
```

**缓存内容**:
```json
[
  {
    "code": "post:create",
    "name": "创建帖子",
    "resource": "post",
    "action": "create"
  }
]
```

**TTL**: 30 分钟

### 6.2 缓存实现

**使用 Spring Cache 注解**:

```java
// PermissionService.java
@Service
@RequiredArgsConstructor
public class PermissionService {
    
    private final PermissionRepository permissionRepository;
    private final UserRoleRepository userRoleRepository;
    
    /**
     * 获取用户权限（带缓存）
     * 缓存 Key: userPermissions::userId
     * TTL: 30分钟
     */
    @Cacheable(value = "userPermissions", key = "#userId")
    public List<Permission> getUserPermissions(Long userId) {
        return permissionRepository.findByUserId(userId);
    }
    
    /**
     * 清除用户权限缓存
     */
    @CacheEvict(value = "userPermissions", key = "#userId")
    public void clearUserPermissionsCache(Long userId) {
        // 缓存会自动清除
    }
    
    /**
     * 清除角色相关的所有用户权限缓存
     */
    @CacheEvict(value = "userPermissions", allEntries = true)
    public void clearRolePermissionsCache(Long roleId) {
        // 清除所有用户权限缓存
        // 或者精确清除：
        List<Long> userIds = userRoleRepository.findUserIdsByRoleId(roleId);
        userIds.forEach(this::clearUserPermissionsCache);
    }
}
```

**Redis 配置**:

```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30))  // 30分钟过期
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new StringRedisSerializer()
                )
            )
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new GenericJackson2JsonRedisSerializer()
                )
            );
        
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .build();
    }
}
```

### 6.3 缓存失效场景

| 场景 | 操作 |
|------|------|
| 用户角色变更 | 清除该用户的权限缓存 |
| 角色权限变更 | 清除所有拥有该角色的用户缓存 |
| 用户被禁用 | 清除该用户的权限缓存 |

**示例**:

```java
// UserService.java
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRoleRepository userRoleRepository;
    private final PermissionService permissionService;
    
    /**
     * 分配角色后清除缓存
     */
    @Transactional
    public void assignRole(Long userId, Long roleId) {
        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        userRoleRepository.save(userRole);
        
        // 清除缓存
        permissionService.clearUserPermissionsCache(userId);
    }
}

// RoleService.java
@Service
@RequiredArgsConstructor
public class RoleService {
    
    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionService permissionService;
    
    /**
     * 修改角色权限后清除缓存
     */
    @Transactional
    public void updateRolePermissions(Long roleId, List<Long> permissionIds) {
        // 删除旧权限
        rolePermissionRepository.deleteByRoleId(roleId);
        
        // 插入新权限
        List<RolePermission> rolePermissions = permissionIds.stream()
            .map(permissionId -> {
                RolePermission rp = new RolePermission();
                rp.setRoleId(roleId);
                rp.setPermissionId(permissionId);
                return rp;
            })
            .collect(Collectors.toList());
        
        rolePermissionRepository.saveAll(rolePermissions);
        
        // 清除所有拥有该角色的用户缓存
        permissionService.clearRolePermissionsCache(roleId);
    }
}
```

---

## 权限扩展

### 7.1 动态权限

**场景**: 根据业务规则动态判断权限

```java
/**
 * 动态权限检查
 */
@Component
@RequiredArgsConstructor
public class DynamicPermissionChecker {
    
    private final PermissionService permissionService;
    private final PostRepository postRepository;
    
    /**
     * 检查动态权限
     */
    public boolean checkDynamicPermission(Long userId, String action, String resource, Long resourceId) {
        // 基础权限检查
        String permissionCode = resource + ":" + action;
        if (!permissionService.hasPermission(userId, permissionCode)) {
            return false;
        }
        
        // 动态规则检查
        if ("update".equals(action) && "post".equals(resource)) {
            // 检查帖子是否在可编辑时间内（如：发布后24小时内）
            return postRepository.findById(resourceId)
                .map(post -> {
                    long hoursSinceCreated = Duration.between(
                        post.getCreatedAt().toInstant(),
                        Instant.now()
                    ).toHours();
                    
                    return hoursSinceCreated <= 24; // 24小时内可编辑
                })
                .orElse(false);
        }
        
        return true;
    }
}
```

### 7.2 权限继承

**场景**: 管理员自动拥有所有权限

```java
@Service
@RequiredArgsConstructor
public class PermissionService {
    
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    
    @Cacheable(value = "userPermissions", key = "#userId")
    public List<Permission> getUserPermissions(Long userId) {
        // 查询用户角色
        List<Role> roles = roleRepository.findByUserId(userId);
        
        // 如果是管理员，返回所有权限
        boolean isAdmin = roles.stream()
            .anyMatch(role -> "admin".equals(role.getCode()));
        
        if (isAdmin) {
            return permissionRepository.findAll();
        }
        
        // 否则查询用户的权限
        return permissionRepository.findByUserId(userId);
    }
}
```

---

**文档版本**: v1.0  
**创建日期**: 2025-10-23  
**最后更新**: 2025-10-23

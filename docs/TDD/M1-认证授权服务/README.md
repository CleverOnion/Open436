# M1 - 认证授权服务技术设计文档

## 文档概述

本文件夹包含 **S1 认证授权服务 (auth-service)** 的详细技术设计文档。

**服务职责**: 用户身份认证、Token 自动续签管理、RBAC 权限控制

**技术栈**: Java 17 + Spring Boot 3.x + **Sa-Token** + PostgreSQL + Redis

**认证方案**: Sa-Token 自动续签（单 Token + 自动续期）

**核心特性**:

- ✅ 使用 Sa-Token（替代 Spring Security，更简单高效）
- ✅ 自动续签机制（单 Token，自动延长有效期，无需手动刷新）
- ✅ 与 Kong Gateway 完美集成

---

## 📚 文档列表

| 文档                                     | 说明                         | 页数   | 状态      |
| ---------------------------------------- | ---------------------------- | ------ | --------- |
| [01-数据库设计](./01-数据库设计.md)      | 数据库表结构、索引、关系设计 | ~40 页 | ✅ 已完成 |
| [02-API 接口设计](./02-API接口设计.md)   | RESTful API 详细设计         | ~35 页 | ✅ 已完成 |
| [03-JWT 实现方案](./03-JWT实现方案.md)   | JWT Token 生成、验证、续期   | ~30 页 | ✅ 已完成 |
| [04-RBAC 权限模型](./04-RBAC权限模型.md) | 角色权限模型设计与实现       | ~25 页 | ✅ 已完成 |
| [05-开发任务清单](./05-开发任务清单.md)  | 详细开发任务和进度跟踪       | ~50 页 | ✅ 已完成 |

---

## 🎯 快速导航

### 新开发者入门

1. **阅读开发任务清单** - 了解开发流程和任务分解（⭐ 推荐首读）
2. **阅读数据库设计** - 了解数据模型
3. **阅读 API 接口设计** - 了解对外接口
4. **阅读 JWT 实现方案** - 理解认证机制
5. **阅读 RBAC 权限模型** - 掌握权限控制

### 核心技术栈

- **语言**: Java 17+
- **框架**: Spring Boot 3.x
- **认证框架**: Sa-Token 1.37.0（替代 Spring Security）
- **数据库**: PostgreSQL 14+
- **缓存**: Redis 7+（存储会话）
- **ORM**: Spring Data JPA
- **密码加密**: BCrypt（spring-security-crypto）
- **Token 方案**: 自动续签（单 Token + 自动续期）

---

## 🔑 核心功能

### 用户认证（自动续签方案）

- ✅ 用户登录（用户名/密码）
- ✅ 生成 Token（自动续签 Token）
- ✅ 用户登出（清除 Session）
- ✅ 自动续签（访问时自动延长有效期）
- ✅ Token 验证

### 密码管理

- ✅ 修改密码
- ✅ 重置密码（管理员）
- ✅ 密码加密存储（BCrypt）

### 用户管理（管理员）

- ✅ 创建用户账号
- ✅ 启用/禁用用户
- ✅ 分配用户角色

### 权限控制（Sa-Token）

- ✅ RBAC 角色权限模型
- ✅ 基于注解的权限验证（`@SaCheckRole`、`@SaCheckPermission`）
- ✅ 编程式权限检查（`StpUtil.checkRole()`）
- ✅ 角色权限查询

---

## 📊 数据模型概览

```
users_auth (用户认证表)
├── id (主键)
├── username (用户名，唯一)
├── password_hash (密码哈希)
├── status (账号状态)
└── created_at, updated_at

roles (角色表)
├── id (主键)
├── name (角色名称)
└── description

permissions (权限表)
├── id (主键)
├── name (权限名称)
└── resource, action

user_roles (用户角色关联)
└── user_id, role_id

role_permissions (角色权限关联)
└── role_id, permission_id
```

---

## 🔗 相关文档

- [PRD - M1 认证与授权模块](../../PRD/M1-认证与授权模块.md)
- [全局架构设计](../00-全局架构/01-全局架构设计.md)
- [服务间通信规范](../00-全局架构/03-服务间通信规范.md)

---

**服务端口**: 8001  
**技术栈**: Java 17 + Spring Boot 3.x + **Sa-Token** + PostgreSQL + Redis  
**优先级**: P0（最高优先级）

**开发效率提升**:

- 相比 Spring Security 方案减少 **9.5 小时**开发时间
- 代码量减少约 **70%**
- 学习成本降低 **60%**

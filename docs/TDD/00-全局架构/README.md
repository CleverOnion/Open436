# 全局架构设计文档

本文件夹包含 Open436 论坛系统的全局架构设计文档，适用于所有微服务。

---

## 📚 文档列表

| 文档 | 说明 | 阅读时间 |
|------|------|---------|
| [01-全局架构设计](./01-全局架构设计.md) | 微服务架构总览、服务划分、Consul 服务发现 | 30分钟 |
| [03-服务间通信规范](./03-服务间通信规范.md) ⭐ | Consul 服务发现、Sa-Token 鉴权集成（重点） | 2小时 |
| [04-API设计规范](./04-API设计规范.md) | RESTful API 规范、响应格式 | 1小时 |
| [05-部署运维指南](./05-部署运维指南.md) | Docker/K8s 部署、CI/CD | 1小时 |

---

## 🎯 推荐阅读顺序

1. **01-全局架构设计** - 理解整体架构和 Consul 服务注册
2. **03-服务间通信规范** - 掌握 Consul 服务发现和 Sa-Token 鉴权（最重要）
3. **04-API设计规范** - 学习 API 标准
4. **05-部署运维指南** - 搭建开发环境

---

## 💡 核心要点

### 服务间如何通信？

**通过 Consul 服务发现 + RESTful API 调用**：

```javascript
const serviceClient = require('./utils/serviceClient');

// 调用用户服务（Consul 自动发现服务地址）
const user = await serviceClient.get('user-service', '/api/users/123', token);
```

**工作原理**：
1. ServiceClient 从 Consul 查询 `user-service` 的健康实例
2. 自动负载均衡选择一个实例
3. 发起 HTTP 请求并携带 Sa-Token
4. 缓存服务实例列表（30秒）

### 如何获取当前登录用户？

**从 Sa-Token 获取用户信息**：

```javascript
const serviceClient = require('./utils/serviceClient');

// 调用 M1 认证服务验证 Sa-Token
const token = req.headers['authorization']?.replace('Bearer ', '');
const result = await serviceClient.post('auth-service', '/api/auth/verify', { token });

if (result.valid) {
  const { userId, username, role } = result;
  // 用户已认证
}
```

**Sa-Token 特点**：
- ✅ 单 Token 自动续签，有效期 30 天
- ✅ 无需手动刷新 Token
- ✅ 存储在 Redis，可随时撤销
- ✅ UUID 格式，非标准 JWT

### 如何验证用户权限？

```javascript
// 检查是否为管理员
if (role !== 'admin') {
  return res.status(403).json({ error: 'Forbidden' });
}

// 检查是否为本人
if (userId !== targetUserId && role !== 'admin') {
  return res.status(403).json({ error: 'Forbidden' });
}

// 检查具体权限
const permissions = await serviceClient.get(
  'auth-service',
  `/api/auth/users/${userId}/permissions`,
  token
);
if (!permissions.some(p => p.code === 'delete_any_post')) {
  return res.status(403).json({ error: 'No permission' });
}
```

---

**返回**: [TDD 主目录](../README.md)

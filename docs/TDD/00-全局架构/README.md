# 全局架构设计文档

本文件夹包含 Open436 论坛系统的全局架构设计文档，适用于所有微服务。

---

## 📚 文档列表

| 文档 | 说明 | 阅读时间 |
|------|------|---------|
| [01-全局架构设计](./01-全局架构设计.md) | 微服务架构总览、服务划分、技术选型 | 30分钟 |
| [02-Kong网关配置指南](./02-Kong网关配置指南.md) | Kong 部署、服务注册、JWT 认证配置 | 1小时 |
| [03-服务间通信规范](./03-服务间通信规范.md) ⭐ | 服务调用、鉴权集成（重点） | 2小时 |
| [04-API设计规范](./04-API设计规范.md) | RESTful API 规范、响应格式 | 1小时 |
| [05-部署运维指南](./05-部署运维指南.md) | Docker/K8s 部署、CI/CD | 1小时 |

---

## 🎯 推荐阅读顺序

1. **01-全局架构设计** - 理解整体架构
2. **03-服务间通信规范** - 掌握服务调用和鉴权（最重要）
3. **02-Kong网关配置指南** - 了解网关配置
4. **04-API设计规范** - 学习 API 标准
5. **05-部署运维指南** - 搭建开发环境

---

## 💡 核心要点

### 服务间如何通信？

**必须通过 Kong 网关**，使用封装好的 `ServiceClient`：

```javascript
const serviceClient = require('./utils/serviceClient');

// 调用用户服务
const user = await serviceClient.get('/api/users/123');
```

### 如何获取当前登录用户？

**从 Kong 注入的 Header 获取**（推荐）：

```javascript
const userId = req.headers['x-jwt-claim-sub'];
const username = req.headers['x-jwt-claim-username'];
const role = req.headers['x-jwt-claim-role'];
```

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
```

---

**返回**: [TDD 主目录](../README.md)

# API 模块目录

此目录用于存放各个业务模块的 API 接口封装。

## 目录结构

建议按照后端服务模块进行划分：

```
modules/
├── auth.js      # M1 认证服务 API
├── user.js      # M2 用户服务 API
├── file.js      # M7 文件服务 API
├── post.js      # M3 内容服务 API
├── comment.js   # M4 评论服务 API
├── section.js   # M5 板块服务 API
└── search.js    # M6 搜索服务 API
```

## 使用示例

```javascript
// auth.js 示例
import request from '../request'

export const authAPI = {
  // 登录
  login(data) {
    return request.post('/api/auth/login', data)
  },
  
  // 登出
  logout() {
    return request.post('/api/auth/logout')
  },
  
  // 验证 Token
  verify() {
    return request.get('/api/auth/verify')
  }
}
```

## 注意事项

1. 所有接口都应该通过 Kong 网关（VITE_API_BASE_URL）
2. 接口路径应与后端服务路由保持一致
3. 使用统一的 request 实例，自动处理 Token 和错误


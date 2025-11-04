# Open436-Frontend

Open436 论坛系统前端项目 - 基于 Vue3 + Vite 的现代化前端架构

## 技术栈

- **框架**: Vue 3.4+
- **构建工具**: Vite 6.0+
- **路由**: Vue Router 4.x
- **状态管理**: Pinia 2.x
- **HTTP 客户端**: Axios 1.x
- **代码规范**: ESLint 8.x + Prettier 3.x

## 项目结构

```
Open436-Frontend/
├── public/                # 静态资源
├── src/
│   ├── api/              # API 接口封装
│   │   ├── modules/      # 业务模块 API（待扩展）
│   │   └── request.js    # Axios 配置
│   ├── assets/           # 静态资源
│   │   └── styles/       # 全局样式
│   ├── components/       # 公共组件（待扩展）
│   ├── router/           # 路由配置
│   │   └── index.js
│   ├── stores/           # Pinia 状态管理
│   │   ├── modules/      # Store 模块（待扩展）
│   │   └── index.js
│   ├── utils/            # 工具函数
│   │   ├── storage.js    # localStorage 封装
│   │   └── constants.js  # 常量定义
│   ├── views/            # 页面组件
│   ├── App.vue           # 根组件
│   └── main.js           # 入口文件
├── .env.development      # 开发环境变量
├── .env.production       # 生产环境变量
├── .eslintrc.cjs         # ESLint 配置
├── .prettierrc           # Prettier 配置
├── .editorconfig         # 编辑器配置
├── .gitignore
├── vite.config.js        # Vite 配置
├── package.json
└── README.md
```

## 快速开始

### 环境要求

- Node.js 18.x 或更高版本
- npm 9.x 或更高版本

### 安装依赖

```bash
npm install
```

### 开发模式

```bash
npm run dev
```

访问 http://localhost:3000

### 构建生产版本

```bash
npm run build
```

### 预览生产构建

```bash
npm run preview
```

### 代码检查

```bash
# ESLint 检查
npm run lint

# 代码格式化
npm run format
```

## 环境配置

### 开发环境

修改 `.env.development` 文件：

```
VITE_API_BASE_URL=http://localhost:8000
VITE_APP_TITLE=Open436 论坛系统
VITE_ENV=development
```

### 生产环境

修改 `.env.production` 文件：

```
VITE_API_BASE_URL=https://api.open436.com
VITE_APP_TITLE=Open436 论坛系统
VITE_ENV=production
```

## 核心配置说明

### 路径别名

项目配置了 `@` 作为 `src/` 目录的别名：

```javascript
import request from '@/api/request'
import { storage } from '@/utils/storage'
```

### API 请求

所有 API 请求通过 Kong 网关（配置在环境变量中）：

```javascript
import request from '@/api/request'

// 示例：调用认证接口
request.post('/api/auth/login', {
  username: 'admin',
  password: 'password'
})
```

### 路由配置

路由配置在 `src/router/index.js`，支持：

- 懒加载
- 路由守卫（权限验证）
- 元信息配置

### 状态管理

使用 Pinia 进行状态管理，Store 模块化存放在 `src/stores/modules/`

### 本地存储

使用封装的 storage 工具，支持 JSON 序列化：

```javascript
import storage from '@/utils/storage'

// 存储
storage.set('token', 'xxx')

// 读取
const token = storage.get('token')

// 删除
storage.remove('token')
```

## 开发规范

### 代码风格

- 使用 ES6+ 语法
- 组件使用 Composition API（`<script setup>`）
- 遵循 ESLint 和 Prettier 规则

### 命名规范

- **组件**: PascalCase（如 `UserCard.vue`）
- **文件夹**: kebab-case（如 `user-management/`）
- **变量/函数**: camelCase（如 `getUserInfo`）
- **常量**: UPPER_CASE（如 `API_BASE_URL`）

### 提交规范

```
<type>(<scope>): <subject>

# 示例
feat(auth): 添加登录功能
fix(user): 修复用户信息显示错误
docs(readme): 更新文档
style(app): 调整页面布局
refactor(api): 重构 API 请求封装
```

**Type 类型**:
- `feat`: 新功能
- `fix`: Bug 修复
- `docs`: 文档更新
- `style`: 样式调整
- `refactor`: 代码重构
- `test`: 测试相关
- `chore`: 构建工具或辅助工具变动

## 与后端服务集成

### 服务路由映射

前端通过 Kong 网关访问后端微服务：

| 前端路由前缀 | 后端服务 | 说明 |
|------------|---------|------|
| `/api/auth` | M1 认证服务 | 用户认证、权限管理 |
| `/api/users` | M2 用户服务 | 用户资料、统计 |
| `/api/posts` | M3 内容服务 | 帖子管理 |
| `/api/comments` | M4 评论服务 | 评论、互动 |
| `/api/sections` | M5 板块服务 | 板块管理 |
| `/api/search` | M6 搜索服务 | 全文搜索 |
| `/api/files` | M7 文件服务 | 文件上传、存储 |

### 认证方式

使用 Sa-Token 进行身份认证：

- Token 存储在 localStorage
- 请求自动携带 `satoken` 请求头
- Token 过期自动跳转登录页

## 扩展开发

### 添加新的 API 模块

1. 在 `src/api/modules/` 创建新文件
2. 导出 API 方法
3. 在组件中使用

```javascript
// src/api/modules/user.js
import request from '../request'

export const userAPI = {
  getUserInfo(userId) {
    return request.get(`/api/users/${userId}`)
  }
}
```

### 添加新的 Store 模块

1. 在 `src/stores/modules/` 创建新文件
2. 使用 `defineStore` 定义 Store
3. 在组件中使用

```javascript
// src/stores/modules/user.js
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref(null)
  
  function setUserInfo(info) {
    userInfo.value = info
  }
  
  return { userInfo, setUserInfo }
})
```

### 添加新路由

在 `src/router/index.js` 的 routes 数组中添加：

```javascript
{
  path: '/new-page',
  name: 'NewPage',
  component: () => import('@/views/NewPage.vue'),
  meta: {
    title: '新页面',
    requiresAuth: true
  }
}
```

## 许可证

MIT License

## 相关链接

- [Vue 3 文档](https://cn.vuejs.org/)
- [Vite 文档](https://cn.vitejs.dev/)
- [Vue Router 文档](https://router.vuejs.org/zh/)
- [Pinia 文档](https://pinia.vuejs.org/zh/)
- [Axios 文档](https://axios-http.com/zh/)

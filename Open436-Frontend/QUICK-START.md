# 快速开始指南

## 前置要求

- Node.js 18.x 或更高版本
- npm 9.x 或更高版本
- 后端服务已启动（Kong 网关运行在 localhost:8000）

## 5 分钟快速启动

### 1. 安装依赖

```bash
cd Open436-Frontend
npm install
```

### 2. 启动开发服务器

```bash
npm run dev
```

访问：http://localhost:3000

### 3. 构建生产版本

```bash
npm run build
```

构建输出：`dist/` 目录

### 4. 预览生产构建

```bash
npm run preview
```

## NPM 脚本说明

| 命令 | 说明 |
|------|------|
| `npm run dev` | 启动开发服务器（端口 3000） |
| `npm run build` | 构建生产版本 |
| `npm run preview` | 预览生产构建 |
| `npm run lint` | ESLint 代码检查并自动修复 |
| `npm run format` | Prettier 格式化代码 |

## 目录说明

```
src/
├── api/              # API 接口封装（Axios）
├── assets/           # 静态资源（样式、图片）
├── components/       # 公共组件
├── router/           # 路由配置
├── stores/           # 状态管理（Pinia）
├── utils/            # 工具函数
├── views/            # 页面组件
├── App.vue           # 根组件
└── main.js           # 入口文件
```

## 环境配置

### 开发环境

编辑 `.env.development`：

```env
VITE_API_BASE_URL=http://localhost:8000
VITE_APP_TITLE=Open436 论坛系统
```

### 生产环境

编辑 `.env.production`：

```env
VITE_API_BASE_URL=https://api.open436.com
VITE_APP_TITLE=Open436 论坛系统
```

## 开发工作流

### 1. 创建新页面

```bash
# 1. 在 src/views/ 创建组件
# 2. 在 src/router/index.js 添加路由
```

示例：

```javascript
// src/views/NewPage.vue
<template>
  <div>新页面</div>
</template>

<script setup>
// 页面逻辑
</script>

// src/router/index.js
{
  path: '/new-page',
  name: 'NewPage',
  component: () => import('@/views/NewPage.vue')
}
```

### 2. 封装 API

```javascript
// src/api/modules/user.js
import request from '../request'

export const userAPI = {
  getUserInfo(id) {
    return request.get(`/api/users/${id}`)
  }
}
```

### 3. 创建 Store

```javascript
// src/stores/modules/user.js
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref(null)
  return { userInfo }
})
```

### 4. 使用 Store

```vue
<script setup>
import { useUserStore } from '@/stores/modules/user'

const userStore = useUserStore()
console.log(userStore.userInfo)
</script>
```

## 常用功能

### 路由跳转

```javascript
import { useRouter } from 'vue-router'

const router = useRouter()

// 跳转
router.push('/path')
router.push({ name: 'RouteName' })
router.push({ path: '/path', query: { id: 1 } })
```

### 发起请求

```javascript
import request from '@/api/request'

// GET 请求
const data = await request.get('/api/users')

// POST 请求
const result = await request.post('/api/auth/login', {
  username: 'admin',
  password: 'password'
})
```

### 本地存储

```javascript
import storage from '@/utils/storage'

// 存储
storage.set('key', { data: 'value' })

// 读取
const value = storage.get('key')

// 删除
storage.remove('key')
```

## 代码规范

### 提交代码前

```bash
# 1. 代码检查
npm run lint

# 2. 代码格式化
npm run format

# 3. 测试构建
npm run build
```

### 提交信息规范

```
feat: 添加用户登录功能
fix: 修复路由跳转错误
docs: 更新 README
style: 调整按钮样式
refactor: 重构 API 请求模块
```

## 故障排查

### 依赖安装失败

```bash
# 清除缓存
npm cache clean --force

# 删除 node_modules
rm -rf node_modules

# 重新安装
npm install
```

### 开发服务器启动失败

1. 检查端口 3000 是否被占用
2. 检查 Node.js 版本（需要 18+）
3. 重新安装依赖

### 构建失败

1. 运行 `npm run lint` 检查语法错误
2. 检查是否有未安装的依赖
3. 查看终端错误信息

## 进阶配置

### 修改开发端口

编辑 `vite.config.js`：

```javascript
server: {
  port: 3001  // 修改为其他端口
}
```

### 添加代理

编辑 `vite.config.js`：

```javascript
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8000',
      changeOrigin: true
    }
  }
}
```

### 添加路径别名

编辑 `vite.config.js`：

```javascript
resolve: {
  alias: {
    '@': fileURLToPath(new URL('./src', import.meta.url)),
    '@components': fileURLToPath(new URL('./src/components', import.meta.url))
  }
}
```

## 部署

### 静态部署（Nginx）

```bash
# 1. 构建
npm run build

# 2. 上传 dist/ 目录到服务器
# 3. 配置 Nginx
```

Nginx 配置示例：

```nginx
server {
  listen 80;
  server_name your-domain.com;
  root /path/to/dist;
  index index.html;

  location / {
    try_files $uri $uri/ /index.html;
  }
}
```

### Docker 部署

```dockerfile
# Dockerfile
FROM node:18 as builder
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
EXPOSE 80
```

构建和运行：

```bash
docker build -t open436-frontend .
docker run -p 80:80 open436-frontend
```

## 相关文档

- [README.md](./README.md) - 项目说明
- [ARCHITECTURE.md](./ARCHITECTURE.md) - 架构说明
- [Vue 3 官方文档](https://cn.vuejs.org/)
- [Vite 官方文档](https://cn.vitejs.dev/)

## 获取帮助

- 查看项目文档
- 提交 Issue
- 联系开发团队

---

祝开发顺利！🚀


# 前端架构说明

## 项目概述

Open436-Frontend 是基于 Vue3 + Vite 构建的现代化前端基础架构，采用模块化设计，为后续业务开发提供坚实的技术基础。

## 技术选型

### 核心技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.5+ | 渐进式 JavaScript 框架 |
| Vite | 7.1+ | 新一代前端构建工具 |
| Vue Router | 4.6+ | Vue 官方路由管理器 |
| Pinia | 2.3+ | Vue 官方状态管理库 |
| Axios | 1.13+ | 基于 Promise 的 HTTP 客户端 |
| ESLint | 8.x | JavaScript 代码检查工具 |
| Prettier | 3.x | 代码格式化工具 |

### 技术选型理由

1. **Vue 3**: 
   - Composition API 提供更好的逻辑复用
   - 性能优化，更小的包体积
   - TypeScript 支持更好

2. **Vite**: 
   - 极速的冷启动
   - 即时的模块热更新
   - 真正的按需编译

3. **Pinia**: 
   - Vue 3 官方推荐
   - 更好的 TypeScript 支持
   - 更简洁的 API

4. **Axios**: 
   - 成熟稳定
   - 支持请求/响应拦截
   - 浏览器和 Node.js 通用

## 目录结构设计

```
Open436-Frontend/
├── public/                   # 静态资源（不经过 Vite 处理）
├── src/
│   ├── api/                 # API 接口层
│   │   ├── modules/         # 按业务模块划分的 API（待扩展）
│   │   └── request.js       # Axios 实例配置
│   │
│   ├── assets/              # 资源文件
│   │   └── styles/          # 全局样式
│   │       └── main.css     # 主样式文件
│   │
│   ├── components/          # 公共组件（待扩展）
│   │
│   ├── router/              # 路由配置
│   │   └── index.js         # 路由定义、守卫
│   │
│   ├── stores/              # 状态管理
│   │   ├── modules/         # 按业务模块划分的 Store（待扩展）
│   │   └── index.js         # Pinia 实例
│   │
│   ├── utils/               # 工具函数
│   │   ├── storage.js       # 本地存储封装
│   │   └── constants.js     # 全局常量
│   │
│   ├── views/               # 页面组件（待扩展）
│   │   └── Home.vue         # 示例首页
│   │
│   ├── App.vue              # 根组件
│   └── main.js              # 应用入口
│
├── .env.development         # 开发环境变量
├── .env.production          # 生产环境变量
├── .eslintrc.cjs            # ESLint 配置
├── .prettierrc              # Prettier 配置
├── .editorconfig            # 编辑器统一配置
├── .gitignore               # Git 忽略配置
├── vite.config.js           # Vite 配置
├── package.json             # 项目依赖
├── README.md                # 项目文档
└── ARCHITECTURE.md          # 本文件
```

## 核心模块说明

### 1. API 接口层 (`src/api/`)

**职责**: 封装所有后端 API 调用，统一管理请求配置

**核心文件**:
- `request.js`: Axios 实例配置
  - 请求拦截器：添加 Token、设置请求头
  - 响应拦截器：统一错误处理、数据转换

**设计要点**:
- 所有请求通过 Kong 网关（环境变量配置）
- 预留 Token 自动添加机制
- 预留错误统一处理逻辑
- 支持按业务模块划分 API（`modules/` 目录）

### 2. 路由层 (`src/router/`)

**职责**: 管理应用路由，控制页面跳转和权限

**核心功能**:
- History 模式路由
- 路由懒加载
- 全局前置守卫（预留权限验证）
- 全局后置钩子（预留埋点统计）
- 页面标题自动设置

**路由配置示例**:
```javascript
{
  path: '/page',
  name: 'Page',
  component: () => import('@/views/Page.vue'),
  meta: {
    title: '页面标题',
    requiresAuth: true  // 是否需要登录
  }
}
```

### 3. 状态管理层 (`src/stores/`)

**职责**: 管理应用全局状态

**设计方式**:
- 使用 Pinia 进行状态管理
- 采用 Composition API 风格（Setup 语法）
- 支持模块化 Store（`modules/` 目录）
- 可扩展持久化插件

**Store 模块示例**:
```javascript
export const useAuthStore = defineStore('auth', () => {
  const token = ref('')
  
  function setToken(newToken) {
    token.value = newToken
  }
  
  return { token, setToken }
})
```

### 4. 工具函数层 (`src/utils/`)

**职责**: 提供通用工具函数

**核心模块**:
- `storage.js`: localStorage 封装
  - 支持 JSON 序列化/反序列化
  - 统一的 key 前缀（`open436_`）
  - 类型安全的读写操作

- `constants.js`: 全局常量定义
  - 存储键名
  - API 路由前缀
  - HTTP 状态码
  - 业务状态码

### 5. 样式层 (`src/assets/styles/`)

**职责**: 管理全局样式

**核心内容**:
- CSS Reset
- 全局基础样式
- 工具类（flex、text-align 等）
- 响应式断点定义

## 配置文件说明

### Vite 配置 (`vite.config.js`)

```javascript
{
  resolve: {
    alias: { '@': 'src/' }  // 路径别名
  },
  server: {
    port: 3000,              // 开发服务器端口
    proxy: { ... }           // 代理配置
  },
  build: {
    rollupOptions: { ... }   // 分包策略
  }
}
```

### ESLint 配置 (`.eslintrc.cjs`)

- Vue 3 官方推荐规则
- JavaScript ES6+ 规则
- 集成 Prettier

### Prettier 配置 (`.prettierrc`)

- 单引号
- 无分号
- 2 空格缩进
- 行宽 100

## 环境变量管理

### 变量命名规范

所有环境变量必须以 `VITE_` 开头才能被 Vite 注入。

### 当前定义的变量

| 变量名 | 说明 | 开发环境 | 生产环境 |
|--------|------|---------|---------|
| `VITE_API_BASE_URL` | API 网关地址 | `http://localhost:8000` | `https://api.open436.com` |
| `VITE_APP_TITLE` | 应用标题 | `Open436 论坛系统` | `Open436 论坛系统` |
| `VITE_ENV` | 环境标识 | `development` | `production` |

### 使用方式

```javascript
const apiUrl = import.meta.env.VITE_API_BASE_URL
```

## 代码规范

### 文件命名

- **组件文件**: PascalCase（`UserCard.vue`）
- **工具文件**: camelCase（`storage.js`）
- **目录名**: kebab-case（`user-management/`）

### 组件开发规范

1. 使用 `<script setup>` 语法
2. Props 必须定义类型和默认值
3. Emits 必须明确声明
4. 样式使用 `scoped`

### 提交规范

遵循 Conventional Commits 规范：

```
<type>(<scope>): <subject>

feat: 新功能
fix: Bug 修复
docs: 文档更新
style: 样式调整
refactor: 重构
test: 测试
chore: 构建/工具变动
```

## 与后端集成

### 服务路由映射

前端通过 Kong 网关（`VITE_API_BASE_URL`）访问后端微服务：

| 路由前缀 | 服务 | 说明 |
|---------|------|------|
| `/api/auth` | M1 认证服务 | 登录、Token 验证 |
| `/api/users` | M2 用户服务 | 用户资料管理 |
| `/api/posts` | M3 内容服务 | 帖子管理 |
| `/api/comments` | M4 评论服务 | 评论、互动 |
| `/api/sections` | M5 板块服务 | 板块管理 |
| `/api/search` | M6 搜索服务 | 全文搜索 |
| `/api/files` | M7 文件服务 | 文件上传 |

### 认证流程

1. 用户登录 → 获取 Token
2. Token 存储到 localStorage
3. 后续请求自动携带 `satoken` 请求头
4. Token 过期 → 401 响应 → 跳转登录页

## 开发流程

### 添加新功能的步骤

1. **定义路由** (`src/router/index.js`)
2. **创建页面组件** (`src/views/`)
3. **封装 API** (`src/api/modules/`)
4. **创建 Store**（如需要）(`src/stores/modules/`)
5. **开发页面逻辑**
6. **编写样式**
7. **测试功能**

### 最佳实践

1. **组件拆分**: 单一职责，可复用性优先
2. **API 封装**: 统一管理，便于维护
3. **状态管理**: 仅全局状态使用 Pinia，局部状态用 ref/reactive
4. **错误处理**: 统一在拦截器处理，特殊情况单独处理
5. **性能优化**: 路由懒加载、图片懒加载、虚拟滚动

## 扩展性设计

### 模块化设计

所有核心模块（API、Store、Router）均支持模块化扩展：

```
api/modules/        → 按业务模块划分 API
stores/modules/     → 按业务模块划分 Store
components/         → 按功能类型划分组件
```

### 插件机制

支持添加 Vue 插件和 Pinia 插件：

```javascript
// main.js
app.use(somePlugin)

// stores/index.js
pinia.use(somePlugin)
```

### 主题定制

通过 CSS 变量支持主题定制（可扩展）。

## 性能优化

### 已实施

1. **路由懒加载**: 所有路由组件按需加载
2. **Vite 分包**: 将 Vue、Router、Pinia 打包为独立 chunk
3. **资源优化**: 使用 Vite 自动优化

### 可扩展

1. 图片懒加载
2. 虚拟滚动列表
3. 组件懒加载
4. CDN 加速
5. Gzip 压缩

## 安全考虑

1. **XSS 防护**: Vue 自动转义
2. **CSRF**: 使用 Token 机制
3. **环境变量**: 敏感信息不提交到仓库（`.env.local`）
4. **依赖安全**: 定期更新依赖，使用 `npm audit`

## 部署方案

### 开发环境

```bash
npm run dev  # 启动开发服务器（端口 3000）
```

### 生产构建

```bash
npm run build  # 构建生产版本到 dist/
```

### 预览生产构建

```bash
npm run preview  # 预览构建结果
```

### 部署建议

1. **静态托管**: Nginx、Apache
2. **CDN**: 阿里云 OSS、腾讯云 COS
3. **容器化**: Docker
4. **CI/CD**: GitHub Actions、GitLab CI

## 后续扩展方向

### 功能扩展

- [ ] 国际化（i18n）
- [ ] 主题切换（暗黑模式）
- [ ] PWA 支持
- [ ] SSR/SSG（Nuxt 3）

### 工具扩展

- [ ] TypeScript 迁移
- [ ] 单元测试（Vitest）
- [ ] E2E 测试（Cypress）
- [ ] 组件文档（Storybook）

### 性能扩展

- [ ] 虚拟滚动
- [ ] 图片懒加载
- [ ] 服务端渲染
- [ ] 预渲染

## 常见问题

### 1. 路径别名 `@` 无法识别？

确保 `vite.config.js` 配置了路径别名，并重启开发服务器。

### 2. 环境变量不生效？

确保变量名以 `VITE_` 开头，修改后需重启开发服务器。

### 3. ESLint 报错？

运行 `npm run lint` 自动修复，或手动修改代码。

### 4. 构建失败？

检查是否有语法错误，确保所有依赖已安装。

## 维护计划

- **依赖更新**: 每月检查并更新依赖
- **代码审查**: 提交前进行 Code Review
- **文档更新**: 功能变更时同步更新文档
- **性能监控**: 定期检查构建体积和性能指标

---

**更新时间**: 2024-11-04  
**架构版本**: v1.0.0  
**维护团队**: Open436 开发团队


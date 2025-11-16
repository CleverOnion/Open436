# Mock 数据使用指南

## 📋 概述

Mock 数据模式允许前端在没有后端服务的情况下独立开发和测试，提高开发效率。

---

## 🚀 快速开始

### 方法一：环境变量切换（推荐）✅

**1. 修改 `.env.development` 文件**：

```bash
# 启用 Mock 模式
VITE_USE_MOCK=true

# 禁用 Mock 模式（使用真实API）
VITE_USE_MOCK=false
```

**2. 重启开发服务器**：

```bash
npm run dev
```

**3. 查看控制台**：

```
🎭 [M5] 使用 Mock 数据模式
```

---

### 方法二：直接导入 Mock API

在需要使用 Mock 的组件中：

```javascript
// 使用 Mock API
import sectionApi from '@/api/modules/section.mock'

// 使用真实 API
import sectionApi from '@/api/modules/section'
```

---

## 📦 Mock 数据说明

### 预设板块数据

Mock 服务预设了 7 个板块：

| ID | Slug | 名称 | 图标 | 状态 | 帖子数 |
|----|------|------|------|------|--------|
| 1 | tech | 技术交流 | 💻 | 启用 | 156 |
| 2 | design | 设计分享 | 🎨 | 启用 | 89 |
| 3 | discuss | 综合讨论 | 💬 | 启用 | 234 |
| 4 | question | 问答求助 | ❓ | 启用 | 178 |
| 5 | share | 资源分享 | 📦 | 启用 | 92 |
| 6 | announce | 公告通知 | 📢 | 启用 | 23 |
| 7 | test-disabled | 测试禁用板块 | 🚫 | 禁用 | 0 |

---

## 🎯 支持的功能

### ✅ 完整支持的 API

- `getList()` - 获取板块列表（支持分页、筛选、排序）
- `getAllEnabled()` - 获取所有启用板块
- `getDetail(idOrSlug)` - 获取板块详情（支持ID和slug）
- `create(data)` - 创建板块
- `update(id, data)` - 更新板块
- `delete(id, permanent)` - 删除板块（软删除/硬删除）
- `toggleStatus(id, isEnabled)` - 切换启用状态
- `reorder(orderData)` - 批量调整排序
- `getStatistics()` - 获取统计数据
- `validate(id)` - 验证板块
- `incrementPosts(id, increment)` - 增加帖子数

### ✅ 支持的特性

- **数据持久化**：在当前会话中，数据修改会保留（刷新页面后重置）
- **模拟延迟**：每个请求延迟 300ms，模拟真实网络环境
- **数据验证**：slug 和 name 唯一性验证
- **错误处理**：模拟常见错误场景
- **控制台日志**：每个 API 调用都会输出日志

---

## 🔧 使用示例

### 示例 1：获取板块列表

```javascript
import sectionApi from '@/api/modules/section.mock'

// 获取所有启用的板块
const response = await sectionApi.getList({ 
  is_enabled: true,
  page: 1,
  page_size: 10
})

console.log(response.results) // 板块数组
```

### 示例 2：创建板块

```javascript
const newSection = await sectionApi.create({
  slug: 'frontend',
  name: '前端开发',
  description: 'HTML、CSS、JavaScript',
  icon: '⚡',
  color: '#42A5F5',
  sort_order: 7,
  is_enabled: true
})

console.log('创建成功:', newSection)
```

### 示例 3：更新板块

```javascript
const updated = await sectionApi.update(1, {
  name: '技术交流（新）',
  description: '更新后的描述'
})

console.log('更新成功:', updated)
```

### 示例 4：删除板块

```javascript
// 软删除（禁用）
await sectionApi.delete(7, false)

// 硬删除（永久删除）
await sectionApi.delete(7, true)
```

---

## 🧪 测试场景

### 测试场景 1：板块列表展示

```javascript
// 1. 访问板块列表页 /sections
// 2. 应该看到 6 个启用的板块
// 3. 不应该看到禁用的板块
```

### 测试场景 2：创建板块

```javascript
// 1. 访问管理后台 /admin/sections
// 2. 点击"添加板块"
// 3. 填写表单并提交
// 4. 应该在列表中看到新板块
```

### 测试场景 3：编辑板块

```javascript
// 1. 点击某个板块的"编辑"按钮
// 2. 修改名称和描述
// 3. 保存
// 4. 应该看到更新后的信息
```

### 测试场景 4：启用/禁用

```javascript
// 1. 点击"禁用"按钮
// 2. 板块应该变灰
// 3. 前台列表不再显示该板块
// 4. 点击"启用"恢复
```

### 测试场景 5：删除板块

```javascript
// 1. 点击"删除"按钮
// 2. 确认删除
// 3. 板块从列表中消失
```

---

## 🔍 调试技巧

### 查看 Mock 数据

在浏览器控制台中：

```javascript
// 查看当前所有板块
import { sectionMockApi } from '@/api/mock/sectionMock'
const data = await sectionMockApi.getList({ page_size: 100 })
console.table(data.results)
```

### 重置 Mock 数据

```javascript
import { resetMockData } from '@/api/mock/sectionMock'
resetMockData()
console.log('Mock 数据已重置')
```

### 查看 API 调用日志

所有 Mock API 调用都会在控制台输出：

```
[Mock API] 获取板块列表 {page: 1, page_size: 20}
[Mock API] 创建板块 {slug: 'test', name: '测试板块', ...}
[Mock API] 更新板块 1 {name: '新名称'}
```

---

## ⚠️ 注意事项

### 数据不持久化

Mock 数据存储在内存中，刷新页面后会重置为初始状态。如果需要持久化，可以：

1. 使用 `localStorage` 存储
2. 使用 IndexedDB
3. 使用真实后端服务

### 模拟延迟

默认每个请求延迟 300ms，可以在 `sectionMock.js` 中修改：

```javascript
const delay = (ms = 300) => new Promise(resolve => setTimeout(resolve, ms))

// 修改为 500ms
const delay = (ms = 500) => new Promise(resolve => setTimeout(resolve, ms))

// 取消延迟
const delay = (ms = 0) => new Promise(resolve => setTimeout(resolve, ms))
```

### 错误处理

Mock API 会模拟一些常见错误：

- 板块不存在
- slug 重复
- name 重复

---

## 🔄 切换到真实 API

当后端服务准备好后，切换到真实 API：

**1. 修改环境变量**：

```bash
# .env.development
VITE_USE_MOCK=false
```

**2. 重启开发服务器**：

```bash
npm run dev
```

**3. 查看控制台**：

```
🌐 [M5] 使用真实 API 模式
```

---

## 📝 自定义 Mock 数据

### 添加更多板块

编辑 `sectionMock.js`：

```javascript
let mockSections = [
  // 现有板块...
  {
    id: 8,
    slug: 'custom',
    name: '自定义板块',
    description: '这是一个自定义板块',
    icon: '🎯',
    color: '#FF5722',
    sort_order: 8,
    is_enabled: true,
    posts_count: 0,
    created_at: new Date().toISOString(),
    updated_at: new Date().toISOString()
  }
]
```

### 修改模拟延迟

```javascript
// 快速响应（无延迟）
const delay = (ms = 0) => new Promise(resolve => setTimeout(resolve, ms))

// 慢速响应（模拟慢网络）
const delay = (ms = 2000) => new Promise(resolve => setTimeout(resolve, ms))
```

---

## 🎉 优势

### 1. 独立开发
- ✅ 不依赖后端服务
- ✅ 前后端并行开发
- ✅ 提高开发效率

### 2. 快速测试
- ✅ 即时反馈
- ✅ 无需等待后端部署
- ✅ 可控的测试数据

### 3. 演示友好
- ✅ 无需配置后端
- ✅ 数据稳定可预测
- ✅ 适合产品演示

### 4. 离线开发
- ✅ 无需网络连接
- ✅ 随时随地开发
- ✅ 不受后端服务影响

---

## 📞 技术支持

如有问题，请查阅：
- [Mock 数据源码](./sectionMock.js)
- [Mock API 封装](../modules/section.mock.js)
- [前端架构文档](../../../ARCHITECTURE.md)

---

**文档创建时间**：2025-11-16  
**维护者**：Open436 前端团队  
**状态**：✅ 可用

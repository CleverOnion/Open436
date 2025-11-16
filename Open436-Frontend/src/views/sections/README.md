# M5 板块管理模块 - 前端组件

## 📋 模块概述

M5 板块管理模块负责论坛板块的展示和管理功能，包含用户浏览板块、管理员管理板块等核心功能。

**完成时间**: 2025-11-11  
**技术栈**: Vue 3 + Composition API + Pinia  
**后端对接**: Open436-SectionService (Django + DRF)

---

## 📁 文件结构

```
src/
├── views/sections/           # 板块页面组件
│   ├── SectionList.vue       # 板块列表页（用户浏览）
│   ├── SectionDetail.vue     # 板块详情页（显示板块信息和帖子）
│   ├── SectionManage.vue     # 板块管理页（管理员后台）
│   └── README.md             # 本文件
├── components/
│   └── SectionSelector.vue   # 板块选择器组件（用于发帖时选择板块）
├── api/modules/
│   └── section.js            # 板块 API 封装
├── stores/modules/
│   └── section.js            # 板块状态管理（Pinia Store）
└── router/
    └── index.js              # 路由配置（已添加M5路由） 
```

---

## 🎯 功能清单

### ✅ 已完成功能

#### 1. 用户端功能
- **板块列表展示** (`/sections`)
  - 显示所有启用的板块
  - 板块图标、名称、描述、帖子数统计
  - 点击进入板块详情

- **板块详情页** (`/sections/:idOrSlug`)
  - 显示板块完整信息
  - 支持通过 slug 或 ID 访问
  - 面包屑导航
  - 发帖入口（待M3模块对接）

#### 2. 管理员功能
- **板块管理后台** (`/admin/sections`)
  - 查看所有板块（包括已禁用）
  - 创建新板块
  - 编辑板块信息（名称、描述、图标、颜色、排序）
  - 启用/禁用板块
  - 删除板块（软删除）
  - 实时统计数据展示

#### 3. 公共组件
- **板块选择器** (`SectionSelector.vue`)
  - 下拉选择板块
  - 支持 v-model 双向绑定
  - 显示板块图标和描述
  - 用于发帖时选择板块

---

## 🚀 使用指南

### 1. 板块列表页 (SectionList.vue)

**路由**: `/sections`  
**权限**: 公开访问

```vue
<!-- 直接通过路由访问 -->
<router-link to="/sections">论坛板块</router-link>
```

**功能**:
- 显示所有启用的板块
- 响应式卡片布局
- 点击卡片进入板块详情

---

### 2. 板块详情页 (SectionDetail.vue)

**路由**: `/sections/:idOrSlug`  
**权限**: 公开访问

```vue
<!-- 通过 slug 访问 -->
<router-link to="/sections/tech">技术交流板块</router-link>

<!-- 通过 ID 访问 -->
<router-link to="/sections/1">板块详情</router-link>
```

**功能**:
- 显示板块详细信息
- 面包屑导航
- 发帖按钮（待M3对接）
- 帖子列表（待M3对接）

---

### 3. 板块管理页 (SectionManage.vue)

**路由**: `/admin/sections`  
**权限**: 需要管理员权限

```vue
<!-- 管理员访问 -->
<router-link to="/admin/sections">板块管理</router-link>
```

**功能**:
- 查看所有板块（包括禁用的）
- 创建板块对话框
  - 板块标识（slug）- 唯一，创建后不可修改
  - 板块名称
  - 板块描述
  - 板块图标（预设emoji选择）
  - 板块颜色（颜色选择器）
  - 排序号
  - 启用状态
- 编辑板块
- 启用/禁用板块
- 删除板块（软删除）

---

### 4. 板块选择器组件 (SectionSelector.vue)

**使用场景**: 发帖时选择板块

```vue
<script setup>
import { ref } from 'vue'
import SectionSelector from '@/components/SectionSelector.vue'

const selectedSectionId = ref(null)
</script>

<template>
  <div class="post-form">
    <label>选择板块</label>
    <SectionSelector
      v-model="selectedSectionId"
      placeholder="请选择发帖板块"
    />
  </div>
</template>
```

**Props**:
- `modelValue` (Number): 选中的板块ID
- `placeholder` (String): 占位文字，默认"请选择板块"

**Events**:
- `update:modelValue`: 选择板块时触发，传递板块ID

**功能**:
- 自动加载启用的板块列表
- 下拉菜单选择
- 显示板块图标、名称、描述
- 支持 v-model 双向绑定

---

## 📡 API 接口对接

所有API接口通过 `@/api/modules/section.js` 封装，已实现完整对接。

### 主要接口

```javascript
import sectionApi from '@/api/modules/section'

// 获取板块列表（公开）
await sectionApi.getList({ page: 1, page_size: 20, enabled_only: true })

// 获取所有启用板块（用于选择器）
await sectionApi.getAllEnabled()

// 获取板块详情（公开）
await sectionApi.getDetail('tech') // 通过slug
await sectionApi.getDetail(1)      // 通过ID

// 创建板块（管理员）
await sectionApi.create({
  slug: 'tech',
  name: '技术交流',
  description: '分享技术经验',
  color: '#1976D2',
  icon: '💻',
  sort_order: 1,
  is_enabled: true
})

// 更新板块（管理员）
await sectionApi.update(1, { name: '新名称' })

// 删除板块（管理员）
await sectionApi.delete(1, false) // false表示软删除

// 切换启用状态（管理员）
await sectionApi.toggleStatus(1, false)

// 批量调整排序（管理员）
await sectionApi.reorder([
  { id: 1, sort_order: 1 },
  { id: 2, sort_order: 2 }
])
```

---

## 💾 状态管理 (Pinia Store)

使用 `@/stores/modules/section.js` 管理板块状态。

### 使用示例

```vue
<script setup>
import { computed } from 'vue'
import { useSectionStore } from '@/stores/modules/section'

const sectionStore = useSectionStore()

// 获取数据
const sections = computed(() => sectionStore.sections)
const sortedSections = computed(() => sectionStore.sortedSections)
const loading = computed(() => sectionStore.loading)

// 调用方法
await sectionStore.fetchSections()           // 获取板块列表
await sectionStore.fetchEnabledSections()    // 获取启用板块
await sectionStore.fetchSectionDetail('tech') // 获取详情
await sectionStore.createSection(data)       // 创建板块
await sectionStore.updateSection(id, data)   // 更新板块
await sectionStore.deleteSection(id)         // 删除板块
</script>
```

### Store 状态

```javascript
{
  sections: [],           // 板块列表
  enabledSections: [],    // 启用的板块列表
  currentSection: null,   // 当前板块详情
  loading: false,         // 加载状态
  pagination: {...},      // 分页信息
  filters: {...},         // 筛选条件
  statistics: null        // 统计数据
}
```

### Store Getters

```javascript
sectionStore.sortedSections        // 排序后的板块列表
sectionStore.getSectionById(id)    // 根据ID获取板块
sectionStore.getSectionBySlug(slug) // 根据slug获取板块
sectionStore.hasMore                // 是否有更多数据
```

---

## 🎨 样式设计

所有组件采用统一的设计风格，参考 Open436 UI 规范：

### 颜色变量
```css
--primary: #1976d2        /* 主色 */
--danger: #f44336         /* 危险色 */
--success: #4caf50        /* 成功色 */
--text-primary: #1f2937   /* 主文本色 */
--text-secondary: #6b7280 /* 次要文本色 */
--border: #e5e7eb         /* 边框色 */
--background: #f9fafb     /* 背景色 */
```

### 响应式设计
- 桌面端：栅格布局，多列展示
- 移动端：单列布局，优化触摸操作

---

## 🔗 路由配置

已在 `@/router/index.js` 中添加以下路由：

```javascript
// 板块列表（公开）
{ path: '/sections', name: 'SectionList', component: SectionList }

// 板块详情（公开）
{ path: '/sections/:idOrSlug', name: 'SectionDetail', component: SectionDetail }

// 板块管理（管理员）
{ 
  path: '/admin/sections', 
  name: 'SectionManage', 
  component: SectionManage,
  meta: { requiresAuth: true, requiresAdmin: true }
}
```

---

## ✅ 测试清单

### 功能测试
- [ ] 板块列表正常显示
- [ ] 板块详情页正常访问（通过ID和slug）
- [ ] 管理员可以创建板块
- [ ] 管理员可以编辑板块
- [ ] 管理员可以启用/禁用板块
- [ ] 管理员可以删除板块
- [ ] 板块选择器正常工作
- [ ] 响应式布局在移动端正常

### API 对接测试
- [ ] GET /api/sections - 获取板块列表
- [ ] GET /api/sections/:id - 获取板块详情
- [ ] POST /api/sections - 创建板块
- [ ] PUT /api/sections/:id - 更新板块
- [ ] DELETE /api/sections/:id - 删除板块
- [ ] PUT /api/sections/:id/status - 切换状态

---

## 🔧 待开发功能

1. **权限控制**
   - 实现路由守卫，验证管理员权限
   - 集成M1认证服务

2. **与M3对接**
   - 板块详情页显示帖子列表
   - 发帖时预填板块ID

3. **增强功能**
   - 板块搜索和筛选
   - 板块拖拽排序
   - 板块图标上传（集成M7）
   - 板块订阅功能

4. **性能优化**
   - 板块列表虚拟滚动
   - 图片懒加载

---

## ⚠️ 重要说明

### 图标字段说明

**当前实现（临时方案）**：
- 前端使用 `icon` 字段存储 emoji 字符串
- 预设12个常用 emoji 供选择
- 适合快速开发和演示

**未来规划（正式方案）**：
- 集成 M7 文件服务
- 支持图片上传
- 使用 `icon_file_id` 字段（UUID）
- 后端返回 `icon_url` 供前端显示

**字段对应关系**：
```javascript
// 当前临时方案
{
  icon: '💻'  // emoji字符串
}

// 未来正式方案
{
  icon_file_id: 'uuid',           // 上传后获得
  icon_url: 'http://...'          // 后端返回
}
```

**迁移建议**：
1. 后端需要同时支持 `icon` 和 `icon_file_id` 字段
2. 优先使用 `icon_file_id`，如果为空则使用 `icon`
3. 前端显示优先使用 `icon_url`，如果为空则显示 `icon`

---

## 📝 开发规范

### 命名规范
- 组件名：PascalCase (`SectionList.vue`)
- 方法名：camelCase (`loadSections`)
- CSS类名：kebab-case (`section-card`)

### 代码规范
- 使用 Composition API (`<script setup>`)
- 使用 ESLint + Prettier
- 注释清晰，说明功能和参数

### 提交规范
```bash
feat(m5-frontend): 实现板块管理页面
feat(m5-frontend): 实现板块选择器组件
fix(m5-frontend): 修复板块详情页加载问题
```

---

## 📞 技术支持

如有问题，请联系前端开发团队或查阅以下文档：
- [M5 API 接口文档](../../docs/TDD/M5-板块管理服务/02-API接口设计.md)
- [前端架构文档](../../ARCHITECTURE.md)
- [项目快速开始](../../QUICK-START.md)

---

**文档更新时间**: 2025-11-11  
**维护者**: Open436 前端团队


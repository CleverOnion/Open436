# 组件目录

此目录用于存放可复用的公共组件。

## 目录结构

建议按照功能类型进行分类：

```
components/
├── common/         # 基础组件
│   ├── Button.vue
│   ├── Input.vue
│   ├── Loading.vue
│   └── Empty.vue
├── layout/         # 布局组件
│   ├── Header.vue
│   ├── Sidebar.vue
│   └── Footer.vue
├── business/       # 业务组件
│   ├── PostCard.vue
│   ├── CommentList.vue
│   └── UserAvatar.vue
└── ...
```

## 组件规范

### 命名规范
- 组件文件名使用 PascalCase（大驼峰）
- 组件名称应该清晰表达其用途
- 基础组件以 Base 开头（如 BaseButton）

### 组件结构
```vue
<template>
  <!-- 模板内容 -->
</template>

<script setup>
// 组件逻辑
</script>

<style scoped>
/* 组件样式 */
</style>
```

### Props 规范
- 必须定义 props 类型
- 提供合理的默认值
- 添加验证规则

```javascript
const props = defineProps({
  title: {
    type: String,
    required: true
  },
  size: {
    type: String,
    default: 'medium',
    validator: (value) => ['small', 'medium', 'large'].includes(value)
  }
})
```

### Emits 规范
- 明确声明组件触发的事件
- 使用 kebab-case 命名事件

```javascript
const emit = defineEmits(['update:modelValue', 'change', 'close'])
```

## 注意事项

1. 组件应该保持单一职责
2. 避免过度抽象，保持组件的可理解性
3. 使用 scoped 样式避免样式污染
4. 提供必要的文档注释


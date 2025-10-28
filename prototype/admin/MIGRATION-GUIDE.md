# 管理后台页面迁移指南

## 🎯 目标
将所有管理后台页面改为使用共用的侧边栏组件，减少代码重复。

## 📋 需要修改的文件
1. dashboard.html
2. sections.html  
3. posts.html
4. comments.html
5. reports.html
6. users.html

## 🔧 修改步骤

### 步骤 1: 删除侧边栏 HTML

**查找并删除：**
```html
<!-- Sidebar -->
<aside class="sidebar">
  ...整个侧边栏内容...
</aside>
```

**替换为：**
```html
<!-- Sidebar Container -->
<div id="admin-sidebar-container"></div>
```

### 步骤 2: 删除侧边栏 CSS

**在 `<style>` 标签中删除以下样式：**
- `.sidebar { ... }`
- `.sidebar-header { ... }`
- `.sidebar-brand { ... }`
- `.sidebar-logo { ... }`
- `.sidebar-menu { ... }`
- `.menu-section { ... }`
- `.menu-section-title { ... }`
- `.menu-item { ... }`
- `.menu-item:hover { ... }`
- `.menu-item.active { ... }`
- `.menu-item svg { ... }`
- `.sidebar-footer { ... }`

**保留以下响应式样式（如果有）：**
```css
@media (max-width: 959px) {
  .sidebar {
    transform: translateX(-100%);
    transition: transform 0.3s;
    z-index: 1000;
  }

  .sidebar.active {
    transform: translateX(0);
  }
}
```
改为：
```css
@media (max-width: 959px) {
  .main-content {
    margin-left: 0;
  }
  /* 或 .main-container { margin-left: 0; } */
}
```

### 步骤 3: 引入组件脚本

**在 `</body>` 标签前，`<script src="../js/common.js"></script>` 之后添加：**
```html
<script src="components/sidebar.js"></script>
```

### 步骤 4: 初始化侧边栏

**在页面的 JavaScript 代码开始处添加：**
```javascript
// 初始化侧边栏
initAdminSidebar('页面标识');
```

**页面标识对应表：**
- dashboard.html → `'dashboard'`
- sections.html → `'sections'`
- posts.html → `'posts'`
- comments.html → `'comments'`
- reports.html → `'reports'`
- users.html → `'users'`

## 📝 完整示例

### 修改前（dashboard.html）:
```html
<!DOCTYPE html>
<html>
<head>
  <style>
    body { display: flex; min-height: 100vh; }
    .sidebar { width: 260px; ... }
    .sidebar-header { ... }
    /* 更多侧边栏样式 */
    .main-container { flex: 1; margin-left: 260px; }
  </style>
</head>
<body>
  <aside class="sidebar">
    <!-- 侧边栏内容 -->
  </aside>
  
  <div class="main-container">
    <!-- 主内容 -->
  </div>
  
  <script src="../js/common.js"></script>
  <script>
    // 页面逻辑
  </script>
</body>
</html>
```

### 修改后（dashboard.html）:
```html
<!DOCTYPE html>
<html>
<head>
  <style>
    body { display: flex; min-height: 100vh; }
    /* 删除了所有侧边栏样式 */
    .main-container { flex: 1; margin-left: 260px; }
    @media (max-width: 959px) {
      .main-container { margin-left: 0; }
    }
  </style>
</head>
<body>
  <!-- 使用组件容器 -->
  <div id="admin-sidebar-container"></div>
  
  <div class="main-container">
    <!-- 主内容 -->
  </div>
  
  <script src="../js/common.js"></script>
  <script src="components/sidebar.js"></script>
  <script>
    // 初始化侧边栏
    initAdminSidebar('dashboard');
    
    // 页面逻辑
  </script>
</body>
</html>
```

## ✅ 验证清单

修改完成后，检查：
- [ ] 侧边栏正常显示
- [ ] 当前页面菜单项高亮
- [ ] 所有菜单链接可点击
- [ ] 响应式布局正常（移动端）
- [ ] 控制台无错误

## 📊 预期效果

每个页面将减少：
- **HTML**: ~70 行
- **CSS**: ~150 行
- **总计**: ~220 行代码

全部 6 个页面总共减少约 **1320 行重复代码**！

## 🎉 完成后的好处

1. **统一维护** - 修改菜单只需改一个文件
2. **代码简洁** - 每个页面更专注于自己的业务逻辑
3. **易于扩展** - 添加新菜单项自动应用到所有页面
4. **减少错误** - 避免不同页面菜单不一致的问题

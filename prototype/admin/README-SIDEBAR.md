# 管理后台侧边栏组件使用说明

## 📋 组件文件
- `components/sidebar.js` - 侧边栏组件

## 🎯 使用方法

### 1. 在 HTML 中引入组件

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>页面标题 - Open436 论坛</title>
  <link rel="stylesheet" href="../css/common.css">
  <style>
    body {
      display: flex;
      min-height: 100vh;
    }

    .main-content {
      flex: 1;
      margin-left: 260px;
      padding: var(--space-xl);
      background: var(--background-secondary);
    }

    @media (max-width: 959px) {
      .main-content {
        margin-left: 0;
      }
    }

    /* 其他页面特定样式 */
  </style>
</head>
<body>
  <!-- 侧边栏容器 -->
  <div id="admin-sidebar-container"></div>

  <!-- 主内容区 -->
  <main class="main-content">
    <!-- 页面内容 -->
  </main>

  <script src="../js/common.js"></script>
  <script src="components/sidebar.js"></script>
  <script>
    // 检查管理员权限
    const currentUser = JSON.parse(localStorage.getItem('currentUser'));
    if (!currentUser || currentUser.role !== 'admin') {
      window.location.href = '../pages/home.html';
    }

    // 初始化侧边栏，传入当前页面标识
    initAdminSidebar('dashboard'); // 可选值: dashboard, sections, posts, comments, reports, users
    
    // 页面其他 JavaScript 代码...
  </script>
</body>
</html>
```

### 2. 页面标识对应关系

| 页面文件 | activePage 参数 |
|---------|----------------|
| dashboard.html | 'dashboard' |
| sections.html | 'sections' |
| posts.html | 'posts' |
| comments.html | 'comments' |
| reports.html | 'reports' |
| users.html | 'users' |

### 3. 移除的内容

使用组件后，可以从各个页面中移除：

1. **HTML 部分**：整个 `<aside class="sidebar">...</aside>` 标签
2. **CSS 部分**：所有侧边栏相关样式（.sidebar, .sidebar-header, .menu-item 等）

### 4. 保留的内容

每个页面仍需保留：

1. **基础样式**：
```css
body {
  display: flex;
  min-height: 100vh;
}

.main-content {
  flex: 1;
  margin-left: 260px;
  padding: var(--space-xl);
  background: var(--background-secondary);
}

@media (max-width: 959px) {
  .main-content {
    margin-left: 0;
  }
}
```

2. **页面特定样式**：如 .page-header, .data-table 等
3. **页面特定 JavaScript**：业务逻辑代码

## ✨ 优势

1. **统一维护** - 侧边栏只需在一个文件中修改
2. **减少重复** - 每个页面减少约 100 行代码
3. **易于扩展** - 添加新菜单项只需修改 sidebar.js
4. **自动高亮** - 根据 activePage 参数自动高亮当前页面

## 🔄 更新现有页面

要将现有页面改为使用组件：

1. 删除 `<aside class="sidebar">...</aside>` 部分
2. 添加 `<div id="admin-sidebar-container"></div>`
3. 删除侧边栏相关 CSS 样式
4. 引入 `<script src="components/sidebar.js"></script>`
5. 调用 `initAdminSidebar('页面标识')`

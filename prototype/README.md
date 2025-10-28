# Open436 论坛原型

这是一个基于 Clean Design UI 设计规范构建的现代化论坛系统静态原型。

## 项目结构

```
prototype/
├── index.html              # 入口文件（自动跳转到登录页）
├── pages/                  # 用户端页面
│   ├── login.html         # 登录页面
│   ├── home.html          # 首页（帖子时间线）
│   ├── post-detail.html   # 帖子详情页
│   ├── post-new.html      # 发布新帖页面
│   └── ...                # 其他用户页面
├── admin/                  # 管理端页面
│   ├── dashboard.html     # 管理后台首页
│   ├── users.html         # 用户管理
│   └── ...                # 其他管理页面
├── css/                    # 样式文件
│   ├── common.css         # 通用样式和组件
│   └── navbar.css         # 导航栏样式
├── js/                     # JavaScript 文件
│   └── common.js          # 通用功能函数
└── assets/                 # 静态资源（图片、图标等）
```

## 功能特性

### 用户端功能
- ✅ 用户登录（支持记住我）
- ✅ 帖子时间线浏览
- ✅ 帖子详情查看
- ✅ 发布新帖（Markdown 编辑器）
- ✅ 实时预览
- ✅ 点赞、收藏功能
- ✅ 回复功能
- ✅ 代码高亮和一键复制
- ✅ 图片预览

### 管理端功能
- ✅ 管理后台仪表盘
- ✅ 用户管理（查看、编辑、禁用）
- ✅ 内容管理
- ✅ 统计数据展示

### 设计特点
- 🎨 遵循 Clean Design 设计规范
- 📱 完全响应式设计
- 🌈 现代化的配色方案
- ⚡ 流畅的动画效果
- ♿ 良好的无障碍支持
- 🎯 清晰的视觉层级

## 快速开始

### 方式一：直接打开
1. 双击 `index.html` 文件
2. 或者在浏览器中打开 `pages/login.html`

### 方式二：使用本地服务器（推荐）
```bash
# 使用 Python
python -m http.server 8000

# 使用 Node.js (http-server)
npx http-server -p 8000

# 使用 PHP
php -S localhost:8000
```

然后在浏览器中访问 `http://localhost:8000`

## 测试账号

### 普通用户
- 用户名：任意
- 密码：任意
- 说明：登录后可访问用户端所有功能

### 管理员
- 用户名：`admin`
- 密码：任意
- 说明：登录后可访问管理后台

## 页面导航

### 用户端页面
- `/pages/login.html` - 登录页面
- `/pages/home.html` - 首页
- `/pages/post-detail.html` - 帖子详情
- `/pages/post-new.html` - 发布新帖

### 管理端页面
- `/admin/dashboard.html` - 管理后台首页
- `/admin/users.html` - 用户管理

## 技术栈

- **HTML5** - 语义化标签
- **CSS3** - 现代 CSS 特性（Grid、Flexbox、CSS Variables）
- **Vanilla JavaScript** - 原生 JavaScript，无依赖
- **LocalStorage** - 模拟用户登录状态

## 设计规范

本项目严格遵循 `Clean_Design_UI规范.md` 中定义的设计规范：

### 色彩系统
- 主色：#1976D2（蓝色）
- 辅助色：#FF6F00（橙色）
- 语义色：成功、警告、错误、信息

### 排版规范
- 字体：Noto Sans SC / PingFang SC / Microsoft YaHei
- 字号：12px - 32px
- 行高：1.5 - 1.8

### 间距系统
- 基于 8px 的间距系统
- 4px、8px、12px、16px、24px、32px、48px、64px

### 组件规范
- 按钮高度：36px
- 输入框高度：40px
- 圆角：4px / 8px / 12px
- 阴影：多层级阴影系统

## 浏览器支持

- Chrome (推荐)
- Firefox
- Safari
- Edge
- 移动端浏览器

## 功能说明

### 模拟数据
所有数据都是前端模拟的，包括：
- 用户信息存储在 LocalStorage
- 帖子列表、回复等使用 Mock 数据
- 所有操作都是模拟的，刷新页面后会重置

### 交互功能
- ✅ 下拉菜单
- ✅ 模态框
- ✅ Toast 提示
- ✅ 确认对话框
- ✅ 图片预览
- ✅ 代码复制
- ✅ Markdown 实时预览

## 开发说明

### 添加新页面
1. 在 `pages/` 或 `admin/` 目录创建 HTML 文件
2. 引入 `common.css` 和 `navbar.css`
3. 引入 `common.js`
4. 使用已定义的组件和样式类

### 自定义样式
所有 CSS 变量定义在 `css/common.css` 的 `:root` 中，可以统一修改主题色彩。

### 扩展功能
在 `js/common.js` 中添加通用功能函数，可在所有页面中使用。

## 注意事项

1. **本地文件限制**：某些浏览器对本地文件有安全限制，建议使用本地服务器运行
2. **数据持久化**：使用 LocalStorage 存储数据，清除浏览器缓存会丢失数据
3. **图片上传**：图片上传功能使用 FileReader 转换为 Base64，实际项目需要后端支持
4. **路由**：使用简单的页面跳转，实际项目建议使用前端路由

## 后续开发建议

### 前端优化
- [ ] 使用前端框架（React/Vue）
- [ ] 实现真实的路由系统
- [ ] 添加状态管理
- [ ] 优化性能和加载速度

### 后端集成
- [ ] 连接真实 API
- [ ] 实现用户认证
- [ ] 数据库集成
- [ ] 文件上传服务

### 功能完善
- [ ] 搜索功能
- [ ] 个人资料页面
- [ ] 收藏页面
- [ ] 通知系统
- [ ] 更多管理功能

## 许可证

MIT License

## 联系方式

如有问题或建议，请联系开发团队。

---

**版本**: v1.0.0  
**创建日期**: 2024-10-23  
**基于**: Clean Design UI 设计规范

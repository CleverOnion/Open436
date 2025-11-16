# M5 板块模块前端测试指南

## 测试概述

本文档提供 M5 板块管理模块前端代码的完整测试方案，包括单元测试和集成测试。

## 测试架构

### 测试框架
- **Vitest**: 现代化的 Vite 原生测试框架
- **@vue/test-utils**: Vue 3 官方测试工具
- **happy-dom**: 轻量级 DOM 环境

### 测试覆盖范围

#### 1. API 模块测试 (`src/api/modules/section.test.js`)
- ✅ 板块列表获取 (getList)
- ✅ 获取所有启用板块 (getAllEnabled)
- ✅ 板块详情获取 (getDetail)
- ✅ 板块创建 (create)
- ✅ 板块更新 (update)
- ✅ 板块删除 (delete)
- ✅ 启用/禁用状态切换 (toggleStatus)
- ✅ 板块排序 (reorder)
- ✅ 统计数据获取 (getStatistics)
- ✅ 板块验证 (validate)
- ✅ 帖子数增减 (incrementPosts)

#### 2. 组件测试 (`src/components/SectionSelector.test.js`)
- ✅ 组件渲染
- ✅ 占位符显示
- ✅ 下拉菜单交互
- ✅ 板块选择功能
- ✅ 选中状态显示
- ✅ 空状态处理
- ✅ 图标和颜色显示

#### 3. 视图集成测试
**板块列表视图** (`src/views/sections/SectionList.test.js`)
- ✅ 页面渲染
- ✅ 加载状态
- ✅ 空状态显示
- ✅ 板块列表展示
- ✅ 路由跳转
- ✅ 统计信息显示
- ✅ 错误处理

**板块管理视图** (`src/views/sections/SectionManage.test.js`)
- ✅ 页面渲染
- ✅ 添加板块对话框
- ✅ 编辑板块对话框
- ✅ 删除确认对话框
- ✅ 启用/禁用状态切换
- ✅ 表单验证
- ✅ 图标和颜色选择器
- ✅ 消息提示

## 快速开始

### 1. 安装依赖
```bash
npm install
```

### 2. 运行所有测试
```bash
npm test
```

### 3. 运行测试并生成覆盖率报告
```bash
npm run test:coverage
```

### 4. 使用 UI 界面运行测试
```bash
npm run test:ui
```

## 测试命令详解

### 基础命令
```bash
# 运行所有测试
npm test

# 监听模式（文件变化时自动重新运行）
npm test -- --watch

# 运行特定测试文件
npm test section.test.js

# 运行匹配模式的测试
npm test -- --grep "getList"
```

### 覆盖率命令
```bash
# 生成覆盖率报告
npm run test:coverage

# 查看 HTML 格式的覆盖率报告
# 报告位置: coverage/index.html
```

### UI 界面
```bash
# 启动测试 UI
npm run test:ui

# 浏览器访问: http://localhost:51204/__vitest__/
```

## 测试文件结构

```
Open436-Frontend/
├── src/
│   ├── api/
│   │   └── modules/
│   │       ├── section.js              # API 模块
│   │       └── section.test.js         # API 测试
│   ├── components/
│   │   ├── SectionSelector.vue         # 板块选择器组件
│   │   └── SectionSelector.test.js     # 组件测试
│   └── views/
│       └── sections/
│           ├── SectionList.vue         # 板块列表视图
│           ├── SectionList.test.js     # 列表视图测试
│           ├── SectionManage.vue       # 板块管理视图
│           └── SectionManage.test.js   # 管理视图测试
├── tests/
│   └── setup.js                        # 测试环境设置
├── vite.config.js                      # Vite 配置（含测试配置）
├── vitest.config.js                    # Vitest 专用配置
└── M5-TEST-GUIDE.md                    # 本文档
```

## 测试覆盖率目标

| 指标 | 目标 | 当前状态 |
|------|------|----------|
| 语句覆盖率 | ≥ 80% | ✅ |
| 分支覆盖率 | ≥ 80% | ✅ |
| 函数覆盖率 | ≥ 80% | ✅ |
| 行覆盖率 | ≥ 80% | ✅ |

## 测试最佳实践

### 1. 测试命名规范
```javascript
describe('模块/组件名称', () => {
  it('应该[预期行为]', () => {
    // 测试代码
  })
})
```

### 2. 测试结构 (AAA 模式)
```javascript
it('应该正确处理数据', () => {
  // Arrange - 准备测试数据
  const data = { id: 1, name: 'test' }
  
  // Act - 执行操作
  const result = processData(data)
  
  // Assert - 断言结果
  expect(result).toBe(expected)
})
```

### 3. Mock 使用
```javascript
// Mock API 请求
vi.mock('../request', () => ({
  default: vi.fn()
}))

// Mock Store
const sectionStore = useSectionStore()
vi.spyOn(sectionStore, 'fetchSections').mockResolvedValue()
```

### 4. 异步测试
```javascript
it('应该处理异步操作', async () => {
  await wrapper.find('.button').trigger('click')
  await flushPromises()
  
  expect(wrapper.text()).toContain('成功')
})
```

## 常见问题

### Q1: 测试运行失败，提示找不到模块
**A**: 检查 `vite.config.js` 中的路径别名配置是否正确。

### Q2: 组件测试中无法访问 Store
**A**: 确保在测试中正确设置了 Pinia：
```javascript
const pinia = createPinia()
setActivePinia(pinia)
```

### Q3: 覆盖率报告不准确
**A**: 检查 `vitest.config.js` 中的 `include` 和 `exclude` 配置。

### Q4: 测试运行很慢
**A**: 使用 `--run` 参数运行一次性测试，或使用 `--reporter=dot` 简化输出。

## 持续集成

### GitHub Actions 配置示例
```yaml
name: Test

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-node@v3
        with:
          node-version: '18'
      
      - name: Install dependencies
        run: npm ci
      
      - name: Run tests
        run: npm run test:coverage
      
      - name: Upload coverage
        uses: codecov/codecov-action@v3
```

## 测试维护

### 定期检查
- [ ] 每周运行完整测试套件
- [ ] 每月审查测试覆盖率
- [ ] 新功能必须包含测试
- [ ] 修复 Bug 时添加回归测试

### 测试更新
- 当 API 接口变更时，更新 API 测试
- 当组件 Props 变更时，更新组件测试
- 当业务逻辑变更时，更新集成测试

## 相关资源

- [Vitest 官方文档](https://vitest.dev/)
- [Vue Test Utils 文档](https://test-utils.vuejs.org/)
- [Testing Library 最佳实践](https://testing-library.com/docs/guiding-principles)

## 联系方式

如有测试相关问题，请联系：
- 项目负责人：[待填写]
- 技术支持：[待填写]

---

**最后更新**: 2024-11-16
**版本**: 1.0.0

# 🚀 使用 Cursor 完整开发论坛板块管理模块指南

## 📋 目录

1. [开发流程总览](#开发流程总览)
2. [阶段一：项目规划](#阶段一项目规划)
3. [阶段二：环境搭建](#阶段二环境搭建)
4. [阶段三：核心开发](#阶段三核心开发)
5. [阶段四：测试与调试](#阶段四测试与调试)
6. [阶段五：Docker部署](#阶段五docker部署)
7. [阶段六：Git版本控制](#阶段六git版本控制)
8. [Cursor使用技巧](#cursor使用技巧)

---

## 开发流程总览

```
┌─────────────────────────────────────────────────────┐
│                  完整开发流程                        │
└─────────────────────────────────────────────────────┘

第一步: 项目规划 (1-2天)
  ├── 编写PRD文档 ✅ (你已完成)
  ├── 编写TDD文档 ✅ (你已完成)
  └── 确定技术栈

第二步: 环境搭建 (半天)
  ├── 初始化Git仓库
  ├── 创建项目结构
  ├── 配置开发环境
  └── 搭建数据库

第三步: 核心开发 (3-5天)
  ├── 数据模型开发
  ├── API接口开发
  ├── 业务逻辑开发
  └── 工具函数开发

第四步: 测试 (1-2天)
  ├── 单元测试
  ├── 集成测试
  └── 手动测试API

第五步: Docker化 (半天)
  ├── 编写Dockerfile
  ├── 配置docker-compose
  └── 测试容器运行

第六步: Git管理 (持续)
  ├── 提交代码
  ├── 推送到远程仓库
  └── 版本管理

第七步: 部署上线 (1天)
  ├── 生产环境配置
  ├── 部署到服务器
  └── 监控与维护
```

---

## 阶段一：项目规划

### ✅ 你已完成的工作

1. **PRD文档** (`docs/PRD/M5-板块管理模块.md`)
   - 定义了功能需求
   - 明确了用户故事
   - 规划了业务规则

2. **TDD文档** (`docs/TDD/M5-板块管理服务/01-数据库设计.md`)
   - 设计了数据库表结构
   - 定义了索引策略
   - 规划了数据关系

### 💡 是否需要这些文档？

**强烈推荐！** 原因：

- ✅ **PRD文档**：帮助你理清需求，避免开发中频繁改需求
- ✅ **TDD文档**：提前设计好技术架构，避免后期重构
- ✅ **让Cursor更懂你**：有了文档，Cursor可以更准确地生成代码

**如何让Cursor帮你写文档？**

```bash
# 在Cursor中直接提问：
"帮我写一个论坛板块管理模块的PRD文档，包括功能需求、用户故事、业务规则"

"根据这个PRD，帮我设计数据库表结构"
```

---

## 阶段二：环境搭建

### 步骤 2.1：初始化 Git 仓库

**在Cursor终端中执行：**

```bash
# 1. 初始化Git（如果还没有）
git init

# 2. 添加所有文件
git add .

# 3. 首次提交
git commit -m "Initial commit: 项目结构和基础代码"

# 4. 关联远程仓库（例如GitHub）
git remote add origin https://github.com/your-username/section-service.git

# 5. 推送到远程
git push -u origin main
```

**如何让Cursor帮你：**

```bash
# 直接在Cursor中说：
"帮我初始化Git仓库并推送到GitHub"
```

### 步骤 2.2：创建 `.env` 文件

**在Cursor终端中执行：**

```bash
# Windows
copy .env.example .env

# Linux/Mac
cp .env.example .env
```

**编辑 `.env` 文件**，设置数据库密码等：

```bash
DATABASE_URL=postgresql://open436:your_password@localhost:55432/open436
POSTGRES_PASSWORD=your_password
```

### 步骤 2.3：安装依赖（本地开发）

**方式一：使用Docker（推荐）**

```bash
# 无需安装Python依赖，直接启动
docker-compose up -d
```

**方式二：本地Python环境**

```bash
# 创建虚拟环境
python -m venv venv

# 激活虚拟环境
# Windows:
venv\Scripts\activate
# Linux/Mac:
source venv/bin/activate

# 安装依赖
pip install -r requirements.txt
```

---

## 阶段三：核心开发

### ✅ 已完成的代码

你现在已经有了完整的代码结构：

```
✅ 数据模型 (models/)
  ├── database.py      # 数据库连接
  ├── section.py       # ORM模型
  └── schemas.py       # Pydantic模型

✅ API路由 (routes/)
  └── sections.py      # 所有板块API

✅ 业务逻辑 (services/)
  └── section_service.py  # 板块服务

✅ 工具函数 (utils/)
  ├── logger.py        # 日志工具
  └── validators.py    # 验证工具

✅ 配置文件
  ├── config.py        # 应用配置
  └── main.py          # FastAPI入口
```

### 步骤 3.1：测试运行服务

**使用Docker：**

```bash
# 启动所有服务（数据库+API）
docker-compose up -d

# 查看日志
docker-compose logs -f section-service

# 访问API文档
# 浏览器打开: http://localhost:8005/docs
```

**本地运行：**

```bash
# 1. 先启动数据库
docker run -d \
  --name postgres-dev \
  -e POSTGRES_USER=open436 \
  -e POSTGRES_PASSWORD=open436_password \
  -e POSTGRES_DB=open436 \
  -p 55432:5432 \
  postgres:14-alpine

# 2. 执行数据库初始化
psql -h localhost -p 55432 -U open436 -d open436 -f db-init/V1__create_sections_table.sql

# 3. 启动服务
uvicorn src.app.main:app --reload --port 8000
```

### 步骤 3.2：测试 API

**方式一：使用 Swagger UI**

访问：http://localhost:8005/docs

- 直接在网页上测试所有API
- 查看请求/响应格式
- 非常直观

**方式二：使用 curl**

```bash
# 1. 创建板块
curl -X POST "http://localhost:8005/api/v1/sections/" \
  -H "Content-Type: application/json" \
  -d '{
    "slug": "tech",
    "name": "技术交流",
    "description": "分享技术",
    "color": "#1976D2",
    "sort_order": 1
  }'

# 2. 获取板块列表
curl "http://localhost:8005/api/v1/sections/"

# 3. 获取单个板块
curl "http://localhost:8005/api/v1/sections/1"

# 4. 更新板块
curl -X PUT "http://localhost:8005/api/v1/sections/1" \
  -H "Content-Type: application/json" \
  -d '{"name": "新名称"}'

# 5. 删除板块
curl -X DELETE "http://localhost:8005/api/v1/sections/1"
```

**方式三：使用 Postman 或 Insomnia**

导入API文档：http://localhost:8005/openapi.json

---

## 阶段四：测试与调试

### 步骤 4.1：运行自动化测试

```bash
# 运行所有测试
pytest

# 运行特定测试文件
pytest src/tests/test_sections.py

# 查看详细输出
pytest -v

# 查看测试覆盖率
pytest --cov=src --cov-report=html
# 打开 htmlcov/index.html 查看覆盖率报告
```

### 步骤 4.2：调试技巧

**1. 查看日志**

```bash
# Docker环境
docker-compose logs -f section-service

# 本地环境（日志会直接输出到终端）
```

**2. 使用Cursor调试**

在代码中设置断点：

```python
# 在需要调试的地方
import pdb; pdb.set_trace()
```

**3. 数据库调试**

```bash
# 进入数据库
docker exec -it section-service-db psql -U open436 -d open436

# 查看数据
SELECT * FROM sections;

# 查看表结构
\d sections

# 退出
\q
```

---

## 阶段五：Docker部署

### ✅ 已完成的Docker配置

你已经有了：
- ✅ `Dockerfile` - 应用镜像构建
- ✅ `docker-compose.yml` - 服务编排
- ✅ `db-init/` - 数据库初始化脚本

### 步骤 5.1：构建和运行

```bash
# 1. 构建镜像
docker-compose build

# 2. 启动服务
docker-compose up -d

# 3. 查看运行状态
docker-compose ps

# 4. 查看日志
docker-compose logs -f

# 5. 停止服务
docker-compose down

# 6. 停止并删除数据
docker-compose down -v
```

### 步骤 5.2：Docker常用命令

```bash
# 进入容器
docker exec -it section-service bash

# 重启服务
docker-compose restart section-service

# 重新构建并启动
docker-compose up -d --build

# 查看资源使用
docker stats
```

### 步骤 5.3：生产环境部署

**1. 创建生产环境配置**

```bash
# 创建 docker-compose.prod.yml
version: '3.8'

services:
  section-service:
    build: .
    environment:
      - DATABASE_URL=postgresql://user:pass@prod-db:5432/dbname
      - LOG_LEVEL=WARNING
    restart: always
```

**2. 使用生产配置启动**

```bash
docker-compose -f docker-compose.prod.yml up -d
```

---

## 阶段六：Git版本控制

### 步骤 6.1：Git基础操作

```bash
# 1. 查看状态
git status

# 2. 添加文件
git add .

# 3. 提交
git commit -m "feat: 实现板块CRUD功能"

# 4. 推送
git push origin main

# 5. 拉取最新代码
git pull origin main
```

### 步骤 6.2：分支管理

```bash
# 1. 创建功能分支
git checkout -b feature/add-section-icons

# 2. 在分支上开发...
git add .
git commit -m "feat: 添加板块图标上传功能"

# 3. 推送分支
git push origin feature/add-section-icons

# 4. 合并到主分支
git checkout main
git merge feature/add-section-icons

# 5. 删除分支
git branch -d feature/add-section-icons
```

### 步骤 6.3：Commit规范

```bash
# 格式: <type>: <description>

feat: 新功能
fix: 修复bug
docs: 文档更新
style: 代码格式
refactor: 重构
test: 测试
chore: 构建/工具

# 例子：
git commit -m "feat: 添加板块排序功能"
git commit -m "fix: 修复删除板块时的并发问题"
git commit -m "docs: 更新API文档"
```

### 步骤 6.4：使用GitHub

```bash
# 1. 在GitHub上创建仓库
# 访问 https://github.com/new

# 2. 关联远程仓库
git remote add origin https://github.com/your-username/section-service.git

# 3. 推送代码
git push -u origin main

# 4. 后续推送
git push
```

---

## Cursor使用技巧

### 技巧 1：让Cursor生成代码

**示例对话：**

```
你: "帮我添加一个批量启用板块的API"

Cursor会：
1. 在 section_service.py 中添加业务逻辑
2. 在 sections.py 中添加路由
3. 在 schemas.py 中添加请求模型
4. 生成对应的测试用例
```

### 技巧 2：让Cursor修复错误

```
你: "运行测试时出现这个错误：[粘贴错误信息]"

Cursor会：
- 分析错误原因
- 提供修复方案
- 直接修改代码
```

### 技巧 3：让Cursor优化代码

```
你: "帮我优化 section_service.py 的性能"

Cursor会：
- 分析代码
- 添加索引优化
- 实现缓存机制
- 优化数据库查询
```

### 技巧 4：让Cursor编写文档

```
你: "帮我为这个函数生成文档字符串"

Cursor会：
- 分析函数参数和返回值
- 生成详细的docstring
- 包含使用示例
```

### 技巧 5：让Cursor执行命令

```
你: "帮我启动Docker服务"

Cursor会：
- 提供 docker-compose up 命令
- 或直接执行命令（如果你允许）
```

---

## 完整开发流程示例

### 假设你要添加一个新功能："板块点赞"

**第1步：询问Cursor**

```
你: "我想添加板块点赞功能，用户可以点赞板块，需要记录点赞数。
请帮我：
1. 设计数据库表
2. 添加API接口
3. 编写测试用例"
```

**第2步：Cursor会做什么**

1. **修改数据库模型**
   - 在 `section.py` 添加 `likes_count` 字段
   - 创建数据库迁移脚本

2. **添加业务逻辑**
   - 在 `section_service.py` 添加 `like_section()` 方法
   - 添加 `unlike_section()` 方法

3. **添加API端点**
   - 在 `sections.py` 添加路由

4. **编写测试**
   - 在 `test_sections.py` 添加测试用例

**第3步：测试和调试**

```bash
# 运行测试
pytest

# 手动测试API
curl -X POST "http://localhost:8005/api/v1/sections/1/like"
```

**第4步：提交代码**

```bash
git add .
git commit -m "feat: 添加板块点赞功能"
git push
```

---

## 常见问题解答

### Q1: Docker启动失败怎么办？

```bash
# 查看详细日志
docker-compose logs

# 常见原因：
# 1. 端口被占用
netstat -an | grep 8005

# 2. 数据库连接失败
docker-compose logs postgres

# 3. 重新构建
docker-compose down
docker-compose build --no-cache
docker-compose up
```

### Q2: 数据库连接不上？

```bash
# 检查数据库是否运行
docker ps | grep postgres

# 检查连接字符串
echo $DATABASE_URL

# 测试连接
docker exec -it section-service-db psql -U open436 -d open436
```

### Q3: 测试失败怎么办？

```bash
# 查看详细错误
pytest -v --tb=long

# 单独运行某个测试
pytest src/tests/test_sections.py::TestSectionAPI::test_create_section -v

# 清理测试数据
rm -f test.db
```

### Q4: 如何调试代码？

```python
# 方法1: 使用print
print(f"Debug: section_id={section_id}")

# 方法2: 使用日志
from src.app.utils.logger import logger
logger.debug(f"Section data: {section}")

# 方法3: 使用断点
import pdb; pdb.set_trace()
```

---

## 下一步计划

### 已完成 ✅
- [x] 项目结构搭建
- [x] 数据模型开发
- [x] API接口开发
- [x] 测试用例编写
- [x] Docker配置

### 待完成 📝

**短期（1-2周）**
- [ ] 添加用户认证（与M1用户服务对接）
- [ ] 实现文件上传（板块图标，与M7对接）
- [ ] 添加API速率限制
- [ ] 实现缓存机制

**中期（1个月）**
- [ ] 添加板块统计功能
- [ ] 实现板块管理员权限
- [ ] 添加板块规则说明
- [ ] 实现板块搜索功能

**长期（2-3个月）**
- [ ] 性能优化
- [ ] 监控和日志系统
- [ ] CI/CD流程
- [ ] 生产环境部署

---

## 总结

### 🎯 核心步骤回顾

1. **规划** → 写PRD和TDD（✅ 已完成）
2. **搭建** → 创建项目结构（✅ 已完成）
3. **开发** → 实现核心功能（✅ 已完成）
4. **测试** → 编写测试用例（✅ 已完成）
5. **Docker** → 容器化部署（✅ 已完成）
6. **Git** → 版本控制（✅ 准备就绪）

### 💡 关键建议

1. **文档先行**：PRD和TDD文档很重要，不要跳过
2. **小步快跑**：一次实现一个功能，及时测试
3. **善用Cursor**：让AI帮你写代码、测试、文档
4. **版本控制**：频繁提交，保持代码可追溯
5. **Docker优先**：使用Docker可以避免环境问题

### 🚀 现在就开始

```bash
# 1. 启动服务
docker-compose up -d

# 2. 访问API文档
# http://localhost:8005/docs

# 3. 开始测试和开发！
```

---

**祝你开发顺利！有任何问题随时问Cursor！** 🎉


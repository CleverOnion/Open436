# Open436 板块管理服务 (M5)

## 服务概述

板块管理服务（section-service）负责论坛板块的组织结构管理、板块配置和板块统计。

- **服务端口**: 8005
- **技术栈**: Python 3.11+ + Django 4.2+ + Django REST Framework
- **数据库**: PostgreSQL (public schema)

## 快速启动

### 1. 安装依赖

```bash
# 创建虚拟环境
python -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate

# 安装依赖
pip install -r requirements.txt
```

### 2. 配置环境变量

```bash
cp env.example .env
# 编辑 .env 文件配置数据库等信息
```

### 3. 执行数据库迁移

```bash
# 通过 Docker 执行 SQL 脚本
Get-Content migrations\V1__create_sections_table.sql | docker exec -i open436-postgres-dev psql -U open436 -d open436
```

### 4. 运行服务

```bash
python manage.py runserver 8005
```

## API 文档

启动服务后访问:
- Swagger UI: http://localhost:8005/api/docs/
- ReDoc: http://localhost:8005/api/redoc/
- 健康检查: http://localhost:8005/health/

## 主要功能

### 公开接口
- `GET /api/sections` - 获取板块列表
- `GET /api/sections/{id_or_slug}` - 获取板块详情

### 管理员接口（需要admin权限）
- `POST /api/sections` - 创建板块
- `PUT /api/sections/{id}` - 更新板块
- `DELETE /api/sections/{id}` - 删除板块
- `PUT /api/sections/{id}/status` - 启用/禁用板块
- `PUT /api/sections/reorder` - 批量调整排序
- `GET /api/sections/statistics` - 获取统计数据

### 内部接口（供M3调用）
- `POST /internal/sections/{id}/increment-posts` - 更新帖子数

## 认证机制

本服务通过Kong Gateway的headers获取用户信息：
- `X-User-Id`: 用户ID
- `X-Username`: 用户名
- `X-User-Role`: 用户角色

无需手动处理Token验证，Kong已完成认证。

## 项目结构

```
Open436-SectionService/
├── manage.py
├── requirements.txt
├── .env
├── config/              # Django配置
│   ├── settings.py
│   ├── urls.py
│   └── wsgi.py
├── apps/
│   ├── sections/        # 板块应用
│   │   ├── models.py
│   │   ├── serializers.py
│   │   ├── views.py
│   │   ├── urls.py
│   │   └── admin.py
│   └── core/            # 核心工具
│       ├── middleware.py
│       ├── permissions.py
│       ├── responses.py
│       └── exceptions.py
└── migrations/          # SQL迁移脚本
    └── V1__create_sections_table.sql
```

## 开发状态

- [x] 阶段1: 项目初始化
- [x] 阶段2: 数据层实现
- [x] 阶段3: 公开API
- [ ] 阶段4: 管理员API
- [ ] 阶段5: 权限控制
- [ ] 阶段6: 服务集成
- [ ] 阶段7: 测试与优化
- [ ] 阶段8: 部署准备

## 参考文档

详细的技术设计文档请参考：
- [TDD文档目录](../docs/TDD/M5-板块管理服务/)







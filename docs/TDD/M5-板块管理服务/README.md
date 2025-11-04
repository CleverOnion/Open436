# M5 - 板块管理服务技术设计文档

## 文档概述

本文件夹包含 **M5 板块管理服务 (section-service)** 的详细技术设计文档。

**服务职责**: 论坛板块的组织结构管理、板块配置、板块与帖子的关联管理

**技术栈**: Python 3.11+ + Django 4.2+ + Django REST Framework + PostgreSQL

---

## 📚 文档列表

| 文档                                         | 说明                                 | 状态      |
| -------------------------------------------- | ------------------------------------ | --------- |
| [00-开发指南](./00-开发指南.md)              | 开发环境搭建、项目结构、开发规范     | ✅ 已完成 |
| [01-数据库设计](./01-数据库设计.md)          | 数据库表结构、索引、关系设计         | ✅ 已完成 |
| [02-API 接口设计](./02-API接口设计.md)       | RESTful API 详细设计                 | ✅ 已完成 |
| [03-Django 模型设计](./03-Django模型设计.md) | Django Models、Serializers、ViewSets | ✅ 已完成 |
| [04-与其他服务集成](./04-与其他服务集成.md)    | 服务间通信、M1/M3/M7集成         | ✅ 已完成 |
| [05-开发任务清单](./05-开发任务清单.md)      | 详细开发任务和进度跟踪               | ✅ 已完成 |

---

## 🎯 快速导航

### 新开发者入门

1. **阅读开发指南** - 搭建开发环境
2. **阅读数据库设计** - 了解数据模型
3. **阅读 Django 模型设计** - 理解 ORM 设计
4. **阅读 API 接口设计** - 了解对外接口
5. **阅读与其他服务集成** - 理解服务协作

### 核心技术栈

- **语言**: Python 3.11+
- **框架**: Django 4.2+ (Django REST Framework 3.14+)
- **数据库**: PostgreSQL 14+ (public schema)
- **ORM**: Django ORM
- **API 文档**: drf-spectacular (OpenAPI 3.0)
- **服务发现**: Consul 1.17+

---

## 🔑 核心功能

### 板块浏览（公开接口）

- ✅ 获取启用板块列表
- ✅ 查看板块详情
- ✅ 板块按排序号显示
- ✅ 板块统计数据（帖子数）

### 板块管理（管理员功能）

- ✅ 创建板块
- ✅ 编辑板块信息
- ✅ 删除板块（软删除）
- ✅ 启用/禁用板块
- ✅ 调整板块排序
- ✅ 查看板块统计

### 板块配置

- ✅ 板块名称、标识、描述
- ✅ 板块图标（集成 M7 文件服务）
- ✅ 板块颜色标识
- ✅ 排序号设置

---

## 📊 数据模型概览

```
sections (板块表 - public schema)
├── id (主键)
├── slug (板块标识，唯一，用于URL)
├── name (板块名称，唯一)
├── description (板块描述)
├── icon_file_id (图标文件ID，外键 → files.id)
├── color (板块颜色，HEX格式)
├── sort_order (排序号，1-999)
├── is_enabled (启用状态)
├── posts_count (帖子数量，冗余字段)
├── created_at (创建时间)
└── updated_at (更新时间)
```

**索引**:
- `idx_sections_slug` - slug 唯一索引
- `idx_sections_sort_order` - sort_order 索引（用于排序查询）
- `idx_sections_is_enabled` - is_enabled 索引（用于筛选启用板块）

---

## 🔗 模块依赖关系

### 依赖的服务

- **M1 认证授权服务**: 验证管理员权限
- **M7 文件存储服务**: 上传板块图标

### 被依赖的服务

- **M3 内容管理服务**: 验证板块有效性、更新帖子数量

### 与M3的协作

```
板块创建流程：
M5 管理员创建板块 → 板块可用于发帖

发帖流程：
M3 验证板块ID → M5 返回板块状态 → M3 创建帖子 → M5 更新 posts_count

板块禁用：
M5 禁用板块 → M3 禁止在该板块发帖 → 已有帖子仍可访问
```

---

## 📡 服务间通信

### 调用 M1 服务（认证验证）

```python
# 验证管理员权限
POST http://auth-service:8001/api/auth/verify
Headers:
  Authorization: Bearer {token}

Response:
{
    "valid": true,
    "userId": 1,
    "username": "admin",
    "role": "admin"
}
```

### 调用 M7 服务（上传图标）

```python
# 上传板块图标
POST http://file-service:8007/api/files/upload
Headers:
  Authorization: Bearer {token}
  Content-Type: multipart/form-data
Body:
  file: [binary]
  file_type: "section_icon"

Response:
{
    "file_id": "uuid",
    "url": "http://minio:9000/open436-icons/...",
    "file_type": "section_icon"
}
```

### 提供给 M3 的接口

```python
# M3 验证板块有效性
GET http://section-service:8005/api/sections/{slug}

Response:
{
    "id": 1,
    "slug": "tech",
    "name": "技术交流",
    "is_enabled": true
}

# M3 更新板块帖子数（内部接口）
POST http://section-service:8005/internal/sections/{id}/increment-posts
{
    "value": 1  // +1 或 -1
}
```

---

## 🗂️ 项目结构

```
section-service/
├── manage.py                    # Django 管理脚本
├── requirements.txt             # Python 依赖
├── Dockerfile                   # Docker 镜像
├── docker-compose.yml           # 服务编排
├── config/                      # 配置文件
│   ├── settings.py             # Django 设置
│   ├── urls.py                 # 全局路由
│   └── wsgi.py
├── apps/
│   ├── sections/               # 板块管理应用
│   │   ├── models.py           # Section 模型
│   │   ├── serializers.py      # DRF 序列化器
│   │   ├── views.py            # 视图集 (SectionViewSet)
│   │   ├── urls.py             # 路由
│   │   ├── permissions.py      # 权限控制
│   │   ├── services.py         # 业务逻辑
│   │   ├── validators.py       # 验证器
│   │   └── migrations/         # 数据库迁移
│   └── core/                   # 核心工具
│       ├── middleware.py       # Token 验证中间件
│       ├── exceptions.py       # 异常处理
│       ├── consul_client.py    # Consul 服务发现
│       └── utils.py            # 工具函数
├── tests/                      # 测试
│   ├── test_models.py
│   ├── test_views.py
│   ├── test_services.py
│   └── test_integration.py
└── docs/                       # API 文档
```

---

## 🚀 快速开始

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
# .env 文件
DEBUG=True
SECRET_KEY=your-secret-key
DATABASE_URL=postgresql://open436:password@localhost:5432/open436
CONSUL_URL=http://localhost:8500
AUTH_SERVICE_URL=http://localhost:8001
FILE_SERVICE_URL=http://localhost:8007
```

### 3. 执行迁移

```bash
# 注意：sections 表由 SQL 直接创建，Django 使用 managed=False
# 这里仅创建 Django 的内部表
python manage.py migrate
```

### 4. 运行开发服务器

```bash
python manage.py runserver 8005
```

### 5. 测试 API

```bash
# 获取板块列表
curl http://localhost:8005/api/sections

# 通过 Kong 网关访问
curl http://localhost:8000/api/sections
```

---

## 📖 API 文档

### 访问 Swagger UI

```
http://localhost:8005/api/docs/
```

### 访问 ReDoc

```
http://localhost:8005/api/redoc/
```

---

## 🧪 测试

### 运行所有测试

```bash
python manage.py test
```

### 运行特定测试

```bash
python manage.py test apps.sections.tests.test_models
```

### 测试覆盖率

```bash
coverage run --source='.' manage.py test
coverage report
coverage html  # 生成 HTML 报告
```

---

## 📦 部署

### Docker 部署

```bash
# 构建镜像
docker build -t section-service:latest .

# 运行容器
docker run -d \
  -p 8005:8005 \
  -e DATABASE_URL=postgresql://open436:pass@db:5432/open436 \
  -e CONSUL_URL=http://consul:8500 \
  section-service:latest
```

### 使用 docker-compose

```bash
docker-compose up -d
```

---

## 🔗 相关文档

- [PRD - M5 板块管理模块](../../PRD/M5-板块管理模块.md)
- [全局架构设计](../00-全局架构/01-全局架构设计.md)
- [服务间通信规范](../00-全局架构/03-服务间通信规范.md)
- [Django 官方文档](https://docs.djangoproject.com/)
- [Django REST Framework 文档](https://www.django-rest-framework.org/)

---

## 🎨 预设板块

系统初始化时，预设 6 个板块：

| slug     | 名称     | 描述                   | 颜色    | 排序 |
|----------|---------|------------------------|---------|------|
| tech     | 技术交流 | 分享编程技术和开发经验  | #1976D2 | 1    |
| design   | 设计分享 | UI/UX 设计作品和心得   | #9C27B0 | 2    |
| discuss  | 综合讨论 | 各类话题的自由讨论     | #4CAF50 | 3    |
| question | 问答求助 | 技术问题求助和解答     | #FF9800 | 4    |
| share    | 资源分享 | 工具、教程等资源推荐   | #00BCD4 | 5    |
| announce | 公告通知 | 官方公告和重要通知     | #F44336 | 6    |

---

**服务端口**: 8005  
**技术栈**: Python + Django + Django REST Framework + PostgreSQL  
**优先级**: P2（中等优先级）  
**数据库**: public schema（与 M1、M7 共享）


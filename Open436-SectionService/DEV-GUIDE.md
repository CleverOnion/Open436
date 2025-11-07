# M5 板块管理服务 - 开发指南

## 快速开始

### 1. 环境要求

- Python 3.11+
- PostgreSQL 14+
- Docker Desktop (可选，用于基础设施)

### 2. 安装步骤

#### 2.1 创建虚拟环境

```bash
# Windows
python -m venv venv
venv\Scripts\activate

# Linux/Mac
python3 -m venv venv
source venv/bin/activate
```

#### 2.2 安装依赖

```bash
pip install -r requirements.txt
```

#### 2.3 配置环境变量

复制 `env.example` 到 `.env` 并根据需要修改：

```bash
copy env.example .env  # Windows
cp env.example .env    # Linux/Mac
```

关键配置项：

```env
DEBUG=True
SECRET_KEY=your-secret-key
DATABASE_URL=postgresql://open436:open436@localhost:55432/open436
CONSUL_URL=http://localhost:8500
SERVICE_PORT=8005
```

#### 2.4 启动基础设施

确保 PostgreSQL 和 Consul 正在运行：

```bash
# 在项目根目录
cd ../../deploy/dev
docker-compose up -d postgres consul
```

#### 2.5 执行数据库迁移

```bash
# 通过 Docker 执行 SQL 脚本
Get-Content migrations\V1__create_sections_table.sql | docker exec -i open436-postgres-dev psql -U open436 -d open436

# 或者直接用 psql（如果已安装）
psql -h localhost -p 55432 -U open436 -d open436 -f migrations/V1__create_sections_table.sql
```

#### 2.6 启动服务

```bash
# Windows
start.bat

# Linux/Mac
chmod +x start.sh
./start.sh

# 或者直接运行开发服务器
python manage.py runserver 8005
```

### 3. 验证服务

访问以下URL确认服务正常：

- 健康检查: http://localhost:8005/health/
- API文档: http://localhost:8005/api/docs/
- 板块列表: http://localhost:8005/api/sections/

## 开发工作流

### 运行测试

```bash
python manage.py test
```

### 创建管理员账户

```bash
python manage.py createsuperuser
```

访问 http://localhost:8005/admin/ 使用管理后台

### 注册到 Consul

```bash
python manage.py register_consul
```

### 代码风格检查

```bash
# 安装工具
pip install black flake8 isort

# 格式化代码
black .

# 检查代码风格
flake8 .

# 排序 imports
isort .
```

## API 测试示例

### 获取板块列表（公开）

```bash
curl http://localhost:8005/api/sections/
```

### 获取板块详情（通过 slug）

```bash
curl http://localhost:8005/api/sections/tech/
```

### 创建板块（需要管理员权限）

```bash
curl -X POST http://localhost:8005/api/sections/ \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 1" \
  -H "X-Username: admin" \
  -H "X-User-Role: admin" \
  -d '{
    "slug": "newboard",
    "name": "新板块",
    "description": "这是一个新板块",
    "color": "#FF5722",
    "sort_order": 100
  }'
```

### 启用/禁用板块

```bash
curl -X PUT http://localhost:8005/api/sections/1/status/ \
  -H "Content-Type: application/json" \
  -H "X-User-Role: admin" \
  -d '{"is_enabled": false}'
```

### 批量调整排序

```bash
curl -X PUT http://localhost:8005/api/sections/reorder/ \
  -H "Content-Type: application/json" \
  -H "X-User-Role: admin" \
  -d '{
    "sections": [
      {"id": 1, "sort_order": 10},
      {"id": 2, "sort_order": 20}
    ]
  }'
```

## 常见问题

### Q: 数据库连接失败

**A**: 确保 Docker PostgreSQL 容器正在运行：

```bash
docker ps | grep postgres
```

### Q: Consul 注册失败

**A**: 检查 Consul 服务是否运行：

```bash
docker ps | grep consul
# 或访问 http://localhost:8500
```

### Q: 找不到模块错误

**A**: 确保虚拟环境已激活并安装了所有依赖：

```bash
pip install -r requirements.txt
```

## 项目结构

```
Open436-SectionService/
├── config/                 # Django 配置
│   ├── settings.py        # 主要设置
│   ├── urls.py            # URL 路由
│   └── wsgi.py            # WSGI 入口
├── apps/
│   ├── sections/          # 板块应用
│   │   ├── models.py      # 数据模型
│   │   ├── serializers.py # DRF 序列化器
│   │   ├── views.py       # 视图/ViewSet
│   │   ├── urls.py        # API 路由
│   │   └── tests/         # 测试
│   └── core/              # 核心工具
│       ├── middleware.py  # 中间件
│       ├── permissions.py # 权限类
│       ├── responses.py   # 响应工具
│       └── consul_client.py # Consul 客户端
├── migrations/            # SQL 迁移脚本
├── manage.py              # Django 管理脚本
├── requirements.txt       # Python 依赖
├── Dockerfile             # Docker 镜像
└── docker-compose.yml     # Docker Compose 配置
```

## 下一步

- 阅读 [TDD文档](../../docs/TDD/M5-板块管理服务/)
- 查看 [API设计](../../docs/TDD/M5-板块管理服务/02-API接口设计.md)
- 了解 [数据库设计](../../docs/TDD/M5-板块管理服务/01-数据库设计.md)


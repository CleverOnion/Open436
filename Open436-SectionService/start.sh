#!/bin/bash

# M5 板块管理服务启动脚本

echo "==============================================="
echo "Open436 板块管理服务 (M5) 启动中..."
echo "==============================================="

# 等待数据库就绪
echo "⏳ 等待数据库连接..."
until PGPASSWORD=$DB_PASSWORD psql -h "$DB_HOST" -U "$DB_USER" -d "$DB_NAME" -c '\q' 2>/dev/null; do
  echo "数据库尚未就绪，等待中..."
  sleep 2
done

echo "✓ 数据库连接成功"

# 执行数据库迁移（如果需要）
echo "📦 检查数据库迁移..."
python manage.py makemigrations --noinput || true
python manage.py migrate --noinput || true

# 收集静态文件
echo "📁 收集静态文件..."
python manage.py collectstatic --noinput || true

# 注册服务到 Consul
echo "📡 注册服务到 Consul..."
python manage.py register_consul || echo "⚠️  Consul 注册失败（非致命错误）"

# 启动服务
echo "🚀 启动服务..."
gunicorn config.wsgi:application \
    --bind 0.0.0.0:${SERVICE_PORT:-8005} \
    --workers ${GUNICORN_WORKERS:-4} \
    --timeout 120 \
    --access-logfile - \
    --error-logfile - \
    --log-level ${LOG_LEVEL:-info}


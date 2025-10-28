#!/bin/bash

# ========================================
# Open436 Kong Gateway 配置脚本
# ========================================
# 用途: 使用 Admin API 配置 Kong Gateway
# 使用: ./kong-config.sh
# ========================================

set -e

KONG_ADMIN_URL=${KONG_ADMIN_URL:-http://localhost:8001}

echo "=========================================="
echo "Open436 Kong Gateway 配置"
echo "=========================================="
echo "Kong Admin API: $KONG_ADMIN_URL"
echo ""

# ========================================
# 1. 注册 M1 认证服务
# ========================================
echo ">>> 注册 M1 认证服务..."
SERVICE_M1=$(curl -s -X POST $KONG_ADMIN_URL/services \
  --data name=auth-service \
  --data protocol=http \
  --data host=auth-service \
  --data port=8001 \
  --data path=/ \
  --data retries=3 \
  --data connect_timeout=5000 \
  --data write_timeout=60000 \
  --data read_timeout=60000 \
  | jq -r '.id')

echo "✓ M1 认证服务已注册: $SERVICE_M1"

# M1 公开路由（登录接口）
echo ">>> 配置 M1 公开路由（登录）..."
curl -s -X POST $KONG_ADMIN_URL/services/auth-service/routes \
  --data name=auth-public \
  --data 'paths[]=/api/auth/login' \
  --data strip_path=false \
  --data 'methods[]=POST' \
  > /dev/null

echo "✓ M1 公开路由已配置"

# M1 受保护路由（需要 Sa-Token 验证）
echo ">>> 配置 M1 受保护路由..."
curl -s -X POST $KONG_ADMIN_URL/services/auth-service/routes \
  --data name=auth-protected \
  --data 'paths[]=/api/auth/logout' \
  --data 'paths[]=/api/auth/verify' \
  --data 'paths[]=/api/auth/current' \
  --data 'paths[]=/api/auth/password' \
  --data 'paths[]=/api/auth/users' \
  --data strip_path=false \
  --data 'methods[]=GET' \
  --data 'methods[]=POST' \
  --data 'methods[]=PUT' \
  --data 'methods[]=DELETE' \
  > /dev/null

echo "✓ M1 受保护路由已配置"

# ========================================
# 2. 注册 M7 文件存储服务
# ========================================
echo ""
echo ">>> 注册 M7 文件存储服务..."
SERVICE_M7=$(curl -s -X POST $KONG_ADMIN_URL/services \
  --data name=file-service \
  --data protocol=http \
  --data host=file-service \
  --data port=8007 \
  --data path=/ \
  --data retries=2 \
  --data connect_timeout=5000 \
  --data write_timeout=60000 \
  --data read_timeout=60000 \
  | jq -r '.id')

echo "✓ M7 文件存储服务已注册: $SERVICE_M7"

# M7 公开路由（文件信息查询 - 无需认证）
echo ">>> 配置 M7 公开路由（文件查询）..."
ROUTE_FILE_PUBLIC=$(curl -s -X POST $KONG_ADMIN_URL/services/file-service/routes \
  --data name=file-public \
  --data 'paths[]=/api/files/batch-info' \
  --data strip_path=false \
  --data 'methods[]=POST' \
  | jq -r '.id')

echo "✓ M7 公开路由已配置: $ROUTE_FILE_PUBLIC"

# M7 文件信息查询路由（使用正则匹配 UUID）
echo ">>> 配置 M7 文件信息查询路由..."
curl -s -X POST $KONG_ADMIN_URL/services/file-service/routes \
  --data name=file-info-get \
  --data 'paths[]=/api/files/[a-f0-9-]{36}' \
  --data 'paths[]=/api/files/[a-f0-9-]{36}/url' \
  --data strip_path=false \
  --data 'methods[]=GET' \
  --data regex_priority=10 \
  > /dev/null

echo "✓ M7 文件信息查询路由已配置"

# M7 受保护路由（文件上传、标记使用 - 需要认证）
echo ">>> 配置 M7 受保护路由（上传、标记）..."
ROUTE_FILE_PROTECTED=$(curl -s -X POST $KONG_ADMIN_URL/services/file-service/routes \
  --data name=file-protected \
  --data 'paths[]=/api/files/upload' \
  --data 'paths[]=/api/files/[a-f0-9-]{36}/mark-used' \
  --data 'paths[]=/api/files/[a-f0-9-]{36}/mark-unused' \
  --data strip_path=false \
  --data 'methods[]=POST' \
  --data regex_priority=20 \
  | jq -r '.id')

echo "✓ M7 受保护路由已配置: $ROUTE_FILE_PROTECTED"

# 为受保护路由添加自定义请求转换（注入用户信息）
echo ">>> 为 M7 受保护路由添加请求转换插件..."
curl -s -X POST $KONG_ADMIN_URL/routes/$ROUTE_FILE_PROTECTED/plugins \
  --data name=request-transformer \
  --data config.add.headers=X-Kong-Gateway:true \
  > /dev/null

# 添加限流（防止上传滥用）
curl -s -X POST $KONG_ADMIN_URL/routes/$ROUTE_FILE_PROTECTED/plugins \
  --data name=rate-limiting \
  --data config.minute=20 \
  --data config.hour=100 \
  --data config.policy=local \
  > /dev/null

echo "✓ M7 受保护路由插件已配置"

# M7 管理员路由（删除、统计、清理 - 需要管理员权限）
echo ">>> 配置 M7 管理员路由..."
ROUTE_FILE_ADMIN=$(curl -s -X POST $KONG_ADMIN_URL/services/file-service/routes \
  --data name=file-admin \
  --data 'paths[]=/api/files/statistics' \
  --data 'paths[]=/api/files/cleanup' \
  --data strip_path=false \
  --data 'methods[]=GET' \
  --data 'methods[]=POST' \
  --data 'methods[]=DELETE' \
  --data regex_priority=30 \
  | jq -r '.id')

echo "✓ M7 管理员路由已配置: $ROUTE_FILE_ADMIN"

# 为管理员路由添加请求转换
curl -s -X POST $KONG_ADMIN_URL/routes/$ROUTE_FILE_ADMIN/plugins \
  --data name=request-transformer \
  --data config.add.headers=X-Kong-Gateway:true \
  --data config.add.headers=X-Requires-Admin:true \
  > /dev/null

echo "✓ M7 管理员路由插件已配置"

# M7 文件删除路由（DELETE /api/files/:id）
echo ">>> 配置 M7 文件删除路由..."
curl -s -X POST $KONG_ADMIN_URL/services/file-service/routes \
  --data name=file-delete \
  --data 'paths[]=/api/files/[a-f0-9-]{36}' \
  --data strip_path=false \
  --data 'methods[]=DELETE' \
  --data regex_priority=25 \
  > /dev/null

echo "✓ M7 文件删除路由已配置"

# ========================================
# 3. 全局插件配置
# ========================================
echo ""
echo ">>> 配置全局插件..."

# CORS 插件
curl -s -X POST $KONG_ADMIN_URL/plugins \
  --data name=cors \
  --data config.origins=http://localhost:3000 \
  --data config.origins=http://localhost:8080 \
  --data config.origins=https://open436.com \
  --data 'config.methods=GET' \
  --data 'config.methods=POST' \
  --data 'config.methods=PUT' \
  --data 'config.methods=DELETE' \
  --data 'config.methods=OPTIONS' \
  --data 'config.methods=PATCH' \
  --data 'config.headers=Authorization' \
  --data 'config.headers=Content-Type' \
  --data 'config.headers=X-User-Id' \
  --data 'config.headers=X-User-Role' \
  --data 'config.exposed_headers=X-Auth-Token' \
  --data 'config.exposed_headers=X-Total-Count' \
  --data config.credentials=true \
  --data config.max_age=3600 \
  > /dev/null

echo "✓ CORS 插件已配置"

# 全局限流
curl -s -X POST $KONG_ADMIN_URL/plugins \
  --data name=rate-limiting \
  --data config.minute=100 \
  --data config.hour=1000 \
  --data config.policy=local \
  --data config.fault_tolerant=true \
  > /dev/null

echo "✓ 全局限流插件已配置"

# 请求大小限制（10 MB，适配文件上传）
curl -s -X POST $KONG_ADMIN_URL/plugins \
  --data name=request-size-limiting \
  --data config.allowed_payload_size=10 \
  --data config.size_unit=megabytes \
  > /dev/null

echo "✓ 请求大小限制插件已配置"

# ========================================
# 4. 健康检查路由（无需认证）
# ========================================
echo ""
echo ">>> 配置健康检查路由..."

# M1 健康检查
curl -s -X POST $KONG_ADMIN_URL/services/auth-service/routes \
  --data name=auth-health \
  --data 'paths[]=/health' \
  --data strip_path=false \
  --data 'methods[]=GET' \
  > /dev/null

# M7 健康检查
curl -s -X POST $KONG_ADMIN_URL/services/file-service/routes \
  --data name=file-health \
  --data 'paths[]=/health' \
  --data strip_path=false \
  --data 'methods[]=GET' \
  > /dev/null

echo "✓ 健康检查路由已配置"

# ========================================
# 完成
# ========================================
echo ""
echo "=========================================="
echo "Kong Gateway 配置完成！"
echo "=========================================="
echo ""
echo "验证配置:"
echo "  curl $KONG_ADMIN_URL/services"
echo "  curl $KONG_ADMIN_URL/routes"
echo "  curl $KONG_ADMIN_URL/plugins"
echo ""
echo "测试接口:"
echo "  # M1 登录"
echo "  curl -X POST http://localhost:8000/api/auth/login -d '{...}'"
echo ""
echo "  # M7 健康检查"
echo "  curl http://localhost:8000/health"
echo ""
echo "  # M7 上传文件（需要 Token）"
echo "  curl -X POST http://localhost:8000/api/files/upload \\"
echo "    -H 'Authorization: Bearer YOUR_TOKEN' \\"
echo "    -F 'file=@test.jpg' -F 'file_type=avatar'"
echo ""


#!/bin/bash
# Kong 开发环境配置脚本（deploy/dev）
# 配置 Kong 路由到宿主机运行的服务

set -euo pipefail

KONG_ADMIN="http://localhost:8001"

# Windows/Mac Docker Desktop 使用 host.docker.internal；Linux 可改为 172.17.0.1
HOST_ADDR="host.docker.internal"

echo "========================================="
echo "Configuring Kong for Development Mode"
echo "========================================="
echo ""
echo "Backend services running on host: ${HOST_ADDR}"
echo ""

# 等待 Kong 就绪
echo "Waiting for Kong to be ready..."
until curl -s http://localhost:8001/status > /dev/null 2>&1; do
    echo "Kong is not ready yet, waiting..."
    sleep 2
done
echo "Kong is ready!"
echo ""

# 删除已存在的配置（如果有）
echo "Cleaning up existing configurations..."
curl -s -X DELETE http://localhost:8001/services/auth-service 2>/dev/null || true
curl -s -X DELETE http://localhost:8001/services/file-service 2>/dev/null || true
sleep 1

# 1. 创建 M1 认证服务
echo "Step 1: Creating auth-service..."
curl -i -X POST $KONG_ADMIN/services \
  --data name=auth-service \
  --data url=http://${HOST_ADDR}:8081

echo ""
echo "Creating route for auth-service..."
curl -i -X POST $KONG_ADMIN/services/auth-service/routes \
  --data "paths[]=/api/auth" \
  --data strip_path=false

echo ""
echo ""

# 2. 创建 M7 文件服务
echo "Step 2: Creating file-service..."
curl -i -X POST $KONG_ADMIN/services \
  --data name=file-service \
  --data url=http://${HOST_ADDR}:8007

echo ""
echo "Creating route for file-service..."
curl -i -X POST $KONG_ADMIN/services/file-service/routes \
  --data "paths[]=/api/files" \
  --data strip_path=false

echo ""
echo ""

# 3. 启用 Sa-Token 认证插件（文件服务需要鉴权）
echo "Step 3: Enabling satoken-auth plugin for file-service..."
curl -i -X POST $KONG_ADMIN/services/file-service/plugins \
  --data name=satoken-auth \
  --data config.auth_service_url=http://${HOST_ADDR}:8081

echo ""
echo ""
echo "========================================="
echo "Kong Configuration Complete!"
echo "========================================="
echo ""
echo "Services registered:"
echo "  - auth-service:  http://${HOST_ADDR}:8081 → http://localhost:8000/api/auth/*"
echo "  - file-service:  http://${HOST_ADDR}:8007 → http://localhost:8000/api/files/*"
echo ""
echo "Verification:"
echo "  - Kong Admin API: curl http://localhost:8001/services"
echo "  - Kong Routes:    curl http://localhost:8001/routes"
echo ""
echo "Test login through Kong:"
echo "  curl -X POST http://localhost:8000/api/auth/login \\\n+    -H 'Content-Type: application/json' \\\n+    -d '{"username":"alice","password":"password123"}'"
echo ""



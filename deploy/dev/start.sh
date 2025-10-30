#!/bin/bash
# 开发环境启动脚本（deploy/dev）
# 仅启动 Docker 基础设施，不启动后端服务

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

echo "========================================="
echo "Starting Open436 Development Environment"
echo "========================================="
echo ""
echo "This will start:"
echo "  - Consul (Service Registry)"
echo "  - Kong Gateway (API Gateway)"
echo "  - PostgreSQL (Database)"
echo "  - Redis (Cache)"
echo "  - Minio (Object Storage)"
echo ""
echo "Backend services (M1, M7) should be run in your IDE"
echo ""

# 1. 启动基础设施
echo "Step 1: Starting infrastructure services..."
docker-compose -f ./docker-compose.yml up -d consul postgres redis minio kong-database

# 等待数据库就绪
echo "Waiting for databases to be ready..."
sleep 10

# 2. 启动 Kong
echo "Step 2: Starting Kong Gateway..."
docker-compose -f ./docker-compose.yml up -d kong

# 等待 Kong 就绪
echo "Waiting for Kong to be ready..."
sleep 8

# 3. 配置 Kong
echo "Step 3: Configuring Kong routes and plugins..."
bash ./kong-config.sh

echo ""
echo "========================================="
echo "Development Environment Ready!"
echo "========================================="
echo ""
echo "Services Status:"
docker-compose -f ./docker-compose.yml ps
echo ""
echo "Access Points:"
echo "  - Consul UI:       http://localhost:8500"
echo "  - Kong Admin API:  http://localhost:8001"
echo "  - Kong Proxy:      http://localhost:8000"
echo "  - Minio Console:   http://localhost:9001 (minioadmin/minioadmin)"
echo "  - PostgreSQL:      localhost:5432 (open436/open436)"
echo "  - Redis:           localhost:6379"
echo ""
echo "Next Steps:"
echo "  1. Start M1 Auth Service in IntelliJ IDEA (port 8081)"
echo "  2. Start M7 File Service in VS Code/Terminal (port 8007)"
echo "  3. Check Consul UI to verify services are registered"
echo "  4. Test login: curl -X POST http://localhost:8000/api/auth/login \\\n+       -H 'Content-Type: application/json' \\\n+       -d '{"username":"alice","password":"password123"}'"
echo ""
echo "To stop: docker-compose -f ./docker-compose.yml down"
echo ""



#!/bin/bash

# 板块管理服务快速启动脚本

echo "🚀 板块管理服务 - 快速启动"
echo "================================"

# 检查Docker是否安装
if ! command -v docker &> /dev/null; then
    echo "❌ Docker未安装，请先安装Docker"
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose未安装，请先安装Docker Compose"
    exit 1
fi

# 检查.env文件
if [ ! -f .env ]; then
    echo "📝 创建环境配置文件..."
    cp .env.example .env
    echo "✅ .env文件已创建，请根据需要修改配置"
fi

# 停止已有服务
echo "🛑 停止已有服务..."
docker-compose down

# 构建镜像
echo "🔨 构建Docker镜像..."
docker-compose build

# 启动服务
echo "▶️  启动服务..."
docker-compose up -d

# 等待服务启动
echo "⏳ 等待服务启动..."
sleep 5

# 检查服务状态
echo "🔍 检查服务状态..."
docker-compose ps

# 健康检查
echo "🏥 健康检查..."
if curl -s http://localhost:8005/health > /dev/null; then
    echo "✅ 服务启动成功！"
    echo ""
    echo "📚 访问以下地址："
    echo "   - API文档: http://localhost:8005/docs"
    echo "   - 健康检查: http://localhost:8005/health"
    echo "   - 板块列表: http://localhost:8005/api/v1/sections/"
    echo ""
    echo "📋 查看日志："
    echo "   docker-compose logs -f section-service"
    echo ""
    echo "🛑 停止服务："
    echo "   docker-compose down"
else
    echo "❌ 服务启动失败，请查看日志："
    echo "   docker-compose logs"
fi


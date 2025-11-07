@echo off
REM 板块管理服务快速启动脚本 (Windows)

echo 🚀 板块管理服务 - 快速启动
echo ================================

REM 检查Docker是否安装
where docker >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Docker未安装，请先安装Docker Desktop
    pause
    exit /b 1
)

REM 检查.env文件
if not exist .env (
    echo 📝 创建环境配置文件...
    copy .env.example .env
    echo ✅ .env文件已创建，请根据需要修改配置
)

REM 停止已有服务
echo 🛑 停止已有服务...
docker-compose down

REM 构建镜像
echo 🔨 构建Docker镜像...
docker-compose build

REM 启动服务
echo ▶️  启动服务...
docker-compose up -d

REM 等待服务启动
echo ⏳ 等待服务启动...
timeout /t 5 /nobreak >nul

REM 检查服务状态
echo 🔍 检查服务状态...
docker-compose ps

REM 健康检查
echo 🏥 健康检查...
curl -s http://localhost:8005/health >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo ✅ 服务启动成功！
    echo.
    echo 📚 访问以下地址：
    echo    - API文档: http://localhost:8005/docs
    echo    - 健康检查: http://localhost:8005/health
    echo    - 板块列表: http://localhost:8005/api/v1/sections/
    echo.
    echo 📋 查看日志：
    echo    docker-compose logs -f section-service
    echo.
    echo 🛑 停止服务：
    echo    docker-compose down
) else (
    echo ❌ 服务启动失败，请查看日志：
    echo    docker-compose logs
)

echo.
pause


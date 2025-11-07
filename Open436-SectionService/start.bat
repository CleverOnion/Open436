@echo off
REM M5 板块管理服务启动脚本 (Windows)

echo ===============================================
echo Open436 板块管理服务 (M5) 启动中...
echo ===============================================

REM 激活虚拟环境（如果存在）
if exist venv\Scripts\activate.bat (
    echo 激活虚拟环境...
    call venv\Scripts\activate.bat
)

REM 注册服务到 Consul
echo 注册服务到 Consul...
python manage.py register_consul

REM 启动开发服务器
echo 启动开发服务器...
python manage.py runserver 0.0.0.0:8005

pause


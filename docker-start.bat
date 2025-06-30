@echo off
chcp 65001 >nul
echo ========================================
echo    WebSocket动态数据模拟器 - Docker启动
echo ========================================
echo.

REM 检查Docker是否安装
docker --version >nul 2>&1
if errorlevel 1 (
    echo ❌ 错误：未检测到Docker，请先安装Docker Desktop
    echo 下载地址：https://www.docker.com/products/docker-desktop
    pause
    exit /b 1
)

REM 检查Docker是否运行
docker info >nul 2>&1
if errorlevel 1 (
    echo ❌ 错误：Docker未运行，请启动Docker Desktop
    pause
    exit /b 1
)

echo ✅ Docker环境检查通过
echo.

REM 创建日志目录
if not exist "logs" mkdir logs
echo ✅ 日志目录已准备

echo.
echo 🚀 正在启动WebSocket服务...
echo.

REM 启动服务
docker-compose up -d

if errorlevel 1 (
    echo ❌ 启动失败，请检查错误信息
    pause
    exit /b 1
)

echo.
echo ✅ 服务启动成功！
echo.
echo 📋 服务信息：
echo    - 容器名称：websocket-simulator
echo    - 访问地址：http://localhost:1883/test-client.html
echo    - WebSocket：ws://localhost:1883/
echo    - API接口：http://localhost:1883/api/connection-count
echo.

echo 📝 常用命令：
echo    - 查看状态：docker-compose ps
echo    - 查看日志：docker-compose logs -f
echo    - 停止服务：docker-compose down
echo    - 重启服务：docker-compose restart
echo.

echo 🎉 现在可以打开浏览器访问测试页面了！
echo.
pause 
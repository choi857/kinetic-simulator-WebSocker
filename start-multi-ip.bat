@echo off
chcp 65001 >nul
echo ========================================
echo WebSocket动态数据模拟器 - 多IP启动脚本
echo ========================================
echo.

REM 检查Docker是否安装
docker --version >nul 2>&1
if errorlevel 1 (
    echo [错误] 未检测到Docker，请先安装Docker Desktop
    pause
    exit /b 1
)

REM 检查Docker Compose是否可用
docker-compose --version >nul 2>&1
if errorlevel 1 (
    echo [错误] 未检测到Docker Compose，请确保Docker Desktop已正确安装
    pause
    exit /b 1
)

echo [信息] 检测到Docker环境，开始启动多IP服务...
echo.

REM 创建日志目录
if not exist "logs\192.168.2.114" mkdir "logs\192.168.2.114"
if not exist "logs\192.168.2.115" mkdir "logs\192.168.2.115"

echo [信息] 正在启动多IP WebSocket服务...
echo [信息] 服务1: 192.168.2.114:1883 (容器内1883端口)
echo [信息] 服务2: 192.168.2.115:1883 (容器内1883端口，主机1885端口)
echo.

REM 启动多IP服务
docker-compose -f docker-compose-multi-ip.yml up -d

if errorlevel 1 (
    echo [错误] 启动服务失败，请检查配置和网络设置
    pause
    exit /b 1
)

echo.
echo [成功] 多IP WebSocket服务启动成功！
echo.
echo 服务状态:
echo - 192.168.2.114:1883 (容器端口映射到主机1883)
echo - 192.168.2.115:1883 (容器端口映射到主机1885)
echo.
echo 访问地址:
echo - 前端界面: http://192.168.2.114:1883/test-client.html
echo - 前端界面: http://192.168.2.115:1885/test-client.html
echo.
echo 管理命令:
echo - 查看服务状态: docker-compose -f docker-compose-multi-ip.yml ps
echo - 查看日志: docker-compose -f docker-compose-multi-ip.yml logs -f
echo - 停止服务: docker-compose -f docker-compose-multi-ip.yml down
echo.
echo 按任意键查看服务状态...
pause >nul

REM 显示服务状态
docker-compose -f docker-compose-multi-ip.yml ps

echo.
echo 按任意键退出...
pause >nul 
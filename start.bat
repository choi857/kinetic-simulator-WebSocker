@echo off
echo 启动模拟器WebSocket服务...
echo 端口: 1883
echo WebSocket端点: /
echo.

REM 检查Java版本
java -version
if %errorlevel% neq 0 (
    echo 错误: 未找到Java环境，请确保已安装JDK 1.8
    pause
    exit /b 1
)

REM 启动应用
echo 正在启动应用...
call mvnw.cmd spring-boot:run

pause 
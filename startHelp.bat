@echo off
chcp 936
setlocal enabledelayedexpansion

set JAR=kinetic-simulator-WebSocker-0.0.1-SNAPSHOT.jar

:MENU
echo.
echo ========== WebSocket多IP多实例管理 ==========
echo 1. 启动实例
echo 2. 停止实例
echo 3. 查看所有实例
echo 4. 退出
echo ============================================
set /p choice=请选择操作（1-4）:

if "%choice%"=="1" goto START
if "%choice%"=="2" goto STOP
if "%choice%"=="3" goto LIST
if "%choice%"=="4" goto END
echo 请输入有效选项！
goto MENU

:START
set /p ip=请输入要监听的本机IP（如192.168.1.10）:
set /p port=请输入端口号（默认1883）:
if "%port%"=="" set port=1883
set configFile=application-%ip%.properties
echo server.address=%ip%> %configFile%
echo server.port=%port%>> %configFile%
set logFile=log-%ip%.txt
REM 检查是否已启动
for /f "tokens=2 delims=," %%a in ('tasklist /FI "IMAGENAME eq java.exe" /FO CSV /NH') do (
    wmic process where "ProcessId=%%a" get CommandLine | findstr /I "%ip%" >nul
    if !errorlevel! == 0 (
        echo 实例 %ip%:%port% 已在运行！
        goto MENU
    )
)
start "ws-%ip%" javaw -jar %JAR% --spring.config.location=%configFile% > %logFile% 2>&1
echo 已启动实例：%ip%:%port%
goto MENU

:STOP
set /p ip=请输入要停止的实例IP:
set /p port=请输入端口号（默认1883）:
if "%port%"=="" set port=1883
for /f "tokens=2 delims=," %%a in ('tasklist /FI "IMAGENAME eq java.exe" /FO CSV /NH') do (
    wmic process where "ProcessId=%%a" get CommandLine | findstr /I "%ip%" >nul
    if !errorlevel! == 0 (
        taskkill /PID %%a /F
        echo 已停止实例 %ip%:%port% (PID: %%a)
    )
)
goto MENU

:LIST
echo.
echo 当前运行的实例：
for /f "tokens=2 delims=," %%a in ('tasklist /FI "IMAGENAME eq java.exe" /FO CSV /NH') do (
    for /f "delims=" %%b in ('wmic process where "ProcessId=%%a" get CommandLine ^| findstr /I "%JAR%"') do (
        set "cmd=%%b"
        for /f "tokens=2 delims== " %%c in ("!cmd!") do (
            echo PID: %%a  %%b
        )
    )
)
goto MENU

:END
echo 脚本已退出。
exit /b
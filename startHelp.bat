@echo off
chcp 936
setlocal enabledelayedexpansion

set JAR=kinetic-simulator-WebSocker-0.0.1-SNAPSHOT.jar

:MENU
echo.
echo ========== WebSocket��IP��ʵ������ ==========
echo 1. ����ʵ��
echo 2. ֹͣʵ��
echo 3. �鿴����ʵ��
echo 4. �˳�
echo ============================================
set /p choice=��ѡ�������1-4��:

if "%choice%"=="1" goto START
if "%choice%"=="2" goto STOP
if "%choice%"=="3" goto LIST
if "%choice%"=="4" goto END
echo ��������Чѡ�
goto MENU

:START
set /p ip=������Ҫ�����ı���IP����192.168.1.10��:
set /p port=������˿ںţ�Ĭ��1883��:
if "%port%"=="" set port=1883
set configFile=application-%ip%.properties
echo server.address=%ip%> %configFile%
echo server.port=%port%>> %configFile%
set logFile=log-%ip%.txt
REM ����Ƿ�������
for /f "tokens=2 delims=," %%a in ('tasklist /FI "IMAGENAME eq java.exe" /FO CSV /NH') do (
    wmic process where "ProcessId=%%a" get CommandLine | findstr /I "%ip%" >nul
    if !errorlevel! == 0 (
        echo ʵ�� %ip%:%port% �������У�
        goto MENU
    )
)
start "ws-%ip%" javaw -jar %JAR% --spring.config.location=%configFile% > %logFile% 2>&1
echo ������ʵ����%ip%:%port%
goto MENU

:STOP
set /p ip=������Ҫֹͣ��ʵ��IP:
set /p port=������˿ںţ�Ĭ��1883��:
if "%port%"=="" set port=1883
for /f "tokens=2 delims=," %%a in ('tasklist /FI "IMAGENAME eq java.exe" /FO CSV /NH') do (
    wmic process where "ProcessId=%%a" get CommandLine | findstr /I "%ip%" >nul
    if !errorlevel! == 0 (
        taskkill /PID %%a /F
        echo ��ֹͣʵ�� %ip%:%port% (PID: %%a)
    )
)
goto MENU

:LIST
echo.
echo ��ǰ���е�ʵ����
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
echo �ű����˳���
exit /b
@echo off
chcp 65001 >nul
echo ========================================
echo Docker构建修复脚本
echo ========================================
echo.

echo [信息] 检查Maven Wrapper文件...
if not exist "mvnw" (
    echo [错误] 未找到mvnw文件
    echo [信息] 尝试使用备用Dockerfile...
    if exist "Dockerfile.maven" (
        echo [信息] 使用Dockerfile.maven进行构建...
        docker build -f Dockerfile.maven -t websocket-simulator .
        goto :end
    ) else (
        echo [错误] 未找到备用Dockerfile
        pause
        exit /b 1
    )
)

if not exist "mvnw.cmd" (
    echo [错误] 未找到mvnw.cmd文件
    echo [信息] 尝试使用备用Dockerfile...
    if exist "Dockerfile.maven" (
        echo [信息] 使用Dockerfile.maven进行构建...
        docker build -f Dockerfile.maven -t websocket-simulator .
        goto :end
    ) else (
        echo [错误] 未找到备用Dockerfile
        pause
        exit /b 1
    )
)

if not exist ".mvn" (
    echo [错误] 未找到.mvn目录
    echo [信息] 尝试使用备用Dockerfile...
    if exist "Dockerfile.maven" (
        echo [信息] 使用Dockerfile.maven进行构建...
        docker build -f Dockerfile.maven -t websocket-simulator .
        goto :end
    ) else (
        echo [错误] 未找到备用Dockerfile
        pause
        exit /b 1
    )
)

echo [信息] Maven Wrapper文件检查通过
echo [信息] 开始构建Docker镜像...

REM 清理旧的构建缓存
docker builder prune -f

REM 构建镜像
docker build -t websocket-simulator .

if errorlevel 1 (
    echo [错误] Docker构建失败
    echo [信息] 尝试使用备用Dockerfile...
    if exist "Dockerfile.maven" (
        echo [信息] 使用Dockerfile.maven进行构建...
        docker build -f Dockerfile.maven -t websocket-simulator .
        if errorlevel 1 (
            echo [错误] 备用Dockerfile构建也失败
            pause
            exit /b 1
        )
    ) else (
        echo [错误] 未找到备用Dockerfile
        pause
        exit /b 1
    )
)

:end
echo.
echo [成功] Docker镜像构建完成！
echo [信息] 镜像名称: websocket-simulator
echo.
echo 接下来可以运行:
echo - 单IP部署: docker-compose up -d
echo - 多IP部署: start-multi-ip.bat
echo.
pause 
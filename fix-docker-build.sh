#!/bin/bash

echo "========================================"
echo "Docker构建修复脚本"
echo "========================================"
echo

echo "[信息] 检查Maven Wrapper文件..."

if [ ! -f "mvnw" ]; then
    echo "[错误] 未找到mvnw文件"
    echo "[信息] 尝试使用备用Dockerfile..."
    if [ -f "Dockerfile.maven" ]; then
        echo "[信息] 使用Dockerfile.maven进行构建..."
        docker build -f Dockerfile.maven -t websocket-simulator .
        exit $?
    else
        echo "[错误] 未找到备用Dockerfile"
        exit 1
    fi
fi

if [ ! -f "mvnw.cmd" ]; then
    echo "[错误] 未找到mvnw.cmd文件"
    echo "[信息] 尝试使用备用Dockerfile..."
    if [ -f "Dockerfile.maven" ]; then
        echo "[信息] 使用Dockerfile.maven进行构建..."
        docker build -f Dockerfile.maven -t websocket-simulator .
        exit $?
    else
        echo "[错误] 未找到备用Dockerfile"
        exit 1
    fi
fi

if [ ! -d ".mvn" ]; then
    echo "[错误] 未找到.mvn目录"
    echo "[信息] 尝试使用备用Dockerfile..."
    if [ -f "Dockerfile.maven" ]; then
        echo "[信息] 使用Dockerfile.maven进行构建..."
        docker build -f Dockerfile.maven -t websocket-simulator .
        exit $?
    else
        echo "[错误] 未找到备用Dockerfile"
        exit 1
    fi
fi

echo "[信息] Maven Wrapper文件检查通过"
echo "[信息] 开始构建Docker镜像..."

# 清理旧的构建缓存
docker builder prune -f

# 构建镜像
docker build -t websocket-simulator .

if [ $? -ne 0 ]; then
    echo "[错误] Docker构建失败"
    echo "[信息] 尝试使用备用Dockerfile..."
    if [ -f "Dockerfile.maven" ]; then
        echo "[信息] 使用Dockerfile.maven进行构建..."
        docker build -f Dockerfile.maven -t websocket-simulator .
        if [ $? -ne 0 ]; then
            echo "[错误] 备用Dockerfile构建也失败"
            exit 1
        fi
    else
        echo "[错误] 未找到备用Dockerfile"
        exit 1
    fi
fi

echo
echo "[成功] Docker镜像构建完成！"
echo "[信息] 镜像名称: websocket-simulator"
echo
echo "接下来可以运行:"
echo "- 单IP部署: docker-compose up -d"
echo "- 多IP部署: ./start-multi-ip.sh"
echo 
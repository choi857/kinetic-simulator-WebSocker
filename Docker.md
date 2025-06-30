# Docker 使用指南

## 📋 目录
- [什么是Docker](#什么是docker)
- [安装Docker](#安装docker)
- [快速开始](#快速开始)
- [常用命令](#常用命令)
- [故障排除](#故障排除)
- [进阶使用](#进阶使用)

---

## 🐳 什么是Docker

Docker是一个开源的容器化平台，可以将应用程序和其依赖打包成一个轻量级的、可移植的容器。

### 为什么使用Docker？
- ✅ **环境一致**：在任何地方运行都相同
- ✅ **快速部署**：一键启动，无需复杂配置
- ✅ **资源隔离**：不同应用互不影响
- ✅ **易于管理**：统一的启动、停止、更新方式

---

## 💻 安装Docker

### Windows用户
1. 下载 [Docker Desktop for Windows](https://www.docker.com/products/docker-desktop)
2. 双击安装包，按提示完成安装
3. 重启电脑
4. 启动Docker Desktop

### macOS用户
1. 下载 [Docker Desktop for Mac](https://www.docker.com/products/docker-desktop)
2. 拖拽到Applications文件夹
3. 启动Docker Desktop

### Linux用户（Ubuntu）
```bash
# 更新包索引
sudo apt-get update

# 安装必要的包
sudo apt-get install apt-transport-https ca-certificates curl gnupg lsb-release

# 添加Docker官方GPG密钥
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg

# 设置稳定版仓库
echo "deb [arch=amd64 signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# 安装Docker Engine
sudo apt-get update
sudo apt-get install docker-ce docker-ce-cli containerd.io

# 启动Docker
sudo systemctl start docker
sudo systemctl enable docker

# 将当前用户添加到docker组（可选，避免每次都要sudo）
sudo usermod -aG docker $USER
```

### 验证安装
打开终端/命令提示符，运行：
```bash
docker --version
docker-compose --version
```
如果显示版本信息，说明安装成功！

---

## 🚀 快速开始

### 方法一：使用Docker Compose（推荐）

1. **打开终端/命令提示符**
   ```bash
   # 进入项目目录
   cd kinetic-simulator-WebSocker
   ```

2. **一键启动服务**
   ```bash
   docker-compose up -d
   ```
   - `-d` 表示后台运行
   - 首次运行会自动下载镜像和构建

3. **查看服务状态**
   ```bash
   docker-compose ps
   ```

4. **访问应用**
   - 打开浏览器访问：`http://localhost:1883/test-client.html`
   - WebSocket连接地址：`ws://localhost:1883/`

### 方法二：使用Docker命令

1. **构建镜像**
   ```bash
   docker build -t websocket-simulator .
   ```

2. **运行容器**
   ```bash
   docker run -d -p 1883:1883 --name websocket-simulator websocket-simulator
   ```

---

## 📝 常用命令

### Docker Compose命令

| 命令 | 说明 |
|------|------|
| `docker-compose up -d` | 启动服务（后台运行） |
| `docker-compose up` | 启动服务（前台运行，可看日志） |
| `docker-compose down` | 停止并删除服务 |
| `docker-compose restart` | 重启服务 |
| `docker-compose ps` | 查看服务状态 |
| `docker-compose logs` | 查看日志 |
| `docker-compose logs -f` | 实时查看日志 |
| `docker-compose build` | 重新构建镜像 |

### Docker命令

| 命令 | 说明 |
|------|------|
| `docker ps` | 查看运行中的容器 |
| `docker ps -a` | 查看所有容器 |
| `docker images` | 查看本地镜像 |
| `docker logs 容器名` | 查看容器日志 |
| `docker stop 容器名` | 停止容器 |
| `docker start 容器名` | 启动容器 |
| `docker rm 容器名` | 删除容器 |
| `docker rmi 镜像名` | 删除镜像 |

---

## 🔧 故障排除

### 1. 端口被占用
**错误信息**：`Bind for 0.0.0.0:1883 failed: port is already allocated`

**解决方案**：
```bash
# 查看占用端口的进程
netstat -ano | findstr :1883  # Windows
lsof -i :1883                 # macOS/Linux

# 停止占用端口的进程，或修改端口
# 编辑 docker-compose.yml，将 1883:1883 改为 1884:1883
```

### 2. 权限问题
**错误信息**：`Got permission denied while trying to connect to the Docker daemon`

**解决方案**：
```bash
# Linux用户
sudo usermod -aG docker $USER
# 重新登录或重启

# Windows/macOS用户
# 确保Docker Desktop正在运行
```

### 3. 镜像构建失败
**错误信息**：`failed to build: error building at step`

**解决方案**：
```bash
# 清理Docker缓存
docker system prune -a

# 重新构建
docker-compose build --no-cache
```

### 4. 容器启动失败
**查看详细错误**：
```bash
# 查看容器日志
docker-compose logs websocket-simulator

# 查看容器状态
docker-compose ps
```

### 5. 无法访问应用
**检查步骤**：
1. 确认容器正在运行：`docker-compose ps`
2. 确认端口映射正确：`docker port websocket-simulator`
3. 检查防火墙设置
4. 尝试访问：`http://localhost:1883/api/connection-count`

---

## 🎯 进阶使用

### 1. 自定义配置
编辑 `application-docker.properties` 文件，修改配置后重启服务：
```bash
docker-compose restart
```

### 2. 查看实时日志
```bash
# 查看所有服务的日志
docker-compose logs -f

# 查看特定服务的日志
docker-compose logs -f websocket-simulator
```

### 3. 进入容器内部
```bash
# 进入容器bash
docker exec -it websocket-simulator sh

# 查看容器内文件
ls -la
cat /app/logs/application.log
```

### 4. 备份和恢复
```bash
# 备份数据
docker cp websocket-simulator:/app/logs ./backup-logs

# 恢复数据
docker cp ./backup-logs websocket-simulator:/app/logs
```

### 5. 多环境部署
创建不同环境的配置文件：
```bash
# 开发环境
docker-compose -f docker-compose.yml -f docker-compose.dev.yml up

# 生产环境
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up
```

---

## 📊 监控和日志

### 查看资源使用
```bash
# 查看容器资源使用情况
docker stats websocket-simulator

# 查看镜像大小
docker images websocket-simulator
```

### 日志管理
```bash
# 查看应用日志
docker-compose logs websocket-simulator

# 清理日志
docker-compose logs --tail=100 websocket-simulator
```

---

## 🆘 获取帮助

### 常用帮助命令
```bash
# Docker帮助
docker --help
docker-compose --help

# 查看特定命令帮助
docker run --help
docker-compose up --help
```

### 在线资源
- [Docker官方文档](https://docs.docker.com/)
- [Docker Compose文档](https://docs.docker.com/compose/)
- [Docker Hub](https://hub.docker.com/)

---

## 🎉 恭喜！

你已经成功学会了Docker的基本使用！现在你可以：
- ✅ 一键启动WebSocket服务
- ✅ 管理容器生命周期
- ✅ 查看日志和监控
- ✅ 进行故障排除

如果遇到问题，请查看故障排除部分或联系技术支持。 
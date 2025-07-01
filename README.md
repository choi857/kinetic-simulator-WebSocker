# WebSocket动态数据模拟器

## 项目简介

这是一个基于Spring Boot的WebSocket动态数据模拟器，支持智能数据类型识别和交互式模板编辑。项目采用高内聚低耦合的设计原则，支持多客户端连接、实时数据推送和动态JSON模板配置。

## 环境要求

### Java版本
- **JDK版本**: 1.8
- **编译目标**: Java 1.8

### Maven版本
- **Maven版本**: 3.6.2
- **Maven Wrapper**: 已配置为使用Maven 3.6.2

### Spring Boot版本
- **Spring Boot**: 2.7.18 (兼容JDK 1.8)

## 功能特性

- ✅ **WebSocket实时通信** - 支持多客户端同时连接
- ✅ **动态JSON模板编辑** - 交互式字段类型配置
- ✅ **智能数据类型识别** - 根据字段名和值自动推断类型
- ✅ **字段限制配置** - 支持数字和日期类型的最大值最小值限制
- ✅ **连接状态监控** - 实时显示连接数和连接状态
- ✅ **前端测试界面** - 完整的Web界面，支持模板编辑和数据预览
- ✅ **REST API接口** - 提供连接数查询等API
- ✅ **自动数据推送** - 根据模板配置自动生成和推送随机数据
- 🔄 实时WebSocket数据推送
- 📊 动态JSON模板配置
- 🎲 随机数据生成
- 📅 支持多种数据类型（字符串、数字、布尔值、日期时间、数组等）
- ⚙️ 可配置的数据范围和限制
- 🎯 高级模式支持复杂嵌套结构
- 🐳 Docker容器化部署
- 🌐 多IP地址支持

## 构建和运行

### 使用Maven Wrapper (推荐)
```bash
# 清理并编译
./mvnw clean compile

# 运行测试
./mvnw test

# 打包
./mvnw package

# 运行应用
./mvnw spring-boot:run
```

### 使用本地Maven
确保您的本地Maven版本为3.6.2或兼容版本：
```bash
mvn clean compile
mvn test
mvn package
mvn spring-boot:run
```

## 服务配置

### 端口配置
- **服务端口**: 1883
- **WebSocket端点**: `/`
- **HTTP API**: `/api/*`

### 配置文件
`application.properties`:
```properties
# 服务器端口配置
server.port=1883

# WebSocket路径配置
websocket.endpoint.path=/

# 应用名称
spring.application.name=kinetic-simulator-websocket

# 日志配置
logging.level.com.example.kineticsimulatorwebsocker=INFO
logging.level.org.springframework.web.socket=DEBUG

# Jackson配置
spring.jackson.default-property-inclusion=non_null
```

## 使用指南

### 1. 启动服务
```bash
./mvnw spring-boot:run
```

### 2. 访问测试页面
打开浏览器访问：`http://localhost:1883/test-client.html`

### 3. WebSocket连接
- **连接地址**: `ws://localhost:1883/`
- **连接方式**: 点击页面上的"连接"按钮

### 4. 模板编辑流程
1. **输入JSON模板** - 在文本框中输入JSON格式的模板
2. **解析并编辑** - 点击按钮解析模板，系统会自动识别字段类型
3. **配置字段类型** - 为每个字段选择合适的数据类型
4. **设置字段限制** - 为数字和日期类型设置最大值最小值（可选）
5. **应用模板** - 点击按钮将配置发送到后端，开始接收数据

## 支持的数据类型

### 基础类型
- `string` - 字符串
- `int` - 整数
- `double` - 浮点数
- `boolean` - 布尔值
- `array` - 数组
- `object` - 对象

### 特殊格式
- `email` - 邮箱地址
- `phone` - 手机号码
- `date` - 日期时间
- `ip` - IP地址
- `url` - URL地址
- `uuid` - UUID标识符
- `name` - 中文姓名
- `color` - 颜色名称

### 范围限制类型
- `age` - 年龄 (1-100)
- `year` - 年份 (2000-2024)
- `month` - 月份 (1-12)
- `day` - 日期 (1-31)
- `hour` - 小时 (0-23)
- `minute` - 分钟 (0-59)
- `second` - 秒 (0-59)
- `port` - 端口 (1024-65535)
- `id` - ID (1-1000000)
- `price` - 价格 (0-1000)
- `rate` - 比率 (0-100%)
- `score` - 分数 (0-10)
- `temperature` - 温度 (-10-40)
- `latitude` - 纬度 (-90-90)
- `longitude` - 经度 (-180-180)

## 字段限制功能

### 功能说明
- 为**数字类型**和**日期类型**字段支持设置"最小值"和"最大值"
- 不填写时，后端会使用默认范围自动生成数据
- 填写后，后端会严格按照设置的区间生成随机数据

### 使用方法
1. 在字段类型配置界面，数字/日期类型字段会显示"最小值""最大值"输入框
2. 填写需要的区间（如年龄最小18，最大65）
3. 点击"应用模板"，后端会根据区间生成数据

### 示例
```json
{
  "user_id": 0,
  "age": 0,
  "score": 0.0,
  "birth_date": ""
}
```

可以设置：
- `user_id`: 最小=1000, 最大=9999
- `age`: 最小=18, 最大=65
- `score`: 最小=0.0, 最大=100.0
- `birth_date`: 最小=1990-01-01 00:00:00

## API接口

### 获取连接数
```bash
GET http://localhost:1883/api/connection-count
```
响应：
```json
{
  "count": 2
}
```

### 获取WebSocket信息
```bash
GET http://localhost:1883/api/websocket-info
```
响应：
```json
{
  "port": 1883,
  "path": "/",
  "url": "ws://localhost:1883/"
}
```

## 外部服务连接

### JavaScript示例
```javascript
const ws = new WebSocket('ws://localhost:1883/');
ws.onopen = function() {
    console.log('连接成功');
    // 发送JSON模板
    ws.send(JSON.stringify({
        template: {"id": 0, "name": ""},
        fieldTypes: {"id": "int", "name": "string"}
    }));
};
ws.onmessage = function(event) {
    console.log('收到数据:', event.data);
};
```

### Java示例
```java
WebSocketContainer container = ContainerProvider.getWebSocketContainer();
Session session = container.connectToServer(
    new WebSocketClient(), 
    new URI("ws://localhost:1883/")
);
```

### Python示例
```python
import websocket
ws = websocket.WebSocket()
ws.connect("ws://localhost:1883/")
```

## 项目结构

```
src/main/java/com/example/kineticsimulatorwebsocker/
├── config/
│   ├── CorsConfig.java                    # CORS配置
│   ├── WebSocketConfig.java               # WebSocket配置
│   └── WebSocketProperties.java           # WebSocket属性配置
├── controller/
│   ├── DynamicOnlineCountController.java  # 连接数控制器
│   └── WebSocketInfoController.java       # WebSocket信息控制器
├── websocket/
│   └── DynamicWebSocketServer.java        # 动态WebSocket服务
└── KineticSimulatorWebSockerApplication.java

src/main/resources/
├── static/
│   └── test-client.html                   # 前端测试页面
└── application.properties                 # 应用配置
```

## 核心组件说明

### 动态WebSocket服务 (`DynamicWebSocketServer`)
- 管理WebSocket连接和会话
- 处理JSON模板和字段类型配置
- 根据配置生成随机数据
- 支持字段限制功能
- 自动推送数据到客户端

### 模板配置系统
- `TemplateConfig`: 模板配置类，包含模板、字段类型和字段限制
- `FieldLimit`: 字段限制类，定义最大值最小值
- 支持嵌套对象和数组的递归处理

### 前端界面 (`test-client.html`)
- 交互式JSON模板编辑器
- 智能字段类型识别和配置
- 实时数据预览
- 连接状态监控

## 注意事项

1. 项目已配置为使用JDK 1.8，请确保开发环境使用正确的Java版本
2. WebSocket连接建立后需要发送JSON模板才能开始接收数据
3. 字段限制功能仅支持数字类型和日期类型
4. 支持多客户端同时连接，每个客户端可以有不同的模板配置
5. 如果客户端未发送模板，系统会在3秒后自动分配全局模板

## 验证环境

您可以使用以下命令验证环境配置：

```bash
# 检查Java版本
java -version

# 检查Maven版本
mvn -version

# 检查Maven Wrapper版本
./mvnw -version
```

## 扩展开发

### 添加新的数据类型
在 `DynamicWebSocketServer` 类中的 `generateRandomByType` 方法中添加新的case分支。

### 自定义字段限制逻辑
修改 `generateRandomInt`、`generateRandomDouble` 等方法，实现自定义的限制逻辑。

### 实现定时推送
可以使用Spring的 `@Scheduled` 注解实现定时推送功能。

## 故障排除

### 常见问题
1. **连接失败**: 检查端口1883是否被占用
2. **数据不推送**: 确保已发送有效的JSON模板
3. **字段限制不生效**: 检查输入的最大最小值格式是否正确

### 日志查看
应用启动后，可以在控制台查看详细的连接和数据处理日志。

## 参考资源

- [Spring Boot WebSocket 官方文档](https://spring.io/guides/gs/messaging-stomp-websocket/)
- [WebSocket API 文档](https://developer.mozilla.org/en-US/docs/Web/API/WebSocket)
- [Jackson JSON 处理](https://github.com/FasterXML/jackson)

---

## 🐳 Docker 部署

### 快速部署（推荐）

#### Windows用户
1. 双击运行 `docker-start.bat`
2. 等待服务启动完成
3. 访问：`http://localhost:1883/test-client.html`

#### Linux/macOS用户
```bash
# 给脚本执行权限（仅首次）
chmod +x docker-start.sh

# 运行启动脚本
./docker-start.sh
```

### 手动部署

#### 1. 构建并启动
```bash
# 进入项目目录
cd kinetic-simulator-WebSocker

# 启动服务
docker-compose up -d
```

#### 2. 查看服务状态
```bash
# 查看容器状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

#### 3. 访问应用
- 测试页面：`http://localhost:1883/test-client.html`
- WebSocket：`ws://localhost:1883/`
- API接口：`http://localhost:1883/api/connection-count`

### Docker构建问题解决

如果遇到Docker构建错误（如Maven Wrapper相关错误），可以使用以下方法：

#### 方法1：使用修复脚本（推荐）
```bash
# Windows
fix-docker-build.bat

# Linux/macOS
./fix-docker-build.sh
```

#### 方法2：使用备用Dockerfile
```bash
# 使用本地Maven的Dockerfile
docker build -f Dockerfile.maven -t websocket-simulator .
```

#### 方法3：手动修复
1. 确保项目根目录包含以下文件：
   - `mvnw` (Maven Wrapper脚本)
   - `mvnw.cmd` (Windows Maven Wrapper脚本)
   - `.mvn/` 目录
2. 清理Docker缓存：`docker builder prune -f`
3. 重新构建：`docker build -t websocket-simulator .`

### 常用Docker命令

| 命令 | 说明 |
|------|------|
| `docker-compose up -d` | 启动服务 |
| `docker-compose down` | 停止服务 |
| `docker-compose restart` | 重启服务 |
| `docker-compose logs -f` | 查看实时日志 |
| `docker-compose ps` | 查看服务状态 |

### 详细说明
更多Docker使用说明请参考：[Docker.md](./Docker.md)

---

## 🚀 交付部署

### 📋 部署文档
详细的交付部署指南请参考：[交付部署.md](./交付部署.md)

### 🎯 快速部署

#### 自动化部署脚本
```bash
# Linux/macOS
./deploy-to-server.sh <服务器IP> [用户名] [镜像版本] [镜像仓库]

# Windows
deploy-to-server.bat <服务器IP> [用户名] [镜像版本] [镜像仓库]
```

#### 部署流程
1. **本地构建** - 构建Docker镜像
2. **镜像上传** - 推送到Docker仓库
3. **远程部署** - 自动部署到目标服务器
4. **服务验证** - 检查服务状态

### 🌐 多IP部署

项目支持同时运行多个不同IP地址的服务实例：

```bash
# Windows
start-multi-ip.bat

# Linux/macOS
./start-multi-ip.sh
```

多IP部署将启动以下服务：
- 192.168.2.114:1883
- 192.168.2.115:1883 (主机端口1885)

访问地址：
- http://192.168.2.114:1883/test-client.html
- http://192.168.2.115:1885/test-client.html

### Docker配置文件

- `docker-compose.yml` - 单IP部署配置
- `docker-compose-multi-ip.yml` - 多IP部署配置
- `application-docker.properties` - Docker环境配置
- `application-192.168.2.114.properties` - IP1专用配置
- `application-192.168.2.115.properties` - IP2专用配置

---

## 📁 项目结构

```
kinetic-simulator-WebSocker/
├── src/                                    # 源代码
│   ├── main/java/                         # Java源码
│   │   └── com/example/kineticsimulatorwebsocker/
│   │       ├── config/                    # 配置类
│   │       ├── controller/                # 控制器
│   │       ├── websocket/                 # WebSocket服务
│   │       └── KineticSimulatorWebSockerApplication.java
│   └── main/resources/
│       ├── static/                        # 静态资源
│       │   └── test-client.html          # 前端测试页面
│       └── application.properties        # 应用配置
├── Dockerfile                             # Docker镜像构建文件
├── Dockerfile.maven                       # 备用Dockerfile（使用本地Maven）
├── docker-compose.yml                     # Docker Compose配置
├── docker-compose-multi-ip.yml           # 多IP Docker Compose配置
├── docker-start.bat                       # Windows启动脚本
├── docker-start.sh                        # Linux/macOS启动脚本
├── fix-docker-build.bat                   # Windows Docker构建修复脚本
├── fix-docker-build.sh                    # Linux/macOS Docker构建修复脚本
├── deploy-to-server.bat                   # Windows服务器部署脚本
├── deploy-to-server.sh                    # Linux/macOS服务器部署脚本
├── application-docker.properties          # Docker环境配置
├── .dockerignore                          # Docker忽略文件
├── Docker.md                              # Docker使用指南
├── 交付部署.md                            # 交付部署指南
├── pom.xml                                # Maven配置
└── README.md                              # 项目说明
```

---

## 🚀 部署方式对比

| 部署方式 | 优点 | 缺点 | 适用场景 |
|----------|------|------|----------|
| **Docker** | 环境一致、快速部署、易于管理 | 需要安装Docker | 生产环境、团队协作 |
| **传统部署** | 无需额外工具、直接运行 | 环境依赖、配置复杂 | 开发测试、单机部署 |

---

## 🎯 下一步

1. **学习Docker**：阅读 [Docker.md](./Docker.md) 了解详细使用方法
2. **交付部署**：阅读 [交付部署.md](./交付部署.md) 了解生产环境部署
3. **自定义配置**：修改 `application-docker.properties` 调整服务配置
4. **生产部署**：根据实际需求调整Docker配置和网络设置
5. **监控运维**：配置日志收集和监控告警

---

## 📞 技术支持

如有问题或建议，请：
1. 查看 [故障排除](#故障排除) 部分
2. 阅读 [Docker.md](./Docker.md) 详细说明
3. 阅读 [交付部署.md](./交付部署.md) 部署指南
4. 联系开发团队获取支持

---

## 📄 许可证

本项目采用 MIT 许可证，详见 [LICENSE](LICENSE) 文件。

---

## 🙏 致谢

感谢所有为项目做出贡献的开发者和用户！

---

*最后更新时间：2024年12月*

## 使用说明

### 1. 连接WebSocket

1. 打开前端界面
2. 点击"连接"按钮建立WebSocket连接
3. 连接成功后状态显示为"已连接"

### 2. 配置数据模板

1. 在JSON模板区域输入数据格式
2. 设置字段类型（string、number、boolean、date、array等）
3. 配置最大值、最小值范围
4. 设置默认值（可选）

### 3. 应用模板

1. 点击"应用模板"按钮发送配置到后端
2. 后端将根据配置生成随机数据
3. 数据将按设定的频率推送到前端

### 4. 高级模式

启用高级模式可以：
- 配置复杂嵌套的JSON结构
- 设置更精细的数据生成规则
- 支持数组和对象的组合

## 配置说明

### 数据类型支持

- `string`: 字符串类型
- `number`: 数字类型
- `boolean`: 布尔类型
- `date`: 日期类型 (yyyy-MM-dd)
- `datetime`: 日期时间类型 (yyyy-MM-dd HH:mm:ss)
- `array`: 数组类型
- `timestamp`: 时间戳类型

### 字段限制配置

每个字段可以设置：
- `min`: 最小值
- `max`: 最大值
- `default`: 默认值

### 推送频率

- 默认推送间隔：5秒
- 可通过前端界面动态调整
- 支持1-60秒范围设置

## 开发说明

### 添加新的数据类型

1. 在`DynamicWebSocketServer.java`中添加新的类型处理逻辑
2. 在前端`test-client.html`中添加对应的类型选项
3. 更新文档说明

### 自定义数据生成规则

1. 继承或修改`AdvancedDataGenerator`类
2. 实现自定义的数据生成逻辑
3. 在WebSocket处理器中集成

## 故障排除

### 常见问题

1. **WebSocket连接失败**
   - 检查端口是否被占用
   - 确认防火墙设置
   - 验证IP地址配置

2. **数据推送异常**
   - 检查JSON模板格式
   - 验证字段类型配置
   - 查看后端日志

3. **Docker部署问题**
   - 确认Docker服务运行状态
   - 检查端口映射配置
   - 验证网络设置

4. **Docker构建失败**
   - 使用修复脚本：`fix-docker-build.bat` 或 `fix-docker-build.sh`
   - 使用备用Dockerfile：`docker build -f Dockerfile.maven -t websocket-simulator .`
   - 检查Maven Wrapper文件是否完整

### 日志查看

```bash
# 本地运行
tail -f logs/application.log

# Docker运行
docker-compose logs -f

# 多IP部署
docker-compose -f docker-compose-multi-ip.yml logs -f
```

## 许可证

本项目采用MIT许可证。

## 贡献

欢迎提交Issue和Pull Request来改进项目。

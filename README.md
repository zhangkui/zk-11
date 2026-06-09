# 新能源汽车充电排队与预约系统

基于 Spring Boot + Vue 3 + MySQL + Redis 的全栈新能源汽车充电管理系统，支持充电站点管理、时段预约、排队叫号、超时释放、充电记录和费用留痕等完整功能。

## 技术栈

### 后端
- **框架**: Spring Boot 3.2.0
- **ORM**: MyBatis Plus 3.5.5
- **缓存**: Redis 7.0
- **数据库**: MySQL 8.0
- **Java 版本**: JDK 17
- **构建工具**: Maven 3.8+
- **工具库**: Lombok, Hutool

### 前端
- **框架**: Vue 3.4 + Vite 5.0
- **路由**: Vue Router 4.2
- **状态管理**: Pinia 2.1
- **UI 组件**: Element Plus 2.4
- **图表**: ECharts 5.4
- **HTTP 客户端**: Axios 1.6

### 部署
- **容器化**: Docker + Docker Compose
- **Web 服务器**: Nginx

## 项目结构

```
charging-system/
├── backend/                    # 后端项目
│   ├── src/main/java/com/charging/
│   │   ├── ChargingApplication.java     # 启动类
│   │   ├── config/                      # 配置类
│   │   ├── common/                      # 通用类
│   │   ├── exception/                   # 异常处理
│   │   ├── entity/                      # 实体类
│   │   ├── mapper/                      # Mapper 接口
│   │   ├── dto/                         # 数据传输对象
│   │   ├── vo/                          # 视图对象
│   │   ├── service/                     # 业务逻辑层
│   │   ├── controller/                  # 控制层
│   │   ├── task/                        # 定时任务
│   │   └── util/                        # 工具类
│   ├── src/main/resources/
│   │   ├── application.yml              # 主配置
│   │   ├── application-dev.yml          # 开发环境配置
│   │   └── application-docker.yml       # Docker 环境配置
│   ├── sql/init.sql                     # 数据库初始化脚本
│   ├── Dockerfile                       # 后端 Dockerfile
│   └── pom.xml                          # Maven 配置
├── frontend/                   # 前端项目
│   ├── src/
│   │   ├── main.js                        # 入口文件
│   │   ├── App.vue                        # 根组件
│   │   ├── router/                        # 路由配置
│   │   ├── store/                         # Pinia 状态管理
│   │   ├── utils/                         # 工具函数
│   │   ├── api/                           # API 接口
│   │   ├── components/                    # 公共组件
│   │   └── views/                         # 页面组件
│   ├── Dockerfile                         # 前端 Dockerfile
│   ├── nginx.conf                         # Nginx 配置
│   ├── vite.config.js                     # Vite 配置
│   └── package.json                       # 依赖配置
├── docker-compose.yml           # Docker Compose 配置
├── start.bat                    # Windows 启动脚本
├── start.sh                     # Linux/Mac 启动脚本
└── README.md
```

## 功能模块

### 1. 充电站点管理
- 站点 CRUD 管理
- 充电桩管理（快充/慢充、功率配置）
- 电价配置（电费单价 + 服务费）
- 站点状态管理（开放/关闭）

### 2. 时段预约
- 24 小时时段选择
- 预约冲突检测
- 15 分钟自动超时释放
- 预约状态跟踪（待确认/已确认/已取消/已超时/已完成）

### 3. 排队叫号
- 实时排队取号
- 自动叫号机制
- 10 分钟叫号超时
- 叫号状态显示（排队中/已叫号/已完成/已过号）

### 4. 超时释放
- 定时任务每分钟扫描
- 自动释放超时预约
- 自动释放超时叫号
- 充电桩状态实时更新

### 5. 充电记录
- 开始充电/结束充电
- 充电量自动计算
- 充电时长统计
- 充电历史查询

### 6. 费用留痕
- 费用自动结算（电费 = 充电量 × 单价 + 充电量 × 服务费）
- 多支付方式支持（余额/微信/支付宝）
- 支付流水记录
- 支持退款操作

## 数据库表结构

| 表名 | 说明 |
|------|------|
| `charging_station` | 充电站点表 |
| `charging_pile` | 充电桩表 |
| `user_info` | 用户表 |
| `reservation` | 预约表 |
| `charging_queue` | 排队表 |
| `charging_record` | 充电记录表 |
| `payment_record` | 支付记录表 |

## API 接口

### 站点管理
- `GET /api/stations` - 查询站点列表
- `GET /api/stations/{id}` - 获取站点详情
- `POST /api/stations` - 新增站点
- `PUT /api/stations/{id}` - 更新站点
- `DELETE /api/stations/{id}` - 删除站点
- `GET /api/stations/{id}/piles` - 获取站点充电桩列表

### 预约管理
- `POST /api/reservations` - 创建预约
- `GET /api/reservations/user/{userId}` - 获取用户预约列表
- `POST /api/reservations/{id}/cancel` - 取消预约
- `POST /api/reservations/{id}/checkin` - 预约签到
- `GET /api/stations/{id}/time-slots` - 查询时段状态

### 排队叫号
- `POST /api/queue/join` - 加入排队
- `GET /api/queue/station/{stationId}` - 获取站点排队状态
- `GET /api/queue/user/{userId}` - 获取用户排队状态
- `POST /api/queue/call` - 叫号
- `POST /api/queue/{id}/complete` - 完成排队

### 充电记录
- `POST /api/records/start` - 开始充电
- `POST /api/records/end` - 结束充电
- `GET /api/records/{id}` - 获取充电记录详情
- `GET /api/records/user/{userId}` - 获取用户充电记录

### 支付管理
- `POST /api/payments` - 发起支付
- `GET /api/payments/{id}` - 获取支付详情
- `GET /api/payments/user/{userId}` - 获取用户支付记录
- `POST /api/payments/{id}/refund` - 申请退款

### 用户管理
- `GET /api/users/{id}` - 获取用户信息
- `GET /api/users/phone/{phone}` - 根据手机号查询用户
- `POST /api/users` - 新增用户
- `PUT /api/users/{id}/balance` - 充值

## 快速开始

### 方式一：Docker 部署（推荐）

#### 前置要求
- Docker 20.10+
- Docker Compose 2.0+

#### 启动服务

**Windows:**
```cmd
start.bat
```

**Linux/Mac:**
```bash
chmod +x start.sh
./start.sh
```

或手动执行：
```bash
docker-compose up -d --build
```

#### 访问系统
- 前端: http://localhost
- 后端 API: http://localhost:8080/api
- MySQL: localhost:3306 (用户名: charging, 密码: charging123)
- Redis: localhost:6379 (密码: redis123)

### 方式二：本地开发

#### 启动 MySQL 和 Redis
```bash
docker-compose up -d mysql redis
```

#### 启动后端
```bash
cd backend
mvn spring-boot:run
```

#### 启动前端
```bash
cd frontend
npm install
npm run dev
```

访问: http://localhost:5173

## 业务流程

### 预约充电流程
1. 用户浏览站点 → 选择站点 → 查看时段状态
2. 选择日期和时段 → 提交预约 → 获得预约号
3. 15 分钟内签到 → 系统分配充电桩
4. 开始充电 → 充电中 → 结束充电
5. 计算费用 → 发起支付 → 支付完成

### 即时排队流程
1. 用户到达站点 → 取号排队
2. 等待叫号 → 叫号后 10 分钟内确认
3. 分配充电桩 → 开始充电
4. 充电完成 → 结算支付

## 初始化数据

系统启动时会自动初始化以下数据：

**测试用户:**
| 手机号 | 昵称 | 车牌号 | 余额 |
|--------|------|--------|------|
| 13800138001 | 张三 | 京A12345 | ¥500 |
| 13800138002 | 李四 | 京B67890 | ¥300 |
| 13800138003 | 王五 | 京C11111 | ¥1000 |

**充电站点:**
1. 中关村充电站 (10个桩)
2. 国贸充电站 (8个桩)
3. 望京充电站 (12个桩)

## 配置说明

### 自定义配置项 (`application.yml`)
```yaml
charging:
  reservation:
    time-out-minutes: 15    # 预约超时时间（分钟）
  queue:
    time-out-minutes: 10    # 叫号超时时间（分钟）
  time-slots: 24            # 每日时段数量
```

### Docker Compose 环境变量
- `SPRING_DATASOURCE_URL` - 数据库连接 URL
- `SPRING_DATASOURCE_USERNAME` - 数据库用户名
- `SPRING_DATASOURCE_PASSWORD` - 数据库密码
- `SPRING_REDIS_HOST` - Redis 主机
- `SPRING_REDIS_PORT` - Redis 端口
- `SPRING_REDIS_PASSWORD` - Redis 密码

## 常用命令

```bash
# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f
docker-compose logs -f backend
docker-compose logs -f frontend

# 重启服务
docker-compose restart

# 停止服务
docker-compose down

# 停止服务并删除数据
docker-compose down -v

# 重新构建
docker-compose build
```

## 开发规范

### 后端代码规范
- 使用 RESTful 风格 API
- 统一返回格式 `Result<T>`
- 业务异常使用 `BusinessException`
- 定时任务使用 `@Scheduled`
- Redis Key 使用 `RedisKeyUtil` 统一管理

### 前端代码规范
- 使用 Composition API
- API 统一封装在 `src/api/` 目录
- 状态管理使用 Pinia
- 组件命名使用 PascalCase

## 注意事项

1. 首次启动会自动执行数据库初始化脚本，请勿重复执行
2. Redis 用于缓存时段状态和生成排队号，需确保 Redis 正常运行
3. 定时任务每分钟执行一次超时释放逻辑
4. 生产环境请修改默认密码和敏感配置
5. 前端请求通过 Nginx 代理到后端，路径前缀为 `/api`

## License

MIT License

# 终身学习学分银行平台

基于 `Java 21 + Spring Boot 3 + Vue 3 + MyBatis-Plus + MySQL + Redis + RabbitMQ + Redisson` 的全栈学习管理系统，支持管理员/学员双角色、积分商城、秒杀、购物车、论坛等功能。

## 功能模块

### 权限系统
- JWT Token 认证（自定义 HandlerInterceptor）
- 管理员（ADMIN）：全部功能
- 学员（STUDENT）：个人信息、积分商城、学习论坛

### 积分商城
- 商品管理：上架/下架、库存、销量、课程同步
- 购物车：加减数量、批量删除、全选结算
- 订单系统：下单→支付→发货→取消→退款（积分直接扣减）
- 商品详情弹窗 + 结算确认页（15分钟倒计时）

### 秒杀专区
- 热点商品秒杀：Lua 脚本原子判库存 + Redis 去重
- RabbitMQ 异步下单（防重 + 手动 ACK/NACK）
- Redisson 分布式锁（一人一单）
- MySQL 乐观锁（WHERE stock > 0 防止超卖）
- 死信队列（15分钟超时未处理自动取消）
- 管理员秒杀活动 CRUD + 实时库存

### 论坛
- 板块分类（学习交流/积分商城/课程讨论/技术问答/活动分享）
- 发帖/我的帖子/关键词搜索
- 管理员隐藏/显示帖子

### 管理后台
- 仪表盘统计
- 学员档案（状态启用/禁用）
- 课程资源、学习记录、成果认定
- 积分账户（增减/冻结/解冻）
- 积分流水（变动前后余额）
- 论坛管理、招聘求职
- 商品管理、秒杀管理

### 其他
- 学员个人信息页
- 订单详情记录表（支持一单多品）
- 订单明细表（兑换时价格快照）

## 技术栈

| 层面 | 技术 | 说明 |
|------|------|------|
| 后端框架 | Spring Boot 3.3.6 | Java 21 |
| ORM | MyBatis-Plus 3.5.9 | 逻辑删除、自动填充、分页 |
| 数据库 | MySQL 8 | Flyway 版本迁移 |
| 缓存 | Redis | Token 存储、分布式锁、秒杀库存 |
| 消息队列 | RabbitMQ | 秒杀异步下单 |
| 分布式锁 | Redisson 3.23.5 | 一人一单 |
| 安全 | SHA-256 + 盐 | 密码哈希 |
| 文档 | SpringDoc OpenAPI 2.6 | Swagger UI |
| 前端 | Vue 3 (Composition API) + Vite | Lucide 图标库 |

## 环境要求

- JDK 21
- MySQL 8（数据库 `credit_bank`）
- Redis（默认 `127.0.0.1:6379`，database 4）
- RabbitMQ（默认 `127.0.0.1:5672`，管理后台 `15672`）
- Node.js 18+（前端开发）

## 快速启动

### 1. 创建数据库

```sql
CREATE DATABASE credit_bank DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 配置连接信息

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/credit_bank
    username: root
    password: 123456
  data:
    redis:
      host: 127.0.0.1
      port: 6379
      database: 4
  rabbitmq:
    host: 127.0.0.1
    port: 5672
    username: guest
    password: guest
```

### 3. 启动后端

```bash
mvn spring-boot:run
# 或 IDE 中运行 CreditBankApplication.java
```

Flyway 会自动执行数据库迁移（V1 ~ V7），创建表结构并插入种子数据。

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

访问 `http://localhost:5173`

## 测试账号

| 账号 | 密码 | 角色 | 积分 |
|------|------|------|------|
| student001 | 123456 | 管理员 | 40 |
| admin001 | 123456 | 管理员 | — |
| admin002 | 123456 | 管理员 | — |
| student002 | 123456 | 学生 | 60 |
| student003 | 123456 | 学生 | 100 |
| student004 | 123456 | 学生 | 50 |
| student005 | 123456 | 学生 | 30 |

## 项目结构

```
lifelong-credit-bank
├── frontend/                         # Vue 3 前端
│   └── src/
│       ├── App.vue                   # 主应用（登录/管理员/学员三态切换）
│       ├── LoginView.vue             # 登录页
│       ├── CreditMall.vue            # 积分商城（商品/秒杀/购物车/订单）
│       ├── ForumView.vue             # 学习论坛
│       ├── modules.js                # 管理后台模块配置
│       └── styles.css                # 全局样式
├── src/main/java/org/csu/creditbank/
│   ├── common/                       # ApiResult / BusinessException
│   ├── config/                       # MyBatis-Plus / AuthInterceptor / MQ / Redisson / WebMvc
│   ├── controller/
│   │   ├── AuthController.java       # 登录/登出
│   │   ├── FlashSaleController.java  # 秒杀入口
│   │   ├── StudentCartController.java
│   │   ├── StudentController.java    # 学员专区
│   │   ├── StudentForumController.java
│   │   └── admin/                    # 管理后台 Controller
│   ├── dto/                          # 请求/响应 DTO
│   ├── entity/                       # 数据库实体（15张表）
│   ├── mapper/                       # MyBatis-Plus Mapper
│   ├── receiver/                     # RabbitMQ 消费者
│   ├── service/
│   │   └── impl/                     # 核心业务逻辑
│   └── util/                         # 工具类（密码/Token/分布式锁/ID生成）
├── src/main/resources/
│   ├── db/migration/                 # Flyway SQL（V1~V7）
│   ├── seckill.lua                   # 秒杀 Lua 脚本
│   ├── unlock.lua                    # 分布式锁释放 Lua 脚本
│   ├── static/                       # 静态资源（旧版管理页）
│   └── application.yml
└── pom.xml
```

## 数据库表

| 表名 | 说明 |
|------|------|
| learner | 学员（含密码、角色） |
| credit_product | 积分商品（含销量、评论数） |
| credit_order | 积分订单（含批次号、关闭时间） |
| credit_order_detail | 订单明细（价格快照） |
| credit_account | 积分账户 |
| credit_transaction | 积分流水（变动前后余额） |
| cart | 购物车（物理删除） |
| flash_sale | 秒杀活动 |
| flash_sale_record | 秒杀记录（一人一单唯一键） |
| course | 课程（与商品自动同步） |
| forum_post | 论坛帖子 |
| achievement | 成果认定 |
| learning_record | 学习记录 |
| sign_in_record | 签到记录 |
| job_posting | 招聘岗位 |
| blockchain_credential | 区块链存证 |

## 秒杀架构

```
用户点击秒杀
  → POST /api/flash/seckill/{id}
    → seckill.lua（原子：判库存 + 判重复 + DECR + SADD）
    → RabbitTemplate 发送异步消息
    → FlashSaleOrderReceiver（@RabbitListener）
      → Redis setIfAbsent 防重
      → Redisson 分布式锁（一人一单）
      → MySQL CAS（UPDATE stock=stock-1 WHERE stock>0）
      → 扣积分 + 创建订单
      → channel.basicAck 手动应答
```

# 终身学习学分银行平台

基于 `Java 21 + Spring Boot 3 + Vue 3 + MyBatis-Plus + MySQL + Redis + RabbitMQ + Redisson` 的全栈学习管理系统，支持多机构多租户、管理员/学员双角色、积分商城、秒杀、购物车、论坛、一键学习等功能。

## 功能模块

### 权限系统
- UUID Token 认证（自定义 HandlerInterceptor + Redis 存储，24h TTL）
- SHA-256 + 随机盐 密码哈希
- 管理员（ADMIN）：全部功能，仅见本机构数据
- 学员（STUDENT）：个人信息、我的课程、学习论坛、积分商城

### 多机构多租户
- 3 个独立机构：终身学习学院、计算机科学与技术学院、经济管理学院
- MyBatis-Plus TenantLineInnerInterceptor 自动注入 `WHERE institution_id = ?`
- InstitutionContext（ThreadLocal）传递机构上下文
- 积分商城（商品/订单/购物车/秒杀）跨机构共享
- 课程/学员/学习记录/论坛/成果认定 机构隔离
- 管理员仅能查看和操作本机构学员数据

### 积分商城
- 商品管理：上架/下架、库存、销量、课程自动同步商品
- 购物车：加减数量、批量删除、全选结算（物理删除，20件上限）
- 订单系统：下单→确认支付→发货→取消→退款（积分直接扣减）
- 商品详情弹窗 + 结算确认页（15分钟倒计时）
- 下单支持单商品和购物车批量两种模式

### 秒杀专区
- 热点商品秒杀：Lua 脚本原子判库存 + Redis 去重
- RabbitMQ 异步下单（防重 + 手动 ACK/NACK）
- Redisson 分布式锁（一人一单）
- MySQL CAS 乐观锁（`WHERE stock > 0` 防止超卖）
- 死信队列（15分钟超时未处理自动取消）
- 管理员秒杀活动 CRUD + 实时 Redis 库存

### 论坛
- 板块分类：学习交流/积分商城/课程讨论/技术问答/活动分享
- 发帖/我的帖子/关键词搜索
- Redisson 点赞/取消点赞（实时点赞数）
- 管理员隐藏/显示帖子

### 学员端
- 个人信息查看与编辑
- **我的课程**：查看本机构已发布课程，一键学习
- **一键学习**：点击即完成课程，自动创建学习记录 + 获得对应积分
- 防重复学习检测
- 积分商城：浏览商品、下单、支付、查看订单

### 管理后台
- 仪表盘统计（学员数/课程数/成果认定/积分流水/论坛帖子/招聘岗位）
- 学员档案（仅显示 STUDENT 角色，状态启用/禁用）
- 课程资源、学习记录、成果认定（仅本机构数据）
- 积分账户：仅本机构学员（增减/冻结/解冻）
- 积分流水：仅本机构学员（变动前后余额）
- 论坛管理、招聘求职
- 商品管理、秒杀管理
- 配置驱动模块（modules.js），Swagger 接口文档

## 技术栈

| 层面 | 技术 | 说明 |
|------|------|------|
| 后端框架 | Spring Boot 3.3.6 | Java 21 |
| ORM | MyBatis-Plus 3.5.9 | 多租户插件、逻辑删除、自动填充、分页 |
| 数据库 | MySQL 8 | Flyway 版本迁移 V1~V9 |
| 缓存 | Redis | Token 存储、分布式锁、秒杀库存 |
| 消息队列 | RabbitMQ | 秒杀异步下单、死信队列 |
| 分布式锁 | Redisson 3.23.5 | 秒杀一人一单、论坛点赞 |
| 安全 | SHA-256 + 随机盐 | 密码哈希 |
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

Flyway 会自动执行数据库迁移（V1 ~ V9），创建表结构并插入种子数据。

### 4. 构建并部署前端

```bash
cd frontend
npm install
npm run dev        # 开发模式
# 或
npx vite build     # 生产构建 → 输出到 ../src/main/resources/static/
```

访问 `http://localhost:8080`

## 测试账号

所有账号密码均为 `123456`：

| 账号 | 角色 | 所属机构 | 积分 |
|------|------|----------|------|
| admin001 | 管理员 | 终身学习学院 | — |
| admin002 | 管理员 | 计算机科学与技术学院 | — |
| admin003 | 管理员 | 经济管理学院 | — |
| student001 | 学生 | 终身学习学院 | 40 |
| student002 | 学生 | 终身学习学院 | 60 |
| student003 | 学生 | 终身学习学院 | 100 |
| student004 | 学生 | 计算机科学与技术学院 | 50 |
| student005 | 学生 | 经济管理学院 | 30 |

## 项目结构

```
lifelong-credit-bank
├── frontend/                         # Vue 3 前端
│   └── src/
│       ├── App.vue                   # 主应用（登录/管理员/学员三态切换）
│       ├── LoginView.vue             # 登录页（SHA-256 认证）
│       ├── CreditMall.vue            # 积分商城（商品/秒杀/购物车/订单/结算）
│       ├── ForumView.vue             # 学习论坛（发帖/点赞/板块筛选）
│       ├── modules.js                # 管理后台模块配置（12个模块）
│       └── styles.css                # 全局样式（CSS 变量 + 布局系统）
├── src/main/java/org/csu/creditbank/
│   ├── common/                       # ApiResult / BusinessException / GlobalExceptionHandler
│   ├── config/                       # MyBatis-Plus（多租户）/ AuthInterceptor / MQ / Redisson / WebMvc
│   ├── controller/
│   │   ├── AuthController.java       # 登录/登出/当前用户
│   │   ├── FlashSaleController.java  # 秒杀入口
│   │   ├── StudentCartController.java
│   │   ├── StudentController.java    # 学员专区（信息/课程/学习/订单）
│   │   ├── StudentForumController.java
│   │   └── admin/                    # 管理后台 Controller
│   │       ├── credit/               # 积分账户/积分流水/订单
│   │       ├── mall/                 # 商品管理/秒杀管理
│   │       └── edu/                  # 成果认定/学习记录/签到记录
│   ├── dto/                          # 请求/响应 DTO
│   ├── entity/                       # 数据库实体（18张表）
│   ├── mapper/                       # MyBatis-Plus Mapper
│   ├── receiver/                     # RabbitMQ 消费者（秒杀/超时）
│   ├── service/
│   │   └── impl/                     # 核心业务逻辑
│   └── util/                         # InstitutionContext / PasswordUtil / TokenUtil / LockUtil
├── src/main/resources/
│   ├── db/migration/                 # Flyway SQL（V1~V9）
│   ├── seckill.lua                   # 秒杀 Lua 脚本（原子判库存+去重+扣减）
│   ├── unlock.lua                    # 分布式锁释放 Lua 脚本（防误删）
│   ├── static/                       # 前端构建产物
│   └── application.yml
└── pom.xml
```

## 数据库表

| 表名 | 说明 | 多租户 |
|------|------|--------|
| institution | 机构信息 | 共享 |
| learner | 学员（含密码、角色、机构ID） | 隔离 |
| credit_product | 积分商品（含销量、评论数） | 共享 |
| credit_order | 积分订单（含批次号、关闭时间） | 共享 |
| credit_order_detail | 订单明细（价格快照） | 共享 |
| credit_account | 积分账户 | 共享* |
| credit_transaction | 积分流水（变动前后余额） | 共享* |
| cart | 购物车（物理删除） | 共享 |
| flash_sale | 秒杀活动 | 共享 |
| flash_sale_record | 秒杀记录（一人一单唯一键） | 共享 |
| course | 课程（与商品自动同步） | 隔离 |
| forum_post | 论坛帖子 | 隔离 |
| forum_post_like | 论坛点赞记录 | 共享 |
| achievement | 成果认定 | 隔离 |
| learning_record | 学习记录 | 隔离 |
| sign_in_record | 签到记录 | 隔离 |
| job_posting | 招聘岗位 | 隔离 |
| blockchain_credential | 区块链存证 | 隔离 |

> \* 共享表无 `institution_id` 字段，通过 `learner_id → learner.institution_id` 间接归属，Controller 层手动过滤。

## 多租户架构

```
请求到达 → AuthInterceptor（解析 Token → InstitutionContext.set(institutionId)）
         → Controller
         → MyBatis-Plus TenantLineInnerInterceptor
              ├─ 隔离表：自动注入 WHERE institution_id = ?
              └─ 共享表（SHARED_TABLES）：跳过过滤
         → afterCompletion → InstitutionContext.clear()
```

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

超时处理：
  死信队列（x-message-ttl=900000）
    → FlashSaleTimeoutReceiver → 退还库存 + 取消订单
```

## API 路由

### 认证
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/auth/login | 登录 |
| POST | /api/auth/logout | 退出 |
| GET | /api/auth/me | 当前用户信息 |

### 学员端
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/student/profile | 个人信息 |
| PUT | /api/student/profile | 更新信息 |
| GET | /api/student/account | 积分账户 |
| POST | /api/student/account/open | 开通账户 |
| GET | /api/student/products | 商品列表 |
| POST | /api/student/orders/place | 下单 |
| GET | /api/student/orders | 我的订单 |
| POST | /api/student/orders/{id}/pay | 支付 |
| POST | /api/student/orders/{id}/cancel | 取消 |
| GET | /api/student/courses | 本机构课程 |
| POST | /api/student/courses/{id}/learn | 一键学习 |
| GET | /api/student/stats | 积分统计 |

### 管理端
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/admin/dashboard/stats | 仪表盘 |
| * | /api/admin/learners | 学员档案（仅 STUDENT） |
| * | /api/admin/courses | 课程资源 |
| * | /api/admin/products | 商品管理 |
| * | /api/admin/credit-accounts | 积分账户（本机构） |
| * | /api/admin/credit-transactions | 积分流水（本机构） |
| * | /api/admin/flash-sales | 秒杀管理 |
| * | /api/admin/achievements | 成果认定 |
| * | /api/admin/learning-records | 学习记录 |
| * | /api/admin/forum-posts | 论坛管理 |
| * | /api/admin/jobs | 招聘求职 |

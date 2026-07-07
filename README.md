# 终身学习学分银行平台学习管理系统

这是一个从零搭建的实训项目骨架，采用 `Java 21 + Spring Boot + MyBatis-Plus + MySQL + Redis`。当前版本先完成“大项目框架”和核心模块入口，后续可以继续扩展登录权限、积分商城、区块链存证对接、论坛评论等功能。

## 功能模块

- 学员档案管理：学员基础信息、教育层次、账号状态。
- 学习成果管理：学习记录、课程成绩、成果认定审核。
- 学分积分管理：积分账户、积分增加、积分消费、积分流水。
- 论坛管理：学习交流帖子管理。
- 报名与签到：已预留签到记录模块。
- 招聘求职管理：职位发布与状态维护。
- 区块链校验：证书、成果、积分流水的存证记录入口。

## 技术栈

- 后端：Spring Boot 3.3.6、Spring Web、Validation、Actuator
- 持久层：MyBatis-Plus
- 数据库：MySQL 8
- 缓存预留：Redis
- 数据迁移：Flyway
- 接口文档：SpringDoc OpenAPI
- 前端：Spring Boot 静态资源目录下的原生 HTML/CSS/JavaScript 管理页

## 运行步骤

1. 在 MySQL 中创建数据库：

```sql
CREATE DATABASE credit_bank DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 修改数据库配置：

文件：`src/main/resources/application.yml`

```yaml
spring:
  datasource:
    username: root
    password: 123456
```

3. 在 IDEA 中打开 `lifelong-credit-bank` 目录，等待 Maven 导入依赖。

4. 运行启动类：

```text
org.csu.creditbank.CreditBankApplication
```

5. 访问页面：

```text
http://localhost:8080/
http://localhost:8080/swagger-ui.html
```

## 主要接口

- `GET /api/admin/dashboard/stats`：平台统计
- `GET /api/admin/learners`：学员分页
- `POST /api/admin/learners`：新增学员
- `GET /api/admin/courses`：课程分页
- `POST /api/admin/credit-accounts/open/{learnerId}`：开通积分账户
- `POST /api/admin/credit-accounts/increase`：增加积分
- `POST /api/admin/credit-accounts/consume`：消费积分
- `GET /api/admin/credit-transactions`：积分流水
- `GET /api/admin/achievements`：成果认定列表
- `GET /api/admin/forum-posts`：论坛帖子列表
- `GET /api/admin/jobs`：招聘岗位列表

## 目录结构

```text
lifelong-credit-bank
├─ docs
├─ src/main/java/org/csu/creditbank
│  ├─ common
│  ├─ config
│  ├─ controller
│  ├─ dto
│  ├─ entity
│  ├─ mapper
│  └─ service
├─ src/main/resources
│  ├─ db/migration
│  ├─ static
│  └─ application.yml
└─ pom.xml
```

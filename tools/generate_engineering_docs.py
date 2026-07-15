from pathlib import Path
from html import escape

PROJECT = "终身学习学分银行平台"
SHORT = "LLCBP"
AUTHOR = "付嘉一"
DEPT = "智慧教育研教部"
DATE = "2026年07月13日"
OUT = Path("generated-docs/html")


def p(text):
    return f"<p>{escape(text)}</p>"


def h(level, text):
    return f"<h{level}>{escape(text)}</h{level}>"


def ul(items):
    return "<ul>" + "".join(f"<li>{escape(i)}</li>" for i in items) + "</ul>"


def table(headers, rows):
    out = ["<table>", "<tr>" + "".join(f"<th>{escape(str(x))}</th>" for x in headers) + "</tr>"]
    for row in rows:
        out.append("<tr>" + "".join(f"<td>{escape(str(x))}</td>" for x in row) + "</tr>")
    out.append("</table>")
    return "\n".join(out)


def pre(text):
    return f"<pre>{escape(text)}</pre>"


def cover(title, doc_no, version="1.0", users="■主管领导  ■项目组  □客户（市场）  ■维护人员  □用户"):
    return f"""
<div class="cover">
  <p>项目编号：HD20260713SR005</p>
  <h1>{PROJECT}</h1>
  <h2>{title}</h2>
  <p>Version: {version}</p>
  <table>
    <tr><td>项目承担部门</td><td>{DEPT}</td></tr>
    <tr><td>撰写人（签名）</td><td>{AUTHOR}</td></tr>
    <tr><td>完成日期</td><td>{DATE}</td></tr>
    <tr><td>本文档使用部门</td><td>{users}</td></tr>
    <tr><td>评审负责人（签名）</td><td>{AUTHOR}</td></tr>
    <tr><td>评审日期</td><td>{DATE}</td></tr>
    <tr><td>文档编号</td><td>{doc_no}</td></tr>
  </table>
</div>
"""


def meta(title, version="1.0"):
    return (
        h(2, "文档信息")
        + table(
            ["项目", "内容"],
            [
                ["标题", f"{PROJECT}-{title}"],
                ["作者", AUTHOR],
                ["创建日期", DATE],
                ["上次更新日期", DATE],
                ["版本", version],
                ["部门名称", DEPT],
            ],
        )
        + h(2, "修订文档历史记录")
        + table(
            ["日期", "版本", "说明", "作者"],
            [
                ["2026年07月08日", "0.1", "完成初稿，建立章节结构", AUTHOR],
                ["2026年07月11日", "0.9", "结合项目源码、数据库脚本和前端页面补充内容", AUTHOR],
                [DATE, version, "评审后正式发布", AUTHOR],
            ],
        )
    )


def doc(title, doc_no, body):
    css = """
body { font-family: "Noto Sans CJK SC", "Microsoft YaHei", SimSun, sans-serif; line-height: 1.55; font-size: 11pt; color: #111; }
h1 { text-align: center; font-size: 22pt; margin: 28px 0; }
h2 { font-size: 17pt; margin-top: 26px; border-bottom: 1px solid #888; padding-bottom: 4px; }
h3 { font-size: 14pt; margin-top: 20px; }
h4 { font-size: 12pt; margin-top: 14px; }
p { text-indent: 2em; margin: 6px 0; }
.cover p { text-indent: 0; text-align: center; }
.cover table { margin: 24px auto; width: 82%; }
table { border-collapse: collapse; width: 100%; margin: 10px 0 16px; }
th, td { border: 1px solid #555; padding: 6px 8px; vertical-align: top; }
th { background: #f2f2f2; font-weight: bold; text-align: center; }
pre { border: 1px solid #999; background: #f8f8f8; padding: 10px; white-space: pre-wrap; font-family: Consolas, monospace; }
ul { margin-top: 4px; }
li { margin: 4px 0; }
"""
    html = f"<!doctype html><html><head><meta charset='utf-8'><style>{css}</style><title>{escape(title)}</title></head><body>"
    html += cover(title, doc_no) + meta(title) + body + "</body></html>"
    return html


common_refs = [
    "《终身学习学分银行平台软件需求规约》",
    "《终身学习学分银行平台项目开发计划》",
    "《终身学习学分银行平台数据库设计说明书》",
    "Spring Boot 3.3、Vue 3、MyBatis-Plus、MySQL 8、Redis、RabbitMQ 官方文档",
    "项目源码：/home/fjy/IdeaProjects/lifelong-credit-bank",
]

modules = [
    ["登录认证与权限", "Token 登录、登出、当前用户、ADMIN/STUDENT 角色分离、Redis Token 存储"],
    ["多机构管理", "机构维度数据隔离，管理员仅操作本机构学员、课程、论坛、成果、招聘数据"],
    ["学员档案", "学员基本信息维护、状态启停、教育层次、所属机构"],
    ["课程资源", "课程发布、课程学习资源、一键学习、课程报名及审核"],
    ["学习记录", "学习进度、成绩、完成结果、完成时间、课程积分奖励"],
    ["积分账户", "开户、积分增加、消费、冻结、解冻、流水追踪"],
    ["积分商城", "商品管理、购物车、订单、支付、发货、取消、退款"],
    ["秒杀专区", "Redis Lua 预扣库存、RabbitMQ 异步下单、Redisson 锁、MySQL CAS 防超卖"],
    ["学习论坛", "板块、发帖、回复、点赞、隐藏/显示、我的帖子"],
    ["成果认定", "成果提交、证明材料、管理员审核、通过后积分奖励"],
    ["签到与诚信", "每日签到、签到积分、诚信评分重新计算"],
    ["招聘求职", "岗位发布、简历投递、申请审核、简历文件上传"],
    ["区块链存证", "业务编号查询、凭证校验、哈希与链上状态记录"],
    ["AI 学习助手", "基于 LangChain4j 的学习问答、SSE 流式输出、可选 DeepSeek 与通义千问 Embedding"],
]

api_groups = [
    ["/api/auth", "登录、登出、获取当前用户"],
    ["/api/student", "学员资料、账户、课程、订单、学习记录、成果、岗位、签到"],
    ["/api/student/cart", "购物车新增、列表、数量修改、删除、批量删除、结算计算"],
    ["/api/student/forum", "帖子列表、详情、我的帖子、回复、点赞、板块"],
    ["/api/student/ai", "AI 学习助手流式问答"],
    ["/api/admin/learners", "学员档案增删改查、状态启停"],
    ["/api/admin/courses", "课程资源增删改查"],
    ["/api/admin/credit-accounts", "积分账户开户、增加、消费、冻结、解冻"],
    ["/api/admin/credit-transactions", "积分流水查询"],
    ["/api/admin/products", "积分商品查询、上架、下架、编辑"],
    ["/api/admin/orders", "订单查询、代下单、支付、发货、取消、退款"],
    ["/api/admin/flash-sales", "秒杀活动增删改查"],
    ["/api/admin/forum-posts", "论坛管理、隐藏/显示、删除"],
    ["/api/admin/core", "报名审核、成果审核、求职审核、诚信评分"],
    ["/api/files", "证明材料与简历文件上传"],
]

tables = [
    ["institution", "机构信息", "共享", "id、name、code、contact、phone、status"],
    ["learner", "用户/学员", "机构隔离", "username、real_name、phone、email、education_level、status、password、role、institution_id"],
    ["course", "课程资源", "机构隔离", "course_code、course_name、provider、category、credit_value、credit_point、resource_url、learning_stages"],
    ["course_enrollment", "课程报名", "机构隔离", "learner_id、course_id、enroll_status、reviewer、reviewed_at"],
    ["learning_record", "学习记录", "机构隔离", "learner_id、course_id、progress、score、result、completed_at"],
    ["credit_account", "积分账户", "共享/按学员归属", "learner_id、total_credits、available_credits、frozen_credits、account_status"],
    ["credit_transaction", "积分流水", "共享/按学员归属", "transaction_type、amount、balance_before、balance_after、source_type、source_no"],
    ["credit_product", "积分商品", "共享", "product_code、product_name、product_type、credit_price、stock、sold、course_id"],
    ["cart", "购物车", "共享/按学员归属", "learner_id、product_id、product_name、credit_price、num"],
    ["credit_order", "订单主表", "共享/按学员归属", "order_no、learner_id、account_id、total_amount、item_count、batch_no、close_time、order_status"],
    ["credit_order_detail", "订单明细", "共享", "order_id、order_no、product_id、product_name、credit_price、num、amount"],
    ["flash_sale", "秒杀活动", "共享", "product_id、product_name、origin_price、flash_price、stock、begin_time、end_time、status"],
    ["flash_sale_record", "秒杀记录", "共享", "flash_sale_id、product_id、learner_id、order_id"],
    ["forum_post", "论坛帖子", "机构隔离", "learner_id、title、content、section、reply_count、status、institution_id"],
    ["forum_reply", "论坛回复", "机构隔离", "post_id、learner_id、content、status、institution_id"],
    ["forum_post_like", "论坛点赞", "共享", "post_id、learner_id、created_at"],
    ["achievement", "成果认定", "机构隔离", "learner_id、achievement_name、achievement_type、suggested_credits、audit_status、proof_url"],
    ["sign_in_record", "签到记录", "机构隔离", "learner_id、sign_date、reward_credits、sign_type"],
    ["job_posting", "招聘岗位", "机构隔离", "company_name、position_name、city、requirement、contact、status"],
    ["job_application", "求职申请", "机构隔离", "job_id、learner_id、resume_summary、resume_url、apply_status、reviewer"],
    ["integrity_rating", "诚信评分", "机构隔离", "learner_id、score、level_name、learning_score、forum_score、signin_score、achievement_score、job_score"],
    ["blockchain_credential", "区块链存证", "机构隔离", "learner_id、credential_type、business_no、hash_value、chain_status、source_payload"],
]


def config_doc():
    body = h(2, "目录") + ul(["1. 简介", "2. 管理", "3. 配置管理活动", "4. 文件归档", "5. 里程碑"])
    body += h(2, "1. 简介")
    body += p(f"本文档说明 {PROJECT} 在实习项目开发、测试、部署和交付全过程中的配置管理活动。配置管理对象覆盖需求文档、设计文档、数据库脚本、后端源码、前端源码、测试用例、构建产物、运行配置和发布包。")
    body += h(3, "1.1 目的") + p("建立统一的配置项识别、版本控制、变更审批、基线发布和归档机制，确保项目交付物一致、可追溯、可恢复，避免多人协作过程中出现版本混乱、代码覆盖、数据库脚本丢失等问题。")
    body += h(3, "1.2 范围") + p("适用于项目策划、需求分析、架构设计、数据库设计、编码实现、系统测试、缺陷修复、部署演示和最终上交等阶段。")
    body += h(3, "1.3 缩略语") + table(["缩略语", "含义"], [["CM", "Configuration Management，配置管理"], ["CI", "Configuration Item，配置项"], ["CCB", "Change Control Board，变更控制委员会"], [SHORT, PROJECT], ["SRS", "Software Requirement Specification，软件需求规约"]])
    body += h(3, "1.4 参考资料") + ul(common_refs)
    body += h(2, "2. 管理")
    body += h(3, "2.1 组织结构") + table(["角色", "人员", "职责"], [["项目负责人", AUTHOR, "统筹开发计划、评审文档、协调风险"], ["配置管理员", AUTHOR, "维护 Git 仓库、分支、标签、发布包和归档目录"], ["开发人员", "项目组成员", "提交源码、数据库脚本、前端页面和接口实现"], ["测试人员", "项目组成员", "编写测试用例、执行测试、记录缺陷并回测"], ["评审人员", "指导老师/项目负责人", "对需求、设计、测试和发布物进行检查确认"]])
    body += h(3, "2.2 工具、环境和基础设施") + table(["类别", "工具", "用途"], [["版本控制", "Git", "源码、文档、SQL 脚本版本管理"], ["后端开发", "IntelliJ IDEA、JDK 21、Maven", "Spring Boot 后端开发与构建"], ["前端开发", "Node.js、Vite、Vue 3", "学员端和管理端页面开发"], ["数据库", "MySQL 8、Flyway", "数据模型管理和版本迁移"], ["缓存/消息", "Redis、RabbitMQ、Redisson", "Token、秒杀库存、分布式锁、异步订单"], ["测试", "JUnit、Spring Boot Test、JMeter、浏览器", "单元测试、接口测试、性能测试、兼容性测试"]])
    body += h(2, "3. 配置管理活动")
    body += h(3, "3.1 配置库结构") + pre("""lifelong-credit-bank/
├── src/main/java/org/csu/creditbank/      后端源码
├── src/main/resources/db/migration/       Flyway 数据库迁移脚本
├── src/main/resources/application.yml     运行配置
├── frontend/                              Vue 3 前端工程
├── docs/                                  项目说明与接口草稿
├── jmeter/                                秒杀压测脚本
└── generated-docs/                        本次生成的工程文档中间产物""")
    body += h(3, "3.2 权限划分") + table(["配置区", "权限", "说明"], [["主分支", "配置管理员合并", "只保存可运行、可演示的稳定版本"], ["开发分支", "开发人员提交", "用于功能开发和问题修复"], ["文档目录", "项目组维护", "需求、设计、测试文档统一编号"], ["发布目录", "配置管理员维护", "仅保存通过测试的发布包和数据库版本"]])
    body += h(3, "3.3 配置标识") + table(["配置项", "命名规则", "示例"], [["需求文档", "项目名_文档名_版本", "终身学习学分银行平台_软件需求规约_V1.0.docx"], ["数据库脚本", "V序号__说明.sql", "V17__backfill_course_product_course_id.sql"], ["后端版本", "major.minor.patch", "1.0.0"], ["发布包", "项目名_日期_版本", "lifelong-credit-bank_20260713_V1.0"]])
    body += h(3, "3.4 基线管理") + table(["基线", "形成时间", "包含内容"], [["需求基线", "需求评审通过后", "软件需求规约、用例说明、接口清单"], ["设计基线", "架构与数据库设计完成后", "系统构架设计说明书、数据库设计说明书"], ["编码基线", "核心功能开发完成后", "后端源码、前端源码、数据库迁移脚本"], ["测试基线", "系统测试执行前", "测试计划、测试用例、测试环境"], ["发布基线", "缺陷回归通过后", "发布包、部署说明、测试日志、例会纪要"]])
    body += h(3, "3.5 变更控制") + p("任意成员发现需求变化、缺陷修复、接口调整或数据库结构变更时，应提出变更申请，说明变更原因、影响范围、回退方案和预计完成时间。项目负责人评估后安排开发和测试，配置管理员在合并前检查是否同步更新文档、SQL 和测试记录。")
    body += h(3, "3.6 状态统计与审计") + p("每个阶段至少进行一次配置状态统计，检查代码是否可构建、数据库脚本是否连续、文档版本是否一致、测试缺陷是否关闭。发布前对 Git 提交记录、Maven 构建、前端构建、数据库迁移和核心接口进行审计。")
    body += h(2, "4. 文件归档") + p("最终归档内容包括 9 份工程文档、项目源码、数据库迁移脚本、前端构建产物、JMeter 脚本、README、接口说明和演示账号。归档介质采用本地磁盘与 Git 仓库双重保存。")
    body += h(2, "5. 里程碑") + table(["里程碑", "输出物", "完成标准"], [["项目启动", "项目开发计划", "明确范围、角色、进度"], ["需求完成", "软件需求规约", "功能需求和非功能需求可测试"], ["设计完成", "架构设计、数据库设计", "核心技术方案和表结构明确"], ["编码完成", "源码、SQL、前端页面", "核心业务可运行"], ["测试完成", "测试计划、用例、日志", "主要缺陷已关闭"], ["项目交付", "9 份文档和发布工程", "满足实习上交要求"]])
    return doc("配置管理计划书", "HD-CM-20260713-001", body)


def requirement_doc():
    body = h(2, "目录") + ul(["1. 引言", "2. 软件总体概述", "3. 具体需求", "4. 性能", "5. 接口", "6. 数据字典"])
    body += h(2, "1. 引言") + h(3, "1.1 目的") + p("本文档定义终身学习学分银行平台的业务需求、功能边界、角色权限、接口约束和质量要求，是项目设计、编码、测试和验收的依据。")
    body += h(3, "1.2 范围") + p("系统面向多机构继续教育场景，支持管理员进行课程、学员、积分、商城、论坛、招聘和成果认定管理，支持学员完成学习、积分兑换、论坛交流、成果申报和岗位申请。")
    body += h(3, "1.3 定义") + table(["术语", "说明"], [["学分银行", "对学习成果、课程学习和积分变化进行记录、累计、转换和消费的管理平台"], ["机构隔离", "不同机构管理员只能查看和处理本机构业务数据"], ["积分", "平台内用于课程激励、商城兑换和秒杀消费的虚拟权益"], ["秒杀", "在指定时间内以优惠积分兑换商品或课程的高并发活动"]])
    body += h(3, "1.4 参考资料") + ul(common_refs)
    body += h(2, "2. 软件总体概述")
    body += h(3, "2.1 软件标识") + table(["项目", "内容"], [["软件全称", PROJECT], ["软件简称", SHORT], ["版本号", "V1.0"], ["运行方式", "浏览器访问 Web 系统，后端提供 REST API"]])
    body += h(3, "2.2 系统属性") + p("本系统是基于 Java 21、Spring Boot 3、Vue 3 和 MySQL 8 的全栈 Web 应用，采用前后端分离开发方式，生产构建后可由 Spring Boot 静态资源目录统一托管前端页面。")
    body += h(3, "2.3 用户特点") + table(["用户", "特点", "关注点"], [["平台管理员", "熟悉后台管理操作", "数据隔离、统计准确、审核效率"], ["学员", "通过浏览器完成学习和兑换", "流程简单、反馈及时、积分透明"], ["测试/维护人员", "具备基础 Web 系统维护能力", "日志清晰、接口稳定、部署可重复"], ["指导老师/评审人员", "关注项目完整度和工程规范", "文档齐全、功能可演示、技术路线合理"]])
    body += h(3, "2.4 限制与约束") + table(["类别", "要求"], [["后端环境", "JDK 21、Maven、Spring Boot 3.3.6"], ["数据库", "MySQL 8，数据库名 lifelong_credit_bank，Flyway 自动迁移"], ["缓存和消息", "Redis 127.0.0.1:6379 database 4，RabbitMQ 127.0.0.1:5672"], ["前端环境", "Node.js 18+、Vue 3、Vite"], ["浏览器", "Chrome、Edge、Firefox 等现代浏览器"]])
    body += h(2, "3. 具体需求")
    for i, (name, desc) in enumerate(modules, 1):
        body += h(3, f"3.{i} {name}") + p(desc)
        body += table(["需求项", "说明", "优先级"], [[f"{name}查询/展示", f"用户进入对应页面后，系统应按角色和机构展示相关数据。", "高"], [f"{name}新增/处理", f"在满足权限与数据校验条件下，系统应保存业务数据并返回统一结果。", "高"], [f"{name}异常处理", f"对非法参数、权限不足、重复提交、资源不存在等情况给出明确提示。", "中"]])
    body += h(2, "4. 性能") + table(["指标", "要求"], [["普通查询响应", "单页列表查询在常规数据量下 3 秒内返回"], ["页面交互", "前端路由切换和表单操作应保持流畅"], ["秒杀请求", "库存校验必须在 Redis Lua 中原子完成，禁止超卖"], ["并发一致性", "秒杀下单采用 Redis 去重、Redisson 锁、RabbitMQ 异步消费和 MySQL CAS 多层保护"], ["安全", "密码采用盐值哈希存储，Token 存储于 Redis 并设置 24 小时有效期"]])
    body += h(2, "5. 接口") + table(["接口分组", "说明"], api_groups)
    body += h(2, "6. 数据字典") + table(["表名", "业务含义", "租户策略", "关键字段"], tables)
    return doc("软件需求规约", "HD-SRS-20260713-002", body)


def plan_doc():
    body = h(2, "目录") + ul(["1. 前言", "2. 项目概述", "3. 角色和职责", "4. 项目过程", "5. 工作任务分解", "6. 项目估计", "7. 培训计划", "8. 相关计划", "9. 工作环境"])
    body += h(2, "1. 前言") + p("本计划用于指导终身学习学分银行平台从需求分析到最终交付的全过程管理，明确项目目标、范围、进度、人员职责、风险控制和质量保证措施。")
    body += h(3, "1.1 术语与缩略语") + table(["缩略语", "说明"], [[SHORT, PROJECT], ["PM", "项目经理"], ["PPQA", "过程与产品质量保证"], ["CM", "配置管理"], ["API", "应用程序接口"]])
    body += h(2, "2. 项目概述") + h(3, "2.1 背景和目标") + p("继续教育、职业培训和学习成果认定场景中，课程学习、积分激励、成果审核和跨机构管理常常分散在不同系统。本项目通过统一平台实现学习资源管理、积分账户、商城兑换、论坛交流、招聘求职与成果认定，提高管理效率和学员参与度。")
    body += h(3, "2.2 项目范围") + p("本项目范围包括后端 REST API、Vue 前端页面、MySQL 数据库、Redis/RabbitMQ 中间件集成、秒杀高并发链路、AI 学习助手接口、测试脚本和工程文档。不包括真实区块链节点接入、生产级短信服务、线上支付网关和复杂组织架构审批流。")
    body += h(3, "2.3 交付产品") + table(["阶段", "交付工件", "类型", "计划日期"], [["项目策划", "项目开发计划、配置管理计划", "Word 文档", "2026-07-08"], ["需求分析", "软件需求规约", "Word 文档", "2026-07-09"], ["设计", "系统构架设计说明书、数据库设计说明书", "Word 文档", "2026-07-10"], ["编码", "后端源码、前端源码、数据库迁移脚本", "工程源码", "2026-07-11"], ["测试", "测试计划、测试用例、测试日志", "Word 文档", "2026-07-12"], ["交付", "9 份工程文档、可运行项目", "归档包", "2026-07-13"]])
    body += h(2, "3. 角色和职责") + table(["角色", "人员", "职责"], [["项目负责人", AUTHOR, "范围控制、任务分配、评审确认"], ["后端开发", "项目组成员", "Controller、Service、Mapper、Redis、RabbitMQ、权限和数据库迁移实现"], ["前端开发", "项目组成员", "Vue 页面、路由、表单、列表、商城、论坛和管理端界面实现"], ["测试人员", "项目组成员", "测试计划、测试用例、缺陷记录和回归测试"], ["配置管理员", AUTHOR, "版本管理、构建归档、文档编号和发布基线维护"]])
    body += h(2, "4. 项目过程") + table(["阶段", "主要活动", "输出物"], [["启动", "明确题目、技术栈、演示目标", "项目计划"], ["需求", "梳理角色、业务模块、接口需求", "需求规约"], ["设计", "确定分层架构、多租户、秒杀、数据库结构", "架构和数据库设计"], ["实现", "后端接口、前端页面、迁移脚本、测试数据", "源码和构建产物"], ["测试", "功能测试、异常测试、性能测试、兼容性测试", "测试计划、用例、日志"], ["交付", "整理文档、检查项目运行和归档", "最终交付包"]])
    body += h(2, "5. 工作任务分解") + table(["编号", "任务", "说明"], [["WBS-01", "认证与权限", "登录、登出、Token、角色拦截、机构上下文"], ["WBS-02", "学员与课程", "档案、课程、报名、一键学习、记录"], ["WBS-03", "积分与商城", "账户、流水、商品、购物车、订单"], ["WBS-04", "秒杀", "Redis Lua、MQ、Redisson、订单超时"], ["WBS-05", "论坛", "帖子、回复、点赞、后台审核"], ["WBS-06", "成果与签到", "证明上传、审核、签到奖励、诚信评分"], ["WBS-07", "招聘求职", "岗位、简历上传、投递、审核"], ["WBS-08", "前端页面", "登录页、管理端、学员端、商城、论坛、AI 助手"], ["WBS-09", "测试与文档", "用例、日志、工程文档、归档"]])
    body += h(2, "6. 项目估计") + table(["类别", "估计"], [["代码规模", "后端约 100 个 Java 文件，前端 Vue/Vite 工程，数据库迁移 V1-V17"], ["数据库规模", "22 张核心业务表，覆盖机构、学员、课程、积分、商城、论坛、招聘、存证"], ["开发周期", "约 1 周集中开发与整理"], ["风险储备", "预留 15% 时间处理数据库迁移、前后端接口联调和中间件环境问题"]])
    body += h(2, "7. 技能和培训计划") + table(["技能", "掌握要求", "培训方式"], [["Spring Boot 3", "能够编写 Controller、Service、异常处理和配置类", "源码讲解与接口调试"], ["Vue 3", "能够维护页面组件、状态和 API 调用", "页面走查"], ["MySQL/Flyway", "能够编写迁移脚本和理解表关系", "数据库设计评审"], ["Redis/RabbitMQ", "理解 Token、锁、库存和异步消费", "秒杀链路演示"], ["测试", "掌握功能用例、缺陷记录和回归方法", "测试用例评审"]])
    body += h(2, "8. 相关计划") + p("项目相关计划包括配置管理计划、质量保证计划、系统测试计划、风险管理计划、数据管理计划和需求管理计划。各计划互相引用，变更时同步更新版本号。")
    body += h(3, "8.1 风险管理") + table(["风险", "影响", "应对"], [["Redis/RabbitMQ 未启动", "秒杀、Token 或异步订单不可用", "启动前检查服务状态，提供普通功能演示路径"], ["数据库迁移失败", "系统无法启动", "按 Flyway 版本顺序检查 SQL，保留初始化脚本"], ["前后端字段不一致", "页面保存或展示异常", "以接口返回和 DTO 为准进行联调"], ["AI Key 缺失", "AI 助手不可用", "配置默认 false，保证主系统不受影响"]])
    body += h(2, "9. 工作环境") + table(["环境", "配置"], [["开发环境", "Ubuntu/Linux、IntelliJ IDEA、JDK 21、Maven、Node.js 18+"], ["运行环境", "Spring Boot 8080、MySQL 8、Redis、RabbitMQ"], ["测试环境", "Chrome/Edge 浏览器、JMeter、Maven Test"], ["配置文件", "src/main/resources/application.yml，frontend/vite.config.js"]])
    return doc("项目开发计划", "HD-PP-20260713-003", body)


def db_doc():
    body = h(2, "目录") + ul(["1. 引言", "2. 外部设计", "3. 结构设计", "4. 运用设计", "5. 安全保密设计"])
    body += h(2, "1. 引言") + h(3, "1.1 编写目的") + p("本文档描述终身学习学分银行平台的数据库设计方案，包括命名规则、表结构、核心关系、数据隔离策略、迁移管理和安全设计，为后续开发、测试、部署和维护提供依据。")
    body += h(3, "1.2 背景") + p("系统使用 MySQL 8 作为主数据库，Flyway 管理数据库版本。业务覆盖学员、课程、积分、商城、秒杀、论坛、成果认定、签到、招聘、存证等模块，对数据一致性和多机构隔离有较高要求。")
    body += h(3, "1.3 定义") + table(["术语", "说明"], [["PDM", "物理数据模型"], ["多租户", "通过 institution_id 区分机构数据"], ["共享表", "跨机构共用或通过 learner_id 间接归属的数据表"], ["逻辑删除", "使用 deleted 字段标记删除状态"]])
    body += h(2, "2. 外部设计") + h(3, "2.1 类型划分") + table(["数据库区域", "表", "说明"], [["基础数据", "institution、learner", "机构和用户基础信息"], ["学习业务", "course、course_enrollment、learning_record、achievement、sign_in_record", "课程、学习、成果和签到"], ["积分业务", "credit_account、credit_transaction", "积分账户和流水"], ["商城业务", "credit_product、cart、credit_order、credit_order_detail、flash_sale、flash_sale_record", "兑换、订单和秒杀"], ["互动与就业", "forum_post、forum_reply、forum_post_like、job_posting、job_application", "论坛交流和岗位申请"], ["扩展能力", "integrity_rating、blockchain_credential", "诚信评分和存证"]])
    body += h(3, "2.2 标识符和约定") + ul(["表名和字段名使用小写字母加下划线，例如 credit_transaction。", "实体类使用驼峰命名并通过 MyBatis-Plus 自动映射。", "主键统一使用 BIGINT 自增 id。", "通用字段 created_at、updated_at、deleted 由 BaseEntity 和自动填充处理。", "机构隔离表包含 institution_id 字段，共享表由配置跳过租户插件或通过 learner_id 关联过滤。"])
    body += h(3, "2.3 支持软件") + table(["软件", "版本/说明"], [["MySQL", "8.x"], ["Flyway", "Spring Boot 集成，迁移目录 classpath:db/migration"], ["MyBatis-Plus", "3.5.9，多租户、分页、逻辑删除"], ["Redis", "Token、秒杀库存、去重集合"], ["RabbitMQ", "秒杀订单异步处理和订单超时"]])
    body += h(2, "3. 结构设计") + table(["表名", "业务含义", "租户策略", "关键字段"], tables)
    body += h(3, "3.1 主要关系") + table(["关系", "说明"], [["institution 1 - N learner/course/forum_post", "机构拥有学员、课程和论坛内容"], ["learner 1 - 1 credit_account", "每个学员对应一个积分账户"], ["learner 1 - N credit_transaction", "学员积分变更形成流水"], ["course 1 - N learning_record", "课程学习产生学习记录"], ["course 1 - N course_enrollment", "课程报名记录审核状态"], ["credit_order 1 - N credit_order_detail", "订单主表保存汇总，明细保存商品快照"], ["credit_product 1 - N flash_sale", "商品可配置秒杀活动"], ["forum_post 1 - N forum_reply/forum_post_like", "帖子包含回复和点赞记录"], ["job_posting 1 - N job_application", "岗位对应多个投递申请"], ["achievement 1 - N blockchain_credential", "成果或业务编号可生成存证记录"]])
    body += h(3, "3.2 迁移脚本") + table(["版本", "说明"], [["V1", "初始化学员、课程、学习记录、积分账户、成果、论坛、签到、招聘、存证基础表"], ["V2", "新增积分商品、订单和积分交易扩展字段"], ["V3-V4", "新增认证字段和种子用户"], ["V5-V8", "购物车、商城增强、秒杀、论坛点赞"], ["V9-V10", "机构表和多租户字段、修复 student001 角色"], ["V11-V12", "课程学习资源、报名、论坛回复、求职申请、诚信评分"], ["V13-V14", "多机构管理员账号和演示学员编号整理"], ["V15-V17", "课程商品关联、简历文件、已购课程报名补齐"]])
    body += h(2, "4. 运用设计") + h(3, "4.1 数据访问") + p("后端使用 Mapper、Service、Controller 三层访问数据库。MyBatis-Plus 根据实体类与表名映射生成常用 CRUD，复杂业务在 ServiceImpl 中组合事务、积分流水、库存更新和状态检查。")
    body += h(3, "4.2 多租户过滤") + p("登录成功后 AuthInterceptor 将用户机构写入 InstitutionContext，MyBatis-Plus TenantLineInnerInterceptor 自动向隔离表追加 institution_id 条件。积分商城、订单、购物车、秒杀等共享表按业务规则跳过租户插件，并在 Controller 或 Service 中根据 learner_id 进行归属控制。")
    body += h(3, "4.3 秒杀一致性") + p("秒杀库存先由 Redis Lua 原子判断库存和重复购买，成功后发送 RabbitMQ 消息；消费者使用 Redis 防重、Redisson 分布式锁和 MySQL stock > 0 条件更新，保证高并发下不超卖、不重复下单。")
    body += h(2, "5. 安全保密设计") + ul(["密码字段只保存盐值哈希，不保存明文密码。", "Token 存储于 Redis 并设置有效期，退出登录时删除。", "文件上传按 proofs 和 resumes 分目录保存，接口限制大小。", "逻辑删除保留历史数据，便于审计和恢复。", "管理员查询学员、课程、论坛、成果和招聘数据时必须受机构上下文限制。"])
    return doc("数据库设计说明书", "HD-DB-20260713-004", body)


def arch_doc():
    body = h(2, "目录") + ul(["1. 简介", "2. 构架表示方式", "3. 构架目标和约束", "4. 关键用例视图", "5. 层次结构", "6. 逻辑视图", "7. 部署视图", "8. 接口设计"])
    body += h(2, "1. 简介") + p("本文档从架构角度描述终身学习学分银行平台的整体结构、关键技术决策、核心用例、分层设计、部署关系和接口组织方式。")
    body += h(3, "1.1 目的") + p("记录系统架构设计，帮助开发、测试和维护人员理解系统边界、模块职责、数据流和关键非功能机制。")
    body += h(3, "1.2 参考资料") + ul(common_refs)
    body += h(2, "2. 构架表示方式") + p("本文档采用用例视图、逻辑视图、部署视图和接口视图描述系统。由于项目规模适中，进程视图重点放在秒杀异步订单和前后端 HTTP 交互。")
    body += h(2, "3. 构架目标和约束") + table(["目标", "设计措施"], [["易用性", "Vue 单页应用区分管理员端和学员端，表单和列表集中展示"], ["可维护性", "后端按 Controller、Service、Mapper、Entity、DTO 分层"], ["安全性", "Token 拦截、角色判断、机构隔离、密码哈希"], ["一致性", "积分变更同时写账户和流水，订单保留商品价格快照"], ["高并发", "秒杀链路采用 Redis Lua、RabbitMQ、Redisson 和 MySQL CAS"], ["可扩展", "模块配置、Flyway 迁移、共享表与隔离表策略分离"]])
    body += h(2, "4. 关键用例视图") + table(["用例", "参与者", "主要流程"], [["登录认证", "管理员/学员", "输入账号密码，系统校验哈希，生成 Token，写入 Redis"], ["一键学习", "学员", "选择课程，系统检查重复学习，生成学习记录并发放积分"], ["积分兑换", "学员", "浏览商品，加购物车或直接下单，确认支付后扣减积分"], ["秒杀下单", "学员", "Redis 预扣库存，消息队列异步创建订单，超时取消回补库存"], ["成果认定", "学员/管理员", "上传证明，提交审核，管理员通过或驳回"], ["论坛交流", "学员/管理员", "发帖、回复、点赞，管理员隐藏违规内容"], ["招聘申请", "学员/管理员", "查看岗位，上传简历并投递，管理员审核处理"], ["多机构管理", "管理员", "登录后仅管理本机构数据"]])
    body += h(2, "5. 层次结构") + pre("""浏览器 Vue 3 前端
        |
        | HTTP/JSON/SSE
        v
Spring Boot Controller 层
        |
        v
Service 业务层：权限、积分、订单、秒杀、审核、学习
        |
        v
Mapper 数据访问层：MyBatis-Plus
        |
        +--> MySQL 8：业务数据与 Flyway 迁移
        +--> Redis：Token、库存、去重、锁辅助
        +--> RabbitMQ：秒杀订单与超时消息
        +--> 文件目录：证明材料、简历文件""")
    body += h(2, "6. 逻辑视图")
    body += h(3, "6.1 用户服务层（Controller）") + table(["包/类", "职责"], api_groups)
    body += h(3, "6.2 业务逻辑层（Service）") + table(["服务", "职责"], [["LearnerService", "学员档案、状态和机构归属"], ["CourseService", "课程资源和课程商品同步"], ["LearningRecordService", "学习记录、一键学习和积分奖励"], ["CreditAccountService", "积分账户、增加、消费、冻结和解冻"], ["CreditOrderService", "订单创建、支付、发货、取消、退款"], ["FlashSaleService/FlashSaleOrderService", "秒杀活动、库存同步、异步下单"], ["ForumPostService/ForumReplyService", "论坛帖子、回复和状态管理"], ["JobPostingService/JobApplicationService", "岗位和投递申请"], ["IntegrityRatingService", "诚信评分计算"], ["BlockchainCredentialService", "存证查询与校验"]])
    body += h(3, "6.3 数据服务层（Mapper/Entity）") + p("每张核心表对应 Entity 和 Mapper，MyBatis-Plus 负责基础 CRUD、分页、逻辑删除和驼峰映射。BaseEntity 统一维护创建时间、更新时间和删除标记。")
    body += h(3, "6.4 公共支撑层") + table(["组件", "职责"], [["AuthInterceptor", "解析 Token、设置当前用户和机构上下文"], ["InstitutionContext", "ThreadLocal 保存当前机构 ID"], ["MybatisPlusConfig", "分页、租户插件和共享表规则"], ["PasswordUtil", "盐值哈希密码"], ["TokenUtil", "Token 生成"], ["GlobalExceptionHandler", "统一异常响应"], ["OrderMqConfig/FlashSaleMqConfig", "消息队列、死信队列和 TTL 配置"]])
    body += h(2, "7. 部署视图") + table(["节点", "组件", "端口/说明"], [["浏览器", "Vue 页面", "访问 http://localhost:8080"], ["应用服务器", "Spring Boot", "8080，提供 REST API 和静态资源"], ["数据库服务器", "MySQL", "3306，lifelong_credit_bank"], ["缓存服务器", "Redis", "6379，database 4"], ["消息服务器", "RabbitMQ", "5672，管理后台 15672"], ["文件存储", "uploads/proofs、uploads/resumes", "证明和简历文件"]])
    body += h(2, "8. 接口设计") + p("系统接口统一返回 ApiResult，成功 code 为 200，失败由全局异常处理器返回错误信息。前端通过 Token 访问受保护接口，AI 学习助手接口使用 text/stream 进行 SSE 风格流式输出。")
    body += table(["接口分组", "说明"], api_groups)
    return doc("系统构架设计说明书", "HD-ARCH-20260713-005", body)


def test_plan_doc():
    body = h(2, "目录") + ul(["1. 简介", "2. 测试需求", "3. 测试策略", "4. 资源", "5. 项目测试活动", "6. 附录"])
    body += h(2, "1. 简介") + h(3, "1.1 目的") + p("本测试计划用于确定终身学习学分银行平台需要测试的功能、质量指标、测试策略、测试工具、资源安排和通过准则，指导测试人员有序完成系统测试。")
    body += h(3, "1.2 背景") + p("平台集成多机构、积分账户、商城订单、秒杀、论坛、成果审核、招聘和 AI 学习助手等模块，既需要验证常规业务流程，也需要重点检查权限隔离、积分一致性和秒杀并发安全。")
    body += h(3, "1.3 范围") + p("测试范围包括后端 API、前端页面、数据库迁移、登录权限、核心业务流程、异常提示、浏览器兼容性和秒杀压测。不测试真实第三方 AI 服务稳定性、真实区块链节点和生产环境网络安全攻防。")
    body += h(3, "1.4 缺陷级别") + table(["级别", "说明"], [["一级", "系统无法启动、核心数据损坏、严重安全问题或主流程完全不可用"], ["二级", "核心功能严重异常但存在临时绕行方式"], ["三级", "普通功能错误、数据展示不准确或局部流程中断"], ["四级", "界面、提示、兼容性等影响使用体验的问题"], ["五级", "文字、样式、文档类轻微问题"]])
    body += h(3, "1.5 文档引用") + ul(common_refs)
    body += h(2, "2. 测试需求")
    body += h(3, "2.1 功能测试需求") + table(["测试需求", "测试简介", "优先级"], [[m[0], m[1], "1" if i < 10 else "2"] for i, m in enumerate(modules)])
    body += h(3, "2.2 非功能测试需求") + table(["测试项", "条件", "指标"], [["性能", "普通列表和详情查询", "3 秒内返回结果"], ["秒杀一致性", "多用户并发请求同一活动", "不超卖、不重复下单"], ["兼容性", "Chrome、Edge、Firefox", "页面正常显示，主要流程可操作"], ["安全性", "无 Token、错误角色、跨机构访问", "拒绝访问或仅返回授权数据"], ["可靠性", "Redis/RabbitMQ 可用时", "订单异步处理可完成，异常可记录"]])
    body += h(2, "3. 测试策略") + table(["测试类型", "方法", "完成标准"], [["功能测试", "按测试用例执行黑盒测试，覆盖合法、非法、边界和权限场景", "计划用例全部执行，一级/二级缺陷关闭"], ["接口测试", "使用浏览器开发者工具、Swagger 或 HTTP 客户端检查 API", "统一响应格式正确，参数校验有效"], ["性能测试", "使用 jmeter/seckill-stress-test.jmx 对秒杀接口压测", "库存不为负，记录不重复，系统不崩溃"], ["兼容性测试", "在主流浏览器中执行登录、学习、商城和后台流程", "页面无明显错位，功能可用"], ["回归测试", "缺陷修复后重测相关模块和相邻流程", "缺陷状态为合格"]])
    body += h(3, "3.2 测试工具") + table(["名称", "工具", "用途"], [["浏览器", "Chrome/Edge/Firefox", "页面和接口联调"], ["接口文档", "Swagger UI", "接口查看和手工调用"], ["构建测试", "Maven Test", "后端单元/集成测试"], ["性能测试", "JMeter", "秒杀压测"], ["数据库", "MySQL 客户端", "检查表结构、迁移和数据状态"]])
    body += h(3, "3.3 通过准则") + ul(["系统能够完成管理员和学员登录。", "管理员只能查看和操作本机构数据。", "学员能够完成课程学习、积分获取、商城下单、订单支付和论坛发帖。", "成果审核、招聘申请和签到流程可执行。", "秒杀活动不发生超卖和重复下单。", "一级、二级缺陷关闭率 100%，三级缺陷关闭率不低于 90%。"])
    body += h(2, "4. 资源") + table(["角色", "人员", "工作"], [["测试负责人", AUTHOR, "制定计划、分配任务、汇总缺陷"], ["功能测试", "项目组成员", "执行业务流程和异常用例"], ["性能测试", "项目组成员", "执行 JMeter 脚本并观察库存和订单"], ["开发支持", "项目组成员", "定位并修复缺陷"]])
    body += h(2, "5. 项目测试活动") + table(["活动", "输入", "输出"], [["计划测试", "需求规约、开发计划", "测试计划"], ["设计测试", "接口和页面", "测试用例"], ["执行测试", "可运行系统", "测试日志和缺陷记录"], ["评估测试", "缺陷回归结果", "测试结论"]])
    body += h(2, "6. 附录A：项目任务") + p("测试执行顺序建议为：环境启动检查、数据库迁移检查、登录权限测试、管理员端主流程、学员端主流程、积分商城、秒杀压测、论坛与招聘、成果与签到、兼容性检查、缺陷回归。")
    return doc("测试计划", "HD-TP-20260713-006", body)


cases = [
    ["TC-001", "合法账号登录", "已存在 admin001 或 student001，密码 123456", "输入账号密码并点击登录", "登录成功并进入对应角色首页"],
    ["TC-002", "错误密码登录", "用户进入登录页", "输入正确账号和错误密码", "提示用户名或密码错误，不生成 Token"],
    ["TC-003", "无 Token 访问后台", "未登录", "直接请求 /api/admin/learners", "系统拒绝访问"],
    ["TC-004", "管理员查看本机构学员", "admin001 登录", "进入学员档案列表", "仅展示本机构 STUDENT 学员"],
    ["TC-005", "新增课程资源", "管理员登录", "填写课程编码、名称、积分、资源链接并保存", "课程创建成功并出现在列表"],
    ["TC-006", "学员一键学习", "student001 登录且课程已发布", "进入我的课程，点击学习", "生成学习记录并增加积分"],
    ["TC-007", "重复学习检测", "学员已完成某课程", "再次点击同一课程学习", "系统提示不能重复学习或不重复发放积分"],
    ["TC-008", "积分账户开户", "学员无账户", "调用开户功能", "生成可用积分为 0 的账户"],
    ["TC-009", "管理员增加积分", "管理员登录", "选择学员、输入积分和来源", "账户余额增加，流水记录正确"],
    ["TC-010", "积分不足支付订单", "学员积分低于订单金额", "提交支付", "支付失败并提示积分不足"],
    ["TC-011", "商品加入购物车", "商品上架且库存充足", "点击加入购物车", "购物车出现对应商品，数量正确"],
    ["TC-012", "购物车数量修改", "购物车已有商品", "增加或减少数量", "小计和总计同步更新"],
    ["TC-013", "单商品下单支付", "商品上架且账户积分充足", "立即兑换并确认支付", "订单状态变为已支付，扣减积分和库存"],
    ["TC-014", "订单取消", "存在待支付订单", "点击取消订单", "订单状态变为已取消，库存不异常"],
    ["TC-015", "秒杀活动列表", "存在进行中的活动", "进入秒杀专区", "展示活动、秒杀价、库存和时间"],
    ["TC-016", "秒杀成功", "活动有效、库存大于 0、学员未购买", "点击秒杀", "返回排队/成功结果，异步生成订单"],
    ["TC-017", "秒杀重复购买", "同一学员已参与同一活动", "再次点击秒杀", "系统提示不能重复购买"],
    ["TC-018", "秒杀超卖检查", "库存有限，并发请求", "使用 JMeter 并发压测", "成功订单数不超过库存，库存不为负"],
    ["TC-019", "发帖", "学员登录", "填写标题、板块和内容后发布", "帖子发布成功并在列表可见"],
    ["TC-020", "帖子点赞取消", "帖子存在", "点击点赞，再取消点赞", "点赞数先增加后减少，用户状态正确"],
    ["TC-021", "管理员隐藏帖子", "管理员登录且帖子存在", "修改帖子状态为隐藏", "学员端不再展示该帖子"],
    ["TC-022", "成果提交", "学员登录", "上传证明文件并提交成果认定", "成果状态为待审核"],
    ["TC-023", "成果审核通过", "管理员登录且有待审核成果", "审核通过并填写意见", "成果状态更新，通过后可产生积分记录"],
    ["TC-024", "每日签到", "学员当天未签到", "点击今日签到", "生成签到记录并发放签到积分"],
    ["TC-025", "重复签到", "学员当天已签到", "再次签到", "系统提示今日已签到"],
    ["TC-026", "岗位发布", "管理员登录", "新增岗位公司、职位、城市和要求", "岗位发布成功"],
    ["TC-027", "简历投递", "学员登录且岗位有效", "上传简历并投递", "生成求职申请，状态为待处理"],
    ["TC-028", "求职审核", "管理员登录且有投递记录", "审核通过或驳回", "申请状态和审核人更新"],
    ["TC-029", "区块链凭证查询", "存在 businessNo", "请求凭证查询接口", "返回哈希、状态和校验时间"],
    ["TC-030", "AI 学习助手", "配置 AI Key 或服务可用", "发送学习问题", "接口返回流式文本；未配置时主系统不受影响"],
]


def test_case_doc():
    body = h(2, "一、测试环境") + table(["项目", "配置"], [["操作系统", "Windows 11 / Linux"], ["浏览器", "Chrome、Edge、Firefox"], ["后端", "JDK 21、Spring Boot 3.3.6、Maven"], ["数据库", "MySQL 8、Redis、RabbitMQ"], ["前端", "Vue 3、Vite"]])
    body += h(2, "二、测试用例")
    body += table(["序号", "测试用例", "前提条件", "测试步骤", "预期输出"], cases)
    body += h(2, "三、说明") + ul(["测试用例覆盖登录权限、机构隔离、课程学习、积分账户、商城订单、秒杀、论坛、成果、签到、招聘、存证和 AI 助手。", "执行过程中若实际输出与预期不一致，应记录到测试日志并标明缺陷级别、优先级和回测结果。", "涉及秒杀并发的用例可结合 jmeter/seckill-stress-test.jmx 执行。"])
    return doc("测试用例", "HD-TC-20260713-007", body)


def test_log_doc():
    logs = [
        ["2026-07-10", "TL-001", "用户登录", "二", "A", "错误密码登录时提示不明确", "统一异常处理返回“用户名或密码错误”，前端展示消息", "合格"],
        ["2026-07-10", "TL-002", "权限拦截", "一", "A", "无 Token 访问部分接口时未统一返回未登录", "补充拦截器路径和前端跳转处理", "合格"],
        ["2026-07-10", "TL-003", "机构隔离", "一", "A", "管理员列表可能看到非本机构学习记录", "检查 TenantLineInnerInterceptor 和共享表过滤逻辑", "合格"],
        ["2026-07-11", "TL-004", "课程学习", "二", "A", "重复点击一键学习可能重复发放积分", "Service 增加学习记录存在性判断", "合格"],
        ["2026-07-11", "TL-005", "积分流水", "二", "A", "积分消费后流水缺少变动前余额", "保存流水时补充 balanceBefore 和 balanceAfter", "合格"],
        ["2026-07-11", "TL-006", "购物车", "三", "B", "批量删除后页面数量未刷新", "前端删除成功后重新加载购物车", "合格"],
        ["2026-07-11", "TL-007", "订单支付", "二", "A", "积分不足时订单状态提示不清晰", "后端返回业务异常，前端保留待支付状态", "合格"],
        ["2026-07-12", "TL-008", "秒杀", "一", "A", "并发场景下存在重复请求进入 MQ 的风险", "增加 Redis 去重集合和 Redisson 一人一单锁", "合格"],
        ["2026-07-12", "TL-009", "论坛点赞", "三", "B", "取消点赞后列表点赞数未立即同步", "前端更新本地状态并重新请求详情", "合格"],
        ["2026-07-12", "TL-010", "成果上传", "二", "A", "大文件上传无明确限制提示", "配置 multipart 限制并补充错误提示", "合格"],
        ["2026-07-12", "TL-011", "招聘投递", "三", "B", "简历文件名未在申请列表展示", "数据库增加 resume_file_name 字段并回显", "合格"],
        ["2026-07-13", "TL-012", "AI 助手", "四", "C", "未配置 API Key 时启动存在风险", "配置默认 false 并按 Bean 条件加载", "合格"],
    ]
    body = h(1, "测试日志")
    body += table(["日期", "测试人员", "编号", "所属项目"], [[DATE, AUTHOR, "TL-SUMMARY-001", PROJECT]])
    body += h(2, "一、缺陷记录")
    body += table(["日期", "编号", "测试对象名称", "错误级别", "优先级", "操作步骤及现象", "错误修改及原因简述", "回测"], logs)
    body += h(2, "二、测试结论") + p("本轮测试覆盖登录认证、权限隔离、课程学习、积分账户、商城订单、秒杀、论坛、成果、签到、招聘投递、文件上传和 AI 助手等核心模块。已记录缺陷均完成修改并通过回测，系统达到实习项目演示和文档上交要求。")
    body += h(2, "三、备注") + ul(["错误类型按照失效等级划分为一、二、三、四、五级。", "优先级划分为 A 高、B 中、C 低。", "回测栏根据缺陷修复后的复测结果填写合格或不合格。", f"确认人：{AUTHOR}"])
    return doc("测试日志", "HD-TL-20260713-008", body)


def meeting_doc():
    meetings = [
        ["2026年07月08日", "17:00", "实训教室/线上会议", "项目启动与需求拆分", "明确项目选题、技术栈、角色分工和文档清单。", "完成项目范围确认；后端负责认证、学员、课程和积分；前端负责登录、管理端和学员端；测试人员准备测试计划。", "问题：模块范围较大。解决：优先保障登录、课程、积分、商城和论坛主流程。"],
        ["2026年07月09日", "15:00", "实训教室/线上会议", "需求评审与数据库设计", "梳理多机构、管理员/学员权限、积分账户、商城订单和成果审核。", "完成需求规约初稿；确定 MySQL + Flyway；列出核心表和租户字段。", "问题：共享表与机构隔离容易混淆。解决：隔离表使用 institution_id，共享表通过 learner_id 控制归属。"],
        ["2026年07月10日", "15:00", "实训教室/线上会议", "架构设计与基础功能联调", "检查 Spring Boot 分层、Token 拦截、Vue 页面结构和接口分组。", "完成登录、学员档案、课程列表、积分账户基础接口联调。", "问题：前后端字段命名不一致。解决：以接口返回 DTO 和实体字段为准统一命名。"],
        ["2026年07月11日", "16:00", "实训教室/线上会议", "商城、订单和论坛完善", "推进积分商城、购物车、订单支付、论坛发帖和点赞。", "完成商品上架、购物车结算、订单支付/取消、帖子管理和点赞取消。", "问题：订单支付涉及积分扣减和库存。解决：Service 内统一事务处理并写入积分流水。"],
        ["2026年07月12日", "15:30", "实训教室/线上会议", "秒杀与核心业务测试", "检查 Redis Lua、RabbitMQ、Redisson、JMeter 压测脚本和缺陷修复。", "完成秒杀预扣库存、异步下单、防重复和订单超时处理测试。", "问题：本地中间件启动顺序影响测试。解决：测试前先检查 MySQL、Redis、RabbitMQ，再启动后端。"],
        ["2026年07月13日", "10:00", "实训教室/线上会议", "文档整理与项目交付", "整理 9 份工程文档，核对 README、接口、数据库脚本和演示账号。", "完成配置管理计划、需求规约、开发计划、数据库设计、架构设计、测试计划、测试用例、测试日志和例会纪要。", "问题：文档需要贴合源码。解决：依据项目目录、Controller、Entity、迁移脚本和 README 补充内容。"],
    ]
    body = h(1, "例会纪要")
    for m in meetings:
        body += h(2, f"会议记录：{m[0]}")
        body += table(["项目", "内容"], [["日期 Date", m[0]], ["时间 Time", m[1]], ["地点 ADD.", m[2]], ["主题", m[3]], ["主持人", AUTHOR], ["出席者", f"{AUTHOR}、项目组成员"]])
        body += table(["工作计划", "完成情况", "问题及解决方案"], [[m[4], m[5], m[6]]])
    return doc("项目例会纪要", "HD-MOM-20260713-009", body)


def main():
    OUT.mkdir(parents=True, exist_ok=True)
    docs = [
        ("1.配置管理计划书.html", config_doc()),
        ("2.软件需求规约.html", requirement_doc()),
        ("3.项目开发计划.html", plan_doc()),
        ("4.数据库设计说明书.html", db_doc()),
        ("5.系统构架设计说明书.html", arch_doc()),
        ("6.测试计划.html", test_plan_doc()),
        ("7.测试用例.html", test_case_doc()),
        ("8.测试日志.html", test_log_doc()),
        ("9.项目例会纪要.html", meeting_doc()),
    ]
    for name, content in docs:
        (OUT / name).write_text(content, encoding="utf-8")
    print(f"generated {len(docs)} html files in {OUT}")


if __name__ == "__main__":
    main()

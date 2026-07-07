-- ============================================================
-- V4: 种子用户数据 — 管理员 + 学生 + 积分账户
-- ============================================================

-- 密码统一为 123456

-- 管理员
INSERT INTO learner (username, real_name, phone, email, education_level, status, password, role, created_at, updated_at, deleted)
VALUES
('admin001', '李管理', '13800000002', 'admin001@creditbank.edu', '硕士', 'ACTIVE', '6c67f9cd42e9253bd95005403015e96e:76c1921011cc00193bc6b3d6f866d483fb7666ce2c17dc7f61dbf11c38dbd181', 'ADMIN', NOW(), NOW(), 0),
('admin002', '王主任', '13800000003', 'admin002@creditbank.edu', '博士', 'ACTIVE', 'f765e290d722c6a757f2a2b64dfd9224:ce1969884a34d39944685dddd879663c0bb52c371a30f798711994522201e943', 'ADMIN', NOW(), NOW(), 0);

-- 学生
INSERT INTO learner (username, real_name, phone, email, education_level, status, password, role, created_at, updated_at, deleted)
VALUES
('student002', '赵小红', '13900001001', 'zhao@example.com', '本科', 'ACTIVE', '5bb6d2397b0a466208c74663a55a2060:d4a11c36b19d7a931bae1fcf68085df26a881200a6d2bd0fc69ba4533978ab64', 'STUDENT', NOW(), NOW(), 0),
('student003', '钱小刚', '13900001002', 'qian@example.com', '大专', 'ACTIVE', '810fd513f4d2bea769a3ec747f2f2591:756f6d483e505ef485b99197fb862c2d63332b3cdde56865ffc0ae7e28c09e52', 'STUDENT', NOW(), NOW(), 0),
('student004', '孙小美', '13900001003', 'sun@example.com', '本科', 'ACTIVE', 'ed94d98e48d3ef14a6e71ddb5e1831e0:fce4d4814ab28edd5d009a27b04ac2bb1df275574c58b27b230a5f7719a8c1a6', 'STUDENT', NOW(), NOW(), 0),
('student005', '周小军', '13900001004', 'zhou@example.com', '高中', 'ACTIVE', '695aced674f428dd929d9a43d21fbbbc:75d7822130b4e7d1f50aeae74360817321da551379602121ca4dd7628f41f9c5', 'STUDENT', NOW(), NOW(), 0);

-- 为新学生开通积分账户并赠初始积分
INSERT INTO credit_account (learner_id, total_credits, available_credits, frozen_credits, account_status, created_at, updated_at, deleted)
VALUES
(4, 80, 80, 0, 'ACTIVE', NOW(), NOW(), 0),
(5, 100, 100, 0, 'ACTIVE', NOW(), NOW(), 0),
(6, 50, 50, 0, 'ACTIVE', NOW(), NOW(), 0),
(7, 30, 30, 0, 'ACTIVE', NOW(), NOW(), 0);

-- 记录初始积分流水（发放）
INSERT INTO credit_transaction (learner_id, account_id, transaction_type, amount, balance_before, balance_after, source_type, source_no, remark, created_at, updated_at, deleted)
VALUES
(4, 2, 'INCREASE', 80, 0, 80, 'ADMIN_GRANT', 'INIT-002', '新用户初始积分', NOW(), NOW(), 0),
(5, 3, 'INCREASE', 100, 0, 100, 'ADMIN_GRANT', 'INIT-003', '新用户初始积分', NOW(), NOW(), 0),
(6, 4, 'INCREASE', 50, 0, 50, 'ADMIN_GRANT', 'INIT-004', '新用户初始积分', NOW(), NOW(), 0),
(7, 5, 'INCREASE', 30, 0, 30, 'ADMIN_GRANT', 'INIT-005', '新用户初始积分', NOW(), NOW(), 0);

-- 给张三补一些流水（演示用）
INSERT INTO credit_transaction (learner_id, account_id, transaction_type, amount, balance_before, balance_after, source_type, source_no, remark, created_at, updated_at, deleted)
VALUES
(1, 1, 'CONSUME', 10, 30, 20, 'ORDER_PAY', 'DEMO-001', '兑换学习笔记本套装', DATE_ADD(NOW(), INTERVAL -3 DAY), NOW(), 0),
(1, 1, 'INCREASE', 15, 20, 35, 'COURSE_COMPLETE', 'COURSE-JAVA-001', '完成Java程序设计基础', DATE_ADD(NOW(), INTERVAL -2 DAY), NOW(), 0),
(1, 1, 'INCREASE', 5, 35, 40, 'DAILY_SIGNIN', 'SIGN-20260701', '每日签到奖励', DATE_ADD(NOW(), INTERVAL -1 DAY), NOW(), 0);

-- 学员账户回正（张三当前余额需要匹配流水）
UPDATE credit_account SET total_credits = 40, available_credits = 40 WHERE id = 1;

-- 为赵小红创建一条兑换订单记录
INSERT INTO credit_order (order_no, learner_id, account_id, product_id, product_name, credit_amount, order_status, paid_at, delivered_at, created_at, updated_at, deleted)
VALUES
('CO202607040010001234', 4, 2, 3, '学习笔记本套装', 20, 'DELIVERED', DATE_ADD(NOW(), INTERVAL -2 DAY), DATE_ADD(NOW(), INTERVAL -1 DAY), DATE_ADD(NOW(), INTERVAL -2 DAY), NOW(), 0);

-- 更新赵小红的账户余额（扣除已消费的20积分）
UPDATE credit_account SET total_credits = 60, available_credits = 60 WHERE learner_id = 4;

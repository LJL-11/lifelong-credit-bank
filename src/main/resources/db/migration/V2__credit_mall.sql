-- ============================================================
-- V2: 积分商城 —— 商品、订单、流水增强
-- ============================================================

-- 1. 积分商品表
CREATE TABLE credit_product (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_code VARCHAR(64) NOT NULL UNIQUE,
    product_name VARCHAR(128) NOT NULL,
    product_type VARCHAR(32) NOT NULL DEFAULT 'MERCHANDISE',
    credit_price INT NOT NULL,
    description TEXT,
    image_url VARCHAR(255),
    stock INT NOT NULL DEFAULT -1,
    is_active TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted TINYINT NOT NULL DEFAULT 0
);

-- 2. 积分兑换订单表
CREATE TABLE credit_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(64) NOT NULL UNIQUE,
    learner_id BIGINT NOT NULL,
    account_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(128) NOT NULL,
    credit_amount INT NOT NULL,
    order_status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    paid_at DATETIME,
    delivered_at DATETIME,
    cancelled_at DATETIME,
    remark VARCHAR(255),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted TINYINT NOT NULL DEFAULT 0
);

-- 3. 流水表增强：增加变动前余额
ALTER TABLE credit_transaction
    ADD COLUMN balance_before INT DEFAULT 0 COMMENT '变动前余额',
    ADD COLUMN related_transaction_id BIGINT DEFAULT NULL COMMENT '关联流水ID',
    ADD COLUMN conversion_id VARCHAR(64) DEFAULT NULL COMMENT '转换批次号',
    ADD COLUMN fee_amount INT NOT NULL DEFAULT 0 COMMENT '手续费';

-- 4. 种子数据：积分商品
INSERT INTO credit_product (product_code, product_name, product_type, credit_price, description, stock, is_active, created_at, updated_at)
VALUES
('COURSE-JAVA-ADV', 'Java高级编程课程', 'COURSE', 50, '深入学习Java并发、JVM调优和设计模式', 100, 1, NOW(), NOW()),
('CERT-PYTHON', 'Python数据分析认证', 'CERTIFICATE', 80, '获得Python数据分析师认证证书', 50, 1, NOW(), NOW()),
('MERCH-NOTEBOOK', '学习笔记本套装', 'MERCHANDISE', 20, '终身学习定制笔记本+笔套装', 200, 1, NOW(), NOW()),
('COURSE-AI', '人工智能实战训练营', 'COURSE', 120, '从零到一掌握机器学习与深度学习', 30, 1, NOW(), NOW()),
('SERVICE-RESUME', '简历优化服务', 'SERVICE', 30, '资深HR一对一简历优化指导', 20, 1, NOW(), NOW()),
('MERCH-BAG', '学分银行定制背包', 'MERCHANDISE', 60, '限量版终身学习双肩背包', 50, 1, NOW(), NOW());

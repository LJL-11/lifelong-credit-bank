-- ============================================================
-- V7: 秒杀活动
-- ============================================================

CREATE TABLE flash_sale (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL COMMENT '关联商品ID',
    product_name VARCHAR(128) NOT NULL COMMENT '商品名称快照',
    origin_price INT NOT NULL COMMENT '原价（积分）',
    flash_price INT NOT NULL COMMENT '秒杀价（积分）',
    stock INT NOT NULL COMMENT '秒杀库存',
    begin_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    status VARCHAR(16) NOT NULL DEFAULT 'UPCOMING' COMMENT 'UPCOMING/ACTIVE/ENDED',
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE flash_sale_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    flash_sale_id BIGINT NOT NULL COMMENT '秒杀活动ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    learner_id BIGINT NOT NULL COMMENT '学员ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_flash_learner (flash_sale_id, learner_id)
);

-- 种子数据：未来24小时内的秒杀活动
INSERT INTO flash_sale (product_id, product_name, origin_price, flash_price, stock, begin_time, end_time, status, created_at, updated_at)
VALUES
(1, 'Java高级编程课程', 50, 10, 5, DATE_ADD(NOW(), INTERVAL -1 HOUR), DATE_ADD(NOW(), INTERVAL 23 HOUR), 'ACTIVE', NOW(), NOW()),
(2, 'Python数据分析认证', 80, 20, 3, DATE_ADD(NOW(), INTERVAL 1 HOUR), DATE_ADD(NOW(), INTERVAL 25 HOUR), 'UPCOMING', NOW(), NOW()),
(3, '学习笔记本套装', 20, 5, 10, DATE_ADD(NOW(), INTERVAL -2 HOUR), DATE_ADD(NOW(), INTERVAL 22 HOUR), 'ACTIVE', NOW(), NOW());

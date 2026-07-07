-- ============================================================
-- V6: 对齐黑马商城表结构 —— 微调增强
-- ============================================================

-- 1. 商品表增强：销量、评论数
ALTER TABLE credit_product
    ADD COLUMN sold INT NOT NULL DEFAULT 0 COMMENT '累计销量',
    ADD COLUMN comment_count INT NOT NULL DEFAULT 0 COMMENT '评论数';

-- 2. 订单明细表（仿 OrderDetail，支持一单多品）
CREATE TABLE credit_order_detail (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL COMMENT '订单ID',
    order_no VARCHAR(64) NOT NULL COMMENT '订单号',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    product_name VARCHAR(128) NOT NULL COMMENT '商品名称快照',
    credit_price INT NOT NULL COMMENT '兑换时积分单价快照',
    num INT NOT NULL DEFAULT 1 COMMENT '购买数量',
    amount INT NOT NULL COMMENT '小计积分',
    image_url VARCHAR(255) COMMENT '商品图片快照',
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_order_id (order_id),
    INDEX idx_order_no (order_no)
);

-- 3. 订单表增强
ALTER TABLE credit_order
    ADD COLUMN total_amount INT NOT NULL DEFAULT 0 COMMENT '订单总积分',
    ADD COLUMN item_count INT NOT NULL DEFAULT 1 COMMENT '商品件数',
    ADD COLUMN batch_no VARCHAR(64) DEFAULT NULL COMMENT '批次号，购物车结算的多个订单共享同一批次',
    ADD COLUMN close_time DATETIME DEFAULT NULL COMMENT '交易关闭时间';

-- 4. 补已有订单的 total_amount / item_count
UPDATE credit_order SET total_amount = credit_amount WHERE total_amount = 0;

-- 5. 给 credit_product 已有数据补销量（按已支付/已发货订单统计）
UPDATE credit_product p
SET sold = (SELECT COALESCE(SUM(1), 0) FROM credit_order o
            WHERE o.product_id = p.id AND o.order_status IN ('PAID','DELIVERED'))
WHERE exists(SELECT 1 FROM credit_order o WHERE o.product_id = p.id);

-- ============================================================
-- V5: 购物车表
-- ============================================================

CREATE TABLE cart (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    learner_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(128) NOT NULL,
    credit_price INT NOT NULL,
    num INT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    UNIQUE KEY uk_learner_product (learner_id, product_id)
);

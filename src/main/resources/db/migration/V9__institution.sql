-- ============================================================
-- V9: 多机构架构
-- ============================================================

CREATE TABLE institution (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(128) NOT NULL COMMENT '机构名称',
    code VARCHAR(32) NOT NULL UNIQUE COMMENT '机构编码',
    contact VARCHAR(128),
    phone VARCHAR(32),
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted TINYINT NOT NULL DEFAULT 0
);

INSERT INTO institution (name, code, status, created_at, updated_at) VALUES
('终身学习学院', 'LIFELONG', 'ACTIVE', NOW(), NOW()),
('计算机科学与技术学院', 'CS', 'ACTIVE', NOW(), NOW()),
('经济管理学院', 'ECON', 'ACTIVE', NOW(), NOW());

ALTER TABLE learner ADD COLUMN institution_id BIGINT DEFAULT 1;
ALTER TABLE course ADD COLUMN institution_id BIGINT DEFAULT 1;
ALTER TABLE learning_record ADD COLUMN institution_id BIGINT DEFAULT 1;
ALTER TABLE achievement ADD COLUMN institution_id BIGINT DEFAULT 1;
ALTER TABLE forum_post ADD COLUMN institution_id BIGINT DEFAULT 1;
ALTER TABLE sign_in_record ADD COLUMN institution_id BIGINT DEFAULT 1;
ALTER TABLE job_posting ADD COLUMN institution_id BIGINT DEFAULT 1;
ALTER TABLE blockchain_credential ADD COLUMN institution_id BIGINT DEFAULT 1;

UPDATE learner SET institution_id = 1 WHERE id IN (1, 4, 5);
UPDATE learner SET institution_id = 2 WHERE id IN (2, 6);
UPDATE learner SET institution_id = 3 WHERE id IN (3, 7);

UPDATE course SET institution_id = 1 WHERE institution_id IS NULL;
UPDATE learning_record SET institution_id = 1 WHERE institution_id IS NULL;
UPDATE achievement SET institution_id = 1 WHERE institution_id IS NULL;
UPDATE forum_post SET institution_id = 1 WHERE institution_id IS NULL;
UPDATE sign_in_record SET institution_id = 1 WHERE institution_id IS NULL;
UPDATE job_posting SET institution_id = 1 WHERE institution_id IS NULL;
UPDATE blockchain_credential SET institution_id = 1 WHERE institution_id IS NULL;

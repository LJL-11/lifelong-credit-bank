-- ============================================================
-- V9: 多机构架构
-- ============================================================

-- 1. 机构表
CREATE TABLE institution (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(128) NOT NULL COMMENT '机构名称',
    code VARCHAR(32) NOT NULL UNIQUE COMMENT '机构编码',
    contact VARCHAR(128) COMMENT '联系人',
    phone VARCHAR(32) COMMENT '联系电话',
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/DISABLED',
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted TINYINT NOT NULL DEFAULT 0
);

-- 2. 给机构专属表加 institution_id
ALTER TABLE learner ADD COLUMN institution_id BIGINT DEFAULT NULL;
ALTER TABLE course ADD COLUMN institution_id BIGINT DEFAULT NULL;
ALTER TABLE learning_record ADD COLUMN institution_id BIGINT DEFAULT NULL;
ALTER TABLE achievement ADD COLUMN institution_id BIGINT DEFAULT NULL;
ALTER TABLE forum_post ADD COLUMN institution_id BIGINT DEFAULT NULL;
ALTER TABLE sign_in_record ADD COLUMN institution_id BIGINT DEFAULT NULL;
ALTER TABLE job_posting ADD COLUMN institution_id BIGINT DEFAULT NULL;
ALTER TABLE blockchain_credential ADD COLUMN institution_id BIGINT DEFAULT NULL;

-- 积分商城相关表不加 institution_id（跨机构共享）

-- 3. 种子机构
INSERT INTO institution (name, code, status, created_at, updated_at) VALUES
('终身学习学院', 'LIFELONG', 'ACTIVE', NOW(), NOW()),
('计算机科学与技术学院', 'CS', 'ACTIVE', NOW(), NOW()),
('经济管理学院', 'ECON', 'ACTIVE', NOW(), NOW());

-- 4. 分配现有学员到机构
UPDATE learner SET institution_id = 1 WHERE id IN (1, 4, 5);  -- student001, student002, student003 → 终身学习学院
UPDATE learner SET institution_id = 2 WHERE id IN (2, 6);       -- admin001, student004 → 计算机学院
UPDATE learner SET institution_id = 3 WHERE id IN (3, 7);       -- admin002, student005 → 经管学院

-- 5. 分配现有课程到机构
UPDATE course SET institution_id = 1 WHERE institution_id IS NULL;

-- 6. 补上 learning_record / achievement / forum_post / sign_in_record / job_posting 的 institution_id
UPDATE learning_record SET institution_id = 1 WHERE institution_id IS NULL;
UPDATE achievement SET institution_id = 1 WHERE institution_id IS NULL;
UPDATE forum_post SET institution_id = 1 WHERE institution_id IS NULL;
UPDATE sign_in_record SET institution_id = 1 WHERE institution_id IS NULL;
UPDATE job_posting SET institution_id = 1 WHERE institution_id IS NULL;
UPDATE blockchain_credential SET institution_id = 1 WHERE institution_id IS NULL;

-- 7. 论坛帖子数据：让每个机构有自己的帖子
INSERT INTO forum_post (learner_id, title, content, section, reply_count, status, institution_id, created_at, updated_at)
VALUES
(5, '计算机学院学习交流', '分享一些算法学习心得', '学习交流', 2, 'VISIBLE', 2, NOW(), NOW()),
(4, '终身学习学院学员报到', '大家好，新学期一起加油！', '学习交流', 1, 'VISIBLE', 1, NOW(), NOW()),
(2, '经管学院课程推荐', '推荐微观经济学课程', '课程讨论', 0, 'VISIBLE', 3, NOW(), NOW());

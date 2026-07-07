-- ============================================================
-- V3: 权限系统 — password + role 字段
-- ============================================================

ALTER TABLE learner
    ADD COLUMN password VARCHAR(128) NOT NULL DEFAULT '',
    ADD COLUMN role VARCHAR(32) NOT NULL DEFAULT 'STUDENT';

-- 种子学员升级为管理员，预设密码 123456
UPDATE learner SET role = 'ADMIN', password = '07408a7aec558c4259416326d6000f91:a9e4077e1b44b45d33787e7fe1381460d29440cb74c215b700ab3a4c6914a19f' WHERE id = 1;

-- ============================================================
-- V13: 三个机构各一个管理员账号
-- ============================================================

-- 旧数据中 admin002 属于经济管理学院，顺延为 admin003。
UPDATE learner
SET username = 'admin003', email = 'admin003@creditbank.edu', updated_at = NOW()
WHERE username = 'admin002'
  AND role = 'ADMIN'
  AND institution_id = 3
  AND NOT EXISTS (
      SELECT 1 FROM (SELECT id FROM learner WHERE username = 'admin003') existing_admin003
  );

-- 旧数据中 admin001 属于计算机科学与技术学院，顺延为 admin002。
UPDATE learner
SET username = 'admin002', email = 'admin002@creditbank.edu', updated_at = NOW()
WHERE username = 'admin001'
  AND role = 'ADMIN'
  AND institution_id = 2
  AND NOT EXISTS (
      SELECT 1 FROM (SELECT id FROM learner WHERE username = 'admin002' AND institution_id = 2) existing_admin002
  );

-- 给终身学习学院补管理员 admin001，密码 123456。
INSERT INTO learner (username, real_name, phone, email, education_level, status, password, role, institution_id, created_at, updated_at, deleted)
SELECT 'admin001', '终身管理员', '13800000001', 'admin001@creditbank.edu', '硕士', 'ACTIVE',
       CONCAT('00112233445566778899aabbccddeeff', ':', SHA2(CONCAT('00112233445566778899aabbccddeeff', '123456'), 256)),
       'ADMIN', 1, NOW(), NOW(), 0
WHERE NOT EXISTS (
    SELECT 1 FROM (SELECT id FROM learner WHERE username = 'admin001') existing_admin001
);

-- ============================================================
-- V14: 调整演示账号 ID 与姓名
-- 目标：admin001 为 id=1 且姓名曹书记；student001 为 id=4 且姓名张小华；原 4/5/6/7 后移一位。
-- ============================================================

SET @needs_remap := (
    SELECT COUNT(*) FROM learner WHERE username = 'admin001' AND id <> 1
);

UPDATE learner SET id = id + 100000 WHERE @needs_remap > 0 AND id IN (1,4,5,6,7,8);
UPDATE credit_account SET learner_id = learner_id + 100000 WHERE @needs_remap > 0 AND learner_id IN (1,4,5,6,7,8);
UPDATE credit_transaction SET learner_id = learner_id + 100000 WHERE @needs_remap > 0 AND learner_id IN (1,4,5,6,7,8);
UPDATE credit_order SET learner_id = learner_id + 100000 WHERE @needs_remap > 0 AND learner_id IN (1,4,5,6,7,8);
UPDATE learning_record SET learner_id = learner_id + 100000 WHERE @needs_remap > 0 AND learner_id IN (1,4,5,6,7,8);
UPDATE achievement SET learner_id = learner_id + 100000 WHERE @needs_remap > 0 AND learner_id IN (1,4,5,6,7,8);
UPDATE forum_post SET learner_id = learner_id + 100000 WHERE @needs_remap > 0 AND learner_id IN (1,4,5,6,7,8);
UPDATE forum_post_like SET learner_id = learner_id + 100000 WHERE @needs_remap > 0 AND learner_id IN (1,4,5,6,7,8);
UPDATE forum_reply SET learner_id = learner_id + 100000 WHERE @needs_remap > 0 AND learner_id IN (1,4,5,6,7,8);
UPDATE sign_in_record SET learner_id = learner_id + 100000 WHERE @needs_remap > 0 AND learner_id IN (1,4,5,6,7,8);
UPDATE blockchain_credential SET learner_id = learner_id + 100000 WHERE @needs_remap > 0 AND learner_id IN (1,4,5,6,7,8);
UPDATE flash_sale_record SET learner_id = learner_id + 100000 WHERE @needs_remap > 0 AND learner_id IN (1,4,5,6,7,8);
UPDATE cart SET learner_id = learner_id + 100000 WHERE @needs_remap > 0 AND learner_id IN (1,4,5,6,7,8);
UPDATE course_enrollment SET learner_id = learner_id + 100000 WHERE @needs_remap > 0 AND learner_id IN (1,4,5,6,7,8);
UPDATE job_application SET learner_id = learner_id + 100000 WHERE @needs_remap > 0 AND learner_id IN (1,4,5,6,7,8);
UPDATE integrity_rating SET learner_id = learner_id + 100000 WHERE @needs_remap > 0 AND learner_id IN (1,4,5,6,7,8);

UPDATE learner
SET id = CASE id
    WHEN 100001 THEN 4
    WHEN 100004 THEN 5
    WHEN 100005 THEN 6
    WHEN 100006 THEN 7
    WHEN 100007 THEN 8
    WHEN 100008 THEN 1
    ELSE id END
WHERE @needs_remap > 0 AND id IN (100001,100004,100005,100006,100007,100008);

UPDATE credit_account SET learner_id = CASE learner_id WHEN 100001 THEN 4 WHEN 100004 THEN 5 WHEN 100005 THEN 6 WHEN 100006 THEN 7 WHEN 100007 THEN 8 WHEN 100008 THEN 1 ELSE learner_id END WHERE @needs_remap > 0 AND learner_id IN (100001,100004,100005,100006,100007,100008);
UPDATE credit_transaction SET learner_id = CASE learner_id WHEN 100001 THEN 4 WHEN 100004 THEN 5 WHEN 100005 THEN 6 WHEN 100006 THEN 7 WHEN 100007 THEN 8 WHEN 100008 THEN 1 ELSE learner_id END WHERE @needs_remap > 0 AND learner_id IN (100001,100004,100005,100006,100007,100008);
UPDATE credit_order SET learner_id = CASE learner_id WHEN 100001 THEN 4 WHEN 100004 THEN 5 WHEN 100005 THEN 6 WHEN 100006 THEN 7 WHEN 100007 THEN 8 WHEN 100008 THEN 1 ELSE learner_id END WHERE @needs_remap > 0 AND learner_id IN (100001,100004,100005,100006,100007,100008);
UPDATE learning_record SET learner_id = CASE learner_id WHEN 100001 THEN 4 WHEN 100004 THEN 5 WHEN 100005 THEN 6 WHEN 100006 THEN 7 WHEN 100007 THEN 8 WHEN 100008 THEN 1 ELSE learner_id END WHERE @needs_remap > 0 AND learner_id IN (100001,100004,100005,100006,100007,100008);
UPDATE achievement SET learner_id = CASE learner_id WHEN 100001 THEN 4 WHEN 100004 THEN 5 WHEN 100005 THEN 6 WHEN 100006 THEN 7 WHEN 100007 THEN 8 WHEN 100008 THEN 1 ELSE learner_id END WHERE @needs_remap > 0 AND learner_id IN (100001,100004,100005,100006,100007,100008);
UPDATE forum_post SET learner_id = CASE learner_id WHEN 100001 THEN 4 WHEN 100004 THEN 5 WHEN 100005 THEN 6 WHEN 100006 THEN 7 WHEN 100007 THEN 8 WHEN 100008 THEN 1 ELSE learner_id END WHERE @needs_remap > 0 AND learner_id IN (100001,100004,100005,100006,100007,100008);
UPDATE forum_post_like SET learner_id = CASE learner_id WHEN 100001 THEN 4 WHEN 100004 THEN 5 WHEN 100005 THEN 6 WHEN 100006 THEN 7 WHEN 100007 THEN 8 WHEN 100008 THEN 1 ELSE learner_id END WHERE @needs_remap > 0 AND learner_id IN (100001,100004,100005,100006,100007,100008);
UPDATE forum_reply SET learner_id = CASE learner_id WHEN 100001 THEN 4 WHEN 100004 THEN 5 WHEN 100005 THEN 6 WHEN 100006 THEN 7 WHEN 100007 THEN 8 WHEN 100008 THEN 1 ELSE learner_id END WHERE @needs_remap > 0 AND learner_id IN (100001,100004,100005,100006,100007,100008);
UPDATE sign_in_record SET learner_id = CASE learner_id WHEN 100001 THEN 4 WHEN 100004 THEN 5 WHEN 100005 THEN 6 WHEN 100006 THEN 7 WHEN 100007 THEN 8 WHEN 100008 THEN 1 ELSE learner_id END WHERE @needs_remap > 0 AND learner_id IN (100001,100004,100005,100006,100007,100008);
UPDATE blockchain_credential SET learner_id = CASE learner_id WHEN 100001 THEN 4 WHEN 100004 THEN 5 WHEN 100005 THEN 6 WHEN 100006 THEN 7 WHEN 100007 THEN 8 WHEN 100008 THEN 1 ELSE learner_id END WHERE @needs_remap > 0 AND learner_id IN (100001,100004,100005,100006,100007,100008);
UPDATE flash_sale_record SET learner_id = CASE learner_id WHEN 100001 THEN 4 WHEN 100004 THEN 5 WHEN 100005 THEN 6 WHEN 100006 THEN 7 WHEN 100007 THEN 8 WHEN 100008 THEN 1 ELSE learner_id END WHERE @needs_remap > 0 AND learner_id IN (100001,100004,100005,100006,100007,100008);
UPDATE cart SET learner_id = CASE learner_id WHEN 100001 THEN 4 WHEN 100004 THEN 5 WHEN 100005 THEN 6 WHEN 100006 THEN 7 WHEN 100007 THEN 8 WHEN 100008 THEN 1 ELSE learner_id END WHERE @needs_remap > 0 AND learner_id IN (100001,100004,100005,100006,100007,100008);
UPDATE course_enrollment SET learner_id = CASE learner_id WHEN 100001 THEN 4 WHEN 100004 THEN 5 WHEN 100005 THEN 6 WHEN 100006 THEN 7 WHEN 100007 THEN 8 WHEN 100008 THEN 1 ELSE learner_id END WHERE @needs_remap > 0 AND learner_id IN (100001,100004,100005,100006,100007,100008);
UPDATE job_application SET learner_id = CASE learner_id WHEN 100001 THEN 4 WHEN 100004 THEN 5 WHEN 100005 THEN 6 WHEN 100006 THEN 7 WHEN 100007 THEN 8 WHEN 100008 THEN 1 ELSE learner_id END WHERE @needs_remap > 0 AND learner_id IN (100001,100004,100005,100006,100007,100008);
UPDATE integrity_rating SET learner_id = CASE learner_id WHEN 100001 THEN 4 WHEN 100004 THEN 5 WHEN 100005 THEN 6 WHEN 100006 THEN 7 WHEN 100007 THEN 8 WHEN 100008 THEN 1 ELSE learner_id END WHERE @needs_remap > 0 AND learner_id IN (100001,100004,100005,100006,100007,100008);

UPDATE learner SET real_name = '曹书记', updated_at = NOW() WHERE username = 'admin001';
UPDATE learner SET real_name = '张小华', updated_at = NOW() WHERE username = 'student001';
ALTER TABLE learner AUTO_INCREMENT = 9;

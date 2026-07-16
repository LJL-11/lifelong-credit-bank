-- 清理早期演示脚本误挂到 student001 上的历史行为数据。
SET @student001_id := (SELECT id FROM learner WHERE username = 'student001' LIMIT 1);
SET @student001_account_id := (SELECT id FROM credit_account WHERE learner_id = @student001_id LIMIT 1);

DELETE FROM credit_transaction
WHERE learner_id = @student001_id
  AND (
      source_no IN ('DEMO-001', 'COURSE-JAVA-001', 'SIGN-20260701')
      OR remark IN ('兑换学习笔记本套装', '完成Java程序设计基础', '每日签到奖励')
  );

UPDATE credit_account
SET total_credits = 30,
    available_credits = 30,
    frozen_credits = 0,
    updated_at = NOW()
WHERE id = @student001_account_id;

DELETE FROM integrity_rating
WHERE learner_id = @student001_id;

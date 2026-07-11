-- V16: 回填历史已支付课程商品为已购买课程
INSERT INTO course_enrollment (
    learner_id, course_id, enroll_status, reviewer, reviewed_at, remark,
    institution_id, created_at, updated_at, deleted
)
SELECT DISTINCT
    o.learner_id,
    p.course_id,
    'APPROVED',
    'SYSTEM',
    NOW(),
    '历史课程商品订单自动开通',
    COALESCE(c.institution_id, 1),
    NOW(),
    NOW(),
    0
FROM credit_order o
JOIN credit_order_detail d ON d.order_id = o.id AND d.deleted = 0
JOIN credit_product p ON p.id = d.product_id AND p.deleted = 0
JOIN course c ON c.id = p.course_id AND c.deleted = 0
LEFT JOIN course_enrollment e
    ON e.learner_id = o.learner_id
    AND e.course_id = p.course_id
    AND e.deleted = 0
WHERE o.deleted = 0
  AND o.order_status IN ('PAID', 'DELIVERED')
  AND p.product_type = 'COURSE'
  AND p.course_id IS NOT NULL
  AND e.id IS NULL;

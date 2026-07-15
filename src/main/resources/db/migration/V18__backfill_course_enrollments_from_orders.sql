-- V18: 补齐历史商城课程商品订单对应的课程报名
-- 覆盖普通订单明细和秒杀订单仅有 order.product_id 的情况。

INSERT INTO course_enrollment (
    learner_id, course_id, enroll_status, reviewer, reviewed_at, remark,
    institution_id, created_at, updated_at, deleted
)
SELECT DISTINCT
    o.learner_id,
    c.id,
    'APPROVED',
    'SYSTEM',
    NOW(),
    '历史课程商品订单自动开通',
    COALESCE(l.institution_id, c.institution_id, 1),
    NOW(),
    NOW(),
    0
FROM credit_order o
LEFT JOIN credit_order_detail d ON d.order_id = o.id AND d.deleted = 0
JOIN credit_product p ON p.id = COALESCE(d.product_id, o.product_id) AND p.deleted = 0
JOIN course c ON c.deleted = 0
    AND c.status = 'PUBLISHED'
    AND (
        c.id = p.course_id
        OR (p.product_code = 'COURSE-JAVA-ADV' AND c.course_code = 'JAVA-BASE')
        OR (p.product_code = 'COURSE-AI' AND c.course_code = 'AI-INTRO')
        OR (p.course_id IS NULL AND p.product_code = CONCAT('COURSE-', c.course_code))
    )
LEFT JOIN learner l ON l.id = o.learner_id AND l.deleted = 0
LEFT JOIN course_enrollment e
    ON e.learner_id = o.learner_id
    AND e.course_id = c.id
    AND e.deleted = 0
WHERE o.deleted = 0
  AND o.order_status IN ('PAID', 'DELIVERED')
  AND p.product_type = 'COURSE'
  AND e.id IS NULL;

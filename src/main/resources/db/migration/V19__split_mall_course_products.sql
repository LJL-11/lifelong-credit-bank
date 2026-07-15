-- V19: 商城课程商品必须对应同名课程，而不是映射到已有基础课程。
-- 这样购买“人工智能实战训练营”后会在“我的课程”中出现同名课程。

INSERT INTO course (
    course_code, course_name, provider, category, credit_value, credit_point, status,
    resource_title, resource_url, resource_summary, estimated_minutes, learning_stages,
    institution_id, created_at, updated_at, deleted
)
SELECT
    'JAVA-ADV',
    'Java高级编程课程',
    '继续教育学院',
    '编程开发',
    5.00,
    p.credit_price,
    'PUBLISHED',
    'Java Advanced Learning Path',
    'https://docs.oracle.com/javase/tutorial/essential/concurrency/',
    COALESCE(p.description, '深入学习Java并发、JVM调优和设计模式'),
    300,
    '[{"title":"并发编程基础","description":"学习线程、锁、并发集合和任务执行。"},{"title":"JVM与性能调优","description":"了解内存模型、垃圾回收和常见性能分析方法。"},{"title":"设计模式实践","description":"结合业务案例复习常用设计模式并完成练习。"}]',
    1,
    NOW(),
    NOW(),
    0
FROM credit_product p
WHERE p.product_code = 'COURSE-JAVA-ADV'
  AND NOT EXISTS (SELECT 1 FROM course c WHERE c.course_code = 'JAVA-ADV');

INSERT INTO course (
    course_code, course_name, provider, category, credit_value, credit_point, status,
    resource_title, resource_url, resource_summary, estimated_minutes, learning_stages,
    institution_id, created_at, updated_at, deleted
)
SELECT
    'AI-PRACTICE',
    '人工智能实战训练营',
    '在线学习中心',
    '人工智能',
    6.00,
    p.credit_price,
    'PUBLISHED',
    'Machine Learning Crash Course - Practice Track',
    'https://developers.google.com/machine-learning/crash-course',
    COALESCE(p.description, '从零到一掌握机器学习与深度学习'),
    420,
    '[{"title":"机器学习基础复盘","description":"完成模型、损失函数、训练评估等基础模块。"},{"title":"实战数据处理","description":"围绕特征工程、过拟合和分类指标完成案例练习。"},{"title":"项目式训练","description":"整理一个机器学习小项目的流程、结果和复盘。"}]',
    1,
    NOW(),
    NOW(),
    0
FROM credit_product p
WHERE p.product_code = 'COURSE-AI'
  AND NOT EXISTS (SELECT 1 FROM course c WHERE c.course_code = 'AI-PRACTICE');

UPDATE credit_product p
JOIN course c ON c.course_code = 'JAVA-ADV'
SET p.course_id = c.id,
    p.product_name = c.course_name
WHERE p.product_code = 'COURSE-JAVA-ADV';

UPDATE credit_product p
JOIN course c ON c.course_code = 'AI-PRACTICE'
SET p.course_id = c.id,
    p.product_name = c.course_name
WHERE p.product_code = 'COURSE-AI';

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
    '历史商城课程商品订单自动开通',
    COALESCE(l.institution_id, c.institution_id, 1),
    NOW(),
    NOW(),
    0
FROM credit_order o
LEFT JOIN credit_order_detail d ON d.order_id = o.id AND d.deleted = 0
JOIN credit_product p ON p.id = COALESCE(d.product_id, o.product_id) AND p.deleted = 0
JOIN course c ON c.id = p.course_id AND c.deleted = 0
LEFT JOIN learner l ON l.id = o.learner_id AND l.deleted = 0
LEFT JOIN course_enrollment e
    ON e.learner_id = o.learner_id
    AND e.course_id = p.course_id
    AND e.deleted = 0
WHERE o.deleted = 0
  AND o.order_status IN ('PAID', 'DELIVERED')
  AND p.product_type = 'COURSE'
  AND p.product_code IN ('COURSE-JAVA-ADV', 'COURSE-AI')
  AND p.course_id IS NOT NULL
  AND e.id IS NULL;

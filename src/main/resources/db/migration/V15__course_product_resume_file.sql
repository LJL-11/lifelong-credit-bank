-- V15: 课程商品关联与岗位投递简历文件
ALTER TABLE credit_product
    ADD COLUMN course_id BIGINT NULL COMMENT '课程类商品关联课程ID' AFTER product_type;

UPDATE credit_product p
JOIN course c ON c.course_code = 'JAVA-BASE'
SET p.course_id = c.id
WHERE p.product_code = 'COURSE-JAVA-ADV' AND p.course_id IS NULL;

UPDATE credit_product p
JOIN course c ON c.course_code = 'AI-INTRO'
SET p.course_id = c.id
WHERE p.product_code = 'COURSE-AI' AND p.course_id IS NULL;

ALTER TABLE job_application
    ADD COLUMN resume_url VARCHAR(512) NULL COMMENT '简历文件URL' AFTER resume_summary,
    ADD COLUMN resume_file_name VARCHAR(255) NULL COMMENT '简历原始文件名' AFTER resume_url;

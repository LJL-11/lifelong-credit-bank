-- ============================================================
-- V12: 补齐核心业务模块：报名、签到、评论、投递、诚信、存证增强
-- ============================================================

CREATE TABLE course_enrollment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    learner_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    enroll_status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    reviewer VARCHAR(64),
    reviewed_at DATETIME,
    remark VARCHAR(255),
    institution_id BIGINT DEFAULT 1,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_enroll_learner_course (learner_id, course_id)
);

CREATE TABLE forum_reply (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id BIGINT NOT NULL,
    learner_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'VISIBLE',
    institution_id BIGINT DEFAULT 1,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE job_application (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    job_id BIGINT NOT NULL,
    learner_id BIGINT NOT NULL,
    resume_summary TEXT,
    apply_status VARCHAR(32) NOT NULL DEFAULT 'SUBMITTED',
    reviewer VARCHAR(64),
    reviewed_at DATETIME,
    remark VARCHAR(255),
    institution_id BIGINT DEFAULT 1,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_job_learner (job_id, learner_id)
);

CREATE TABLE integrity_rating (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    learner_id BIGINT NOT NULL,
    score INT NOT NULL DEFAULT 100,
    level_name VARCHAR(32) NOT NULL DEFAULT 'A',
    learning_score INT NOT NULL DEFAULT 0,
    forum_score INT NOT NULL DEFAULT 0,
    signin_score INT NOT NULL DEFAULT 0,
    achievement_score INT NOT NULL DEFAULT 0,
    job_score INT NOT NULL DEFAULT 0,
    remark VARCHAR(255),
    institution_id BIGINT DEFAULT 1,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_integrity_learner (learner_id)
);

ALTER TABLE achievement
    ADD COLUMN proof_url VARCHAR(512),
    ADD COLUMN reject_reason VARCHAR(255);

ALTER TABLE blockchain_credential
    ADD COLUMN verified_at DATETIME,
    ADD COLUMN source_payload TEXT;

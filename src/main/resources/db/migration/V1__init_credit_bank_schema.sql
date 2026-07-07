CREATE TABLE learner (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL UNIQUE,
    real_name VARCHAR(64) NOT NULL,
    phone VARCHAR(32),
    email VARCHAR(128),
    education_level VARCHAR(64),
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE course (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_code VARCHAR(64) NOT NULL UNIQUE,
    course_name VARCHAR(128) NOT NULL,
    provider VARCHAR(128),
    category VARCHAR(64),
    credit_value DECIMAL(6,2) NOT NULL DEFAULT 0,
    credit_point INT NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'PUBLISHED',
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE learning_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    learner_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    progress DECIMAL(5,2) NOT NULL DEFAULT 0,
    score DECIMAL(5,2),
    result VARCHAR(32) NOT NULL DEFAULT 'LEARNING',
    completed_at DATETIME,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE credit_account (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    learner_id BIGINT NOT NULL UNIQUE,
    total_credits INT NOT NULL DEFAULT 0,
    available_credits INT NOT NULL DEFAULT 0,
    frozen_credits INT NOT NULL DEFAULT 0,
    account_status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE credit_transaction (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    learner_id BIGINT NOT NULL,
    account_id BIGINT NOT NULL,
    transaction_type VARCHAR(32) NOT NULL,
    amount INT NOT NULL,
    balance_after INT NOT NULL,
    source_type VARCHAR(64),
    source_no VARCHAR(128),
    remark VARCHAR(255),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE achievement (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    learner_id BIGINT NOT NULL,
    achievement_name VARCHAR(128) NOT NULL,
    achievement_type VARCHAR(64) NOT NULL,
    suggested_credits INT NOT NULL DEFAULT 0,
    audit_status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    auditor VARCHAR(64),
    audited_at DATETIME,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE forum_post (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    learner_id BIGINT NOT NULL,
    title VARCHAR(128) NOT NULL,
    content TEXT NOT NULL,
    section VARCHAR(64) NOT NULL,
    reply_count INT NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'VISIBLE',
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE sign_in_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    learner_id BIGINT NOT NULL,
    sign_date DATE NOT NULL,
    reward_credits INT NOT NULL DEFAULT 0,
    sign_type VARCHAR(32) NOT NULL DEFAULT 'DAILY',
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_sign_in_learner_date (learner_id, sign_date)
);

CREATE TABLE job_posting (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    company_name VARCHAR(128) NOT NULL,
    position_name VARCHAR(128) NOT NULL,
    city VARCHAR(64),
    requirement TEXT,
    contact VARCHAR(128),
    status VARCHAR(32) NOT NULL DEFAULT 'OPEN',
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE blockchain_credential (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    learner_id BIGINT NOT NULL,
    credential_type VARCHAR(64) NOT NULL,
    business_no VARCHAR(128) NOT NULL,
    hash_value VARCHAR(128) NOT NULL,
    chain_status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_chain_business_no (business_no)
);

INSERT INTO learner (username, real_name, phone, email, education_level, status, created_at, updated_at, deleted)
VALUES ('student001', '张三', '13800000001', 'student001@example.com', '本科', 'ACTIVE', NOW(), NOW(), 0);

INSERT INTO course (course_code, course_name, provider, category, credit_value, credit_point, status, created_at, updated_at, deleted)
VALUES
('JAVA-BASE', 'Java程序设计基础', '继续教育学院', '编程开发', 3.00, 30, 'PUBLISHED', NOW(), NOW(), 0),
('AI-INTRO', '人工智能导论', '在线学习中心', '人工智能', 2.00, 20, 'PUBLISHED', NOW(), NOW(), 0);

INSERT INTO credit_account (learner_id, total_credits, available_credits, frozen_credits, account_status, created_at, updated_at, deleted)
VALUES (1, 30, 30, 0, 'ACTIVE', NOW(), NOW(), 0);

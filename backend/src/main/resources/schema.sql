-- 删除已存在的表
DROP TABLE IF EXISTS publish_record;
DROP TABLE IF EXISTS article;
DROP TABLE IF EXISTS platform_config;

-- 创建文章表
CREATE TABLE article (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    summary VARCHAR(500),
    cover_image VARCHAR(500),
    images TEXT,
    status INT DEFAULT 0,
    deleted INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 创建平台配置表
CREATE TABLE platform_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    platform VARCHAR(50) NOT NULL UNIQUE,
    credentials TEXT NOT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    deleted INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 创建发布记录表
CREATE TABLE publish_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    article_id BIGINT NOT NULL,
    platform VARCHAR(50) NOT NULL,
    status INT DEFAULT 0,
    publish_url VARCHAR(500),
    error_message TEXT,
    deleted INT DEFAULT 0,
    published_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (article_id) REFERENCES article(id)
);

-- 创建索引
CREATE INDEX idx_article_created ON article(created_at);
CREATE INDEX idx_publish_article ON publish_record(article_id);
CREATE INDEX idx_publish_platform ON publish_record(platform);
CREATE INDEX idx_publish_status ON publish_record(status);

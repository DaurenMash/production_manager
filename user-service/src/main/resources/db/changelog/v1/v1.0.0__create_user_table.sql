--liquibase formatted sql

--changeset prodman:1
CREATE TABLE IF NOT EXISTS users (
                                     id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'OPERATOR', 'ANALYST', 'VISITOR')),
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

--changeset prodman:2
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);

--changeset prodman:3
INSERT INTO users (id, username, email, password, role, enabled)
VALUES (
           'admin-001',
           'admin',
           'admin@prodman.com',
           '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', -- password: admin123
           'ADMIN',
           true
       ) ON CONFLICT (username) DO NOTHING;
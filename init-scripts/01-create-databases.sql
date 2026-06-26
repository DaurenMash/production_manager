-- Создаем базы данных (без IF NOT EXISTS - это не поддерживается в PostgreSQL)
-- Если базы уже есть, будет ошибка, но это нормально
CREATE DATABASE prodman_users;
CREATE DATABASE prodman_calendar;

-- Подключаемся к prodman_users
\c prodman_users;

-- Создаем таблицу users
CREATE TABLE IF NOT EXISTS users (
                                     id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Создаем тестового пользователя (пароль: admin123)
INSERT INTO users (id, username, email, password, role, enabled)
VALUES (
           '11111111-1111-1111-1111-111111111111',
           'admin',
           'admin@prodman.com',
           '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi',
           'ADMIN',
           true
       ) ON CONFLICT (email) DO NOTHING;

-- Подключаемся к prodman_calendar
\c prodman_calendar;

-- Создаем таблицу employees
CREATE TABLE IF NOT EXISTS employees (
                                         id VARCHAR(36) PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    status VARCHAR(20) DEFAULT 'AVAILABLE',
    phone_number VARCHAR(20),
    department VARCHAR(100),
    max_hours_per_week INT DEFAULT 40,
    preferred_shift VARCHAR(20),
    vacation_start DATE,
    vacation_end DATE,
    sick_leave_start DATE,
    sick_leave_end DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Создаем таблицу employee_positions
CREATE TABLE IF NOT EXISTS employee_positions (
                                                  employee_id VARCHAR(36) NOT NULL,
    position VARCHAR(100) NOT NULL,
    PRIMARY KEY (employee_id, position)
    );
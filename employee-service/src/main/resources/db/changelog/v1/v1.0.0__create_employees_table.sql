CREATE TABLE IF NOT EXISTS employees (
                                         id VARCHAR(36) PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone_number VARCHAR(20),
    department VARCHAR(100),
    position VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    user_id VARCHAR(36) UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_employees_email ON employees(email);
CREATE INDEX IF NOT EXISTS idx_employees_status ON employees(status);
CREATE INDEX IF NOT EXISTS idx_employees_user_id ON employees(user_id);

COMMENT ON TABLE employees IS 'Сотрудники';
COMMENT ON COLUMN employees.user_id IS 'Связь 1:1 с пользователем (может быть NULL)';
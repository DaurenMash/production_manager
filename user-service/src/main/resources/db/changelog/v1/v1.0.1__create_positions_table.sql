-- ============================================
-- Таблица positions (позиции/рабочие места)
-- ============================================
CREATE TABLE IF NOT EXISTS positions (
                                         id VARCHAR(36) PRIMARY KEY,
    department VARCHAR(100) NOT NULL,
    title VARCHAR(100) NOT NULL,
    assigned_employee_id VARCHAR(36),
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Индексы для быстрого поиска
CREATE INDEX IF NOT EXISTS idx_positions_status ON positions(status);
CREATE INDEX IF NOT EXISTS idx_positions_assigned_employee ON positions(assigned_employee_id);

-- Комментарии к таблице и колонкам
COMMENT ON TABLE positions IS 'Рабочие позиции/места на производстве';
COMMENT ON COLUMN positions.department IS 'Отдел';
COMMENT ON COLUMN positions.title IS 'Название позиции';
COMMENT ON COLUMN positions.assigned_employee_id IS 'ID назначенного сотрудника (null = свободна)';
COMMENT ON COLUMN positions.status IS 'Статус: OPEN, ASSIGNED, CLOSED';
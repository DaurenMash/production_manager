-- Таблица рабочих мест
CREATE TABLE IF NOT EXISTS workstations (
                                            id VARCHAR(36) PRIMARY KEY,
    department VARCHAR(100) NOT NULL,
    title VARCHAR(100) NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
    );

-- Таблица связи ManyToMany (рабочее место → сотрудник)
CREATE TABLE IF NOT EXISTS workstation_employees (
                                                     workstation_id VARCHAR(36) NOT NULL,
    employee_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (workstation_id, employee_id),
    FOREIGN KEY (workstation_id) REFERENCES workstations(id) ON DELETE CASCADE
    );

-- Индексы
CREATE INDEX IF NOT EXISTS idx_workstations_department ON workstations(department);
CREATE INDEX IF NOT EXISTS idx_workstations_title ON workstations(title);
CREATE INDEX IF NOT EXISTS idx_workstations_is_active ON workstations(is_active);
CREATE INDEX IF NOT EXISTS idx_workstation_employees_employee ON workstation_employees(employee_id);

COMMENT ON TABLE workstations IS 'Рабочие места';
COMMENT ON COLUMN workstations.department IS 'Отдел';
COMMENT ON COLUMN workstations.title IS 'Название рабочего места';
COMMENT ON COLUMN workstations.created_by IS 'Кто создал (email)';
COMMENT ON COLUMN workstations.created_at IS 'Дата создания';
COMMENT ON COLUMN workstations.is_active IS 'Актуально/неактуально';
COMMENT ON TABLE workstation_employees IS 'Связь ManyToMany: рабочие места ↔ сотрудники';
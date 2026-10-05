-- =====================================================================
-- employee-service: начальная схема
-- =====================================================================
-- Мультитенантность: shared schema + tenant_id (см. ADR-0002).
-- Идентификаторы — UUID v7, генерируются приложением.
--
-- Иерархия:
--   Tenant → Department → Position → Qualification
--   Employee принадлежит Department, имеет Position и набор Qualifications.
--
-- Телефон сотрудника — логин в рамках тенанта:
--   ввод:        1231231212
--   хранение:    +71231231212
--   отображение: +7 123 123 12 12
-- =====================================================================

-- ---------------------------------------------------------------------
-- departments
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS departments (
                                           id             UUID         PRIMARY KEY,
                                           tenant_id      UUID         NOT NULL,
                                           code           VARCHAR(64)  NOT NULL,
    name           VARCHAR(255) NOT NULL,
    description    TEXT,
    admin_user_id  UUID,
    is_active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW()
    );

CREATE UNIQUE INDEX IF NOT EXISTS ux_departments_tenant_code
    ON departments (tenant_id, code);

CREATE INDEX IF NOT EXISTS ix_departments_tenant_active
    ON departments (tenant_id, is_active);

COMMENT ON TABLE  departments IS 'Отделы (в рамках тенанта)';
COMMENT ON COLUMN departments.tenant_id IS 'Организация-арендатор.';
COMMENT ON COLUMN departments.admin_user_id IS 'Админ отдела. Ссылка на user-service.users.id. Может быть NULL.';
COMMENT ON COLUMN departments.code IS 'Уникален в рамках тенанта.';

-- ---------------------------------------------------------------------
-- positions
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS positions (
                                         id             UUID         PRIMARY KEY,
                                         tenant_id      UUID         NOT NULL,
                                         department_id  UUID         NOT NULL,
                                         code           VARCHAR(64)  NOT NULL,
    name           VARCHAR(255) NOT NULL,
    description    TEXT,
    is_active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_positions_department
    FOREIGN KEY (department_id) REFERENCES departments (id)
    );

CREATE UNIQUE INDEX IF NOT EXISTS ux_positions_tenant_code
    ON positions (tenant_id, code);

CREATE INDEX IF NOT EXISTS ix_positions_tenant_department
    ON positions (tenant_id, department_id);

CREATE INDEX IF NOT EXISTS ix_positions_tenant_active
    ON positions (tenant_id, is_active);

COMMENT ON TABLE  positions IS 'Должности (в рамках тенанта)';
COMMENT ON COLUMN positions.department_id IS 'Должность принадлежит отделу.';
COMMENT ON COLUMN positions.code IS 'Уникален в рамках тенанта.';

-- ---------------------------------------------------------------------
-- qualifications
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS qualifications (
                                              id           UUID         PRIMARY KEY,
                                              tenant_id    UUID         NOT NULL,
                                              position_id  UUID         NOT NULL,
                                              code         VARCHAR(64)  NOT NULL,
    name         VARCHAR(255) NOT NULL,
    description  TEXT,
    is_active    BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_qualifications_position
    FOREIGN KEY (position_id) REFERENCES positions (id)
    );

CREATE UNIQUE INDEX IF NOT EXISTS ux_qualifications_tenant_code
    ON qualifications (tenant_id, code);

CREATE INDEX IF NOT EXISTS ix_qualifications_tenant_position
    ON qualifications (tenant_id, position_id);

CREATE INDEX IF NOT EXISTS ix_qualifications_tenant_active
    ON qualifications (tenant_id, is_active);

COMMENT ON TABLE  qualifications IS 'Квалификации (в рамках тенанта)';
COMMENT ON COLUMN qualifications.position_id IS 'Квалификация привязана к должности.';
COMMENT ON COLUMN qualifications.code IS 'Уникален в рамках тенанта.';

-- ---------------------------------------------------------------------
-- employees
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS employees (
                                         id                     UUID         PRIMARY KEY,
                                         tenant_id              UUID         NOT NULL,
                                         code                   VARCHAR(64)  NOT NULL,
    first_name             VARCHAR(100) NOT NULL,
    last_name              VARCHAR(100) NOT NULL,
    middle_name            VARCHAR(100),
    phone                  VARCHAR(16)  NOT NULL,
    hired_at               DATE,
    fired_at               DATE,
    status                 VARCHAR(32)  NOT NULL DEFAULT 'AVAILABLE',
    department_id          UUID,
    position_id            UUID,
    user_id                UUID,
    max_consecutive_hours  INTEGER      NOT NULL DEFAULT 12,
    created_at             TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at             TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_employees_department
    FOREIGN KEY (department_id) REFERENCES departments (id),
    CONSTRAINT fk_employees_position
    FOREIGN KEY (position_id)   REFERENCES positions (id)
    );

CREATE UNIQUE INDEX IF NOT EXISTS ux_employees_tenant_code
    ON employees (tenant_id, code);

CREATE UNIQUE INDEX IF NOT EXISTS ux_employees_tenant_phone
    ON employees (tenant_id, phone);

CREATE UNIQUE INDEX IF NOT EXISTS ux_employees_tenant_user
    ON employees (tenant_id, user_id)
    WHERE user_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS ix_employees_tenant_status
    ON employees (tenant_id, status);

CREATE INDEX IF NOT EXISTS ix_employees_tenant_department
    ON employees (tenant_id, department_id);

CREATE INDEX IF NOT EXISTS ix_employees_tenant_position
    ON employees (tenant_id, position_id);

COMMENT ON TABLE  employees IS 'Сотрудники (в рамках тенанта)';
COMMENT ON COLUMN employees.code IS 'Табельный номер. Уникален в рамках тенанта.';
COMMENT ON COLUMN employees.phone IS 'E.164 без пробелов, например +71231231212. Логин в рамках тенанта.';
COMMENT ON COLUMN employees.position_id IS 'Должен принадлежать тому же отделу, что и сотрудник.';

-- ---------------------------------------------------------------------
-- employee_qualifications
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS employee_qualifications (
                                                       id                UUID         PRIMARY KEY,
                                                       employee_id       UUID         NOT NULL,
                                                       qualification_id  UUID         NOT NULL,
                                                       level             INTEGER      NOT NULL DEFAULT 1,
                                                       assigned_at       DATE         NOT NULL DEFAULT CURRENT_DATE,
                                                       assigned_by       UUID,
                                                       notes             TEXT,
                                                       created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_eq_employee
    FOREIGN KEY (employee_id)      REFERENCES employees (id)      ON DELETE CASCADE,
    CONSTRAINT fk_eq_qualification
    FOREIGN KEY (qualification_id) REFERENCES qualifications (id) ON DELETE CASCADE,
    CONSTRAINT chk_eq_level CHECK (level >= 1)
    );

CREATE UNIQUE INDEX IF NOT EXISTS ux_eq_employee_qualification
    ON employee_qualifications (employee_id, qualification_id);

CREATE INDEX IF NOT EXISTS ix_eq_qualification
    ON employee_qualifications (qualification_id);

CREATE INDEX IF NOT EXISTS ix_eq_assigned_at
    ON employee_qualifications (assigned_at);

COMMENT ON TABLE  employee_qualifications IS 'Связь сотрудник ↔ квалификация с уровнем и датой присвоения.';
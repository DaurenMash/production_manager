-- =====================================================================
-- workstation: начальная схема
-- =====================================================================
-- Мультитенантность: shared schema + tenant_id (см. ADR-0002).
-- Идентификаторы — UUID v7, генерируются приложением.
--
-- Модель:
--   Workstation принадлежит отделу (department_id) и тенанту (tenant_id).
--   required_qualification_id — необязательная квалификация, нужная для работы.
--   department_id и required_qualification_id — логические ссылки на
--   employee-service (разные БД, FK не ставим).
-- =====================================================================

CREATE TABLE IF NOT EXISTS workstations (
                                            id                         UUID         PRIMARY KEY,
                                            tenant_id                  UUID         NOT NULL,
                                            department_id              UUID         NOT NULL,
                                            required_qualification_id  UUID,
                                            code                       VARCHAR(64)  NOT NULL,
    name                       VARCHAR(255) NOT NULL,
    description                TEXT,
    is_active                  BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by                 UUID,
    created_at                 TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at                 TIMESTAMPTZ  NOT NULL DEFAULT NOW()
    );

CREATE UNIQUE INDEX IF NOT EXISTS ux_workstations_tenant_code
    ON workstations (tenant_id, code);

CREATE INDEX IF NOT EXISTS ix_workstations_tenant_department
    ON workstations (tenant_id, department_id);

CREATE INDEX IF NOT EXISTS ix_workstations_tenant_active
    ON workstations (tenant_id, is_active);

CREATE INDEX IF NOT EXISTS ix_workstations_tenant_qualification
    ON workstations (tenant_id, required_qualification_id);

COMMENT ON TABLE  workstations IS 'Рабочие станции (в рамках тенанта)';
COMMENT ON COLUMN workstations.tenant_id IS 'Организация-арендатор.';
COMMENT ON COLUMN workstations.department_id IS 'Отдел. Логическая ссылка на employee-service.departments.';
COMMENT ON COLUMN workstations.required_qualification_id IS 'Требуемая квалификация. Логическая ссылка на employee-service.qualifications. Может быть NULL.';
COMMENT ON COLUMN workstations.code IS 'Уникален в рамках тенанта.';
COMMENT ON COLUMN workstations.created_by IS 'user_id из user-service, кто создал.';
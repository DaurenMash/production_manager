-- =====================================================================
-- work-calendar: локальные проекции workstation и employee
-- =====================================================================
-- Нужны, чтобы:
--   - показывать имена станков в календаре месяца;
--   - показывать ФИО и отдел сотрудника в слотах.
-- Заполняются через REST-синхронизацию (SyncService).
-- =====================================================================

CREATE TABLE IF NOT EXISTS workstations_ref (
                                                id            UUID         PRIMARY KEY,
                                                tenant_id     UUID         NOT NULL,
                                                code          VARCHAR(64),
    name          VARCHAR(255),
    department_id UUID,
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    synced_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW()
    );

CREATE INDEX IF NOT EXISTS ix_workstations_ref_tenant_dept
    ON workstations_ref (tenant_id, department_id);

CREATE INDEX IF NOT EXISTS ix_workstations_ref_tenant_active
    ON workstations_ref (tenant_id, is_active);

COMMENT ON TABLE workstations_ref IS 'Локальная проекция станков из workstation-service';

CREATE TABLE IF NOT EXISTS employees_ref (
                                             id             UUID         PRIMARY KEY,
                                             tenant_id      UUID         NOT NULL,
                                             full_name      VARCHAR(255),
    department_id  UUID,
    is_active      BOOLEAN      NOT NULL DEFAULT TRUE,
    synced_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
    );

CREATE INDEX IF NOT EXISTS ix_employees_ref_tenant_dept
    ON employees_ref (tenant_id, department_id);

COMMENT ON TABLE employees_ref IS 'Локальная проекция сотрудников из employee-service';
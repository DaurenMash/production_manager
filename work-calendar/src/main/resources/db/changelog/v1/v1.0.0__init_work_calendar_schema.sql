-- =====================================================================
-- work-calendar: начальная схема
-- =====================================================================
-- Мультитенантность: shared schema + tenant_id (см. ADR-0002).
-- Идентификаторы — UUID v7.
--
-- Модель:
--   TenantCalendarSettings — настройки тенанта (ночное окно).
--   ShiftPattern           — шаблон смены (справочник тенанта).
--   WorkstationDayStatus   — override «станок работает/не работает в дату».
--   WorkstationShift       — какие смены выбраны для станка в дату.
--   ShiftSlot              — слот (место для 1 сотрудника на смене станка).
-- =====================================================================

-- ---------------------------------------------------------------------
-- tenant_calendar_settings
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS tenant_calendar_settings (
                                                        tenant_id           UUID         PRIMARY KEY,
                                                        night_window_start  TIME         NOT NULL DEFAULT '22:00',
                                                        night_window_end    TIME         NOT NULL DEFAULT '06:00',
                                                        created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW()
    );

COMMENT ON TABLE  tenant_calendar_settings IS 'Настройки календаря тенанта (ночное окно и т.п.)';
COMMENT ON COLUMN tenant_calendar_settings.night_window_start IS 'Начало ночного окна (по умолчанию 22:00)';
COMMENT ON COLUMN tenant_calendar_settings.night_window_end IS 'Конец ночного окна (по умолчанию 06:00)';

-- ---------------------------------------------------------------------
-- shift_patterns
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS shift_patterns (
                                              id                  UUID          PRIMARY KEY,
                                              tenant_id           UUID          NOT NULL,
                                              code                VARCHAR(64)   NOT NULL,
    name                VARCHAR(255)  NOT NULL,
    start_time          TIME          NOT NULL,
    end_time            TIME          NOT NULL,
    crosses_midnight    BOOLEAN       NOT NULL DEFAULT FALSE,
    total_hours         NUMERIC(4,2)  NOT NULL,
    night_hours         NUMERIC(4,2)  NOT NULL DEFAULT 0,
    night_window_start  TIME,
    night_window_end    TIME,
    coefficient         NUMERIC(4,2)  NOT NULL DEFAULT 1.00,
    is_active           BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_shift_patterns_hours
    CHECK (total_hours >= 0 AND night_hours >= 0 AND night_hours <= total_hours),
    CONSTRAINT chk_shift_patterns_coefficient
    CHECK (coefficient > 0)
    );

CREATE UNIQUE INDEX IF NOT EXISTS ux_shift_patterns_tenant_code
    ON shift_patterns (tenant_id, code);

CREATE INDEX IF NOT EXISTS ix_shift_patterns_tenant_active
    ON shift_patterns (tenant_id, is_active);

COMMENT ON TABLE  shift_patterns IS 'Шаблоны смен (в рамках тенанта)';
COMMENT ON COLUMN shift_patterns.code IS 'Уникален в рамках тенанта';
COMMENT ON COLUMN shift_patterns.crosses_midnight IS 'TRUE, если смена пересекает полночь (end < start)';
COMMENT ON COLUMN shift_patterns.total_hours IS 'Всего часов смены (например 11.50)';
COMMENT ON COLUMN shift_patterns.night_hours IS 'Из них ночных (например 7.50)';
COMMENT ON COLUMN shift_patterns.night_window_start IS 'Переопределение ночного окна для этого шаблона. NULL — берётся из tenant_calendar_settings';
COMMENT ON COLUMN shift_patterns.coefficient IS 'Множитель (пока 1.00)';

-- ---------------------------------------------------------------------
-- workstation_day_status
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS workstation_day_status (
                                                      id              UUID         PRIMARY KEY,
                                                      tenant_id       UUID         NOT NULL,
                                                      workstation_id  UUID         NOT NULL,
                                                      date            DATE         NOT NULL,
                                                      is_working      BOOLEAN      NOT NULL DEFAULT TRUE,
                                                      note            TEXT,
                                                      created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
    );

CREATE UNIQUE INDEX IF NOT EXISTS ux_workstation_day_status
    ON workstation_day_status (tenant_id, workstation_id, date);

CREATE INDEX IF NOT EXISTS ix_workstation_day_status_tenant_date
    ON workstation_day_status (tenant_id, date);

COMMENT ON TABLE  workstation_day_status IS 'Override «станок работает/не работает в дату». Нет записи = работает по умолчанию.';
COMMENT ON COLUMN workstation_day_status.is_working IS 'FALSE — станок в простое в этот день';

-- ---------------------------------------------------------------------
-- workstation_shifts
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS workstation_shifts (
                                                  id                UUID         PRIMARY KEY,
                                                  tenant_id         UUID         NOT NULL,
                                                  workstation_id    UUID         NOT NULL,
                                                  date              DATE         NOT NULL,
                                                  shift_pattern_id  UUID         NOT NULL,
                                                  created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW()
    );

CREATE UNIQUE INDEX IF NOT EXISTS ux_workstation_shifts
    ON workstation_shifts (tenant_id, workstation_id, date, shift_pattern_id);

CREATE INDEX IF NOT EXISTS ix_workstation_shifts_tenant_date
    ON workstation_shifts (tenant_id, date);

CREATE INDEX IF NOT EXISTS ix_workstation_shifts_ws_date
    ON workstation_shifts (tenant_id, workstation_id, date);

COMMENT ON TABLE  workstation_shifts IS 'Какие смены (шаблоны) выбраны для станка в дату';

-- ---------------------------------------------------------------------
-- shift_slots
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS shift_slots (
                                           id                         UUID          PRIMARY KEY,
                                           tenant_id                  UUID          NOT NULL,
                                           date                       DATE          NOT NULL,
                                           workstation_id             UUID          NOT NULL,
                                           workstation_name           VARCHAR(255),
    shift_pattern_id           UUID          NOT NULL,
    shift_pattern_name         VARCHAR(255),
    required_qualification_id  UUID,
    employee_id                UUID,
    employee_full_name         VARCHAR(255),
    employee_department_id     UUID,
    planned_hours              NUMERIC(4,2)  NOT NULL,
    planned_night_hours        NUMERIC(4,2)  NOT NULL DEFAULT 0,
    actual_hours               NUMERIC(4,2),
    actual_night_hours         NUMERIC(4,2),
    coefficient_override       NUMERIC(4,2),
    status                     VARCHAR(32)   NOT NULL DEFAULT 'OPEN',
    comment                    TEXT,
    overridden                 BOOLEAN       NOT NULL DEFAULT FALSE,
    override_reason            TEXT,
    created_by                 UUID,
    updated_by                 UUID,
    created_at                 TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at                 TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_slots_status
    CHECK (status IN ('OPEN', 'FILLED', 'CANCELLED')),
    CONSTRAINT chk_slots_hours
    CHECK (planned_hours >= 0 AND planned_night_hours >= 0),
    CONSTRAINT fk_slots_pattern
    FOREIGN KEY (shift_pattern_id) REFERENCES shift_patterns (id)
    );

CREATE INDEX IF NOT EXISTS ix_slots_tenant_date
    ON shift_slots (tenant_id, date);

CREATE INDEX IF NOT EXISTS ix_slots_tenant_ws_date
    ON shift_slots (tenant_id, workstation_id, date);

CREATE INDEX IF NOT EXISTS ix_slots_tenant_employee_date
    ON shift_slots (tenant_id, employee_id, date);

CREATE INDEX IF NOT EXISTS ix_slots_tenant_department_date
    ON shift_slots (tenant_id, employee_department_id, date);

CREATE INDEX IF NOT EXISTS ix_slots_tenant_status
    ON shift_slots (tenant_id, status);

COMMENT ON TABLE  shift_slots IS 'Слот на смене станка. Может быть свободным (OPEN) или занят сотрудником (FILLED).';
COMMENT ON COLUMN shift_slots.required_qualification_id IS 'Нужная квалификация. По умолчанию — из workstation.required_qualification_id.';
COMMENT ON COLUMN shift_slots.employee_id IS 'Назначенный сотрудник. NULL = свободный слот.';
COMMENT ON COLUMN shift_slots.employee_department_id IS 'Денормализация: department_id сотрудника на момент назначения';
COMMENT ON COLUMN shift_slots.status IS 'OPEN | FILLED | CANCELLED';
COMMENT ON COLUMN shift_slots.overridden IS 'TRUE, если создано в обход правил (например, сотрудник без квалификации)';
COMMENT ON COLUMN shift_slots.override_reason IS 'Причина override (обязательна при overridden = TRUE)';
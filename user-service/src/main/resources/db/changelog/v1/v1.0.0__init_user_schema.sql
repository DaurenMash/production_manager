-- =====================================================================
-- user-service: начальная схема
-- =====================================================================
-- Мультитенантность: shared schema + tenant_id (см. ADR-0002).
-- Идентификаторы — UUID v7, генерируются приложением.
--
-- Роли:
--   PLATFORM_ADMIN — суперадмин платформы (tenant_id = NULL)
--   ADMIN, OPERATOR, ANALYST, VISITOR — роли внутри тенанта
--
-- Статусы тенанта:
--   TRIAL     — триальный период (по умолчанию 30 дней)
--   ACTIVE    — оплачен и работает
--   SUSPENDED — отключён (нет оплаты, закончился триал и т.п.)
--   CANCELLED — расторгнут
--
-- Планы тенанта:
--   FREE  — триальный, доступ ко всем сервисам
--   BASIC — платный, доступ к: расписание/станции, сотрудники, пользователи, справочники
--   UNLIM — платный, доступ ко всем сервисам
-- =====================================================================

-- ---------------------------------------------------------------------
-- tenants
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS tenants (
                                       id                     UUID         PRIMARY KEY,
                                       name                   VARCHAR(255) NOT NULL,
    slug                   VARCHAR(64)  NOT NULL,
    status                 VARCHAR(32)  NOT NULL DEFAULT 'TRIAL',
    plan                   VARCHAR(32)  NOT NULL DEFAULT 'FREE',
    trial_ends_at          TIMESTAMPTZ,
    subscription_ends_at   TIMESTAMPTZ,
    created_at             TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at             TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_tenants_status
    CHECK (status IN ('TRIAL','ACTIVE','SUSPENDED','CANCELLED')),
    CONSTRAINT chk_tenants_plan
    CHECK (plan IN ('FREE','BASIC','UNLIM'))
    );

CREATE UNIQUE INDEX IF NOT EXISTS ux_tenants_slug
    ON tenants (slug);

CREATE INDEX IF NOT EXISTS ix_tenants_status
    ON tenants (status);

COMMENT ON TABLE  tenants IS 'Организации-клиенты (тенанты)';
COMMENT ON COLUMN tenants.slug IS 'Короткий идентификатор тенанта, уникален глобально';
COMMENT ON COLUMN tenants.status IS 'TRIAL | ACTIVE | SUSPENDED | CANCELLED';
COMMENT ON COLUMN tenants.plan IS 'FREE | BASIC | UNLIM';
COMMENT ON COLUMN tenants.trial_ends_at IS 'Окончание триала. По умолчанию +30 дней от регистрации.';
COMMENT ON COLUMN tenants.subscription_ends_at IS 'Окончание оплаченного периода. NULL, если не оплачено.';

-- ---------------------------------------------------------------------
-- users
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
                                     id           UUID         PRIMARY KEY,
                                     tenant_id    UUID,
                                     username     VARCHAR(64)  NOT NULL,
    email        VARCHAR(255) NOT NULL,
    password     VARCHAR(255) NOT NULL,
    role         VARCHAR(32)  NOT NULL,
    enabled      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_users_tenant
    FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT chk_users_role
    CHECK (role IN ('PLATFORM_ADMIN','ADMIN','OPERATOR','ANALYST','VISITOR'))
    );

CREATE UNIQUE INDEX IF NOT EXISTS ux_users_tenant_username
    ON users (tenant_id, username)
    WHERE tenant_id IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS ux_users_tenant_email
    ON users (tenant_id, email)
    WHERE tenant_id IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS ux_users_platform_username
    ON users (username)
    WHERE tenant_id IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS ux_users_platform_email
    ON users (email)
    WHERE tenant_id IS NULL;

CREATE INDEX IF NOT EXISTS ix_users_tenant
    ON users (tenant_id);

COMMENT ON TABLE  users IS 'Пользователи: суперадмин платформы или пользователь тенанта';
COMMENT ON COLUMN users.tenant_id IS 'NULL для PLATFORM_ADMIN, иначе — организация-арендатор';
COMMENT ON COLUMN users.role IS 'PLATFORM_ADMIN | ADMIN | OPERATOR | ANALYST | VISITOR';

-- ---------------------------------------------------------------------
-- shift_configs
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS shift_configs (
                                             id           UUID         PRIMARY KEY,
                                             tenant_id    UUID         NOT NULL,
                                             name         VARCHAR(100) NOT NULL,
    shift_count  INTEGER      NOT NULL,
    is_active    BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_shift_configs_tenant
    FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT chk_shift_configs_count
    CHECK (shift_count IN (2, 3))
    );

CREATE INDEX IF NOT EXISTS ix_shift_configs_tenant_active
    ON shift_configs (tenant_id, is_active);

COMMENT ON TABLE  shift_configs IS 'Настройки смен тенанта (2 или 3 смены)';

-- ---------------------------------------------------------------------
-- shifts
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS shifts (
                                      id               UUID         PRIMARY KEY,
                                      tenant_id        UUID         NOT NULL,
                                      shift_config_id  UUID         NOT NULL,
                                      name             VARCHAR(50)  NOT NULL,
    start_time       VARCHAR(5)   NOT NULL,
    end_time         VARCHAR(5)   NOT NULL,
    display_order    INTEGER      NOT NULL,
    color            VARCHAR(7)   NOT NULL,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_shifts_tenant
    FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_shifts_config
    FOREIGN KEY (shift_config_id) REFERENCES shift_configs (id) ON DELETE CASCADE
    );

CREATE INDEX IF NOT EXISTS ix_shifts_tenant_config
    ON shifts (tenant_id, shift_config_id);

COMMENT ON TABLE  shifts IS 'Смены в рамках конфигурации тенанта';

-- ---------------------------------------------------------------------
-- Seed: тенант по умолчанию + админ
-- ---------------------------------------------------------------------
-- Тенант "default" — чтобы было куда логиниться в dev.
-- Пароль admin: admin123 (BCrypt).
-- Триал: 30 дней от NOW().
-- Заменим позже на нормальную регистрацию через API.

INSERT INTO tenants (id, name, slug, status, plan, trial_ends_at)
VALUES (
           '00000000-0000-0000-0000-000000000001',
           'Default Tenant',
           'default',
           'ACTIVE',
           'UNLIM',
           NULL
       ) ON CONFLICT (id) DO NOTHING;

INSERT INTO users (id, tenant_id, username, email, password, role, enabled)
VALUES (
           '00000000-0000-0000-0000-000000000002',
           '00000000-0000-0000-0000-000000000001',
           'admin',
           'admin@prodman.local',
           '$2a$10$Kwce0KaqY9ZL6nPg5iHfmuiXGQDo7k7bFHwSLZjEcSSdVrDwRvEDW',
           'ADMIN',
           TRUE
       ) ON CONFLICT (id) DO NOTHING;
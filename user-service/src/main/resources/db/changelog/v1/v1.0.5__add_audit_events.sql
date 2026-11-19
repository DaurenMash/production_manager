--liquibase formatted sql

--changeset prodman:v1.0.5-create-audit-events
-- Аудит бизнес-событий: кто что сделал (для UI PLATFORM_ADMIN).
-- Не заменяет сырые логи (они в Loki), а дополняет: хранится в БД столько, сколько нужно.
CREATE TABLE audit_events (
                              id           UUID         PRIMARY KEY,
                              tenant_id    UUID,
                              user_id      UUID,
                              username     VARCHAR(64),
                              action       VARCHAR(64)  NOT NULL,
                              target       VARCHAR(255),
                              ip           VARCHAR(45),
                              metadata     JSONB,
                              created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- индексы под типовые запросы UI:
-- «последние N событий тенанта»,
-- «последние N событий платформы (tenant_id IS NULL)»,
-- «все IMPERSONATE за период»
CREATE INDEX ix_audit_events_tenant_created
    ON audit_events (tenant_id, created_at DESC);

CREATE INDEX ix_audit_events_created
    ON audit_events (created_at DESC);

CREATE INDEX ix_audit_events_action_created
    ON audit_events (action, created_at DESC);

CREATE INDEX ix_audit_events_user_created
    ON audit_events (user_id, created_at DESC);

COMMENT ON TABLE audit_events IS 'Аудит действий пользователей (для UI PLATFORM_ADMIN)';
COMMENT ON COLUMN audit_events.tenant_id IS 'NULL для платформенных событий (login PLATFORM_ADMIN, create tenant)';
COMMENT ON COLUMN audit_events.action IS 'LOGIN | IMPERSONATE | CREATE_USER | DELETE_USER | ASSIGN_TEMP_ROLE | REVOKE_TEMP_ROLE | CREATE_TENANT | ...';
COMMENT ON COLUMN audit_events.target IS 'Объект действия: username, tenant slug, имя пользователя и т.п.';
COMMENT ON COLUMN audit_events.ip IS 'IPv4 или IPv6 клиента (X-Forwarded-For, если за прокси)';
COMMENT ON COLUMN audit_events.metadata IS 'JSONB: любые доп. поля (e.g. {"role":"ADMIN","validUntil":"..."})';
--liquibase formatted sql

--changeset prodman:1.0.1-1
CREATE TABLE IF NOT EXISTS tenant_settings (
                                               tenant_id                UUID PRIMARY KEY,
                                               month_crossing_mode      VARCHAR(32) NOT NULL DEFAULT 'KEEP_START_DAY',
    base_coefficient_name    VARCHAR(64) NOT NULL DEFAULT 'Базовая смена',
    base_coefficient_value   NUMERIC(4,2) NOT NULL DEFAULT 1.00,
    weekend_coefficient_name VARCHAR(64) NOT NULL DEFAULT 'Работа в выходной',
    weekend_coefficient_value NUMERIC(4,2) NOT NULL DEFAULT 2.00,
    night_coefficient_name   VARCHAR(64) NOT NULL DEFAULT 'Ночная надбавка',
    night_coefficient_value  NUMERIC(4,2) NOT NULL DEFAULT 1.20,
    created_at               TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at               TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );

--rollback DROP TABLE IF EXISTS tenant_settings;
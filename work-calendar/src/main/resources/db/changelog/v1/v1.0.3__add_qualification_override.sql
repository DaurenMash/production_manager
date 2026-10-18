--liquibase formatted sql

--changeset prodman:1.0.3-1
ALTER TABLE shift_slots
    ADD COLUMN IF NOT EXISTS qualification_overridden BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS qualification_override_reason VARCHAR(500) NULL;

--rollback ALTER TABLE shift_slots DROP COLUMN IF EXISTS qualification_overridden, DROP COLUMN IF EXISTS qualification_override_reason;
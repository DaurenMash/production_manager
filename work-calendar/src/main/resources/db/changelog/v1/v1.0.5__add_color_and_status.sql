--liquibase formatted sql

--changeset prodman:1.0.5-1
ALTER TABLE shift_patterns
    ADD COLUMN IF NOT EXISTS color VARCHAR(7) NULL;

--changeset prodman:1.0.5-2
ALTER TABLE employees_ref
    ADD COLUMN IF NOT EXISTS status VARCHAR(32) NULL;

--rollback ALTER TABLE shift_patterns DROP COLUMN IF EXISTS color;
--rollback ALTER TABLE employees_ref DROP COLUMN IF EXISTS status;
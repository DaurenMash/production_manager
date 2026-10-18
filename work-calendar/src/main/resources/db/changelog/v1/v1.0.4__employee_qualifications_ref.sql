--liquibase formatted sql

--changeset prodman:1.0.4-1
CREATE TABLE IF NOT EXISTS employee_qualifications_ref (
                                                           id                UUID PRIMARY KEY,
                                                           tenant_id         UUID NOT NULL,
                                                           employee_id       UUID NOT NULL,
                                                           qualification_id  UUID NOT NULL,
                                                           synced_at         TIMESTAMP NOT NULL DEFAULT NOW()
    );

CREATE INDEX IF NOT EXISTS idx_emp_qual_ref_emp
    ON employee_qualifications_ref(tenant_id, employee_id);

CREATE UNIQUE INDEX IF NOT EXISTS uq_emp_qual_ref
    ON employee_qualifications_ref(tenant_id, employee_id, qualification_id);

--rollback DROP TABLE IF EXISTS employee_qualifications_ref;
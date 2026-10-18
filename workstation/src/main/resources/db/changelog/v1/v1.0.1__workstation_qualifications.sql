--liquibase formatted sql

--changeset prodman:1.0.1-1
CREATE TABLE IF NOT EXISTS workstation_qualifications (
                                                          id              UUID PRIMARY KEY,
                                                          tenant_id       UUID NOT NULL,
                                                          workstation_id  UUID NOT NULL REFERENCES workstations(id) ON DELETE CASCADE,
    qualification_id UUID NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
    );

CREATE INDEX IF NOT EXISTS idx_ws_qual_ws
    ON workstation_qualifications(tenant_id, workstation_id);

CREATE UNIQUE INDEX IF NOT EXISTS uq_ws_qual
    ON workstation_qualifications(tenant_id, workstation_id, qualification_id);

--changeset prodman:1.0.1-2
ALTER TABLE workstations
DROP COLUMN IF EXISTS required_qualification_id;

--rollback ALTER TABLE workstations ADD COLUMN required_qualification_id UUID NULL;
--rollback DROP TABLE IF EXISTS workstation_qualifications;
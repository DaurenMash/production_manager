--liquibase formatted sql

--changeset prodman:v1.0.2-drop-old-role-constraint
ALTER TABLE users DROP CONSTRAINT IF EXISTS chk_users_role;

--changeset prodman:v1.0.2-add-new-role-constraint
ALTER TABLE users ADD CONSTRAINT chk_users_role CHECK (
    role IN (
             'PLATFORM_ADMIN',
             'ADMIN',
             'HEAD_OF_DEPARTMENT',
             'TENANT_VISITOR',
             'DEPT_VISITOR',
             'VISITOR',
             'ANALYST_1',
             'ANALYST_2'
        )
    );
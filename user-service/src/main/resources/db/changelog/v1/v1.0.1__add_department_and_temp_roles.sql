--liquibase formatted sql

--changeset prodman:v1.0.1-add-department-id
ALTER TABLE users ADD COLUMN department_id UUID;

--changeset prodman:v1.0.1-create-user-roles-table
CREATE TABLE user_roles (
                            id UUID PRIMARY KEY,
                            user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                            role VARCHAR(50) NOT NULL,
                            valid_from TIMESTAMPTZ NOT NULL DEFAULT now(),
                            valid_until TIMESTAMPTZ,
                            created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX ix_user_roles_user_id ON user_roles(user_id);
CREATE INDEX ix_user_roles_valid_until ON user_roles(valid_until);
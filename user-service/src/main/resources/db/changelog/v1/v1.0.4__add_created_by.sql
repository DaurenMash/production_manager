--liquibase formatted sql

--changeset prodman:v1.0.4-add-created-by-columns
-- Кто создал пользователя. NULL = создан системой/платформой → удалять нельзя
-- (кроме случаев, когда текущий пользователь — PLATFORM_ADMIN).
ALTER TABLE users ADD COLUMN created_by UUID;
ALTER TABLE users ADD COLUMN created_by_role VARCHAR(32);

--changeset prodman:v1.0.4-backfill-seed-user
-- Пользователь "user" (DEPT_VISITOR) создан тенантным admin'ом — проставляем.
-- admin (тенант) и PLATFORM_ADMIN остаются NULL (созданы системой/сидом).
UPDATE users
SET created_by = '00000000-0000-0000-0000-000000000002',
    created_by_role = 'ADMIN'
WHERE username = 'user'
  AND tenant_id = '00000000-0000-0000-0000-000000000001'
  AND created_by IS NULL;

--changeset prodman:v1.0.4-index-created-by
CREATE INDEX IF NOT EXISTS ix_users_created_by ON users(created_by);
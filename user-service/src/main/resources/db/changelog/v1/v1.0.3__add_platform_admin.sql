--liquibase formatted sql

--changeset prodman:v1.0.3-add-platform-admin
-- PLATFORM_ADMIN: пользователь без тенанта (tenant_id = NULL).
-- Пароль: admin123 (BCrypt-хэш совпадает с сидом v1.0.0).
-- Логин без slug (slug = null) → AuthService.login ищет findByTenantIdIsNullAndUsername.
INSERT INTO users (id, tenant_id, username, email, password, role, enabled)
VALUES (
           '00000000-0000-0000-0000-000000000099',
           NULL,
           'admin',
           'platform-admin@prodman.local',
           '$2a$10$Kwce0KaqY9ZL6nPg5iHfmuiXGQDo7k7bFHwSLZjEcSSdVrDwRvEDW',
           'PLATFORM_ADMIN',
           TRUE
       ) ON CONFLICT (id) DO NOTHING;
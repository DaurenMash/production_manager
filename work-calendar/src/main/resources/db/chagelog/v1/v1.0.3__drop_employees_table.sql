-- Удаляем таблицу employees (она перенесена в employee-service)
DROP TABLE IF EXISTS employees CASCADE;

-- Удаляем связанные индексы
DROP INDEX IF EXISTS idx_employees_email;
DROP INDEX IF EXISTS idx_employees_status;
-- ============================================
-- Добавление полей в таблицу employees
-- для расширенной информации о сотрудниках
-- ============================================

-- Добавляем новые колонки
ALTER TABLE employees ADD COLUMN IF NOT EXISTS phone_number VARCHAR(20);
ALTER TABLE employees ADD COLUMN IF NOT EXISTS department VARCHAR(100);
ALTER TABLE employees ADD COLUMN IF NOT EXISTS position VARCHAR(100);
ALTER TABLE employees ADD COLUMN IF NOT EXISTS max_hours_per_week INTEGER DEFAULT 40;
ALTER TABLE employees ADD COLUMN IF NOT EXISTS preferred_shift VARCHAR(20);
ALTER TABLE employees ADD COLUMN IF NOT EXISTS vacation_start DATE;
ALTER TABLE employees ADD COLUMN IF NOT EXISTS vacation_end DATE;
ALTER TABLE employees ADD COLUMN IF NOT EXISTS sick_leave_start DATE;
ALTER TABLE employees ADD COLUMN IF NOT EXISTS sick_leave_end DATE;

-- Добавляем колонку для связи с пользователем (1:1)
ALTER TABLE employees ADD COLUMN IF NOT EXISTS user_id VARCHAR(36) UNIQUE;

-- Добавляем индексы для быстрого поиска
CREATE INDEX IF NOT EXISTS idx_employees_phone ON employees(phone_number);
CREATE INDEX IF NOT EXISTS idx_employees_department ON employees(department);
CREATE INDEX IF NOT EXISTS idx_employees_user_id ON employees(user_id);

-- Комментарии к колонкам
COMMENT ON COLUMN employees.phone_number IS 'Номер телефона сотрудника';
COMMENT ON COLUMN employees.department IS 'Отдел';
COMMENT ON COLUMN employees.position IS 'Должность';
COMMENT ON COLUMN employees.max_hours_per_week IS 'Максимальное количество часов в неделю';
COMMENT ON COLUMN employees.preferred_shift IS 'Предпочтительная смена';
COMMENT ON COLUMN employees.vacation_start IS 'Начало отпуска';
COMMENT ON COLUMN employees.vacation_end IS 'Конец отпуска';
COMMENT ON COLUMN employees.sick_leave_start IS 'Начало больничного';
COMMENT ON COLUMN employees.sick_leave_end IS 'Конец больничного';
COMMENT ON COLUMN employees.user_id IS 'Связь 1:1 с пользователем (user-service)';
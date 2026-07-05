-- ============================================
-- Таблица настроек смен
-- ============================================
CREATE TABLE IF NOT EXISTS shift_configs (
                                             id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    shift_count INTEGER NOT NULL CHECK (shift_count IN (2, 3)),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

COMMENT ON TABLE shift_configs IS 'Настройки смен (2 или 3 смены)';
COMMENT ON COLUMN shift_configs.shift_count IS 'Количество смен: 2 или 3';
COMMENT ON COLUMN shift_configs.is_active IS 'Активная конфигурация (только одна)';

-- ============================================
-- Таблица смен
-- ============================================
CREATE TABLE IF NOT EXISTS shifts (
                                      id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    start_time VARCHAR(5) NOT NULL,
    end_time VARCHAR(5) NOT NULL,
    display_order INTEGER NOT NULL,
    color VARCHAR(7) NOT NULL,
    shift_config_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (shift_config_id) REFERENCES shift_configs(id) ON DELETE CASCADE
    );

COMMENT ON TABLE shifts IS 'Отдельные смены в рамках конфигурации';
COMMENT ON COLUMN shifts.name IS 'Название смены: Утро, День, Ночь';
COMMENT ON COLUMN shifts.start_time IS 'Время начала (HH:MM)';
COMMENT ON COLUMN shifts.end_time IS 'Время окончания (HH:MM)';
COMMENT ON COLUMN shifts.display_order IS 'Порядок отображения';
COMMENT ON COLUMN shifts.color IS 'Цвет для календаря (HEX)';

-- Индексы
CREATE INDEX IF NOT EXISTS idx_shifts_config ON shifts(shift_config_id);
CREATE INDEX IF NOT EXISTS idx_shift_configs_active ON shift_configs(is_active);

-- ============================================
-- Данные по умолчанию
-- ============================================

-- 1. Конфигурация: 2 смены (День/Ночь) — активна по умолчанию
INSERT INTO shift_configs (id, name, shift_count, is_active)
VALUES ('default-2-shifts', '2 смены (День/Ночь)', 2, true)
    ON CONFLICT (id) DO NOTHING;

INSERT INTO shifts (id, name, start_time, end_time, display_order, color, shift_config_id) VALUES
                                                                                               (gen_random_uuid()::varchar, 'День', '08:00', '20:00', 1, '#2ecc71', 'default-2-shifts'),
                                                                                               (gen_random_uuid()::varchar, 'Ночь', '20:00', '08:00', 2, '#3498db', 'default-2-shifts')
    ON CONFLICT DO NOTHING;

-- 2. Конфигурация: 3 смены (Утро/День/Ночь) — неактивна
INSERT INTO shift_configs (id, name, shift_count, is_active)
VALUES ('default-3-shifts', '3 смены (Утро/День/Ночь)', 3, false)
    ON CONFLICT (id) DO NOTHING;

INSERT INTO shifts (id, name, start_time, end_time, display_order, color, shift_config_id) VALUES
                                                                                               (gen_random_uuid()::varchar, 'Утро', '06:00', '14:00', 1, '#f1c40f', 'default-3-shifts'),
                                                                                               (gen_random_uuid()::varchar, 'День', '14:00', '22:00', 2, '#2ecc71', 'default-3-shifts'),
                                                                                               (gen_random_uuid()::varchar, 'Ночь', '22:00', '06:00', 3, '#3498db', 'default-3-shifts')
    ON CONFLICT DO NOTHING;
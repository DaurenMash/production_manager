CREATE TABLE IF NOT EXISTS positions (
                                         id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    color VARCHAR(20) NOT NULL DEFAULT '#3498db',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_positions_name ON positions(name);

COMMENT ON TABLE positions IS 'Справочник должностей';
COMMENT ON COLUMN positions.color IS 'HEX-цвет для UI-маркировки';
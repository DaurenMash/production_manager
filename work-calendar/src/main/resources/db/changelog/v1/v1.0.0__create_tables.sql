--liquibase formatted sql

--changeset workcalendar:1
CREATE TABLE IF NOT EXISTS employees (
                                         id VARCHAR(36) PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL,
    phone_number VARCHAR(20),
    department VARCHAR(50),
    max_hours_per_week INTEGER DEFAULT 40,
    preferred_shift VARCHAR(20),
    vacation_start DATE,
    vacation_end DATE,
    sick_leave_start DATE,
    sick_leave_end DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

--changeset workcalendar:2
CREATE TABLE IF NOT EXISTS employee_positions (
                                                  employee_id VARCHAR(36) NOT NULL,
    position VARCHAR(50) NOT NULL,
    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE
    );

--changeset workcalendar:3
CREATE TABLE IF NOT EXISTS schedules (
                                         id VARCHAR(36) PRIMARY KEY,
    date DATE NOT NULL UNIQUE,
    shift_config VARCHAR(20) NOT NULL,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

--changeset workcalendar:4
CREATE TABLE IF NOT EXISTS shifts (
                                      id VARCHAR(36) PRIMARY KEY,
    employee_id VARCHAR(36) NOT NULL,
    schedule_id VARCHAR(36) NOT NULL,
    date DATE NOT NULL,
    shift_type VARCHAR(20) NOT NULL,
    is_available BOOLEAN DEFAULT TRUE,
    is_holiday BOOLEAN DEFAULT FALSE,
    note TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    FOREIGN KEY (schedule_id) REFERENCES schedules(id) ON DELETE CASCADE
    );

--changeset workcalendar:5
CREATE TABLE IF NOT EXISTS equipment_schedules (
                                                   id VARCHAR(36) PRIMARY KEY,
    equipment_id VARCHAR(36) NOT NULL,
    equipment_name VARCHAR(100) NOT NULL,
    date DATE NOT NULL,
    is_working BOOLEAN DEFAULT TRUE,
    shift_type VARCHAR(20),
    note TEXT,
    maintenance_scheduled BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

--changeset workcalendar:6
CREATE INDEX idx_shifts_employee_date ON shifts(employee_id, date);
CREATE INDEX idx_shifts_date ON shifts(date);
CREATE INDEX idx_equipment_schedules_date ON equipment_schedules(date);
CREATE INDEX idx_employees_status ON employees(status);
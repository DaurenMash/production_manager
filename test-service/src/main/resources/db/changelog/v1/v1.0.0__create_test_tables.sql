-- Тесты
CREATE TABLE IF NOT EXISTS tests (
                                     id VARCHAR(36) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    created_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

-- Вопросы
CREATE TABLE IF NOT EXISTS questions (
                                         id VARCHAR(36) PRIMARY KEY,
    text TEXT NOT NULL,
    multiple_choice BOOLEAN DEFAULT FALSE,
    test_id VARCHAR(36) NOT NULL,
    FOREIGN KEY (test_id) REFERENCES tests(id) ON DELETE CASCADE
    );

-- Варианты ответов
CREATE TABLE IF NOT EXISTS question_options (
                                                question_id VARCHAR(36) NOT NULL,
    option_text TEXT NOT NULL,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
    );

-- Правильные ответы (индексы)
CREATE TABLE IF NOT EXISTS question_correct_options (
                                                        question_id VARCHAR(36) NOT NULL,
    correct_index INTEGER NOT NULL,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
    );

-- Результаты тестов
CREATE TABLE IF NOT EXISTS test_results (
                                            id VARCHAR(36) PRIMARY KEY,
    test_id VARCHAR(36) NOT NULL,
    employee_id VARCHAR(36) NOT NULL,
    employee_name VARCHAR(255),
    score DOUBLE PRECISION NOT NULL,
    passed BOOLEAN NOT NULL,
    completed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

-- Ответы пользователя
CREATE TABLE IF NOT EXISTS answers (
                                       id VARCHAR(36) PRIMARY KEY,
    result_id VARCHAR(36) NOT NULL,
    question_id VARCHAR(36) NOT NULL,
    FOREIGN KEY (result_id) REFERENCES test_results(id) ON DELETE CASCADE
    );

-- Выбранные варианты
CREATE TABLE IF NOT EXISTS answer_selected_options (
                                                       answer_id VARCHAR(36) NOT NULL,
    selected_index INTEGER NOT NULL,
    FOREIGN KEY (answer_id) REFERENCES answers(id) ON DELETE CASCADE
    );

-- Индексы
CREATE INDEX idx_tests_created_by ON tests(created_by);
CREATE INDEX idx_test_results_test ON test_results(test_id);
CREATE INDEX idx_test_results_employee ON test_results(employee_id);
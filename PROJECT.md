# ProdMan — Production Manager

Приложение для помощи менеджеру производства:
- Расстановка сотрудников по рабочим станциям на основе квалификации.
- Расчёт рабочих часов за месяц, включая отдельный учёт ночных часов.

## Стек

### Backend
- Java 21, Spring Boot 3
- PostgreSQL (по сервисам), Redis (кэш/сессии)
- Liquibase (миграции)
- JWT (auth)
- Сервисы: `user-service`, `employee-service`, `work-calendar`, `test-service`, `workstation`, `prodman-api-gateway`

### Frontend
- React 18, TypeScript, Vite
- Ant Design, axios, react-router-dom, zustand

### Инфраструктура
- Docker / docker-compose
- Helm (в `prodman-helm/`)

## Архитектура

Микросервисы за API Gateway (Spring Cloud Gateway). Каждый сервис — отдельная БД.

- **user-service** — пользователи, роли, JWT, конфиг смен.
- **employee-service** — сотрудники, отделы, должности (квалификация).
- **work-calendar** — расписания, смены, назначения на оборудование.
- **test-service** — тесты (проверка квалификации).
- **workstation** — рабочие станции.
- **prodman-api-gateway** — маршрутизация, JWT-фильтр, rate-limit, логирование.

Фронт: `prodman-web-react/` (SPA). Desktop-версия (`prodman-desktop/`) удалена — переход на веб-интерфейс завершён.

## Структура репозитория

- `user-service/` — сервис пользователей
- `employee-service/` — сервис сотрудников, отделов, должностей
- `work-calendar/` — сервис расписаний и смен
- `test-service/` — сервис тестов
- `workstation/` — сервис рабочих станций
- `prodman-api-gateway/` — API Gateway
- `prodman-web-react/` — React SPA
- `prodman-helm/` — Helm-чарты
- `docker-compose.yml` — локальный запуск инфраструктуры
- `run_*.bat` — скрипты запуска сервисов (Windows)
- `docs/` — документация (ADR, архитектура)

## Текущий этап

Справочники отделов и должностей в `employee-service` + UI.
Далее: ночные часы, расчёт часов за месяц.

## Как передать контекст в новый чат

1. Приложить `PROJECT.md`.
2. Приложить `docs/architecture.md` (Mermaid-диаграмма).
3. Приложить нужный ADR из `docs/adr/`, если вопрос про конкретное решение.
4. Приложить файлы, которые относятся к задаче (не весь репо).

## Соглашения

- Коммиты: `feat(scope): ...`, `fix(scope): ...`, `chore(scope): ...`
- Ветки: `<feature>_<short_desc>` (например `work_calendar_improvement`)
- API-контракты: держать в `docs/api/` (заведём позже)
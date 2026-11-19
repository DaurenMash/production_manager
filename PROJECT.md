# ProdMan — Production Manager

SaaS-приложение для управления производством. 
Каждый **клиент** (организация-арендатор) получает свой **личный кабинет** с сотрудниками, отделами, должностями, станциями, расписаниями и тестами.

## Что делает приложение

- Расстановка сотрудников по рабочим станциям на основе квалификации.
- Расчёт рабочих часов за месяц, отдельно — ночных.
- Справочники (сотрудники, отделы, должности, станции, квалификации).
- Тесты квалификации сотрудников.
- Управление клиентами платформы (superadmin) + имперсонация в контекст тенанта.

Данные одного клиента **полностью изолированы** от других.

## Модель работы

- **Клиент (tenant / организация)** — регистрируется в системе, получает изолированный кабинет.
- **Пользователи клиента** — роли внутри организации (см. «Роли и иерархия»).
- **PLATFORM_ADMIN** — суперадмин платформы (`tenant_id = NULL`). Управляет тенантами, видит статистику, может имперсонироваться в тенант.

Все бизнес-сущности принадлежат клиенту: у каждой есть `tenant_id`.

## Роли и иерархия (`Role.java` → `level()`)

| Роль | Уровень | Описание |
|---|---|---|
| PLATFORM_ADMIN | 0 | Админ платформы (вне иерархии тенанта) |
| ADMIN | 100 | Полный доступ в тенанте |
| HEAD_OF_DEPARTMENT | 80 | Управляет своим департаментом |
| TENANT_VISITOR | 60 | Видит весь тенант, не меняет |
| DEPT_VISITOR | 40 | Видит свой департамент |
| VISITOR | 20 | Демо-доступ |
| ANALYST_1/2 | 0 | Заглушки |

**Правило:** роль может создать/назначить себе подобного или ниже (`target.level() <= current.level()`). `PLATFORM_ADMIN` назначать нельзя никому. Управление (отзыв, удаление) строже: `target.level() < current.level()`.

## Стек

### Backend
- Java 21, Spring Boot 3
- PostgreSQL (database-per-service, shared schema + tenant_id)
- Liquibase (миграции)
- JWT (auth; в токене — userId, tenantId, roles[], tenantStatus, departmentId)
- Сервисы: `user-service` (8081), `employee-service` (8084), `workstation` (8083), `work-calendar` (8082), `test-service` (8085), `prodman-api-gateway` (8888)

### Frontend
- React 18, TypeScript, Vite
- Ant Design 6, axios, react-router-dom, zustand
- Одно SPA: разные роли → разные меню (`PLATFORM_ADMIN` → дашборд платформы; остальные → кабинет тенанта)

### Инфраструктура
- Docker / docker-compose
- Helm (в `prodman-helm/`)
- Порты БД: 54322 (users), 54344 (workstations), 54355 (employees), 54333 (calendar), 54366 (tests)

## Архитектура

Микросервисы за API Gateway. У каждого — своя БД.

- **user-service** — организации, пользователи, роли, JWT, временные роли, аудит создания. Публичные эндпоинты: `/api/v1/auth/**`, `/api/v1/users/**`, `/api/v1/tenants/**` (только PLATFORM_ADMIN).
- **employee-service** — сотрудники, отделы, должности, квалификации, `tenant_settings`.
- **workstation** — станции + `workstation_qualifications` (M:N).
- **work-calendar** — расписания, смены, слоты, ref-кэш квалификаций.
- **test-service** — тесты квалификации (пока НЕ мультитенантный).
- **prodman-api-gateway** — маршрутизация + JWT-фильтр.

Фронт: `prodman-web-react/`.

## Мультитенантность

Подход **shared database, shared schema, tenant_id** (см. ADR-0002). Правила:

1. У каждой бизнес-сущности — `tenant_id`.
2. Запросы **всегда** фильтруются по `tenant_id`.
3. `tenant_id` **никогда** не приходит в теле от клиента — только из JWT (через `JwtAuthenticationFilter` / `TenantFilter`).
4. Бизнес-ключи уникальны в рамках тенанта: `(tenant_id, code)`.

## Аутентификация

- JWT HS512. Claims: `sub` (username), `userId`, `roles[]`, `tenantId`, `tenantStatus`, `departmentId`.
- `JwtAuthenticationFilter` — парсит токен, заполняет `SecurityContext` (authorities `ROLE_*`), кладёт `userId` в `details`.
- `TenantFilter` — читает `X-Tenant-Id` (шлёт Gateway или сам клиент в dev), кладёт в `TenantContext`.
- `SecurityConfig`:
  - `/api/v1/auth/**`, swagger, actuator — `permitAll`;
  - `/api/v1/users/**` — `authenticated`;
  - `/api/v1/tenants/**` — `hasRole('PLATFORM_ADMIN')`;
  - остальное — `authenticated`.
- `X-User-Role` больше не используется (удалён из фронта).

## Тарифы, триалы, статусы

### TenantStatus
- `TRIAL` — триал (30 дней).
- `ACTIVE` — оплачен.
- `SUSPENDED` — отключён.
- `CANCELLED` — расторгнут.

### TenantPlan
- `FREE` — триал, все сервисы.
- `BASIC` — платный (расписание/станции, сотрудники, пользователи, справочники).
- `UNLIM` — платный, все сервисы.

### Уведомления и платежи
- Уведомления (5 и 1 день до `trial_ends_at`) — отдельный сервис, позже.
- `payment-service` — отдельный сервис, позже.

### Верификация тенанта (бэклог, не блокирует MVP)

Проверка «реальности» организации через ИНН. Пока не реализовано, но модель заложена.

**Поля в `Tenant`:**
- `inn VARCHAR(12)` — nullable. ИНН организации (10 цифр для юрлиц, 12 для ИП).
- `verification_status VARCHAR(32) NOT NULL DEFAULT 'UNVERIFIED'` — `UNVERIFIED | PENDING | VERIFIED | REJECTED`.
- `verified_at TIMESTAMPTZ` — nullable. Когда подтверждено.
- `verified_by UUID` — nullable. Кто подтвердил (PLATFORM_ADMIN или система через ФНС).

**Логика:**
- При регистрации тенанта — `verification_status = UNVERIFIED`.
- Триал работает, но с ограничениями для `UNVERIFIED`: например, нельзя пригласить > 5 пользователей, нельзя выгружать отчёты в CSV.
- `PLATFORM_ADMIN` может подтвердить вручную (кнопка в `PlatformDashboard` → «Подтвердить») или отклонить (`REJECTED`).
- Позже — автоматическая проверка через ФНС API (запрос по ИНН → сверка названия/ОГРН). Если ФНС недоступен — ручной режим.

**UI:**
- `PlatformDashboard`: колонка «Верификация» (`UNVERIFIED` / `PENDING` / `VERIFIED` / `REJECTED` с цветными тегами) + кнопка «Подтвердить / Отклонить» для `PLATFORM_ADMIN`.
- В кабинете тенанта: баннер «Верифицируйте организацию, чтобы снять ограничения» с формой ввода ИНН и загрузкой документов.

**API (план):**
- `POST /api/v1/tenants/{id}/verify` — `PLATFORM_ADMIN` подтверждает.
- `POST /api/v1/tenants/{id}/reject` — `PLATFORM_ADMIN` отклоняет.
- `PUT /api/v1/tenants/{id}/inn` — тенант вводит ИНН (сам).
- `GET /api/v1/tenants/{id}/verification` — статус.

**Не сейчас.** Сначала — регистрация, метрики, демо-страницы.

## Аудит создания пользователей (`created_by`)

- `users.created_by UUID` — кто создал.
- `users.created_by_role VARCHAR(32)` — роль создателя на момент создания.
- Правила удаления:
  - нельзя удалить себя;
  - `created_by = null` (сид / платформа) → 403 «System user cannot be deleted»;
  - `created_by_role = PLATFORM_ADMIN` → 403;
  - `created_by != currentUserId` → 403 «Only creator can delete this user»;
  - роль удаляемого должна быть **строго ниже** текущей.

## Имперсонация (в разработке, план)

`PLATFORM_ADMIN` из дашборда тенантов может «войти» в тенант:
- `POST /api/v1/tenants/{id}/impersonate` → `AuthResponse` с `role=ADMIN`, `tenantId=<id>`.
- На фронте два слота в `localStorage`: `accessToken` (текущий) и `platformAccessToken` (всегда платформенный).
- «Выйти из тенанта» → `accessToken = platformAccessToken`, редирект на `/platform-dashboard`.

## Структура репозитория

- `user-service/` — организации и пользователи
- `employee-service/` — сотрудники, отделы, должности, квалификации
- `work-calendar/` — расписания и смены
- `test-service/` — тесты
- `workstation/` — рабочие станции
- `prodman-api-gateway/` — Gateway
- `prodman-web-react/` — React SPA
- `prodman-helm/` — Helm-чарты
- `docker-compose.yml` — локальная инфраструктура
- `run_*.bat` — скрипты запуска (Windows)
- `docs/` — ADR, архитектура, схемы БД

## Текущий этап

Календарь: расстановка сотрудников по станкам с проверкой квалификации + отчёты.

Сделано:
- Справочники (employee-service / workstation).
- Мультитенантность во всех сервисах, кроме test-service.
- `workstation_qualifications` (M:N), `required_qualification_id` в слоте.
- 409 QUALIFICATION_MISMATCH + override (причина).
- Ref-кэш `employee_qualifications_ref` + `POST /api/sync/all`.
- Визуальная валидация: оранжевый слот/день при mismatch.
- `tenant_settings`: 3 коэффициента (base/weekend/night) + month_crossing_mode.
- Отчёт «Часы по сотрудникам» + CSV.
- JWT-фильтр, `SecurityContext`, иерархия ролей.
- Аудит `created_by` / `created_by_role`.
- `TenantController` (`GET /api/v1/tenants`).
- Фронт: `PlatformDashboard.tsx` (статистика + список тенантов + заглушки логов/метрик), `UsersList.tsx` с иерархией и колонкой «Создатель».

Далее:
1. Имперсонация (`/tenants/{id}/impersonate` + фронт).
2. Регистрация тенантов через UI.
3. Уведомления о триале (`notification-service`).
4. `payment-service`.
5. test-service — мультитенантность.
6. Код-сплиттинг фронта, кастомные 401/403 JSON.

## Как передать контекст в новый чат

1. Приложить `PROJECT.md`.
2. Приложить `docs/architecture.md`.
3. Приложить нужный ADR из `docs/adr/`.
4. Приложить файлы, относящиеся к задаче (не весь репо).

## Соглашения

- Коммиты: `feat(scope): ...`, `fix(scope): ...`, `chore(scope): ...`.
- Ветки: `<feature>_<short_desc>`.
- API-контракты — в `docs/api/`.
- Схемы БД — `docs/db/<service>.dbml`.
- **JPA:** не использовать `@Data` на `@Entity`. Только `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`. `@ToString(onlyExplicitlyIncluded = true)` — только на безопасных полях. `equals/hashCode` — не генерировать (identity по умолчанию, при необходимости — вручную по `id`). См. ADR-0003.
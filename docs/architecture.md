# Архитектура ProdMan

ProdMan — SaaS-приложение: каждая организация-клиент работает в изолированном личном кабинете. Все бизнес-данные принадлежат клиенту и разделяются по `tenant_id`. Отдельно — `PLATFORM_ADMIN` (вне тенантов), который управляет клиентами платформы.

## Уровень 1. Контекст (C4 Context)

```mermaid
flowchart LR
    ClientUser([Пользователь клиента<br/>ADMIN / HEAD / VISITOR])
    SuperAdmin([PLATFORM_ADMIN<br/>админ платформы])

    ProdMan[ProdMan<br/>SaaS-платформа]
    DB[(PostgreSQL)]
    Redis[(Redis)]

    ClientUser -->|личный кабинет| ProdMan
    SuperAdmin -->|дашборд платформы| ProdMan

    ProdMan --> DB
    ProdMan --> Redis
```

## Уровень 2. Контейнеры (C4 Container)

```mermaid
flowchart TB
    Browser([Браузер])

    subgraph Front["Frontend"]
        SPA[React SPA<br/>prodman-web-react]
    end

    subgraph Back["Backend (микросервисы)"]
        GW[API Gateway<br/>prodman-api-gateway<br/>JWT + tenantId]
        US[user-service<br/>tenants + users + roles]
        ES[employee-service<br/>employees + dictionaries]
        WC[work-calendar<br/>schedules + slots]
        TS[test-service]
        WS[workstation<br/>workstations + quals]
    end

    subgraph Data["Хранилища (database-per-service)"]
        DBU[(prodman_users)]
        DBE[(prodman_employees)]
        DBW[(prodman_calendar)]
        DBT[(prodman_tests)]
        DBX[(prodman_workstations)]
        RDS[(Redis)]
    end

    Browser -->|HTTP/JSON| SPA
    SPA -->|REST через Gateway<br/>Bearer JWT| GW

    GW --> US
    GW --> ES
    GW --> WC
    GW --> TS
    GW --> WS

    US --- DBU
    ES --- DBE
    WC --- DBW
    TS --- DBT
    WS --- DBX

    GW --- RDS
    US --- RDS
```

**Ключевое:** Gateway извлекает `tenantId` из JWT и прокидывает `X-Tenant-Id` во внутренние сервисы. `PLATFORM_ADMIN` не имеет `tenantId`, поэтому его запросы идут без `X-Tenant-Id`, а `/api/v1/tenants/**` разрешён только ему.

## Уровень 3. Компоненты user-service

```mermaid
flowchart LR
    subgraph US["user-service"]
        JwtFilter[JwtAuthenticationFilter<br/>Bearer → SecurityContext]
        TenantFilter[TenantFilter<br/>X-Tenant-Id → TenantContext]
        AC[AuthController<br/>/api/v1/auth]
        UC[UserController<br/>/api/v1/users]
        TC[TenantController<br/>/api/v1/tenants]
        AS[AuthService]
        USvc[UserService<br/>role hierarchy, createdBy, delete rules]
        TSvc[TenantService<br/>list + impersonate]
        Repos[Repositories<br/>UserRepository, TenantRepository, UserRoleRepository]
    end

    JwtFilter --> AC
    JwtFilter --> UC
    JwtFilter --> TC
    TenantFilter --> UC
    AC --> AS
    UC --> USvc
    TC --> TSvc
    USvc --> Repos
    TSvc --> Repos
    Repos --> PG[(PostgreSQL<br/>prodman_users)]
```

## Роли и иерархия

```mermaid
flowchart TD
    PA[PLATFORM_ADMIN<br/>level 0, tenant_id=NULL]
    AD[ADMIN<br/>level 100]
    HD[HEAD_OF_DEPARTMENT<br/>level 80]
    TV[TENANT_VISITOR<br/>level 60]
    DV[DEPT_VISITOR<br/>level 40]
    V[VISITOR<br/>level 20]

    PA -.->|управляет| AD
    AD --> HD
    HD --> TV
    TV --> DV
    DV --> V

    classDef plat fill:#7a2a7a,stroke:#fff,color:#fff
    classDef admin fill:#c0392b,stroke:#fff,color:#fff
    classDef head fill:#e67e22,stroke:#fff,color:#fff
    classDef vis fill:#3498db,stroke:#fff,color:#fff
    classDef dept fill:#16a085,stroke:#fff,color:#fff
    classDef demo fill:#555,stroke:#fff,color:#fff

    class PA plat
    class AD admin
    class HD head
    class TV vis
    class DV dept
    class V demo
```

**Правило назначения:** `target.level() <= current.level()`. `PLATFORM_ADMIN` назначать нельзя. Удаление/отзыв — `target.level() < current.level()`.

## Модель мультитенантности

```mermaid
flowchart LR
    T[Tenant<br/>Организация-клиент]
    U[Пользователи клиента]
    E[Сотрудники]
    D[Отделы]
    P[Должности]
    W[Рабочие станции]
    S[Расписания]
    TR[Тесты]

    T -->|1:N| U
    T -->|1:N| E
    T -->|1:N| D
    T -->|1:N| P
    T -->|1:N| W
    T -->|1:N| S
    T -->|1:N| TR
```

Правила:
- У каждой бизнес-сущности — `tenant_id`.
- `tenant_id` приходит **только из JWT** (через `X-Tenant-Id` от Gateway) или из `TenantContext` в user-service.
- Уникальность — составная: `(tenant_id, code)`.
- Пользователь видит только данные своего тенанта.

Подробнее — [ADR-0002](./adr/0002-multi-tenancy.md).

## Квалификации: станок ↔ сотрудник

```mermaid
flowchart LR
    Q[employee-service.qualifications]
    EQ[employee_qualifications<br/>M:N сотрудник-квал]
    WQ[workstation_qualifications<br/>M:N станок-квал]
    SS[shift_slots.required_qualification_id]

    Q --> EQ
    Q --> WQ
    WQ --> SS
```

Поток:
1. `shift_slot` хранит `required_qualification_id` (одну из квалификаций станка) или `null`.
2. При `assign` `work-calendar` сверяет `employee_qualifications_ref` (локальный кэш).
3. Не совпало → **409 QUALIFICATION_MISMATCH** + модалка override.
4. Override → `qualification_overridden = true`, `qualification_override_reason` сохраняется.
5. Визуально: слот и день оранжевые (`qualificationMismatch = true`) — независимо от override.

Синк `employee_qualifications_ref`: при каждом `assign` + `POST /api/sync/all`.

## Потоки аутентификации

### Логин тенантного пользователя
1. `POST /api/v1/auth/login` с `{ slug, username, password }`.
2. `AuthService` ищет `(tenant, username)`, проверяет пароль, статус тенанта.
3. Собирает `activeRoles` = основная + временные (активные на сейчас).
4. `JwtService.generateToken` → access (24ч) + refresh (7д).
5. `AuthResponse` содержит `role` (основная), `tenantId`, `tenantSlug`, `tenantStatus`, `tenantPlan`, `departmentId`.

### Логин PLATFORM_ADMIN
1. `POST /api/v1/auth/login` с `{ username, password }` (без slug).
2. `findByTenantIdIsNullAndUsername` → `PLATFORM_ADMIN`.
3. Токен без `tenantId`, без `tenantStatus`. `AuthResponse` с пустыми `tenantId`/`tenantSlug`.

### Имперсонация (план)
1. `PLATFORM_ADMIN` открывает `/platform-dashboard`, видит список тенантов.
2. `POST /api/v1/tenants/{id}/impersonate` → `AuthResponse` с `role=ADMIN`, `tenantId=<id>`, `tenantSlug`, `tenantStatus`, `tenantPlan`.
3. Фронт: `localStorage.accessToken = новый`, `localStorage.platformAccessToken = старый`.
4. Меню тенанта. «Выйти из тенанта» → восстановить `platformAccessToken`.

## Основные потоки

### Расстановка сотрудников
1. SPA запрашивает сотрудников (`employee-service`) и станции (`workstation`) — только своего тенанта.
2. Менеджер назначает через `POST /work-calendar/api/shift-slots/{id}/assign`.
3. `work-calendar` проверяет квалификацию + занятость, сохраняет слот.
4. Отчёты: `GET /work-calendar/api/reports/hours/by-employee`, `/schedule`.

## Принципы

- Database-per-service.
- Мультитенантность: shared schema + `tenant_id` (ADR-0002).
- REST-общение, синхронно.
- JWT — единственный источник идентичности и `tenantId`.
- Liquibase — все изменения схемы.
- Фронт не знает внутренних адресов — только Gateway (или Vite proxy в dev).

## Связанные документы

- [ADR](./adr/)
- [PROJECT.md](../PROJECT.md)
- [README.md](../README.md)
```

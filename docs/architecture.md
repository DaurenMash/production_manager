# Архитектура ProdMan

ProdMan — SaaS-приложение: каждая организация-клиент работает в своём изолированном личном кабинете. Все бизнес-данные принадлежат клиенту и разделяются по `tenant_id`.

## Уровень 1. Контекст (C4 Context)

```mermaid
flowchart LR
    ClientUser([Пользователь клиента<br/>админ или менеджер])
    SuperAdmin([Суперадмин платформы])

    ProdMan[ProdMan<br/>SaaS-платформа]
    DB[(PostgreSQL)]
    Redis[(Redis)]

    ClientUser -->|личный кабинет| ProdMan
    SuperAdmin -->|управление клиентами| ProdMan

    ProdMan --> DB
    ProdMan --> Redis
```

## Уровень 2. Контейнеры (C4 Container)

```mermaid
flowchart TB
    Browser([Браузер клиента])

    subgraph Front["Frontend"]
        SPA[React SPA<br/>prodman-web-react]
    end

    subgraph Back["Backend (микросервисы)"]
        GW[API Gateway<br/>prodman-api-gateway<br/>JWT + tenantId]
        US[user-service<br/>tenants + users]
        ES[employee-service]
        WC[work-calendar]
        TS[test-service]
        WS[workstation]
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

**Ключевое:** Gateway извлекает `tenantId` из JWT и прокидывает в заголовке (`X-Tenant-Id`) во внутренние сервисы. Сервисы фильтруют данные по `tenantId`.

## Уровень 3. Компоненты (пример: employee-service)

```mermaid
flowchart LR
    subgraph ES["employee-service"]
        Filter[TenantFilter<br/>из X-Tenant-Id]
        EController[Controllers<br/>Employee / Department / Position]
        EService[Services<br/>EmployeeService / DepartmentService / PositionService]
        ERepo[Repositories<br/>Spring Data JPA<br/>всегда с tenantId]
        EMapper[DTO / Mapping]
    end

    EController --> Filter
    EController --> EService
    EService --> ERepo
    EService --> EMapper
    ERepo --> PG[(PostgreSQL<br/>prodman_employees)]
```

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
- У каждой бизнес-сущности — колонка `tenant_id`.
- `tenant_id` **всегда** приходит из JWT, а не из тела запроса.
- Уникальность составная: например, `(tenant_id, code)` для сотрудников, отделов, станций.
- Пользователь видит только данные своего тенанта.

Детали — в [ADR-0002](./adr/0002-multi-tenancy.md).

## Основные потоки

### Регистрация клиента и вход
1. Клиент регистрируется → `user-service` создаёт `Tenant` + первого `User` (admin тенанта).
2. SPA → `POST /api/auth/login` через Gateway.
3. `user-service` валидирует, выдаёт JWT (access + refresh). В токене — `userId`, `tenantId`, `role`.
4. Gateway проверяет JWT на каждом запросе (`JwtAuthenticationFilter`) и добавляет `X-Tenant-Id` во внутренний запрос.
5. SPA хранит токен, шлёт в заголовке `Authorization: Bearer ...`.

### Расстановка сотрудников по станциям (план)
1. SPA запрашивает список сотрудников (`employee-service`) и станций (`workstation`) — только своего тенанта.
2. Менеджер делает назначения.
3. Назначения сохраняются в `work-calendar` (`Schedule` / `EquipmentSchedule`) с `tenant_id`.
4. `work-calendar` рассчитывает часы за месяц и ночные часы.

## Принципы

- Каждый сервис — своя БД (database-per-service).
- Мультитенантность: shared schema + `tenant_id` (см. ADR-0002).
- Общение сервисов через REST (синхронно), позже — возможны события.
- JWT — единственный источник идентичности и `tenantId`.
- Миграции — только через Liquibase, версионируются в `src/main/resources/db/changelog/`.
- Фронт не знает о внутренних адресах сервисов — только Gateway.

## Связанные документы

- [ADR](./adr/) — принятые решения
- [PROJECT.md](../PROJECT.md) — общее описание
- [README.md](../README.md) — быстрый старт
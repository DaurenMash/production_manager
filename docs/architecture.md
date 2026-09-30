# Архитектура ProdMan

## Уровень 1. Контекст (C4 Context)

```mermaid
flowchart LR
    Manager([Менеджер производства])
    Admin([Администратор])
    ProdMan[ProdMan<br/>Веб-приложение]
    DB[(PostgreSQL)]
    Redis[(Redis)]

    Manager -->|расстановка, отчёты| ProdMan
    Admin -->|управление справочниками| ProdMan
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
        GW[API Gateway<br/>prodman-api-gateway]
        US[user-service]
        ES[employee-service]
        WC[work-calendar]
        TS[test-service]
        WS[workstation]
    end

    subgraph Data["Хранилища"]
        DBU[(prodman_users)]
        DBE[(prodman_employees)]
        DBW[(prodman_calendar)]
        DBT[(prodman_tests)]
        DBX[(prodman_workstations)]
        RDS[(Redis)]
    end

    Browser -->|HTTP/JSON| SPA
    SPA -->|REST через Gateway| GW

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

## Уровень 3. Компоненты (пример: employee-service)

```mermaid
flowchart LR
    subgraph ES["employee-service"]
        EController[Controllers<br/>Employee / Department / Position]
        EService[Services<br/>EmployeeService / DepartmentService / PositionService]
        ERepo[Repositories<br/>Spring Data JPA]
        EMapper[DTO / Mapping]
    end

    EController --> EService
    EService --> ERepo
    EService --> EMapper
    ERepo --> PG[(PostgreSQL<br/>prodman_employees)]
```

## Основные потоки

### Аутентификация
1. SPA → `POST /api/auth/login` через Gateway.
2. `user-service` валидирует, выдаёт JWT (access + refresh).
3. Gateway проверяет JWT на каждом запросе (`JwtAuthenticationFilter`).
4. SPA хранит токен, шлёт в заголовке `Authorization: Bearer ...`.

### Расстановка сотрудников по станциям (план)
1. SPA запрашивает список сотрудников (`employee-service`) и станций (`workstation`).
2. Менеджер делает назначения.
3. Назначения сохраняются в `work-calendar` (Schedule / EquipmentSchedule).
4. `work-calendar` рассчитывает часы за месяц и ночные часы.

## Принципы

- Каждый сервис — своя БД (database-per-service).
- Общение сервисов через REST (синхронно), позже — возможны события.
- JWT — единственный источник идентичности.
- Миграции — только через Liquibase, версионируются в `src/main/resources/db/changelog/`.
- Фронт не знает о внутренних адресах сервисов — только Gateway.

## Связанные документы

- [ADR](./adr/) — принятые решения
- [PROJECT.md](../PROJECT.md) — общее описание
- [README.md](../README.md) — быстрый старт
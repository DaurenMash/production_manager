# ADR-0003. Соглашения по JPA-сущностям

Дата: 2026-10-01
Статус: Принято

## Контекст

В проекте много JPA-сущностей: `User`, `Tenant`, `UserRole`, `Employee`, `Department`, `Position`, `Qualification`, `EmployeeQualification`, `Workstation`, `WorkstationQualification`, `ShiftPattern`, `WorkstationShift`, `ShiftSlot`, `WorkstationDayStatus`. Изначально использовалась аннотация Lombok `@Data`, которая генерирует `@Getter + @Setter + @ToString + @EqualsAndHashCode + @RequiredArgsConstructor` по всем полям.

Это приводило к проблемам:

1. **`equals`/`hashCode` по всем полям:**
   - `id` может быть `null` до сохранения. Объект, положенный в `HashSet`/`HashMap` до save, после save «теряется» (hashCode меняется).
   - LAZY-связи триггерят загрузку при вызове `equals`/`hashCode` — лишние SQL и `LazyInitializationException` вне транзакции.
   - Двусторонние связи (`Parent → Child → Parent`) дают бесконечную рекурсию.

2. **`toString` по всем полям:**
   - Та же беда с LAZY-связями.
   - Случайный лог может вытащить из БД целый граф.
   - Пароли и токены попадают в логи.

3. **`@RequiredArgsConstructor` и `@AllArgsConstructor`** — для Hibernate нужен **пустой** конструктор, остальные не обязательны и могут мешать рефлексии.

## Решение

**Правила для JPA-сущностей:**

1. **Не использовать `@Data`.**
2. Использовать:
   - `@Getter`
   - `@Setter`
   - `@NoArgsConstructor`
   - `@AllArgsConstructor` (для `@Builder`)
   - `@Builder`
3. **`@ToString(onlyExplicitlyIncluded = true)`** + `@ToString.Include` **только** на безопасных полях (id, code, name). Никогда — на паролях, коллекциях, LAZY-связях.
4. **Не генерировать `equals`/`hashCode`** через Lombok. По умолчанию — identity-сравнение (по ссылке), что корректно для JPA.
5. Если `equals`/`hashCode` действительно нужны (например, сущность кладётся в `Set`):

```java
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof MyEntity)) return false;
    MyEntity other = (MyEntity) o;
    return id != null && id.equals(other.id);
}

@Override
public int hashCode() {
    return getClass().hashCode();
}
```

## Пример: `User` (актуальная версия)

```java
@Data        // ❌ НЕ используем
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User implements UserDetails {
    // ...
}
```

Правильно:

```java
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", updatable = false, nullable = false)
    @ToString.Include
    private UUID id;

    @Column(name = "username", nullable = false)
    @ToString.Include
    private String username;

    // password — НЕ включаем в toString
    @Column(name = "password", nullable = false)
    private String password;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<UserRole> tempRoles = new HashSet<>();
    // ...
}
```

## Специфичные правила для новых сущностей

### `tenant_id` — всегда `@Column(name = "tenant_id", nullable = false, updatable = false)`
- Значение приходит из `TenantContext` в `@PrePersist` или ставится сервисом вручную.
- Никогда не принимается из тела запроса.

### `created_at`, `updated_at` — через `@PrePersist` / `@PreUpdate`
- `created_at` — `updatable = false`.
- `updated_at` — обновляется на `@PreUpdate`.

### Аудит создания (`created_by`, `created_by_role`)
- Только для `User` (и, возможно, `Tenant`).
- `created_by` — `UUID` (nullable, null = системный/сидовый пользователь).
- `created_by_role` — `@Enumerated(EnumType.STRING)`, `length = 32`.

### Временные роли (`UserRole`)
- Отдельная сущность, `@ManyToOne` на `User`.
- `valid_from` (not null), `valid_until` (nullable = бессрочно).
- `isActiveNow()` — вычисляемое (не поле БД).

### Enum-поля (`Role`, `TenantStatus`, `TenantPlan`)
- `@Enumerated(EnumType.STRING)`.
- `@Column(nullable = false, length = 32)`.

### UUID
- `@UuidGenerator(style = UuidGenerator.Style.TIME)` (UUID v7, сортируемый).
- `updatable = false, nullable = false`.

## Связанные документы

- [ADR-0002](./0002-multi-tenancy.md) — мультитенантность
- [docs/db/*.dbml](../db/) — схемы БД
- [PROJECT.md](../../PROJECT.md) — раздел «Соглашения»
```

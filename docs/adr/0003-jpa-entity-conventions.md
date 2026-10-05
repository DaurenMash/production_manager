# ADR-0003. Соглашения по JPA-сущностям

Дата: 2026-10-01
Статус: Принято

## Контекст

В проекте много JPA-сущностей (`User`, `Tenant`, `Employee`, `Department`, `Position`, `Qualification`, `EmployeeQualification`, `Shift`, `ShiftConfig`). Изначально использовалась аннотация Lombok `@Data`, которая генерирует `@Getter + @Setter + @ToString + @EqualsAndHashCode + @RequiredArgsConstructor` по всем полям.

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
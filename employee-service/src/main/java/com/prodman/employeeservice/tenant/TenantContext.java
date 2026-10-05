package com.prodman.employeeservice.tenant;

import java.util.UUID;

/**
 * Хранит tenantId текущего HTTP-запроса в ThreadLocal.
 * Заполняется TenantFilter, читается сервисами/репозиториями.
 * Всегда очищается после запроса (см. TenantFilter).
 */
public final class TenantContext {

    private static final ThreadLocal<UUID> CURRENT = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void set(UUID tenantId) {
        CURRENT.set(tenantId);
    }

    public static UUID get() {
        return CURRENT.get();
    }

    /**
     * Возвращает tenantId или бросает исключение, если контекст пуст.
     * Используется в сервисах: без tenantId работать нельзя.
     */
    public static UUID require() {
        UUID tenantId = CURRENT.get();
        if (tenantId == null) {
            throw new IllegalStateException(
                    "tenantId отсутствует в контексте запроса. " +
                    "Проверь TenantFilter и заголовок X-Tenant-Id.");
        }
        return tenantId;
    }

    public static void clear() {
        CURRENT.remove();
    }
}
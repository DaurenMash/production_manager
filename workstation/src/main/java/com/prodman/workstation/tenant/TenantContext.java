package com.prodman.workstation.tenant;

import java.util.UUID;

public final class TenantContext {

    private static final ThreadLocal<UUID> CURRENT = new ThreadLocal<>();

    private TenantContext() {}

    public static void set(UUID tenantId) { CURRENT.set(tenantId); }
    public static UUID get() { return CURRENT.get(); }

    public static UUID require() {
        UUID tenantId = CURRENT.get();
        if (tenantId == null) {
            throw new IllegalStateException(
                    "tenantId отсутствует в контексте запроса. Проверь TenantFilter и заголовок X-Tenant-Id.");
        }
        return tenantId;
    }

    public static void clear() { CURRENT.remove(); }
}
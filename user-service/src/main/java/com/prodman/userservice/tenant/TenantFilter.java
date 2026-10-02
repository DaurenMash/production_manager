package com.prodman.userservice.tenant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Читает заголовок X-Tenant-Id (его проставляет API Gateway после валидации JWT)
 * и кладёт значение в TenantContext. Всегда очищает контекст в finally.
 *
 * Если заголовка нет — работаем без tenantId (например, health-check, swagger,
 * а также login/register, где tenantId ещё не установлен).
 * Сервисы, которым tenantId обязателен, вызовут TenantContext.require().
 */
@Slf4j
@Component
@Order(1)
public class TenantFilter extends OncePerRequestFilter {

    public static final String TENANT_HEADER = "X-Tenant-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        String raw = request.getHeader(TENANT_HEADER);
        try {
            if (raw != null && !raw.isBlank()) {
                try {
                    UUID tenantId = UUID.fromString(raw.trim());
                    TenantContext.set(tenantId);
                } catch (IllegalArgumentException ex) {
                    log.warn("Некорректный заголовок {} = '{}'", TENANT_HEADER, raw);
                }
            }
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
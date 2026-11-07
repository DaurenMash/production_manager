package com.prodman.userservice.security;

import com.prodman.userservice.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Читает JWT из Authorization: Bearer ... и кладёт аутентификацию в SecurityContext.
 * Authorities = "ROLE_" + role для каждой роли из claim "roles".
 * userId кладётся в details — его можно достать через JwtAuthenticationFilter.currentUserId().
 *
 * TenantContext заполняется отдельно TenantFilter'ом из заголовка X-Tenant-Id,
 * чтобы не было двух источников правды.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader(AUTH_HEADER);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(BEARER_PREFIX.length()).trim();
        if (!jwtService.isTokenValid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String username = jwtService.extractUsername(token);
            UUID userId = jwtService.extractUserId(token);
            List<String> roles = jwtService.extractRoles(token);

            List<GrantedAuthority> authorities = (roles == null ? List.<String>of() : roles)
                    .stream()
                    .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                    .map(GrantedAuthority.class::cast)
                    .toList();

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);
            // userId в details — доступен в UserService для проверки «кто создал» и «нельзя удалить себя»
            auth.setDetails(userId);
            SecurityContextHolder.getContext().setAuthentication(auth);

            log.debug("JWT auth: user={}, userId={}, roles={}", username, userId, roles);
        } catch (Exception e) {
            log.warn("JWT parse failed: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Достаёт userId текущего запроса из SecurityContext.
     * Возвращает null, если запрос не аутентифицирован или details не UUID
     * (например, старая реализация без userId).
     */
    public static UUID currentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        Object details = auth.getDetails();
        return details instanceof UUID u ? u : null;
    }
}
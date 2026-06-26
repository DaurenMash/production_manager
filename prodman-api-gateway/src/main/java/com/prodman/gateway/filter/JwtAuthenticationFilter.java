package com.prodman.gateway.filter;

import com.prodman.gateway.service.JwtService;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
public class JwtAuthenticationFilter implements GatewayFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    /**
     * ДОПОЛНИТЕЛЬНЫЕ ПРОВЕРКИ JWT:
     * 
     * 1. Проверка ролей для конкретных маршрутов
     * 2. Проверка IP адреса (если привязан к токену)
     * 3. Проверка времени жизни refresh token
     * 4. Инвалидация токенов через Redis (blacklist)
     * 5. Обновление токенов прозрачно для клиента
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        
        // Пропускаем публичные маршруты
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }
        
        // Извлекаем токен из заголовка
        String authHeader = request.getHeaders().getFirst("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorizedResponse(exchange, "Missing or invalid Authorization header");
        }
        
        String token = authHeader.substring(7);
        
        // Валидируем токен
        if (!jwtService.validateToken(token)) {
            return unauthorizedResponse(exchange, "Invalid or expired token");
        }
        
        // Извлекаем username и роль
        String username = jwtService.extractUsername(token);
        String role = jwtService.extractRole(token);
        
        // Добавляем информацию о пользователе в заголовки для downstream сервисов
        ServerHttpRequest mutatedRequest = request.mutate()
            .header("X-User-Id", username)
            .header("X-User-Role", role)
            .build();
        
        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/api/v1/auth/") ||
                path.startsWith("/api/v1/demo/") ||
                path.startsWith("/actuator/health") ||
                path.startsWith("/swagger-ui/") ||
                path.startsWith("/v3/api-docs/") ||
                path.startsWith("/webjars/") ||
                path.contains("/v3/api-docs");
    }
    
    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        byte[] bytes = String.format("{\"error\": \"%s\"}", message).getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }
}
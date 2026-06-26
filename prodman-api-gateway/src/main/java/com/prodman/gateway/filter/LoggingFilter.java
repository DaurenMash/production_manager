package com.prodman.gateway.filter;

import com.prodman.gateway.model.LogEntry;
import com.prodman.gateway.service.LoggingService;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    private final LoggingService loggingService;

    public LoggingFilter(LoggingService loggingService) {
        this.loggingService = loggingService;
    }

    /**
     * ДОПОЛНИТЕЛЬНЫЕ ВОЗМОЖНОСТИ ЛОГГИРОВАНИЯ:
     * 
     * 1. Логирование тела запроса/ответа для отладки
     * 2. Агрегация метрик в Prometheus
     * 3. Отправка критических ошибок в Telegram
     * 4. Ротация логов по размеру/времени
     * 5. Маскирование чувствительных данных (пароли, токены)
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        long startTime = System.currentTimeMillis();
        
        LogEntry logEntry = LogEntry.builder()
            .timestamp(LocalDateTime.now())
            .method(exchange.getRequest().getMethod().name())
            .path(exchange.getRequest().getURI().getPath())
            .queryParams(exchange.getRequest().getURI().getQuery())
            .clientIp(getClientIp(exchange))
            .build();
        
        return chain.filter(exchange)
            .doFinally(signalType -> {
                logEntry.setResponseTime(System.currentTimeMillis() - startTime);
                logEntry.setStatusCode(exchange.getResponse().getStatusCode() != null 
                    ? exchange.getResponse().getStatusCode().value() : 0);
                loggingService.saveLog(logEntry);
            });
    }
    
    private String getClientIp(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddress() != null 
            ? request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
    }
    
    @Override
    public int getOrder() {
        return -1;
    }
}
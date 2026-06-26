package com.prodman.gateway.filter;

import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimitFilter {

    /**
     * ДОПОЛНИТЕЛЬНЫЕ НАСТРОЙКИ RATE LIMITING:
     * 
     * 1. Разные лимиты для разных типов пользователей:
     *    - ADMIN: 1000 запросов/минуту
     *    - OPERATOR: 100 запросов/минуту
     *    - VISITOR: 10 запросов/минуту
     * 
     * 2. Лимиты для конкретных эндпоинтов:
     *    - /api/v1/auth/login: 5 запросов/минуту (защита от брутфорса)
     *    - /api/v1/analytics/** : 20 запросов/минуту
     *    - /api/v1/equipment/** : 200 запросов/минуту
     * 
     * 3. Whitelist IP (внутренние сервисы без лимитов)
     */
    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(10, 20, 1);  // replenishRate, burstCapacity, requestedTokens
    }
}
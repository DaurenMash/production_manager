package com.prodman.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    /**
     * ДОПОЛНИТЕЛЬНЫЕ ВОЗМОЖНОСТИ REDIS:
     * 
     * 1. Rate Limiting:
     *    - Хранение счетчиков запросов для rate limiting
     *    - Настройка разных лимитов для разных API
     * 
     * 2. Кэширование маршрутов:
     *    - Кэшировать информацию о доступных сервисах
     * 
     * 3. Хранение сессий:
     *    - Если нужны stateful сессии
     * 
     * 4. Блокировки (Distributed Locks):
     *    - Для предотвращения дублирующихся запросов
     */
    @Bean
    public ReactiveRedisTemplate<String, Object> reactiveRedisTemplate(
            ReactiveRedisConnectionFactory connectionFactory) {
        
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
        
        RedisSerializationContext<String, Object> serializationContext = RedisSerializationContext
            .<String, Object>newSerializationContext(new StringRedisSerializer())
            .value(serializer)
            .build();
        
        return new ReactiveRedisTemplate<>(connectionFactory, serializationContext);
    }
}
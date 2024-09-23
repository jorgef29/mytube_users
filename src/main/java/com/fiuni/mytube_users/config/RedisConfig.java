package com.fiuni.mytube_users.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.cache.RedisCacheManager;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        // Configuración por defecto con TTL global (por ejemplo, 24 horas)
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofDays(1))
                .disableCachingNullValues();

        // TTL de 10 minutos para el cache my_tube_users
        RedisCacheConfiguration userCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues();

        // TTL de 20 segundos para el cache my_tube_subscriptions
        RedisCacheConfiguration subscriptionCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(20))
                .disableCachingNullValues();

        // TTL de 5 minutos para el cache my_tube_user_subscriptions_user
        RedisCacheConfiguration userSubscriptionsCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))
                .disableCachingNullValues();

        // Mapa para definir las configuraciones de cada cache
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put("my_tube_users", userCacheConfig);
        cacheConfigurations.put("my_tube_subscriptions", subscriptionCacheConfig);
        cacheConfigurations.put("my_tube_subscriptions_user", userSubscriptionsCacheConfig);

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultCacheConfig) // Configuración por defecto
                .withInitialCacheConfigurations(cacheConfigurations) // Configuraciones personalizadas
                .build();
    }
}

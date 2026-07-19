package com.example.taskone.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Cache configuration class enabling declarative caching (@EnableCaching).
 * Configures Redis cache parameters, standard JSON serializers, and Time-To-Live (TTL).
 * Also registers an in-memory CacheManager for test environments.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${app.cache.ttl-minutes:10}")
    private int ttlMinutes;

    /**
     * Configures the Redis CacheManager.
     * Uses String serialization for keys and Generic Jackson JSON serialization for values.
     * This bean is only registered when spring.cache.type is not overridden to simple/none in tests.
     */
    @Bean
    @ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis", matchIfMissing = true)
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Configure JSON serializer for generic objects
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();

        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(ttlMinutes))
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(cacheConfig)
                .build();
    }

    /**
     * Configures an in-memory CacheManager using ConcurrentHashMap.
     * Used for testing/local environments when Redis is not available.
     */
    @Bean
    @ConditionalOnProperty(name = "spring.cache.type", havingValue = "simple")
    public CacheManager simpleCacheManager() {
        return new ConcurrentMapCacheManager();
    }
}

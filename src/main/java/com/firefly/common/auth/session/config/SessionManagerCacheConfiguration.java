/*
 * Copyright 2025 Firefly Software Solutions Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.firefly.common.auth.session.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Cache configuration for Session Manager
 * Supports either Caffeine (in-memory) OR Redis (distributed) caching
 */
@Configuration
@EnableCaching
@Slf4j
public class SessionManagerCacheConfiguration {

    public static final String SESSION_CACHE_MANAGER = "sessionCacheManager";

    // Cache names
    public static final String SESSION_CONTEXT_CACHE = "sessionContext";
    public static final String CUSTOMER_PROFILE_CACHE = "customerProfile";
    public static final String CONTRACT_CACHE = "contractCache";
    public static final String PRODUCT_CACHE = "productCache";
    public static final String PRODUCT_RESOURCE_CACHE = "productResourceCache";

    /**
     * Caffeine cache manager - in-memory caching
     */
    @Bean(SESSION_CACHE_MANAGER)
    @Primary
    @ConditionalOnProperty(name = "firefly.session-manager.cache.type", havingValue = "caffeine", matchIfMissing = true)
    public CacheManager caffeineCacheManager(SessionManagerCacheProperties properties) {
        log.info("Configuring Session Manager Caffeine Cache with properties: {}", properties);

        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(properties.getCaffeine().getMaximumSize())
                .expireAfterWrite(properties.getCaffeine().getExpireAfterWrite(), TimeUnit.MINUTES)
                .expireAfterAccess(properties.getCaffeine().getExpireAfterAccess(), TimeUnit.MINUTES)
                .recordStats());

        cacheManager.setCacheNames(Arrays.asList(
                SESSION_CONTEXT_CACHE,
                CUSTOMER_PROFILE_CACHE,
                CONTRACT_CACHE,
                PRODUCT_CACHE,
                PRODUCT_RESOURCE_CACHE
        ));

        return cacheManager;
    }

    /**
     * Redis cache manager - distributed caching
     */
    @Bean(SESSION_CACHE_MANAGER)
    @ConditionalOnProperty(name = "firefly.session-manager.cache.type", havingValue = "redis")
    public CacheManager redisCacheManager(
            RedisConnectionFactory redisConnectionFactory,
            SessionManagerCacheProperties properties) {

        log.info("Configuring Session Manager Redis Cache with properties: {}", properties);

        RedisCacheConfiguration cacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(properties.getRedis().getTtlMinutes()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues()
                .prefixCacheNameWith(properties.getRedis().getKeyPrefix());

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(cacheConfiguration)
                .build();
    }
}

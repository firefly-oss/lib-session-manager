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

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Tests for SessionManagerCacheConfiguration
 */
class SessionManagerCacheConfigurationTest {

    private final SessionManagerCacheConfiguration cacheConfiguration = new SessionManagerCacheConfiguration();
    private final SessionManagerCacheProperties properties = new SessionManagerCacheProperties();

    @Test
    void shouldCreateCaffeineCacheManager() {
        // Given
        properties.setType("caffeine");
        properties.getCaffeine().setMaximumSize(5000);
        properties.getCaffeine().setExpireAfterWrite(20);
        properties.getCaffeine().setExpireAfterAccess(10);

        // When
        CacheManager cacheManager = cacheConfiguration.caffeineCacheManager(properties);

        // Then
        assertThat(cacheManager).isInstanceOf(CaffeineCacheManager.class);
        CaffeineCacheManager caffeineCacheManager = (CaffeineCacheManager) cacheManager;
        assertThat(caffeineCacheManager.getCacheNames()).containsExactlyInAnyOrder(
                SessionManagerCacheConfiguration.SESSION_CONTEXT_CACHE,
                SessionManagerCacheConfiguration.CUSTOMER_PROFILE_CACHE,
                SessionManagerCacheConfiguration.CONTRACT_CACHE,
                SessionManagerCacheConfiguration.PRODUCT_CACHE,
                SessionManagerCacheConfiguration.PRODUCT_RESOURCE_CACHE
        );
    }

    @Test
    void shouldCreateRedisCacheManager() {
        // Given
        RedisConnectionFactory redisConnectionFactory = mock(RedisConnectionFactory.class);
        properties.setType("redis");
        properties.getRedis().setTtlMinutes(120);
        properties.getRedis().setKeyPrefix("test:session:");

        // When
        CacheManager cacheManager = cacheConfiguration.redisCacheManager(redisConnectionFactory, properties);

        // Then
        assertThat(cacheManager).isInstanceOf(RedisCacheManager.class);
    }

    @Test
    void shouldUseDefaultCaffeineProperties() {
        // Given - default properties
        SessionManagerCacheProperties defaultProperties = new SessionManagerCacheProperties();

        // When
        CacheManager cacheManager = cacheConfiguration.caffeineCacheManager(defaultProperties);

        // Then
        assertThat(cacheManager).isInstanceOf(CaffeineCacheManager.class);
        assertThat(defaultProperties.getType()).isEqualTo("caffeine");
        assertThat(defaultProperties.getCaffeine().getMaximumSize()).isEqualTo(10000);
        assertThat(defaultProperties.getCaffeine().getExpireAfterWrite()).isEqualTo(30);
        assertThat(defaultProperties.getCaffeine().getExpireAfterAccess()).isEqualTo(15);
    }

    @Test
    void shouldUseDefaultRedisProperties() {
        // Given - default properties
        SessionManagerCacheProperties defaultProperties = new SessionManagerCacheProperties();

        // Then
        assertThat(defaultProperties.getRedis().getTtlMinutes()).isEqualTo(60);
        assertThat(defaultProperties.getRedis().getKeyPrefix()).isEqualTo("firefly:session:");
    }

    @SpringBootTest(classes = {
            SessionManagerCacheConfiguration.class,
            SessionManagerCacheProperties.class
    })
    @TestPropertySource(properties = {
            "firefly.session-manager.cache.type=caffeine",
            "firefly.session-manager.cache.caffeine.maximum-size=5000",
            "firefly.session-manager.cache.caffeine.expire-after-write=20",
            "firefly.session-manager.cache.caffeine.expire-after-access=10"
    })
    static class CaffeineConfigurationIntegrationTest {

        @Test
        void shouldLoadCaffeineConfiguration() {
            // This test verifies that the configuration loads properly with caffeine properties
            // The actual bean creation is tested in the unit tests above
        }
    }

    @SpringBootTest(classes = {
            SessionManagerCacheConfiguration.class,
            SessionManagerCacheProperties.class,
            RedisConfigurationIntegrationTest.TestRedisConfiguration.class
    })
    @TestPropertySource(properties = {
            "firefly.session-manager.cache.type=redis",
            "firefly.session-manager.cache.redis.ttl-minutes=120",
            "firefly.session-manager.cache.redis.key-prefix=test:session:"
    })
    static class RedisConfigurationIntegrationTest {

        @Test
        void shouldLoadRedisConfiguration() {
            // This test verifies that the configuration loads properly with redis properties
            // The actual bean creation is tested in the unit tests above
        }

        @TestConfiguration
        static class TestRedisConfiguration {

            @Bean
            @Primary
            public RedisConnectionFactory redisConnectionFactory() {
                // Mock Redis connection factory for testing
                return mock(RedisConnectionFactory.class);
            }
        }
    }
}

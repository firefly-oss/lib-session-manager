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

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for Session Manager caching
 */
@Configuration
@ConfigurationProperties(prefix = "firefly.session-manager.cache")
@Data
public class SessionManagerCacheProperties {

    /**
     * Cache type: caffeine or redis
     */
    private String type = "caffeine";

    /**
     * Caffeine cache properties
     */
    private CaffeineProperties caffeine = new CaffeineProperties();

    /**
     * Redis cache properties
     */
    private RedisProperties redis = new RedisProperties();

    @Data
    public static class CaffeineProperties {
        /**
         * Maximum number of entries in cache
         */
        private long maximumSize = 10000;

        /**
         * Expire after write time in minutes
         */
        private long expireAfterWrite = 30;

        /**
         * Expire after access time in minutes
         */
        private long expireAfterAccess = 15;
    }

    @Data
    public static class RedisProperties {
        /**
         * TTL for cache entries in minutes
         */
        private long ttlMinutes = 60;

        /**
         * Key prefix for cache
         */
        private String keyPrefix = "firefly:session:";

        /**
         * Redis connection configuration
         */
        private ConnectionProperties connection = new ConnectionProperties();

        @Data
        public static class ConnectionProperties {
            /**
             * Redis server host
             */
            private String host = "localhost";

            /**
             * Redis server port
             */
            private int port = 6379;

            /**
             * Redis database index
             */
            private int database = 0;

            /**
             * Redis password (optional)
             */
            private String password;

            /**
             * Redis username (optional, for Redis 6+)
             */
            private String username;

            /**
             * Connection timeout in milliseconds
             */
            private int timeout = 2000;

            /**
             * Enable SSL connection
             */
            private boolean ssl = false;
        }
    }
}

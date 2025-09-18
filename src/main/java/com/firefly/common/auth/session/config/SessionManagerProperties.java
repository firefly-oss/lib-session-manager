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
 * Configuration properties for Session Manager
 */
@Configuration
@ConfigurationProperties(prefix = "firefly.session-manager")
@Data
public class SessionManagerProperties {

    /**
     * Enable/disable session manager
     */
    private boolean enabled = true;

    /**
     * Session timeout in minutes
     */
    private int sessionTimeoutMinutes = 30;

    /**
     * Maximum number of concurrent sessions per user
     */
    private int maxConcurrentSessions = 5;

    /**
     * Enable session validation
     */
    private boolean sessionValidationEnabled = true;

    /**
     * Session cleanup configuration
     */
    private CleanupProperties cleanup = new CleanupProperties();

    /**
     * Security configuration
     */
    private SecurityProperties security = new SecurityProperties();

    @Data
    public static class CleanupProperties {
        /**
         * Enable automatic session cleanup
         */
        private boolean enabled = true;

        /**
         * Cleanup interval in minutes
         */
        private int intervalMinutes = 10;

        /**
         * Grace period for expired sessions in minutes
         */
        private int gracePeriodMinutes = 5;
    }

    @Data
    public static class SecurityProperties {
        /**
         * Enable IP address validation
         */
        private boolean ipValidationEnabled = false;

        /**
         * Enable user agent validation
         */
        private boolean userAgentValidationEnabled = false;

        /**
         * Maximum session idle time in minutes
         */
        private int maxIdleTimeMinutes = 60;

        /**
         * Enable session hijacking protection
         */
        private boolean hijackingProtectionEnabled = true;
    }
}

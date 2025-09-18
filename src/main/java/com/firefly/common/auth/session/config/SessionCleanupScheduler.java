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

import com.firefly.common.auth.session.core.DefaultSessionContextExtractor;
import com.firefly.common.auth.session.core.SessionContextExtractor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled task for cleaning up expired sessions
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SessionCleanupScheduler {

    private final SessionContextExtractor sessionContextExtractor;

    /**
     * Cleanup expired sessions every 10 minutes
     */
    @Scheduled(fixedRateString = "${firefly.session-manager.cleanup.interval-minutes:10}000")
    public void cleanupExpiredSessions() {
        log.debug("Starting scheduled session cleanup");
        
        try {
            if (sessionContextExtractor instanceof DefaultSessionContextExtractor) {
                DefaultSessionContextExtractor extractor = (DefaultSessionContextExtractor) sessionContextExtractor;
                int beforeCount = extractor.getActiveSessionCount();
                
                extractor.cleanupExpiredSessions();
                
                int afterCount = extractor.getActiveSessionCount();
                int cleanedUp = beforeCount - afterCount;
                
                if (cleanedUp > 0) {
                    log.info("Cleaned up {} expired sessions. Active sessions: {}", cleanedUp, afterCount);
                } else {
                    log.debug("No expired sessions to clean up. Active sessions: {}", afterCount);
                }
            }
        } catch (Exception e) {
            log.error("Error during session cleanup", e);
        }
    }
}

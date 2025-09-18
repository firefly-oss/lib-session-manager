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

package com.firefly.common.auth.session.core;

import com.firefly.common.auth.session.models.SessionContext;
import com.firefly.common.auth.session.models.SessionStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Default implementation of SessionContextExtractor
 * Uses reactive context and in-memory storage for session management
 */
@Component
@Slf4j
public class DefaultSessionContextExtractor implements SessionContextExtractor {

    private static final String SESSION_CONTEXT_KEY = "SESSION_CONTEXT";
    private final ConcurrentMap<String, SessionContext> sessionStore = new ConcurrentHashMap<>();

    @Override
    public Mono<SessionContext> getCurrentSessionContext() {
        return Mono.deferContextual(contextView -> {
            if (contextView.hasKey(SESSION_CONTEXT_KEY)) {
                SessionContext sessionContext = contextView.get(SESSION_CONTEXT_KEY);
                log.debug("Retrieved session context from reactive context: {}", sessionContext.getSessionId());
                return Mono.just(sessionContext);
            } else {
                log.debug("No session context found in reactive context");
                return Mono.empty();
            }
        });
    }

    @Override
    public Mono<SessionContext> getSessionContext(String sessionId) {
        log.debug("Retrieving session context for session ID: {}", sessionId);
        
        SessionContext sessionContext = sessionStore.get(sessionId);
        if (sessionContext != null) {
            // Check if session is still valid
            if (isSessionExpired(sessionContext)) {
                log.info("Session expired, removing from store: {}", sessionId);
                sessionStore.remove(sessionId);
                return Mono.empty();
            }
            
            log.debug("Found session context for session ID: {}", sessionId);
            return Mono.just(sessionContext);
        } else {
            log.debug("No session context found for session ID: {}", sessionId);
            return Mono.empty();
        }
    }

    @Override
    public Mono<Void> storeSessionContext(SessionContext sessionContext) {
        log.debug("Storing session context: {}", sessionContext.getSessionId());
        
        sessionStore.put(sessionContext.getSessionId(), sessionContext);
        
        return Mono.deferContextual(contextView ->
            Mono.<Void>empty().contextWrite(Context.of(SESSION_CONTEXT_KEY, sessionContext))
        );
    }

    @Override
    public Mono<Void> invalidateSession(String sessionId) {
        log.info("Invalidating session: {}", sessionId);
        
        SessionContext sessionContext = sessionStore.get(sessionId);
        if (sessionContext != null) {
            // Mark session as invalidated
            SessionContext invalidatedSession = sessionContext.toBuilder()
                    .status(SessionStatus.INVALIDATED)
                    .build();
            
            sessionStore.put(sessionId, invalidatedSession);
            
            // Schedule removal after a delay to allow for cleanup
            Mono.delay(java.time.Duration.ofMinutes(5))
                    .doOnNext(ignored -> {
                        sessionStore.remove(sessionId);
                        log.debug("Removed invalidated session from store: {}", sessionId);
                    })
                    .subscribe();
        }
        
        return Mono.empty();
    }

    private boolean isSessionExpired(SessionContext sessionContext) {
        LocalDateTime now = LocalDateTime.now();
        return sessionContext.getExpiresAt().isBefore(now) || 
               SessionStatus.EXPIRED.equals(sessionContext.getStatus()) ||
               SessionStatus.INVALIDATED.equals(sessionContext.getStatus());
    }

    /**
     * Cleanup method to remove expired sessions
     * This should be called periodically by a scheduled task
     */
    public void cleanupExpiredSessions() {
        log.debug("Cleaning up expired sessions");
        
        sessionStore.entrySet().removeIf(entry -> {
            boolean expired = isSessionExpired(entry.getValue());
            if (expired) {
                log.debug("Removing expired session: {}", entry.getKey());
            }
            return expired;
        });
        
        log.debug("Session cleanup completed. Active sessions: {}", sessionStore.size());
    }

    /**
     * Get current session count for monitoring
     */
    public int getActiveSessionCount() {
        return sessionStore.size();
    }
}

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
import com.firefly.common.auth.session.models.SessionMetadata;
import com.firefly.common.auth.session.models.SessionStatus;
import com.firefly.common.auth.session.services.CustomerProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Core session manager for the Firefly Core Banking Platform.
 *
 * <p>The FireflySessionManager serves as the central orchestrator for customer session
 * management across all microservices in the platform. It provides a unified interface
 * for creating, retrieving, updating, and invalidating customer sessions while
 * aggregating customer context from multiple domain services.</p>
 *
 * <p><strong>Key Responsibilities:</strong></p>
 * <ul>
 *   <li>Session lifecycle management (create, retrieve, update, invalidate)</li>
 *   <li>Customer context aggregation from multiple microservices</li>
 *   <li>Contract and product relationship management</li>
 *   <li>Performance optimization through intelligent caching</li>
 *   <li>Security and audit trail maintenance</li>
 * </ul>
 *
 * <p><strong>Integration with lib-common-auth:</strong></p>
 * <p>This session manager is designed to work seamlessly with the lib-common-auth
 * library, providing the customer context needed for authorization decisions.
 * The session data can be used by {@code @RequiresOwnership} annotations and
 * custom access validators.</p>
 *
 * <p><strong>Usage Example:</strong></p>
 * <pre>{@code
 * @RestController
 * public class AccountController {
 *
 *     @Autowired
 *     private FireflySessionManager sessionManager;
 *
 *     @GetMapping("/accounts/{accountId}")
 *     @RequiresOwnership(resourceType = "product", resourceIdParam = "accountId")
 *     public Mono<AccountResponse> getAccount(@PathVariable String accountId,
 *                                           ServerWebExchange exchange) {
 *         return sessionManager.createOrGetSession(exchange)
 *             .flatMap(session -> processAccountRequest(accountId, session));
 *     }
 * }
 * }</pre>
 *
 * <p><strong>Caching Strategy:</strong></p>
 * <p>The session manager uses configurable caching (Caffeine or Redis) to optimize
 * performance and reduce load on downstream services. Cache keys are based on
 * party IDs and sessions are automatically evicted upon expiration or invalidation.</p>
 *
 * <p><strong>Thread Safety:</strong> This class is thread-safe and designed for
 * concurrent access in a reactive environment.</p>
 *
 * @author Firefly Team
 * @since 1.0.0
 * @see SessionContext
 * @see CustomerProfileService
 * @see SessionContextExtractor
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FireflySessionManager {

    private static final String X_PARTY_ID_HEADER = "X-Party-Id";
    private static final String SESSION_ID_HEADER = "X-Session-Id";
    private static final int DEFAULT_SESSION_TIMEOUT_MINUTES = 30;

    private final CustomerProfileService customerProfileService;
    private final SessionContextExtractor sessionContextExtractor;

    /**
     * Creates or retrieves a session based on X-Party-Id header
     *
     * @param exchange The server web exchange containing headers
     * @return Mono<SessionContext> containing the session context
     */
    public Mono<SessionContext> createOrGetSession(ServerWebExchange exchange) {
        String partyIdHeader = exchange.getRequest().getHeaders().getFirst(X_PARTY_ID_HEADER);
        String sessionIdHeader = exchange.getRequest().getHeaders().getFirst(SESSION_ID_HEADER);

        if (partyIdHeader == null) {
            return Mono.error(new IllegalArgumentException("X-Party-Id header is required"));
        }

        try {
            UUID partyId = UUID.fromString(partyIdHeader);
            String sessionId = sessionIdHeader != null ? sessionIdHeader : generateSessionId(partyId);

            return getOrCreateSession(sessionId, partyId, exchange);
        } catch (IllegalArgumentException e) {
            return Mono.error(new IllegalArgumentException("Invalid X-Party-Id format", e));
        }
    }

    /**
     * Retrieves current session context
     *
     * @return Mono<SessionContext> containing the current session
     */
    public Mono<SessionContext> getCurrentSession() {
        return sessionContextExtractor.getCurrentSessionContext();
    }

    /**
     * Retrieves session by session ID
     *
     * @param sessionId The session identifier
     * @return Mono<SessionContext> containing the session context
     */
    @Cacheable(value = "sessionContext", key = "#sessionId")
    public Mono<SessionContext> getSession(String sessionId) {
        log.debug("Retrieving session: {}", sessionId);
        return sessionContextExtractor.getSessionContext(sessionId);
    }

    /**
     * Invalidates a session
     *
     * @param sessionId The session identifier
     * @return Mono<Void> indicating completion
     */
    @CacheEvict(value = "sessionContext", key = "#sessionId")
    public Mono<Void> invalidateSession(String sessionId) {
        log.info("Invalidating session: {}", sessionId);
        return sessionContextExtractor.invalidateSession(sessionId);
    }

    /**
     * Refreshes session with updated customer data
     *
     * @param sessionId The session identifier
     * @return Mono<SessionContext> containing the refreshed session
     */
    @CacheEvict(value = "sessionContext", key = "#sessionId")
    public Mono<SessionContext> refreshSession(String sessionId) {
        log.debug("Refreshing session: {}", sessionId);

        return getSession(sessionId)
                .flatMap(session -> customerProfileService.refreshCustomerProfile(session.getPartyId())
                        .map(refreshedProfile -> session.toBuilder()
                                .customerProfile(refreshedProfile)
                                .lastAccessedAt(LocalDateTime.now())
                                .build()));
    }

    /**
     * Validates if session is active and not expired
     *
     * @param sessionContext The session context to validate
     * @return Mono<Boolean> indicating if session is valid
     */
    public Mono<Boolean> isSessionValid(SessionContext sessionContext) {
        if (sessionContext == null) {
            return Mono.just(false);
        }

        LocalDateTime now = LocalDateTime.now();
        boolean isNotExpired = sessionContext.getExpiresAt().isAfter(now);
        boolean isActive = SessionStatus.ACTIVE.equals(sessionContext.getStatus());

        return Mono.just(isNotExpired && isActive);
    }

    private Mono<SessionContext> getOrCreateSession(String sessionId, UUID partyId, ServerWebExchange exchange) {
        return getSession(sessionId)
                .cast(SessionContext.class)
                .switchIfEmpty(createNewSession(sessionId, partyId, exchange))
                .flatMap(session -> {
                    // Update last accessed time
                    SessionContext updatedSession = session.toBuilder()
                            .lastAccessedAt(LocalDateTime.now())
                            .build();

                    return Mono.just(updatedSession);
                });
    }

    private Mono<SessionContext> createNewSession(String sessionId, UUID partyId, ServerWebExchange exchange) {
        log.info("Creating new session for party ID: {}", partyId);

        return customerProfileService.getCustomerProfile(partyId)
                .map(customerProfile -> {
                    LocalDateTime now = LocalDateTime.now();

                    return SessionContext.builder()
                            .sessionId(sessionId)
                            .partyId(partyId)
                            .customerProfile(customerProfile)
                            .createdAt(now)
                            .lastAccessedAt(now)
                            .expiresAt(now.plusMinutes(DEFAULT_SESSION_TIMEOUT_MINUTES))
                            .ipAddress(getClientIpAddress(exchange))
                            .userAgent(getUserAgent(exchange))
                            .status(SessionStatus.ACTIVE)
                            .metadata(buildSessionMetadata(exchange))
                            .build();
                });
    }

    private String generateSessionId(UUID partyId) {
        return "session_" + partyId.toString() + "_" + System.currentTimeMillis();
    }

    private String getClientIpAddress(ServerWebExchange exchange) {
        String xForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = exchange.getRequest().getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
    }

    private String getUserAgent(ServerWebExchange exchange) {
        return exchange.getRequest().getHeaders().getFirst("User-Agent");
    }

    private SessionMetadata buildSessionMetadata(ServerWebExchange exchange) {
        return SessionMetadata.builder()
                .channel(determineChannel(exchange))
                .sourceApplication(exchange.getRequest().getHeaders().getFirst("X-Source-Application"))
                .deviceInfo(getUserAgent(exchange))

                .build();
    }

    private String determineChannel(ServerWebExchange exchange) {
        String userAgent = getUserAgent(exchange);
        if (userAgent != null) {
            if (userAgent.contains("Mobile")) {
                return "mobile";
            } else if (userAgent.contains("Mozilla")) {
                return "web";
            }
        }
        return "api";
    }


}

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

package com.firefly.common.auth.session.filters;

import com.firefly.common.auth.session.core.FireflySessionManager;
import com.firefly.common.auth.session.core.SessionContextExtractor;
import com.firefly.common.auth.session.models.SessionContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Web filter for automatic session management
 * Automatically creates or retrieves sessions based on X-Party-Id header
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "firefly.session-manager.auto-filter.enabled", havingValue = "true", matchIfMissing = true)
public class SessionManagementWebFilter implements WebFilter, Ordered {

    private static final String X_PARTY_ID_HEADER = "X-Party-Id";
    private static final int FILTER_ORDER = -100; // Execute early in the filter chain

    private final FireflySessionManager sessionManager;
    private final SessionContextExtractor sessionContextExtractor;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String partyIdHeader = exchange.getRequest().getHeaders().getFirst(X_PARTY_ID_HEADER);
        
        // Skip session management if no X-Party-Id header is present
        if (partyIdHeader == null || partyIdHeader.trim().isEmpty()) {
            log.debug("No X-Party-Id header found, skipping session management");
            return chain.filter(exchange);
        }

        log.debug("Processing request with X-Party-Id: {}", partyIdHeader);

        return sessionManager.createOrGetSession(exchange)
                .flatMap(sessionContext -> {
                    log.debug("Session context created/retrieved: {}", sessionContext.getSessionId());
                    
                    // Store session context in reactive context
                    return sessionContextExtractor.storeSessionContext(sessionContext)
                            .then(chain.filter(exchange))
                            .contextWrite(ctx -> ctx.put("SESSION_CONTEXT", sessionContext));
                })
                .onErrorResume(error -> {
                    log.error("Error in session management filter", error);
                    // Continue with the request even if session management fails
                    return chain.filter(exchange);
                });
    }

    @Override
    public int getOrder() {
        return FILTER_ORDER;
    }
}

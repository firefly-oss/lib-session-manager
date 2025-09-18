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
import reactor.core.publisher.Mono;

/**
 * Interface for extracting session context from various sources
 */
public interface SessionContextExtractor {
    
    /**
     * Gets the current session context from the reactive context
     *
     * @return Mono<SessionContext> containing the current session
     */
    Mono<SessionContext> getCurrentSessionContext();
    
    /**
     * Gets session context by session ID
     *
     * @param sessionId The session identifier
     * @return Mono<SessionContext> containing the session context
     */
    Mono<SessionContext> getSessionContext(String sessionId);
    
    /**
     * Stores session context in the reactive context
     *
     * @param sessionContext The session context to store
     * @return Mono<Void> indicating completion
     */
    Mono<Void> storeSessionContext(SessionContext sessionContext);
    
    /**
     * Invalidates a session
     *
     * @param sessionId The session identifier
     * @return Mono<Void> indicating completion
     */
    Mono<Void> invalidateSession(String sessionId);
}

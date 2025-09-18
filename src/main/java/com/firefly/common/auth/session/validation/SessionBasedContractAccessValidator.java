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

package com.firefly.common.auth.session.validation;

import com.firefly.common.auth.annotation.AccessValidatorFor;
import com.firefly.common.auth.model.AuthInfo;
import com.firefly.common.auth.service.AccessValidator;
import com.firefly.common.auth.session.core.FireflySessionManager;
import com.firefly.common.auth.session.models.ActiveContract;
import com.firefly.common.auth.session.models.SessionContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Access validator for contract resources that uses the session manager
 * to validate if the current user has access to a specific contract.
 *
 * This validator integrates with the session manager to check if the party
 * in the current session has an active relationship with the requested contract.
 * Resources are contract IDs.
 */
@Component
@AccessValidatorFor("contract")
@RequiredArgsConstructor
@Slf4j
public class SessionBasedContractAccessValidator implements AccessValidator {

    private final FireflySessionManager sessionManager;

    @Override
    public String getResourceName() {
        return "contract";
    }

    @Override
    public Mono<Boolean> canAccess(String resourceId, AuthInfo authInfo) {
        log.debug("Validating contract access for resource ID: {} with auth info: {}", resourceId, authInfo);

        try {
            UUID contractId = UUID.fromString(resourceId);

            return sessionManager.getCurrentSession()
                    .cast(SessionContext.class)
                    .flatMap(sessionContext -> validateContractAccess(contractId, sessionContext, authInfo))
                    .defaultIfEmpty(false)
                    .doOnNext(hasAccess -> log.debug("Contract access validation result for {}: {}", resourceId, hasAccess))
                    .doOnError(error -> log.error("Error validating contract access for {}: {}", resourceId, error.getMessage()));

        } catch (IllegalArgumentException e) {
            log.warn("Invalid contract ID format: {}", resourceId);
            return Mono.just(false);
        }
    }

    private Mono<Boolean> validateContractAccess(UUID contractId, SessionContext sessionContext, AuthInfo authInfo) {
        // Check if user has employee role that bypasses ownership checks
        if (hasEmployeeRole(authInfo)) {
            log.debug("Bypassing contract access check for employee role: {}", authInfo.getRoles());
            return Mono.just(true);
        }

        // Check if the party in the session has access to this contract
        return hasContractAccess(contractId, sessionContext)
                .doOnNext(hasAccess -> {
                    if (hasAccess) {
                        log.debug("Party {} has access to contract {}", sessionContext.getPartyId(), contractId);
                    } else {
                        log.warn("Party {} does not have access to contract {}", sessionContext.getPartyId(), contractId);
                    }
                });
    }

    private Mono<Boolean> hasContractAccess(UUID contractId, SessionContext sessionContext) {
        // Check if any of the party's active contracts matches the requested contract
        return Mono.fromCallable(() -> {
            return sessionContext.getCustomerProfile()
                    .getActiveContracts()
                    .stream()
                    .filter(contract -> Boolean.TRUE.equals(contract.getIsActive()))
                    .anyMatch(contract -> contractId.equals(contract.getContractId()));
        })
        .onErrorReturn(false);
    }

    private boolean hasEmployeeRole(AuthInfo authInfo) {
        if (authInfo == null || authInfo.getRoles() == null) {
            return false;
        }

        // Check for employee roles that bypass ownership checks
        return authInfo.getRoles().stream()
                .anyMatch(role -> isEmployeeRole(role));
    }

    private boolean isEmployeeRole(String role) {
        return "ADMIN".equals(role) ||
               "CUSTOMER_SUPPORT".equals(role) ||
               "SUPERVISOR".equals(role) ||
               "MANAGER".equals(role) ||
               "BRANCH_STAFF".equals(role);
    }
}

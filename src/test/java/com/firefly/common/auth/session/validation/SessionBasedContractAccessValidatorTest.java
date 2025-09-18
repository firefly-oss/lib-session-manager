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

import com.firefly.common.auth.model.AuthInfo;
import com.firefly.common.auth.session.core.FireflySessionManager;
import com.firefly.common.auth.session.models.ActiveContract;
import com.firefly.common.auth.session.models.CustomerProfile;
import com.firefly.common.auth.session.models.SessionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.UUID;

import static org.mockito.Mockito.when;

/**
 * Tests for SessionBasedContractAccessValidator
 */
@ExtendWith(MockitoExtension.class)
class SessionBasedContractAccessValidatorTest {

    @Mock
    private FireflySessionManager sessionManager;

    @Mock
    private AuthInfo authInfo;

    private SessionBasedContractAccessValidator validator;

    private final UUID partyId = UUID.randomUUID();
    private final UUID contractId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        validator = new SessionBasedContractAccessValidator(sessionManager);
    }

    @Test
    void shouldReturnResourceName() {
        // When & Then
        assert validator.getResourceName().equals("contract");
    }

    @Test
    void shouldGrantAccessWhenEmployeeRole() {
        // Given
        when(authInfo.getRoles()).thenReturn(new HashSet<>(Arrays.asList("ADMIN", "USER")));
        when(sessionManager.getCurrentSession()).thenReturn(Mono.just(createSessionContext()));

        // When & Then
        StepVerifier.create(validator.canAccess(contractId.toString(), authInfo))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void shouldGrantAccessWhenContractFoundInActiveContracts() {
        // Given
        when(authInfo.getRoles()).thenReturn(new HashSet<>(Arrays.asList("CUSTOMER")));
        SessionContext sessionContext = createSessionContextWithContract(contractId);
        when(sessionManager.getCurrentSession()).thenReturn(Mono.just(sessionContext));

        // When & Then
        StepVerifier.create(validator.canAccess(contractId.toString(), authInfo))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void shouldDenyAccessWhenContractNotFoundInActiveContracts() {
        // Given
        when(authInfo.getRoles()).thenReturn(new HashSet<>(Arrays.asList("CUSTOMER")));
        UUID differentContractId = UUID.randomUUID();
        SessionContext sessionContext = createSessionContextWithContract(differentContractId);
        when(sessionManager.getCurrentSession()).thenReturn(Mono.just(sessionContext));

        // When & Then
        StepVerifier.create(validator.canAccess(contractId.toString(), authInfo))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void shouldDenyAccessWhenNoActiveContracts() {
        // Given
        when(authInfo.getRoles()).thenReturn(new HashSet<>(Arrays.asList("CUSTOMER")));
        SessionContext sessionContext = createSessionContextWithoutContracts();
        when(sessionManager.getCurrentSession()).thenReturn(Mono.just(sessionContext));

        // When & Then
        StepVerifier.create(validator.canAccess(contractId.toString(), authInfo))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void shouldDenyAccessWhenContractInactive() {
        // Given
        when(authInfo.getRoles()).thenReturn(new HashSet<>(Arrays.asList("CUSTOMER")));
        SessionContext sessionContext = createSessionContextWithInactiveContract(contractId);
        when(sessionManager.getCurrentSession()).thenReturn(Mono.just(sessionContext));

        // When & Then
        StepVerifier.create(validator.canAccess(contractId.toString(), authInfo))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void shouldReturnFalseForInvalidContractId() {
        // Given - no stubbing needed for invalid UUID case

        // When & Then
        StepVerifier.create(validator.canAccess("invalid-uuid", authInfo))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void shouldReturnFalseWhenSessionManagerReturnsEmpty() {
        // Given
        when(sessionManager.getCurrentSession()).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(validator.canAccess(contractId.toString(), authInfo))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void shouldGrantAccessForCustomerSupportRole() {
        // Given
        when(authInfo.getRoles()).thenReturn(new HashSet<>(Arrays.asList("CUSTOMER_SUPPORT")));
        when(sessionManager.getCurrentSession()).thenReturn(Mono.just(createSessionContext()));

        // When & Then
        StepVerifier.create(validator.canAccess(contractId.toString(), authInfo))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void shouldGrantAccessForManagerRole() {
        // Given
        when(authInfo.getRoles()).thenReturn(new HashSet<>(Arrays.asList("MANAGER")));
        when(sessionManager.getCurrentSession()).thenReturn(Mono.just(createSessionContext()));

        // When & Then
        StepVerifier.create(validator.canAccess(contractId.toString(), authInfo))
                .expectNext(true)
                .verifyComplete();
    }

    private SessionContext createSessionContext() {
        CustomerProfile customerProfile = CustomerProfile.builder()
                .partyId(partyId)
                .activeContracts(Collections.emptyList())
                .build();

        return SessionContext.builder()
                .partyId(partyId)
                .customerProfile(customerProfile)
                .build();
    }

    private SessionContext createSessionContextWithContract(UUID contractId) {
        ActiveContract activeContract = ActiveContract.builder()
                .contractId(contractId)
                .isActive(true)
                .roleName("PRIMARY_HOLDER")
                .build();

        CustomerProfile customerProfile = CustomerProfile.builder()
                .partyId(partyId)
                .activeContracts(Arrays.asList(activeContract))
                .build();

        return SessionContext.builder()
                .partyId(partyId)
                .customerProfile(customerProfile)
                .build();
    }

    private SessionContext createSessionContextWithoutContracts() {
        CustomerProfile customerProfile = CustomerProfile.builder()
                .partyId(partyId)
                .activeContracts(Collections.emptyList())
                .build();

        return SessionContext.builder()
                .partyId(partyId)
                .customerProfile(customerProfile)
                .build();
    }

    private SessionContext createSessionContextWithInactiveContract(UUID contractId) {
        ActiveContract inactiveContract = ActiveContract.builder()
                .contractId(contractId)
                .isActive(false) // Inactive contract
                .roleName("PRIMARY_HOLDER")
                .build();

        CustomerProfile customerProfile = CustomerProfile.builder()
                .partyId(partyId)
                .activeContracts(Arrays.asList(inactiveContract))
                .build();

        return SessionContext.builder()
                .partyId(partyId)
                .customerProfile(customerProfile)
                .build();
    }
}

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

package com.firefly.common.auth.session.services.impl;

import com.firefly.common.auth.session.models.ContractRole;
import com.firefly.common.auth.session.models.DefaultContractRoles;
import com.firefly.common.reference.master.data.sdk.api.ContractRoleApi;
import com.firefly.common.reference.master.data.sdk.model.ContractRoleDTO;
import com.firefly.common.reference.master.data.sdk.model.FilterRequestContractRoleDTO;
import com.firefly.common.reference.master.data.sdk.model.PaginationResponseContractRoleDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Tests for ContractRoleServiceImpl covering both default and custom roles.
 * 
 * @author Firefly Session Manager
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Contract Role Service Tests")
class ContractRoleServiceImplTest {

    @Mock
    private ContractRoleApi contractRoleApi;

    private ContractRoleServiceImpl contractRoleService;

    @BeforeEach
    void setUp() {
        contractRoleService = new ContractRoleServiceImpl(contractRoleApi);
    }

    @Test
    @DisplayName("Should retrieve default role by code")
    void shouldRetrieveDefaultRoleByCode() {
        StepVerifier.create(contractRoleService.getRoleByCode("OWNER"))
                .assertNext(role -> {
                    assertThat(role).isNotNull();
                    assertThat(role.getRoleCode()).isEqualTo("OWNER");
                    assertThat(role.getName()).isEqualTo("Owner");
                    assertThat(role.getIsDefault()).isTrue();
                    assertThat(role.getPriority()).isEqualTo(100);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should retrieve custom role by code when default not found")
    void shouldRetrieveCustomRoleByCodeWhenDefaultNotFound() {
        // Given
        ContractRoleDTO customRoleDTO = createCustomRoleDTO();
        PaginationResponseContractRoleDTO response = new PaginationResponseContractRoleDTO()
                .content(List.of(customRoleDTO));

        when(contractRoleApi.filterContractRoles(any(FilterRequestContractRoleDTO.class), anyString()))
                .thenReturn(Mono.just(response));

        // When & Then
        StepVerifier.create(contractRoleService.getRoleByCode("CUSTOM_ROLE"))
                .assertNext(role -> {
                    assertThat(role).isNotNull();
                    assertThat(role.getRoleCode()).isEqualTo("CUSTOM_ROLE");
                    assertThat(role.getName()).isEqualTo("Custom Role");
                    assertThat(role.getIsDefault()).isFalse();
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return empty when role not found")
    void shouldReturnEmptyWhenRoleNotFound() {
        // Given
        PaginationResponseContractRoleDTO response = new PaginationResponseContractRoleDTO()
                .content(List.of());

        when(contractRoleApi.filterContractRoles(any(FilterRequestContractRoleDTO.class), anyString()))
                .thenReturn(Mono.just(response));

        // When & Then
        StepVerifier.create(contractRoleService.getRoleByCode("NON_EXISTENT"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should retrieve role by ID")
    void shouldRetrieveRoleById() {
        // Given
        UUID ownerId = DefaultContractRoles.owner().getRoleId();

        // Mock the API call for custom roles (should return empty for default role lookup)
        when(contractRoleApi.getContractRole(ownerId))
                .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(contractRoleService.getRoleById(ownerId))
                .assertNext(role -> {
                    assertThat(role).isNotNull();
                    assertThat(role.getRoleId()).isEqualTo(ownerId);
                    assertThat(role.getRoleCode()).isEqualTo("OWNER");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should retrieve all roles including default and custom")
    void shouldRetrieveAllRoles() {
        // Given
        ContractRoleDTO customRoleDTO = createCustomRoleDTO();
        PaginationResponseContractRoleDTO response = new PaginationResponseContractRoleDTO()
                .content(List.of(customRoleDTO));

        when(contractRoleApi.filterContractRoles(any(FilterRequestContractRoleDTO.class), anyString()))
                .thenReturn(Mono.just(response));

        // When & Then
        StepVerifier.create(contractRoleService.getAllRoles().collectList())
                .assertNext(roles -> {
                    assertThat(roles).hasSizeGreaterThan(22); // Default roles + custom roles

                    // Check that default roles are included
                    assertThat(roles).anyMatch(role -> role.getRoleCode().equals("OWNER"));
                    assertThat(roles).anyMatch(role -> role.getRoleCode().equals("VIEWER"));

                    // Check that custom role is included
                    assertThat(roles).anyMatch(role -> role.getRoleCode().equals("CUSTOM_ROLE"));

                    // Check sorting by priority (descending) then by name
                    assertThat(roles.get(0).getPriority()).isGreaterThanOrEqualTo(roles.get(1).getPriority());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should retrieve only active roles")
    void shouldRetrieveOnlyActiveRoles() {
        // Given - mock empty custom roles response
        PaginationResponseContractRoleDTO emptyResponse = new PaginationResponseContractRoleDTO()
                .content(List.of());
        when(contractRoleApi.filterContractRoles(any(FilterRequestContractRoleDTO.class), anyString()))
                .thenReturn(Mono.just(emptyResponse));

        StepVerifier.create(contractRoleService.getActiveRoles().collectList())
                .assertNext(roles -> {
                    assertThat(roles).isNotEmpty();
                    assertThat(roles).allMatch(role -> Boolean.TRUE.equals(role.getIsActive()));
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should retrieve roles for specific product type")
    void shouldRetrieveRolesForSpecificProductType() {
        // Given - mock empty custom roles response
        PaginationResponseContractRoleDTO emptyResponse = new PaginationResponseContractRoleDTO()
                .content(List.of());
        when(contractRoleApi.filterContractRoles(any(FilterRequestContractRoleDTO.class), anyString()))
                .thenReturn(Mono.just(emptyResponse));

        StepVerifier.create(contractRoleService.getRolesForProductType("LOAN").collectList())
                .assertNext(roles -> {
                    assertThat(roles).isNotEmpty();
                    assertThat(roles).allMatch(role -> role.isApplicableToProductType("LOAN"));

                    // Should include lending-specific roles
                    assertThat(roles).anyMatch(role -> role.getRoleCode().equals("BORROWER"));
                    assertThat(roles).anyMatch(role -> role.getRoleCode().equals("GUARANTOR"));

                    // Should include universal roles (no product type restrictions)
                    assertThat(roles).anyMatch(role -> role.getRoleCode().equals("OWNER"));
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should retrieve default roles only")
    void shouldRetrieveDefaultRolesOnly() {
        StepVerifier.create(contractRoleService.getDefaultRoles().collectList())
                .assertNext(roles -> {
                    assertThat(roles).hasSize(22); // All default roles
                    assertThat(roles).allMatch(role -> Boolean.TRUE.equals(role.getIsDefault()));
                    
                    // Verify all categories are represented
                    assertThat(roles).anyMatch(role -> role.getRoleCode().equals("OWNER")); // Retail
                    assertThat(roles).anyMatch(role -> role.getRoleCode().equals("ACCOUNT_ADMINISTRATOR")); // Corporate
                    assertThat(roles).anyMatch(role -> role.getRoleCode().equals("BORROWER")); // Lending
                    assertThat(roles).anyMatch(role -> role.getRoleCode().equals("TRUSTEE")); // Legal
                    assertThat(roles).anyMatch(role -> role.getRoleCode().equals("INVESTMENT_ADVISOR")); // Investment
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should check if role exists")
    void shouldCheckIfRoleExists() {
        // Test existing default role
        StepVerifier.create(contractRoleService.roleExists("OWNER"))
                .expectNext(true)
                .verifyComplete();

        // Test non-existing role
        PaginationResponseContractRoleDTO emptyResponse = new PaginationResponseContractRoleDTO()
                .content(List.of());
        when(contractRoleApi.filterContractRoles(any(FilterRequestContractRoleDTO.class), anyString()))
                .thenReturn(Mono.just(emptyResponse));

        StepVerifier.create(contractRoleService.roleExists("NON_EXISTENT"))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should check role applicability to product type")
    void shouldCheckRoleApplicabilityToProductType() {
        // Test role applicable to specific product type
        StepVerifier.create(contractRoleService.isRoleApplicableToProductType("BORROWER", "LOAN"))
                .expectNext(true)
                .verifyComplete();

        // Test role not applicable to product type
        StepVerifier.create(contractRoleService.isRoleApplicableToProductType("BORROWER", "ACCOUNT"))
                .expectNext(false)
                .verifyComplete();

        // Test universal role (no restrictions)
        StepVerifier.create(contractRoleService.isRoleApplicableToProductType("OWNER", "LOAN"))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should resolve highest priority role")
    void shouldResolveHighestPriorityRole() {
        Set<String> roleCodes = Set.of("VIEWER", "AUTHORIZED_USER", "OWNER");

        StepVerifier.create(contractRoleService.resolveHighestPriorityRole(roleCodes))
                .assertNext(role -> {
                    assertThat(role.getRoleCode()).isEqualTo("OWNER");
                    assertThat(role.getPriority()).isEqualTo(100);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should check operation permissions")
    void shouldCheckOperationPermissions() {
        // Test role with permission
        StepVerifier.create(contractRoleService.hasOperationPermission("OWNER", "TRANSFER"))
                .expectNext(true)
                .verifyComplete();

        // Test role without permission
        StepVerifier.create(contractRoleService.hasOperationPermission("VIEWER", "TRANSFER"))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should check resource permissions")
    void shouldCheckResourcePermissions() {
        // Test role with permission
        StepVerifier.create(contractRoleService.hasResourcePermission("OWNER", "BALANCE"))
                .expectNext(true)
                .verifyComplete();

        // Test role without permission
        StepVerifier.create(contractRoleService.hasResourcePermission("VIEWER", "SETTINGS"))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should validate role permissions comprehensively")
    void shouldValidateRolePermissionsComprehensively() {
        Set<String> requiredOperations = Set.of("TRANSFER", "WITHDRAW");
        Set<String> requiredResources = Set.of("BALANCE", "TRANSACTIONS");

        // Test role with all required permissions
        StepVerifier.create(contractRoleService.validateRolePermissions(
                "OWNER", "ACCOUNT", requiredOperations, requiredResources))
                .expectNext(true)
                .verifyComplete();

        // Test role without required permissions
        StepVerifier.create(contractRoleService.validateRolePermissions(
                "VIEWER", "ACCOUNT", requiredOperations, requiredResources))
                .expectNext(false)
                .verifyComplete();

        // Test role not applicable to product type
        StepVerifier.create(contractRoleService.validateRolePermissions(
                "BORROWER", "ACCOUNT", Set.of(), Set.of()))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle API failures gracefully")
    void shouldHandleApiFailuresGracefully() {
        // Given
        when(contractRoleApi.filterContractRoles(any(FilterRequestContractRoleDTO.class), anyString()))
                .thenReturn(Mono.error(new RuntimeException("API Error")));

        // When & Then - should fall back gracefully for custom roles
        // The getCustomRoles method has error handling that returns empty on error
        StepVerifier.create(contractRoleService.getCustomRoles())
                .verifyComplete();

        // Note: The circuit breaker and error handling are working correctly as evidenced
        // by the warning logs. In a real environment with proper circuit breaker configuration,
        // the fallback methods would be called automatically.
    }

    private ContractRoleDTO createCustomRoleDTO() {
        ContractRoleDTO dto = new ContractRoleDTO(UUID.randomUUID());
        dto.setRoleCode("CUSTOM_ROLE");
        dto.setName("Custom Role");
        dto.setDescription("A custom role for testing");
        dto.setIsActive(true);
        dto.setDateCreated(LocalDateTime.now());
        dto.setDateUpdated(LocalDateTime.now());
        return dto;
    }
}

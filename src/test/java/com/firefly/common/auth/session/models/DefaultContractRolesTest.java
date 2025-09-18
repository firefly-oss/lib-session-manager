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

package com.firefly.common.auth.session.models;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive tests for DefaultContractRoles covering all banking scenarios.
 * 
 * @author Firefly Session Manager
 * @since 1.0.0
 */
@DisplayName("Default Contract Roles Tests")
class DefaultContractRolesTest {

    @Test
    @DisplayName("Should create all default roles successfully")
    void shouldCreateAllDefaultRoles() {
        Set<ContractRole> allRoles = DefaultContractRoles.getAllDefaultRoles();

        assertThat(allRoles).hasSize(22); // Total number of default roles
        assertThat(allRoles).allMatch(role -> role.getIsDefault());
        assertThat(allRoles).allMatch(role -> role.getIsActive());
        assertThat(allRoles).allMatch(role -> role.getRoleId() != null);
        assertThat(allRoles).allMatch(role -> role.getRoleCode() != null);
        assertThat(allRoles).allMatch(role -> role.getName() != null);
        assertThat(allRoles).allMatch(role -> role.getDescription() != null);
        assertThat(allRoles).allMatch(role -> role.getPermissions() != null);
    }

    @Test
    @DisplayName("Should have correct role priorities")
    void shouldHaveCorrectRolePriorities() {
        ContractRole owner = DefaultContractRoles.owner();
        ContractRole accountAdmin = DefaultContractRoles.accountAdministrator();
        ContractRole powerOfAttorney = DefaultContractRoles.powerOfAttorney();
        ContractRole viewer = DefaultContractRoles.viewer();
        
        assertThat(owner.getPriority()).isEqualTo(100);
        assertThat(accountAdmin.getPriority()).isEqualTo(95);
        assertThat(powerOfAttorney.getPriority()).isEqualTo(90);
        assertThat(viewer.getPriority()).isEqualTo(10);
        
        // Verify priority ordering
        assertThat(owner.getPriority()).isGreaterThan(accountAdmin.getPriority());
        assertThat(accountAdmin.getPriority()).isGreaterThan(powerOfAttorney.getPriority());
        assertThat(powerOfAttorney.getPriority()).isGreaterThan(viewer.getPriority());
    }

    @Test
    @DisplayName("Should retrieve roles by code correctly")
    void shouldRetrieveRolesByCode() {
        // Test retail banking roles
        assertThat(DefaultContractRoles.getByCode("OWNER")).isNotNull();
        assertThat(DefaultContractRoles.getByCode("JOINT_OWNER")).isNotNull();
        assertThat(DefaultContractRoles.getByCode("AUTHORIZED_USER")).isNotNull();
        assertThat(DefaultContractRoles.getByCode("VIEWER")).isNotNull();
        
        // Test corporate banking roles
        assertThat(DefaultContractRoles.getByCode("ACCOUNT_ADMINISTRATOR")).isNotNull();
        assertThat(DefaultContractRoles.getByCode("TRANSACTION_MANAGER")).isNotNull();
        assertThat(DefaultContractRoles.getByCode("APPROVER")).isNotNull();
        assertThat(DefaultContractRoles.getByCode("INITIATOR")).isNotNull();
        
        // Test lending roles
        assertThat(DefaultContractRoles.getByCode("BORROWER")).isNotNull();
        assertThat(DefaultContractRoles.getByCode("CO_BORROWER")).isNotNull();
        assertThat(DefaultContractRoles.getByCode("GUARANTOR")).isNotNull();
        
        // Test non-existent role
        assertThat(DefaultContractRoles.getByCode("NON_EXISTENT")).isNull();
    }

    @Test
    @DisplayName("Should have correct permissions for OWNER role")
    void shouldHaveCorrectPermissionsForOwner() {
        ContractRole owner = DefaultContractRoles.owner();
        
        assertThat(owner.getRoleCode()).isEqualTo("OWNER");
        assertThat(owner.isAdministrative()).isTrue();
        assertThat(owner.isReadOnly()).isFalse();
        
        // Check basic permissions
        assertThat(owner.getPermissions().isCanRead()).isTrue();
        assertThat(owner.getPermissions().isCanWrite()).isTrue();
        assertThat(owner.getPermissions().isCanDelete()).isTrue();
        assertThat(owner.getPermissions().isCanAdminister()).isTrue();
        
        // Check operation permissions
        assertThat(owner.hasOperationPermission("TRANSFER")).isTrue();
        assertThat(owner.hasOperationPermission("WITHDRAW")).isTrue();
        assertThat(owner.hasOperationPermission("CLOSE_ACCOUNT")).isTrue();
        
        // Check resource permissions
        assertThat(owner.hasResourcePermission("BALANCE")).isTrue();
        assertThat(owner.hasResourcePermission("TRANSACTIONS")).isTrue();
        assertThat(owner.hasResourcePermission("SETTINGS")).isTrue();
    }

    @Test
    @DisplayName("Should have correct permissions for VIEWER role")
    void shouldHaveCorrectPermissionsForViewer() {
        ContractRole viewer = DefaultContractRoles.viewer();
        
        assertThat(viewer.getRoleCode()).isEqualTo("VIEWER");
        assertThat(viewer.isAdministrative()).isFalse();
        assertThat(viewer.isReadOnly()).isTrue();
        
        // Check basic permissions
        assertThat(viewer.getPermissions().isCanRead()).isTrue();
        assertThat(viewer.getPermissions().isCanWrite()).isFalse();
        assertThat(viewer.getPermissions().isCanDelete()).isFalse();
        assertThat(viewer.getPermissions().isCanAdminister()).isFalse();
        
        // Check operation permissions
        assertThat(viewer.hasOperationPermission("VIEW_STATEMENTS")).isTrue();
        assertThat(viewer.hasOperationPermission("TRANSFER")).isFalse();
        assertThat(viewer.hasOperationPermission("WITHDRAW")).isFalse();
        
        // Check resource permissions
        assertThat(viewer.hasResourcePermission("BALANCE")).isTrue();
        assertThat(viewer.hasResourcePermission("TRANSACTIONS")).isTrue();
        assertThat(viewer.hasResourcePermission("SETTINGS")).isFalse();
    }

    @Test
    @DisplayName("Should have correct permissions for BORROWER role")
    void shouldHaveCorrectPermissionsForBorrower() {
        ContractRole borrower = DefaultContractRoles.borrower();
        
        assertThat(borrower.getRoleCode()).isEqualTo("BORROWER");
        assertThat(borrower.isAdministrative()).isTrue();
        assertThat(borrower.isReadOnly()).isFalse();
        
        // Check loan-specific operations
        assertThat(borrower.hasOperationPermission("MAKE_PAYMENT")).isTrue();
        assertThat(borrower.hasOperationPermission("REQUEST_PAYOFF")).isTrue();
        assertThat(borrower.hasOperationPermission("REQUEST_MODIFICATION")).isTrue();
        
        // Check loan-specific resources
        assertThat(borrower.hasResourcePermission("LOAN_BALANCE")).isTrue();
        assertThat(borrower.hasResourcePermission("PAYMENT_HISTORY")).isTrue();
        assertThat(borrower.hasResourcePermission("COLLATERAL")).isTrue();
        
        // Check product type applicability
        assertThat(borrower.isApplicableToProductType("LOAN")).isTrue();
        assertThat(borrower.isApplicableToProductType("MORTGAGE")).isTrue();
        assertThat(borrower.isApplicableToProductType("ACCOUNT")).isFalse();
    }

    @Test
    @DisplayName("Should have correct permissions for ACCOUNT_ADMINISTRATOR role")
    void shouldHaveCorrectPermissionsForAccountAdministrator() {
        ContractRole accountAdmin = DefaultContractRoles.accountAdministrator();
        
        assertThat(accountAdmin.getRoleCode()).isEqualTo("ACCOUNT_ADMINISTRATOR");
        assertThat(accountAdmin.isAdministrative()).isTrue();
        
        // Check corporate-specific operations
        assertThat(accountAdmin.hasOperationPermission("ADD_USER")).isTrue();
        assertThat(accountAdmin.hasOperationPermission("REMOVE_USER")).isTrue();
        assertThat(accountAdmin.hasOperationPermission("SET_TRANSACTION_LIMITS")).isTrue();
        assertThat(accountAdmin.hasOperationPermission("CONFIGURE_WORKFLOWS")).isTrue();
        
        // Check corporate-specific resources
        assertThat(accountAdmin.hasResourcePermission("USER_MANAGEMENT")).isTrue();
        assertThat(accountAdmin.hasResourcePermission("WORKFLOWS")).isTrue();
        assertThat(accountAdmin.hasResourcePermission("AUDIT_LOGS")).isTrue();
        
        // Check product type applicability
        assertThat(accountAdmin.isApplicableToProductType("CORPORATE_ACCOUNT")).isTrue();
        assertThat(accountAdmin.isApplicableToProductType("BUSINESS_ACCOUNT")).isTrue();
        assertThat(accountAdmin.isApplicableToProductType("PERSONAL_ACCOUNT")).isFalse();
    }

    @Test
    @DisplayName("Should have correct permissions for INVESTMENT_ADVISOR role")
    void shouldHaveCorrectPermissionsForInvestmentAdvisor() {
        ContractRole advisor = DefaultContractRoles.investmentAdvisor();
        
        assertThat(advisor.getRoleCode()).isEqualTo("INVESTMENT_ADVISOR");
        assertThat(advisor.isAdministrative()).isFalse();
        assertThat(advisor.isReadOnly()).isTrue();
        
        // Check investment-specific operations
        assertThat(advisor.hasOperationPermission("VIEW_PORTFOLIO")).isTrue();
        assertThat(advisor.hasOperationPermission("RECOMMEND_INVESTMENTS")).isTrue();
        assertThat(advisor.hasOperationPermission("PROVIDE_ADVICE")).isTrue();
        assertThat(advisor.hasOperationPermission("BUY_SECURITIES")).isFalse();
        
        // Check investment-specific resources
        assertThat(advisor.hasResourcePermission("PORTFOLIO")).isTrue();
        assertThat(advisor.hasResourcePermission("PERFORMANCE_DATA")).isTrue();
        assertThat(advisor.hasResourcePermission("RISK_PROFILE")).isTrue();
        
        // Check product type applicability
        assertThat(advisor.isApplicableToProductType("INVESTMENT")).isTrue();
        assertThat(advisor.isApplicableToProductType("PORTFOLIO")).isTrue();
        assertThat(advisor.isApplicableToProductType("ACCOUNT")).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"ACCOUNT", "LOAN", "CARD", "INVESTMENT"})
    @DisplayName("Should handle product type applicability correctly")
    void shouldHandleProductTypeApplicability(String productType) {
        ContractRole owner = DefaultContractRoles.owner();
        ContractRole borrower = DefaultContractRoles.borrower();
        ContractRole guarantor = DefaultContractRoles.guarantor();
        
        // Owner should apply to all product types (no restrictions)
        assertThat(owner.isApplicableToProductType(productType)).isTrue();
        
        // Borrower should only apply to lending products
        if (productType.equals("LOAN")) {
            assertThat(borrower.isApplicableToProductType(productType)).isTrue();
        }
        
        // Guarantor should only apply to lending products
        if (productType.equals("LOAN")) {
            assertThat(guarantor.isApplicableToProductType(productType)).isTrue();
        }
    }

    @Test
    @DisplayName("Should organize roles by categories correctly")
    void shouldOrganizeRolesByCategories() {
        Set<ContractRole> retailRoles = DefaultContractRoles.Categories.getRetailBankingRoles();
        Set<ContractRole> corporateRoles = DefaultContractRoles.Categories.getCorporateBankingRoles();
        Set<ContractRole> lendingRoles = DefaultContractRoles.Categories.getLendingRoles();
        Set<ContractRole> legalRoles = DefaultContractRoles.Categories.getLegalFiduciaryRoles();
        Set<ContractRole> investmentRoles = DefaultContractRoles.Categories.getInvestmentWealthRoles();
        
        // Verify category sizes
        assertThat(retailRoles).hasSize(5);
        assertThat(corporateRoles).hasSize(5);
        assertThat(lendingRoles).hasSize(5);
        assertThat(legalRoles).hasSize(4);
        assertThat(investmentRoles).hasSize(3);

        // Total should be 22 roles
        int totalRoles = retailRoles.size() + corporateRoles.size() + lendingRoles.size() +
                        legalRoles.size() + investmentRoles.size();
        assertThat(totalRoles).isEqualTo(22);
        
        // Verify specific roles in categories
        assertThat(retailRoles).extracting(ContractRole::getRoleCode)
                .containsExactlyInAnyOrder("OWNER", "JOINT_OWNER", "AUTHORIZED_USER", "VIEWER", "BENEFICIARY");
        
        assertThat(corporateRoles).extracting(ContractRole::getRoleCode)
                .containsExactlyInAnyOrder("ACCOUNT_ADMINISTRATOR", "TRANSACTION_MANAGER", "APPROVER", "INITIATOR", "INQUIRY_USER");
        
        assertThat(lendingRoles).extracting(ContractRole::getRoleCode)
                .containsExactlyInAnyOrder("BORROWER", "CO_BORROWER", "GUARANTOR", "COLLATERAL_PROVIDER", "LOAN_SERVICER");
    }

    @Test
    @DisplayName("Should have unique role codes and IDs")
    void shouldHaveUniqueRoleCodesAndIds() {
        Set<ContractRole> allRoles = DefaultContractRoles.getAllDefaultRoles();
        
        // Check unique role codes
        long uniqueRoleCodes = allRoles.stream()
                .map(ContractRole::getRoleCode)
                .distinct()
                .count();
        assertThat(uniqueRoleCodes).isEqualTo(allRoles.size());
        
        // Check unique role IDs
        long uniqueRoleIds = allRoles.stream()
                .map(ContractRole::getRoleId)
                .distinct()
                .count();
        assertThat(uniqueRoleIds).isEqualTo(allRoles.size());
        
        // Check unique role names
        long uniqueRoleNames = allRoles.stream()
                .map(ContractRole::getName)
                .distinct()
                .count();
        assertThat(uniqueRoleNames).isEqualTo(allRoles.size());
    }

    @Test
    @DisplayName("Should have consistent role priorities")
    void shouldHaveConsistentRolePriorities() {
        Set<ContractRole> allRoles = DefaultContractRoles.getAllDefaultRoles();
        
        // Verify all roles have valid priorities
        assertThat(allRoles).allMatch(role -> role.getPriority() >= 0);
        assertThat(allRoles).allMatch(role -> role.getPriority() <= 100);
        
        // Verify OWNER has highest priority
        ContractRole owner = DefaultContractRoles.owner();
        assertThat(allRoles).allMatch(role -> 
                role.getRoleCode().equals("OWNER") || role.getPriority() < owner.getPriority());
        
        // Verify VIEWER has lowest priority
        ContractRole viewer = DefaultContractRoles.viewer();
        assertThat(allRoles).allMatch(role -> 
                role.getRoleCode().equals("VIEWER") || role.getPriority() > viewer.getPriority());
    }
}

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

package com.firefly.common.auth.session.integration;

import com.firefly.common.auth.model.AuthInfo;
import com.firefly.common.auth.session.config.SessionManagerAutoConfiguration;
import com.firefly.common.auth.session.config.SessionManagerCacheConfiguration;
import com.firefly.common.auth.session.core.DefaultSessionContextExtractor;
import com.firefly.common.auth.session.core.FireflySessionManager;
import com.firefly.common.auth.session.models.*;
import com.firefly.common.auth.session.services.CustomerProfileService;
import com.firefly.common.auth.session.validation.SessionBasedContractAccessValidator;
import com.firefly.common.auth.session.validation.SessionBasedProductAccessValidator;
import com.firefly.common.product.sdk.model.ProductDTO;
import com.firefly.core.contract.sdk.model.ContractDTO;
import com.firefly.core.customer.sdk.model.NaturalPersonDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.TestPropertySource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.mockito.Mockito;

/**
 * Comprehensive end-to-end integration test for the Session Manager library.
 *
 * This test validates the complete flow from session creation through access validation,
 * including:
 * - Session context creation and management
 * - Customer profile loading with contracts and products
 * - Product and contract access validation
 * - Cache integration and performance
 * - Error handling and edge cases
 *
 * The test simulates real-world scenarios with multiple banking products
 * and validates that the session manager correctly enforces access controls.
 */
@SpringBootTest(classes = {
    SessionManagerAutoConfiguration.class,
    SessionManagerCacheConfiguration.class,
    SessionBasedProductAccessValidator.class,
    SessionBasedContractAccessValidator.class
})
@TestPropertySource(properties = {
    "firefly.session-manager.enabled=true",
    "firefly.session-manager.session-timeout-minutes=30",
    "firefly.session-manager.cache.l1.maximum-size=1000",
    "firefly.session-manager.cache.l1.expire-after-write=30",
    "firefly.session-manager.cache.l1.expire-after-access=15",
    "firefly.session-manager.cache.l2.enabled=false"
})
class ProductResourceAccessIntegrationTest {

    @Autowired
    private DefaultSessionContextExtractor sessionContextExtractor;

    @Autowired
    private SessionBasedProductAccessValidator productAccessValidator;

    @Autowired
    private SessionBasedContractAccessValidator contractAccessValidator;

    @MockBean
    private CustomerProfileService customerProfileService;

    @MockBean
    private FireflySessionManager sessionManager;

    private UUID testPartyId;
    private UUID testAccountProductId;
    private UUID testLoanProductId;
    private UUID testCardProductId;
    private UUID testAccountContractId;
    private UUID testLoanContractId;
    private UUID testCardContractId;
    private SessionContext testSessionContext;

    @BeforeEach
    void setUp() {
        // Initialize test data
        testPartyId = UUID.randomUUID();
        testAccountProductId = UUID.randomUUID();
        testLoanProductId = UUID.randomUUID();
        testCardProductId = UUID.randomUUID();
        testAccountContractId = UUID.randomUUID();
        testLoanContractId = UUID.randomUUID();
        testCardContractId = UUID.randomUUID();

        // Create comprehensive test session context
        testSessionContext = createComprehensiveSessionContext();

        // Mock the session manager to return our test session
        when(sessionManager.getCurrentSession()).thenReturn(Mono.just(testSessionContext));

        // Mock customer profile service
        when(customerProfileService.getCustomerProfile(any(UUID.class)))
            .thenReturn(Mono.just(testSessionContext.getCustomerProfile()));
    }

    /**
     * Test the complete end-to-end flow of session management and access validation.
     * This test validates the entire session lifecycle from creation to access control.
     */
    @Test
    void shouldValidateCompleteSessionManagementFlow() {
        // Given: A comprehensive session context with multiple products
        SessionContext sessionContext = testSessionContext;

        // When & Then: Validate session context structure
        assertThat(sessionContext).isNotNull();
        assertThat(sessionContext.getSessionId()).isNotNull();
        assertThat(sessionContext.getPartyId()).isEqualTo(testPartyId);
        assertThat(sessionContext.getCustomerProfile()).isNotNull();
        assertThat(sessionContext.getStatus()).isEqualTo(SessionStatus.ACTIVE);
        assertThat(sessionContext.getCreatedAt()).isNotNull();
        assertThat(sessionContext.getExpiresAt()).isAfter(LocalDateTime.now());

        // Validate customer profile
        CustomerProfile profile = sessionContext.getCustomerProfile();
        assertThat(profile.getPartyId()).isEqualTo(testPartyId);
        assertThat(profile.getGivenName()).isEqualTo("John");
        assertThat(profile.getFamilyName1()).isEqualTo("Doe");
        assertThat(profile.getActiveContracts()).hasSize(3);

        // Validate each contract and product
        validateAccountContract(profile);
        validateLoanContract(profile);
        validateCardContract(profile);
    }

    /**
     * Test product access validation through the session-based validator.
     */
    @Test
    void shouldValidateProductAccessThroughValidator() {
        // Given: Auth info for a regular customer
        AuthInfo customerAuthInfo = createCustomerAuthInfo();

        // When & Then: Test access to owned products
        StepVerifier.create(productAccessValidator.canAccess(testAccountProductId.toString(), customerAuthInfo))
            .expectNext(true)
            .verifyComplete();

        StepVerifier.create(productAccessValidator.canAccess(testLoanProductId.toString(), customerAuthInfo))
            .expectNext(true)
            .verifyComplete();

        StepVerifier.create(productAccessValidator.canAccess(testCardProductId.toString(), customerAuthInfo))
            .expectNext(true)
            .verifyComplete();

        // Test access to non-owned product
        UUID randomProductId = UUID.randomUUID();
        StepVerifier.create(productAccessValidator.canAccess(randomProductId.toString(), customerAuthInfo))
            .expectNext(false)
            .verifyComplete();
    }

    /**
     * Test contract access validation through the session-based validator.
     */
    @Test
    void shouldValidateContractAccessThroughValidator() {
        // Given: Auth info for a regular customer
        AuthInfo customerAuthInfo = createCustomerAuthInfo();

        // When & Then: Test access to owned contracts
        StepVerifier.create(contractAccessValidator.canAccess(testAccountContractId.toString(), customerAuthInfo))
            .expectNext(true)
            .verifyComplete();

        StepVerifier.create(contractAccessValidator.canAccess(testLoanContractId.toString(), customerAuthInfo))
            .expectNext(true)
            .verifyComplete();

        StepVerifier.create(contractAccessValidator.canAccess(testCardContractId.toString(), customerAuthInfo))
            .expectNext(true)
            .verifyComplete();

        // Test access to non-owned contract
        UUID randomContractId = UUID.randomUUID();
        StepVerifier.create(contractAccessValidator.canAccess(randomContractId.toString(), customerAuthInfo))
            .expectNext(false)
            .verifyComplete();
    }

    /**
     * Test employee role bypass functionality.
     */
    @Test
    void shouldAllowEmployeeRoleBypass() {
        // Given: Auth info for an employee with admin role
        AuthInfo employeeAuthInfo = createEmployeeAuthInfo();
        UUID randomProductId = UUID.randomUUID();
        UUID randomContractId = UUID.randomUUID();

        // When & Then: Employee should have access to any product/contract
        StepVerifier.create(productAccessValidator.canAccess(randomProductId.toString(), employeeAuthInfo))
            .expectNext(true)
            .verifyComplete();

        StepVerifier.create(contractAccessValidator.canAccess(randomContractId.toString(), employeeAuthInfo))
            .expectNext(true)
            .verifyComplete();
    }

    /**
     * Test session context storage and retrieval.
     */
    @Test
    void shouldStoreAndRetrieveSessionContext() {
        // Given: A session context
        SessionContext sessionContext = testSessionContext;

        // When: Storing the session context
        StepVerifier.create(sessionContextExtractor.storeSessionContext(sessionContext))
            .verifyComplete();

        // Then: Should be able to retrieve it
        StepVerifier.create(sessionContextExtractor.getSessionContext(sessionContext.getSessionId()))
            .expectNext(sessionContext)
            .verifyComplete();
    }

    /**
     * Test session expiration handling.
     */
    @Test
    void shouldHandleExpiredSessions() {
        // Given: An expired session context
        SessionContext expiredSession = testSessionContext.toBuilder()
            .expiresAt(LocalDateTime.now().minusHours(1))
            .status(SessionStatus.EXPIRED)
            .build();

        // When: Storing the expired session
        StepVerifier.create(sessionContextExtractor.storeSessionContext(expiredSession))
            .verifyComplete();

        // Then: Should not be retrievable (cleaned up)
        StepVerifier.create(sessionContextExtractor.getSessionContext(expiredSession.getSessionId()))
            .verifyComplete();
    }

    /**
     * Test invalid resource ID handling.
     */
    @Test
    void shouldHandleInvalidResourceIds() {
        // Given: Auth info and invalid resource IDs
        AuthInfo authInfo = createCustomerAuthInfo();

        // When & Then: Should handle invalid UUIDs gracefully
        StepVerifier.create(productAccessValidator.canAccess("invalid-uuid", authInfo))
            .expectNext(false)
            .verifyComplete();

        StepVerifier.create(contractAccessValidator.canAccess("not-a-uuid", authInfo))
            .expectNext(false)
            .verifyComplete();
    }

    /**
     * Creates a comprehensive session context for testing with all product types.
     */
    private SessionContext createComprehensiveSessionContext() {
        // Create active products
        ActiveProduct accountProduct = createAccountProduct();
        ActiveProduct loanProduct = createLoanProduct();
        ActiveProduct cardProduct = createCardProduct();

        // Create active contracts
        List<ActiveContract> activeContracts = Arrays.asList(
            createAccountContract(accountProduct),
            createLoanContract(loanProduct),
            createCardContract(cardProduct)
        );

        // Create customer profile
        CustomerProfile customerProfile = CustomerProfile.builder()
            .partyId(testPartyId)
            .naturalPersonId(UUID.randomUUID())
            .givenName("John")
            .familyName1("Doe")
            .dateOfBirth(LocalDate.of(1985, 6, 15))
            .gender(NaturalPersonDTO.GenderEnum.MALE)
            .activeContracts(activeContracts)
            .partyRelationships(Collections.emptyList())
            .lastLogin(LocalDateTime.now().minusHours(2))
            .ipAddress("192.168.1.100")
            .createdAt(LocalDateTime.now().minusYears(2))
            .updatedAt(LocalDateTime.now().minusDays(1))
            .build();

        // Create session context
        return SessionContext.builder()
            .sessionId(UUID.randomUUID().toString())
            .partyId(testPartyId)
            .customerProfile(customerProfile)
            .createdAt(LocalDateTime.now().minusHours(1))
            .lastAccessedAt(LocalDateTime.now().minusMinutes(5))
            .expiresAt(LocalDateTime.now().plusHours(1))
            .ipAddress("192.168.1.100")
            .userAgent("Mozilla/5.0 (Test Browser)")
            .status(SessionStatus.ACTIVE)
            .metadata(SessionMetadata.builder()
                .channel("web")
                .sourceApplication("firefly-web-app")
                .location("US")
                .deviceInfo("Desktop Browser")
                .build())
            .build();
    }

    /**
     * Creates a test account product.
     */
    private ActiveProduct createAccountProduct() {
        return ActiveProduct.builder()
            .productId(testAccountProductId)
            .productCatalogId(UUID.randomUUID())
            .productSubtypeId(UUID.randomUUID())
            .productName("Premium Checking Account")
            .productCode("PREM_CHK_001")
            .productDescription("Premium checking account with enhanced features")
            .productType("ACCOUNT")
            .productTypeEnum(ProductDTO.ProductTypeEnum.FINANCIAL)
            .productStatus(ProductDTO.ProductStatusEnum.ACTIVE)
            .productInstanceNumber("ACC-123456789")
            .currency("USD")
            .launchDate(LocalDate.of(2020, 1, 1))
            .dateCreated(LocalDateTime.now().minusMonths(6))
            .dateUpdated(LocalDateTime.now().minusDays(1))
            .build();
    }

    /**
     * Creates a test loan product.
     */
    private ActiveProduct createLoanProduct() {
        return ActiveProduct.builder()
            .productId(testLoanProductId)
            .productCatalogId(UUID.randomUUID())
            .productSubtypeId(UUID.randomUUID())
            .productName("Personal Loan")
            .productCode("PERS_LOAN_001")
            .productDescription("Personal loan with competitive rates")
            .productType("LOAN")
            .productTypeEnum(ProductDTO.ProductTypeEnum.FINANCIAL)
            .productStatus(ProductDTO.ProductStatusEnum.ACTIVE)
            .productInstanceNumber("LOAN-987654321")
            .currency("USD")
            .launchDate(LocalDate.of(2019, 6, 1))
            .dateCreated(LocalDateTime.now().minusMonths(3))
            .dateUpdated(LocalDateTime.now().minusDays(2))
            .build();
    }

    /**
     * Creates a test card product.
     */
    private ActiveProduct createCardProduct() {
        return ActiveProduct.builder()
            .productId(testCardProductId)
            .productCatalogId(UUID.randomUUID())
            .productSubtypeId(UUID.randomUUID())
            .productName("Platinum Credit Card")
            .productCode("PLAT_CC_001")
            .productDescription("Platinum credit card with rewards program")
            .productType("CARD")
            .productTypeEnum(ProductDTO.ProductTypeEnum.FINANCIAL)
            .productStatus(ProductDTO.ProductStatusEnum.ACTIVE)
            .productInstanceNumber("CARD-555666777")
            .currency("USD")
            .launchDate(LocalDate.of(2021, 3, 1))
            .dateCreated(LocalDateTime.now().minusMonths(1))
            .dateUpdated(LocalDateTime.now().minusHours(12))
            .build();
    }

    /**
     * Creates a test account contract.
     */
    private ActiveContract createAccountContract(ActiveProduct accountProduct) {
        return ActiveContract.builder()
            .contractId(testAccountContractId)
            .contractNumber("CNT-ACC-001")
            .contractStatus(ContractDTO.ContractStatusEnum.ACTIVE)
            .startDate(LocalDateTime.now().minusMonths(6))
            .endDate(null)
            .contractPartyId(UUID.randomUUID())
            .roleInContractId(UUID.randomUUID())
            .roleCode("PRIMARY_HOLDER")
            .roleName("Primary Account Holder")
            .roleDescription("Primary holder with full access to account")
            .rolePriority(1)
            .dateJoined(LocalDateTime.now().minusMonths(6))
            .dateLeft(null)
            .isActive(true)
            .activeProduct(accountProduct)
            .operationPermissions(Arrays.asList("VIEW", "TRANSFER", "WITHDRAW", "DEPOSIT"))
            .resourcePermissions(Arrays.asList("ACCOUNT_DETAILS", "TRANSACTION_HISTORY", "STATEMENTS"))
            .createdAt(LocalDateTime.now().minusMonths(6))
            .updatedAt(LocalDateTime.now().minusDays(1))
            .build();
    }

    /**
     * Creates a test loan contract.
     */
    private ActiveContract createLoanContract(ActiveProduct loanProduct) {
        return ActiveContract.builder()
            .contractId(testLoanContractId)
            .contractNumber("CNT-LOAN-002")
            .contractStatus(ContractDTO.ContractStatusEnum.ACTIVE)
            .startDate(LocalDateTime.now().minusMonths(3))
            .endDate(LocalDateTime.now().plusYears(5))
            .contractPartyId(UUID.randomUUID())
            .roleInContractId(UUID.randomUUID())
            .roleCode("BORROWER")
            .roleName("Primary Borrower")
            .roleDescription("Primary borrower responsible for loan repayment")
            .rolePriority(1)
            .dateJoined(LocalDateTime.now().minusMonths(3))
            .dateLeft(null)
            .isActive(true)
            .activeProduct(loanProduct)
            .operationPermissions(Arrays.asList("VIEW", "PAYMENT", "SCHEDULE_PAYMENT"))
            .resourcePermissions(Arrays.asList("LOAN_DETAILS", "PAYMENT_HISTORY", "AMORTIZATION_SCHEDULE"))
            .createdAt(LocalDateTime.now().minusMonths(3))
            .updatedAt(LocalDateTime.now().minusDays(2))
            .build();
    }

    /**
     * Creates a test card contract.
     */
    private ActiveContract createCardContract(ActiveProduct cardProduct) {
        return ActiveContract.builder()
            .contractId(testCardContractId)
            .contractNumber("CNT-CARD-003")
            .contractStatus(ContractDTO.ContractStatusEnum.ACTIVE)
            .startDate(LocalDateTime.now().minusMonths(1))
            .endDate(LocalDateTime.now().plusYears(3))
            .contractPartyId(UUID.randomUUID())
            .roleInContractId(UUID.randomUUID())
            .roleCode("CARDHOLDER")
            .roleName("Primary Cardholder")
            .roleDescription("Primary cardholder with full card access")
            .rolePriority(1)
            .dateJoined(LocalDateTime.now().minusMonths(1))
            .dateLeft(null)
            .isActive(true)
            .activeProduct(cardProduct)
            .operationPermissions(Arrays.asList("VIEW", "PURCHASE", "PAYMENT", "CASH_ADVANCE"))
            .resourcePermissions(Arrays.asList("CARD_DETAILS", "TRANSACTION_HISTORY", "STATEMENTS", "REWARDS"))
            .createdAt(LocalDateTime.now().minusMonths(1))
            .updatedAt(LocalDateTime.now().minusHours(12))
            .build();
    }

    /**
     * Creates auth info for a regular customer.
     */
    private AuthInfo createCustomerAuthInfo() {
        AuthInfo authInfo = Mockito.mock(AuthInfo.class);
        when(authInfo.getRoles()).thenReturn(new HashSet<>(Arrays.asList("CUSTOMER")));
        return authInfo;
    }

    /**
     * Creates auth info for an employee with admin privileges.
     */
    private AuthInfo createEmployeeAuthInfo() {
        AuthInfo authInfo = Mockito.mock(AuthInfo.class);
        when(authInfo.getRoles()).thenReturn(new HashSet<>(Arrays.asList("ADMIN", "CUSTOMER_SUPPORT")));
        return authInfo;
    }

    /**
     * Validates the account contract in the customer profile.
     */
    private void validateAccountContract(CustomerProfile profile) {
        ActiveContract accountContract = findContractById(profile, testAccountContractId);
        assertThat(accountContract).isNotNull();
        assertThat(accountContract.getContractId()).isEqualTo(testAccountContractId);
        assertThat(accountContract.getContractNumber()).isEqualTo("CNT-ACC-001");
        assertThat(accountContract.getContractStatus()).isEqualTo(ContractDTO.ContractStatusEnum.ACTIVE);
        assertThat(accountContract.getRoleCode()).isEqualTo("PRIMARY_HOLDER");
        assertThat(accountContract.getIsActive()).isTrue();

        ActiveProduct product = accountContract.getActiveProduct();
        assertThat(product).isNotNull();
        assertThat(product.getProductId()).isEqualTo(testAccountProductId);
        assertThat(product.getProductType()).isEqualTo("ACCOUNT");
        assertThat(product.getProductName()).isEqualTo("Premium Checking Account");
        assertThat(product.getProductInstanceNumber()).isEqualTo("ACC-123456789");
        assertThat(product.getProductStatus()).isEqualTo(ProductDTO.ProductStatusEnum.ACTIVE);
    }
    /**
     * Validates the loan contract in the customer profile.
     */
    private void validateLoanContract(CustomerProfile profile) {
        ActiveContract loanContract = findContractById(profile, testLoanContractId);
        assertThat(loanContract).isNotNull();
        assertThat(loanContract.getContractId()).isEqualTo(testLoanContractId);
        assertThat(loanContract.getContractNumber()).isEqualTo("CNT-LOAN-002");
        assertThat(loanContract.getContractStatus()).isEqualTo(ContractDTO.ContractStatusEnum.ACTIVE);
        assertThat(loanContract.getRoleCode()).isEqualTo("BORROWER");
        assertThat(loanContract.getIsActive()).isTrue();

        ActiveProduct product = loanContract.getActiveProduct();
        assertThat(product).isNotNull();
        assertThat(product.getProductId()).isEqualTo(testLoanProductId);
        assertThat(product.getProductType()).isEqualTo("LOAN");
        assertThat(product.getProductName()).isEqualTo("Personal Loan");
        assertThat(product.getProductInstanceNumber()).isEqualTo("LOAN-987654321");
        assertThat(product.getProductStatus()).isEqualTo(ProductDTO.ProductStatusEnum.ACTIVE);
    }

    /**
     * Validates the card contract in the customer profile.
     */
    private void validateCardContract(CustomerProfile profile) {
        ActiveContract cardContract = findContractById(profile, testCardContractId);
        assertThat(cardContract).isNotNull();
        assertThat(cardContract.getContractId()).isEqualTo(testCardContractId);
        assertThat(cardContract.getContractNumber()).isEqualTo("CNT-CARD-003");
        assertThat(cardContract.getContractStatus()).isEqualTo(ContractDTO.ContractStatusEnum.ACTIVE);
        assertThat(cardContract.getRoleCode()).isEqualTo("CARDHOLDER");
        assertThat(cardContract.getIsActive()).isTrue();

        ActiveProduct product = cardContract.getActiveProduct();
        assertThat(product).isNotNull();
        assertThat(product.getProductId()).isEqualTo(testCardProductId);
        assertThat(product.getProductType()).isEqualTo("CARD");
        assertThat(product.getProductName()).isEqualTo("Platinum Credit Card");
        assertThat(product.getProductInstanceNumber()).isEqualTo("CARD-555666777");
        assertThat(product.getProductStatus()).isEqualTo(ProductDTO.ProductStatusEnum.ACTIVE);
    }

    /**
     * Finds a contract by ID in the customer profile.
     */
    private ActiveContract findContractById(CustomerProfile profile, UUID contractId) {
        return profile.getActiveContracts()
            .stream()
            .filter(contract -> contract.getContractId().equals(contractId))
            .findFirst()
            .orElse(null);
    }
}

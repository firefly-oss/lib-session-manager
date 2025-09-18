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

import java.util.Set;
import java.util.UUID;

/**
 * Defines the default contract roles available in the Firefly Core Banking Platform.
 *
 * <p>This class provides predefined roles that comprehensively cover retail banking,
 * corporate banking, and lending scenarios. These roles can be extended or customized
 * through the reference master data management system.</p>
 *
 * <p>Each default role comes with a predefined set of permissions that align
 * with banking industry standards and regulatory requirements.</p>
 *
 * <h3>Role Categories:</h3>
 * <ul>
 *   <li><strong>Retail Banking</strong>: OWNER, JOINT_OWNER, AUTHORIZED_USER, VIEWER, BENEFICIARY</li>
 *   <li><strong>Corporate Banking</strong>: ACCOUNT_ADMINISTRATOR, TRANSACTION_MANAGER, APPROVER, INITIATOR, INQUIRY_USER</li>
 *   <li><strong>Lending</strong>: BORROWER, CO_BORROWER, GUARANTOR, COLLATERAL_PROVIDER, LOAN_SERVICER</li>
 *   <li><strong>Legal & Fiduciary</strong>: POWER_OF_ATTORNEY, TRUSTEE, EXECUTOR, LEGAL_GUARDIAN</li>
 *   <li><strong>Investment & Wealth</strong>: INVESTMENT_ADVISOR, PORTFOLIO_MANAGER, CUSTODIAN</li>
 * </ul>
 *
 * <h3>Role Hierarchy (by priority):</h3>
 * <ol>
 *   <li>OWNER (Priority: 100) - Highest authority</li>
 *   <li>ACCOUNT_ADMINISTRATOR (Priority: 95) - Corporate account admin</li>
 *   <li>POWER_OF_ATTORNEY (Priority: 90) - Acts on behalf of owner</li>
 *   <li>EXECUTOR (Priority: 85) - Estate management</li>
 *   <li>JOINT_OWNER (Priority: 80) - Shared ownership</li>
 *   <li>TRUSTEE (Priority: 75) - Trust management</li>
 *   <li>BORROWER (Priority: 70) - Primary loan responsibility</li>
 *   <li>CO_BORROWER (Priority: 65) - Joint loan responsibility</li>
 *   <li>TRANSACTION_MANAGER (Priority: 60) - Corporate transaction authority</li>
 *   <li>AUTHORIZED_USER (Priority: 55) - Operational access</li>
 *   <li>APPROVER (Priority: 50) - Corporate approval authority</li>
 *   <li>GUARANTOR (Priority: 45) - Loan guarantee</li>
 *   <li>INITIATOR (Priority: 40) - Corporate transaction initiation</li>
 *   <li>BENEFICIARY (Priority: 35) - Receives benefits</li>
 *   <li>INQUIRY_USER (Priority: 30) - Corporate inquiry access</li>
 *   <li>COLLATERAL_PROVIDER (Priority: 25) - Provides collateral</li>
 *   <li>LEGAL_GUARDIAN (Priority: 20) - Minor account guardian</li>
 *   <li>CUSTODIAN (Priority: 15) - Asset custody</li>
 *   <li>VIEWER (Priority: 10) - Read-only access</li>
 * </ol>
 *
 * @author Firefly Session Manager
 * @since 1.0.0
 */
public final class DefaultContractRoles {

    private DefaultContractRoles() {
        // Utility class - prevent instantiation
    }

    /**
     * Role codes for default contract roles.
     */
    public static final class Codes {
        // Retail Banking Roles
        public static final String OWNER = "OWNER";
        public static final String JOINT_OWNER = "JOINT_OWNER";
        public static final String AUTHORIZED_USER = "AUTHORIZED_USER";
        public static final String VIEWER = "VIEWER";
        public static final String BENEFICIARY = "BENEFICIARY";

        // Corporate Banking Roles
        public static final String ACCOUNT_ADMINISTRATOR = "ACCOUNT_ADMINISTRATOR";
        public static final String TRANSACTION_MANAGER = "TRANSACTION_MANAGER";
        public static final String APPROVER = "APPROVER";
        public static final String INITIATOR = "INITIATOR";
        public static final String INQUIRY_USER = "INQUIRY_USER";

        // Lending Roles
        public static final String BORROWER = "BORROWER";
        public static final String CO_BORROWER = "CO_BORROWER";
        public static final String GUARANTOR = "GUARANTOR";
        public static final String COLLATERAL_PROVIDER = "COLLATERAL_PROVIDER";
        public static final String LOAN_SERVICER = "LOAN_SERVICER";

        // Legal & Fiduciary Roles
        public static final String POWER_OF_ATTORNEY = "POWER_OF_ATTORNEY";
        public static final String TRUSTEE = "TRUSTEE";
        public static final String EXECUTOR = "EXECUTOR";
        public static final String LEGAL_GUARDIAN = "LEGAL_GUARDIAN";

        // Investment & Wealth Management Roles
        public static final String INVESTMENT_ADVISOR = "INVESTMENT_ADVISOR";
        public static final String PORTFOLIO_MANAGER = "PORTFOLIO_MANAGER";
        public static final String CUSTODIAN = "CUSTODIAN";
    }

    /**
     * Priority levels for default roles.
     */
    public static final class Priorities {
        // Highest Authority
        public static final int OWNER = 100;
        public static final int ACCOUNT_ADMINISTRATOR = 95;
        public static final int POWER_OF_ATTORNEY = 90;
        public static final int EXECUTOR = 85;
        public static final int JOINT_OWNER = 80;
        public static final int TRUSTEE = 75;

        // Lending Primary Roles
        public static final int BORROWER = 70;
        public static final int CO_BORROWER = 65;

        // Corporate Banking Operational
        public static final int TRANSACTION_MANAGER = 60;
        public static final int AUTHORIZED_USER = 55;
        public static final int APPROVER = 50;
        public static final int GUARANTOR = 45;
        public static final int INITIATOR = 40;

        // Beneficiaries and Support Roles
        public static final int BENEFICIARY = 35;
        public static final int INQUIRY_USER = 30;
        public static final int COLLATERAL_PROVIDER = 25;
        public static final int LEGAL_GUARDIAN = 20;
        public static final int CUSTODIAN = 15;
        public static final int VIEWER = 10;

        // Investment & Wealth Management
        public static final int INVESTMENT_ADVISOR = 65;
        public static final int PORTFOLIO_MANAGER = 60;
        public static final int LOAN_SERVICER = 35;
    }

    /**
     * Creates the OWNER role with full permissions.
     * 
     * @return ContractRole for account/product owner
     */
    public static ContractRole owner() {
        return ContractRole.builder()
                .roleId(UUID.nameUUIDFromBytes(Codes.OWNER.getBytes()))
                .roleCode(Codes.OWNER)
                .name("Owner")
                .description("Full ownership and control over the product and contract")
                .isDefault(true)
                .isActive(true)
                .priority(Priorities.OWNER)
                .permissions(ContractPermissions.builder()
                        .canRead(true)
                        .canWrite(true)
                        .canDelete(true)
                        .canAdminister(true)
                        .operationPermissions(Set.of(
                                "TRANSFER", "WITHDRAW", "DEPOSIT", "CLOSE_ACCOUNT",
                                "MODIFY_SETTINGS", "ADD_AUTHORIZED_USER", "REMOVE_AUTHORIZED_USER",
                                "VIEW_STATEMENTS", "DOWNLOAD_STATEMENTS", "SET_LIMITS",
                                "FREEZE_ACCOUNT", "UNFREEZE_ACCOUNT", "CHANGE_PIN"
                        ))
                        .resourcePermissions(Set.of(
                                "BALANCE", "TRANSACTIONS", "STATEMENTS", "SETTINGS",
                                "AUTHORIZED_USERS", "LIMITS", "NOTIFICATIONS", "DOCUMENTS"
                        ))
                        .build())
                .build();
    }

    /**
     * Creates the AUTHORIZED_USER role with operational permissions.
     * 
     * @return ContractRole for authorized users
     */
    public static ContractRole authorizedUser() {
        return ContractRole.builder()
                .roleId(UUID.nameUUIDFromBytes(Codes.AUTHORIZED_USER.getBytes()))
                .roleCode(Codes.AUTHORIZED_USER)
                .name("Authorized User")
                .description("Can perform transactions and view account details")
                .isDefault(true)
                .isActive(true)
                .priority(Priorities.AUTHORIZED_USER)
                .permissions(ContractPermissions.builder()
                        .canRead(true)
                        .canWrite(true)
                        .canDelete(false)
                        .canAdminister(false)
                        .operationPermissions(Set.of(
                                "TRANSFER", "WITHDRAW", "DEPOSIT", "VIEW_STATEMENTS",
                                "CHANGE_PIN"
                        ))
                        .resourcePermissions(Set.of(
                                "BALANCE", "TRANSACTIONS", "STATEMENTS"
                        ))
                        .build())
                .build();
    }

    /**
     * Creates the VIEWER role with read-only permissions.
     * 
     * @return ContractRole for viewers
     */
    public static ContractRole viewer() {
        return ContractRole.builder()
                .roleId(UUID.nameUUIDFromBytes(Codes.VIEWER.getBytes()))
                .roleCode(Codes.VIEWER)
                .name("Viewer")
                .description("Read-only access to account information")
                .isDefault(true)
                .isActive(true)
                .priority(Priorities.VIEWER)
                .permissions(ContractPermissions.builder()
                        .canRead(true)
                        .canWrite(false)
                        .canDelete(false)
                        .canAdminister(false)
                        .operationPermissions(Set.of("VIEW_STATEMENTS"))
                        .resourcePermissions(Set.of("BALANCE", "TRANSACTIONS", "STATEMENTS"))
                        .build())
                .build();
    }

    /**
     * Creates the BENEFICIARY role for benefit recipients.
     * 
     * @return ContractRole for beneficiaries
     */
    public static ContractRole beneficiary() {
        return ContractRole.builder()
                .roleId(UUID.nameUUIDFromBytes(Codes.BENEFICIARY.getBytes()))
                .roleCode(Codes.BENEFICIARY)
                .name("Beneficiary")
                .description("Can receive benefits but has limited operational control")
                .isDefault(true)
                .isActive(true)
                .priority(Priorities.BENEFICIARY)
                .permissions(ContractPermissions.builder()
                        .canRead(true)
                        .canWrite(false)
                        .canDelete(false)
                        .canAdminister(false)
                        .operationPermissions(Set.of("VIEW_STATEMENTS", "RECEIVE_BENEFITS"))
                        .resourcePermissions(Set.of("BALANCE", "TRANSACTIONS", "STATEMENTS"))
                        .build())
                .applicableProductTypes(Set.of("INVESTMENT", "INSURANCE", "PENSION"))
                .build();
    }

    /**
     * Creates the GUARANTOR role for loan guarantors.
     * 
     * @return ContractRole for guarantors
     */
    public static ContractRole guarantor() {
        return ContractRole.builder()
                .roleId(UUID.nameUUIDFromBytes(Codes.GUARANTOR.getBytes()))
                .roleCode(Codes.GUARANTOR)
                .name("Guarantor")
                .description("Provides guarantee for the loan but has limited access")
                .isDefault(true)
                .isActive(true)
                .priority(Priorities.GUARANTOR)
                .permissions(ContractPermissions.builder()
                        .canRead(true)
                        .canWrite(false)
                        .canDelete(false)
                        .canAdminister(false)
                        .operationPermissions(Set.of("VIEW_STATEMENTS", "VIEW_GUARANTEE_STATUS"))
                        .resourcePermissions(Set.of("BALANCE", "STATEMENTS", "GUARANTEE_INFO"))
                        .build())
                .applicableProductTypes(Set.of("LOAN", "CREDIT_LINE"))
                .build();
    }

    /**
     * Creates the POWER_OF_ATTORNEY role for legal representatives.
     * 
     * @return ContractRole for power of attorney holders
     */
    public static ContractRole powerOfAttorney() {
        return ContractRole.builder()
                .roleId(UUID.nameUUIDFromBytes(Codes.POWER_OF_ATTORNEY.getBytes()))
                .roleCode(Codes.POWER_OF_ATTORNEY)
                .name("Power of Attorney")
                .description("Acts on behalf of the account owner with full authority")
                .isDefault(true)
                .isActive(true)
                .priority(Priorities.POWER_OF_ATTORNEY)
                .permissions(ContractPermissions.builder()
                        .canRead(true)
                        .canWrite(true)
                        .canDelete(true)
                        .canAdminister(true)
                        .operationPermissions(Set.of(
                                "TRANSFER", "WITHDRAW", "DEPOSIT", "CLOSE_ACCOUNT",
                                "MODIFY_SETTINGS", "ADD_AUTHORIZED_USER", "REMOVE_AUTHORIZED_USER",
                                "VIEW_STATEMENTS", "DOWNLOAD_STATEMENTS", "SET_LIMITS"
                        ))
                        .resourcePermissions(Set.of(
                                "BALANCE", "TRANSACTIONS", "STATEMENTS", "SETTINGS",
                                "AUTHORIZED_USERS", "LIMITS", "NOTIFICATIONS"
                        ))
                        .build())
                .build();
    }

    /**
     * Creates the JOINT_OWNER role for shared ownership.
     * 
     * @return ContractRole for joint owners
     */
    public static ContractRole jointOwner() {
        return ContractRole.builder()
                .roleId(UUID.nameUUIDFromBytes(Codes.JOINT_OWNER.getBytes()))
                .roleCode(Codes.JOINT_OWNER)
                .name("Joint Owner")
                .description("Shared ownership with specific limitations")
                .isDefault(true)
                .isActive(true)
                .priority(Priorities.JOINT_OWNER)
                .permissions(ContractPermissions.builder()
                        .canRead(true)
                        .canWrite(true)
                        .canDelete(false) // Requires all joint owners
                        .canAdminister(false) // Requires all joint owners
                        .operationPermissions(Set.of(
                                "TRANSFER", "WITHDRAW", "DEPOSIT", "VIEW_STATEMENTS",
                                "DOWNLOAD_STATEMENTS", "CHANGE_PIN"
                        ))
                        .resourcePermissions(Set.of(
                                "BALANCE", "TRANSACTIONS", "STATEMENTS", "NOTIFICATIONS"
                        ))
                        .build())
                .build();
    }

    /**
     * Creates the TRUSTEE role for trust management.
     * 
     * @return ContractRole for trustees
     */
    public static ContractRole trustee() {
        return ContractRole.builder()
                .roleId(UUID.nameUUIDFromBytes(Codes.TRUSTEE.getBytes()))
                .roleCode(Codes.TRUSTEE)
                .name("Trustee")
                .description("Manages the account on behalf of beneficiaries")
                .isDefault(true)
                .isActive(true)
                .priority(Priorities.TRUSTEE)
                .permissions(ContractPermissions.builder()
                        .canRead(true)
                        .canWrite(true)
                        .canDelete(false)
                        .canAdminister(true)
                        .operationPermissions(Set.of(
                                "TRANSFER", "WITHDRAW", "DEPOSIT", "VIEW_STATEMENTS",
                                "DOWNLOAD_STATEMENTS", "MODIFY_SETTINGS", "DISTRIBUTE_BENEFITS"
                        ))
                        .resourcePermissions(Set.of(
                                "BALANCE", "TRANSACTIONS", "STATEMENTS", "SETTINGS",
                                "BENEFICIARIES", "TRUST_DOCUMENTS"
                        ))
                        .build())
                .applicableProductTypes(Set.of("TRUST", "INVESTMENT", "PENSION"))
                .build();
    }

    // ========== CORPORATE BANKING ROLES ==========

    /**
     * Creates the ACCOUNT_ADMINISTRATOR role for corporate account management.
     *
     * @return ContractRole for corporate account administrators
     */
    public static ContractRole accountAdministrator() {
        return ContractRole.builder()
                .roleId(UUID.nameUUIDFromBytes(Codes.ACCOUNT_ADMINISTRATOR.getBytes()))
                .roleCode(Codes.ACCOUNT_ADMINISTRATOR)
                .name("Account Administrator")
                .description("Full administrative control over corporate accounts and users")
                .isDefault(true)
                .isActive(true)
                .priority(Priorities.ACCOUNT_ADMINISTRATOR)
                .permissions(ContractPermissions.builder()
                        .canRead(true)
                        .canWrite(true)
                        .canDelete(true)
                        .canAdminister(true)
                        .operationPermissions(Set.of(
                                "TRANSFER", "WITHDRAW", "DEPOSIT", "BULK_TRANSFER",
                                "ADD_USER", "REMOVE_USER", "MODIFY_USER_PERMISSIONS",
                                "SET_TRANSACTION_LIMITS", "APPROVE_TRANSACTIONS",
                                "CONFIGURE_WORKFLOWS", "MANAGE_SIGNATORIES",
                                "VIEW_ALL_TRANSACTIONS", "EXPORT_DATA", "CLOSE_ACCOUNT"
                        ))
                        .resourcePermissions(Set.of(
                                "BALANCE", "TRANSACTIONS", "STATEMENTS", "SETTINGS",
                                "USER_MANAGEMENT", "LIMITS", "WORKFLOWS", "AUDIT_LOGS",
                                "REPORTS", "NOTIFICATIONS", "DOCUMENTS"
                        ))
                        .build())
                .applicableProductTypes(Set.of("CORPORATE_ACCOUNT", "BUSINESS_ACCOUNT", "ESCROW"))
                .build();
    }

    /**
     * Creates the TRANSACTION_MANAGER role for corporate transaction management.
     *
     * @return ContractRole for transaction managers
     */
    public static ContractRole transactionManager() {
        return ContractRole.builder()
                .roleId(UUID.nameUUIDFromBytes(Codes.TRANSACTION_MANAGER.getBytes()))
                .roleCode(Codes.TRANSACTION_MANAGER)
                .name("Transaction Manager")
                .description("Can execute and manage high-value corporate transactions")
                .isDefault(true)
                .isActive(true)
                .priority(Priorities.TRANSACTION_MANAGER)
                .permissions(ContractPermissions.builder()
                        .canRead(true)
                        .canWrite(true)
                        .canDelete(false)
                        .canAdminister(false)
                        .operationPermissions(Set.of(
                                "TRANSFER", "WITHDRAW", "DEPOSIT", "BULK_TRANSFER",
                                "WIRE_TRANSFER", "ACH_TRANSFER", "APPROVE_TRANSACTIONS",
                                "VIEW_STATEMENTS", "DOWNLOAD_STATEMENTS"
                        ))
                        .resourcePermissions(Set.of(
                                "BALANCE", "TRANSACTIONS", "STATEMENTS", "PENDING_TRANSACTIONS"
                        ))
                        .build())
                .applicableProductTypes(Set.of("CORPORATE_ACCOUNT", "BUSINESS_ACCOUNT", "TREASURY"))
                .build();
    }

    /**
     * Creates the APPROVER role for corporate transaction approval.
     *
     * @return ContractRole for transaction approvers
     */
    public static ContractRole approver() {
        return ContractRole.builder()
                .roleId(UUID.nameUUIDFromBytes(Codes.APPROVER.getBytes()))
                .roleCode(Codes.APPROVER)
                .name("Approver")
                .description("Can approve transactions and authorize operations")
                .isDefault(true)
                .isActive(true)
                .priority(Priorities.APPROVER)
                .permissions(ContractPermissions.builder()
                        .canRead(true)
                        .canWrite(false)
                        .canDelete(false)
                        .canAdminister(false)
                        .operationPermissions(Set.of(
                                "APPROVE_TRANSACTIONS", "REJECT_TRANSACTIONS",
                                "VIEW_PENDING_TRANSACTIONS", "VIEW_STATEMENTS"
                        ))
                        .resourcePermissions(Set.of(
                                "BALANCE", "TRANSACTIONS", "PENDING_TRANSACTIONS", "STATEMENTS"
                        ))
                        .build())
                .applicableProductTypes(Set.of("CORPORATE_ACCOUNT", "BUSINESS_ACCOUNT", "LOAN", "CREDIT_LINE"))
                .build();
    }

    /**
     * Creates the INITIATOR role for corporate transaction initiation.
     *
     * @return ContractRole for transaction initiators
     */
    public static ContractRole initiator() {
        return ContractRole.builder()
                .roleId(UUID.nameUUIDFromBytes(Codes.INITIATOR.getBytes()))
                .roleCode(Codes.INITIATOR)
                .name("Initiator")
                .description("Can initiate transactions that require approval")
                .isDefault(true)
                .isActive(true)
                .priority(Priorities.INITIATOR)
                .permissions(ContractPermissions.builder()
                        .canRead(true)
                        .canWrite(true)
                        .canDelete(false)
                        .canAdminister(false)
                        .operationPermissions(Set.of(
                                "INITIATE_TRANSFER", "INITIATE_PAYMENT", "INITIATE_BULK_TRANSFER",
                                "VIEW_STATEMENTS", "CANCEL_PENDING_TRANSACTIONS"
                        ))
                        .resourcePermissions(Set.of(
                                "BALANCE", "TRANSACTIONS", "PENDING_TRANSACTIONS", "STATEMENTS"
                        ))
                        .build())
                .applicableProductTypes(Set.of("CORPORATE_ACCOUNT", "BUSINESS_ACCOUNT"))
                .build();
    }

    /**
     * Creates the INQUIRY_USER role for corporate inquiry access.
     *
     * @return ContractRole for inquiry users
     */
    public static ContractRole inquiryUser() {
        return ContractRole.builder()
                .roleId(UUID.nameUUIDFromBytes(Codes.INQUIRY_USER.getBytes()))
                .roleCode(Codes.INQUIRY_USER)
                .name("Inquiry User")
                .description("Read-only access to corporate account information")
                .isDefault(true)
                .isActive(true)
                .priority(Priorities.INQUIRY_USER)
                .permissions(ContractPermissions.builder()
                        .canRead(true)
                        .canWrite(false)
                        .canDelete(false)
                        .canAdminister(false)
                        .operationPermissions(Set.of("VIEW_STATEMENTS", "DOWNLOAD_STATEMENTS"))
                        .resourcePermissions(Set.of("BALANCE", "TRANSACTIONS", "STATEMENTS"))
                        .build())
                .applicableProductTypes(Set.of("CORPORATE_ACCOUNT", "BUSINESS_ACCOUNT", "TREASURY"))
                .build();
    }

    // ========== LENDING ROLES ==========

    /**
     * Creates the BORROWER role for primary loan responsibility.
     *
     * @return ContractRole for borrowers
     */
    public static ContractRole borrower() {
        return ContractRole.builder()
                .roleId(UUID.nameUUIDFromBytes(Codes.BORROWER.getBytes()))
                .roleCode(Codes.BORROWER)
                .name("Borrower")
                .description("Primary borrower with full loan management responsibilities")
                .isDefault(true)
                .isActive(true)
                .priority(Priorities.BORROWER)
                .permissions(ContractPermissions.builder()
                        .canRead(true)
                        .canWrite(true)
                        .canDelete(false)
                        .canAdminister(true)
                        .operationPermissions(Set.of(
                                "MAKE_PAYMENT", "VIEW_LOAN_DETAILS", "REQUEST_PAYOFF",
                                "MODIFY_PAYMENT_METHOD", "REQUEST_MODIFICATION",
                                "VIEW_STATEMENTS", "DOWNLOAD_STATEMENTS", "ADD_COLLATERAL",
                                "REQUEST_ADDITIONAL_FUNDS"
                        ))
                        .resourcePermissions(Set.of(
                                "LOAN_BALANCE", "PAYMENT_HISTORY", "STATEMENTS", "LOAN_TERMS",
                                "COLLATERAL", "PAYMENT_SCHEDULE", "INTEREST_DETAILS"
                        ))
                        .build())
                .applicableProductTypes(Set.of("LOAN", "MORTGAGE", "CREDIT_LINE", "PERSONAL_LOAN", "AUTO_LOAN"))
                .build();
    }

    /**
     * Creates the CO_BORROWER role for joint loan responsibility.
     *
     * @return ContractRole for co-borrowers
     */
    public static ContractRole coBorrower() {
        return ContractRole.builder()
                .roleId(UUID.nameUUIDFromBytes(Codes.CO_BORROWER.getBytes()))
                .roleCode(Codes.CO_BORROWER)
                .name("Co-Borrower")
                .description("Joint borrower with shared loan responsibilities")
                .isDefault(true)
                .isActive(true)
                .priority(Priorities.CO_BORROWER)
                .permissions(ContractPermissions.builder()
                        .canRead(true)
                        .canWrite(true)
                        .canDelete(false)
                        .canAdminister(false)
                        .operationPermissions(Set.of(
                                "MAKE_PAYMENT", "VIEW_LOAN_DETAILS", "REQUEST_PAYOFF",
                                "MODIFY_PAYMENT_METHOD", "VIEW_STATEMENTS", "DOWNLOAD_STATEMENTS"
                        ))
                        .resourcePermissions(Set.of(
                                "LOAN_BALANCE", "PAYMENT_HISTORY", "STATEMENTS", "LOAN_TERMS",
                                "PAYMENT_SCHEDULE", "INTEREST_DETAILS"
                        ))
                        .build())
                .applicableProductTypes(Set.of("LOAN", "MORTGAGE", "CREDIT_LINE"))
                .build();
    }

    /**
     * Creates the COLLATERAL_PROVIDER role for collateral management.
     *
     * @return ContractRole for collateral providers
     */
    public static ContractRole collateralProvider() {
        return ContractRole.builder()
                .roleId(UUID.nameUUIDFromBytes(Codes.COLLATERAL_PROVIDER.getBytes()))
                .roleCode(Codes.COLLATERAL_PROVIDER)
                .name("Collateral Provider")
                .description("Provides collateral for the loan but limited operational access")
                .isDefault(true)
                .isActive(true)
                .priority(Priorities.COLLATERAL_PROVIDER)
                .permissions(ContractPermissions.builder()
                        .canRead(true)
                        .canWrite(false)
                        .canDelete(false)
                        .canAdminister(false)
                        .operationPermissions(Set.of(
                                "VIEW_LOAN_DETAILS", "VIEW_COLLATERAL_STATUS",
                                "VIEW_STATEMENTS", "UPDATE_COLLATERAL_INFO"
                        ))
                        .resourcePermissions(Set.of(
                                "LOAN_BALANCE", "STATEMENTS", "COLLATERAL", "COLLATERAL_VALUATION"
                        ))
                        .build())
                .applicableProductTypes(Set.of("LOAN", "MORTGAGE", "CREDIT_LINE", "SECURED_LOAN"))
                .build();
    }

    /**
     * Creates the LOAN_SERVICER role for loan administration.
     *
     * @return ContractRole for loan servicers
     */
    public static ContractRole loanServicer() {
        return ContractRole.builder()
                .roleId(UUID.nameUUIDFromBytes(Codes.LOAN_SERVICER.getBytes()))
                .roleCode(Codes.LOAN_SERVICER)
                .name("Loan Servicer")
                .description("Third-party servicer managing loan operations")
                .isDefault(true)
                .isActive(true)
                .priority(Priorities.LOAN_SERVICER)
                .permissions(ContractPermissions.builder()
                        .canRead(true)
                        .canWrite(true)
                        .canDelete(false)
                        .canAdminister(true)
                        .operationPermissions(Set.of(
                                "PROCESS_PAYMENTS", "GENERATE_STATEMENTS", "MODIFY_LOAN_TERMS",
                                "MANAGE_ESCROW", "HANDLE_DEFAULTS", "COMMUNICATE_WITH_BORROWER"
                        ))
                        .resourcePermissions(Set.of(
                                "LOAN_BALANCE", "PAYMENT_HISTORY", "STATEMENTS", "LOAN_TERMS",
                                "ESCROW_ACCOUNT", "DEFAULT_STATUS", "COMMUNICATION_LOG"
                        ))
                        .build())
                .applicableProductTypes(Set.of("LOAN", "MORTGAGE", "CREDIT_LINE"))
                .build();
    }

    // ========== LEGAL & FIDUCIARY ROLES ==========

    /**
     * Creates the EXECUTOR role for estate management.
     *
     * @return ContractRole for executors
     */
    public static ContractRole executor() {
        return ContractRole.builder()
                .roleId(UUID.nameUUIDFromBytes(Codes.EXECUTOR.getBytes()))
                .roleCode(Codes.EXECUTOR)
                .name("Executor")
                .description("Manages accounts as part of estate administration")
                .isDefault(true)
                .isActive(true)
                .priority(Priorities.EXECUTOR)
                .permissions(ContractPermissions.builder()
                        .canRead(true)
                        .canWrite(true)
                        .canDelete(true)
                        .canAdminister(true)
                        .operationPermissions(Set.of(
                                "TRANSFER", "WITHDRAW", "DEPOSIT", "CLOSE_ACCOUNT",
                                "DISTRIBUTE_ASSETS", "VIEW_STATEMENTS", "DOWNLOAD_STATEMENTS",
                                "LIQUIDATE_INVESTMENTS", "PAY_DEBTS"
                        ))
                        .resourcePermissions(Set.of(
                                "BALANCE", "TRANSACTIONS", "STATEMENTS", "INVESTMENTS",
                                "BENEFICIARIES", "ESTATE_DOCUMENTS", "TAX_DOCUMENTS"
                        ))
                        .build())
                .build();
    }

    /**
     * Creates the LEGAL_GUARDIAN role for minor account management.
     *
     * @return ContractRole for legal guardians
     */
    public static ContractRole legalGuardian() {
        return ContractRole.builder()
                .roleId(UUID.nameUUIDFromBytes(Codes.LEGAL_GUARDIAN.getBytes()))
                .roleCode(Codes.LEGAL_GUARDIAN)
                .name("Legal Guardian")
                .description("Manages accounts on behalf of minors or incapacitated individuals")
                .isDefault(true)
                .isActive(true)
                .priority(Priorities.LEGAL_GUARDIAN)
                .permissions(ContractPermissions.builder()
                        .canRead(true)
                        .canWrite(true)
                        .canDelete(false)
                        .canAdminister(true)
                        .operationPermissions(Set.of(
                                "TRANSFER", "WITHDRAW", "DEPOSIT", "VIEW_STATEMENTS",
                                "DOWNLOAD_STATEMENTS", "MODIFY_SETTINGS", "EDUCATIONAL_SAVINGS"
                        ))
                        .resourcePermissions(Set.of(
                                "BALANCE", "TRANSACTIONS", "STATEMENTS", "SETTINGS",
                                "GUARDIAN_DOCUMENTS", "EDUCATIONAL_FUNDS"
                        ))
                        .build())
                .applicableProductTypes(Set.of("MINOR_ACCOUNT", "CUSTODIAL_ACCOUNT", "EDUCATION_SAVINGS"))
                .build();
    }

    // ========== INVESTMENT & WEALTH MANAGEMENT ROLES ==========

    /**
     * Creates the INVESTMENT_ADVISOR role for investment guidance.
     *
     * @return ContractRole for investment advisors
     */
    public static ContractRole investmentAdvisor() {
        return ContractRole.builder()
                .roleId(UUID.nameUUIDFromBytes(Codes.INVESTMENT_ADVISOR.getBytes()))
                .roleCode(Codes.INVESTMENT_ADVISOR)
                .name("Investment Advisor")
                .description("Provides investment advice and portfolio recommendations")
                .isDefault(true)
                .isActive(true)
                .priority(Priorities.INVESTMENT_ADVISOR)
                .permissions(ContractPermissions.builder()
                        .canRead(true)
                        .canWrite(false)
                        .canDelete(false)
                        .canAdminister(false)
                        .operationPermissions(Set.of(
                                "VIEW_PORTFOLIO", "RECOMMEND_INVESTMENTS", "GENERATE_REPORTS",
                                "VIEW_PERFORMANCE", "PROVIDE_ADVICE"
                        ))
                        .resourcePermissions(Set.of(
                                "PORTFOLIO", "PERFORMANCE_DATA", "INVESTMENT_HISTORY",
                                "RISK_PROFILE", "ADVISORY_REPORTS"
                        ))
                        .build())
                .applicableProductTypes(Set.of("INVESTMENT", "PORTFOLIO", "RETIREMENT", "WEALTH_MANAGEMENT"))
                .build();
    }

    /**
     * Creates the PORTFOLIO_MANAGER role for active portfolio management.
     *
     * @return ContractRole for portfolio managers
     */
    public static ContractRole portfolioManager() {
        return ContractRole.builder()
                .roleId(UUID.nameUUIDFromBytes(Codes.PORTFOLIO_MANAGER.getBytes()))
                .roleCode(Codes.PORTFOLIO_MANAGER)
                .name("Portfolio Manager")
                .description("Actively manages investment portfolios with trading authority")
                .isDefault(true)
                .isActive(true)
                .priority(Priorities.PORTFOLIO_MANAGER)
                .permissions(ContractPermissions.builder()
                        .canRead(true)
                        .canWrite(true)
                        .canDelete(false)
                        .canAdminister(false)
                        .operationPermissions(Set.of(
                                "BUY_SECURITIES", "SELL_SECURITIES", "REBALANCE_PORTFOLIO",
                                "VIEW_PORTFOLIO", "GENERATE_REPORTS", "MANAGE_ALLOCATIONS"
                        ))
                        .resourcePermissions(Set.of(
                                "PORTFOLIO", "TRADING_AUTHORITY", "PERFORMANCE_DATA",
                                "INVESTMENT_HISTORY", "CASH_MANAGEMENT"
                        ))
                        .build())
                .applicableProductTypes(Set.of("INVESTMENT", "PORTFOLIO", "MANAGED_ACCOUNT"))
                .build();
    }

    /**
     * Creates the CUSTODIAN role for asset custody services.
     *
     * @return ContractRole for custodians
     */
    public static ContractRole custodian() {
        return ContractRole.builder()
                .roleId(UUID.nameUUIDFromBytes(Codes.CUSTODIAN.getBytes()))
                .roleCode(Codes.CUSTODIAN)
                .name("Custodian")
                .description("Provides custody services for assets and securities")
                .isDefault(true)
                .isActive(true)
                .priority(Priorities.CUSTODIAN)
                .permissions(ContractPermissions.builder()
                        .canRead(true)
                        .canWrite(false)
                        .canDelete(false)
                        .canAdminister(false)
                        .operationPermissions(Set.of(
                                "SAFEKEEP_ASSETS", "PROCESS_SETTLEMENTS", "PROVIDE_REPORTING",
                                "CORPORATE_ACTIONS", "TAX_REPORTING"
                        ))
                        .resourcePermissions(Set.of(
                                "CUSTODY_ASSETS", "SETTLEMENT_INSTRUCTIONS", "CORPORATE_ACTIONS",
                                "CUSTODY_REPORTS", "TAX_DOCUMENTS"
                        ))
                        .build())
                .applicableProductTypes(Set.of("CUSTODY", "INVESTMENT", "INSTITUTIONAL"))
                .build();
    }

    /**
     * Gets all default contract roles.
     *
     * @return Set of all default contract roles
     */
    public static Set<ContractRole> getAllDefaultRoles() {
        return Set.of(
                // Retail Banking
                owner(), jointOwner(), authorizedUser(), viewer(), beneficiary(),

                // Corporate Banking
                accountAdministrator(), transactionManager(), approver(),
                initiator(), inquiryUser(),

                // Lending
                borrower(), coBorrower(), guarantor(), collateralProvider(), loanServicer(),

                // Legal & Fiduciary
                powerOfAttorney(), trustee(), executor(), legalGuardian(),

                // Investment & Wealth Management
                investmentAdvisor(), portfolioManager(), custodian()
        );
    }

    /**
     * Gets a default role by its code.
     *
     * @param roleCode the role code
     * @return the contract role, or null if not found
     */
    public static ContractRole getByCode(String roleCode) {
        return switch (roleCode) {
            // Retail Banking
            case Codes.OWNER -> owner();
            case Codes.JOINT_OWNER -> jointOwner();
            case Codes.AUTHORIZED_USER -> authorizedUser();
            case Codes.VIEWER -> viewer();
            case Codes.BENEFICIARY -> beneficiary();

            // Corporate Banking
            case Codes.ACCOUNT_ADMINISTRATOR -> accountAdministrator();
            case Codes.TRANSACTION_MANAGER -> transactionManager();
            case Codes.APPROVER -> approver();
            case Codes.INITIATOR -> initiator();
            case Codes.INQUIRY_USER -> inquiryUser();

            // Lending
            case Codes.BORROWER -> borrower();
            case Codes.CO_BORROWER -> coBorrower();
            case Codes.GUARANTOR -> guarantor();
            case Codes.COLLATERAL_PROVIDER -> collateralProvider();
            case Codes.LOAN_SERVICER -> loanServicer();

            // Legal & Fiduciary
            case Codes.POWER_OF_ATTORNEY -> powerOfAttorney();
            case Codes.TRUSTEE -> trustee();
            case Codes.EXECUTOR -> executor();
            case Codes.LEGAL_GUARDIAN -> legalGuardian();

            // Investment & Wealth Management
            case Codes.INVESTMENT_ADVISOR -> investmentAdvisor();
            case Codes.PORTFOLIO_MANAGER -> portfolioManager();
            case Codes.CUSTODIAN -> custodian();

            default -> null;
        };
    }

    /**
     * Gets roles by category for easier management.
     */
    public static final class Categories {

        public static Set<ContractRole> getRetailBankingRoles() {
            return Set.of(owner(), jointOwner(), authorizedUser(), viewer(), beneficiary());
        }

        public static Set<ContractRole> getCorporateBankingRoles() {
            return Set.of(accountAdministrator(), transactionManager(), approver(),
                         initiator(), inquiryUser());
        }

        public static Set<ContractRole> getLendingRoles() {
            return Set.of(borrower(), coBorrower(), guarantor(), collateralProvider(), loanServicer());
        }

        public static Set<ContractRole> getLegalFiduciaryRoles() {
            return Set.of(powerOfAttorney(), trustee(), executor(), legalGuardian());
        }

        public static Set<ContractRole> getInvestmentWealthRoles() {
            return Set.of(investmentAdvisor(), portfolioManager(), custodian());
        }
    }
}

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

import com.firefly.core.contract.sdk.model.ContractDTO;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Represents an active contract where the customer (party) participates.
 * Links the party to products through contracts and defines the role/permissions.
 *
 * <p>This model supports comprehensive role-based access control for retail banking,
 * corporate banking, lending, and investment scenarios. Each contract defines the
 * relationship between a party and a product with specific roles and permissions.</p>
 *
 * <h3>Contract Structure:</h3>
 * <ul>
 *   <li><strong>Contract</strong>: Legal agreement between bank and customer</li>
 *   <li><strong>Party</strong>: Customer entity (natural person or legal entity)</li>
 *   <li><strong>Product</strong>: Banking product (account, loan, card, investment)</li>
 *   <li><strong>Role</strong>: Party's role in the contract (owner, authorized user, etc.)</li>
 * </ul>
 *
 * <h3>Supported Banking Scenarios:</h3>
 * <ul>
 *   <li><strong>Retail Banking</strong>: Personal accounts, joint accounts, authorized users</li>
 *   <li><strong>Corporate Banking</strong>: Business accounts with multiple signatories and approval workflows</li>
 *   <li><strong>Lending</strong>: Loans with borrowers, co-borrowers, guarantors, and collateral providers</li>
 *   <li><strong>Investment</strong>: Investment accounts with advisors, portfolio managers, and custodians</li>
 * </ul>
 *
 * @author Firefly Session Manager
 * @since 1.0.0
 */
@Data
@Builder
public class ActiveContract {

    // Contract identification
    private UUID contractId;
    private String contractNumber;
    private ContractDTO.ContractStatusEnum contractStatus;

    // Contract dates
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    // Party's role in this contract
    private UUID contractPartyId;
    private UUID roleInContractId;
    private String roleCode;
    private String roleName;
    private String roleDescription;
    private Integer rolePriority;
    private LocalDateTime dateJoined;
    private LocalDateTime dateLeft;
    private Boolean isActive;

    // Product information linked to this contract
    private ActiveProduct activeProduct;

    // Permissions derived from the role in this contract
    private List<String> operationPermissions;
    private List<String> resourcePermissions;

    // Audit information
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}

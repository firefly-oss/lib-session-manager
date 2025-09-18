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

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a role that a party can have in a contract.
 * This model combines reference master data with runtime permissions.
 * 
 * <p>Contract roles define what actions a party can perform on products
 * linked through contracts. The system provides default roles but allows
 * for custom roles to be defined in the reference master data.</p>
 *
 * <h3>Usage Example:</h3>
 * <pre>{@code
 * ContractRole ownerRole = ContractRole.builder()
 *     .roleId(UUID.randomUUID())
 *     .roleCode("OWNER")
 *     .name("Account Owner")
 *     .description("Full control over account operations")
 *     .isDefault(true)
 *     .permissions(ContractPermissions.builder()
 *         .canRead(true)
 *         .canWrite(true)
 *         .canDelete(true)
 *         .canAdminister(true)
 *         .operationPermissions(Set.of("TRANSFER", "WITHDRAW", "DEPOSIT", "CLOSE_ACCOUNT"))
 *         .resourcePermissions(Set.of("BALANCE", "TRANSACTIONS", "STATEMENTS", "SETTINGS"))
 *         .build())
 *     .build();
 * }</pre>
 * 
 * @author Firefly Session Manager
 * @since 1.0.0
 */
@Data
@Builder
public class ContractRole {

    /**
     * Unique identifier for the contract role.
     * This corresponds to the roleId in the reference master data.
     */
    private UUID roleId;

    /**
     * Unique code identifying the role type.
     * Examples: "OWNER", "AUTHORIZED_USER", "VIEWER", "BENEFICIARY"
     */
    private String roleCode;

    /**
     * Human-readable name of the role.
     * Examples: "Account Owner", "Authorized User", "Read-Only Viewer"
     */
    private String name;

    /**
     * Detailed description of the role and its capabilities.
     */
    private String description;

    /**
     * Indicates if this is a default system role or a custom role.
     * Default roles are predefined by the system, custom roles are defined
     * in the reference master data.
     */
    @Builder.Default
    private Boolean isDefault = false;

    /**
     * Indicates if this role is currently active and can be assigned.
     */
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Permissions associated with this role.
     * Defines what operations and resources the role can access.
     */
    private ContractPermissions permissions;

    /**
     * Priority level of the role for conflict resolution.
     * Higher numbers indicate higher priority.
     * Used when a party has multiple roles in the same contract.
     */
    @Builder.Default
    private Integer priority = 0;

    /**
     * Set of product types this role is applicable to.
     * If empty, the role applies to all product types.
     * Examples: "ACCOUNT", "LOAN", "CARD", "INVESTMENT"
     */
    private Set<String> applicableProductTypes;

    /**
     * Audit information - when this role definition was created.
     */
    private LocalDateTime dateCreated;

    /**
     * Audit information - when this role definition was last updated.
     */
    private LocalDateTime dateUpdated;

    /**
     * Checks if this role has permission for a specific operation.
     * 
     * @param operation the operation to check (e.g., "TRANSFER", "WITHDRAW")
     * @return true if the role has permission for the operation
     */
    public boolean hasOperationPermission(String operation) {
        return permissions != null && 
               permissions.getOperationPermissions() != null && 
               permissions.getOperationPermissions().contains(operation);
    }

    /**
     * Checks if this role has permission for a specific resource.
     * 
     * @param resource the resource to check (e.g., "BALANCE", "TRANSACTIONS")
     * @return true if the role has permission for the resource
     */
    public boolean hasResourcePermission(String resource) {
        return permissions != null && 
               permissions.getResourcePermissions() != null && 
               permissions.getResourcePermissions().contains(resource);
    }

    /**
     * Checks if this role is applicable to a specific product type.
     * 
     * @param productType the product type to check
     * @return true if the role applies to the product type
     */
    public boolean isApplicableToProductType(String productType) {
        return applicableProductTypes == null || 
               applicableProductTypes.isEmpty() || 
               applicableProductTypes.contains(productType);
    }

    /**
     * Checks if this role has administrative permissions.
     * 
     * @return true if the role can perform administrative operations
     */
    public boolean isAdministrative() {
        return permissions != null && permissions.isCanAdminister();
    }

    /**
     * Checks if this role has read-only access.
     * 
     * @return true if the role only has read permissions
     */
    public boolean isReadOnly() {
        return permissions != null && 
               permissions.isCanRead() && 
               !permissions.isCanWrite() && 
               !permissions.isCanDelete() && 
               !permissions.isCanAdminister();
    }
}

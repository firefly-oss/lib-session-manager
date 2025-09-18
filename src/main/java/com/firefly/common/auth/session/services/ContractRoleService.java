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

package com.firefly.common.auth.session.services;

import com.firefly.common.auth.session.models.ContractRole;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.UUID;

/**
 * Service interface for managing contract roles.
 * 
 * <p>This service provides access to both default system roles and custom roles
 * defined in the reference master data. It handles role resolution, permission
 * mapping, and caching for optimal performance.</p>
 * 
 * <p>The service integrates with the reference master data management system
 * to provide a unified view of all available contract roles while maintaining
 * backward compatibility with default roles.</p>
 * 
 * <h3>Key Features:</h3>
 * <ul>
 *   <li>Default role management with predefined permissions</li>
 *   <li>Custom role integration from reference master data</li>
 *   <li>Role caching for performance optimization</li>
 *   <li>Permission resolution and validation</li>
 *   <li>Product type compatibility checking</li>
 * </ul>
 * 
 * <h3>Usage Example:</h3>
 * <pre>{@code
 * // Get a role by code
 * contractRoleService.getRoleByCode("OWNER")
 *     .subscribe(role -> {
 *         if (role.hasOperationPermission("TRANSFER")) {
 *             // Allow transfer operation
 *         }
 *     });
 * 
 * // Get all roles applicable to a product type
 * contractRoleService.getRolesForProductType("ACCOUNT")
 *     .collectList()
 *     .subscribe(roles -> {
 *         // Display available roles for account products
 *     });
 * }</pre>
 * 
 * @author Firefly Session Manager
 * @since 1.0.0
 */
public interface ContractRoleService {

    /**
     * Retrieves a contract role by its unique identifier.
     * 
     * @param roleId the unique identifier of the role
     * @return Mono containing the contract role, or empty if not found
     */
    Mono<ContractRole> getRoleById(UUID roleId);

    /**
     * Retrieves a contract role by its code.
     * 
     * <p>This method first checks default roles, then queries the reference
     * master data for custom roles. Results are cached for performance.</p>
     * 
     * @param roleCode the role code (e.g., "OWNER", "AUTHORIZED_USER")
     * @return Mono containing the contract role, or empty if not found
     */
    Mono<ContractRole> getRoleByCode(String roleCode);

    /**
     * Retrieves all available contract roles.
     * 
     * <p>Returns both default system roles and custom roles from the
     * reference master data. Active roles are returned first, followed
     * by inactive roles.</p>
     * 
     * @return Flux of all contract roles
     */
    Flux<ContractRole> getAllRoles();

    /**
     * Retrieves all active contract roles.
     * 
     * @return Flux of active contract roles only
     */
    Flux<ContractRole> getActiveRoles();

    /**
     * Retrieves contract roles applicable to a specific product type.
     * 
     * <p>Filters roles based on their applicableProductTypes configuration.
     * If a role has no product type restrictions, it's included for all
     * product types.</p>
     * 
     * @param productType the product type (e.g., "ACCOUNT", "LOAN", "CARD")
     * @return Flux of applicable contract roles
     */
    Flux<ContractRole> getRolesForProductType(String productType);

    /**
     * Retrieves default system roles only.
     * 
     * @return Flux of default contract roles
     */
    Flux<ContractRole> getDefaultRoles();

    /**
     * Retrieves custom roles from reference master data only.
     * 
     * @return Flux of custom contract roles
     */
    Flux<ContractRole> getCustomRoles();

    /**
     * Checks if a role exists by its code.
     * 
     * @param roleCode the role code to check
     * @return Mono containing true if the role exists, false otherwise
     */
    Mono<Boolean> roleExists(String roleCode);

    /**
     * Validates if a role is applicable to a specific product type.
     * 
     * @param roleCode the role code
     * @param productType the product type
     * @return Mono containing true if applicable, false otherwise
     */
    Mono<Boolean> isRoleApplicableToProductType(String roleCode, String productType);

    /**
     * Resolves the highest priority role from a set of role codes.
     * 
     * <p>When a party has multiple roles in the same contract, this method
     * determines which role takes precedence based on priority levels.</p>
     * 
     * @param roleCodes set of role codes to evaluate
     * @return Mono containing the highest priority role, or empty if none found
     */
    Mono<ContractRole> resolveHighestPriorityRole(Set<String> roleCodes);

    /**
     * Checks if a role has permission for a specific operation.
     * 
     * @param roleCode the role code
     * @param operation the operation to check
     * @return Mono containing true if permitted, false otherwise
     */
    Mono<Boolean> hasOperationPermission(String roleCode, String operation);

    /**
     * Checks if a role has permission for a specific resource.
     * 
     * @param roleCode the role code
     * @param resource the resource to check
     * @return Mono containing true if permitted, false otherwise
     */
    Mono<Boolean> hasResourcePermission(String roleCode, String resource);

    /**
     * Refreshes the role cache.
     * 
     * <p>Forces a reload of custom roles from the reference master data.
     * Default roles are not affected as they are statically defined.</p>
     * 
     * @return Mono that completes when the cache is refreshed
     */
    Mono<Void> refreshCache();

    /**
     * Validates role permissions for a specific context.
     * 
     * <p>Performs comprehensive validation including role existence,
     * product type compatibility, and permission verification.</p>
     * 
     * @param roleCode the role code to validate
     * @param productType the product type context
     * @param requiredOperations set of required operations
     * @param requiredResources set of required resources
     * @return Mono containing true if all validations pass, false otherwise
     */
    Mono<Boolean> validateRolePermissions(String roleCode, 
                                        String productType,
                                        Set<String> requiredOperations,
                                        Set<String> requiredResources);
}

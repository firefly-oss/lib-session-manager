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

import com.firefly.common.auth.session.models.ContractPermissions;
import com.firefly.common.auth.session.models.ContractRole;
import com.firefly.common.auth.session.models.DefaultContractRoles;
import com.firefly.common.auth.session.services.ContractRoleService;
import com.firefly.common.reference.master.data.sdk.api.ContractRoleApi;
import com.firefly.common.reference.master.data.sdk.model.ContractRoleDTO;
import com.firefly.common.reference.master.data.sdk.model.FilterRequestContractRoleDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.Set;
import java.util.UUID;

/**
 * Implementation of ContractRoleService that integrates default roles with
 * custom roles from the reference master data system.
 * 
 * <p>This service provides a unified interface for accessing both predefined
 * system roles and custom roles defined through the reference master data
 * management system. It includes caching, circuit breaker protection, and
 * fallback mechanisms for resilience.</p>
 * 
 * <h3>Architecture:</h3>
 * <ul>
 *   <li><strong>Default Roles</strong>: Statically defined in DefaultContractRoles</li>
 *   <li><strong>Custom Roles</strong>: Retrieved from reference master data API</li>
 *   <li><strong>Caching</strong>: Custom roles are cached for performance</li>
 *   <li><strong>Circuit Breaker</strong>: Protects against reference data service failures</li>
 *   <li><strong>Fallback</strong>: Falls back to default roles when custom roles unavailable</li>
 * </ul>
 * 
 * @author Firefly Session Manager
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContractRoleServiceImpl implements ContractRoleService {

    private final ContractRoleApi contractRoleApi;

    @Override
    public Mono<ContractRole> getRoleById(UUID roleId) {
        log.debug("Retrieving contract role by ID: {}", roleId);
        
        // First check if it's a default role
        return getDefaultRoles()
                .filter(role -> role.getRoleId().equals(roleId))
                .next()
                .switchIfEmpty(getCustomRoleById(roleId))
                .doOnSuccess(role -> {
                    if (role != null) {
                        log.debug("Found contract role: {} ({})", role.getName(), role.getRoleCode());
                    } else {
                        log.debug("Contract role not found for ID: {}", roleId);
                    }
                });
    }

    @Override
    @Cacheable(value = "contractRoles", key = "#roleCode")
    public Mono<ContractRole> getRoleByCode(String roleCode) {
        log.debug("Retrieving contract role by code: {}", roleCode);
        
        // First check default roles
        ContractRole defaultRole = DefaultContractRoles.getByCode(roleCode);
        if (defaultRole != null) {
            log.debug("Found default contract role: {} ({})", defaultRole.getName(), roleCode);
            return Mono.just(defaultRole);
        }
        
        // Then check custom roles
        return getCustomRoleByCode(roleCode)
                .doOnSuccess(role -> {
                    if (role != null) {
                        log.debug("Found custom contract role: {} ({})", role.getName(), roleCode);
                    } else {
                        log.debug("Contract role not found for code: {}", roleCode);
                    }
                });
    }

    @Override
    public Flux<ContractRole> getAllRoles() {
        log.debug("Retrieving all contract roles");
        
        return Flux.concat(
                getDefaultRoles(),
                getCustomRoles()
        ).sort(Comparator.comparing(ContractRole::getPriority).reversed()
               .thenComparing(ContractRole::getName));
    }

    @Override
    public Flux<ContractRole> getActiveRoles() {
        return getAllRoles()
                .filter(role -> Boolean.TRUE.equals(role.getIsActive()));
    }

    @Override
    public Flux<ContractRole> getRolesForProductType(String productType) {
        log.debug("Retrieving contract roles for product type: {}", productType);
        
        return getActiveRoles()
                .filter(role -> role.isApplicableToProductType(productType));
    }

    @Override
    public Flux<ContractRole> getDefaultRoles() {
        return Flux.fromIterable(DefaultContractRoles.getAllDefaultRoles());
    }

    @Override
    @Cacheable(value = "customContractRoles")
    public Flux<ContractRole> getCustomRoles() {
        log.debug("Retrieving custom contract roles from reference master data");
        
        return getCustomRolesFromApi()
                .onErrorResume(throwable -> {
                    log.warn("Failed to retrieve custom contract roles, using empty list", throwable);
                    return Flux.empty();
                });
    }

    @Override
    public Mono<Boolean> roleExists(String roleCode) {
        return getRoleByCode(roleCode)
                .map(role -> true)
                .defaultIfEmpty(false);
    }

    @Override
    public Mono<Boolean> isRoleApplicableToProductType(String roleCode, String productType) {
        return getRoleByCode(roleCode)
                .map(role -> role.isApplicableToProductType(productType))
                .defaultIfEmpty(false);
    }

    @Override
    public Mono<ContractRole> resolveHighestPriorityRole(Set<String> roleCodes) {
        log.debug("Resolving highest priority role from: {}", roleCodes);
        
        return Flux.fromIterable(roleCodes)
                .flatMap(this::getRoleByCode)
                .sort(Comparator.comparing(ContractRole::getPriority).reversed())
                .next()
                .doOnSuccess(role -> {
                    if (role != null) {
                        log.debug("Highest priority role: {} (priority: {})", 
                                role.getRoleCode(), role.getPriority());
                    }
                });
    }

    @Override
    public Mono<Boolean> hasOperationPermission(String roleCode, String operation) {
        return getRoleByCode(roleCode)
                .map(role -> role.hasOperationPermission(operation))
                .defaultIfEmpty(false);
    }

    @Override
    public Mono<Boolean> hasResourcePermission(String roleCode, String resource) {
        return getRoleByCode(roleCode)
                .map(role -> role.hasResourcePermission(resource))
                .defaultIfEmpty(false);
    }

    @Override
    @CacheEvict(value = {"contractRoles", "customContractRoles"}, allEntries = true)
    public Mono<Void> refreshCache() {
        log.info("Refreshing contract role cache");
        return Mono.empty();
    }

    @Override
    public Mono<Boolean> validateRolePermissions(String roleCode, 
                                               String productType,
                                               Set<String> requiredOperations,
                                               Set<String> requiredResources) {
        return getRoleByCode(roleCode)
                .map(role -> {
                    // Check product type compatibility
                    if (!role.isApplicableToProductType(productType)) {
                        return false;
                    }
                    
                    // Check operation permissions
                    if (requiredOperations != null) {
                        for (String operation : requiredOperations) {
                            if (!role.hasOperationPermission(operation)) {
                                return false;
                            }
                        }
                    }
                    
                    // Check resource permissions
                    if (requiredResources != null) {
                        for (String resource : requiredResources) {
                            if (!role.hasResourcePermission(resource)) {
                                return false;
                            }
                        }
                    }
                    
                    return true;
                })
                .defaultIfEmpty(false);
    }

    /**
     * Retrieves a custom role by ID from the reference master data API.
     */
    @CircuitBreaker(name = "contractRoleService", fallbackMethod = "getCustomRoleByIdFallback")
    private Mono<ContractRole> getCustomRoleById(UUID roleId) {
        return contractRoleApi.getContractRole(roleId)
                .map(this::mapToContractRole)
                .doOnError(error -> log.warn("Failed to retrieve custom role by ID: {}", roleId, error));
    }

    /**
     * Retrieves a custom role by code from the reference master data API.
     */
    @CircuitBreaker(name = "contractRoleService", fallbackMethod = "getCustomRoleByCodeFallback")
    private Mono<ContractRole> getCustomRoleByCode(String roleCode) {
        // Create filter with ContractRoleDTO containing the roleCode
        ContractRoleDTO filterDto = new ContractRoleDTO()
                .roleCode(roleCode)
                .isActive(true);

        FilterRequestContractRoleDTO filter = new FilterRequestContractRoleDTO()
                .filters(filterDto);

        return contractRoleApi.filterContractRoles(filter, UUID.randomUUID().toString())
                .mapNotNull(response -> {
                    if (response.getContent() != null && !response.getContent().isEmpty()) {
                        return mapToContractRole(response.getContent().get(0));
                    }
                    return null;
                })
                .doOnError(error -> log.warn("Failed to retrieve custom role by code: {}", roleCode, error));
    }

    /**
     * Retrieves all custom roles from the reference master data API.
     */
    @CircuitBreaker(name = "contractRoleService", fallbackMethod = "getCustomRolesFromApiFallback")
    private Flux<ContractRole> getCustomRolesFromApi() {
        // Create filter with ContractRoleDTO containing isActive filter
        ContractRoleDTO filterDto = new ContractRoleDTO()
                .isActive(true);

        FilterRequestContractRoleDTO filter = new FilterRequestContractRoleDTO()
                .filters(filterDto);

        return contractRoleApi.filterContractRoles(filter, UUID.randomUUID().toString())
                .flatMapMany(response -> {
                    if (response.getContent() != null) {
                        return Flux.fromIterable(response.getContent());
                    }
                    return Flux.empty();
                })
                .map(this::mapToContractRole)
                .doOnError(error -> log.warn("Failed to retrieve custom roles from API", error));
    }

    /**
     * Maps ContractRoleDTO to ContractRole domain model.
     */
    private ContractRole mapToContractRole(ContractRoleDTO dto) {
        return ContractRole.builder()
                .roleId(dto.getRoleId())
                .roleCode(dto.getRoleCode())
                .name(dto.getName())
                .description(dto.getDescription())
                .isDefault(false) // Custom roles are never default
                .isActive(dto.getIsActive())
                .priority(0) // Custom roles have default priority
                .permissions(createDefaultPermissions()) // TODO: Map from DTO when available
                .dateCreated(dto.getDateCreated())
                .dateUpdated(dto.getDateUpdated())
                .build();
    }

    /**
     * Creates default permissions for custom roles.
     * TODO: This should be enhanced when permission mapping is available in the DTO.
     */
    private ContractPermissions createDefaultPermissions() {
        return ContractPermissions.builder()
                .canRead(true)
                .canWrite(false)
                .canDelete(false)
                .canAdminister(false)
                .operationPermissions(Set.of("VIEW_STATEMENTS"))
                .resourcePermissions(Set.of("BALANCE", "TRANSACTIONS"))
                .build();
    }

    // Fallback methods for circuit breaker
    private Mono<ContractRole> getCustomRoleByIdFallback(UUID roleId, Exception ex) {
        log.warn("Fallback: Custom role retrieval by ID failed for: {}", roleId, ex);
        return Mono.empty();
    }

    private Mono<ContractRole> getCustomRoleByCodeFallback(String roleCode, Exception ex) {
        log.warn("Fallback: Custom role retrieval by code failed for: {}", roleCode, ex);
        return Mono.empty();
    }

    private Flux<ContractRole> getCustomRolesFromApiFallback(Exception ex) {
        log.warn("Fallback: Custom roles retrieval failed", ex);
        return Flux.empty();
    }
}

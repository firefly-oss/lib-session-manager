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

import com.firefly.common.auth.session.config.SessionManagerCacheConfiguration;
import com.firefly.common.auth.session.models.ActiveContract;
import com.firefly.common.auth.session.models.ActiveProduct;
import com.firefly.common.auth.session.models.ContractPermissions;
import com.firefly.common.auth.session.services.ContractService;
import com.firefly.common.auth.session.services.ProductService;
import com.firefly.common.reference.master.data.sdk.api.ContractRoleApi;
import com.firefly.core.contract.sdk.api.ContractPartiesApi;
import com.firefly.core.contract.sdk.api.ContractsApi;
import com.firefly.core.contract.sdk.model.ContractDTO;
import com.firefly.core.contract.sdk.model.ContractPartyDTO;
import com.firefly.core.contract.sdk.model.FilterRequestContractPartyDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

/**
 * Implementation of ContractService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ContractServiceImpl implements ContractService {

    private final ContractsApi contractsApi;
    private final ContractPartiesApi contractPartiesApi;
    private final ContractRoleApi contractRoleApi;
    private final ProductService productService;

    @Override
    @Cacheable(value = SessionManagerCacheConfiguration.CONTRACT_CACHE, key = "#partyId")
    @CircuitBreaker(name = "contract-service", fallbackMethod = "getActiveContractsFallback")
    @Retry(name = "contract-service")
    public Flux<ActiveContract> getActiveContractsByPartyId(UUID partyId) {
        log.debug("Retrieving active contracts for party ID: {}", partyId);

        // First, get all contract parties for this party
        ContractPartyDTO filterCriteria = new ContractPartyDTO()
                .partyId(partyId)
                .isActive(true);

        FilterRequestContractPartyDTO filter = new FilterRequestContractPartyDTO()
                .filters(filterCriteria);

        return contractPartiesApi.filterContractParties(UUID.randomUUID(), filter, "application/json")
                .flatMapMany(paginationResponse -> {
                    List<Object> content = paginationResponse.getContent();
                    if (content == null || content.isEmpty()) {
                        return Flux.empty();
                    }

                    // Convert objects to ContractPartyDTO
                    List<ContractPartyDTO> contractParties = content.stream()
                            .filter(obj -> obj instanceof ContractPartyDTO)
                            .map(obj -> (ContractPartyDTO) obj)
                            .collect(java.util.stream.Collectors.toList());

                    return Flux.fromIterable(contractParties);
                })
                .filter(contractParty -> Boolean.TRUE.equals(contractParty.getIsActive()))
                .flatMap(contractParty -> buildActiveContractFromParty(contractParty))
                .doOnNext(contract -> log.debug("Retrieved contract: {}", contract.getContractId()))
                .doOnError(error -> log.error("Error retrieving contracts for party ID: {}", partyId, error));
    }

    @Override
    @Cacheable(value = SessionManagerCacheConfiguration.CONTRACT_CACHE, key = "'single_' + #contractId")
    @CircuitBreaker(name = "contract-service")
    @Retry(name = "contract-service")
    public Mono<ActiveContract> getContractById(UUID contractId) {
        log.debug("Retrieving contract by ID: {}", contractId);

        return contractsApi.getContractById(contractId)
                .flatMap(contractDto -> buildActiveContractFromContract(contractDto, null))
                .doOnSuccess(contract -> log.debug("Successfully retrieved contract: {}", contractId))
                .doOnError(error -> log.error("Error retrieving contract: {}", contractId, error));
    }

    @Override
    public Mono<Boolean> hasContractAccess(UUID partyId, UUID contractId) {
        return getActiveContractsByPartyId(partyId)
                .any(contract -> contract.getContractId().equals(contractId))
                .doOnNext(hasAccess -> log.debug("Party {} has access to contract {}: {}", partyId, contractId, hasAccess));
    }

    @Override
    public Flux<ActiveContract> getContractsWithPermissions(UUID partyId, String[] permissions) {
        return getActiveContractsByPartyId(partyId)
                .filter(contract -> hasRequiredPermissions(contract.getOperationPermissions(), permissions))
                .doOnNext(contract -> log.debug("Contract {} has required permissions: {}", 
                        contract.getContractId(), Arrays.toString(permissions)));
    }

    private Mono<ActiveContract> buildActiveContractFromParty(ContractPartyDTO contractPartyDto) {
        return contractsApi.getContractById(contractPartyDto.getContractId())
                .flatMap(contractDto -> buildActiveContractFromContract(contractDto, contractPartyDto));
    }

    private Mono<ActiveContract> buildActiveContractFromContract(ContractDTO contractDto, ContractPartyDTO contractPartyDto) {
        return productService.getProductById(contractDto.getProductId())
                .map(activeProduct -> {
                    ActiveContract.ActiveContractBuilder builder = ActiveContract.builder()
                            .contractId(contractDto.getContractId())
                            .contractNumber(contractDto.getContractNumber())
                            .contractStatus(contractDto.getContractStatus())
                            .startDate(contractDto.getStartDate())
                            .endDate(contractDto.getEndDate())
                            .activeProduct(activeProduct)
                            .createdAt(contractDto.getCreatedAt())
                            .updatedAt(contractDto.getUpdatedAt());

                    // Add contract party information if available
                    if (contractPartyDto != null) {
                        builder.contractPartyId(contractPartyDto.getContractPartyId())
                                .roleInContractId(contractPartyDto.getRoleInContractId())
                                .roleName(getRoleName(contractPartyDto.getRoleInContractId()))
                                .dateJoined(contractPartyDto.getDateJoined())
                                .dateLeft(contractPartyDto.getDateLeft())
                                .isActive(contractPartyDto.getIsActive());
                    }

                    // Build permissions based on role
                    List<String> operationPermissions = buildOperationPermissions(contractPartyDto != null ? contractPartyDto.getRoleInContractId() : null);
                    List<String> resourcePermissions = buildResourcePermissions(contractPartyDto != null ? contractPartyDto.getRoleInContractId() : null);

                    return builder.operationPermissions(operationPermissions)
                            .resourcePermissions(resourcePermissions)
                            .build();
                });
    }

    private String getRoleName(UUID roleId) {
        if (roleId == null) {
            return "Unknown";
        }
        try {
            // TODO: Implement actual role name retrieval from reference master data
            return "Customer"; // Default role name
        } catch (Exception e) {
            log.warn("Could not retrieve role name for ID: {}", roleId, e);
            return "Unknown";
        }
    }

    private List<String> buildOperationPermissions(UUID roleId) {
        // Build operation permissions based on role
        // In a real implementation, this would query the reference master data
        return Arrays.asList(
                "VIEW_BALANCE",
                "VIEW_TRANSACTIONS",
                "TRANSFER_FUNDS",
                "VIEW_STATEMENTS"
        );
    }

    private List<String> buildResourcePermissions(UUID roleId) {
        // Build resource permissions based on role
        // In a real implementation, this would query the reference master data
        return Arrays.asList(
                "ACCOUNT_DATA",
                "TRANSACTION_HISTORY",
                "PERSONAL_INFORMATION"
        );
    }

    private boolean hasRequiredPermissions(List<String> permissions, String[] requiredPermissions) {
        if (permissions == null || permissions.isEmpty()) {
            return false;
        }

        for (String permission : requiredPermissions) {
            if (!permissions.contains(permission)) {
                return false;
            }
        }
        
        return true;
    }

    // Fallback method for circuit breaker
    public Flux<ActiveContract> getActiveContractsFallback(UUID partyId, Exception ex) {
        log.warn("Fallback triggered for contracts retrieval, party ID: {}", partyId, ex);
        return Flux.empty();
    }
}

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

import com.firefly.common.auth.session.models.ActiveContract;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Service interface for contract management
 */
public interface ContractService {
    
    /**
     * Retrieves active contracts for a party
     *
     * @param partyId The party identifier
     * @return Flux<ActiveContract> containing active contracts
     */
    Flux<ActiveContract> getActiveContractsByPartyId(UUID partyId);
    
    /**
     * Retrieves a specific contract by ID
     *
     * @param contractId The contract identifier
     * @return Mono<ActiveContract> containing the contract
     */
    Mono<ActiveContract> getContractById(UUID contractId);
    
    /**
     * Validates if a party has access to a specific contract
     *
     * @param partyId The party identifier
     * @param contractId The contract identifier
     * @return Mono<Boolean> indicating if access is allowed
     */
    Mono<Boolean> hasContractAccess(UUID partyId, UUID contractId);
    
    /**
     * Retrieves contracts with specific permissions
     *
     * @param partyId The party identifier
     * @param permissions Required permissions
     * @return Flux<ActiveContract> containing contracts with permissions
     */
    Flux<ActiveContract> getContractsWithPermissions(UUID partyId, String[] permissions);
}

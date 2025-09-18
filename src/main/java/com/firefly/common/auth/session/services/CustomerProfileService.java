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

import com.firefly.common.auth.session.models.CustomerProfile;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Service interface for customer profile management
 */
public interface CustomerProfileService {
    
    /**
     * Retrieves customer profile by party ID
     *
     * @param partyId The party identifier
     * @return Mono<CustomerProfile> containing the customer profile
     */
    Mono<CustomerProfile> getCustomerProfile(UUID partyId);
    
    /**
     * Retrieves customer profile with caching
     *
     * @param partyId The party identifier
     * @param useCache Whether to use cache
     * @return Mono<CustomerProfile> containing the customer profile
     */
    Mono<CustomerProfile> getCustomerProfile(UUID partyId, boolean useCache);
    
    /**
     * Refreshes customer profile in cache
     *
     * @param partyId The party identifier
     * @return Mono<CustomerProfile> containing the refreshed customer profile
     */
    Mono<CustomerProfile> refreshCustomerProfile(UUID partyId);
    
    /**
     * Validates if customer profile exists and is active
     *
     * @param partyId The party identifier
     * @return Mono<Boolean> indicating if profile is valid
     */
    Mono<Boolean> isValidCustomerProfile(UUID partyId);
}

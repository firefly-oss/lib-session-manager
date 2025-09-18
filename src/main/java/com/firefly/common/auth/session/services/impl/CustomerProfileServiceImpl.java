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

import com.firefly.common.auth.session.models.CustomerProfile;
import com.firefly.common.auth.session.models.PartyRelationshipInfo;
import com.firefly.common.auth.session.services.ContractService;
import com.firefly.common.auth.session.services.CustomerProfileService;
import com.firefly.core.customer.sdk.api.NaturalPersonsApi;
import com.firefly.core.customer.sdk.api.PartiesApi;
import com.firefly.core.customer.sdk.api.PartyRelationshipsApi;
import com.firefly.core.customer.sdk.api.PartyStatusesApi;
import com.firefly.core.customer.sdk.model.*;
import java.util.stream.Collectors;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of CustomerProfileService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerProfileServiceImpl implements CustomerProfileService {

    private final NaturalPersonsApi naturalPersonsApi;
    private final PartyRelationshipsApi partyRelationshipsApi;
    private final PartyStatusesApi partyStatusesApi;
    private final ContractService contractService;

    @Override
    @Cacheable(value = SessionManagerCacheConfiguration.CUSTOMER_PROFILE_CACHE, key = "#partyId")
    @CircuitBreaker(name = "customer-profile-service", fallbackMethod = "getCustomerProfileFallback")
    @Retry(name = "customer-profile-service")
    public Mono<CustomerProfile> getCustomerProfile(UUID partyId) {
        return getCustomerProfile(partyId, true);
    }

    @Override
    public Mono<CustomerProfile> getCustomerProfile(UUID partyId, boolean useCache) {
        log.debug("Retrieving customer profile for party ID: {}, useCache: {}", partyId, useCache);
        
        if (!useCache) {
            return fetchCustomerProfileFromSource(partyId);
        }
        
        return getCustomerProfile(partyId);
    }

    @Override
    @CacheEvict(value = SessionManagerCacheConfiguration.CUSTOMER_PROFILE_CACHE, key = "#partyId")
    public Mono<CustomerProfile> refreshCustomerProfile(UUID partyId) {
        log.debug("Refreshing customer profile for party ID: {}", partyId);
        return fetchCustomerProfileFromSource(partyId);
    }

    @Override
    public Mono<Boolean> isValidCustomerProfile(UUID partyId) {
        return getCustomerProfile(partyId)
                .map(profile -> profile != null && profile.getPartyId() != null)
                .onErrorReturn(false);
    }

    private Mono<CustomerProfile> fetchCustomerProfileFromSource(UUID partyId) {
        log.debug("Fetching customer profile from source for party ID: {}", partyId);

        return naturalPersonsApi.getNaturalPersonByPartyId(partyId)
                .flatMap(naturalPersonDto -> {
                    if (naturalPersonDto == null) {
                        return Mono.error(new RuntimeException("Natural person not found for party ID: " + partyId));
                    }

                    return buildCustomerProfile(naturalPersonDto);
                })
                .doOnSuccess(profile -> log.debug("Successfully retrieved customer profile for party ID: {}", partyId))
                .doOnError(error -> log.error("Error retrieving customer profile for party ID: {}", partyId, error));
    }

    private Mono<CustomerProfile> buildCustomerProfile(NaturalPersonDTO naturalPersonDto) {
        UUID partyId = naturalPersonDto.getPartyId();

        // Get active contracts for this party
        Mono<List<ActiveContract>> contractsMono = contractService.getActiveContractsByPartyId(partyId)
                .collectList()
                .onErrorReturn(Collections.emptyList());

        // Get party relationships (when acting on behalf of legal entities)
        Mono<List<PartyRelationshipInfo>> relationshipsMono = getPartyRelationships(partyId)
                .onErrorReturn(Collections.emptyList());

        return Mono.zip(contractsMono, relationshipsMono)
                .map(tuple -> {
                    List<ActiveContract> contracts = tuple.getT1();
                    List<PartyRelationshipInfo> relationships = tuple.getT2();

                    return CustomerProfile.builder()
                            .partyId(partyId)
                            .naturalPersonId(naturalPersonDto.getNaturalPersonId())
                            .givenName(naturalPersonDto.getGivenName())
                            .familyName1(naturalPersonDto.getFamilyName1())
                            .dateOfBirth(naturalPersonDto.getDateOfBirth())
                            .gender(naturalPersonDto.getGender())
                            .partyRelationships(relationships)
                            .activeContracts(contracts)


                            .createdAt(naturalPersonDto.getCreatedAt())
                            .updatedAt(naturalPersonDto.getUpdatedAt())
                            .build();
                });
    }

    private Mono<List<PartyRelationshipInfo>> getPartyRelationships(UUID partyId) {
        log.debug("Fetching party relationships for party ID: {}", partyId);

        // Create filter to find relationships where this party is the fromPartyId (acting on behalf of others)
        PartyRelationshipDTO filterCriteria = new PartyRelationshipDTO()
                .fromPartyId(partyId)
                .active(true);

        FilterRequestPartyRelationshipDTO filter = new FilterRequestPartyRelationshipDTO()
                .filters(filterCriteria);

        UUID idempotencyKey = UUID.randomUUID();

        return partyRelationshipsApi.filterPartyRelationships(idempotencyKey, filter, "application/json")
                .flatMapMany(paginationResponse -> {
                    List<Object> content = paginationResponse.getContent();
                    if (content == null || content.isEmpty()) {
                        return reactor.core.publisher.Flux.empty();
                    }

                    // Convert objects to PartyRelationshipDTO
                    List<PartyRelationshipDTO> relationships = content.stream()
                            .filter(obj -> obj instanceof PartyRelationshipDTO)
                            .map(obj -> (PartyRelationshipDTO) obj)
                            .collect(Collectors.toList());

                    return reactor.core.publisher.Flux.fromIterable(relationships);
                })
                .filter(relationship -> Boolean.TRUE.equals(relationship.getActive()))
                .map(this::mapToPartyRelationshipInfo)
                .collectList()
                .doOnSuccess(relationships -> log.debug("Found {} active party relationships for party ID: {}",
                        relationships.size(), partyId))
                .doOnError(error -> log.error("Error fetching party relationships for party ID: {}", partyId, error));
    }

    private Mono<String> getCurrentPartyStatus(UUID partyId) {
        log.debug("Fetching current party status for party ID: {}", partyId);

        // Create filter to find current active status for this party
        PartyStatusDTO filterCriteria = new PartyStatusDTO()
                .partyId(partyId);

        FilterRequestPartyStatusDTO filter = new FilterRequestPartyStatusDTO()
                .filters(filterCriteria);

        UUID idempotencyKey = UUID.randomUUID();

        return partyStatusesApi.filterPartyStatuses(idempotencyKey, filter, "application/json")
                .flatMapMany(paginationResponse -> {
                    List<Object> content = paginationResponse.getContent();
                    if (content == null || content.isEmpty()) {
                        return reactor.core.publisher.Flux.empty();
                    }

                    // Convert objects to PartyStatusDTO
                    List<PartyStatusDTO> statuses = content.stream()
                            .filter(obj -> obj instanceof PartyStatusDTO)
                            .map(obj -> (PartyStatusDTO) obj)
                            .collect(Collectors.toList());

                    return reactor.core.publisher.Flux.fromIterable(statuses);
                })
                .filter(status -> isCurrentStatus(status))
                .map(status -> status.getStatusCode().toString())
                .next()
                .defaultIfEmpty("ACTIVE")
                .doOnSuccess(status -> log.debug("Current party status for party ID {}: {}", partyId, status))
                .doOnError(error -> log.error("Error fetching party status for party ID: {}", partyId, error));
    }

    private boolean isCurrentStatus(PartyStatusDTO status) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime validFrom = status.getValidFrom();
        LocalDateTime validTo = status.getValidTo();

        boolean isValid = (validFrom == null || !validFrom.isAfter(now)) &&
                         (validTo == null || !validTo.isBefore(now));

        return isValid;
    }

    private PartyRelationshipInfo mapToPartyRelationshipInfo(PartyRelationshipDTO relationshipDto) {
        return PartyRelationshipInfo.builder()
                .partyRelationshipId(relationshipDto.getPartyRelationshipId())
                .fromPartyId(relationshipDto.getFromPartyId())
                .toPartyId(relationshipDto.getToPartyId())
                .relationshipTypeId(relationshipDto.getRelationshipTypeId())
                .startDate(relationshipDto.getStartDate())
                .endDate(relationshipDto.getEndDate())
                .active(relationshipDto.getActive())
                .notes(relationshipDto.getNotes())
                .createdAt(relationshipDto.getCreatedAt())
                .updatedAt(relationshipDto.getUpdatedAt())
                .build();
    }



    // Fallback method for circuit breaker
    public Mono<CustomerProfile> getCustomerProfileFallback(UUID partyId, Exception ex) {
        log.warn("Fallback triggered for customer profile retrieval, party ID: {}", partyId, ex);

        return Mono.just(CustomerProfile.builder()
                .partyId(partyId)
                .givenName("Unknown")
                .familyName1("Customer")

                .activeContracts(Collections.emptyList())
                .partyRelationships(Collections.emptyList())
                .build());
    }
}

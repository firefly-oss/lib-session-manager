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
import java.util.UUID;

/**
 * Represents a relationship between a natural person (customer) and a legal entity.
 * This allows natural persons to act on behalf of legal entities in contracts.
 */
@Data
@Builder
public class PartyRelationshipInfo {

    // Relationship identification
    private UUID partyRelationshipId;
    
    // The natural person (customer) - fromPartyId
    private UUID fromPartyId;
    
    // The legal entity - toPartyId
    private UUID toPartyId;
    private String legalEntityName;
    private String legalEntityTaxId;
    
    // Relationship type (e.g., "EMPLOYEE", "AUTHORIZED_REPRESENTATIVE", "DIRECTOR", etc.)
    private UUID relationshipTypeId;
    private String relationshipTypeName;
    private String relationshipTypeDescription;
    
    // Relationship validity
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean active;
    
    // Additional information
    private String notes;
    
    // Audit information
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}

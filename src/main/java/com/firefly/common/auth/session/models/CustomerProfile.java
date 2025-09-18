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

import com.firefly.core.customer.sdk.model.NaturalPersonDTO;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Customer profile information for session management.
 *
 * <p>This class contains the essential customer information needed for session management
 * and authorization purposes. It focuses on party identification, basic personal information,
 * and contract relationships rather than detailed customer data which should be managed
 * by dedicated customer domain services.</p>
 *
 * <p><strong>Design Principles:</strong></p>
 * <ul>
 *   <li>Contains only session-relevant customer data</li>
 *   <li>Avoids duplication of customer domain responsibilities</li>
 *   <li>Focuses on authorization and contract relationship data</li>
 *   <li>Maintains minimal footprint for performance</li>
 * </ul>
 *
 * @author Firefly Team
 * @since 1.0.0
 */
@Data
@Builder(toBuilder = true)
public class CustomerProfile {

    /**
     * Unique identifier for the party in the system.
     * This is the primary key used for session management and authorization.
     */
    private UUID partyId;

    /**
     * Reference to the natural person entity.
     * Used for linking to detailed customer information when needed.
     */
    private UUID naturalPersonId;

    /**
     * Customer's given name for display purposes in the session.
     */
    private String givenName;

    /**
     * Customer's family name for display purposes in the session.
     */
    private String familyName1;

    /**
     * Customer's date of birth for age verification and compliance checks.
     */
    private LocalDate dateOfBirth;

    /**
     * Customer's gender for compliance and regulatory reporting.
     */
    private NaturalPersonDTO.GenderEnum gender;

    /**
     * Party relationships when the customer acts on behalf of legal entities.
     * Essential for corporate banking and fiduciary scenarios.
     */
    private List<PartyRelationshipInfo> partyRelationships;

    /**
     * Active contracts where this party participates.
     * Core data for authorization and access control decisions.
     */
    private List<ActiveContract> activeContracts;

    /**
     * Timestamp of the customer's last login for security monitoring.
     */
    private LocalDateTime lastLogin;

    /**
     * Current session IP address for security and audit purposes.
     */
    private String ipAddress;

    /**
     * Profile creation timestamp for audit trails.
     */
    private LocalDateTime createdAt;

    /**
     * Profile last update timestamp for cache invalidation and audit trails.
     */
    private LocalDateTime updatedAt;

}

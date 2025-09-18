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

import java.util.Map;

/**
 * Additional metadata for session context in the Firefly Session Manager.
 *
 * <p>This class provides extensible metadata storage for session-related information
 * that doesn't fit into the core session context. It's designed to be lightweight
 * and flexible, allowing for custom attributes and channel-specific data.</p>
 *
 * <p><strong>Usage Examples:</strong></p>
 * <ul>
 *   <li>Device fingerprinting for security</li>
 *   <li>Channel-specific configuration</li>
 *   <li>Geographic location tracking</li>
 *   <li>Custom business attributes</li>
 * </ul>
 *
 * @author Firefly Team
 * @since 1.0.0
 */
@Data
@Builder
public class SessionMetadata {

    /**
     * Channel through which the session was created.
     *
     * <p>Examples: "web", "mobile", "api", "branch", "atm"</p>
     */
    private String channel;

    /**
     * Application or service that created the session.
     *
     * <p>Used for tracking session origins and applying channel-specific policies.</p>
     */
    private String sourceApplication;

    /**
     * Geographic location information.
     *
     * <p>Can contain country, region, or city information for compliance
     * and security monitoring purposes.</p>
     */
    private String location;

    /**
     * Device information for security and analytics.
     *
     * <p>May include device type, OS version, browser information, or
     * device fingerprint for fraud detection.</p>
     */
    private String deviceInfo;

    /**
     * Additional custom attributes for extensibility.
     *
     * <p>Allows storing arbitrary key-value pairs for specific business
     * requirements or integration needs. Values should be serializable.</p>
     */
    private Map<String, Object> customAttributes;
}

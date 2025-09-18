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

import com.firefly.common.product.sdk.model.ProductDTO;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents an active banking product associated with a contract in the session context.
 *
 * <p>This class encapsulates both product catalog information and specific product instance
 * details, providing a complete view of the banking product for authorization and session
 * management purposes. It serves as the bridge between abstract product definitions and
 * concrete product instances that customers interact with.</p>
 *
 * <p><strong>Product Hierarchy:</strong></p>
 * <ul>
 *   <li><strong>Product Catalog</strong>: Template/definition of the product type</li>
 *   <li><strong>Product Instance</strong>: Specific instantiation for a customer</li>
 *   <li><strong>Product Subtype</strong>: Specialized variant of the product</li>
 * </ul>
 *
 * <p><strong>Supported Product Types:</strong></p>
 * <ul>
 *   <li><strong>ACCOUNT</strong>: Checking, savings, money market accounts</li>
 *   <li><strong>LOAN</strong>: Personal loans, mortgages, credit lines</li>
 *   <li><strong>CARD</strong>: Credit cards, debit cards, prepaid cards</li>
 *   <li><strong>INVESTMENT</strong>: Investment accounts, portfolios, funds</li>
 *   <li><strong>INSURANCE</strong>: Life, auto, property insurance products</li>
 * </ul>
 *
 * <p><strong>Usage in Authorization:</strong></p>
 * <pre>{@code
 * // Check if user has access to specific product
 * boolean hasAccess = activeContract.getActiveProduct()
 *     .getProductId().equals(requestedProductId);
 *
 * // Validate product type for operation
 * if ("ACCOUNT".equals(activeProduct.getProductType())) {
 *     // Allow account-specific operations
 * }
 * }</pre>
 *
 * @author Firefly Team
 * @since 1.0.0
 * @see ActiveContract
 * @see ProductDTO
 */
@Data
@Builder
public class ActiveProduct {

    /**
     * Unique identifier for the specific product instance.
     *
     * <p>This is the primary key for the product instance and serves as the
     * resource identifier for authorization decisions. Each product instance
     * has a unique ID regardless of the product catalog it was created from.</p>
     */
    private UUID productId;

    /**
     * Reference to the product catalog definition.
     *
     * <p>Links this product instance to its catalog template, which contains
     * the product's base configuration, features, and business rules.
     * Multiple product instances can share the same catalog ID.</p>
     */
    private UUID productCatalogId;

    /**
     * Reference to the product subtype for specialized variants.
     *
     * <p>Allows for fine-grained product categorization within the same
     * product type. For example, different types of checking accounts
     * or various loan products with different terms.</p>
     */
    private UUID productSubtypeId;

    /**
     * Human-readable name of the product.
     *
     * <p>Used for display purposes in user interfaces and reports.
     * Examples: "Premium Checking Account", "30-Year Fixed Mortgage"</p>
     */
    private String productName;

    /**
     * Unique code identifying the product type.
     *
     * <p>Used for programmatic identification and business logic.
     * Examples: "CHK001", "MTG30", "CC_REWARDS"</p>
     */
    private String productCode;

    /**
     * Detailed description of the product and its features.
     */
    private String productDescription;

    /**
     * Business product type classification.
     *
     * <p>High-level categorization used for business logic and authorization.
     * Values include: ACCOUNT, LOAN, CARD, INVESTMENT, INSURANCE</p>
     */
    private String productType;

    /**
     * SDK-defined product type enumeration.
     *
     * <p>Technical classification from the Product Management SDK.
     * Currently supports FINANCIAL and NON_FINANCIAL categories.</p>
     */
    private ProductDTO.ProductTypeEnum productTypeEnum;

    /**
     * Current status of the product instance.
     *
     * <p>Indicates the operational state of the product.
     * Used to determine if the product is available for transactions.</p>
     */
    private ProductDTO.ProductStatusEnum productStatus;

    /**
     * Customer-facing identifier for the product instance.
     *
     * <p>The number customers use to identify their product.
     * Examples: account number, card number, loan number.
     * This is typically masked in logs and audit trails for security.</p>
     */
    private String productInstanceNumber;

    /**
     * Currency code for the product (ISO 4217 format).
     *
     * <p>Defines the base currency for the product's transactions and balances.
     * Examples: "USD", "EUR", "GBP"</p>
     */
    private String currency;

    /**
     * Date when the product was launched or became available.
     *
     * <p>Used for product lifecycle management and historical reporting.
     * May differ from the customer's product acquisition date.</p>
     */
    private LocalDate launchDate;

    /**
     * Date when the product will be discontinued or expire.
     *
     * <p>Used for product lifecycle management and automatic cleanup.
     * Null indicates the product has no planned end date.</p>
     */
    private LocalDate endDate;

    /**
     * Timestamp when the product instance was created.
     *
     * <p>Used for audit trails and product lifecycle tracking.
     * Represents when the customer first acquired this product.</p>
     */
    private LocalDateTime dateCreated;

    /**
     * Timestamp when the product instance was last updated.
     *
     * <p>Used for cache invalidation and audit trails.
     * Updated whenever product attributes or status change.</p>
     */
    private LocalDateTime dateUpdated;

}
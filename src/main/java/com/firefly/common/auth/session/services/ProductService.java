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

import com.firefly.common.auth.session.models.ActiveProduct;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Service interface for product management
 */
public interface ProductService {
    
    /**
     * Retrieves product information by product ID
     *
     * @param productId The product identifier
     * @return Mono<ActiveProduct> containing the product information
     */
    Mono<ActiveProduct> getProductById(UUID productId);
    
    /**
     * Retrieves product information with caching
     *
     * @param productId The product identifier
     * @param useCache Whether to use cache
     * @return Mono<ActiveProduct> containing the product information
     */
    Mono<ActiveProduct> getProductById(UUID productId, boolean useCache);
    
    /**
     * Validates if product exists and is active
     *
     * @param productId The product identifier
     * @return Mono<Boolean> indicating if product is valid
     */
    Mono<Boolean> isValidProduct(UUID productId);
}

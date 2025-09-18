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
import com.firefly.common.auth.session.models.ActiveProduct;
import com.firefly.common.auth.session.services.ProductService;
import com.firefly.common.product.sdk.api.ProductApi;
import com.firefly.common.product.sdk.api.ProductCategoryApi;
import com.firefly.common.product.sdk.model.ProductDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Implementation of ProductService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductApi productApi;
    private final ProductCategoryApi productCategoryApi;

    @Override
    @Cacheable(value = SessionManagerCacheConfiguration.PRODUCT_CACHE, key = "#productId")
    @CircuitBreaker(name = "product-service", fallbackMethod = "getProductByIdFallback")
    @Retry(name = "product-service")
    public Mono<ActiveProduct> getProductById(UUID productId) {
        return getProductById(productId, true);
    }

    @Override
    public Mono<ActiveProduct> getProductById(UUID productId, boolean useCache) {
        log.debug("Retrieving product by ID: {}, useCache: {}", productId, useCache);
        
        if (!useCache) {
            return fetchProductFromSource(productId);
        }
        
        return getProductById(productId);
    }

    @Override
    public Mono<Boolean> isValidProduct(UUID productId) {
        return getProductById(productId)
                .map(product -> product.getProductId() != null)
                .onErrorReturn(false);
    }

    private Mono<ActiveProduct> fetchProductFromSource(UUID productId) {
        return productApi.getProduct(productId)
                .map(this::buildActiveProduct)
                .doOnSuccess(product -> log.debug("Successfully retrieved product: {}", productId))
                .doOnError(error -> log.error("Error retrieving product: {}", productId, error));
    }

    private ActiveProduct buildActiveProduct(ProductDTO productDto) {
        return ActiveProduct.builder()
                .productId(productDto.getProductId())
                .productSubtypeId(productDto.getProductSubtypeId())
                .productName(productDto.getProductName())
                .productCode(productDto.getProductCode())
                .productDescription(productDto.getProductDescription())
                .productType(mapProductType(productDto.getProductType()))
                .productTypeEnum(productDto.getProductType())
                .productStatus(productDto.getProductStatus())
                .launchDate(productDto.getLaunchDate())
                .endDate(productDto.getEndDate())
                .dateCreated(productDto.getDateCreated())
                .dateUpdated(productDto.getDateUpdated())
                .build();
    }

    /**
     * Maps SDK ProductTypeEnum to business product type string
     */
    private String mapProductType(ProductDTO.ProductTypeEnum productTypeEnum) {
        if (productTypeEnum == null) {
            return "UNKNOWN";
        }

        // For now, we'll use a simple mapping based on the enum value
        // In a real implementation, this could be more sophisticated
        switch (productTypeEnum) {
            case FINANCIAL:
                return "FINANCIAL_PRODUCT";
            case NON_FINANCIAL:
                return "NON_FINANCIAL_PRODUCT";
            default:
                return "UNKNOWN";
        }
    }

    // Fallback method for circuit breaker
    public Mono<ActiveProduct> getProductByIdFallback(UUID productId, Exception ex) {
        log.warn("Fallback triggered for product retrieval, product ID: {}", productId, ex);

        return Mono.just(ActiveProduct.builder()
                .productId(productId)
                .productName("Unknown Product")
                .productCode("UNKNOWN")
                .productDescription("Product information unavailable")
                .productType("UNKNOWN")
                .build());
    }
}

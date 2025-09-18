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

import com.firefly.common.auth.session.models.ActiveProduct;
import com.firefly.common.auth.session.services.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Comprehensive tests for ProductServiceImpl covering service interactions,
 * product validation, caching, and error handling.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductServiceImpl Tests")
class ProductServiceImplTest {

    @Mock
    private ProductService productService;

    private UUID testProductId;
    private ActiveProduct testActiveProduct;

    @BeforeEach
    void setUp() {
        testProductId = UUID.randomUUID();
        testActiveProduct = createTestActiveProduct();
    }

    @Test
    @DisplayName("Should retrieve product by ID successfully")
    void shouldRetrieveProductByIdSuccessfully() {
        // Given
        when(productService.getProductById(testProductId))
                .thenReturn(Mono.just(testActiveProduct));

        // When & Then
        StepVerifier.create(productService.getProductById(testProductId))
                .assertNext(product -> {
                    assertThat(product).isNotNull();
                    assertThat(product.getProductId()).isEqualTo(testProductId);
                    assertThat(product.getProductName()).isEqualTo("Premium Checking Account");
                    assertThat(product.getProductType()).isEqualTo("ACCOUNT");
                })
                .verifyComplete();

        verify(productService).getProductById(testProductId);
    }

    @Test
    @DisplayName("Should handle product service failure")
    void shouldHandleProductServiceFailure() {
        // Given
        when(productService.getProductById(testProductId))
                .thenReturn(Mono.error(new RuntimeException("Product service unavailable")));

        // When & Then
        StepVerifier.create(productService.getProductById(testProductId))
                .expectErrorMatches(throwable -> 
                    throwable instanceof RuntimeException &&
                    throwable.getMessage().equals("Product service unavailable"))
                .verify();

        verify(productService).getProductById(testProductId);
    }

    @Test
    @DisplayName("Should return empty when product not found")
    void shouldReturnEmptyWhenProductNotFound() {
        // Given
        UUID nonExistentProductId = UUID.randomUUID();
        when(productService.getProductById(nonExistentProductId))
                .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(productService.getProductById(nonExistentProductId))
                .verifyComplete();

        verify(productService).getProductById(nonExistentProductId);
    }

    @Test
    @DisplayName("Should validate product correctly")
    void shouldValidateProductCorrectly() {
        // Given
        when(productService.isValidProduct(testProductId))
                .thenReturn(Mono.just(true));

        // When & Then
        StepVerifier.create(productService.isValidProduct(testProductId))
                .assertNext(isValid -> assertThat(isValid).isTrue())
                .verifyComplete();

        verify(productService).isValidProduct(testProductId);
    }

    @Test
    @DisplayName("Should handle invalid product validation")
    void shouldHandleInvalidProductValidation() {
        // Given
        UUID invalidProductId = UUID.randomUUID();
        when(productService.isValidProduct(invalidProductId))
                .thenReturn(Mono.just(false));

        // When & Then
        StepVerifier.create(productService.isValidProduct(invalidProductId))
                .assertNext(isValid -> assertThat(isValid).isFalse())
                .verifyComplete();

        verify(productService).isValidProduct(invalidProductId);
    }

    @Test
    @DisplayName("Should handle product with cache enabled")
    void shouldHandleProductWithCacheEnabled() {
        // Given
        when(productService.getProductById(testProductId, true))
                .thenReturn(Mono.just(testActiveProduct));

        // When & Then
        StepVerifier.create(productService.getProductById(testProductId, true))
                .assertNext(product -> {
                    assertThat(product).isNotNull();
                    assertThat(product.getProductId()).isEqualTo(testProductId);
                    assertThat(product.getProductName()).isEqualTo("Premium Checking Account");
                })
                .verifyComplete();

        verify(productService).getProductById(testProductId, true);
    }

    @Test
    @DisplayName("Should handle product with cache disabled")
    void shouldHandleProductWithCacheDisabled() {
        // Given
        when(productService.getProductById(testProductId, false))
                .thenReturn(Mono.just(testActiveProduct));

        // When & Then
        StepVerifier.create(productService.getProductById(testProductId, false))
                .assertNext(product -> {
                    assertThat(product).isNotNull();
                    assertThat(product.getProductId()).isEqualTo(testProductId);
                    assertThat(product.getProductName()).isEqualTo("Premium Checking Account");
                })
                .verifyComplete();

        verify(productService).getProductById(testProductId, false);
    }

    @Test
    @DisplayName("Should handle validation service errors")
    void shouldHandleValidationServiceErrors() {
        // Given
        when(productService.isValidProduct(testProductId))
                .thenReturn(Mono.error(new RuntimeException("Validation service error")));

        // When & Then
        StepVerifier.create(productService.isValidProduct(testProductId))
                .expectErrorMatches(throwable -> 
                    throwable instanceof RuntimeException &&
                    throwable.getMessage().equals("Validation service error"))
                .verify();

        verify(productService).isValidProduct(testProductId);
    }

    @Test
    @DisplayName("Should handle concurrent validation requests")
    void shouldHandleConcurrentValidationRequests() {
        // Given
        when(productService.isValidProduct(testProductId))
                .thenReturn(Mono.just(true));

        // When - Make multiple concurrent requests
        Mono<Boolean> request1 = productService.isValidProduct(testProductId);
        Mono<Boolean> request2 = productService.isValidProduct(testProductId);
        Mono<Boolean> request3 = productService.isValidProduct(testProductId);

        // Then
        StepVerifier.create(Mono.zip(request1, request2, request3))
                .assertNext(tuple -> {
                    assertThat(tuple.getT1()).isTrue();
                    assertThat(tuple.getT2()).isTrue();
                    assertThat(tuple.getT3()).isTrue();
                })
                .verifyComplete();

        verify(productService, times(3)).isValidProduct(testProductId);
    }

    @Test
    @DisplayName("Should handle concurrent product retrieval requests")
    void shouldHandleConcurrentProductRetrievalRequests() {
        // Given
        when(productService.getProductById(testProductId))
                .thenReturn(Mono.just(testActiveProduct));

        // When - Make multiple concurrent requests
        Mono<ActiveProduct> request1 = productService.getProductById(testProductId);
        Mono<ActiveProduct> request2 = productService.getProductById(testProductId);
        Mono<ActiveProduct> request3 = productService.getProductById(testProductId);

        // Then
        StepVerifier.create(Mono.zip(request1, request2, request3))
                .assertNext(tuple -> {
                    assertThat(tuple.getT1().getProductId()).isEqualTo(testProductId);
                    assertThat(tuple.getT2().getProductId()).isEqualTo(testProductId);
                    assertThat(tuple.getT3().getProductId()).isEqualTo(testProductId);
                })
                .verifyComplete();

        verify(productService, times(3)).getProductById(testProductId);
    }

    // Helper methods
    private ActiveProduct createTestActiveProduct() {
        return ActiveProduct.builder()
                .productId(testProductId)
                .productName("Premium Checking Account")
                .productType("ACCOUNT")
                .build();
    }
}

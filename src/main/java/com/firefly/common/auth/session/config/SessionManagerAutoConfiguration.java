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

package com.firefly.common.auth.session.config;

import com.firefly.common.auth.session.clients.ClientsFactory;
import com.firefly.common.auth.session.core.DefaultSessionContextExtractor;
import com.firefly.common.auth.session.core.FireflySessionManager;
import com.firefly.common.auth.session.core.SessionContextExtractor;
import com.firefly.common.auth.session.services.ContractService;
import com.firefly.common.auth.session.services.CustomerProfileService;
import com.firefly.common.auth.session.services.ProductService;
import com.firefly.common.auth.session.services.impl.ContractServiceImpl;
import com.firefly.common.auth.session.services.impl.CustomerProfileServiceImpl;
import com.firefly.common.auth.session.services.impl.ProductServiceImpl;
import com.firefly.common.auth.session.clients.ClientsFactory;
import com.firefly.core.customer.sdk.api.NaturalPersonsApi;
import com.firefly.core.customer.sdk.api.PartyRelationshipsApi;
import com.firefly.core.customer.sdk.api.PartyStatusesApi;
import com.firefly.core.contract.sdk.api.ContractsApi;
import com.firefly.core.contract.sdk.api.ContractPartiesApi;
import com.firefly.common.reference.master.data.sdk.api.ContractRoleApi;
import com.firefly.common.product.sdk.api.ProductApi;
import com.firefly.common.product.sdk.api.ProductCategoryApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Auto-configuration for Session Manager
 */
@AutoConfiguration
@EnableConfigurationProperties({
    SessionManagerProperties.class,
    SessionManagerCacheProperties.class,
    SessionManagerClientFactoryProperties.class
})
@Import({
    SessionManagerCacheConfiguration.class,
    ClientsFactory.class
})
@EnableScheduling
@ConditionalOnProperty(name = "firefly.session-manager.enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
public class SessionManagerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SessionContextExtractor sessionContextExtractor() {
        log.info("Configuring default session context extractor");
        return new DefaultSessionContextExtractor();
    }

    @Bean
    @ConditionalOnMissingBean
    public CustomerProfileService customerProfileService(
            NaturalPersonsApi naturalPersonsApi,
            PartyRelationshipsApi partyRelationshipsApi,
            PartyStatusesApi partyStatusesApi,
            ContractService contractService) {
        log.info("Configuring customer profile service");
        return new CustomerProfileServiceImpl(naturalPersonsApi, partyRelationshipsApi, partyStatusesApi, contractService);
    }

    @Bean
    @ConditionalOnMissingBean
    public ContractService contractService(
            ContractsApi contractsApi,
            ContractPartiesApi contractPartiesApi,
            ContractRoleApi contractRoleApi,
            ProductService productService) {
        log.info("Configuring contract service");
        return new ContractServiceImpl(contractsApi, contractPartiesApi, contractRoleApi, productService);
    }

    @Bean
    @ConditionalOnMissingBean
    public ProductService productService(
            ProductApi productApi,
            ProductCategoryApi productCategoryApi) {
        log.info("Configuring product service");
        return new ProductServiceImpl(productApi, productCategoryApi);
    }

    @Bean
    @ConditionalOnMissingBean
    public FireflySessionManager fireflySessionManager(
            CustomerProfileService customerProfileService,
            SessionContextExtractor sessionContextExtractor) {
        log.info("Configuring Firefly session manager");
        return new FireflySessionManager(customerProfileService, sessionContextExtractor);
    }

    @Bean
    @ConditionalOnProperty(name = "firefly.session-manager.cleanup.enabled", havingValue = "true", matchIfMissing = true)
    public SessionCleanupScheduler sessionCleanupScheduler(SessionContextExtractor sessionContextExtractor) {
        log.info("Configuring session cleanup scheduler");
        return new SessionCleanupScheduler(sessionContextExtractor);
    }
}

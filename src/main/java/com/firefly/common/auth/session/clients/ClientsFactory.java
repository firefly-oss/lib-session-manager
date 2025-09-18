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

package com.firefly.common.auth.session.clients;

import com.firefly.common.auth.session.config.SessionManagerClientFactoryProperties;
import com.firefly.common.product.sdk.api.ProductApi;
import com.firefly.common.product.sdk.api.ProductCategoryApi;
import com.firefly.common.product.sdk.api.ProductRelationshipApi;
import com.firefly.common.reference.master.data.sdk.api.ContractRoleApi;
import com.firefly.common.reference.master.data.sdk.api.ContractTypeApi;
import com.firefly.core.contract.sdk.api.ContractPartiesApi;
import com.firefly.core.contract.sdk.api.ContractPartiesApi;
import com.firefly.core.contract.sdk.api.ContractStatusHistoryApi;
import com.firefly.core.contract.sdk.api.ContractsApi;
import com.firefly.core.customer.sdk.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class ClientsFactory {
    private final com.firefly.core.customer.sdk.invoker.ApiClient customerApiClient;
    private final com.firefly.core.contract.sdk.invoker.ApiClient contractApiClient;
    private final com.firefly.common.product.sdk.invoker.ApiClient productApiClient;
    private final com.firefly.common.reference.master.data.sdk.invoker.ApiClient referenceMasterDataApiClient;


    @Autowired
    public ClientsFactory(SessionManagerClientFactoryProperties properties) {

        this.customerApiClient = new com.firefly.core.customer.sdk.invoker.ApiClient()
                .setBasePath(properties.getCustomerApiBasePath());

        this.contractApiClient = new com.firefly.core.contract.sdk.invoker.ApiClient()
                .setBasePath(properties.getContractApiBasePath());

        this.productApiClient = new com.firefly.common.product.sdk.invoker.ApiClient()
                .setBasePath(properties.getProductApiBasePath());

        this.referenceMasterDataApiClient = new com.firefly.common.reference.master.data.sdk.invoker.ApiClient()
                .setBasePath(properties.getReferenceMasterDataApiBasePath());
    }

    // ======= Customers =======

    @Bean
    public PartiesApi partiesApi() {
        return new PartiesApi(customerApiClient);
    }

    @Bean
    public NaturalPersonsApi naturalPersonsApi() {
        return new NaturalPersonsApi(customerApiClient);
    }

    @Bean
    public LegalEntitiesApi legalEntitiesApi() {
        return new LegalEntitiesApi(customerApiClient);
    }

    @Bean
    public PartyStatusesApi partyStatusesApi() {
        return new PartyStatusesApi(customerApiClient);
    }

    @Bean
    public PartyRelationshipsApi partyRelationshipsApi() {
        return new PartyRelationshipsApi(customerApiClient);
    }

    // ======= Contracts =======

    @Bean
    public ContractsApi contractsApi() {
        return new ContractsApi(contractApiClient);
    }

    @Bean
    public ContractPartiesApi contractPartiesApi() {
        return new ContractPartiesApi(contractApiClient);
    }

    @Bean
    public ContractStatusHistoryApi contractStatusHistoryApi() {
        return new ContractStatusHistoryApi(contractApiClient);
    }

    // ======= Products =======

    @Bean
    public ProductApi productApi() {
        return new ProductApi(productApiClient);
    }

    @Bean
    public ProductCategoryApi productCategoryApi() {
        return new ProductCategoryApi(productApiClient);
    }

    @Bean
    public ProductRelationshipApi productRelationshipApi() {
        return new ProductRelationshipApi(productApiClient);
    }


    // ======= Reference Master Data =======

    @Bean
    public ContractTypeApi contractTypeApi() {
        return new ContractTypeApi(referenceMasterDataApiClient);
    }

    @Bean
    public ContractRoleApi contractRoleApi() {
        return new ContractRoleApi(referenceMasterDataApiClient);
    }

}

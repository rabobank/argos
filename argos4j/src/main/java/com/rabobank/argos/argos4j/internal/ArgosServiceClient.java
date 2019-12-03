package com.rabobank.argos.argos4j.internal;

/*-
 * #%L
 * Argos Supply Chain Notary
 * %%
 * Copyright (C) 2019 Rabobank Nederland
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import com.fasterxml.jackson.annotation.JsonInclude;
import com.rabobank.argos.argos4j.Argos4jError;
import com.rabobank.argos.argos4j.Argos4jSettings;
import com.rabobank.argos.argos4j.internal.mapper.LinkMetaBlockMapper;
import com.rabobank.argos.argos4j.rest.api.ApiClient;
import com.rabobank.argos.argos4j.rest.api.client.LinkApi;
import com.rabobank.argos.argos4j.rest.api.client.SupplychainApi;
import com.rabobank.argos.argos4j.rest.api.model.RestLinkMetaBlock;
import com.rabobank.argos.argos4j.rest.api.model.RestSupplyChainItem;
import com.rabobank.argos.domain.link.LinkMetaBlock;
import feign.FeignException;
import lombok.AllArgsConstructor;
import org.mapstruct.factory.Mappers;

import java.util.List;


@AllArgsConstructor
public class ArgosServiceClient {

    private final Argos4jSettings settings;

    public void uploadLinkMetaBlockToService(LinkMetaBlock linkMetaBlock) {
        RestLinkMetaBlock restLinkMetaBlock = Mappers.getMapper(LinkMetaBlockMapper.class).convertToRestLinkMetaBlock(linkMetaBlock);

        ApiClient apiClient = new ApiClient().setBasePath(settings.getArgosServerBaseUrl());
        apiClient.getObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
        LinkApi linkApi = apiClient.buildClient(LinkApi.class);
        SupplychainApi supplychainApi = apiClient.buildClient(SupplychainApi.class);

        try {
            List<RestSupplyChainItem> supplyChains = supplychainApi.searchSupplyChains(settings.getSupplyChainName());
            if (supplyChains.isEmpty()) {
                throw new Argos4jError("supply chain id not found for:" + settings.getSupplyChainName());
            }
            linkApi.createLink(supplyChains.get(0).getId(), restLinkMetaBlock);
        } catch (FeignException e) {
            throw new Argos4jError(e.status() + " " + e.contentUTF8(), e);
        }
    }
}

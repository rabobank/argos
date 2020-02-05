/*
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rabobank.argos.argos4j.internal;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.rabobank.argos.argos4j.Argos4jError;
import com.rabobank.argos.argos4j.Argos4jSettings;
import com.rabobank.argos.argos4j.internal.mapper.LinkMetaBlockMapper;
import com.rabobank.argos.argos4j.rest.api.ApiClient;
import com.rabobank.argos.argos4j.rest.api.client.KeyApi;
import com.rabobank.argos.argos4j.rest.api.client.LinkApi;
import com.rabobank.argos.argos4j.rest.api.client.SupplychainApi;
import com.rabobank.argos.argos4j.rest.api.model.RestKeyPair;
import com.rabobank.argos.argos4j.rest.api.model.RestLinkMetaBlock;
import com.rabobank.argos.domain.link.LinkMetaBlock;
import feign.FeignException;
import org.mapstruct.factory.Mappers;


public class ArgosServiceClient {

    private final Argos4jSettings settings;
    private final ApiClient apiClient;

    public ArgosServiceClient(Argos4jSettings settings) {
        this.settings = settings;
        apiClient = new ApiClient().setBasePath(settings.getArgosServerBaseUrl());
        apiClient.getObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public void uploadLinkMetaBlockToService(LinkMetaBlock linkMetaBlock) {
        try {
            LinkApi linkApi = apiClient.buildClient(LinkApi.class);
            RestLinkMetaBlock restLinkMetaBlock = Mappers.getMapper(LinkMetaBlockMapper.class).convertToRestLinkMetaBlock(linkMetaBlock);
            linkApi.createLink(getSupplyChainId(), restLinkMetaBlock);
        } catch (FeignException e) {
            throw new Argos4jError(e.status() + " " + e.contentUTF8(), e);
        }
    }

    public RestKeyPair getKeyPair() {
        try {
            KeyApi keyApi = apiClient.buildClient(KeyApi.class);
            return keyApi.getKey(settings.getSigningKeyId());
        } catch (FeignException e) {
            throw new Argos4jError(e.status() + " " + e.contentUTF8(), e);
        }
    }

    private String getSupplyChainId() {
        SupplychainApi supplychainApi = apiClient.buildClient(SupplychainApi.class);
        return supplychainApi.getSupplyChainByPathToRoot(settings.getSupplyChainName(), settings.getPathToLabelRoot()).getId();
    }
}

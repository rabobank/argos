package com.rabobank.argos.argos4j.internal;


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

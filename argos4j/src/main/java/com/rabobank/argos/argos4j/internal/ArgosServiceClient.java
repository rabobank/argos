package com.rabobank.argos.argos4j.internal;

import com.google.api.client.http.HttpResponse;
import com.rabobank.argos.argos4j.Argos4jError;
import com.rabobank.argos.argos4j.Argos4jSettings;
import com.rabobank.argos.argos4j.internal.mapper.LinkMetaBlockMapper;
import com.rabobank.argos.argos4j.rest.api.ApiClient;
import com.rabobank.argos.argos4j.rest.api.client.LinkApi;
import com.rabobank.argos.argos4j.rest.api.client.SupplychainApi;
import com.rabobank.argos.argos4j.rest.api.model.RestLinkMetaBlock;
import com.rabobank.argos.argos4j.rest.api.model.RestSupplyChainItem;
import com.rabobank.argos.domain.model.LinkMetaBlock;
import lombok.AllArgsConstructor;
import org.mapstruct.factory.Mappers;

import java.io.IOException;
import java.util.List;

@AllArgsConstructor
public class ArgosServiceClient {

    private final Argos4jSettings settings;

    public void uploadLinkMetaBlockToService(LinkMetaBlock linkMetaBlock) {
        RestLinkMetaBlock restLinkMetaBlock = Mappers.getMapper(LinkMetaBlockMapper.class).convertToRestLinkMetaBlock(linkMetaBlock);
        LinkApi linkApi = new LinkApi(new ApiClient(settings.getArgosServerBaseUrl(), null, null, null));
        SupplychainApi supplychainApi = new SupplychainApi(new ApiClient(settings.getArgosServerBaseUrl(), null, null, null));
        try {
            List<RestSupplyChainItem> supplyChains = supplychainApi.searchSupplyChains(settings.getSupplyChainName());

            if (supplyChains.isEmpty()) {
                throw new Argos4jError("supply chain id not found for:" + settings.getSupplyChainName());
            }

            HttpResponse response = linkApi.createLinkForHttpResponse(supplyChains.get(0).getId(), restLinkMetaBlock);
            if (response.getStatusCode() != 204) {
                throw new Argos4jError("service returned code " + response.getStatusCode() + " message: " + response.getStatusMessage());
            }
        } catch (IOException e) {
            throw new Argos4jError(e.getMessage(), e);
        }
    }
}

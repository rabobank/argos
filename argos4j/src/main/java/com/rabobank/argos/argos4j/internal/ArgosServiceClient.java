package com.rabobank.argos.argos4j.internal;

import com.google.api.client.http.HttpResponse;
import com.rabobank.argos.argos4j.Argos4jError;
import com.rabobank.argos.argos4j.Argos4jSettings;
import com.rabobank.argos.argos4j.internal.mapper.LinkMetaBlockMapper;
import com.rabobank.argos.argos4j.rest.api.ApiClient;
import com.rabobank.argos.argos4j.rest.api.client.LinkApi;
import com.rabobank.argos.argos4j.rest.api.model.RestLinkMetaBlock;
import com.rabobank.argos.domain.model.LinkMetaBlock;
import lombok.AllArgsConstructor;

import java.io.IOException;

@AllArgsConstructor
public class ArgosServiceClient {

    private final Argos4jSettings settings;

    public void uploadLinkMetaBlockToService(LinkMetaBlock linkMetaBlock) {
        RestLinkMetaBlock restLinkMetaBlock = LinkMetaBlockMapper.INSTANCE.convertToRestLinkMetaBlock(linkMetaBlock);
        LinkApi linkApi = new LinkApi(new ApiClient(settings.getBaseUrl(), null, null, null));
        try {
            HttpResponse response = linkApi.createLinkForHttpResponse(settings.getSupplyChainId(), restLinkMetaBlock);
            if (response.getStatusCode() != 204) {
                throw new Argos4jError("service returned code " + response.getStatusCode() + " message: " + response.getStatusMessage());
            }
        } catch (IOException e) {
            throw new Argos4jError(e.getMessage(), e);
        }
    }
}

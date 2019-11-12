package com.rabobank.argos.argos4j.internal;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.rabobank.argos.argos4j.Argos4jError;
import com.rabobank.argos.argos4j.Argos4jSettings;
import com.rabobank.argos.argos4j.internal.mapper.LinkMetaBlockMapper;
import com.rabobank.argos.argos4j.rest.api.ApiClient;
import com.rabobank.argos.argos4j.rest.api.client.LinkApi;
import com.rabobank.argos.argos4j.rest.api.model.RestLinkMetaBlock;
import com.rabobank.argos.domain.model.LinkMetaBlock;
import feign.FeignException;
import lombok.AllArgsConstructor;
import org.mapstruct.factory.Mappers;



@AllArgsConstructor
public class ArgosServiceClient {

    private final Argos4jSettings settings;

    public void uploadLinkMetaBlockToService(LinkMetaBlock linkMetaBlock) {
        RestLinkMetaBlock restLinkMetaBlock = Mappers.getMapper(LinkMetaBlockMapper.class).convertToRestLinkMetaBlock(linkMetaBlock);

        ApiClient apiClient = new ApiClient().setBasePath(settings.getArgosServerBaseUrl());
        apiClient.getObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
        LinkApi linkApi = apiClient.buildClient(LinkApi.class);

        try {
            linkApi.createLink(settings.getSupplyChainId(), restLinkMetaBlock);
        } catch (FeignException e) {
            throw new Argos4jError(e.status() + " " + e.contentUTF8(), e);
        }

    }
}

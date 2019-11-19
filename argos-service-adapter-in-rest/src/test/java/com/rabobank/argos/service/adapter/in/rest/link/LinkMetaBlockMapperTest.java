package com.rabobank.argos.service.adapter.in.rest.link;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabobank.argos.domain.model.LinkMetaBlock;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestLinkMetaBlock;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;

class LinkMetaBlockMapperTest {

    private LinkMetaBlockMapper converter;
    private ObjectMapper mapper;
    private String linkJson;

    @BeforeEach
    void setUp() throws IOException {
        converter = Mappers.getMapper(LinkMetaBlockMapper.class);
        mapper = new ObjectMapper();
        linkJson = IOUtils.toString(LinkMetaBlockMapperTest.class.getResourceAsStream("/link.json"), "UTF-8");
    }

    @Test
    void convertFromRestLinkMetaBlock() throws JsonProcessingException, JSONException {
        LinkMetaBlock link = converter.convertFromRestLinkMetaBlock(mapper.readValue(linkJson, RestLinkMetaBlock.class));
        JSONAssert.assertEquals(linkJson, mapper.writeValueAsString(converter.convertToRestLinkMetaBlock(link)), true);
    }
}
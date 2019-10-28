package com.rabobank.argos.service.adapter.in.rest.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabobank.argos.domain.model.LinkMetaBlock;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestLinkMetaBlock;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {LinkMetaBlockMapperImpl.class, SignatureMapperImpl.class})
class LinkMetaBlockMapperTest {

    @Autowired
    private LinkMetaBlockMapper converter;

    private ObjectMapper mapper;
    private String linkJson;

    @BeforeEach
    void setUp() throws IOException {
        mapper = new ObjectMapper();
        linkJson = IOUtils.toString(LinkMetaBlockMapperTest.class.getResourceAsStream("/link.json"), "UTF-8");
    }

    @Test
    void convertFromRestLinkMetaBlock() throws JsonProcessingException {
        LinkMetaBlock link = converter.convertFromRestLinkMetaBlock(mapper.readValue(linkJson, RestLinkMetaBlock.class));
        assertEquals(linkJson, mapper.writeValueAsString(converter.convertToRestLinkMetaBlock(link)));
    }
}
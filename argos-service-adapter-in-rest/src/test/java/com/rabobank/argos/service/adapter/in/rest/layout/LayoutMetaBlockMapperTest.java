package com.rabobank.argos.service.adapter.in.rest.layout;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabobank.argos.domain.model.LayoutMetaBlock;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestLayoutMetaBlock;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasLength;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {LayoutMetaBlockMapperImpl.class, RuleMapperImpl.class, MatchRuleMapperImpl.class, StepMapperImpl.class})
class LayoutMetaBlockMapperTest {

    @Autowired
    private LayoutMetaBlockMapper converter;
    private ObjectMapper mapper;
    private String layoutJson;

    @BeforeEach
    void setUp() throws IOException {

        mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        layoutJson = IOUtils.toString(getClass().getResourceAsStream("/layout.json"), UTF_8);
    }

    @Test
    void convertFromRestLayoutMetaBlock() throws JsonProcessingException, JSONException {
        LayoutMetaBlock layoutMetaBlock = converter.convertFromRestLayoutMetaBlock(mapper.readValue(layoutJson, RestLayoutMetaBlock.class));
        RestLayoutMetaBlock restLayoutMetaBlock = converter.convertToRestLayoutMetaBlock(layoutMetaBlock);
        assertThat(restLayoutMetaBlock.getId(), hasLength(36));
        restLayoutMetaBlock.setId(null);
        JSONAssert.assertEquals(layoutJson, mapper.writeValueAsString(restLayoutMetaBlock), true);
    }

}
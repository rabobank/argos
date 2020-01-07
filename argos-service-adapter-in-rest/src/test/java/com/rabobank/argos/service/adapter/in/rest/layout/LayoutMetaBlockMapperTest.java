/*
 * Copyright (C) 2020 Rabobank Nederland
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
package com.rabobank.argos.service.adapter.in.rest.layout;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabobank.argos.domain.layout.LayoutMetaBlock;
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

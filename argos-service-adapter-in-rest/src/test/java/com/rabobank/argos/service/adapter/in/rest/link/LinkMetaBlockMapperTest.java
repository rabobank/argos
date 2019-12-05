package com.rabobank.argos.service.adapter.in.rest.link;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabobank.argos.domain.link.LinkMetaBlock;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestLinkMetaBlock;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

class LinkMetaBlockMapperTest {

    private LinkMetaBlockMapper converter;
    private ObjectMapper mapper;
    private String linkJson;

    @BeforeEach
    void setUp() throws IOException {
        converter = Mappers.getMapper(LinkMetaBlockMapper.class);
        mapper = new ObjectMapper();
        linkJson = IOUtils.toString(LinkMetaBlockMapperTest.class.getResourceAsStream("/link.json"), StandardCharsets.UTF_8);
    }

    @Test
    void convertFromRestLinkMetaBlock() throws JsonProcessingException, JSONException {
        LinkMetaBlock link = converter.convertFromRestLinkMetaBlock(mapper.readValue(linkJson, RestLinkMetaBlock.class));
        JSONAssert.assertEquals(linkJson, mapper.writeValueAsString(converter.convertToRestLinkMetaBlock(link)), true);
    }
}

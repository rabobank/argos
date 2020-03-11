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

import com.rabobank.argos.argos4j.internal.mapper.RestMapper;
import com.rabobank.argos.argos4j.rest.api.model.RestLinkMetaBlock;
import com.rabobank.argos.domain.link.Link;
import com.rabobank.argos.domain.link.LinkMetaBlock;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class RestMapperTest {

    @Test
    void testOnNullValues() {
        RestMapper mapper = Mappers.getMapper(RestMapper.class);
        LinkMetaBlock linkMetaBlock = LinkMetaBlock.builder().link(Link.builder().build()).build();
        RestLinkMetaBlock metablock = mapper.convertToRestLinkMetaBlock(linkMetaBlock);

        assertNotNull(metablock.getLink().getCommand());
        assertNotNull(metablock.getLink().getMaterials());
        assertNotNull(metablock.getLink().getProducts());

    }
}

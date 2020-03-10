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
package com.rabobank.argos.service.adapter.in.rest.supplychain;

import com.rabobank.argos.service.domain.security.LabelIdCheckParam;
import com.rabobank.argos.service.domain.supplychain.SupplyChainRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupplyChainLabelIdExtractorTest {

    private static final String SUPPLY_CHAIN_ID = "supplyChainId";
    private static final String LABEL_ID = "labelId";

    @Mock
    private SupplyChainRepository supplyChainRepository;

    @Mock
    private LabelIdCheckParam checkParam;

    private SupplyChainLabelIdExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new SupplyChainLabelIdExtractor(supplyChainRepository);
    }

    @Test
    void extractLabelId() {
        when(supplyChainRepository.findParentLabelIdBySupplyChainId(SUPPLY_CHAIN_ID)).thenReturn(Optional.of(LABEL_ID));
        assertThat(extractor.extractLabelId(checkParam, SUPPLY_CHAIN_ID), is(Optional.of(LABEL_ID)));
    }
}
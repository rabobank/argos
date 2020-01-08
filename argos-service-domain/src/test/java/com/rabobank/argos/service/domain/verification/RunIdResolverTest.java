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
package com.rabobank.argos.service.domain.verification;

import com.rabobank.argos.domain.layout.DestinationType;
import com.rabobank.argos.domain.layout.Layout;
import com.rabobank.argos.domain.layout.LayoutMetaBlock;
import com.rabobank.argos.domain.layout.LayoutSegment;
import com.rabobank.argos.domain.layout.MatchFilter;
import com.rabobank.argos.domain.link.Artifact;
import com.rabobank.argos.domain.link.Link;
import com.rabobank.argos.domain.link.LinkMetaBlock;
import com.rabobank.argos.service.domain.link.LinkMetaBlockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RunIdResolverTest {
    public static final String STEP_NAME = "stepName";
    public static final String ARTIFACT_JAVA = "/artifact.java";
    public static final String RUN_ID = "run_id";
    public static final String HASH = "hash";
    private static final String SEGMENT_NAME = "segmentName";
    private static final String SUPPLY_CHAIN_ID = "supplyChainId";

    @Mock
    private LayoutMetaBlock layoutMetaBlock;

    private List<Artifact> productsToVerify;

    @Mock
    private LinkMetaBlockRepository linkMetaBlockRepository;

    private RunIdResolver resolver;

    @Mock
    private Layout layout;

    @Mock
    private LayoutSegment layoutSegment;

    @BeforeEach
    public void setup() {

        withLayout(DestinationType.PRODUCTS);
        productsToVerify = singletonList(Artifact.builder().hash(HASH).uri(ARTIFACT_JAVA).build());

        resolver = new RunIdResolver(linkMetaBlockRepository);
    }

    private void withLayout(DestinationType destinationType) {
        when(layoutMetaBlock.getLayout()).thenReturn(layout);
        when(layout.getLayoutSegments()).thenReturn(List.of(layoutSegment));
        when(layoutSegment.getName()).thenReturn(SEGMENT_NAME);
        when(layoutMetaBlock.getSupplyChainId()).thenReturn(SUPPLY_CHAIN_ID);

        when(layout.getExpectedEndProducts())
                .thenReturn(singletonList(MatchFilter.builder()
                        .destinationStepName(STEP_NAME)
                        .pattern(ARTIFACT_JAVA)
                        .destinationSegmentName(SEGMENT_NAME)
                        .destinationType(destinationType)
                        .build()));
    }

    @Test
    void getRunIdWithValidProductsShouldReturnResult() {

        when(linkMetaBlockRepository.findBySupplyChainAndSegmentNameAndStepNameAndProductHashes(SUPPLY_CHAIN_ID, SEGMENT_NAME, STEP_NAME, List.of(HASH)))
                .thenReturn(singletonList(LinkMetaBlock.builder().link(Link.builder().runId(RUN_ID).build()).build()));
        List<RunIdsWithSegment> runIdsWithSegments = resolver.getRunIdPerSegment(layoutMetaBlock, productsToVerify);
        assertThat(runIdsWithSegments.get(0).getRunIds(), contains(RUN_ID));
        assertThat(runIdsWithSegments.get(0).getSegment(), sameInstance(layoutSegment));
    }

    @Test
    void getRunIdWithInValidProductsShouldReturnEmpty() {
        when(linkMetaBlockRepository.findBySupplyChainAndSegmentNameAndStepNameAndProductHashes(SUPPLY_CHAIN_ID, SEGMENT_NAME, STEP_NAME, List.of(HASH)))
                .thenReturn(emptyList());
        List<RunIdsWithSegment> runIdsWithSegments = resolver.getRunIdPerSegment(layoutMetaBlock, productsToVerify);
        assertThat(runIdsWithSegments.get(0).getRunIds(), empty());
    }

    @Test
    void getRunIdWithValidMaterialsShouldReturnResult() {
        withLayout(DestinationType.MATERIALS);
        when(linkMetaBlockRepository.findBySupplyChainAndSegmentNameAndStepNameAndMaterialHash(SUPPLY_CHAIN_ID, SEGMENT_NAME, STEP_NAME, List.of(HASH)))
                .thenReturn(singletonList(LinkMetaBlock
                        .builder().link(Link.builder().runId(RUN_ID).build()).build()));
        List<RunIdsWithSegment> runIdsWithSegments = resolver.getRunIdPerSegment(layoutMetaBlock, productsToVerify);
        assertThat(runIdsWithSegments.get(0).getRunIds(), contains(RUN_ID));
        assertThat(runIdsWithSegments.get(0).getSegment(), sameInstance(layoutSegment));
    }
}

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
import com.rabobank.argos.domain.layout.Step;
import com.rabobank.argos.domain.layout.rule.MatchRule;
import com.rabobank.argos.domain.layout.rule.Rule;
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
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerificationContextsProviderTest {

    private static final String STEP_NAME_1 = "stepName1";
    private static final String STEP_NAME_2 = "stepName2";
    private static final String STEP_NAME_3 = "stepName3";
    private static final String SUPPLY_CHAIN_ID = "supplyChainId";
    private static final String SEGMENT_NAME_1 = "segmentName1";
    private static final String SEGMENT_NAME_2 = "segmentName2";
    private static final String SEGMENT_NAME_3 = "segmentName3";
    private static final String RUN_ID_1 = "runId1";
    private static final String RUN_ID_2 = "runId2";
    private static final String RUN_ID_3 = "runId3";

    @Mock
    private LinkMetaBlockRepository linkMetaBlockRepository;

    @Mock
    private LayoutMetaBlock layoutMetaBlock;

    @Mock
    private Layout layout;

    @Mock
    private LayoutSegment layoutSegment1;

    @Mock
    private LayoutSegment layoutSegment2;

    @Mock
    private LayoutSegment layoutSegment3;

    @Mock
    private Step step1;

    @Mock
    private Step step2;

    @Mock
    private Step step3;

    private List<Artifact> artifacts;

    private List<MatchFilter> matchFilters;

    private List<Rule> matchRulesForProductsStep1;

    private LinkMetaBlock linkMetaBlockFromInput;

    private LinkMetaBlock linkMetaBlockFromRunId1_1;

    private LinkMetaBlock linkMetaBlockFromRunId1_2;

    private VerificationContextsProvider verificationContextsProvider;

    private LinkMetaBlock linkMetaBlockFromInput2;

    private LinkMetaBlock linkMetaBlockFromMatchRuleSegment2_1;

    private LinkMetaBlock linkMetaBlockFromMatchRuleSegment2_2;

    private LinkMetaBlock linkMetaBlockFromMatchRuleSegment3;

    @BeforeEach
    void setup() {
        createMatchFilters();
        createArtifacts();

        linkMetaBlockFromInput = LinkMetaBlock
                .builder()
                .supplyChainId(SUPPLY_CHAIN_ID)
                .link(Link.builder()
                        .layoutSegmentName(SEGMENT_NAME_1)
                        .stepName(STEP_NAME_1)
                        .runId(RUN_ID_1)
                        .materials(artifacts)
                        .products(artifacts)
                        .build()
                ).build();

        linkMetaBlockFromInput2 = LinkMetaBlock
                .builder()
                .supplyChainId(SUPPLY_CHAIN_ID)
                .link(Link.builder()
                        .layoutSegmentName(SEGMENT_NAME_1)
                        .stepName(STEP_NAME_1)
                        .runId(RUN_ID_1)
                        .materials(artifacts)
                        .products(artifacts)
                        .command(singletonList("cmd"))
                        .build()
                ).build();

        linkMetaBlockFromMatchRuleSegment2_1 = LinkMetaBlock
                .builder()
                .supplyChainId(SUPPLY_CHAIN_ID)
                .link(Link.builder()
                        .layoutSegmentName(SEGMENT_NAME_2)
                        .stepName(STEP_NAME_2)
                        .runId(RUN_ID_2)
                        .materials(artifacts)
                        .products(artifacts)
                        .build()
                ).build();

        linkMetaBlockFromMatchRuleSegment3 = LinkMetaBlock
                .builder()
                .supplyChainId(SUPPLY_CHAIN_ID)
                .link(Link.builder()
                        .layoutSegmentName(SEGMENT_NAME_3)
                        .stepName(STEP_NAME_3)
                        .runId(RUN_ID_3)
                        .materials(artifacts)
                        .products(artifacts)
                        .build()
                ).build();

        linkMetaBlockFromRunId1_1 = LinkMetaBlock
                .builder()
                .supplyChainId(SUPPLY_CHAIN_ID)
                .link(Link.builder()
                        .layoutSegmentName(SEGMENT_NAME_1)
                        .stepName(STEP_NAME_2)
                        .runId(RUN_ID_1)
                        .materials(artifacts)
                        .products(artifacts)
                        .build()
                ).build();

        linkMetaBlockFromRunId1_2 = LinkMetaBlock
                .builder()
                .supplyChainId(SUPPLY_CHAIN_ID)
                .link(Link.builder()
                        .layoutSegmentName(SEGMENT_NAME_1)
                        .stepName(STEP_NAME_2)
                        .runId(RUN_ID_1)
                        .materials(artifacts)
                        .command(singletonList("cmd"))
                        .products(artifacts)
                        .build()
                ).build();

        verificationContextsProvider = new VerificationContextsProviderImpl(linkMetaBlockRepository);
    }

    private void createArtifacts() {
        Artifact artifact = Artifact
                .builder()
                .hash("hash")
                .uri("path/artifact.jar")
                .build();
        artifacts = List.of(artifact);
    }

    private void createMatchFilters() {
        MatchFilter matchFilterProduct = MatchFilter.builder()
                .destinationType(DestinationType.PRODUCTS)
                .destinationStepName(STEP_NAME_1)
                .destinationSegmentName(SEGMENT_NAME_1)
                .pattern("**/*.jar")
                .build();

        MatchFilter matchFilterMaterials = MatchFilter.builder()
                .destinationType(DestinationType.MATERIALS)
                .destinationStepName(STEP_NAME_1)
                .destinationSegmentName(SEGMENT_NAME_1)
                .pattern("**/*.jar")
                .build();

        matchFilters = List.of(matchFilterProduct, matchFilterMaterials);
    }

    private void createMatchFilterMaterials() {

        MatchFilter matchFilterMaterials = MatchFilter.builder()
                .destinationType(DestinationType.MATERIALS)
                .destinationStepName(STEP_NAME_1)
                .destinationSegmentName(SEGMENT_NAME_1)
                .pattern("**/*.jar")
                .build();

        matchFilters = List.of(matchFilterMaterials);
    }

    @Test
    void createPossibleVerificationContextsWithMultipleStepsAndMultipleEqualLinkSets() {
        setupMocksForMultipleSteps();
        List<VerificationContext> verificationContexts = verificationContextsProvider.createPossibleVerificationContexts(layoutMetaBlock, artifacts);
        assertThat(verificationContexts, hasSize(2));
    }

    @Test
    void createPossibleVerificationContextsSingleStepAndMultipleEqualLinkSets() {
        setupMocksForSingleStep();
        List<VerificationContext> verificationContexts = verificationContextsProvider.createPossibleVerificationContexts(layoutMetaBlock, artifacts);
        assertThat(verificationContexts, hasSize(2));
    }


    @Test
    void createPossibleVerificationContextsWithNonMatchingArtifacts() {
        when(layoutMetaBlock.expectedEndProducts()).thenReturn(matchFilters);
        when(layoutMetaBlock.allLayoutSegmentsAreResolved(any())).thenReturn(true);
        Artifact wrongArtifact = Artifact.builder().uri("/wrong.exe").hash("hash").build();
        List<VerificationContext> verificationContexts = verificationContextsProvider.createPossibleVerificationContexts(layoutMetaBlock, singletonList(wrongArtifact));
        assertThat(verificationContexts, hasSize(0));
    }

    @Test
    void createPossibleVerificationContextsWithMatchinMaterialArtifacts() {
        setupMocksForMultipleSteps();
        createMatchFilterMaterials();
        List<VerificationContext> verificationContexts = verificationContextsProvider.createPossibleVerificationContexts(layoutMetaBlock, artifacts);
        assertThat(verificationContexts, hasSize(2));
    }

    @Test
    void createPossibleVerificationContextsWithMultipleSegmentsShouldReturnOneVerificationContext() {
        setupMocksForMultipleSegments();
        List<VerificationContext> verificationContexts = verificationContextsProvider.createPossibleVerificationContexts(layoutMetaBlock, artifacts);
        assertThat(verificationContexts, hasSize(1));
        VerificationContext verificationContext = verificationContexts.iterator().next();
        assertThat(verificationContext.getLinkMetaBlocks(), hasSize(3));
        assertThat(verificationContext.getLinkMetaBlocks().contains(linkMetaBlockFromInput), is(true));
        assertThat(verificationContext.getLinkMetaBlocks().contains(linkMetaBlockFromMatchRuleSegment2_1), is(true));
        assertThat(verificationContext.getLinkMetaBlocks().contains(linkMetaBlockFromMatchRuleSegment3), is(true));
    }

    @Test
        // with hop mean that from segment one upstream segment 2 is resolved and then from segment 2 segment 3 is resolved
    void createPossibleVerificationContextsWithMultipleSegmentsWithHopShouldReturnOneVerificationContext() {
        setupMocksForMultipleSegmentsWithHop();
        List<VerificationContext> verificationContexts = verificationContextsProvider.createPossibleVerificationContexts(layoutMetaBlock, artifacts);
        assertThat(verificationContexts, hasSize(1));
        VerificationContext verificationContext = verificationContexts.iterator().next();
        assertThat(verificationContext.getLinkMetaBlocks(), hasSize(3));
        assertThat(verificationContext.getLinkMetaBlocks().contains(linkMetaBlockFromInput), is(true));
        assertThat(verificationContext.getLinkMetaBlocks().contains(linkMetaBlockFromMatchRuleSegment2_1), is(true));
        assertThat(verificationContext.getLinkMetaBlocks().contains(linkMetaBlockFromMatchRuleSegment3), is(true));
    }

    @Test
    void createPossibleVerificationContextsWithMultipleSegmentsWithHopShouldReturnFourVerificationContexts() {
        setupMocksForMultipleSegmentsWithHopMultipleSets();
        List<VerificationContext> verificationContexts = verificationContextsProvider.createPossibleVerificationContexts(layoutMetaBlock, artifacts);
        assertThat(verificationContexts, hasSize(4));
    }

    private void setupMocksForSingleStep() {
        when(layoutMetaBlock.getLayout()).thenReturn(layout);
        when(layout.getLayoutSegments()).thenReturn(singletonList(layoutSegment1));
        when(layoutSegment1.getSteps()).thenReturn(singletonList(step1));
        when(step1.getStepName()).thenReturn(STEP_NAME_1);
        when(layoutMetaBlock.expectedEndProducts()).thenReturn(matchFilters);
        when(linkMetaBlockRepository
                .findBySupplyChainAndSegmentNameAndStepNameAndProductHashes(any(), any(), any(), any()))
                .thenReturn(List.of(linkMetaBlockFromInput, linkMetaBlockFromInput2));
        when(layoutMetaBlock.allLayoutSegmentsAreResolved(any())).thenReturn(true);
    }

    void setupMocksForMultipleSteps() {
        when(layoutMetaBlock.getSupplyChainId()).thenReturn(SUPPLY_CHAIN_ID);
        when(layoutMetaBlock.getLayout()).thenReturn(layout);
        when(layout.getLayoutSegments()).thenReturn(singletonList(layoutSegment1));
        when(layoutSegment1.getSteps()).thenReturn(singletonList(step1));
        when(step1.getStepName()).thenReturn(STEP_NAME_1);
        when(layoutMetaBlock.expectedEndProducts()).thenReturn(matchFilters);
        when(linkMetaBlockRepository
                .findBySupplyChainAndSegmentNameAndStepNameAndProductHashes(any(), any(), any(), any()))
                .thenReturn(List.of(linkMetaBlockFromInput));

        when(linkMetaBlockRepository
                .findBySupplyChainAndSegmentNameAndStepNameAndMaterialHash(any(), any(), any(), any()))
                .thenReturn(List.of(linkMetaBlockFromInput));

        when(linkMetaBlockRepository.findByRunId(any(), any(), any(), any()))
                .thenReturn(List.of(linkMetaBlockFromRunId1_1, linkMetaBlockFromRunId1_2));
        when(layoutMetaBlock.allLayoutSegmentsAreResolved(any())).thenReturn(true);
    }

    void setupMocksForMultipleSegments() {
        MatchRule matchRule4StepOne2StepTwo = MatchRule.builder()
                .destinationStepName(STEP_NAME_2).pattern("**/*.jar")
                .destinationType(DestinationType.PRODUCTS)
                .destinationSegmentName(SEGMENT_NAME_2)
                .build();

        MatchRule matchRule4StepOne2StepThree = MatchRule.builder()
                .destinationStepName(STEP_NAME_3).pattern("**/*.jar")
                .destinationType(DestinationType.PRODUCTS)
                .destinationSegmentName(SEGMENT_NAME_3)
                .build();
        matchRulesForProductsStep1 = List.of(matchRule4StepOne2StepTwo, matchRule4StepOne2StepThree);
        when(layoutMetaBlock.getSupplyChainId()).thenReturn(SUPPLY_CHAIN_ID);
        when(layoutMetaBlock.getLayout()).thenReturn(layout);
        when(layout.getLayoutSegments()).thenReturn(List.of(layoutSegment1, layoutSegment2, layoutSegment3));
        when(layoutSegment1.getSteps()).thenReturn(singletonList(step1));
        when(layoutSegment1.getName()).thenReturn(SEGMENT_NAME_1);

        when(layoutSegment2.getSteps()).thenReturn(singletonList(step2));
        when(layoutSegment2.getName()).thenReturn(SEGMENT_NAME_2);

        when(layoutSegment2.getName()).thenReturn(SEGMENT_NAME_3);
        when(layoutSegment3.getSteps()).thenReturn(singletonList(step3));
        when(step1.getExpectedProducts()).thenReturn(matchRulesForProductsStep1);
        when(step2.getExpectedProducts()).thenReturn(emptyList());
        when(step1.getStepName()).thenReturn(STEP_NAME_1);
        when(step2.getStepName()).thenReturn(STEP_NAME_2);
        when(step3.getStepName()).thenReturn(STEP_NAME_3);
        when(layoutMetaBlock.expectedEndProducts()).thenReturn(matchFilters);

        when(linkMetaBlockRepository.findBySupplyChainAndSegmentNameAndStepNameAndMaterialHash(eq(SUPPLY_CHAIN_ID), eq(SEGMENT_NAME_1), eq(STEP_NAME_1), any()))
                .thenReturn(List.of(linkMetaBlockFromInput));
        when(linkMetaBlockRepository
                .findBySupplyChainAndSegmentNameAndStepNameAndProductHashes(eq(SUPPLY_CHAIN_ID), eq(SEGMENT_NAME_1), eq(STEP_NAME_1), any()))
                .thenReturn(List.of(linkMetaBlockFromInput));

        when(linkMetaBlockRepository
                .findBySupplyChainAndSegmentNameAndStepNameAndProductHashes(eq(SUPPLY_CHAIN_ID), eq(SEGMENT_NAME_2), eq(STEP_NAME_2), any()))
                .thenReturn(List.of(linkMetaBlockFromMatchRuleSegment2_1));

        when(linkMetaBlockRepository
                .findBySupplyChainAndSegmentNameAndStepNameAndProductHashes(eq(SUPPLY_CHAIN_ID), eq(SEGMENT_NAME_3), eq(STEP_NAME_3), any()))
                .thenReturn(List.of(linkMetaBlockFromMatchRuleSegment3));

        when(linkMetaBlockRepository.findByRunId(any(), any(), any(), any()))
                .thenReturn(emptyList());
    }

    private void setupMocksForMultipleSegmentsWithHopMultipleSets() {

        linkMetaBlockFromMatchRuleSegment2_2 = LinkMetaBlock
                .builder()
                .supplyChainId(SUPPLY_CHAIN_ID)
                .link(Link.builder()
                        .layoutSegmentName(SEGMENT_NAME_2)
                        .stepName(STEP_NAME_2)
                        .runId(RUN_ID_2)
                        .materials(artifacts)
                        .products(artifacts)
                        .command(singletonList("cmd"))
                        .build()
                ).build();

        MatchRule matchRule4StepOne2StepTwo = MatchRule.builder()
                .destinationStepName(STEP_NAME_2).pattern("**/*.jar")
                .destinationType(DestinationType.PRODUCTS)
                .destinationSegmentName(SEGMENT_NAME_2)
                .build();

        MatchRule matchRule4StepTwo2StepThree = MatchRule.builder()
                .destinationStepName(STEP_NAME_3).pattern("**/*.jar")
                .destinationType(DestinationType.PRODUCTS)
                .destinationSegmentName(SEGMENT_NAME_3)
                .build();

        matchRulesForProductsStep1 = List.of(matchRule4StepOne2StepTwo);
        List<Rule> matchRulesForProductsStep2 = List.of(matchRule4StepTwo2StepThree);

        when(layoutMetaBlock.getSupplyChainId()).thenReturn(SUPPLY_CHAIN_ID);
        when(layoutMetaBlock.getLayout()).thenReturn(layout);
        when(layout.getLayoutSegments()).thenReturn(List.of(layoutSegment1, layoutSegment2, layoutSegment3));

        when(layoutSegment1.getSteps()).thenReturn(singletonList(step1));
        when(layoutSegment1.getName()).thenReturn(SEGMENT_NAME_1);

        when(layoutSegment2.getSteps()).thenReturn(singletonList(step2));
        when(layoutSegment2.getName()).thenReturn(SEGMENT_NAME_2);

        when(layoutSegment3.getSteps()).thenReturn(singletonList(step3));
        when(step1.getExpectedProducts()).thenReturn(matchRulesForProductsStep1);
        when(step2.getExpectedProducts()).thenReturn(matchRulesForProductsStep2);
        when(step1.getStepName()).thenReturn(STEP_NAME_1);
        when(step2.getStepName()).thenReturn(STEP_NAME_2);
        when(step3.getStepName()).thenReturn(STEP_NAME_3);
        when(layoutMetaBlock.expectedEndProducts()).thenReturn(matchFilters);

        when(linkMetaBlockRepository.findBySupplyChainAndSegmentNameAndStepNameAndMaterialHash(eq(SUPPLY_CHAIN_ID), eq(SEGMENT_NAME_1), eq(STEP_NAME_1), any()))
                .thenReturn(List.of(linkMetaBlockFromInput));

        when(linkMetaBlockRepository.findBySupplyChainAndSegmentNameAndStepNameAndProductHashes(eq(SUPPLY_CHAIN_ID), eq(SEGMENT_NAME_1), eq(STEP_NAME_1), any()))
                .thenReturn(List.of(linkMetaBlockFromInput, linkMetaBlockFromInput2));

        when(linkMetaBlockRepository.findBySupplyChainAndSegmentNameAndStepNameAndProductHashes(eq(SUPPLY_CHAIN_ID), eq(SEGMENT_NAME_2), eq(STEP_NAME_2), any()))
                .thenReturn(List.of(linkMetaBlockFromMatchRuleSegment2_1, linkMetaBlockFromMatchRuleSegment2_2));

        when(linkMetaBlockRepository
                .findBySupplyChainAndSegmentNameAndStepNameAndProductHashes(eq(SUPPLY_CHAIN_ID), eq(SEGMENT_NAME_3), eq(STEP_NAME_3), any()))
                .thenReturn(List.of(linkMetaBlockFromMatchRuleSegment3));

        when(linkMetaBlockRepository.findByRunId(any(), any(), any(), any()))
                .thenReturn(emptyList());
    }


    private void setupMocksForMultipleSegmentsWithHop() {

        MatchRule matchRule4StepOne2StepTwo = MatchRule.builder()
                .destinationStepName(STEP_NAME_2).pattern("**/*.jar")
                .destinationType(DestinationType.PRODUCTS)
                .destinationSegmentName(SEGMENT_NAME_2)
                .build();

        MatchRule matchRule4StepTwo2StepThree = MatchRule.builder()
                .destinationStepName(STEP_NAME_3).pattern("**/*.jar")
                .destinationType(DestinationType.PRODUCTS)
                .destinationSegmentName(SEGMENT_NAME_3)
                .build();

        matchRulesForProductsStep1 = List.of(matchRule4StepOne2StepTwo);
        List<Rule> matchRulesForProductsStep2 = List.of(matchRule4StepTwo2StepThree);

        when(layoutMetaBlock.getSupplyChainId()).thenReturn(SUPPLY_CHAIN_ID);
        when(layoutMetaBlock.getLayout()).thenReturn(layout);
        when(layout.getLayoutSegments()).thenReturn(List.of(layoutSegment1, layoutSegment2, layoutSegment3));

        when(layoutSegment1.getSteps()).thenReturn(singletonList(step1));
        when(layoutSegment1.getName()).thenReturn(SEGMENT_NAME_1);

        when(layoutSegment2.getSteps()).thenReturn(singletonList(step2));
        when(layoutSegment2.getName()).thenReturn(SEGMENT_NAME_2);

        when(layoutSegment3.getSteps()).thenReturn(singletonList(step3));
        when(step1.getExpectedProducts()).thenReturn(matchRulesForProductsStep1);
        when(step2.getExpectedProducts()).thenReturn(matchRulesForProductsStep2);
        when(step1.getStepName()).thenReturn(STEP_NAME_1);
        when(step2.getStepName()).thenReturn(STEP_NAME_2);
        when(step3.getStepName()).thenReturn(STEP_NAME_3);
        when(layoutMetaBlock.expectedEndProducts()).thenReturn(matchFilters);

        when(linkMetaBlockRepository.findBySupplyChainAndSegmentNameAndStepNameAndMaterialHash(eq(SUPPLY_CHAIN_ID), eq(SEGMENT_NAME_1), eq(STEP_NAME_1), any()))
                .thenReturn(List.of(linkMetaBlockFromInput));
        when(linkMetaBlockRepository
                .findBySupplyChainAndSegmentNameAndStepNameAndProductHashes(eq(SUPPLY_CHAIN_ID), eq(SEGMENT_NAME_1), eq(STEP_NAME_1), any()))
                .thenReturn(List.of(linkMetaBlockFromInput));

        when(linkMetaBlockRepository
                .findBySupplyChainAndSegmentNameAndStepNameAndProductHashes(eq(SUPPLY_CHAIN_ID), eq(SEGMENT_NAME_2), eq(STEP_NAME_2), any()))
                .thenReturn(List.of(linkMetaBlockFromMatchRuleSegment2_1));

        when(linkMetaBlockRepository
                .findBySupplyChainAndSegmentNameAndStepNameAndProductHashes(eq(SUPPLY_CHAIN_ID), eq(SEGMENT_NAME_3), eq(STEP_NAME_3), any()))
                .thenReturn(List.of(linkMetaBlockFromMatchRuleSegment3));

        when(linkMetaBlockRepository.findByRunId(any(), any(), any(), any()))
                .thenReturn(emptyList());
    }



}
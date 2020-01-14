package com.rabobank.argos.service.domain.verification;

import com.rabobank.argos.domain.layout.DestinationType;
import com.rabobank.argos.domain.layout.Layout;
import com.rabobank.argos.domain.layout.LayoutMetaBlock;
import com.rabobank.argos.domain.layout.LayoutSegment;
import com.rabobank.argos.domain.layout.MatchFilter;
import com.rabobank.argos.domain.layout.Step;
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

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NewVerificationContextsProviderTest {

    private static final String STEP_NAME = "stepName";
    private static final String SUPPLY_CHAIN_ID = "supplyChainId";
    private static final String SEGMENT_NAME = "segmentName";
    private static final String RUN_ID = "runId";

    @Mock
    private LinkMetaBlockRepository linkMetaBlockRepository;

    @Mock
    private LayoutMetaBlock layoutMetaBlock;

    @Mock
    private Layout layout;

    @Mock
    private LayoutSegment layoutSegment;

    @Mock
    private Step step;

    private List<Artifact> artifacts;

    private List<MatchFilter> matchFilters;

    private LinkMetaBlock linkMetaBlockFromInput;

    private LinkMetaBlock linkMetaBlockFromRunId1;

    private LinkMetaBlock linkMetaBlockFromRunId2;

    private NewVerificationContextsProvider verificationContextsProvider;

    @BeforeEach
    void setup() {
        createMatchFilters();
        createArtifacts();
        linkMetaBlockFromInput = LinkMetaBlock
                .builder()
                .supplyChainId(SUPPLY_CHAIN_ID)
                .link(Link.builder()
                        .layoutSegmentName(SEGMENT_NAME)
                        .stepName(STEP_NAME)
                        .runId(RUN_ID)
                        .materials(artifacts)
                        .products(artifacts).build()
                ).build();
        verificationContextsProvider = new NewVerificationContextsProvider(linkMetaBlockRepository);
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
                .destinationStepName(STEP_NAME)
                .destinationSegmentName(SEGMENT_NAME)
                .pattern("**/*.jar")
                .build();

        MatchFilter matchFilterMaterials = MatchFilter.builder()
                .destinationType(DestinationType.MATERIALS)
                .destinationStepName(STEP_NAME)
                .destinationSegmentName(SEGMENT_NAME)
                .pattern("**/*.jar")
                .build();

        matchFilters = List.of(matchFilterProduct, matchFilterMaterials);
    }

    @Test
    void createPossibleVerificationContexts() {
        setupMocks();
        List<VerificationContext> verificationContexts = verificationContextsProvider.createPossibleVerificationContexts(layoutMetaBlock, artifacts);
        assertThat(verificationContexts, hasSize(1));
    }


    void setupMocks() {
        when(layoutMetaBlock.getSupplyChainId()).thenReturn(SUPPLY_CHAIN_ID);
        when(layoutMetaBlock.getLayout()).thenReturn(layout);
        when(layout.getLayoutSegments()).thenReturn(singletonList(layoutSegment));
        when(layoutSegment.getSteps()).thenReturn(singletonList(step));
        when(step.getStepName()).thenReturn(STEP_NAME);
        when(layoutMetaBlock.expectedEndProducts()).thenReturn(matchFilters);
        when(linkMetaBlockRepository
                .findBySupplyChainAndSegmentNameAndStepNameAndProductHashes(any(), any(), any(), any()))
                .thenReturn(List.of(linkMetaBlockFromInput));
        when(linkMetaBlockRepository
                .findBySupplyChainAndSegmentNameAndStepNameAndMaterialHash(any(), any(), any(), any()))
                .thenReturn(List.of(linkMetaBlockFromInput));
    }
}
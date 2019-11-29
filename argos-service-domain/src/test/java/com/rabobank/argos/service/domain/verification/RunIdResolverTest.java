package com.rabobank.argos.service.domain.verification;

import com.rabobank.argos.domain.layout.DestinationType;
import com.rabobank.argos.domain.layout.LayoutMetaBlock;
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
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RunIdResolverTest {
    public static final String SUPPLY_CHAIN_ID = "supply-chain-id";
    public static final String STEP_NAME = "stepName";
    public static final String ARTIFACT_JAVA = "/artifact.java";
    public static final String RUN_ID = "run_id";
    public static final String HASH = "hash";
    @Mock(answer = RETURNS_DEEP_STUBS)
    private LayoutMetaBlock layoutMetaBlock;

    private List<Artifact> productsToVerify;
    @Mock
    private LinkMetaBlockRepository linkMetaBlockRepository;

    RunIdResolver resolver;

    @BeforeEach
    public void setup() {
        withLayout(DestinationType.PRODUCTS);
        when(layoutMetaBlock.getLayout().getSteps())
                .thenReturn(singletonList(Step.builder().stepName(STEP_NAME).build()));
        productsToVerify = singletonList(Artifact.builder().hash(HASH).uri(ARTIFACT_JAVA).build());

        resolver = new RunIdResolver(linkMetaBlockRepository);
    }

    private void withLayout(DestinationType destinationType) {
        when(layoutMetaBlock.getLayout().getExpectedEndProducts())
                .thenReturn(singletonList(MatchFilter.builder()
                        .destinationStepName(STEP_NAME)
                        .pattern(ARTIFACT_JAVA)
                        .destinationType(destinationType)
                        .build()));
    }

    @Test
    void getRunIdWithValidProductsShouldReturnResult() {
        when(linkMetaBlockRepository.findBySupplyChainAndStepNameAndProductHashes(any(), any(), any()))
                .thenReturn(singletonList(LinkMetaBlock
                        .builder().link(Link.builder().runId(RUN_ID).build()).build()));

        Optional<String> runId = resolver.getRunId(layoutMetaBlock, productsToVerify);
        assertThat(runId.get(), is(RUN_ID));
    }

    @Test
    void getRunIdWithInValidProductsShouldReturnEmpty() {
        when(linkMetaBlockRepository.findBySupplyChainAndStepNameAndProductHashes(any(), eq(STEP_NAME), eq(singletonList(HASH))))
                .thenReturn(emptyList());
        Optional<String> runId = resolver.getRunId(layoutMetaBlock, productsToVerify);
        assertThat(runId.isPresent(), is(false));
    }

    @Test
    void getRunIdWithValidMaterialsShouldReturnResult() {
        withLayout(DestinationType.MATERIALS);
        when(linkMetaBlockRepository.findBySupplyChainAndStepNameAndMaterialHash(any(), any(), any()))
                .thenReturn(singletonList(LinkMetaBlock
                        .builder().link(Link.builder().runId(RUN_ID).build()).build()));
        Optional<String> runId = resolver.getRunId(layoutMetaBlock, productsToVerify);
        assertThat(runId.get(), is(RUN_ID));
    }
}
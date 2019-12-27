package com.rabobank.argos.service.domain;

import com.rabobank.argos.domain.layout.LayoutMetaBlock;
import com.rabobank.argos.domain.layout.LayoutSegment;
import com.rabobank.argos.domain.layout.Step;
import com.rabobank.argos.domain.link.Link;
import com.rabobank.argos.domain.link.LinkMetaBlock;
import com.rabobank.argos.service.domain.verification.VerificationContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StepWithEqualLinkSetsContainerTest {

    private static final String STEPNAME1 = "STEP_ONE";
    private static final String STEPNAME2 = "STEP_TWO";
    private static final String STEPNAME3 = "STEP_THREE";
    public static final String SEGMENT_NAME = "segmentName";

    @Mock
    private Step step1;

    @Mock
    private Step step2;

    @Mock
    private Step step3;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    LayoutMetaBlock layoutMetaBlock;

    @Mock()
    LayoutSegment layoutSegment;

    private LinkMetaBlock linkMetaBlock1_1;
    private LinkMetaBlock linkMetaBlock1_2;

    private LinkMetaBlock linkMetaBlock2_1;
    private LinkMetaBlock linkMetaBlock2_2;

    private LinkMetaBlock linkMetaBlock3_1;
    private LinkMetaBlock linkMetaBlock3_2;

    private StepWithEqualLinkSetsContainer stepWithEqualLinkSetsContainer;


    @Test
    void calculatePossibleVerificationContexts() {
        setupMocks();
        List<VerificationContext> result = stepWithEqualLinkSetsContainer.calculatePossibleVerificationContexts();
        assertThat(result, hasSize(8));
        assertThat(result.get(0).getLinkMetaBlocks(), hasSize(3));
        assertThat(result.get(0).getLinkMetaBlocks().get(0), is(linkMetaBlock1_2));
        assertThat(result.get(0).getLinkMetaBlocks().get(1), is(linkMetaBlock2_2));
        assertThat(result.get(0).getLinkMetaBlocks().get(2), is(linkMetaBlock3_2));

        assertThat(result.get(1).getLinkMetaBlocks().get(0), is(linkMetaBlock1_2));
        assertThat(result.get(1).getLinkMetaBlocks().get(1), is(linkMetaBlock2_1));
        assertThat(result.get(1).getLinkMetaBlocks().get(2), is(linkMetaBlock3_2));

        assertThat(result.get(2).getLinkMetaBlocks().get(0), is(linkMetaBlock1_2));
        assertThat(result.get(2).getLinkMetaBlocks().get(1), is(linkMetaBlock2_2));
        assertThat(result.get(2).getLinkMetaBlocks().get(2), is(linkMetaBlock3_1));

        assertThat(result.get(3).getLinkMetaBlocks().get(0), is(linkMetaBlock1_2));
        assertThat(result.get(3).getLinkMetaBlocks().get(1), is(linkMetaBlock2_1));
        assertThat(result.get(3).getLinkMetaBlocks().get(2), is(linkMetaBlock3_1));

        assertThat(result.get(4).getLinkMetaBlocks().get(0), is(linkMetaBlock1_1));
        assertThat(result.get(4).getLinkMetaBlocks().get(1), is(linkMetaBlock2_2));
        assertThat(result.get(4).getLinkMetaBlocks().get(2), is(linkMetaBlock3_2));

        assertThat(result.get(5).getLinkMetaBlocks().get(0), is(linkMetaBlock1_1));
        assertThat(result.get(5).getLinkMetaBlocks().get(1), is(linkMetaBlock2_1));
        assertThat(result.get(5).getLinkMetaBlocks().get(2), is(linkMetaBlock3_2));

        assertThat(result.get(6).getLinkMetaBlocks().get(0), is(linkMetaBlock1_1));
        assertThat(result.get(6).getLinkMetaBlocks().get(1), is(linkMetaBlock2_2));
        assertThat(result.get(6).getLinkMetaBlocks().get(2), is(linkMetaBlock3_1));

        assertThat(result.get(7).getLinkMetaBlocks().get(0), is(linkMetaBlock1_1));
        assertThat(result.get(7).getLinkMetaBlocks().get(1), is(linkMetaBlock2_1));
        assertThat(result.get(7).getLinkMetaBlocks().get(2), is(linkMetaBlock3_1));
    }

    private void setupMocks() {
        when(step1.getStepName()).thenReturn(STEPNAME1);
        when(step2.getStepName()).thenReturn(STEPNAME2);
        when(step3.getStepName()).thenReturn(STEPNAME3);

        when(layoutMetaBlock.getLayout().getLayoutSegments())
                .thenReturn(singletonList(layoutSegment));

        when(layoutSegment.getSteps())
                .thenReturn(asList(step1, step2, step3));

        linkMetaBlock1_1 = LinkMetaBlock.builder()
                .link(Link
                        .builder()
                        .stepName(STEPNAME1)
                        .segmentName(SEGMENT_NAME)
                        .build())
                .build();

        linkMetaBlock1_2 = LinkMetaBlock.builder()
                .link(Link
                        .builder()
                        .stepName(STEPNAME1)
                        .command(singletonList("cmd"))
                        .segmentName(SEGMENT_NAME)
                        .build())
                .build();


        linkMetaBlock2_1 = LinkMetaBlock.builder()
                .link(Link
                        .builder()
                        .stepName(STEPNAME2)
                        .segmentName(SEGMENT_NAME)
                        .build())
                .build();

        linkMetaBlock2_2 = LinkMetaBlock.builder()
                .link(Link
                        .builder()
                        .stepName(STEPNAME2)
                        .command(singletonList("cmd"))
                        .segmentName(SEGMENT_NAME)
                        .build())
                .build();

        linkMetaBlock3_1 = LinkMetaBlock.builder()
                .link(Link
                        .builder()
                        .stepName(STEPNAME3)
                        .segmentName(SEGMENT_NAME)
                        .build())
                .build();

        linkMetaBlock3_2 = LinkMetaBlock.builder()
                .link(Link
                        .builder()
                        .stepName(STEPNAME3)
                        .command(singletonList("cmd"))
                        .segmentName(SEGMENT_NAME)
                        .build())
                .build();

        stepWithEqualLinkSetsContainer = new StepWithEqualLinkSetsContainer(
                asList(linkMetaBlock1_1, linkMetaBlock1_2, linkMetaBlock2_1, linkMetaBlock2_2, linkMetaBlock3_1, linkMetaBlock3_2),
                layoutSegment,
                layoutMetaBlock
        );
    }

}
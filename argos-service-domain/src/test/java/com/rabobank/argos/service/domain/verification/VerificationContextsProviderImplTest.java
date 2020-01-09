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

import com.rabobank.argos.domain.layout.LayoutMetaBlock;
import com.rabobank.argos.domain.layout.LayoutSegment;
import com.rabobank.argos.domain.layout.Step;
import com.rabobank.argos.domain.link.Link;
import com.rabobank.argos.domain.link.LinkMetaBlock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerificationContextsProviderImplTest {

    private static final String STEPNAME1 = "STEP_ONE";
    private static final String STEPNAME2 = "STEP_TWO";
    private static final String STEPNAME3 = "STEP_THREE";
    private static final String SEGMENT_NAME = "segmentName";

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

    private VerificationContextsProviderImpl verificationContextsProviderImpl;

    @BeforeEach
    void setup() {
        verificationContextsProviderImpl = new VerificationContextsProviderImpl();
    }
    @Test
    void calculatePossibleVerificationContextsWithOneStepAndMultipleSetsShouldResultCorrectCombinations() {
        setupMocksForOneStep();
        List<VerificationContext> result = verificationContextsProviderImpl.calculatePossibleVerificationContexts(asList(linkMetaBlock1_1, linkMetaBlock1_2),
                layoutSegment,
                layoutMetaBlock);
        assertThat(result, hasSize(2));

    }

    @Test
    void calculatePossibleVerificationContextsWithNoLinksShouldReturnEmptyList() {
        setupMocksForNoLinks();
        List<VerificationContext> result = verificationContextsProviderImpl.calculatePossibleVerificationContexts(emptyList(), layoutSegment, layoutMetaBlock);
        assertThat(result, hasSize(0));

    }

    @Test
    void calculatePossibleVerificationContextsWithMultipleStepsAndMultipleSetsShouldReturnCorrectCombinations() {
        setupMocksForMultipleSteps();
        List<VerificationContext> result = verificationContextsProviderImpl.calculatePossibleVerificationContexts(asList(linkMetaBlock1_1, linkMetaBlock1_2, linkMetaBlock2_1, linkMetaBlock2_2, linkMetaBlock3_1, linkMetaBlock3_2),
                layoutSegment,
                layoutMetaBlock);
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

    private void setupMocksForMultipleSteps() {
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
                        .layoutSegmentName(SEGMENT_NAME)
                        .build())
                .build();

        linkMetaBlock1_2 = LinkMetaBlock.builder()
                .link(Link
                        .builder()
                        .stepName(STEPNAME1)
                        .command(singletonList("cmd"))
                        .layoutSegmentName(SEGMENT_NAME)
                        .build())
                .build();


        linkMetaBlock2_1 = LinkMetaBlock.builder()
                .link(Link
                        .builder()
                        .stepName(STEPNAME2)
                        .layoutSegmentName(SEGMENT_NAME)
                        .build())
                .build();

        linkMetaBlock2_2 = LinkMetaBlock.builder()
                .link(Link
                        .builder()
                        .stepName(STEPNAME2)
                        .command(singletonList("cmd"))
                        .layoutSegmentName(SEGMENT_NAME)
                        .build())
                .build();

        linkMetaBlock3_1 = LinkMetaBlock.builder()
                .link(Link
                        .builder()
                        .stepName(STEPNAME3)
                        .layoutSegmentName(SEGMENT_NAME)
                        .build())
                .build();

        linkMetaBlock3_2 = LinkMetaBlock.builder()
                .link(Link
                        .builder()
                        .stepName(STEPNAME3)
                        .command(singletonList("cmd"))
                        .layoutSegmentName(SEGMENT_NAME)
                        .build())
                .build();


    }

    private void setupMocksForOneStep() {
        when(step1.getStepName()).thenReturn(STEPNAME1);

        when(layoutMetaBlock.getLayout().getLayoutSegments())
                .thenReturn(singletonList(layoutSegment));

        when(layoutSegment.getSteps())
                .thenReturn(singletonList(step1));

        linkMetaBlock1_1 = LinkMetaBlock.builder()
                .link(Link
                        .builder()
                        .stepName(STEPNAME1)
                        .layoutSegmentName(SEGMENT_NAME)
                        .build())
                .build();

        linkMetaBlock1_2 = LinkMetaBlock.builder()
                .link(Link
                        .builder()
                        .stepName(STEPNAME1)
                        .command(singletonList("cmd"))
                        .layoutSegmentName(SEGMENT_NAME)
                        .build())
                .build();

    }

    private void setupMocksForNoLinks() {
        when(step1.getStepName()).thenReturn(STEPNAME1);
        when(layoutMetaBlock.getLayout().getLayoutSegments())
                .thenReturn(singletonList(layoutSegment));
        when(layoutSegment.getSteps())
                .thenReturn(singletonList(step1));
    }

}
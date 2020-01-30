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

import com.rabobank.argos.domain.layout.LayoutSegment;
import com.rabobank.argos.domain.layout.Step;
import com.rabobank.argos.domain.link.Link;
import com.rabobank.argos.domain.link.LinkMetaBlock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequiredNumberOfLinksVerificationTest {

    private static final String STEP_NAME = "stepName";
    private static final String SEGMENT_NAME = "segmentName";
    private RequiredNumberOfLinksVerification requiredNumberOfLinksVerification;

    @Mock
    private VerificationContext context;

    @Mock
    private Step step;

    @Mock
    private LayoutSegment layoutSegment;

    private LinkMetaBlock linkMetaBlock;

    private LinkMetaBlock linkMetaBlock2;

    @BeforeEach
    void setup() {
        requiredNumberOfLinksVerification = new RequiredNumberOfLinksVerification();

        linkMetaBlock = createLinkMetaBlock(SEGMENT_NAME, STEP_NAME);
        linkMetaBlock2 = createLinkMetaBlock(SEGMENT_NAME, STEP_NAME);
    }

    private LinkMetaBlock createLinkMetaBlock(String segmentName, String stepName) {
        return LinkMetaBlock.builder()
                .link(Link.builder()
                        .layoutSegmentName(segmentName)
                        .stepName(stepName)
                        .build())
                .build();
    }

    @Test
    void getPriority() {
        assertThat(requiredNumberOfLinksVerification.getPriority(), is(Verification.Priority.REQUIRED_NUMBER_OF_LINKS));
    }

    @Test
    void verifyWithRequiredNumberOfLinksShouldReturnValid() {
        when(context.layoutSegments()).thenReturn(singletonList(layoutSegment));
        when(layoutSegment.getName()).thenReturn(SEGMENT_NAME);
        when(context.getExpectedStepNamesBySegmentName(SEGMENT_NAME)).thenReturn(singletonList(STEP_NAME));
        when(context.layoutSegments()).thenReturn(singletonList(layoutSegment));
        when(context.getStepBySegmentNameAndStepName(SEGMENT_NAME, STEP_NAME)).thenReturn(step);
        when(context.getLinksBySegmentNameAndStepName(SEGMENT_NAME, STEP_NAME)).thenReturn(singletonList(linkMetaBlock));
        when(step.getRequiredNumberOfLinks()).thenReturn(1);
        VerificationRunResult result = requiredNumberOfLinksVerification.verify(context);
        assertThat(result.isRunIsValid(), is(true));
        verify(context, times(0)).removeLinkMetaBlocks(anyList());
    }

    @Test
    void verifyWithNoRequiredNumberOfLinksShouldReturnInValid() {
        when(context.layoutSegments()).thenReturn(singletonList(layoutSegment));
        when(layoutSegment.getName()).thenReturn(SEGMENT_NAME);
        when(context.getExpectedStepNamesBySegmentName(SEGMENT_NAME)).thenReturn(singletonList(STEP_NAME));
        when(context.layoutSegments()).thenReturn(singletonList(layoutSegment));
        when(context.getStepBySegmentNameAndStepName(SEGMENT_NAME, STEP_NAME)).thenReturn(step);
        when(context.getLinksBySegmentNameAndStepName(SEGMENT_NAME, STEP_NAME)).thenReturn(singletonList(linkMetaBlock));
        when(step.getRequiredNumberOfLinks()).thenReturn(2);
        VerificationRunResult result = requiredNumberOfLinksVerification.verify(context);
        assertThat(result.isRunIsValid(), is(false));
        verify(context, times(0)).removeLinkMetaBlocks(anyList());
    }

    @Test
    void verifyWithRequiredNumberOfLinks2ShouldReturnValid() {
        when(context.layoutSegments()).thenReturn(singletonList(layoutSegment));
        when(layoutSegment.getName()).thenReturn(SEGMENT_NAME);
        when(context.getExpectedStepNamesBySegmentName(SEGMENT_NAME)).thenReturn(singletonList(STEP_NAME));
        when(context.layoutSegments()).thenReturn(singletonList(layoutSegment));
        when(context.getStepBySegmentNameAndStepName(SEGMENT_NAME, STEP_NAME)).thenReturn(step);
        when(context.getLinksBySegmentNameAndStepName(SEGMENT_NAME, STEP_NAME)).thenReturn(List.of(linkMetaBlock, linkMetaBlock2));
        when(step.getRequiredNumberOfLinks()).thenReturn(2);
        VerificationRunResult result = requiredNumberOfLinksVerification.verify(context);
        assertThat(result.isRunIsValid(), is(true));
        verify(context, times(0)).removeLinkMetaBlocks(anyList());
    }


    @Test
    void verifyTwoLinkHashesForOneStepIsInvalid() {
        when(context.layoutSegments()).thenReturn(singletonList(layoutSegment));
        when(layoutSegment.getName()).thenReturn(SEGMENT_NAME);
        when(context.getExpectedStepNamesBySegmentName(SEGMENT_NAME)).thenReturn(singletonList(STEP_NAME));
        when(context.layoutSegments()).thenReturn(singletonList(layoutSegment));
        linkMetaBlock.getLink().setCommand(List.of("cmd"));
        when(context.getLinksBySegmentNameAndStepName(SEGMENT_NAME, STEP_NAME)).thenReturn(List.of(linkMetaBlock, linkMetaBlock2));
        VerificationRunResult result = requiredNumberOfLinksVerification.verify(context);
        assertThat(result.isRunIsValid(), is(false));
        verify(context, times(0)).removeLinkMetaBlocks(anyList());
    }
}

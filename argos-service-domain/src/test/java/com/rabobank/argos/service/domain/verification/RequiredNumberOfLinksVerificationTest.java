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

import com.rabobank.argos.domain.layout.Layout;
import com.rabobank.argos.domain.layout.LayoutMetaBlock;
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
    private static final String STEP_NAME2 = "stepName2";
    private static final String SEGMENT_NAME = "segmentName";
    private static final String SEGMENT_NAME2 = "segmentName2";
    private RequiredNumberOfLinksVerification requiredNumberOfLinksVerification;

    private VerificationContext context;

    private Step step;    
    
    private Step step2;
    
    private LayoutMetaBlock layoutMetaBlock; 
    
    private Layout layout;

    private LayoutSegment layoutSegment;    
    
    private LayoutSegment layoutSegment2;

    private LinkMetaBlock linkMetaBlock;

    private LinkMetaBlock linkMetaBlock2;

    private LinkMetaBlock linkMetaBlock3;

    private LinkMetaBlock linkMetaBlock4;

    @BeforeEach
    void setup() {
        requiredNumberOfLinksVerification = new RequiredNumberOfLinksVerification();

        linkMetaBlock = createLinkMetaBlock(SEGMENT_NAME, STEP_NAME);
        linkMetaBlock2 = createLinkMetaBlock(SEGMENT_NAME, STEP_NAME);
        linkMetaBlock3 = createLinkMetaBlock(SEGMENT_NAME2, STEP_NAME2);
        linkMetaBlock4 = createLinkMetaBlock(SEGMENT_NAME2, STEP_NAME2);

        step = Step.builder().name(STEP_NAME).requiredNumberOfLinks(1).build();
        step2 = Step.builder().name(STEP_NAME2).requiredNumberOfLinks(1).build();
        layoutSegment = LayoutSegment.builder().name(SEGMENT_NAME).steps(List.of(step)).build();
        layoutSegment2 = LayoutSegment.builder().name(SEGMENT_NAME2).steps(List.of(step2)).build();
        layout = Layout.builder().layoutSegments(List.of(layoutSegment, layoutSegment2)).build();
        
        layoutMetaBlock = LayoutMetaBlock.builder().layout(layout).build();
        context = VerificationContext.builder().layoutMetaBlock(layoutMetaBlock).linkMetaBlocks(List.of(linkMetaBlock, linkMetaBlock2, linkMetaBlock3, linkMetaBlock4)).build();
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
        VerificationRunResult result = requiredNumberOfLinksVerification.verify(context);
        assertThat(result.isRunIsValid(), is(true));
    }

    @Test
    void verifyWithNotRequiredNumberOfLinksShouldReturnInValid() {
        step.setRequiredNumberOfLinks(3);
        VerificationRunResult result = requiredNumberOfLinksVerification.verify(context);
        assertThat(result.isRunIsValid(), is(false));
    }

    @Test
    void verifyWithRequiredNumberOfLinks2ShouldReturnValid() {
        step.setRequiredNumberOfLinks(2);
        VerificationRunResult result = requiredNumberOfLinksVerification.verify(context);
        assertThat(result.isRunIsValid(), is(true));
    }


    @Test
    void verifyTwoLinkHashesForOneStepIsInvalid() {
        linkMetaBlock.getLink().setCommand(List.of("cmd"));
        VerificationRunResult result = requiredNumberOfLinksVerification.verify(context);
        assertThat(result.isRunIsValid(), is(false));
    }
    
    @Test
    void verifyWithSegmentNotFoundReturnInValid() {
        step2.setRequiredNumberOfLinks(2);
        context = VerificationContext.builder()
                .layoutMetaBlock(layoutMetaBlock)
                .linkMetaBlocks(List.of(linkMetaBlock, linkMetaBlock2))
                .build();
        VerificationRunResult result = requiredNumberOfLinksVerification.verify(context);
        assertThat(result.isRunIsValid(), is(false));
    }
}

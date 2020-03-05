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

import com.rabobank.argos.domain.Signature;
import com.rabobank.argos.domain.layout.Layout;
import com.rabobank.argos.domain.layout.LayoutMetaBlock;
import com.rabobank.argos.domain.layout.LayoutSegment;
import com.rabobank.argos.domain.layout.Step;
import com.rabobank.argos.domain.link.Link;
import com.rabobank.argos.domain.link.LinkMetaBlock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(MockitoExtension.class)
class RequiredNumberOfLinksVerificationTest {

    private static final String STEP_NAME1 = "stepName1";
    private static final String STEP_NAME2 = "stepName2";
    private static final String STEP_NAME3 = "stepName3";
    private static final String SEGMENT_NAME = "segmentName";
    private static final String SEGMENT_NAME2 = "segmentName2";
    private static final String KEY_ID_1 = "keyid1";
    private static final String KEY_ID_2 = "keyid2";
    private static final Signature SIGNATURE_1 = Signature.builder().keyId(KEY_ID_1).signature("sig1").build();
    private static final Signature SIGNATURE_2 = Signature.builder().keyId(KEY_ID_2).signature("sig2").build();
    private static final Signature SIGNATURE_3 = Signature.builder().keyId(KEY_ID_1).signature("sig3").build();
    private RequiredNumberOfLinksVerification requiredNumberOfLinksVerification;

    private VerificationContext context;

    private Step step1;    
    
    private Step step2;    
    
    private Step step3;
    
    private LayoutMetaBlock layoutMetaBlock; 
    
    private Layout layout;

    private LayoutSegment layoutSegment;

    private LinkMetaBlock linkMetaBlock1;

    private LinkMetaBlock linkMetaBlock2;

    private LinkMetaBlock linkMetaBlock3;

    private LinkMetaBlock linkMetaBlock4;

    private LinkMetaBlock linkMetaBlock5;

    @BeforeEach
    void setup() {
        requiredNumberOfLinksVerification = new RequiredNumberOfLinksVerification();

        linkMetaBlock1 = createLinkMetaBlock(SIGNATURE_1, SEGMENT_NAME, STEP_NAME1);
        linkMetaBlock2 = createLinkMetaBlock(SIGNATURE_2, SEGMENT_NAME, STEP_NAME1);
        linkMetaBlock3 = createLinkMetaBlock(SIGNATURE_1, SEGMENT_NAME, STEP_NAME2);
        linkMetaBlock4 = createLinkMetaBlock(SIGNATURE_1, SEGMENT_NAME, STEP_NAME3);
        linkMetaBlock5 = createLinkMetaBlock(SIGNATURE_3, SEGMENT_NAME, STEP_NAME3);

        step1 = Step.builder().name(STEP_NAME1).requiredNumberOfLinks(1).build();
        step2 = Step.builder().name(STEP_NAME2).requiredNumberOfLinks(1).build();
        step3 = Step.builder().name(STEP_NAME3).requiredNumberOfLinks(1).build();
        layoutSegment = LayoutSegment.builder().name(SEGMENT_NAME).steps(List.of(step1, step2, step3)).build();
        layout = Layout.builder().layoutSegments(List.of(layoutSegment)).build();
        
        layoutMetaBlock = LayoutMetaBlock.builder().layout(layout).build();
        context = VerificationContext.builder().layoutMetaBlock(layoutMetaBlock).linkMetaBlocks(List.of(linkMetaBlock1, linkMetaBlock2, linkMetaBlock3, linkMetaBlock4)).build();
    }

    private LinkMetaBlock createLinkMetaBlock(Signature sig,String segmentName, String stepName) {
        return LinkMetaBlock.builder()
                .signature(sig)
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
        step1.setRequiredNumberOfLinks(3);
        VerificationRunResult result = requiredNumberOfLinksVerification.verify(context);
        assertThat(result.isRunIsValid(), is(false));
    }

    @Test
    void verifyWithRequiredNumberOfLinks2ShouldReturnValid() {
        step1.setRequiredNumberOfLinks(2);
        VerificationRunResult result = requiredNumberOfLinksVerification.verify(context);
        assertThat(result.isRunIsValid(), is(true));
    }

    @Test
    void verifyWithRequiredNumberOfLinks2SignedBySameFunctShouldReturnInValid() {
        step3.setRequiredNumberOfLinks(2);
        VerificationRunResult result = requiredNumberOfLinksVerification.verify(context);
        assertThat(result.isRunIsValid(), is(false));
    }


    @Test
    void verifyTwoLinkHashesForOneStepIsInvalid() {
        linkMetaBlock1.getLink().setCommand(List.of("cmd"));
        VerificationRunResult result = requiredNumberOfLinksVerification.verify(context);
        assertThat(result.isRunIsValid(), is(false));
    }
    
    @Test
    void verifyWithSegmentNotFoundReturnInValid() {
        step2.setRequiredNumberOfLinks(2);
        context = VerificationContext.builder()
                .layoutMetaBlock(layoutMetaBlock)
                .linkMetaBlocks(List.of(linkMetaBlock1, linkMetaBlock2))
                .build();
        VerificationRunResult result = requiredNumberOfLinksVerification.verify(context);
        assertThat(result.isRunIsValid(), is(false));
    }
}

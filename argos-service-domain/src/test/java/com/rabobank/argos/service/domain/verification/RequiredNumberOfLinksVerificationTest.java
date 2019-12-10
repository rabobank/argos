/*
 * Copyright (C) 2019 Rabobank Nederland
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
import com.rabobank.argos.domain.layout.Step;
import com.rabobank.argos.domain.link.Link;
import com.rabobank.argos.domain.link.LinkMetaBlock;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequiredNumberOfLinksVerificationTest {

    private static final String STEP_NAME = "stepName";
    private static final String KEY_1 = "key1";
    private static final String KEY_2 = "key2";
    private static final String KEY_3 = "key3";
    private RequiredNumberOfLinksVerification requiredNumberOfLinksVerification;

    @Mock
    private VerificationContext context;

    @Mock
    private Step step;

    private LinkMetaBlock linkMetaBlock;

    private LinkMetaBlock linkMetaBlock2;

    private LinkMetaBlock linkMetaBlock3;

    @BeforeEach
    void setup() {
        requiredNumberOfLinksVerification = new RequiredNumberOfLinksVerification();
        linkMetaBlock = LinkMetaBlock.builder().signature(Signature.builder().keyId(KEY_1).build()).link(Link.builder().stepName(STEP_NAME).build()).build();
        linkMetaBlock2 = LinkMetaBlock.builder().signature(Signature.builder().keyId(KEY_2).build()).link(Link.builder().stepName(STEP_NAME).build()).build();
        linkMetaBlock3 = LinkMetaBlock.builder().signature(Signature.builder().keyId(KEY_3).build()).link(Link.builder().stepName("other").build()).build();
    }

    @Test
    void getPriority() {
        assertThat(requiredNumberOfLinksVerification.getPriority(), is(Verification.Priority.REQUIRED_NUMBER_OF_LINKS));
    }

    @Test
    void verifyWithRequiredNumberOfLinksShouldReturnValid() {
        when(context.getStepByStepName(STEP_NAME)).thenReturn(step);
        when(step.getRequiredNumberOfLinks()).thenReturn(1);
        when(step.getAuthorizedKeyIds()).thenReturn(singletonList(KEY_1));
        when(context.getLinkMetaBlocks()).thenReturn(singletonList(linkMetaBlock));
        VerificationRunResult result = requiredNumberOfLinksVerification.verify(context);
        assertThat(result.isRunIsValid(), is(true));
        verify(context).removeLinkMetaBlocks(emptyList());
    }

    @Test
    void verifyWithNoRequiredNumberOfLinksShouldReturnInValid() {
        when(context.getStepByStepName(STEP_NAME)).thenReturn(step);
        when(step.getRequiredNumberOfLinks()).thenReturn(2);
        when(context.getLinkMetaBlocks()).thenReturn(singletonList(linkMetaBlock));
        VerificationRunResult result = requiredNumberOfLinksVerification.verify(context);
        assertThat(result.isRunIsValid(), is(true));
        verify(context).removeLinkMetaBlocks(singletonList(linkMetaBlock));
    }

    @Test
    void verifyWithRequiredNumberOfLinksAndUniqueKeysShouldReturnValid() {
        when(context.getStepByStepName(STEP_NAME)).thenReturn(step);
        when(step.getRequiredNumberOfLinks()).thenReturn(2);
        when(context.getLinkMetaBlocks()).thenReturn(List.of(linkMetaBlock, linkMetaBlock2));
        when(step.getAuthorizedKeyIds()).thenReturn(List.of(KEY_2, KEY_1));
        VerificationRunResult result = requiredNumberOfLinksVerification.verify(context);
        assertThat(result.isRunIsValid(), is(true));
        verify(context).removeLinkMetaBlocks(emptyList());
    }

    @Test
    void verifyWithRequiredNumberOfLinksAndUniqueKeysShouldReturnValidWithOtherStep() {
        when(context.getStepByStepName("other")).thenReturn(step);
        when(context.getStepByStepName(STEP_NAME)).thenReturn(step);
        when(step.getRequiredNumberOfLinks()).thenReturn(2);
        when(context.getLinkMetaBlocks()).thenReturn(List.of(linkMetaBlock, linkMetaBlock2, linkMetaBlock3));
        when(step.getAuthorizedKeyIds()).thenReturn(List.of(KEY_2, KEY_1));
        VerificationRunResult result = requiredNumberOfLinksVerification.verify(context);
        assertThat(result.isRunIsValid(), is(true));
        verify(context).removeLinkMetaBlocks(List.of(linkMetaBlock3));
    }

    @Test
    void verifyWithRequiredNumberOfLinksAndNonUniqueKeysShouldReturnInValid() {
        when(context.getStepByStepName(STEP_NAME)).thenReturn(step);
        when(step.getRequiredNumberOfLinks()).thenReturn(2);
        when(context.getLinkMetaBlocks()).thenReturn(List.of(linkMetaBlock2));
        VerificationRunResult result = requiredNumberOfLinksVerification.verify(context);
        assertThat(result.isRunIsValid(), is(true));
        verify(context).removeLinkMetaBlocks(singletonList(linkMetaBlock2));
    }
}

/*
 * Copyright (C) 2020 Rabobank Nederland
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

import com.rabobank.argos.domain.layout.Step;
import com.rabobank.argos.domain.link.LinkMetaBlock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StepAuthorizedKeyIdVerificationTest {

    private static final String STEP_NAME = "stepName";

    private StepAuthorizedKeyIdVerification stepAuthorizedKeyIdVerification;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private VerificationContext context;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Step step;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private LinkMetaBlock linkMetaBlock;

    @Captor
    ArgumentCaptor<List<LinkMetaBlock>> listArgumentCaptor;

    @BeforeEach
    void setup() {
        stepAuthorizedKeyIdVerification = new StepAuthorizedKeyIdVerification();
    }

    @Test
    void getPriority() {
        assertThat(stepAuthorizedKeyIdVerification.getPriority(), is(Verification.Priority.STEP_AUTHORIZED_KEYID));
    }

    @Test
    void verifyWithCorrectKeyIdShouldReturnValidResponse() {
        when(context.getLinkMetaBlocks()).thenReturn(Collections.singletonList(linkMetaBlock));
        when(linkMetaBlock.getLink().getStepName()).thenReturn(STEP_NAME);
        when(context.getLayoutMetaBlock().getLayout().getSteps()).thenReturn(Collections.singletonList(step));
        when(step.getAuthorizedKeyIds()).thenReturn(Collections.singletonList("keyId"));
        when(context.getStepByStepName(eq(STEP_NAME))).thenReturn(step);
        when(context.getLinksByStepName(eq(STEP_NAME))).thenReturn(Collections.singletonList(linkMetaBlock));
        when(linkMetaBlock.getSignature().getKeyId()).thenReturn("keyId");
        VerificationRunResult result = stepAuthorizedKeyIdVerification.verify(context);
        verify(context, times(0)).removeLinkMetaBlocks(listArgumentCaptor.capture());
        assertThat(result.isRunIsValid(), is(true));
    }

    @Test
    void verifyWithCorrectIncorrectKeyIdShouldReturnInValidResponse() {
        when(context.getLinkMetaBlocks()).thenReturn(Collections.singletonList(linkMetaBlock));
        when(context.getLayoutMetaBlock().getLayout().getSteps()).thenReturn(Collections.singletonList(step));
        when(step.getAuthorizedKeyIds()).thenReturn(Collections.singletonList("keyId"));
        when(context.getLinksByStepName(eq(STEP_NAME))).thenReturn(Collections.singletonList(linkMetaBlock));
        when(linkMetaBlock.getSignature().getKeyId()).thenReturn("unTrustedKeyId");
        VerificationRunResult result = stepAuthorizedKeyIdVerification.verify(context);
        verify(context).removeLinkMetaBlocks(listArgumentCaptor.capture());
        assertThat(listArgumentCaptor.getValue(), hasSize(1));
        assertThat(result.isRunIsValid(), is(true));
    }
}

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

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpectedCommandVerificationTest {

    private static final String STEP_NAME = "stepName";

    private ExpectedCommandVerification expectedCommandVerification;

    @Mock
    private VerificationContext context;

    @Captor
    private ArgumentCaptor<List<LinkMetaBlock>> listArgumentCaptor;

    @Mock
    private Step step;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private LinkMetaBlock linkMetaBlock;

    @BeforeEach
    void setup() {
        expectedCommandVerification = new ExpectedCommandVerification();
    }

    @Test
    void verifyWithCorrectCommandsShouldReturnValid() {
        mockValid();
        VerificationRunResult verificationRunResult = expectedCommandVerification.verify(context);
        assertThat(verificationRunResult.isRunIsValid(), is(true));
    }

    @Test
    void verifyWithInCorrectCommandsShouldRemoveInvalidLinks() {
        mockInValid();
        VerificationRunResult verificationRunResult = expectedCommandVerification.verify(context);
        verify(context).removeLinkMetaBlocks(listArgumentCaptor.capture());
        assertThat(listArgumentCaptor.getValue(), hasSize(1));
        assertThat(verificationRunResult.isRunIsValid(), is(true));
    }

    @Test
    void verifyWithNullCorrectCommandsShouldRemoveInvalidLinks() {
        mockInValidWithNullInLink();
        VerificationRunResult verificationRunResult = expectedCommandVerification.verify(context);
        verify(context).removeLinkMetaBlocks(listArgumentCaptor.capture());
        assertThat(listArgumentCaptor.getValue(), hasSize(1));
        assertThat(verificationRunResult.isRunIsValid(), is(true));
    }

    private void mockInValidWithNullInLink() {
        List<String> stepCommands = asList("command1", "command2");
        when(step.getExpectedCommand()).thenReturn(stepCommands);
        when(linkMetaBlock.getLink().getStepName()).thenReturn(STEP_NAME);
        when(linkMetaBlock.getLink().getCommand()).thenReturn(null);
        when(linkMetaBlock.getLink().getStepName()).thenReturn(STEP_NAME);
        when(context.getStepByStepName(eq(STEP_NAME))).thenReturn(step);
        when(context.getLinkMetaBlocks()).thenReturn(Collections.singletonList(linkMetaBlock));
    }


    private void mockValid() {
        List<String> commands = asList("command1", "command2");
        when(step.getExpectedCommand()).thenReturn(commands);
        when(linkMetaBlock.getLink().getStepName()).thenReturn(STEP_NAME);
        when(linkMetaBlock.getLink().getCommand()).thenReturn(commands);
        when(linkMetaBlock.getLink().getStepName()).thenReturn(STEP_NAME);
        when(context.getStepByStepName(eq(STEP_NAME))).thenReturn(step);
        when(context.getLinkMetaBlocks()).thenReturn(Collections.singletonList(linkMetaBlock));

    }

    private void mockInValid() {
        List<String> stepCommands = asList("command1", "command2");
        List<String> linkCommands = asList("command1", "command3");
        when(step.getExpectedCommand()).thenReturn(stepCommands);
        when(linkMetaBlock.getLink().getStepName()).thenReturn(STEP_NAME);
        when(linkMetaBlock.getLink().getCommand()).thenReturn(linkCommands);
        when(linkMetaBlock.getLink().getStepName()).thenReturn(STEP_NAME);
        when(context.getStepByStepName(eq(STEP_NAME))).thenReturn(step);
        when(context.getLinkMetaBlocks()).thenReturn(Collections.singletonList(linkMetaBlock));

    }
}

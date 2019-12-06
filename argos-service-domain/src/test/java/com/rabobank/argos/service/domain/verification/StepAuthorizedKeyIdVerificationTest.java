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

/*-
 * #%L
 * Argos Supply Chain Notary
 * %%
 * Copyright (C) 2019 Rabobank Nederland
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.rabobank.argos.domain.layout.Step;
import com.rabobank.argos.domain.link.LinkMetaBlock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
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
        when(step.getStepName()).thenReturn(STEP_NAME);
        when(context.getLayoutMetaBlock().getLayout().getSteps()).thenReturn(Collections.singletonList(step));
        when(step.getAuthorizedKeyIds()).thenReturn(Collections.singletonList("keyId"));
        when(context.getLinksByStepName(eq(STEP_NAME))).thenReturn(Collections.singletonList(linkMetaBlock));
        when(linkMetaBlock.getSignature().getKeyId()).thenReturn("keyId");
        VerificationRunResult result = stepAuthorizedKeyIdVerification.verify(context);
        assertThat(result.isRunIsValid(), is(true));
    }

    @Test
    void verifyWithCorrectIncorrectKeyIdShouldReturnInValidResponse() {
        when(step.getStepName()).thenReturn(STEP_NAME);
        when(context.getLayoutMetaBlock().getLayout().getSteps()).thenReturn(Collections.singletonList(step));
        when(step.getAuthorizedKeyIds()).thenReturn(Collections.singletonList("keyId"));
        when(context.getLinksByStepName(eq(STEP_NAME))).thenReturn(Collections.singletonList(linkMetaBlock));
        when(linkMetaBlock.getSignature().getKeyId()).thenReturn("unTrustedKeyId");
        VerificationRunResult result = stepAuthorizedKeyIdVerification.verify(context);
        assertThat(result.isRunIsValid(), is(false));
    }
}

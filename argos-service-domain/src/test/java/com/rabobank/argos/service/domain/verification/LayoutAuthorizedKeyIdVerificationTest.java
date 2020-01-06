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

import com.rabobank.argos.domain.Signature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LayoutAuthorizedKeyIdVerificationTest {

    private static final String KEY_1 = "key1";
    public static final String KEY_2 = "key2";
    private LayoutAuthorizedKeyIdVerification layoutAuthorizedKeyIdVerification;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private VerificationContext context;

    @Mock
    private Signature signature;

    @BeforeEach
    void setUp() {
        layoutAuthorizedKeyIdVerification = new LayoutAuthorizedKeyIdVerification();
    }

    @Test
    void getPriority() {
        assertThat(layoutAuthorizedKeyIdVerification.getPriority(), is(Verification.Priority.LAYOUT_AUTHORIZED_KEYID));
    }

    @Test
    void verifyWithCorrectKeyIdShouldReturnValidResponse() {
        when(signature.getKeyId()).thenReturn(KEY_1);
        when(context.getLayoutMetaBlock().getSignatures()).thenReturn(singletonList(signature));
        when(context.getLayoutMetaBlock().getLayout().getAuthorizedKeyIds()).thenReturn(singletonList(KEY_1));
        VerificationRunResult result = layoutAuthorizedKeyIdVerification.verify(context);
        assertThat(result.isRunIsValid(), is(true));
    }

    @Test
    void verifyWithInCorrectKeyIdShouldReturnInValidResponse() {
        when(signature.getKeyId()).thenReturn(KEY_1);
        when(context.getLayoutMetaBlock().getSignatures()).thenReturn(singletonList(signature));
        when(context.getLayoutMetaBlock().getLayout().getAuthorizedKeyIds()).thenReturn(singletonList(KEY_2));
        VerificationRunResult result = layoutAuthorizedKeyIdVerification.verify(context);
        assertThat(result.isRunIsValid(), is(false));
    }
}
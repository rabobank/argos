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
import com.rabobank.argos.domain.signing.SignatureValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.PublicKey;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LayoutMetaBlockSignatureVerificationTest {

    private static final String KEY_ID = "keyId";
    private static final String SIG = "sig";
    @Mock
    private SignatureValidator signatureValidator;

    @Mock
    private VerificationContext context;

    private LayoutMetaBlockSignatureVerification verification;

    @Mock
    private LayoutMetaBlock layoutMetaBlock;

    @Mock
    private Signature signature;

    @Mock
    private PublicKey publicKey;

    @Mock
    private Layout layout;

    @Mock
    private com.rabobank.argos.domain.layout.PublicKey domainPublicKey;

    @BeforeEach
    void setUp() {
        verification = new LayoutMetaBlockSignatureVerification(signatureValidator);
    }

    @Test
    void getPriority() {
        assertThat(verification.getPriority(), is(Verification.Priority.LAYOUT_METABLOCK_SIGNATURE));
    }

    @Test
    void verifyOkay() {
        when(domainPublicKey.getId()).thenReturn(KEY_ID);
        when(domainPublicKey.getKey()).thenReturn(publicKey);
        mockSetup(true);
        assertThat(verification.verify(context).isRunIsValid(), is(true));
    }

    @Test
    void verifyNotOkay() {
        when(domainPublicKey.getId()).thenReturn(KEY_ID);
        when(domainPublicKey.getKey()).thenReturn(publicKey);
        mockSetup(false);
        assertThat(verification.verify(context).isRunIsValid(), is(false));
    }

    private void mockSetup(boolean valid) {
        when(layoutMetaBlock.getLayout()).thenReturn(layout);
        when(signatureValidator.isValid(layout, SIG, publicKey)).thenReturn(valid);
        when(signature.getSignature()).thenReturn(SIG);
        when(signature.getKeyId()).thenReturn(KEY_ID);
        when(layout.getKeys()).thenReturn(List.of(domainPublicKey));
        when(context.getLayoutMetaBlock()).thenReturn(layoutMetaBlock);
        when(layoutMetaBlock.getSignatures()).thenReturn(Collections.singletonList(signature));
    }

    @Test
    void verifyKeyNotFound() {
        when(layoutMetaBlock.getLayout()).thenReturn(layout);
        when(layout.getKeys()).thenReturn(List.of(domainPublicKey));
        when(domainPublicKey.getId()).thenReturn(KEY_ID);
        when(signature.getKeyId()).thenReturn(KEY_ID);
        when(domainPublicKey.getId()).thenReturn("other");
        when(context.getLayoutMetaBlock()).thenReturn(layoutMetaBlock);
        when(layoutMetaBlock.getSignatures()).thenReturn(Collections.singletonList(signature));
        assertThat(verification.verify(context).isRunIsValid(), is(false));
    }
}

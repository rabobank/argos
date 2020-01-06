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
import com.rabobank.argos.domain.key.KeyPair;
import com.rabobank.argos.domain.link.Link;
import com.rabobank.argos.domain.link.LinkMetaBlock;
import com.rabobank.argos.domain.signing.SignatureValidator;
import com.rabobank.argos.service.domain.key.KeyPairRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.PublicKey;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LinkMetaBlockSignatureVerificationTest {

    private static final String KEY_ID = "keyId";
    private static final String SIG = "sigNa";
    @Mock
    private SignatureValidator signatureValidator;

    @Mock
    private KeyPairRepository keyPairRepository;

    @Mock
    private VerificationContext context;

    @Mock
    private LinkMetaBlock linkMetaBlock;

    private LinkMetaBlockSignatureVerification verification;

    @Mock
    private Link link;

    @Mock
    private Signature signature;

    @Mock
    private KeyPair keyPair;

    @Mock
    private PublicKey publicKey;

    @BeforeEach
    void setUp() {
        verification = new LinkMetaBlockSignatureVerification(signatureValidator, keyPairRepository);
    }

    @Test
    void getPriority() {
        assertThat(verification.getPriority(), is(Verification.Priority.LINK_METABLOCK_SIGNATURE));
    }

    @Test
    void verifyOkay() {
        mockSetup(true);
        assertThat(verification.verify(context).isRunIsValid(), is(true));
        verify(context).removeLinkMetaBlocks(Collections.emptyList());
    }

    @Test
    void verifyNotValid() {
        mockSetup(false);
        assertThat(verification.verify(context).isRunIsValid(), is(true));
        verify(context).removeLinkMetaBlocks(List.of(linkMetaBlock));
    }

    @Test
    void verifyKeyNotFound() {
        when(context.getLinkMetaBlocks()).thenReturn(List.of(linkMetaBlock));
        when(linkMetaBlock.getSignature()).thenReturn(signature);
        when(signature.getKeyId()).thenReturn(KEY_ID);
        when(keyPairRepository.findByKeyId(KEY_ID)).thenReturn(Optional.empty());
        assertThat(verification.verify(context).isRunIsValid(), is(true));
        verify(context).removeLinkMetaBlocks(List.of(linkMetaBlock));
    }

    private void mockSetup(boolean valid) {
        when(context.getLinkMetaBlocks()).thenReturn(List.of(linkMetaBlock));
        when(linkMetaBlock.getLink()).thenReturn(link);
        when(linkMetaBlock.getSignature()).thenReturn(signature);
        when(signature.getKeyId()).thenReturn(KEY_ID);
        when(signature.getSignature()).thenReturn(SIG);
        when(keyPairRepository.findByKeyId(KEY_ID)).thenReturn(Optional.of(keyPair));
        when(keyPair.getPublicKey()).thenReturn(publicKey);
        when(signatureValidator.isValid(link, SIG, publicKey)).thenReturn(valid);
    }
}

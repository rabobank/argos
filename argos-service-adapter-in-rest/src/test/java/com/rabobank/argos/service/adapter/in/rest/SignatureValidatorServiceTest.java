/**
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
package com.rabobank.argos.service.adapter.in.rest;

import com.rabobank.argos.domain.Signature;
import com.rabobank.argos.domain.key.KeyPair;
import com.rabobank.argos.domain.link.Link;
import com.rabobank.argos.domain.signing.SignatureValidator;
import com.rabobank.argos.service.domain.key.KeyPairRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.security.PublicKey;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class SignatureValidatorServiceTest {

    private static final String KEY_ID = "keyId";
    private static final String SIGNATURE = "signature";
    @Mock
    private SignatureValidator signatureValidator;

    @Mock
    private KeyPairRepository keyPairRepository;
    private SignatureValidatorService service;

    @Mock
    private Link signable;

    @Mock
    private Signature signature;

    @Mock
    private KeyPair keyPair;

    @Mock
    private PublicKey publicKey;

    @BeforeEach
    void setUp() {
        service = new SignatureValidatorService(signatureValidator, keyPairRepository);
    }

    @Test
    void validateSignature() {
        when(keyPair.getPublicKey()).thenReturn(publicKey);
        when(keyPairRepository.findByKeyId(KEY_ID)).thenReturn(Optional.of(keyPair));
        when(signature.getKeyId()).thenReturn(KEY_ID);
        when(signature.getSignature()).thenReturn(SIGNATURE);

        when(signatureValidator.isValid(signable, SIGNATURE, publicKey)).thenReturn(true);
        service.validateSignature(signable, signature);
    }

    @Test
    void createInValidSignature() {
        when(keyPair.getPublicKey()).thenReturn(publicKey);
        when(keyPairRepository.findByKeyId(KEY_ID)).thenReturn(Optional.of(keyPair));
        when(signature.getKeyId()).thenReturn(KEY_ID);
        when(signature.getSignature()).thenReturn(SIGNATURE);

        when(signatureValidator.isValid(signable, SIGNATURE, publicKey)).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.validateSignature(signable, signature));
        assertThat(exception.getStatus().value(), is(400));
        assertThat(exception.getReason(), is("invalid signature"));
    }

    @Test
    void createSignatureKeyIdNotFound() {
        when(signature.getKeyId()).thenReturn(KEY_ID);
        when(keyPairRepository.findByKeyId(KEY_ID)).thenReturn(Optional.empty());


        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.validateSignature(signable, signature));
        assertThat(exception.getStatus().value(), is(400));
        assertThat(exception.getReason(), is("signature with keyId keyId not found"));
    }

}

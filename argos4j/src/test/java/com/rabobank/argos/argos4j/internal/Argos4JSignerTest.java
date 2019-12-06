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
package com.rabobank.argos.argos4j.internal;

import com.rabobank.argos.argos4j.SigningKey;
import com.rabobank.argos.domain.Signature;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Argos4JSignerTest {

    private Argos4JSigner signer;
    private SigningKey signingKey;
    private KeyPair pair;

    @BeforeEach
    void setUp() throws NoSuchAlgorithmException {

        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        pair = generator.generateKeyPair();

        signer = new Argos4JSigner();
        signingKey = SigningKey.builder()
                .keyPair(pair).build();
    }

    @Test
    void sign() throws NoSuchAlgorithmException, InvalidKeyException, DecoderException, SignatureException {
        Signature signature = signer.sign(signingKey, "string to sign");
        assertThat(signature.getKeyId(), is(DigestUtils.sha256Hex(pair.getPublic().getEncoded())));

        java.security.Signature signatureValidator = java.security.Signature.getInstance("SHA256WithRSA");
        signatureValidator.initVerify(pair.getPublic());
        signatureValidator.update("string to sign".getBytes(StandardCharsets.UTF_8));

        assertTrue(signatureValidator.verify(Hex.decodeHex(signature.getSignature())));

    }
}

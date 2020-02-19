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
package com.rabobank.argos.argos4j.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabobank.argos.argos4j.rest.api.model.RestNonPersonalAccountKeyPair;
import com.rabobank.argos.domain.Signature;
import com.rabobank.argos.domain.key.RSAPublicKeyFactory;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Argos4JSignerTest {

    private static final char[] PASSWORD = "gBM1Q4sc3kh05E".toCharArray();
    private Argos4JSigner signer;

    private RestNonPersonalAccountKeyPair pair;
    private PublicKey publicKey;

    @BeforeEach
    void setUp() throws GeneralSecurityException, IOException {
        pair = new ObjectMapper().readValue(this.getClass().getResourceAsStream("/keypair.json"), RestNonPersonalAccountKeyPair.class);
        publicKey = RSAPublicKeyFactory.instance(pair.getPublicKey());
        signer = new Argos4JSigner();
    }

    @Test
    void sign() throws NoSuchAlgorithmException, DecoderException, SignatureException, InvalidKeyException {
        Signature signature = signer.sign(pair, PASSWORD, "string to sign");
        assertThat(signature.getKeyId(), is(pair.getKeyId()));

        java.security.Signature signatureValidator = java.security.Signature.getInstance("SHA256WithRSA");
        signatureValidator.initVerify(publicKey);
        signatureValidator.update("string to sign".getBytes(StandardCharsets.UTF_8));

        assertTrue(signatureValidator.verify(Hex.decodeHex(signature.getSignature())));
    }
}

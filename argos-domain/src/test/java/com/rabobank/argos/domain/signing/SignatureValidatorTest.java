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
package com.rabobank.argos.domain.signing;

import com.rabobank.argos.domain.ArgosError;
import com.rabobank.argos.domain.link.Artifact;
import com.rabobank.argos.domain.link.Link;
import com.rabobank.argos.domain.link.LinkMetaBlock;
import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SignatureValidatorTest {

    private SignatureValidator validator;
    private Link link;
    private KeyPair pair;

    @BeforeEach
    void setUp() throws NoSuchAlgorithmException {
        validator = new SignatureValidator();
        link = Link.builder()
                .products(singletonList(Artifact.builder().hash("hash2").uri("/path/tofile2").build()))
                .materials(singletonList(Artifact.builder().hash("hash").uri("/path/tofile").build())).build();

        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);

        pair = generator.generateKeyPair();
    }

    @Test
    void isValid() throws InvalidKeyException, SignatureException, NoSuchAlgorithmException {

        Signature privateSignature = Signature.getInstance("SHA256withRSA");
        privateSignature.initSign(pair.getPrivate());

        privateSignature.update(new JsonSigningSerializer().serialize(link).getBytes(UTF_8));

        String signature = Hex.encodeHexString(privateSignature.sign());

        LinkMetaBlock linkMetaBlock = LinkMetaBlock.builder()
                .signature(com.rabobank.argos.domain.Signature.builder().signature(signature).build()).link(link).build();
        assertThat(validator.isValid(linkMetaBlock.getLink(), linkMetaBlock.getSignature().getSignature(), pair.getPublic()), is(true));
    }

    @Test
    void isNotValid() throws InvalidKeyException, SignatureException, NoSuchAlgorithmException {

        Signature privateSignature = Signature.getInstance("SHA256withRSA");
        privateSignature.initSign(pair.getPrivate());

        privateSignature.update(new JsonSigningSerializer().serialize(link).getBytes(UTF_8));
        String signature = Hex.encodeHexString(privateSignature.sign());

        link.setStepName("extra");

        LinkMetaBlock linkMetaBlock = LinkMetaBlock.builder()
                .signature(com.rabobank.argos.domain.Signature.builder().signature(signature).build()).link(link).build();
        assertThat(validator.isValid(linkMetaBlock.getLink(), linkMetaBlock.getSignature().getSignature(), pair.getPublic()), is(false));
    }

    @Test
    void inValidSignature() {
        String signature = "bla";
        LinkMetaBlock linkMetaBlock = LinkMetaBlock.builder()
                .signature(com.rabobank.argos.domain.Signature.builder().signature(signature).build()).link(link).build();

        ArgosError argosError = assertThrows(ArgosError.class, () -> validator.isValid(linkMetaBlock.getLink(), linkMetaBlock.getSignature().getSignature(), pair.getPublic()));
        assertThat(argosError.getMessage(), is("Odd number of characters."));

    }
}

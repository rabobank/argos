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
package com.rabobank.argos.service.adapter.out.mongodb.key.converter;

import com.rabobank.argos.domain.key.RSAPublicKeyFactory;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

class PublicKeyToByteArrayWriteConverterTest {

    private byte[] bytePublicKey;
    private String base64EncodedPublicKey;
    private PublicKeyToByteArrayWriteConverter publicKeyToByteArrayWriteConverter;

    @BeforeEach
    void setup() throws IOException {
        base64EncodedPublicKey = IOUtils.toString(ByteArrayToPublicKeyToReadConverterTest.class.getResourceAsStream("/testkey.pub"), "UTF-8");
        bytePublicKey = Base64.getDecoder().decode(base64EncodedPublicKey);
        publicKeyToByteArrayWriteConverter = new PublicKeyToByteArrayWriteConverter();
    }

    @Test
    void convert() throws InvalidKeySpecException, NoSuchAlgorithmException {
        byte[] byteConverted=  publicKeyToByteArrayWriteConverter.convert( RSAPublicKeyFactory.instance(bytePublicKey));
        assertThat(byteConverted,is(bytePublicKey));
    }
}

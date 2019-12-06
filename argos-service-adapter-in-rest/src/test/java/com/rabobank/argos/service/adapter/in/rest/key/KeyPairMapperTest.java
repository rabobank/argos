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
package com.rabobank.argos.service.adapter.in.rest.key;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabobank.argos.domain.key.KeyPair;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestKeyPair;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;
import java.security.PublicKey;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KeyPairMapperTest {

    private KeyPairMapper converter;
    private ObjectMapper mapper;
    private String keyPairJson;
    private byte[] bytePublicKey;
    private String base64EncodedPublicKey;


    @BeforeEach
    void setUp() throws IOException {
        converter = Mappers.getMapper(KeyPairMapper.class);
        mapper = new ObjectMapper();
        keyPairJson = IOUtils.toString(getClass().getResourceAsStream("/keypair.json"), "UTF-8");
        base64EncodedPublicKey = IOUtils.toString(getClass().getResourceAsStream("/testkey.pub"), "UTF-8");
        bytePublicKey = Base64.getDecoder().decode(base64EncodedPublicKey);
    }

    @Test
    void convertFromRestKeyPair() throws JsonProcessingException, JSONException {
        KeyPair keyPair = converter.convertFromRestKeyPair(mapper.readValue(keyPairJson, RestKeyPair.class));
        JSONAssert.assertEquals(keyPairJson, mapper.writeValueAsString(converter.convertToRestKeyPair(keyPair)), true);
    }

    @Test
    void convertByteArrayToPublicKey() {
        PublicKey publicKey = converter.convertByteArrayToPublicKey(bytePublicKey);
        assertEquals(base64EncodedPublicKey, Base64.getEncoder().encodeToString(converter.convertPublicKeyToByteArray(publicKey)));
    }
}

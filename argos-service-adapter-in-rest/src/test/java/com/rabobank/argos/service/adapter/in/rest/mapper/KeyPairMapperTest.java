package com.rabobank.argos.service.adapter.in.rest.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabobank.argos.domain.model.KeyPair;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestKeyPair;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.security.PublicKey;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = {KeyPairMapperImpl.class})
class KeyPairMapperTest {

    @Autowired
    private KeyPairMapper converter;
    private ObjectMapper mapper;
    private String keyPairJson;
    private byte[] bytePublicKey;
    private String base64EncodedPublicKey;


    @BeforeEach
    void setUp() throws IOException {
        mapper = new ObjectMapper();
        keyPairJson = IOUtils.toString(LinkMetaBlockMapperTest.class.getResourceAsStream("/keypair.json"), "UTF-8");
        base64EncodedPublicKey = IOUtils.toString(LinkMetaBlockMapperTest.class.getResourceAsStream("/testkey.pub"), "UTF-8");
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
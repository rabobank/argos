package com.rabobank.argos.service.adapter.out.mongodb.converter;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Base64;

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
    void convert() {

    }
}
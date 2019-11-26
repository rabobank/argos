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
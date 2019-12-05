package com.rabobank.argos.service.adapter.out.mongodb.key.converter;

import org.apache.commons.io.IOUtils;
import org.bson.types.Binary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.security.PublicKey;
import java.util.Base64;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ByteArrayToPublicKeyToReadConverterTest {

    @Mock
    private Binary binary;
    private byte[] bytePublicKey;
    private String base64EncodedPublicKey;
    private ByteArrayToPublicKeyToReadConverter byteArrayToPublicKeyToReadConverter;

    @BeforeEach
    void setup() throws IOException {
        base64EncodedPublicKey = IOUtils.toString(ByteArrayToPublicKeyToReadConverterTest.class.getResourceAsStream("/testkey.pub"), "UTF-8");
        bytePublicKey = Base64.getDecoder().decode(base64EncodedPublicKey);
        byteArrayToPublicKeyToReadConverter = new ByteArrayToPublicKeyToReadConverter();
    }

    @Test
    void testConvert() {
        when(binary.getData()).thenReturn(bytePublicKey);
        PublicKey publicKey = byteArrayToPublicKeyToReadConverter.convert(binary);
        assertThat(publicKey.getEncoded(), is(bytePublicKey));
    }

    @Test
    void testConvertShouldThrowError() {
        when(binary.getData()).thenReturn(new byte[0]);
        assertThrows(RuntimeException.class, () -> {
            byteArrayToPublicKeyToReadConverter.convert(binary);
        });
    }
}

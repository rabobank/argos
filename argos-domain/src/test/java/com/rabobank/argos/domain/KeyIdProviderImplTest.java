package com.rabobank.argos.domain;

import com.rabobank.argos.domain.key.KeyIdProviderImpl;
import com.rabobank.argos.domain.key.RSAPublicKeyFactory;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

class KeyIdProviderImplTest {

    @Test
    void computeKeyId() throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
        byte[] decode = Base64.getDecoder().decode(IOUtils.toByteArray(this.getClass().getResourceAsStream("/publickey.txt")));
        String keyId = new KeyIdProviderImpl().computeKeyId(RSAPublicKeyFactory.instance(decode));
        assertThat(keyId, is("1aaf91a3f8e540cfd9ebbacd6147d43c76abefc535feefd85592197055bea1c8"));
    }
}
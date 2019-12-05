package com.rabobank.argos.argos4j.internal;

import com.rabobank.argos.argos4j.Argos4jError;
import com.rabobank.argos.argos4j.SigningKey;
import com.rabobank.argos.domain.Signature;
import com.rabobank.argos.domain.key.KeyIdProvider;
import com.rabobank.argos.domain.key.KeyIdProviderImpl;
import org.apache.commons.codec.binary.Hex;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;

public class Argos4JSigner {

    private KeyIdProvider keyIdProvider = new KeyIdProviderImpl();

    public  Signature sign(SigningKey signingKey, String jsonRepresentation) {
        String keyId = keyIdProvider.computeKeyId(signingKey.getKeyPair().getPublic());
        return Signature.builder().keyId(keyId).signature(createSignature(signingKey.getKeyPair().getPrivate(), jsonRepresentation)).build();
    }

    private static String createSignature(PrivateKey privateKey, String jsonRepr) {
        try {
            java.security.Signature privateSignature = java.security.Signature.getInstance("SHA256withRSA");
            privateSignature.initSign(privateKey);
            privateSignature.update(jsonRepr.getBytes(StandardCharsets.UTF_8));
            return Hex.encodeHexString(privateSignature.sign());
        } catch (GeneralSecurityException e) {
            throw new Argos4jError(e.getMessage(), e);
        }
    }
}

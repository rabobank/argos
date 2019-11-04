package com.rabobank.argos.argos4j.internal;

import com.rabobank.argos.argos4j.SigningKey;
import com.rabobank.argos.domain.model.Signature;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.signers.PSSSigner;
import org.bouncycastle.crypto.util.PublicKeyFactory;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStreamReader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Argos4JSignerTest {

    private Argos4JSigner signer;
    private SigningKey signingKey;

    @BeforeEach
    void setUp() throws IOException {
        signer = new Argos4JSigner();
        signingKey = SigningKey.builder()
                .pemKey(IOUtils.toByteArray(getClass().getResourceAsStream("/bob.key")))
                .build();
    }

    @Test
    void sign() throws IOException {
        Signature signature = signer.sign(signingKey, "string to sign");
        assertThat(signature.getKeyId(), is("35ad968500981d129660dceab11f00bb2e84fb763c2dd48674b0a48c1aa03829"));

        PEMParser pemReader = new PEMParser(new InputStreamReader(getClass().getResourceAsStream("/bob.key")));
        PEMKeyPair keyPair = (PEMKeyPair) pemReader.readObject();

        AsymmetricKeyParameter publKey = PublicKeyFactory.createKey(keyPair.getPublicKeyInfo());

        AsymmetricBlockCipher engine = new RSAEngine();
        SHA256Digest digest = new SHA256Digest();
        PSSSigner pssSigner = new PSSSigner(engine, digest, digest.getDigestSize());
        pssSigner.init(false, publKey);

        pssSigner.update("string to sign".getBytes(), 0, "string to sign".getBytes().length);

        assertTrue(pssSigner.verifySignature(Hex.decodeStrict(signature.getSignature())));

    }
}
package com.rabobank.argos.argos4j.internal;

import com.rabobank.argos.argos4j.Argos4jError;
import com.rabobank.argos.argos4j.SigningKey;
import com.rabobank.argos.domain.model.Signature;
import org.apache.commons.io.input.CharSequenceReader;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.Signer;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.signers.PSSSigner;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.util.encoders.Hex;

import java.io.IOException;
import java.io.Reader;

public class Argos4JSigner {

    public Signature sign(SigningKey signingKey, String jsonRepr) {
        return Signature.builder().keyId(computeKeyId(jsonRepr)).signature(createSignature(signingKey, jsonRepr)).build();
    }

    private String createSignature(SigningKey signingKey, String jsonRepr) {
        byte[] payload = jsonRepr.getBytes();
        AsymmetricKeyParameter privateKeyParameter = getPrivateKeyParameter(signingKey);
        Signer signer = getSigner(privateKeyParameter);
        signer.init(true, privateKeyParameter);
        signer.update(payload, 0, payload.length);
        try {
            return Hex.toHexString(signer.generateSignature());
        } catch (CryptoException e) {
            throw new Argos4jError("Couldn't sign payload!: " + e.getMessage(), e);
        }
    }

    private Signer getSigner(AsymmetricKeyParameter privateKeyParameter) {
        AsymmetricBlockCipher engine = new RSAEngine();
        engine.init(false, privateKeyParameter);
        SHA256Digest digest = new SHA256Digest();
        return new PSSSigner(engine, digest, digest.getDigestSize());
    }

    private AsymmetricKeyParameter getPrivateKeyParameter(SigningKey signingKey) {
        try {
            return PrivateKeyFactory.createKey(getPemKeyPair(signingKey).getPrivateKeyInfo());
        } catch (IOException e) {
            throw new Argos4jError(e.toString(), e);
        }
    }

    private PEMKeyPair getPemKeyPair(SigningKey signingKey) {
        try (Reader reader = new CharSequenceReader(new String(signingKey.getKey()));
             PEMParser pemReader = new PEMParser(reader)) {
            Object pem = pemReader.readObject();
            PEMKeyPair kpr;
            if (pem instanceof PEMKeyPair) {
                kpr = (PEMKeyPair) pem;
            } else if (pem instanceof SubjectPublicKeyInfo) {
                kpr = new PEMKeyPair((SubjectPublicKeyInfo) pem, null);
            } else {
                throw new Argos4jError("Couldn't parse PEM object: " + pem.toString());
            }
            return kpr;
        } catch (IOException e) {
            throw new Argos4jError(e.toString(), e);
        }
    }

    private String computeKeyId(String jsonRepr) {
        // initialize digest
        byte[] jsonReprBytes = jsonRepr.getBytes();
        SHA256Digest digest = new SHA256Digest();
        byte[] result = new byte[digest.getDigestSize()];
        digest.update(jsonReprBytes, 0, jsonReprBytes.length);
        digest.doFinal(result, 0);
        return Hex.toHexString(result);
    }
}

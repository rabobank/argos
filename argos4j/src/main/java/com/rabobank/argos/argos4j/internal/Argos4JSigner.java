package com.rabobank.argos.argos4j.internal;

import com.rabobank.argos.argos4j.Argos4jError;
import com.rabobank.argos.argos4j.SigningKey;
import com.rabobank.argos.domain.model.Signature;
import org.apache.commons.io.input.CharSequenceReader;
import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;

public class Argos4JSigner {

    public Signature sign(SigningKey signingKey, String jsonRepresentation) {
        PEMKeyPair keyPair = getPemKeyPair(signingKey);
        return Signature.builder().keyId(computeKeyId(keyPair.getPublicKeyInfo())).signature(createSignature(keyPair.getPrivateKeyInfo(), jsonRepresentation)).build();
    }

    private String createSignature(PrivateKeyInfo signingKey, String jsonRepr) {
        byte[] payload = jsonRepr.getBytes();
        AsymmetricKeyParameter privateKeyParameter = getPrivateKeyParameter(signingKey);
        Signer signer = getSigner();
        signer.init(true, privateKeyParameter);
        signer.update(payload, 0, payload.length);
        try {
            return Hex.toHexString(signer.generateSignature());
        } catch (CryptoException e) {
            throw new Argos4jError("Couldn't sign payload!: " + e.getMessage(), e);
        }
    }

    private Signer getSigner() {
        SHA256Digest digest = new SHA256Digest();
        return new PSSSigner(new RSAEngine(), digest, digest.getDigestSize());
    }

    private AsymmetricKeyParameter getPrivateKeyParameter(PrivateKeyInfo signingKey) {
        try {
            return PrivateKeyFactory.createKey(signingKey);
        } catch (IOException e) {
            throw new Argos4jError(e.toString(), e);
        }
    }

    private PEMKeyPair getPemKeyPair(SigningKey signingKey) {
        try (Reader reader = new CharSequenceReader(new String(signingKey.getPemKey()));
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

    private String computeKeyId(SubjectPublicKeyInfo publicKey) {
        // initialize digest
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            publicKey.encodeTo(outputStream, ASN1Encoding.BER);
            byte[] bytes = outputStream.toByteArray();
            SHA256Digest digest = new SHA256Digest();
            byte[] result = new byte[digest.getDigestSize()];
            digest.update(bytes, 0, bytes.length);
            digest.doFinal(result, 0);
            return Hex.toHexString(result);
        } catch (IOException e) {
            throw new Argos4jError(e.toString(), e);
        }
    }
}

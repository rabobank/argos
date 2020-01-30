/*
 * Copyright (C) 2019 - 2020 Rabobank Nederland
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
package com.rabobank.argos.argos4j.internal;

import com.rabobank.argos.argos4j.rest.api.model.RestKeyPair;
import com.rabobank.argos.domain.Signature;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.Cipher;
import javax.crypto.EncryptedPrivateKeyInfo;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.SignatureException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Argos4JSignerTest {

    public static final String PBE_WITH_SHA_1_AND_DE_SEDE = "PBEWithSHA1AndDESede";

    private static final char[] PASSWORD = "password".toCharArray();
    private static final String KEY_ID = "keyId";
    private Argos4JSigner signer;

    private RestKeyPair pair;
    private PublicKey publicKey;

    @BeforeEach
    void setUp() throws NoSuchAlgorithmException {

        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();
        publicKey = keyPair.getPublic();
        pair = new RestKeyPair().keyId(KEY_ID).encryptedPrivateKey(addPassword(keyPair.getPrivate().getEncoded(), PASSWORD)).publicKey(publicKey.getEncoded());

        signer = new Argos4JSigner();

    }

    @Test
    void sign() throws NoSuchAlgorithmException, DecoderException, SignatureException, InvalidKeyException {
        Signature signature = signer.sign(pair, PASSWORD, "string to sign");
        assertThat(signature.getKeyId(), is(KEY_ID));

        java.security.Signature signatureValidator = java.security.Signature.getInstance("SHA256WithRSA");
        signatureValidator.initVerify(publicKey);
        signatureValidator.update("string to sign".getBytes(StandardCharsets.UTF_8));

        assertTrue(signatureValidator.verify(Hex.decodeHex(signature.getSignature())));

    }

    private byte[] addPassword(byte[] encodedprivkey, char[] password) {
        // extract the encoded private key, this is an unencrypted PKCS#8 private key

        try {
            int count = 20;// hash iteration count
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[8];
            random.nextBytes(salt);

            // Create PBE parameter set
            PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt, count);
            PBEKeySpec pbeKeySpec = new PBEKeySpec(password);
            SecretKeyFactory keyFac = SecretKeyFactory.getInstance(PBE_WITH_SHA_1_AND_DE_SEDE);
            SecretKey pbeKey = keyFac.generateSecret(pbeKeySpec);

            Cipher pbeCipher = Cipher.getInstance(PBE_WITH_SHA_1_AND_DE_SEDE);

            // Initialize PBE Cipher with key and parameters
            pbeCipher.init(Cipher.ENCRYPT_MODE, pbeKey, pbeParamSpec);

            // Encrypt the encoded Private Key with the PBE key
            byte[] ciphertext = pbeCipher.doFinal(encodedprivkey);

            // Now construct  PKCS #8 EncryptedPrivateKeyInfo object
            AlgorithmParameters algparms = AlgorithmParameters.getInstance(PBE_WITH_SHA_1_AND_DE_SEDE);
            algparms.init(pbeParamSpec);
            EncryptedPrivateKeyInfo encinfo = new EncryptedPrivateKeyInfo(algparms, ciphertext);


            // and here we have it! a DER encoded PKCS#8 encrypted key!
            return encinfo.getEncoded();
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}

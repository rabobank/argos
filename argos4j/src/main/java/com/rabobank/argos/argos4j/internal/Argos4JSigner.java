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

import com.rabobank.argos.argos4j.Argos4jError;
import com.rabobank.argos.argos4j.rest.api.model.RestNonPersonalAccountKeyPair;
import com.rabobank.argos.domain.ArgosError;
import com.rabobank.argos.domain.Signature;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8DecryptorProviderBuilder;
import org.bouncycastle.operator.InputDecryptorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.bouncycastle.pkcs.PKCSException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.Security;

@Slf4j
public class Argos4JSigner {

    public Argos4JSigner() {
        Security.addProvider(new BouncyCastleProvider());
    }

    public Signature sign(RestNonPersonalAccountKeyPair keyPair, char[] keyPassphrase, String jsonRepresentation) {
        return Signature.builder().keyId(keyPair.getKeyId())
                .signature(createSignature(decryptPrivateKey(keyPair.getEncryptedPrivateKey(), keyPassphrase), jsonRepresentation))
                .build();
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

    private static PrivateKey decryptPrivateKey(byte[] encodedPrivateKey, char[] keyPassphrase) {
        try {
            PKCS8EncryptedPrivateKeyInfo encPKInfo = new PKCS8EncryptedPrivateKeyInfo(encodedPrivateKey);
            log.info("EncryptionAlgorithm : {}", encPKInfo.getEncryptionAlgorithm().getAlgorithm());
            InputDecryptorProvider decProv = new JceOpenSSLPKCS8DecryptorProviderBuilder().setProvider("BC").build(keyPassphrase);
            PrivateKeyInfo pkInfo = encPKInfo.decryptPrivateKeyInfo(decProv);
            return new JcaPEMKeyConverter().setProvider("BC").getPrivateKey(pkInfo);
        } catch (IOException | PKCSException | OperatorCreationException e) {
            throw new ArgosError(e.getMessage(), e);
        }
    }
}

package com.rabobank.argos.argos4j.internal;

/*-
 * #%L
 * Argos Supply Chain Notary
 * %%
 * Copyright (C) 2019 Rabobank Nederland
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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

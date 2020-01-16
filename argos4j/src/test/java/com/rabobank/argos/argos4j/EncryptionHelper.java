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
package com.rabobank.argos.argos4j;

import javax.crypto.Cipher;
import javax.crypto.EncryptedPrivateKeyInfo;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

public class EncryptionHelper {

    private static final String ALGORITHM = "PBEWithSHA1AndDESede";

    public static byte[] addPassword(byte[] encodedprivkey, char[] password) {


        try {
            int count = 20;// hash iteration count
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[8];
            random.nextBytes(salt);


            // Create PBE parameter set
            PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt, count);


            PBEKeySpec pbeKeySpec = new PBEKeySpec(password);
            SecretKeyFactory keyFac = SecretKeyFactory.getInstance(ALGORITHM);
            SecretKey pbeKey = keyFac.generateSecret(pbeKeySpec);

            Cipher pbeCipher = Cipher.getInstance(ALGORITHM);

            // Initialize PBE Cipher with key and parameters
            pbeCipher.init(Cipher.ENCRYPT_MODE, pbeKey, pbeParamSpec);

            // Encrypt the encoded Private Key with the PBE key
            byte[] ciphertext = pbeCipher.doFinal(encodedprivkey);

            // Now construct  PKCS #8 EncryptedPrivateKeyInfo object
            AlgorithmParameters algparms = AlgorithmParameters.getInstance(ALGORITHM);
            algparms.init(pbeParamSpec);
            EncryptedPrivateKeyInfo encinfo = new EncryptedPrivateKeyInfo(algparms, ciphertext);


            // and here we have it! a DER encoded PKCS#8 encrypted key!
            return encinfo.getEncoded();
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
    }

}

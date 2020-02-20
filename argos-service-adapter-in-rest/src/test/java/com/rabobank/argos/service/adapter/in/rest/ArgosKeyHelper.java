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
package com.rabobank.argos.service.adapter.in.rest;

import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import com.rabobank.argos.domain.key.KeyIdProvider;
import com.rabobank.argos.domain.key.KeyPair;
import com.rabobank.argos.domain.layout.PublicKey;

public class ArgosKeyHelper {
    
    public static KeyPair generateKeyPair() {
        java.security.KeyPair keyPair = genKeyPair();
        return KeyPair.builder().keyId(KeyIdProvider.computeKeyId(keyPair.getPublic())).publicKey(keyPair.getPublic()).build();
    }
    
    public static java.security.PublicKey generatePublickKey() {
        return genKeyPair().getPublic();
    }
    
    public static PublicKey generateArgosPublickKey() {
        java.security.PublicKey publicKey = generatePublickKey();
        return PublicKey.builder().id(KeyIdProvider.computeKeyId(publicKey)).key(publicKey).build();
    }
    
    private static java.security.KeyPair genKeyPair() {

        KeyPairGenerator keyGen = null;
        try {
            keyGen = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        keyGen.initialize(2048);
        return keyGen.genKeyPair();
        
    }

}

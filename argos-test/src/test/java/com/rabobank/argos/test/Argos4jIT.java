/*
 * Copyright (C) 2019 Rabobank Nederland
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
package com.rabobank.argos.test;

import com.rabobank.argos.argos4j.Argos4j;
import com.rabobank.argos.argos4j.Argos4jSettings;
import com.rabobank.argos.argos4j.SigningKey;
import com.rabobank.argos.argos4j.rest.api.model.RestCreateSupplyChainCommand;
import com.rabobank.argos.argos4j.rest.api.model.RestKeyPair;
import com.rabobank.argos.domain.key.KeyIdProviderImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

import static com.rabobank.argos.test.ServiceStatusHelper.clearDatabase;
import static com.rabobank.argos.test.ServiceStatusHelper.getKeyApiApi;
import static com.rabobank.argos.test.ServiceStatusHelper.getSupplychainApi;
import static com.rabobank.argos.test.ServiceStatusHelper.waitForArgosServiceToStart;

public class Argos4jIT {

    private static Properties properties = Properties.getInstance();
    private KeyPair keyPair;

    @BeforeAll
    static void setUp() {
        waitForArgosServiceToStart();
    }

    @BeforeEach
    void reset() throws NoSuchAlgorithmException {

        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        keyPair = generator.generateKeyPair();
        clearDatabase();
    }

    @Test
    void postLinkMetaBlockWithSignatureValidation() {

        PublicKey publicKey = keyPair.getPublic();
        String keyId = new KeyIdProviderImpl().computeKeyId(publicKey);

        getKeyApiApi().storeKey(new RestKeyPair().keyId(keyId).publicKey(publicKey.getEncoded()));
        getSupplychainApi().createSupplyChain(new RestCreateSupplyChainCommand().name("test-supply-chain"));

        Argos4jSettings settings = Argos4jSettings.builder()
                .argosServerBaseUrl(properties.getApiBaseUrl() + "/api")
                .stepName("build")
                .runId("runId")
                .supplyChainName("test-supply-chain")
                .signingKey(SigningKey.builder()
                        .keyPair(keyPair).build())
                .build();
        Argos4j argos4j = new Argos4j(settings);
        argos4j.collectProducts(new File("."));
        argos4j.collectMaterials(new File("."));
        argos4j.store();
    }
}

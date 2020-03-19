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
package com.rabobank.argos.test;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabobank.argos.argos4j.internal.ArgosServiceClient;
import com.rabobank.argos.argos4j.rest.api.client.NonPersonalAccountApi;
import com.rabobank.argos.argos4j.rest.api.client.PersonalAccountApi;
import com.rabobank.argos.argos4j.rest.api.model.RestKeyPair;
import com.rabobank.argos.argos4j.rest.api.model.RestLabel;
import com.rabobank.argos.argos4j.rest.api.model.RestLayoutMetaBlock;
import com.rabobank.argos.argos4j.rest.api.model.RestNonPersonalAccount;
import com.rabobank.argos.argos4j.rest.api.model.RestNonPersonalAccountKeyPair;
import com.rabobank.argos.domain.ArgosError;
import com.rabobank.argos.test.rest.api.ApiClient;
import com.rabobank.argos.test.rest.api.client.IntegrationTestServiceApi;
import com.rabobank.argos.test.rest.api.model.TestLayoutMetaBlock;
import com.rabobank.argos.test.rest.api.model.TestPersonalAccount;
import com.rabobank.argos.test.rest.api.model.TestPersonalAccountWithToken;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.mapstruct.factory.Mappers;

import java.io.IOException;
import java.util.List;

import static com.rabobank.argos.argos4j.rest.api.model.RestPermission.LAYOUT_ADD;
import static com.rabobank.argos.argos4j.rest.api.model.RestPermission.READ;
import static com.rabobank.argos.argos4j.rest.api.model.RestPermission.VERIFY;
import static com.rabobank.argos.test.ServiceStatusHelper.getHierarchyApi;
import static com.rabobank.argos.test.ServiceStatusHelper.getLayoutApi;
import static com.rabobank.argos.test.ServiceStatusHelper.getNonPersonalAccountApi;
import static com.rabobank.argos.test.ServiceStatusHelper.getPersonalAccountApi;
import static com.rabobank.argos.test.ServiceStatusHelper.getToken;

public class TestServiceHelper {

    private static Properties properties = Properties.getInstance();

    public static void clearDatabase() {
        getTestApi().resetDatabase();
    }

    public static IntegrationTestServiceApi getTestApi() {
        return getApiClient().buildClient(IntegrationTestServiceApi.class);
    }

    private static ApiClient getApiClient() {
        return new ApiClient().setBasePath(properties.getIntegrationTestServiceBaseUrl() + "/integration-test");
    }

    public static DefaultTestData createDefaultTestData() {
        getTestApi().resetDatabaseAll();
        DefaultTestData hierarchy = new DefaultTestData();
        hierarchy.setAdminToken(getToken());
        createDefaultRootLabel(hierarchy);
        createDefaultPersonalAccount(hierarchy);
        createDefaultNpaAccounts(hierarchy);
        return hierarchy;
    }

    private static void createDefaultRootLabel(DefaultTestData hierarchy) {
        hierarchy.setDefaultRootLabel(getHierarchyApi(hierarchy.getAdminToken()).createLabel(new RestLabel().name("default_root_label")));
    }

    public static void createDefaultPersonalAccount(DefaultTestData hierarchy) {
        IntegrationTestServiceApi testApi = getTestApi();
        TestPersonalAccount testPersonalAccount = new TestPersonalAccount();
        testPersonalAccount.setName("Default User");
        testPersonalAccount.setEmail("default@nl.nl");
        TestPersonalAccountWithToken personalAccountWithToken = testApi.createPersonalAccount(testPersonalAccount);
        PersonalAccountApi personalAccountApi = getPersonalAccountApi(hierarchy.getAdminToken());
        personalAccountApi.updateLocalPermissionsForLabel(personalAccountWithToken.getId(), hierarchy.getDefaultRootLabel().getId(), List.of(LAYOUT_ADD, READ, VERIFY));
        TestDateKeyPair keyPair = readKeyPair(1);
        getPersonalAccountApi(personalAccountWithToken.getToken()).createKey(new RestKeyPair()
                .encryptedPrivateKey(keyPair.getEncryptedPrivateKey())
                .publicKey(keyPair.getPublicKey())
                .keyId(keyPair.getKeyId()));
        hierarchy.getPersonalAccounts().put("default-pa1", DefaultTestData.PersonalAccount.builder()
                .passphrase(keyPair.getPassphrase())
                .keyId(keyPair.getKeyId())
                .token(personalAccountWithToken.getToken())
                .publicKey(keyPair.getPublicKey())
                .build());
    }

    public static void createDefaultNpaAccounts(DefaultTestData defaultTestData) {
        createNpaWithActiveKey(defaultTestData, readKeyPair(1), "default-npa1");
        createNpaWithActiveKey(defaultTestData, readKeyPair(2), "default-npa2");
        createNpaWithActiveKey(defaultTestData, readKeyPair(3), "default-npa3");
        createNpaWithActiveKey(defaultTestData, readKeyPair(4), "default-npa4");
        createNpaWithActiveKey(defaultTestData, readKeyPair(5), "default-npa5");
    }

    private static void createNpaWithActiveKey(DefaultTestData defaultTestData, TestDateKeyPair keyPair, String name) {
        NonPersonalAccountApi nonPersonalAccountApi = getNonPersonalAccountApi(defaultTestData.getPersonalAccounts().get("default-pa1").getToken());
        RestNonPersonalAccount npa = nonPersonalAccountApi.createNonPersonalAccount(new RestNonPersonalAccount().parentLabelId(defaultTestData.getDefaultRootLabel().getId()).name(name));

        String hashedKeyPassphrase = ArgosServiceClient.calculatePassphrase(keyPair.getKeyId(), keyPair.getPassphrase());

        nonPersonalAccountApi.createNonPersonalAccountKeyById(npa.getId(),
                new RestNonPersonalAccountKeyPair().keyId(keyPair.getKeyId())
                        .hashedKeyPassphrase(hashedKeyPassphrase)
                        .encryptedPrivateKey(keyPair.getEncryptedPrivateKey())
                        .publicKey(keyPair.getPublicKey()));
        defaultTestData.getNonPersonalAccount().put(name,
                DefaultTestData.NonPersonalAccount.builder()
                        .passphrase(keyPair.getPassphrase())
                        .keyId(keyPair.getKeyId())
                        .hashedKeyPassphrase(hashedKeyPassphrase)
                        .publicKey(keyPair.getPublicKey())
                        .build());
    }

    private static TestDateKeyPair readKeyPair(int index) {
        try {
            return new ObjectMapper().readValue(TestServiceHelper.class.getResourceAsStream("/testmessages/key/default-test-keypair" + index + ".json"), TestDateKeyPair.class);
        } catch (IOException e) {
            throw new ArgosError(e.getMessage(), e);
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    private static class TestDateKeyPair {
        private String keyId;
        private byte[] publicKey;
        private String passphrase;
        private byte[] encryptedPrivateKey;
    }

    public static void signAndStoreLayout(String token, String supplyChainId, RestLayoutMetaBlock restLayout, String keyId, String password) {
        RestMapper mapper = Mappers.getMapper(RestMapper.class);
        TestLayoutMetaBlock testLayout = mapper.mapRestLayout(restLayout);
        TestLayoutMetaBlock signed = getTestApi().signLayout(password, keyId, testLayout);
        getLayoutApi(token).createLayout(supplyChainId, mapper.mapTestLayout(signed));
    }

}

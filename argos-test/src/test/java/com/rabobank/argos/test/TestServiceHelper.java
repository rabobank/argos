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


import com.rabobank.argos.argos4j.rest.api.model.RestKeyPair;
import com.rabobank.argos.argos4j.rest.api.model.RestLayoutMetaBlock;
import com.rabobank.argos.argos4j.rest.api.model.RestNonPersonalAccount;
import com.rabobank.argos.argos4j.rest.api.model.RestNonPersonalAccountKeyPair;
import com.rabobank.argos.test.rest.api.ApiClient;
import com.rabobank.argos.test.rest.api.client.IntegrationTestServiceApi;
import com.rabobank.argos.test.rest.api.model.TestKeyPair;
import com.rabobank.argos.test.rest.api.model.TestLayoutMetaBlock;
import org.apache.commons.codec.digest.DigestUtils;
import org.mapstruct.factory.Mappers;

import static com.rabobank.argos.test.ServiceStatusHelper.getLayoutApi;
import static com.rabobank.argos.test.ServiceStatusHelper.getNonPersonalAccountApi;

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

    public static RestKeyPair createAndStoreKeyPair(String password, String parentLabelId) {
        RestMapper mapper = Mappers.getMapper(RestMapper.class);
        TestKeyPair layoutKeyPair = getTestApi().createKeyPair(password);
        RestKeyPair restKeyPair = mapper.mapTestKeyPair(layoutKeyPair);
        RestNonPersonalAccount npa = getNonPersonalAccountApi().createNonPersonalAccount(new RestNonPersonalAccount().name("npa").parentLabelId(parentLabelId));
        getNonPersonalAccountApi().createNonPersonalAccountKeyById(npa.getId(), new RestNonPersonalAccountKeyPair()
                .keyId(restKeyPair.getKeyId())
                .encryptedPrivateKey(restKeyPair.getEncryptedPrivateKey())
                .publicKey(restKeyPair.getPublicKey())
                .hashedKeyPassphrase(DigestUtils.sha256Hex(password)));
        return restKeyPair;
    }

    public static void signAndStoreLayout(String supplyChainId, RestLayoutMetaBlock restLayout, String keyId, String password) {
        RestMapper mapper = Mappers.getMapper(RestMapper.class);
        TestLayoutMetaBlock testLayout = mapper.mapRestLayout(restLayout);
        TestLayoutMetaBlock signed = getTestApi().signLayout(password, keyId, testLayout);
        getLayoutApi().createLayout(supplyChainId, mapper.mapTestLayout(signed));
    }

}

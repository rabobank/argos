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

import com.rabobank.argos.argos4j.Argos4j;
import com.rabobank.argos.argos4j.Argos4jSettings;
import com.rabobank.argos.argos4j.rest.api.client.KeyApi;
import com.rabobank.argos.argos4j.rest.api.model.RestArtifact;
import com.rabobank.argos.argos4j.rest.api.model.RestCreateSupplyChainCommand;
import com.rabobank.argos.argos4j.rest.api.model.RestKeyPair;
import com.rabobank.argos.argos4j.rest.api.model.RestLayout;
import com.rabobank.argos.argos4j.rest.api.model.RestLayoutMetaBlock;
import com.rabobank.argos.argos4j.rest.api.model.RestLayoutSegment;
import com.rabobank.argos.argos4j.rest.api.model.RestMatchRule;
import com.rabobank.argos.argos4j.rest.api.model.RestRule;
import com.rabobank.argos.argos4j.rest.api.model.RestStep;
import com.rabobank.argos.argos4j.rest.api.model.RestVerificationResult;
import com.rabobank.argos.argos4j.rest.api.model.RestVerifyCommand;
import com.rabobank.argos.domain.key.KeyIdProviderImpl;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;

import static com.rabobank.argos.test.ServiceStatusHelper.getKeyApi;
import static com.rabobank.argos.test.ServiceStatusHelper.getSupplychainApi;
import static com.rabobank.argos.test.ServiceStatusHelper.getVerificationApi;
import static com.rabobank.argos.test.ServiceStatusHelper.waitForArgosServiceToStart;
import static com.rabobank.argos.test.TestServiceHelper.clearDatabase;
import static com.rabobank.argos.test.TestServiceHelper.createAndStoreKeyPair;
import static com.rabobank.argos.test.TestServiceHelper.signAndStoreLayout;
import static org.hamcrest.MatcherAssert.assertThat;

public class Argos4jIT {

    private static Properties properties = Properties.getInstance();
    private RestKeyPair restKeyPair;

    @BeforeAll
    static void setUp() {
        waitForArgosServiceToStart();
    }

    @BeforeEach
    void reset() {
        clearDatabase();
    }

    @Test
    void postLinkMetaBlockWithSignatureValidationAndVerify() throws GeneralSecurityException {

        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();

        PublicKey publicKey = keyPair.getPublic();
        String keyId = new KeyIdProviderImpl().computeKeyId(publicKey);

        KeyApi keyApiApi = getKeyApi();
        keyApiApi.storeKey(new RestKeyPair().keyId(keyId).publicKey(publicKey.getEncoded()));
        String supplyChainId = getSupplychainApi().createSupplyChain(new RestCreateSupplyChainCommand().name("test-supply-chain")).getId();

        restKeyPair = createAndStoreKeyPair("test");

        RestLayoutMetaBlock layout = new RestLayoutMetaBlock().layout(createLayout(restKeyPair.getKeyId(), keyId));
        signAndStoreLayout(supplyChainId, layout, restKeyPair.getKeyId(), "test");


        Argos4jSettings settings = Argos4jSettings.builder()
                .argosServerBaseUrl(properties.getApiBaseUrl() + "/api")
                .layoutSegmentName("layoutSegmentName")
                .stepName("build")
                .runId("runId")
                .supplyChainName("test-supply-chain")
                .signingKeyId(restKeyPair.getKeyId())
                .build();
        Argos4j argos4j = new Argos4j(settings);
        argos4j.collectProducts(new File("."));
        argos4j.collectMaterials(new File("."));
        argos4j.store("test".toCharArray());

        RestVerificationResult verificationResult = getVerificationApi().performVerification(supplyChainId, new RestVerifyCommand()
                .addExpectedProductsItem(new RestArtifact().uri("src/test/resources/karate-config.js").hash("9b33afe5598c5ea4cc702b231b2a98a906bc2fdcd10ebab103bbb20596db07a2")));
        assertThat(verificationResult.getRunIsValid(), Matchers.is(true));
    }

    private RestLayout createLayout(String layoutKeyId, String linkKeyId) {
        return new RestLayout().addAuthorizedKeyIdsItem(layoutKeyId)
                .addExpectedEndProductsItem(new RestMatchRule()
                        .destinationSegmentName("layoutSegmentName")
                        .destinationStepName("build")
                        .destinationType(RestMatchRule.DestinationTypeEnum.PRODUCTS)
                        .pattern("**/karate-config.js"))
                .addLayoutSegmentsItem(new RestLayoutSegment().name("layoutSegmentName")
                        .addStepsItem(new RestStep().requiredNumberOfLinks(1)
                                .addAuthorizedKeyIdsItem(restKeyPair.getKeyId())
                                .addExpectedProductsItem(new RestRule().ruleType(RestRule.RuleTypeEnum.ALLOW).pattern("**"))
                                .addExpectedMaterialsItem(new RestRule().ruleType(RestRule.RuleTypeEnum.ALLOW).pattern("**"))
                                .addAuthorizedKeyIdsItem(linkKeyId).name("build")));
    }
}

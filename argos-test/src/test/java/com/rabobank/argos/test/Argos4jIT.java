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
import com.rabobank.argos.argos4j.rest.api.model.RestArtifact;
import com.rabobank.argos.argos4j.rest.api.model.RestKeyPair;
import com.rabobank.argos.argos4j.rest.api.model.RestLabel;
import com.rabobank.argos.argos4j.rest.api.model.RestLayout;
import com.rabobank.argos.argos4j.rest.api.model.RestLayoutMetaBlock;
import com.rabobank.argos.argos4j.rest.api.model.RestLayoutSegment;
import com.rabobank.argos.argos4j.rest.api.model.RestMatchRule;
import com.rabobank.argos.argos4j.rest.api.model.RestPublicKey;
import com.rabobank.argos.argos4j.rest.api.model.RestRule;
import com.rabobank.argos.argos4j.rest.api.model.RestStep;
import com.rabobank.argos.argos4j.rest.api.model.RestSupplyChain;
import com.rabobank.argos.argos4j.rest.api.model.RestVerificationResult;
import com.rabobank.argos.argos4j.rest.api.model.RestVerifyCommand;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static com.rabobank.argos.test.ServiceStatusHelper.getHierarchyApi;
import static com.rabobank.argos.test.ServiceStatusHelper.getSupplychainApi;
import static com.rabobank.argos.test.ServiceStatusHelper.getToken;
import static com.rabobank.argos.test.ServiceStatusHelper.getVerificationApi;
import static com.rabobank.argos.test.ServiceStatusHelper.waitForArgosServiceToStart;
import static com.rabobank.argos.test.TestServiceHelper.clearDatabase;
import static com.rabobank.argos.test.TestServiceHelper.createAndStoreKeyPair;
import static com.rabobank.argos.test.TestServiceHelper.createPersonalAccountTokenWithLayoutPermissions;
import static com.rabobank.argos.test.TestServiceHelper.signAndStoreLayout;
import static org.hamcrest.MatcherAssert.assertThat;

class Argos4jIT {

    private static Properties properties = Properties.getInstance();
    private RestKeyPair keyPair;

    @BeforeAll
    static void setUp() {
        waitForArgosServiceToStart();
    }

    @BeforeEach
    void reset() {
        clearDatabase();
    }

    @Test
    void postLinkMetaBlockWithSignatureValidationAndVerify() {

        String adminAccountToken = getToken();
        RestLabel rootLabel = getHierarchyApi(adminAccountToken).createLabel(new RestLabel().name("root_label"));
        RestLabel childLabel = getHierarchyApi(adminAccountToken).createLabel(new RestLabel().name("child_label").parentLabelId(rootLabel.getId()));
        String supplyChainId = getSupplychainApi(adminAccountToken).createSupplyChain(new RestSupplyChain().name("test-supply-chain").parentLabelId(childLabel.getId())).getId();
        keyPair = createAndStoreKeyPair(adminAccountToken, "test", childLabel.getId());
        RestLayoutMetaBlock layout = new RestLayoutMetaBlock().layout(createLayout());
        String accountWithLayoutPermissionsToken = createPersonalAccountTokenWithLayoutPermissions(adminAccountToken, childLabel.getId());
        signAndStoreLayout(accountWithLayoutPermissionsToken, supplyChainId, layout, keyPair.getKeyId(), "test");
        Argos4jSettings settings = Argos4jSettings.builder()
                .argosServerBaseUrl(properties.getApiBaseUrl() + "/api")
                .layoutSegmentName("layoutSegmentName")
                .stepName("build")
                .runId("runId")
                .supplyChainName("test-supply-chain")
                .pathToLabelRoot(List.of("child_label", "root_label"))
                .signingKeyId(keyPair.getKeyId())
                .build();
        Argos4j argos4j = new Argos4j(settings);
        argos4j.collectProducts(new File("."));
        argos4j.collectMaterials(new File("."));
        argos4j.store("test".toCharArray());

        RestVerificationResult verificationResult = getVerificationApi(adminAccountToken).performVerification(supplyChainId, new RestVerifyCommand()
                .addExpectedProductsItem(new RestArtifact().uri("src/test/resources/karate-config.js").hash("9b33afe5598c5ea4cc702b231b2a98a906bc2fdcd10ebab103bbb20596db07a2")));
        assertThat(verificationResult.getRunIsValid(), Matchers.is(true));
    }

    private RestLayout createLayout() {
        return new RestLayout()
                .addKeysItem(new RestPublicKey().id(keyPair.getKeyId()).key(keyPair.getPublicKey()))
                .addAuthorizedKeyIdsItem(keyPair.getKeyId())
                .addExpectedEndProductsItem(new RestMatchRule()
                        .destinationSegmentName("layoutSegmentName")
                        .destinationStepName("build")
                        .destinationType(RestMatchRule.DestinationTypeEnum.PRODUCTS)
                        .pattern("**/karate-config.js"))
                .addLayoutSegmentsItem(new RestLayoutSegment().name("layoutSegmentName")
                        .addStepsItem(new RestStep().requiredNumberOfLinks(1)
                                .addAuthorizedKeyIdsItem(keyPair.getKeyId())
                                .addExpectedProductsItem(new RestRule().ruleType(RestRule.RuleTypeEnum.ALLOW).pattern("**"))
                                .addExpectedMaterialsItem(new RestRule().ruleType(RestRule.RuleTypeEnum.ALLOW).pattern("**"))
                                .name("build")));
    }
}

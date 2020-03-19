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
import com.rabobank.argos.argos4j.FileCollector;
import com.rabobank.argos.argos4j.LinkBuilder;
import com.rabobank.argos.argos4j.LinkBuilderSettings;
import com.rabobank.argos.argos4j.LocalFileCollector;
import com.rabobank.argos.argos4j.VerifyBuilder;
import com.rabobank.argos.argos4j.rest.api.model.RestLabel;
import com.rabobank.argos.argos4j.rest.api.model.RestLayout;
import com.rabobank.argos.argos4j.rest.api.model.RestLayoutMetaBlock;
import com.rabobank.argos.argos4j.rest.api.model.RestLayoutSegment;
import com.rabobank.argos.argos4j.rest.api.model.RestMatchRule;
import com.rabobank.argos.argos4j.rest.api.model.RestPublicKey;
import com.rabobank.argos.argos4j.rest.api.model.RestRule;
import com.rabobank.argos.argos4j.rest.api.model.RestStep;
import com.rabobank.argos.argos4j.rest.api.model.RestSupplyChain;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static com.rabobank.argos.test.ServiceStatusHelper.getHierarchyApi;
import static com.rabobank.argos.test.ServiceStatusHelper.getSupplychainApi;
import static com.rabobank.argos.test.ServiceStatusHelper.waitForArgosServiceToStart;
import static com.rabobank.argos.test.TestServiceHelper.clearDatabase;
import static com.rabobank.argos.test.TestServiceHelper.createDefaultHierarchy;
import static com.rabobank.argos.test.TestServiceHelper.signAndStoreLayout;
import static org.hamcrest.MatcherAssert.assertThat;

class Argos4jIT {

    private static Properties properties = Properties.getInstance();
    private DefaultTestData.NonPersonalAccount nonPersonalAccount;
    private DefaultTestData.PersonalAccount personalAccount;

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

        DefaultTestData defaultTestData = createDefaultHierarchy();
        String adminAccountToken = defaultTestData.getAdminToken();
        getHierarchyApi(adminAccountToken).updateLabelById(defaultTestData.getDefaultRootLabel().getId(), new RestLabel().name("root_label"));
        RestLabel childLabel = getHierarchyApi(adminAccountToken).createLabel(new RestLabel().name("child_label").parentLabelId(defaultTestData.getDefaultRootLabel().getId()));

        String supplyChainId = getSupplychainApi(adminAccountToken).createSupplyChain(new RestSupplyChain().name("test-supply-chain").parentLabelId(childLabel.getId())).getId();

        nonPersonalAccount = defaultTestData.getNonPersonalAccount().values().iterator().next();

        personalAccount = defaultTestData.getPersonalAccounts().values().iterator().next();

        RestLayoutMetaBlock layout = new RestLayoutMetaBlock().layout(createLayout());

        signAndStoreLayout(personalAccount.getToken(), supplyChainId, layout, personalAccount.getKeyId(), personalAccount.getPassphrase());
        Argos4jSettings settings = Argos4jSettings.builder()
                .argosServerBaseUrl(properties.getApiBaseUrl() + "/api")
                .supplyChainName("test-supply-chain")
                .pathToLabelRoot(List.of("child_label", "root_label"))
                .signingKeyId(nonPersonalAccount.getKeyId())
                .build();
        Argos4j argos4j = new Argos4j(settings);
        LinkBuilder linkBuilder = argos4j.getLinkBuilder(LinkBuilderSettings.builder().layoutSegmentName("layoutSegmentName").stepName("build").runId("runId").build());
        FileCollector fileCollector = LocalFileCollector.builder().path(new File(".").toPath()).basePath(new File(".").toPath()).build();
        linkBuilder.collectProducts(fileCollector);
        linkBuilder.collectMaterials(fileCollector);
        linkBuilder.store(nonPersonalAccount.getPassphrase().toCharArray());


        VerifyBuilder verifyBuilder = argos4j.getVerifyBuilder();

        File fileToVerify = new File("src/test/resources/karate-config.js");

        boolean runIsValid = verifyBuilder.addFileCollector(LocalFileCollector.builder()
                .path(fileToVerify.toPath())
                .basePath(fileToVerify.toPath().getParent()).build())
                .verify("test".toCharArray()).isRunIsValid();

        assertThat(runIsValid, Matchers.is(true));
    }

    private RestLayout createLayout() {
        return new RestLayout()
                .addKeysItem(new RestPublicKey().id(personalAccount.getKeyId()).key(personalAccount.getPublicKey()))
                .addKeysItem(new RestPublicKey().id(nonPersonalAccount.getKeyId()).key(nonPersonalAccount.getPublicKey()))
                .addAuthorizedKeyIdsItem(personalAccount.getKeyId())
                .addExpectedEndProductsItem(new RestMatchRule()
                        .destinationSegmentName("layoutSegmentName")
                        .destinationStepName("build")
                        .destinationType(RestMatchRule.DestinationTypeEnum.PRODUCTS)
                        .destinationPathPrefix("src/test/resources/")
                        .pattern("karate-config.js"))
                .addLayoutSegmentsItem(new RestLayoutSegment().name("layoutSegmentName")
                        .addStepsItem(new RestStep().requiredNumberOfLinks(1)
                                .addAuthorizedKeyIdsItem(nonPersonalAccount.getKeyId())
                                .addExpectedProductsItem(new RestRule().ruleType(RestRule.RuleTypeEnum.ALLOW).pattern("**"))
                                .addExpectedMaterialsItem(new RestRule().ruleType(RestRule.RuleTypeEnum.ALLOW).pattern("**"))
                                .name("build")));
    }
}

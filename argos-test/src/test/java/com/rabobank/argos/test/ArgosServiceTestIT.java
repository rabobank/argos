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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.intuit.karate.KarateOptions;
import com.intuit.karate.junit5.Karate;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;

import static com.rabobank.argos.test.ServiceStatusHelper.waitForArgosIntegrationTestServiceToStart;
import static com.rabobank.argos.test.ServiceStatusHelper.waitForArgosServiceToStart;
import static com.rabobank.argos.test.TestServiceHelper.createDefaultTestData;

@Slf4j
@KarateOptions(tags = {"~@ignore"})
class ArgosServiceTestIT {

    private static final String SERVER_BASEURL = "server.baseurl";
    private static final String SERVER_INTEGRATION_TEST_BASEURL = "server.integration-test-service.baseurl";
    private static final String DEFAULT_TESTDATA = "default-testdata";
    private static Properties properties = Properties.getInstance();

    @BeforeAll
    static void setUp() throws JsonProcessingException {
        log.info("karate base url : {}", properties.getApiBaseUrl());
        System.setProperty(SERVER_BASEURL, properties.getApiBaseUrl());
        System.setProperty(SERVER_INTEGRATION_TEST_BASEURL, properties.getIntegrationTestServiceBaseUrl());
        waitForArgosServiceToStart();
        waitForArgosIntegrationTestServiceToStart();
        DefaultTestData defaultTestData = createDefaultTestData();
        ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        String defaultTestDataJson = objectMapper.writeValueAsString(defaultTestData);
        System.setProperty(DEFAULT_TESTDATA, defaultTestDataJson);
        log.info("default test data: {}", defaultTestDataJson);

    }


    @Karate.Test
    Karate link() {
        return new Karate().feature("classpath:feature/link/link.feature");
    }

    @Karate.Test
    Karate supplyChain() {
        return new Karate().feature("classpath:feature/supplychain/supplychain.feature");
    }

    @Karate.Test
    Karate layout() {
        return new Karate().feature("classpath:feature/layout/layout.feature");
    }

    @Karate.Test
    Karate verification() {
        return new Karate().feature("classpath:feature/verification/verification.feature");
    }

    @Karate.Test
    Karate personalaccount() {
        return new Karate().feature("classpath:feature/account/personalaccount.feature");
    }

    @Karate.Test
    Karate label() {
        return new Karate().feature("classpath:feature/label/label.feature");
    }

    @Karate.Test
    Karate hierarchy() {
        return new Karate().feature("classpath:feature/hierarchy/hierarchy.feature");
    }

    @Karate.Test
    Karate nonPersonalAccount() {
        return new Karate().feature("classpath:feature/account/non-personal-account.feature");
    }

    @Karate.Test
    Karate permission() {
        return new Karate().feature("classpath:feature/permission/permission.feature");
    }

}

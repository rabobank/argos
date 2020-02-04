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

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.Getter;

@Getter
public class Properties {

    private final String apiBaseUrl;
    private static Properties INSTANCE;
    private final String jenkinsBaseUrl;
    private final String integrationTestServiceBaseUrl;
    private final String nexusDarSnapshotUrl;
    private final String nexusWarSnapshotUrl;
    private final String argosTestAppBranch;

    public static Properties getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Properties();
        }
        return INSTANCE;
    }

    private Properties() {
        Config conf = ConfigFactory.load();
        apiBaseUrl = conf.getString("argos-service.rest-api.base-url");
        jenkinsBaseUrl = conf.getString("jenkins.base-url");
        integrationTestServiceBaseUrl = conf.getString("argos-integration-test-service.rest-api.base-url");
        nexusDarSnapshotUrl = conf.getString("nexus.dar-snapshot-url");
        nexusWarSnapshotUrl = conf.getString("nexus.war-snapshot-url");
        argosTestAppBranch = conf.getString("argos-test-app.branch");

    }
}


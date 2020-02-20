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
package com.rabobank.argos.service.security.oauth2.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class AzureOAuth2PersonalAccountInfoTest {

    private final static String ID = "id";
    private static final String DISPLAY_NAME = "diplayName";
    private static final String USER_PRINCIPAL_NAME = "userPrincipalName";
    private AzureOAuth2UserInfo userInfo;

    @BeforeEach
    void setUp() {
        Map<String, Object> attributes = Map.of("id", ID, "displayName", DISPLAY_NAME, "userPrincipalName", USER_PRINCIPAL_NAME);
        userInfo = new AzureOAuth2UserInfo(attributes);
    }

    @Test
    void getId() {
        assertThat(userInfo.getId(), is(ID));
    }

    @Test
    void getName() {
        assertThat(userInfo.getName(), is(DISPLAY_NAME));
    }

    @Test
    void getEmail() {
        assertThat(userInfo.getEmail(), is(USER_PRINCIPAL_NAME.toLowerCase()));
    }
}
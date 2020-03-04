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
package com.rabobank.argos.domain.account;

import com.rabobank.argos.domain.key.KeyPair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasLength;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;

@ExtendWith(MockitoExtension.class)
class PersonalAccountTest {

    private static final String EMAIL = "email";
    private static final String NAME = "name";
    private static final String PROVIDER_ID = "providerId";
    private static final String ROLE_ID = "roleId";

    @Mock
    private KeyPair activeKeyPair;

    @Mock
    private KeyPair keyPair;

    @Test
    void builder() {
        PersonalAccount account = PersonalAccount.builder().name(NAME)
                .email(EMAIL)
                .activeKeyPair(activeKeyPair)
                .inactiveKeyPairs(Collections.singletonList(keyPair))
                .provider(AuthenticationProvider.AZURE)
                .providerId(PROVIDER_ID)
                .roleIds(Collections.singletonList(ROLE_ID))
                .build();
        assertThat(account.getAccountId(), hasLength(36));
        assertThat(account.getName(), is(NAME));
        assertThat(account.getEmail(), is(EMAIL));
        assertThat(account.getActiveKeyPair(), sameInstance(activeKeyPair));
        assertThat(account.getProvider(), is(AuthenticationProvider.AZURE));
        assertThat(account.getInactiveKeyPairs(), contains(keyPair));
        assertThat(account.getProviderId(), is(PROVIDER_ID));
        assertThat(account.getRoleIds(), contains(ROLE_ID));
    }
}
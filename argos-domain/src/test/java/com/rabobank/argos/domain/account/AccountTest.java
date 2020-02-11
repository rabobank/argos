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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.nullValue;

class AccountTest {

    private static final String ACCOUNT_ID = "accountId";
    private static final String ACCOUNT_NAME = "accountName";

    private KeyPair activeKeyPair = KeyPair.builder().build();
    private KeyPair inactiveKeyPair = KeyPair.builder().build();

    @BeforeEach
    void setUp() {
    }

    @Test
    void deactivateKeyPairNoActiveKeyAndNoInactiveKeys() {
        Account account = new Account(ACCOUNT_ID, ACCOUNT_NAME, null, null);
        account.deactivateKeyPair();
        assertThat(account.getInactiveKeyPairs(), nullValue());
        assertThat(account.getActiveKeyPair(), nullValue());
    }

    @Test
    void deactivateKeyPairNoActiveKey() {
        Account account = new Account(ACCOUNT_ID, ACCOUNT_NAME, activeKeyPair, null);
        account.deactivateKeyPair();
        assertThat(account.getInactiveKeyPairs(), contains(activeKeyPair));
        assertThat(account.getActiveKeyPair(), nullValue());
    }

    @Test
    void deactivateKeyPairNoActiveKeyAndEmptyList() {
        Account account = new Account(ACCOUNT_ID, ACCOUNT_NAME, activeKeyPair, Collections.emptyList());
        account.deactivateKeyPair();
        assertThat(account.getInactiveKeyPairs(), contains(activeKeyPair));
        assertThat(account.getActiveKeyPair(), nullValue());
    }

    @Test
    void deactivateKeyPairNoActiveKeyAndInactiveKeyPair() {
        Account account = new Account(ACCOUNT_ID, ACCOUNT_NAME, activeKeyPair, Collections.singletonList(inactiveKeyPair));
        account.deactivateKeyPair();
        assertThat(account.getInactiveKeyPairs(), contains(inactiveKeyPair, activeKeyPair));
        assertThat(account.getActiveKeyPair(), nullValue());
    }
}
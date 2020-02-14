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
package com.rabobank.argos.service.domain.account;

import com.rabobank.argos.domain.account.Account;
import com.rabobank.argos.domain.account.PersonalAccount;
import com.rabobank.argos.domain.key.KeyPair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyCollectionOf;
import static org.hamcrest.Matchers.sameInstance;

class AccountServiceImplTest {

    private static final String ACCOUNT_ID = "accountId";
    private static final String ACCOUNT_NAME = "accountName";

    private KeyPair activeKeyPair = new KeyPair();
    private KeyPair inactiveKeyPair = new KeyPair();
    private KeyPair newKeyPair = new KeyPair();
    private AccountServiceImpl accountService;

    @BeforeEach
    void setUp() {
        accountService = new AccountServiceImpl();
    }

    @Test
    void deactivateKeyPairNoActiveKeyAndNoInactiveKeys() {
        Account<KeyPair> account = new PersonalAccount(ACCOUNT_ID, ACCOUNT_NAME, null, null, null, null);
        accountService.activateNewKey(account, newKeyPair);
        assertThat(account.getInactiveKeyPairs(), emptyCollectionOf(KeyPair.class));
        assertThat(account.getActiveKeyPair(), sameInstance(newKeyPair));
    }

    @Test
    void deactivateKeyPairNoActiveKey() {
        Account<KeyPair> account = new PersonalAccount(ACCOUNT_ID, ACCOUNT_NAME, activeKeyPair, null, null, null);
        accountService.activateNewKey(account, newKeyPair);
        assertThat(account.getInactiveKeyPairs(), contains(activeKeyPair));
        assertThat(account.getActiveKeyPair(), sameInstance(newKeyPair));
    }

    @Test
    void deactivateKeyPairNoActiveKeyAndEmptyList() {
        Account<KeyPair> account = new PersonalAccount(ACCOUNT_ID, ACCOUNT_NAME, activeKeyPair, Collections.emptyList(), null, null);
        accountService.activateNewKey(account, newKeyPair);
        assertThat(account.getInactiveKeyPairs(), contains(activeKeyPair));
        assertThat(account.getActiveKeyPair(), sameInstance(newKeyPair));
    }

    @Test
    void deactivateKeyPairNoActiveKeyAndInactiveKeyPair() {
        Account<KeyPair> account = new PersonalAccount(ACCOUNT_ID, ACCOUNT_NAME, activeKeyPair, Collections.singletonList(inactiveKeyPair), null, null);
        accountService.activateNewKey(account, newKeyPair);
        assertThat(account.getInactiveKeyPairs(), contains(inactiveKeyPair, activeKeyPair));
        assertThat(account.getActiveKeyPair(), sameInstance(newKeyPair));
    }
}
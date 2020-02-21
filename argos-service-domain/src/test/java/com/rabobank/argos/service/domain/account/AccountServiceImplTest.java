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

import com.rabobank.argos.domain.account.PersonalAccount;
import com.rabobank.argos.domain.key.KeyPair;
import com.rabobank.argos.service.domain.permission.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    private static final String ACCOUNT_NAME = "accountName";
    private static final String ACCOUNT_ID = "accountId";

    private KeyPair activeKeyPair = new KeyPair();
    private KeyPair inactiveKeyPair = new KeyPair();
    private KeyPair newKeyPair = new KeyPair();
    private AccountServiceImpl accountService;

    @Mock
    private NonPersonalAccountRepository nonPersonalAccountRepository;

    @Mock
    private PersonalAccountRepository personalAccountRepository;

    @Mock
    private RoleRepository roleRepository;

    @BeforeEach
    void setUp() {
        accountService = new AccountServiceImpl(nonPersonalAccountRepository, personalAccountRepository, roleRepository);
    }

    @Test
    void deactivateKeyPairNoActiveKeyAndNoInactiveKeys() {
        PersonalAccount account = PersonalAccount.builder().name(ACCOUNT_NAME).build();
        when(personalAccountRepository.findByAccountId(ACCOUNT_ID)).thenReturn(Optional.of(account));
        accountService.activateNewKey(ACCOUNT_ID, newKeyPair);
        assertThat(account.getInactiveKeyPairs(), empty());
        assertThat(account.getActiveKeyPair(), sameInstance(newKeyPair));
    }

    @Test
    void deactivateKeyPairNoActiveKey() {
        PersonalAccount account = PersonalAccount.builder().name(ACCOUNT_NAME).activeKeyPair(activeKeyPair).build();
        when(personalAccountRepository.findByAccountId(ACCOUNT_ID)).thenReturn(Optional.of(account));
        accountService.activateNewKey(ACCOUNT_ID, newKeyPair);
        assertThat(account.getInactiveKeyPairs(), contains(activeKeyPair));
        assertThat(account.getActiveKeyPair(), sameInstance(newKeyPair));
    }

    @Test
    void deactivateKeyPairNoActiveKeyAndEmptyList() {
        PersonalAccount account = PersonalAccount.builder().name(ACCOUNT_NAME).activeKeyPair(activeKeyPair).inactiveKeyPairs(Collections.emptyList()).build();
        when(personalAccountRepository.findByAccountId(ACCOUNT_ID)).thenReturn(Optional.of(account));
        accountService.activateNewKey(ACCOUNT_ID, newKeyPair);
        assertThat(account.getInactiveKeyPairs(), contains(activeKeyPair));
        assertThat(account.getActiveKeyPair(), sameInstance(newKeyPair));
    }

    @Test
    void deactivateKeyPairNoActiveKeyAndInactiveKeyPair() {
        PersonalAccount account = PersonalAccount.builder().name(ACCOUNT_NAME).activeKeyPair(activeKeyPair).inactiveKeyPairs(Collections.singletonList(inactiveKeyPair)).build();
        when(personalAccountRepository.findByAccountId(ACCOUNT_ID)).thenReturn(Optional.of(account));
        accountService.activateNewKey(ACCOUNT_ID, newKeyPair);
        assertThat(account.getInactiveKeyPairs(), contains(inactiveKeyPair, activeKeyPair));
        assertThat(account.getActiveKeyPair(), sameInstance(newKeyPair));
        verify(personalAccountRepository).update(account);
    }
}
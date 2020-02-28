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
package com.rabobank.argos.service.domain.security;

import com.rabobank.argos.domain.account.Account;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountSecurityContextImplTest {

    private AccountSecurityContextImpl context;

    @Mock
    private Authentication authentication;

    @Mock
    private AccountUserDetailsAdapter accountUserDetailsAdapter;

    @Mock
    private Account account;

    @BeforeEach
    void setUp() {
        context = new AccountSecurityContextImpl();
    }

    @Test
    void getAuthenticatedAccountNotFound() {
        assertThat(context.getAuthenticatedAccount(), is(Optional.empty()));
    }

    @Test
    void getAuthenticatedAccount() {
        when(authentication.getPrincipal()).thenReturn(accountUserDetailsAdapter);
        when(accountUserDetailsAdapter.getAccount()).thenReturn(account);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        assertThat(context.getAuthenticatedAccount(), is(Optional.of(account)));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.getContext().setAuthentication(null);
    }
}
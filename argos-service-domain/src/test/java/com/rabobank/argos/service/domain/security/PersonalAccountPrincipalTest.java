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

import com.rabobank.argos.service.domain.account.PersonalAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonalAccountPrincipalTest {

    @Mock
    private PersonalAccount personalAccount;

    @BeforeEach
    void setUp() {
        when(personalAccount.getName()).thenReturn("name");
    }

    @Test
    void getId() {
        when(personalAccount.getAccountId()).thenReturn("id");
        assertThat(new AccountUserDetailsAdapter(personalAccount).getId(), is("id"));
    }

    @Test
    void getPassword() {
        assertThat(new AccountUserDetailsAdapter(personalAccount).getPassword(), is(""));
    }

    @Test
    void getAuthorities() {
        assertThat(new AccountUserDetailsAdapter(personalAccount).getAuthorities(), contains(new SimpleGrantedAuthority("ROLE_USER")));
    }
}
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
package com.rabobank.argos.service.security;

import com.rabobank.argos.domain.ArgosError;
import com.rabobank.argos.domain.account.PersonalAccount;
import com.rabobank.argos.service.domain.account.PersonalAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomPersonalAccountDetailsServiceTest {

    @Mock
    private PersonalAccountRepository personalAccountRepository;
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private PersonalAccount personalAccount;

    @BeforeEach
    void setUp() {
        customUserDetailsService = new CustomUserDetailsService(personalAccountRepository);
    }

    @Test
    void loadUserById() {
        when(personalAccountRepository.findByAccountId("id")).thenReturn(Optional.of(personalAccount));
        when(personalAccount.getName()).thenReturn("name");
        UserDetails userDetails = customUserDetailsService.loadUserById("id");
        assertThat(userDetails.getUsername(), is("name"));
    }

    @Test
    void loadUserByIdNotFound() {
        when(personalAccountRepository.findByAccountId("id")).thenReturn(Optional.empty());
        ArgosError argosError = assertThrows(ArgosError.class, () -> customUserDetailsService.loadUserById("id"));
        assertThat(argosError.getMessage(), is("Personal account with id id not found"));
    }
}
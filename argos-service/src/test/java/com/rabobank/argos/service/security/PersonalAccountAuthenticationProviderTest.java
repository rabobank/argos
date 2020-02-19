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
import com.rabobank.argos.service.domain.security.AccountUserDetailsAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonalAccountAuthenticationProviderTest {

    @Mock
    private PersonalAccountUserDetailsService personalAccountUserDetailsService;
    private PersonalAccountAuthenticationProvider personalAccountAuthenticationProvider;
    private static final String NOT_AUTHENTICATED = "not authenticated";
    private UserDetails userDetails = new AccountUserDetailsAdapter(PersonalAccount.builder().name("test").build());


    private Authentication authentication = new PersonalAccountAuthenticationToken("id", null, null);


    @BeforeEach
    void setup() {
        personalAccountAuthenticationProvider = new PersonalAccountAuthenticationProvider(personalAccountUserDetailsService);
    }

    @Test
    void testAuthenticateWithValidCredentials() {
        when(personalAccountUserDetailsService.loadUserById(eq("id"))).thenReturn(userDetails);
        Authentication authorized = personalAccountAuthenticationProvider.authenticate(authentication);
        assertThat(authorized.isAuthenticated(), is(true));
        assertThat(authorized.getPrincipal(), sameInstance(userDetails));
    }

    @Test
    void testAuthenticateWithInValidCredentials() {
        when(personalAccountUserDetailsService.loadUserById(eq("id"))).thenThrow(new ArgosError("Personal account with id  not found"));
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> personalAccountAuthenticationProvider.authenticate(authentication));
        assertThat(exception.getMessage(), is(NOT_AUTHENTICATED));
    }

    @Test
    void supports() {
        assertThat(personalAccountAuthenticationProvider.supports(PersonalAccountAuthenticationToken.class), is(true));
    }
}
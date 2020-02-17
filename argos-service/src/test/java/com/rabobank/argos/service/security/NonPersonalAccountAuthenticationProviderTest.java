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
import com.rabobank.argos.domain.account.NonPersonalAccount;
import com.rabobank.argos.domain.account.NonPersonalAccountKeyPair;
import com.rabobank.argos.service.domain.security.AccountUserDetailsAdapter;
import com.rabobank.argos.service.security.NonPersonalAccountAuthenticationToken.NonPersonalAccountCredentials;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NonPersonalAccountAuthenticationProviderTest {

    private static final String KEYID = "keyid";
    private static final String PASSWORD = "password";
    private static final String ENCRYPTEDPASSWORD = "encryptedpassword";
    @Mock
    private NonPersonalAccountUserDetailsService nonPersonalAccountUserDetailsService;
    @Mock
    private PasswordEncoder passwordEncoder;
    private Authentication authentication;
    private NonPersonalAccountAuthenticationProvider nonPersonalAccountAuthenticationProvider;
    private UserDetails userDetails = new AccountUserDetailsAdapter(NonPersonalAccount.builder()
            .name("test")
            .activeKeyPair(new NonPersonalAccountKeyPair(KEYID, null, null, ENCRYPTEDPASSWORD))
            .build());

    @BeforeEach
    void setup() {
        NonPersonalAccountCredentials credentials = NonPersonalAccountCredentials
                .builder()
                .keyId(KEYID)
                .password(PASSWORD)
                .build();
        authentication = new NonPersonalAccountAuthenticationToken(credentials, null);
        nonPersonalAccountAuthenticationProvider = new NonPersonalAccountAuthenticationProvider(nonPersonalAccountUserDetailsService, passwordEncoder);
    }

    @Test
    void authenticateWithValidCredentialsShouldReturnAuthenticated() {
        when(nonPersonalAccountUserDetailsService.loadUserById(eq(KEYID))).thenReturn(userDetails);
        when(passwordEncoder.matches(eq(PASSWORD), eq(ENCRYPTEDPASSWORD))).thenReturn(true);
        Authentication authenticatedAccount = nonPersonalAccountAuthenticationProvider.authenticate(authentication);
        assertThat(authenticatedAccount.getPrincipal(), sameInstance(userDetails));
        assertThat(authenticatedAccount.isAuthenticated(), is(true));
    }

    @Test
    void authenticateWithInValidIdShouldReturnUnAuthenticated() {
        when(nonPersonalAccountUserDetailsService.loadUserById(eq(KEYID))).thenThrow(new ArgosError("non personal account not found"));
        Authentication authenticatedAccount = nonPersonalAccountAuthenticationProvider.authenticate(authentication);
        assertThat(authenticatedAccount.getPrincipal(), nullValue());
        assertThat(authenticatedAccount.isAuthenticated(), is(false));
    }

    @Test
    void authenticateWithInValidPasswordShouldReturnUnAuthenticated() {
        when(nonPersonalAccountUserDetailsService.loadUserById(eq(KEYID))).thenReturn(userDetails);
        when(passwordEncoder.matches(eq(PASSWORD), eq(ENCRYPTEDPASSWORD))).thenReturn(false);
        Authentication authenticatedAccount = nonPersonalAccountAuthenticationProvider.authenticate(authentication);
        assertThat(authenticatedAccount.getPrincipal(), nullValue());
        assertThat(authenticatedAccount.isAuthenticated(), is(false));
    }

    @Test
    void supports() {
        assertThat(nonPersonalAccountAuthenticationProvider
                        .supports(NonPersonalAccountAuthenticationToken.class),
                is(true));
    }
}
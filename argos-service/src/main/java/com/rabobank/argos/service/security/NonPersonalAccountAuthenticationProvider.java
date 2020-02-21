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

import com.rabobank.argos.domain.account.NonPersonalAccountKeyPair;
import com.rabobank.argos.service.domain.security.AccountUserDetailsAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;


@Slf4j
@RequiredArgsConstructor
public class NonPersonalAccountAuthenticationProvider implements AuthenticationProvider {

    private static final String NOT_AUTHENTICATED = "not authenticated";
    private final NonPersonalAccountUserDetailsService nonPersonalAccountUserDetailsService;

    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication notAuthenticatedNonPersonalAccount) {
        NonPersonalAccountAuthenticationToken nonPersonalAccountAuthenticationToken = (NonPersonalAccountAuthenticationToken) notAuthenticatedNonPersonalAccount;
        try {
            AccountUserDetailsAdapter userDetails = (AccountUserDetailsAdapter) nonPersonalAccountUserDetailsService
                    .loadUserById(nonPersonalAccountAuthenticationToken.getNonPersonalAccountCredentials().getKeyId());
            log.debug("successfully found non personal account by key id {}", userDetails.getUsername());
            String password = nonPersonalAccountAuthenticationToken.getNonPersonalAccountCredentials().getPassword();
            NonPersonalAccountKeyPair nonPersonalAccountKeyPair = (NonPersonalAccountKeyPair) userDetails.getAccount().getActiveKeyPair();
            if (passwordEncoder.matches(password, nonPersonalAccountKeyPair.getEncryptedHashedKeyPassphrase())) {
                log.debug("successfully authenticated non personal account {}", userDetails.getUsername());
                return new NonPersonalAccountAuthenticationToken(nonPersonalAccountAuthenticationToken.getNonPersonalAccountCredentials(),
                        userDetails,
                        userDetails.getAuthorities());
            } else {
                log.warn("invalid access attempt {}", nonPersonalAccountAuthenticationToken);
                throw new BadCredentialsException(NOT_AUTHENTICATED);
            }
        } catch (Exception ex) {
            log.warn("invalid access attempt {}", nonPersonalAccountAuthenticationToken);
            throw new BadCredentialsException(NOT_AUTHENTICATED);
        }
    }


    @Override
    public boolean supports(Class<?> authenticationTokenClass) {
        return authenticationTokenClass.equals(NonPersonalAccountAuthenticationToken.class);
    }
}

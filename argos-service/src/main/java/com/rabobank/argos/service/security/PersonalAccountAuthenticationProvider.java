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

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;


@Slf4j
public class PersonalAccountAuthenticationProvider implements AuthenticationProvider {

    private static final String NOT_AUTHENTICATED = "not authenticated";
    private final PersonalAccountUserDetailsService personalAccountUserDetailsService;

    public PersonalAccountAuthenticationProvider(PersonalAccountUserDetailsService personalAccountUserDetailsService) {
        this.personalAccountUserDetailsService = personalAccountUserDetailsService;
    }

    @Override
    public Authentication authenticate(Authentication notAuthenticatedPersonalAccount) {
        PersonalAccountAuthenticationToken personalAccountAuthenticationToken = (PersonalAccountAuthenticationToken) notAuthenticatedPersonalAccount;
        try {
            UserDetails userDetails = personalAccountUserDetailsService.loadUserById(personalAccountAuthenticationToken.getCredentials());
            Authentication authenticatedPersonalAccount = new PersonalAccountAuthenticationToken(personalAccountAuthenticationToken.getCredentials(), userDetails, userDetails.getAuthorities());
            authenticatedPersonalAccount.setAuthenticated(true);
            log.debug("successfully authenticated personal account {}", userDetails.getUsername());
            return authenticatedPersonalAccount;
        } catch (Exception ex) {
            log.warn("invalid access attempt  {}", personalAccountAuthenticationToken);
            throw new BadCredentialsException(NOT_AUTHENTICATED);
        }
    }

    @Override
    public boolean supports(Class<?> authenticationTokenClass) {
        return authenticationTokenClass.equals(PersonalAccountAuthenticationToken.class);
    }
}

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

import lombok.EqualsAndHashCode;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@EqualsAndHashCode
public class PersonalAccountAuthenticationToken extends AbstractAuthenticationToken {

    private final String accountId;
    private final UserDetails principal;

    public PersonalAccountAuthenticationToken(String accountId, UserDetails principal, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.accountId = accountId;
        this.principal = principal;
    }

    @Override
    public String getCredentials() {
        return accountId;
    }

    @Override
    public UserDetails getPrincipal() {
        return principal;
    }
}

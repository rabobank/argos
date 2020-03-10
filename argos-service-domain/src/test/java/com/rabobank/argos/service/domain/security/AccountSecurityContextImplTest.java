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
import com.rabobank.argos.domain.permission.LocalPermissions;
import com.rabobank.argos.domain.permission.Permission;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountSecurityContextImplTest {

    private static final String LABEL_ID = "label_id";
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

    @Test
    void getAllPermissionsWithCorrectIdsShouldReturnCorrectSet() {
        List<LocalPermissions> localPermissions = Collections.singletonList(LocalPermissions.builder()
                .labelId(LABEL_ID)
                .permissions(List.of(Permission.READ))
                .build());
        when(account.getLocalPermissions()).thenReturn(localPermissions);
        when(authentication.getPrincipal()).thenReturn(accountUserDetailsAdapter);
        when(accountUserDetailsAdapter.getAccount()).thenReturn(account);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        Set<Permission> permissions = context.allLocalPermissions(List.of(LABEL_ID));
        assertThat(permissions, is(Set.of(Permission.READ)));
    }

    @Test
    void getAllPermissionsWithNoAuthenticationShouldReturnEmptySet() {
        when(authentication.getPrincipal()).thenReturn(null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        Set<Permission> permissions = context.allLocalPermissions(List.of(LABEL_ID));
        assertThat(permissions.isEmpty(), is(true));
    }

    @Test
    void getGlobalPermisionsShouldReturnResult() {
        when(authentication.getPrincipal()).thenReturn(accountUserDetailsAdapter);
        when(accountUserDetailsAdapter.getGlobalPermissions()).thenReturn(Set.of(Permission.READ));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        Set<Permission> permissions = context.getGlobalPermission();
        assertThat(permissions, is(Set.of(Permission.READ)));
    }

    @Test
    void getGlobalPermisionsEmptyAccountShouldReturnEmpty() {
        when(authentication.getPrincipal()).thenReturn(null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        Set<Permission> permissions = context.getGlobalPermission();
        assertThat(permissions.isEmpty(), is(true));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.getContext().setAuthentication(null);
    }
}
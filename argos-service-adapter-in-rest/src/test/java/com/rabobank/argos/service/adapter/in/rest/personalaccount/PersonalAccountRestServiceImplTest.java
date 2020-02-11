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
package com.rabobank.argos.service.adapter.in.rest.personalaccount;

import com.rabobank.argos.service.adapter.in.rest.api.model.RestPersonalAccount;
import com.rabobank.argos.service.domain.account.PersonalAccount;
import com.rabobank.argos.service.domain.security.AccountSecurityContextImpl;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonalAccountRestServiceImplTest {

    private static final String NAME = "name";
    private static final String EMAIL = "email";
    private static final String ID = "id";

    private PersonalAccountRestServiceImpl service;
    @Mock
    private AccountSecurityContextImpl accountSecurityContext;

    private PersonalAccount personalAccount = PersonalAccount.builder().name(NAME).email(EMAIL).accountId(ID).build();

    @BeforeEach
    void setUp() {
        service = new PersonalAccountRestServiceImpl(accountSecurityContext);
    }


    @Test
    void getCurrentUserNotFound() {
        when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.getPersonalAccountOfAuthenticatedUser());
        assertThat(exception.getStatus().value(), is(404));
        assertThat(exception.getMessage(), is("404 NOT_FOUND \"profile not found\""));
    }

    @Test
    void getPersonalAccountOfAuthenticatedUser() {
        when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.of(personalAccount));
        ResponseEntity<RestPersonalAccount> responseEntity = service.getPersonalAccountOfAuthenticatedUser();
        assertThat(responseEntity.getStatusCodeValue(), Matchers.is(200));
        RestPersonalAccount restPersonalAccount = responseEntity.getBody();
        assertThat(restPersonalAccount, is(notNullValue()));
        assertThat(restPersonalAccount.getId(), is(ID));
        assertThat(restPersonalAccount.getName(), is(NAME));
        assertThat(restPersonalAccount.getEmail(), is(EMAIL));
    }
}
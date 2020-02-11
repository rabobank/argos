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
import com.rabobank.argos.service.domain.account.PersonalAccountRepository;
import com.rabobank.argos.service.domain.security.AccountUserDetailsAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonalAccountRestServiceImplTest {

    private static final String NAME = "name";
    private static final String EMAIL = "email";
    private static final String ID = "id";
    private static final String KEY_ID = "keyId";
    @Mock
    private PersonalAccountRepository personalAccountRepository;

    private PersonalAccountRestServiceImpl service;

    @Mock
    private AccountUserDetailsAdapter accountUserDetailsAdapter;

    private PersonalAccount personalAccount = PersonalAccount.builder().name(NAME).email(EMAIL).accountId(ID).build();

    @BeforeEach
    void setUp() {
        service = new PersonalAccountRestServiceImpl(personalAccountRepository);
    }

    @Test
    void getCurrentUser() {
        when(accountUserDetailsAdapter.getId()).thenReturn(ID);
        when(personalAccountRepository.findByUserId(ID)).thenReturn(Optional.of(personalAccount));
        RestPersonalAccount user = service.getCurrentUser(accountUserDetailsAdapter);
        assertThat(user.getEmail(), is(EMAIL));
        assertThat(user.getId(), is(ID));
        assertThat(user.getName(), is(NAME));

    }

    @Test
    void getCurrentUserNotFound() {
        when(accountUserDetailsAdapter.getId()).thenReturn(ID);
        when(personalAccountRepository.findByUserId(ID)).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.getCurrentUser(accountUserDetailsAdapter));
        assertThat(exception.getStatus().value(), is(404));
        assertThat(exception.getMessage(), is("404 NOT_FOUND \"profile not found for : id\""));
    }

    @Test
    void updateUserProfile() {
        when(accountUserDetailsAdapter.getId()).thenReturn(ID);
        when(personalAccountRepository.findByUserId(ID)).thenReturn(Optional.of(personalAccount));
        RestPersonalAccount user = service.updateUserProfile(accountUserDetailsAdapter, new RestPersonalAccount().id("otherId")
                .email("other_email")
                .name("other_name"));
        assertThat(user.getEmail(), is(EMAIL));
        assertThat(user.getId(), is(ID));
        assertThat(user.getName(), is(NAME));

    }

    @Test
    void updateUserProfileNotFound() {
        when(accountUserDetailsAdapter.getId()).thenReturn(ID);
        when(personalAccountRepository.findByUserId(ID)).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.updateUserProfile(accountUserDetailsAdapter, new RestPersonalAccount()));
        assertThat(exception.getStatus().value(), is(404));
        assertThat(exception.getMessage(), is("404 NOT_FOUND \"profile not found for : id\""));
    }
}
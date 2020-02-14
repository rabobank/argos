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

import com.rabobank.argos.domain.key.KeyIdProvider;
import com.rabobank.argos.domain.key.KeyPair;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestKeyPair;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestPersonalAccount;
import com.rabobank.argos.service.adapter.in.rest.key.KeyPairMapper;
import com.rabobank.argos.service.domain.account.PersonalAccount;
import com.rabobank.argos.service.domain.account.PersonalAccountRepository;
import com.rabobank.argos.service.domain.security.AccountSecurityContextImpl;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonalAccountRestServiceTest {

    private static final String NAME = "name";
    private static final String EMAIL = "email";
    private static final String ID = "id";
    private static final String KEY_ID = "keyId";
    private static final String KEY_ID_PROVIDER = "keyIdProvider";
    private static final String PERSONAL_ACCOUNT_NOT_FOUND = "404 NOT_FOUND \"personal account not found\"";
    public static final String ACTIVE_KEYPAIR_NOT_FOUND = "404 NOT_FOUND \"no active keypair found for account: name\"";

    private PersonalAccountRestService service;
    @Mock
    private AccountSecurityContextImpl accountSecurityContext;
    @Mock
    private PersonalAccountRepository personalAccountRepository;
    @Mock
    private KeyPairMapper keyPairMapper;
    @Mock
    private RestKeyPair restKeyPair;
    @Mock
    private KeyPair keyPair;
    @Mock
    private KeyIdProvider keyIdProvider;
    private PersonalAccount personalAccount = PersonalAccount.builder().name(NAME).email(EMAIL).build();

    @BeforeEach
    void setUp() {
        service = new PersonalAccountRestService(accountSecurityContext, personalAccountRepository, keyPairMapper);
    }

    @Test
    void getCurrentUserNotFound() {
        when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service
                .getPersonalAccountOfAuthenticatedUser());
        assertThat(exception.getStatus().value(), is(404));
        assertThat(exception.getMessage(), is(PERSONAL_ACCOUNT_NOT_FOUND));
    }

    @Test
    void getPersonalAccountOfAuthenticatedUser() {
        when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.of(personalAccount));
        ResponseEntity<RestPersonalAccount> responseEntity = service.getPersonalAccountOfAuthenticatedUser();
        assertThat(responseEntity.getStatusCodeValue(), Matchers.is(200));
        RestPersonalAccount restPersonalAccount = responseEntity.getBody();
        assertThat(restPersonalAccount, is(notNullValue()));
        assertThat(restPersonalAccount.getName(), is(NAME));
        assertThat(restPersonalAccount.getEmail(), is(EMAIL));
    }

    @Test
    void storeKeyShouldReturnSuccess() {
        when(keyIdProvider.computeKeyId(any())).thenReturn(KEY_ID);
        when(keyPair.getKeyId()).thenReturn(KEY_ID);
        when(keyPairMapper.convertFromRestKeyPair(restKeyPair)).thenReturn(keyPair);
        when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.of(personalAccount));
        ReflectionTestUtils.setField(service, KEY_ID_PROVIDER, keyIdProvider);
        assertThat(service.createKey(restKeyPair).getStatusCodeValue(), is(204));
        assertThat(personalAccount.getActiveKeyPair(), sameInstance(keyPair));
        verify(personalAccountRepository).update(personalAccount);
    }

    @Test
    void storeKeyShouldReturnBadRequest() {
        when(keyIdProvider.computeKeyId(any())).thenReturn(KEY_ID);
        when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.of(personalAccount));
        when(keyPair.getKeyId()).thenReturn("incorrect key");
        when(keyPairMapper.convertFromRestKeyPair(restKeyPair)).thenReturn(keyPair);
        ReflectionTestUtils.setField(service, KEY_ID_PROVIDER, keyIdProvider);
        assertThrows(ResponseStatusException.class, () -> service.createKey(restKeyPair));
    }

    @Test
    void storeKeyShouldReturnNotFound() {
        when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.createKey(restKeyPair));
        assertThat(exception.getStatus().value(), is(404));
        assertThat(exception.getMessage(), is(PERSONAL_ACCOUNT_NOT_FOUND));
    }

    @Test
    void getKeyPairShouldReturnOK() {
        when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.of(personalAccount));
        when(keyPairMapper.convertToRestKeyPair(keyPair)).thenReturn(restKeyPair);
        when(personalAccountRepository.findActiveKeyPair(any())).thenReturn(Optional.of(keyPair));
        ResponseEntity<RestKeyPair> responseEntity = service.getKeyPair();
        assertThat(responseEntity.getStatusCodeValue(), Matchers.is(200));
        assertThat(responseEntity.getBody(), sameInstance(restKeyPair));
    }

    @Test
    void getKeyPairShouldReturnNotFound() {
        when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.of(personalAccount));
        when(personalAccountRepository.findActiveKeyPair(any())).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.getKeyPair());
        assertThat(exception.getStatus().value(), is(404));
        assertThat(exception.getMessage(), is(ACTIVE_KEYPAIR_NOT_FOUND));
    }
}
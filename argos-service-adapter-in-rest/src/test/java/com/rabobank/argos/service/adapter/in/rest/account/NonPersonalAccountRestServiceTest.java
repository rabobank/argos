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
package com.rabobank.argos.service.adapter.in.rest.account;

import com.rabobank.argos.domain.account.NonPersonalAccount;
import com.rabobank.argos.domain.account.NonPersonalAccountKeyPair;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestNonPersonalAccount;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestNonPersonalAccountKeyPair;
import com.rabobank.argos.service.domain.account.AccountService;
import com.rabobank.argos.service.domain.hierarchy.LabelRepository;
import com.rabobank.argos.service.domain.security.AccountSecurityContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NonPersonalAccountRestServiceTest {

    private static final String PARENT_LABEL_ID = "parentLabelId";
    private static final String ACCOUNT_ID = "accountId";

    @Mock
    private NonPersonalAccountMapper accountMapper;

    @Mock
    private LabelRepository labelRepository;

    @Mock
    private AccountKeyPairMapper keyPairMapper;

    @Mock
    private RestNonPersonalAccount restNonPersonalAccount;

    private NonPersonalAccountRestService service;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private NonPersonalAccount nonPersonalAccount;

    @Mock
    private RestNonPersonalAccountKeyPair restKeyPair;

    @Mock
    private NonPersonalAccountKeyPair keyPair;

    @Mock
    private AccountService accountService;

    @Mock
    private AccountSecurityContext accountSecurityContext;

    @BeforeEach
    void setUp() {
        service = new NonPersonalAccountRestService(accountMapper, labelRepository, keyPairMapper, accountService, accountSecurityContext);
    }

    @Test
    void createNonPersonalAccount() {
        when(restNonPersonalAccount.getParentLabelId()).thenReturn(PARENT_LABEL_ID);
        when(labelRepository.exists(PARENT_LABEL_ID)).thenReturn(true);
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        when(accountMapper.convertFromRestNonPersonalAccount(restNonPersonalAccount)).thenReturn(nonPersonalAccount);
        when(accountMapper.convertToRestNonPersonalAccount(nonPersonalAccount)).thenReturn(restNonPersonalAccount);
        ResponseEntity<RestNonPersonalAccount> response = service.createNonPersonalAccount(restNonPersonalAccount);
        assertThat(response.getStatusCodeValue(), is(201));
        assertThat(response.getBody(), sameInstance(restNonPersonalAccount));
        assertThat(response.getHeaders().getLocation(), notNullValue());
        verify(accountService).save(nonPersonalAccount);
    }

    @Test
    void createNonPersonalAccountParentLabelIdDoesNotExist() {
        when(restNonPersonalAccount.getParentLabelId()).thenReturn(PARENT_LABEL_ID);
        when(labelRepository.exists(PARENT_LABEL_ID)).thenReturn(false);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.createNonPersonalAccount(restNonPersonalAccount));
        assertThat(exception.getMessage(), is("400 BAD_REQUEST \"parent label id not found : parentLabelId\""));
    }

    @Test
    void createNonPersonalAccountKeyById() {
        when(accountService.activateNewKey(ACCOUNT_ID, keyPair)).thenReturn(Optional.of(nonPersonalAccount));
        when(nonPersonalAccount.getActiveKeyPair()).thenReturn(keyPair);
        when(keyPairMapper.convertToRestKeyPair(keyPair)).thenReturn(restKeyPair);
        when(keyPairMapper.convertFromRestKeyPair(restKeyPair)).thenReturn(keyPair);
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        ResponseEntity<RestNonPersonalAccountKeyPair> response = service.createNonPersonalAccountKeyById(ACCOUNT_ID, restKeyPair);
        assertThat(response.getStatusCodeValue(), is(201));
        assertThat(response.getBody(), sameInstance(restKeyPair));
        assertThat(response.getHeaders().getLocation(), notNullValue());
        verify(accountService).activateNewKey(ACCOUNT_ID, keyPair);
    }

    @Test
    void createNonPersonalAccountKeyByIdAccountNotFound() {
        when(keyPairMapper.convertFromRestKeyPair(restKeyPair)).thenReturn(keyPair);
        when(accountService.activateNewKey(ACCOUNT_ID, keyPair)).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.createNonPersonalAccountKeyById(ACCOUNT_ID, restKeyPair));
        assertThat(exception.getMessage(), is("404 NOT_FOUND \"no non personal account with id : accountId found\""));
    }

    @Test
    void getNonPersonalAccountKeyById() {
        when(accountService.findNonPersonalAccountById(ACCOUNT_ID)).thenReturn(Optional.of(nonPersonalAccount));
        when(nonPersonalAccount.getActiveKeyPair()).thenReturn(keyPair);
        when(keyPairMapper.convertToRestKeyPair(keyPair)).thenReturn(restKeyPair);
        ResponseEntity<RestNonPersonalAccountKeyPair> response = service.getNonPersonalAccountKeyById(ACCOUNT_ID);
        assertThat(response.getStatusCodeValue(), is(200));
        assertThat(response.getBody(), sameInstance(restKeyPair));
    }

    @Test
    void getNonPersonalAccountKeyByIdAccountNotFound() {
        when(accountService.findNonPersonalAccountById(ACCOUNT_ID)).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.getNonPersonalAccountKeyById(ACCOUNT_ID));
        assertThat(exception.getMessage(), is("404 NOT_FOUND \"no active non personal account key with id : accountId found\""));
    }

    @Test
    void getNonPersonalAccountKeyByIdNoActiveKey() {
        when(accountService.findNonPersonalAccountById(ACCOUNT_ID)).thenReturn(Optional.of(nonPersonalAccount));
        when(nonPersonalAccount.getActiveKeyPair()).thenReturn(null);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.getNonPersonalAccountKeyById(ACCOUNT_ID));
        assertThat(exception.getMessage(), is("404 NOT_FOUND \"no active non personal account key with id : accountId found\""));
    }

    @Test
    void getNonPersonalAccountById() {
        when(accountService.findNonPersonalAccountById(ACCOUNT_ID)).thenReturn(Optional.of(nonPersonalAccount));
        when(accountMapper.convertToRestNonPersonalAccount(nonPersonalAccount)).thenReturn(restNonPersonalAccount);
        ResponseEntity<RestNonPersonalAccount> response = service.getNonPersonalAccountById(ACCOUNT_ID);
        assertThat(response.getStatusCodeValue(), is(200));
        assertThat(response.getBody(), sameInstance(restNonPersonalAccount));
    }

    @Test
    void getNonPersonalAccountByIdAccountNotFound() {
        when(accountService.findNonPersonalAccountById(ACCOUNT_ID)).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.getNonPersonalAccountById(ACCOUNT_ID));
        assertThat(exception.getMessage(), is("404 NOT_FOUND \"no non personal account with id : accountId found\""));
    }

    @Test
    void updateNonPersonalAccountById() {
        when(restNonPersonalAccount.getParentLabelId()).thenReturn(PARENT_LABEL_ID);
        when(labelRepository.exists(PARENT_LABEL_ID)).thenReturn(true);
        when(accountMapper.convertFromRestNonPersonalAccount(restNonPersonalAccount)).thenReturn(nonPersonalAccount);
        when(accountService.update(ACCOUNT_ID, nonPersonalAccount)).thenReturn(Optional.of(nonPersonalAccount));
        when(accountMapper.convertToRestNonPersonalAccount(nonPersonalAccount)).thenReturn(restNonPersonalAccount);
        ResponseEntity<RestNonPersonalAccount> response = service.updateNonPersonalAccountById(ACCOUNT_ID, restNonPersonalAccount);
        assertThat(response.getStatusCodeValue(), is(200));
        assertThat(response.getBody(), sameInstance(restNonPersonalAccount));
    }

    @Test
    void updateNonPersonalAccountByIdAccountNotFound() {
        when(restNonPersonalAccount.getParentLabelId()).thenReturn(PARENT_LABEL_ID);
        when(labelRepository.exists(PARENT_LABEL_ID)).thenReturn(true);
        when(accountMapper.convertFromRestNonPersonalAccount(restNonPersonalAccount)).thenReturn(nonPersonalAccount);
        when(accountService.update(ACCOUNT_ID, nonPersonalAccount)).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.updateNonPersonalAccountById(ACCOUNT_ID, restNonPersonalAccount));
        assertThat(exception.getMessage(), is("404 NOT_FOUND \"no non personal account with id : accountId found\""));
    }

    @Test
    void updateNonPersonalAccountByIdParentLabelIdNotFound() {
        when(restNonPersonalAccount.getParentLabelId()).thenReturn(PARENT_LABEL_ID);
        when(labelRepository.exists(PARENT_LABEL_ID)).thenReturn(false);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.updateNonPersonalAccountById(ACCOUNT_ID, restNonPersonalAccount));
        assertThat(exception.getMessage(), is("400 BAD_REQUEST \"parent label id not found : parentLabelId\""));
    }

    @Test
    void getNonPersonalAccountKey() {
        when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.of(nonPersonalAccount));
        when(nonPersonalAccount.getActiveKeyPair()).thenReturn(keyPair);
        when(keyPairMapper.convertToRestKeyPair(keyPair)).thenReturn(restKeyPair);
        ResponseEntity<RestNonPersonalAccountKeyPair> response = service.getNonPersonalAccountKey();
        assertThat(response.getStatusCodeValue(), is(200));
        assertThat(response.getBody(), sameInstance(restKeyPair));
    }

    @Test
    void getNonPersonalAccountKeyNotFound() {
        when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.getNonPersonalAccountKey());
        assertThat(exception.getMessage(), is("404 NOT_FOUND \"no active non personal account key found\""));
    }

    @Test
    void getNonPersonalAccountKeyNoActiveKey() {
        when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.of(nonPersonalAccount));
        when(nonPersonalAccount.getActiveKeyPair()).thenReturn(null);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.getNonPersonalAccountKey());
        assertThat(exception.getMessage(), is("404 NOT_FOUND \"no active non personal account key found\""));
    }
}
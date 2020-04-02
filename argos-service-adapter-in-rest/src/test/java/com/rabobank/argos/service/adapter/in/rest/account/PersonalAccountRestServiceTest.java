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

import com.rabobank.argos.domain.account.PersonalAccount;
import com.rabobank.argos.domain.key.KeyPair;
import com.rabobank.argos.domain.permission.LocalPermissions;
import com.rabobank.argos.domain.permission.Permission;
import com.rabobank.argos.service.adapter.in.rest.ArgosKeyHelper;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestKeyPair;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestLocalPermissions;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestPermission;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestPersonalAccount;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestProfile;
import com.rabobank.argos.service.domain.account.AccountSearchParams;
import com.rabobank.argos.service.domain.account.AccountService;
import com.rabobank.argos.service.domain.hierarchy.LabelRepository;
import com.rabobank.argos.service.domain.security.AccountSecurityContextImpl;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonalAccountRestServiceTest {

    private static final String NAME = "name";
    private static final String PERSONAL_ACCOUNT_NOT_FOUND = "404 NOT_FOUND \"personal account not found\"";
    public static final String ACTIVE_KEYPAIR_NOT_FOUND = "404 NOT_FOUND \"no active keypair found for account: name\"";
    private static final String ACCOUNT_ID = "accountId";
    private static final String ROLE_NAME = "roleName";
    private static final String LABEL_ID = "labelId";
    private static final String ROLE_ID = "roleId";

    private PersonalAccountRestService service;
    @Mock
    private AccountSecurityContextImpl accountSecurityContext;

    @Mock
    private AccountKeyPairMapper keyPairMapper;
    @Mock
    private RestKeyPair restKeyPair;

    private KeyPair keyPair;

    @Mock
    private PersonalAccount personalAccount;

    @Mock
    private AccountService accountService;

    @Mock
    private PersonalAccountMapper personalAccountMapper;

    @Mock
    private RestPersonalAccount restPersonalAccount;

    @Mock
    private RestProfile restProfile;

    @Captor
    private ArgumentCaptor<AccountSearchParams> searchParamsArgumentCaptor;

    @Mock
    private LocalPermissions localPermissions;

    @Mock
    private RestLocalPermissions restLocalPermissions;

    @Captor
    private ArgumentCaptor<LocalPermissions> localPermissionsArgumentCaptor;

    @Mock
    private LabelRepository labelRepository;

    @BeforeEach
    void setUp() {
        personalAccount.setAccountId(ACCOUNT_ID);
        service = new PersonalAccountRestService(accountSecurityContext, keyPairMapper, accountService, personalAccountMapper, labelRepository);
        keyPair = ArgosKeyHelper.generateKeyPair();
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
        when(personalAccountMapper.convertToRestProfile(personalAccount)).thenReturn(restProfile);
        ResponseEntity<RestProfile> responseEntity = service.getPersonalAccountOfAuthenticatedUser();
        assertThat(responseEntity.getStatusCodeValue(), Matchers.is(200));
        RestProfile restPersonalAccount = responseEntity.getBody();
        assertThat(restPersonalAccount, sameInstance(restPersonalAccount));
    }

    @Test
    void storeKeyShouldReturnSuccess() {
        when(personalAccount.getAccountId()).thenReturn(ACCOUNT_ID);
        when(keyPairMapper.convertFromRestKeyPair(restKeyPair)).thenReturn(keyPair);
        when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.of(personalAccount));
        assertThat(service.createKey(restKeyPair).getStatusCodeValue(), is(204));
        verify(accountService).activateNewKey(ACCOUNT_ID, keyPair);
    }

    @Test
    void storeKeyShouldReturnBadRequest() {
        when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.of(personalAccount));
        keyPair.setKeyId("incorrect key");
        when(keyPairMapper.convertFromRestKeyPair(restKeyPair)).thenReturn(keyPair);
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
        when(personalAccount.getActiveKeyPair()).thenReturn(keyPair);
        when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.of(personalAccount));
        when(keyPairMapper.convertToRestKeyPair(keyPair)).thenReturn(restKeyPair);
        personalAccount.setActiveKeyPair(keyPair);
        ResponseEntity<RestKeyPair> responseEntity = service.getKeyPair();
        assertThat(responseEntity.getStatusCodeValue(), Matchers.is(200));
        assertThat(responseEntity.getBody(), sameInstance(restKeyPair));
    }

    @Test
    void getKeyPairShouldReturnNotFound() {
        when(personalAccount.getName()).thenReturn(NAME);
        when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.of(personalAccount));
        personalAccount.setActiveKeyPair(null);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.getKeyPair());
        assertThat(exception.getStatus().value(), is(404));
        assertThat(exception.getMessage(), is(ACTIVE_KEYPAIR_NOT_FOUND));
    }

    @Test
    void getPersonalAccountById() {
        when(accountService.getPersonalAccountById(ACCOUNT_ID)).thenReturn(Optional.of(personalAccount));
        when(personalAccountMapper.convertToRestPersonalAccount(personalAccount)).thenReturn(restPersonalAccount);
        ResponseEntity<RestPersonalAccount> response = service.getPersonalAccountById(ACCOUNT_ID);
        assertThat(response.getBody(), sameInstance(restPersonalAccount));
        assertThat(response.getStatusCodeValue(), Matchers.is(200));
    }

    @Test
    void getPersonalAccountByIdNotFound() {
        when(accountService.getPersonalAccountById(ACCOUNT_ID)).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.getPersonalAccountById(ACCOUNT_ID));
        assertThat(exception.getStatus().value(), is(404));
        assertThat(exception.getMessage(), is(PERSONAL_ACCOUNT_NOT_FOUND));
    }

    @Test
    void searchPersonalAccounts() {
        when(personalAccountMapper.convertToRoleId(ROLE_NAME)).thenReturn(ROLE_ID);
        when(accountService.searchPersonalAccounts(any(AccountSearchParams.class))).thenReturn(List.of(personalAccount));
        when(personalAccountMapper.convertToRestPersonalAccountWithoutRoles(personalAccount)).thenReturn(restPersonalAccount);
        ResponseEntity<List<RestPersonalAccount>> response = service.searchPersonalAccounts(ROLE_NAME, LABEL_ID, NAME);
        assertThat(response.getBody(), contains(restPersonalAccount));
        assertThat(response.getStatusCodeValue(), Matchers.is(200));
        verify(accountService).searchPersonalAccounts(searchParamsArgumentCaptor.capture());
        AccountSearchParams searchParams = searchParamsArgumentCaptor.getValue();
        assertThat(searchParams.getLocalPermissionsLabelId(), is(Optional.of(LABEL_ID)));
        assertThat(searchParams.getRoleId(), is(Optional.of(ROLE_ID)));
        assertThat(searchParams.getName(), is(Optional.of(NAME)));
    }

    @Test
    void updatePersonalAccountRolesById() {
        when(accountService.updatePersonalAccountRolesById(ACCOUNT_ID, List.of(ROLE_NAME))).thenReturn(Optional.of(personalAccount));
        when(personalAccountMapper.convertToRestPersonalAccount(personalAccount)).thenReturn(restPersonalAccount);
        ResponseEntity<RestPersonalAccount> response = service.updatePersonalAccountRolesById(ACCOUNT_ID, List.of(ROLE_NAME));
        assertThat(response.getBody(), sameInstance(restPersonalAccount));
        assertThat(response.getStatusCodeValue(), Matchers.is(200));
    }

    @Test
    void updatePersonalAccountRolesByIdNotFound() {
        when(accountService.updatePersonalAccountRolesById(ACCOUNT_ID, List.of(ROLE_NAME))).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.updatePersonalAccountRolesById(ACCOUNT_ID, List.of(ROLE_NAME)));
        assertThat(exception.getStatus().value(), is(404));
        assertThat(exception.getMessage(), is(PERSONAL_ACCOUNT_NOT_FOUND));
    }

    @Test
    void getAllLocalPermissions() {
        when(personalAccountMapper.convertToRestLocalPermissions(List.of(localPermissions))).thenReturn(List.of(restLocalPermissions));
        when(personalAccount.getLocalPermissions()).thenReturn(List.of(localPermissions));
        when(accountService.getPersonalAccountById(ACCOUNT_ID)).thenReturn(Optional.of(personalAccount));
        ResponseEntity<List<RestLocalPermissions>> response = service.getAllLocalPermissions(ACCOUNT_ID);
        assertThat(response.getStatusCodeValue(), is(200));
        assertThat(response.getBody(), contains(restLocalPermissions));
    }

    @Test
    void getLocalPermissionsForLabel() {
        when(accountService.getPersonalAccountById(ACCOUNT_ID)).thenReturn(Optional.of(personalAccount));
        when(localPermissions.getLabelId()).thenReturn(LABEL_ID);
        when(personalAccount.getLocalPermissions()).thenReturn(List.of(localPermissions));
        when(personalAccountMapper.convertToRestLocalPermission(localPermissions)).thenReturn(restLocalPermissions);
        ResponseEntity<RestLocalPermissions> response = service.getLocalPermissionsForLabel(ACCOUNT_ID, LABEL_ID);
        assertThat(response.getStatusCodeValue(), is(200));
        assertThat(response.getBody(), sameInstance(restLocalPermissions));
    }

    @Test
    void getLocalPermissionsForLabelAccountNotFound() {
        when(accountService.getPersonalAccountById(ACCOUNT_ID)).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.getLocalPermissionsForLabel(ACCOUNT_ID, LABEL_ID));
        assertThat(exception.getStatus().value(), is(404));
        assertThat(exception.getMessage(), is(PERSONAL_ACCOUNT_NOT_FOUND));
    }

    @Test
    void getLocalPermissionsForLabelWhenLabelIdNotFound() {
        when(accountService.getPersonalAccountById(ACCOUNT_ID)).thenReturn(Optional.of(personalAccount));
        when(localPermissions.getLabelId()).thenReturn("otherLabel");
        when(personalAccount.getLocalPermissions()).thenReturn(List.of(localPermissions));
        ResponseEntity<RestLocalPermissions> response = service.getLocalPermissionsForLabel(ACCOUNT_ID, LABEL_ID);
        assertThat(response.getStatusCodeValue(), is(200));
        assertThat(response.getBody().getLabelId(), is(LABEL_ID));
        assertThat(response.getBody().getPermissions(), empty());
    }

    @Test
    void updateLocalPermissionsForLabel() {
        when(labelRepository.exists(LABEL_ID)).thenReturn(true);
        when(accountService.updatePersonalAccountLocalPermissionsById(eq(ACCOUNT_ID), any(LocalPermissions.class)))
                .thenReturn(Optional.of(personalAccount));
        when(personalAccountMapper.convertToLocalPermissions(List.of(RestPermission.READ))).thenReturn(List.of(Permission.READ));
        when(localPermissions.getLabelId()).thenReturn(LABEL_ID);
        when(personalAccount.getLocalPermissions()).thenReturn(List.of(localPermissions));
        when(personalAccountMapper.convertToRestLocalPermission(localPermissions)).thenReturn(restLocalPermissions);
        ResponseEntity<RestLocalPermissions> response = service.updateLocalPermissionsForLabel(ACCOUNT_ID, LABEL_ID, List.of(RestPermission.READ));
        assertThat(response.getStatusCodeValue(), is(200));
        assertThat(response.getBody(), sameInstance(restLocalPermissions));
        verify(accountService).updatePersonalAccountLocalPermissionsById(eq(ACCOUNT_ID), localPermissionsArgumentCaptor.capture());
        LocalPermissions localPermissions = localPermissionsArgumentCaptor.getValue();
        assertThat(localPermissions.getPermissions(), contains(Permission.READ));
        assertThat(localPermissions.getLabelId(), is(LABEL_ID));
    }

    @Test
    void updateLocalPermissionsForLabelNotExists() {
        when(labelRepository.exists(LABEL_ID)).thenReturn(false);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.updateLocalPermissionsForLabel(ACCOUNT_ID, LABEL_ID, List.of(RestPermission.READ)));
        assertThat(exception.getStatus().value(), is(400));
        assertThat(exception.getMessage(), is("400 BAD_REQUEST \"label not found : labelId\""));
    }
}
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
package com.rabobank.argos.service.domain.account;

import com.rabobank.argos.domain.ArgosError;
import com.rabobank.argos.domain.account.Account;
import com.rabobank.argos.domain.account.NonPersonalAccount;
import com.rabobank.argos.domain.account.NonPersonalAccountKeyPair;
import com.rabobank.argos.domain.account.PersonalAccount;
import com.rabobank.argos.domain.key.KeyPair;
import com.rabobank.argos.domain.permission.LocalPermissions;
import com.rabobank.argos.domain.permission.Permission;
import com.rabobank.argos.domain.permission.Role;
import com.rabobank.argos.service.domain.permission.RoleRepository;
import com.rabobank.argos.service.domain.security.AccountSecurityContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.rabobank.argos.domain.permission.Role.ADMINISTRATOR_ROLE_NAME;
import static com.rabobank.argos.domain.permission.Role.USER_ROLE;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    private static final String ACCOUNT_NAME = "accountName";
    private static final String ACCOUNT_ID = "accountId";
    private static final String EMAIL = "email";
    private static final String ROLE_ID = "roleId";
    private static final String KEY_ID = "keyId";
    private static final String ROLE_NAME = "roleName";
    private static final String PARENT_LABEL_ID = "parentLabelId";
    private static final String LABEL_ID = "labelId";
    private static final String ADMIN_ROLE_ID = "adminRoleId";
    private static final String ADMIN_ACCOUNT_ID = "adminAccountId";


    private KeyPair activeKeyPair = new KeyPair();
    private KeyPair inactiveKeyPair = new KeyPair();
    private KeyPair newKeyPair = new KeyPair();

    private AccountServiceImpl accountService;

    @Mock
    private NonPersonalAccountRepository nonPersonalAccountRepository;

    @Mock
    private PersonalAccountRepository personalAccountRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PersonalAccount account;

    @Mock
    private Role role;

    @Mock
    private NonPersonalAccountKeyPair nonPersonalAccountKeyPair;

    @Mock
    private PersonalAccount existingAccount;

    @Mock
    private NonPersonalAccount existingNonPersonalAccount;

    @Mock
    private NonPersonalAccount nonPersonalAccount;

    @Mock
    private AccountSearchParams params;

    @Mock
    private LocalPermissions newLocalPermissions;

    @Mock
    private LocalPermissions existingLocalPermissions;

    @Captor
    private ArgumentCaptor<List<LocalPermissions>> localPermissionsListArgumentCaptor;

    @Mock
    private Role adminRole;

    @Mock
    private AccountSecurityContext accountSecurityContext;

    @Mock
    private Account adminAccount;

    @BeforeEach
    void setUp() {
        accountService = new AccountServiceImpl(nonPersonalAccountRepository, personalAccountRepository, roleRepository, accountSecurityContext);
    }

    @Test
    void deactivateKeyPairNoActiveKeyAndNoInactiveKeys() {
        PersonalAccount account = PersonalAccount.builder().name(ACCOUNT_NAME).build();
        when(personalAccountRepository.findByAccountId(ACCOUNT_ID)).thenReturn(Optional.of(account));
        assertThat(accountService.activateNewKey(ACCOUNT_ID, newKeyPair), is(Optional.of(account)));
        assertThat(account.getInactiveKeyPairs(), empty());
        assertThat(account.getActiveKeyPair(), sameInstance(newKeyPair));
    }

    @Test
    void deactivateKeyPairNoActiveKey() {
        PersonalAccount account = PersonalAccount.builder().name(ACCOUNT_NAME).activeKeyPair(activeKeyPair).build();
        when(personalAccountRepository.findByAccountId(ACCOUNT_ID)).thenReturn(Optional.of(account));
        assertThat(accountService.activateNewKey(ACCOUNT_ID, newKeyPair), is(Optional.of(account)));
        assertThat(account.getInactiveKeyPairs(), contains(activeKeyPair));
        assertThat(account.getActiveKeyPair(), sameInstance(newKeyPair));
    }

    @Test
    void deactivateKeyPairNoActiveKeyAndEmptyList() {
        PersonalAccount account = PersonalAccount.builder().name(ACCOUNT_NAME).activeKeyPair(activeKeyPair).inactiveKeyPairs(emptyList()).build();
        when(personalAccountRepository.findByAccountId(ACCOUNT_ID)).thenReturn(Optional.of(account));
        assertThat(accountService.activateNewKey(ACCOUNT_ID, newKeyPair), is(Optional.of(account)));
        assertThat(account.getInactiveKeyPairs(), contains(activeKeyPair));
        assertThat(account.getActiveKeyPair(), sameInstance(newKeyPair));
    }

    @Test
    void deactivateKeyPairNoActiveKeyAndInactiveKeyPair() {
        PersonalAccount account = PersonalAccount.builder().name(ACCOUNT_NAME).activeKeyPair(activeKeyPair).inactiveKeyPairs(Collections.singletonList(inactiveKeyPair)).build();
        when(personalAccountRepository.findByAccountId(ACCOUNT_ID)).thenReturn(Optional.of(account));
        accountService.activateNewKey(ACCOUNT_ID, newKeyPair);
        assertThat(account.getInactiveKeyPairs(), contains(inactiveKeyPair, activeKeyPair));
        assertThat(account.getActiveKeyPair(), sameInstance(newKeyPair));
        verify(personalAccountRepository).update(account);
    }

    @Test
    void authenticateFirstUserShouldBeAssignedRoleAdminAndRoleUser() {
        when(personalAccountRepository.getTotalNumberOfAccounts()).thenReturn(0L);
        when(account.getEmail()).thenReturn(EMAIL);
        when(personalAccountRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(role.getRoleId()).thenReturn(ROLE_ID);
        when(roleRepository.findByName(ADMINISTRATOR_ROLE_NAME)).thenReturn(Optional.of(role));
        when(roleRepository.findByName(USER_ROLE)).thenReturn(Optional.of(role));
        PersonalAccount personalAccount = accountService.authenticateUser(account).get();
        assertThat(personalAccount, sameInstance(account));
        verify(personalAccount).setRoleIds(List.of(ROLE_ID));
        verify(personalAccount).addRoleId(ROLE_ID);
        verify(personalAccountRepository).save(personalAccount);
    }

    @Test
    void authenticateSecondUserShouldBeAssignedRoleUser() {
        when(personalAccountRepository.getTotalNumberOfAccounts()).thenReturn(1L);
        when(account.getEmail()).thenReturn(EMAIL);
        when(personalAccountRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(role.getRoleId()).thenReturn(ROLE_ID);
        when(roleRepository.findByName(ArgumentMatchers.anyString())).thenReturn(Optional.of(role));
        PersonalAccount personalAccount = accountService.authenticateUser(account).get();
        assertThat(personalAccount, sameInstance(account));
        verify(personalAccount).addRoleId(ROLE_ID);
        verify(personalAccountRepository).save(personalAccount);
    }

    @Test
    void authenticateUserSecondTime() {
        when(account.getEmail()).thenReturn(EMAIL);
        when(account.getName()).thenReturn(ACCOUNT_NAME);
        when(personalAccountRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existingAccount));
        PersonalAccount personalAccount = accountService.authenticateUser(account).get();
        assertThat(personalAccount, sameInstance(existingAccount));
        verify(personalAccount).setName(ACCOUNT_NAME);
        verify(personalAccountRepository).update(personalAccount);
    }

    @Test
    void activateNewKey() {
        when(nonPersonalAccountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(nonPersonalAccount));
        assertThat(accountService.activateNewKey(ACCOUNT_ID, nonPersonalAccountKeyPair), is(Optional.of(nonPersonalAccount)));
        verify(nonPersonalAccount).setActiveKeyPair(nonPersonalAccountKeyPair);
        verify(nonPersonalAccountRepository).update(nonPersonalAccount);
    }

    @Test
    void keyPairExistsNot() {
        assertThat(accountService.keyPairExists(KEY_ID), is(false));
    }

    @Test
    void nonPersonalAccountKeyPairExists() {
        when(nonPersonalAccountRepository.activeKeyExists(KEY_ID)).thenReturn(true);
        assertThat(accountService.keyPairExists(KEY_ID), is(true));
    }

    @Test
    void personalAccountKeyPairExists() {
        when(personalAccountRepository.activeKeyExists(KEY_ID)).thenReturn(true);
        assertThat(accountService.keyPairExists(KEY_ID), is(true));
    }

    @Test
    void findKeyPairByKeyIdNot() {
        assertThat(accountService.findKeyPairByKeyId(KEY_ID), is(Optional.empty()));
    }

    @Test
    void nonPersonalAccountFindKeyPairByKeyId() {
        when(nonPersonalAccount.getActiveKeyPair()).thenReturn(activeKeyPair);
        when(nonPersonalAccountRepository.findByActiveKeyId(KEY_ID)).thenReturn(Optional.of(nonPersonalAccount));
        assertThat(accountService.findKeyPairByKeyId(KEY_ID), is(Optional.of(activeKeyPair)));
    }

    @Test
    void personalAccountFindKeyPairByKeyId() {
        when(account.getActiveKeyPair()).thenReturn(activeKeyPair);
        when(personalAccountRepository.findByActiveKeyId(KEY_ID)).thenReturn(Optional.of(account));
        assertThat(accountService.findKeyPairByKeyId(KEY_ID), is(Optional.of(activeKeyPair)));
    }

    @Test
    void getPersonalAccountById() {
        when(personalAccountRepository.findByAccountId(ACCOUNT_ID)).thenReturn(Optional.of(account));
        assertThat(accountService.getPersonalAccountById(ACCOUNT_ID), is(Optional.of(account)));
    }

    @Test
    void searchPersonalAccounts() {
        when(personalAccountRepository.search(params)).thenReturn(List.of(account));
        assertThat(accountService.searchPersonalAccounts(params), contains(account));
    }


    @Test
    void updatePersonalAccountRolesById() {
        when(adminAccount.getAccountId()).thenReturn(ADMIN_ACCOUNT_ID);
        when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.of(adminAccount));
        when(role.getRoleId()).thenReturn(ROLE_ID);
        when(roleRepository.findByName(ROLE_NAME)).thenReturn(Optional.of(role));

        when(account.getAccountId()).thenReturn(ACCOUNT_ID);
        when(personalAccountRepository.findByAccountId(ACCOUNT_ID)).thenReturn(Optional.of(account));
        assertThat(accountService.updatePersonalAccountRolesById(ACCOUNT_ID, List.of(ROLE_NAME)), is(Optional.of(account)));
        verify(account).setRoleIds(List.of(ROLE_ID));
        verify(personalAccountRepository).update(account);
    }

    @Test
    void administratorUpdatePersonalAccountRolesById() {
        when(account.getAccountId()).thenReturn(ACCOUNT_ID);
        when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.of(account));
        when(role.getRoleId()).thenReturn(ROLE_ID);
        when(roleRepository.findByName(ROLE_NAME)).thenReturn(Optional.of(role));
        when(adminRole.getRoleId()).thenReturn(ADMIN_ROLE_ID);
        when(roleRepository.findByName(ADMINISTRATOR_ROLE_NAME)).thenReturn(Optional.of(adminRole));
        when(personalAccountRepository.findByAccountId(ACCOUNT_ID)).thenReturn(Optional.of(account));
        assertThat(accountService.updatePersonalAccountRolesById(ACCOUNT_ID, List.of(ROLE_NAME, ADMINISTRATOR_ROLE_NAME)), is(Optional.of(account)));
        verify(account).setRoleIds(List.of(ROLE_ID, ADMIN_ROLE_ID));
        verify(personalAccountRepository).update(account);
    }

    @Test
    void updatePersonalAccountRolesByIdNotAllowed() {
        when(account.getAccountId()).thenReturn(ACCOUNT_ID);
        when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.of(account));
        when(adminRole.getRoleId()).thenReturn(ADMIN_ROLE_ID);
        when(roleRepository.findByName(ADMINISTRATOR_ROLE_NAME)).thenReturn(Optional.of(adminRole));
        when(personalAccountRepository.findByAccountId(ACCOUNT_ID)).thenReturn(Optional.of(account));
        when(account.getRoleIds()).thenReturn(List.of(ADMIN_ROLE_ID));
        ArgosError exception = assertThrows(ArgosError.class, () -> accountService.updatePersonalAccountRolesById(ACCOUNT_ID, List.of(ROLE_NAME)));
        assertThat(exception.getMessage(), is("administrators can not unassign there own administrator role"));
        assertThat(exception.getLevel(), is(ArgosError.Level.WARNING));
    }

    @Test
    void updatePersonalAccountAdministratorRoleNotFound() {
        when(account.getAccountId()).thenReturn(ACCOUNT_ID);
        when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.of(account));
        when(roleRepository.findByName(ADMINISTRATOR_ROLE_NAME)).thenReturn(Optional.empty());
        when(personalAccountRepository.findByAccountId(ACCOUNT_ID)).thenReturn(Optional.of(account));
        when(account.getRoleIds()).thenReturn(List.of(ADMIN_ROLE_ID));
        ArgosError exception = assertThrows(ArgosError.class, () -> accountService.updatePersonalAccountRolesById(ACCOUNT_ID, List.of(ROLE_NAME)));
        assertThat(exception.getMessage(), is("administrator role not found"));
        assertThat(exception.getLevel(), is(ArgosError.Level.ERROR));
    }

    @Test
    void updatePersonalAccountAdministratorNoAuthenticatedAccount() {

        when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.empty());
        when(personalAccountRepository.findByAccountId(ACCOUNT_ID)).thenReturn(Optional.of(account));
        ArgosError exception = assertThrows(ArgosError.class, () -> accountService.updatePersonalAccountRolesById(ACCOUNT_ID, List.of(ROLE_NAME)));
        assertThat(exception.getMessage(), is("no authenticated account"));
        assertThat(exception.getLevel(), is(ArgosError.Level.ERROR));
    }

    @Test
    void save() {
        accountService.save(nonPersonalAccount);
        verify(nonPersonalAccountRepository).save(nonPersonalAccount);
    }

    @Test
    void findNonPersonalAccountById() {
        when(nonPersonalAccountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(nonPersonalAccount));
        assertThat(accountService.findNonPersonalAccountById(ACCOUNT_ID), is(Optional.of(nonPersonalAccount)));
    }

    @Test
    void update() {
        when(nonPersonalAccount.getName()).thenReturn(ACCOUNT_NAME);
        when(nonPersonalAccount.getEmail()).thenReturn(EMAIL);
        when(nonPersonalAccount.getParentLabelId()).thenReturn(PARENT_LABEL_ID);
        when(nonPersonalAccountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(existingNonPersonalAccount));
        assertThat(accountService.update(ACCOUNT_ID, nonPersonalAccount), is(Optional.of(existingNonPersonalAccount)));
        verify(nonPersonalAccountRepository).update(existingNonPersonalAccount);
        verify(existingNonPersonalAccount).setEmail(EMAIL);
        verify(existingNonPersonalAccount).setName(ACCOUNT_NAME);
        verify(existingNonPersonalAccount).setParentLabelId(PARENT_LABEL_ID);
    }

    @Test
    void updatePersonalAccountLocalPermissionsByIdExistingLocalPermissions() {
        when(existingLocalPermissions.getLabelId()).thenReturn(LABEL_ID);
        when(account.getLocalPermissions()).thenReturn(List.of(existingLocalPermissions));
        when(newLocalPermissions.getLabelId()).thenReturn(LABEL_ID);
        when(newLocalPermissions.getPermissions()).thenReturn(List.of(Permission.READ));
        when(personalAccountRepository.findByAccountId(ACCOUNT_ID)).thenReturn(Optional.of(account));
        assertThat(accountService.updatePersonalAccountLocalPermissionsById(ACCOUNT_ID, newLocalPermissions), is(Optional.of(account)));
        verify(existingLocalPermissions).setPermissions(List.of(Permission.READ));
        verify(personalAccountRepository).update(account);
    }

    @Test
    void updatePersonalAccountLocalPermissionsByIdExistingLocalPermissionsDelete() {
        when(existingLocalPermissions.getLabelId()).thenReturn(LABEL_ID);
        when(account.getLocalPermissions()).thenReturn(List.of(existingLocalPermissions));
        when(newLocalPermissions.getLabelId()).thenReturn(LABEL_ID);
        when(newLocalPermissions.getPermissions()).thenReturn(emptyList());
        when(personalAccountRepository.findByAccountId(ACCOUNT_ID)).thenReturn(Optional.of(account));
        assertThat(accountService.updatePersonalAccountLocalPermissionsById(ACCOUNT_ID, newLocalPermissions), is(Optional.of(account)));
        verify(account).setLocalPermissions(localPermissionsListArgumentCaptor.capture());
        assertThat(localPermissionsListArgumentCaptor.getValue(), empty());
        verify(personalAccountRepository).update(account);
        verify(personalAccountRepository).update(account);
    }

    @Test
    void updatePersonalAccountLocalPermissionsByIdNonExistingLocalPermissions() {
        when(existingLocalPermissions.getLabelId()).thenReturn(LABEL_ID);
        when(account.getLocalPermissions()).thenReturn(List.of(existingLocalPermissions));
        when(newLocalPermissions.getLabelId()).thenReturn("other");
        when(newLocalPermissions.getPermissions()).thenReturn(List.of(Permission.READ));
        when(personalAccountRepository.findByAccountId(ACCOUNT_ID)).thenReturn(Optional.of(account));
        assertThat(accountService.updatePersonalAccountLocalPermissionsById(ACCOUNT_ID, newLocalPermissions), is(Optional.of(account)));
        verify(account).setLocalPermissions(localPermissionsListArgumentCaptor.capture());
        assertThat(localPermissionsListArgumentCaptor.getValue(), contains(existingLocalPermissions, newLocalPermissions));
        verify(personalAccountRepository).update(account);
    }

    @Test
    void updatePersonalAccountLocalPermissionsByIdAccountNotFound() {
        when(personalAccountRepository.findByAccountId(ACCOUNT_ID)).thenReturn(Optional.empty());
        assertThat(accountService.updatePersonalAccountLocalPermissionsById(ACCOUNT_ID, newLocalPermissions), is(Optional.empty()));
        verifyNoMoreInteractions(personalAccountRepository);
    }
}
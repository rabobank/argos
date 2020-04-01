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
import com.rabobank.argos.domain.permission.Role;
import com.rabobank.argos.service.domain.permission.RoleRepository;
import com.rabobank.argos.service.domain.security.AccountSecurityContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.rabobank.argos.domain.permission.Role.ADMINISTRATOR_ROLE_NAME;
import static com.rabobank.argos.domain.permission.Role.USER_ROLE;


@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {

    private final NonPersonalAccountRepository nonPersonalAccountRepository;
    private final PersonalAccountRepository personalAccountRepository;
    private final RoleRepository roleRepository;
    private final AccountSecurityContext accountSecurityContext;

    @Override
    public Optional<PersonalAccount> activateNewKey(String accountId, KeyPair newKeyPair) {
        return personalAccountRepository.findByAccountId(accountId).map(account -> {
            activateNewKey(account, newKeyPair);
            personalAccountRepository.update(account);
            return account;
        });
    }

    @Override
    public Optional<NonPersonalAccount> activateNewKey(String accountId, NonPersonalAccountKeyPair newKeyPair) {
        return nonPersonalAccountRepository.findById(accountId).map(account -> {
            activateNewKey(account, newKeyPair);
            nonPersonalAccountRepository.update(account);
            return account;
        });
    }

    @Override
    public boolean keyPairExists(String keyId) {
        return nonPersonalAccountRepository.activeKeyExists(keyId) ||
                personalAccountRepository.activeKeyExists(keyId);
    }

    @Override
    public Optional<KeyPair> findKeyPairByKeyId(String keyId) {
        return nonPersonalAccountRepository
                .findByActiveKeyId(keyId).map(nonPersonalAccount -> (Account) nonPersonalAccount)
                .or(() -> personalAccountRepository.findByActiveKeyId(keyId)).map(Account::getActiveKeyPair);
    }

    @Override
    public Optional<PersonalAccount> authenticateUser(PersonalAccount personalAccount) {
        return Optional.of(personalAccountRepository.findByEmail(personalAccount.getEmail()).map(currentAccount -> {
            currentAccount.setName(personalAccount.getName());
            personalAccountRepository.update(currentAccount);
            return currentAccount;
        }).orElseGet(() -> {
            if (getTotalPersonalAccounts() == 0) {
                makeAdministrator(personalAccount);
            }
            addRoleUser(personalAccount);
            personalAccountRepository.save(personalAccount);
            return personalAccount;
        }));
    }

    @Override
    public Optional<PersonalAccount> getPersonalAccountById(String accountId) {
        return personalAccountRepository.findByAccountId(accountId);
    }

    @Override
    public List<PersonalAccount> searchPersonalAccounts(AccountSearchParams params) {
        return personalAccountRepository.search(params);
    }

    @Override
    public Optional<PersonalAccount> updatePersonalAccountRolesById(String accountId, List<String> roleNames) {
        return personalAccountRepository.findByAccountId(accountId).map(personalAccount -> {
            checkAdministratorRoleChange(personalAccount, roleNames);
            personalAccount.setRoleIds(getRoleIds(roleNames));
            personalAccountRepository.update(personalAccount);
            return personalAccount;
        });
    }

    private void checkAdministratorRoleChange(PersonalAccount personalAccount, List<String> roleNames) {
        Account authenticatedAccount = accountSecurityContext.getAuthenticatedAccount().orElseThrow(() -> new ArgosError("no authenticated account"));
        if (authenticatedAccount.getAccountId().equals(personalAccount.getAccountId()) && !roleNames.contains(ADMINISTRATOR_ROLE_NAME) &&
                personalAccount.getRoleIds().contains(resolveAdministratorRoleId())) {
            throw new ArgosError("administrators can not unassign there own administrator role", ArgosError.Level.WARNING);
        }
    }

    @Override
    public Optional<PersonalAccount> updatePersonalAccountLocalPermissionsById(String accountId, LocalPermissions newLocalPermissions) {
        return personalAccountRepository.findByAccountId(accountId).map(personalAccount -> {
            if (newLocalPermissions.getPermissions().isEmpty()) {
                removeLocalPermissions(newLocalPermissions, personalAccount);
            } else {
                addOrUpdateLocalPermissions(newLocalPermissions, personalAccount);
            }
            return personalAccount;
        });
    }

    private void addOrUpdateLocalPermissions(LocalPermissions newLocalPermissions, PersonalAccount personalAccount) {
        findLocalPermissions(newLocalPermissions, personalAccount)
                .ifPresentOrElse(localPermissions -> localPermissions.setPermissions(newLocalPermissions.getPermissions()),
                        () -> {
                            ArrayList<LocalPermissions> localPermissions = new ArrayList<>(personalAccount.getLocalPermissions());
                            localPermissions.add(newLocalPermissions);
                            personalAccount.setLocalPermissions(localPermissions);
                        });
        personalAccountRepository.update(personalAccount);
    }

    private void removeLocalPermissions(LocalPermissions newLocalPermissions, PersonalAccount personalAccount) {
        findLocalPermissions(newLocalPermissions, personalAccount).ifPresent(localPermissions -> {
            ArrayList<LocalPermissions> localPermissionList = new ArrayList<>(personalAccount.getLocalPermissions());
            localPermissionList.remove(localPermissions);
            personalAccount.setLocalPermissions(localPermissionList);
            personalAccountRepository.update(personalAccount);
        });
    }

    private Optional<LocalPermissions> findLocalPermissions(LocalPermissions newLocalPermissions, PersonalAccount personalAccount) {
        return personalAccount.getLocalPermissions().stream()
                .filter(localPermissions -> localPermissions.getLabelId().equals(newLocalPermissions.getLabelId()))
                .findFirst();
    }

    @Override
    public void save(NonPersonalAccount nonPersonalAccount) {
        nonPersonalAccountRepository.save(nonPersonalAccount);
    }

    @Override
    public Optional<NonPersonalAccount> findNonPersonalAccountById(String accountId) {
        return nonPersonalAccountRepository.findById(accountId);
    }

    @Override
    public Optional<NonPersonalAccount> update(String accountId, NonPersonalAccount nonPersonalAccount) {
        return nonPersonalAccountRepository.findById(accountId).map(account -> {
            account.setParentLabelId(nonPersonalAccount.getParentLabelId());
            account.setName(nonPersonalAccount.getName());
            account.setEmail(nonPersonalAccount.getEmail());
            nonPersonalAccountRepository.update(account);
            return account;
        });
    }

    private void addRoleUser(PersonalAccount personalAccount) {
        roleRepository.findByName(USER_ROLE)
                .ifPresent(userRole -> personalAccount.addRoleId(userRole.getRoleId()));

    }

    private List<String> getRoleIds(List<String> roleNames) {
        return roleNames.stream().map(roleRepository::findByName)
                .filter(Optional::isPresent)
                .map(Optional::get).map(Role::getRoleId).collect(Collectors.toList());
    }

    private long getTotalPersonalAccounts() {
        return personalAccountRepository.getTotalNumberOfAccounts();
    }

    private void makeAdministrator(PersonalAccount personalAccount) {
        log.info("Assigned administrator role to personal account " + personalAccount.getName());
        personalAccount.setRoleIds(List.of(resolveAdministratorRoleId()));
    }

    private String resolveAdministratorRoleId() {
        return roleRepository.findByName(ADMINISTRATOR_ROLE_NAME).map(Role::getRoleId)
                .orElseThrow(() -> new ArgosError(ADMINISTRATOR_ROLE_NAME + " role not found"));
    }

    private void activateNewKey(Account account, KeyPair newKeyPair) {
        deactivateKeyPair(account);
        account.setActiveKeyPair(newKeyPair);
    }

    private void deactivateKeyPair(Account account) {
        Optional.ofNullable(account.getActiveKeyPair()).ifPresent(keyPair -> {
            List<KeyPair> inactiveKeyPairs = new ArrayList<>(Optional.ofNullable(account.getInactiveKeyPairs()).orElse(Collections.emptyList()));
            inactiveKeyPairs.add(keyPair);
            account.setActiveKeyPair(null);
            account.setInactiveKeyPairs(inactiveKeyPairs);
        });
    }

}

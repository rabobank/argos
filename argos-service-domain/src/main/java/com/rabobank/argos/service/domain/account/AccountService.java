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

import com.rabobank.argos.domain.account.NonPersonalAccount;
import com.rabobank.argos.domain.account.NonPersonalAccountKeyPair;
import com.rabobank.argos.domain.account.PersonalAccount;
import com.rabobank.argos.domain.key.KeyPair;
import com.rabobank.argos.domain.permission.LocalPermissions;

import java.util.List;
import java.util.Optional;

public interface AccountService {
    Optional<PersonalAccount> activateNewKey(String accountId, KeyPair newKeyPair);

    Optional<NonPersonalAccount> activateNewKey(String accountId, NonPersonalAccountKeyPair newKeyPair);

    boolean keyPairExists(String keyId);

    Optional<KeyPair> findKeyPairByKeyId(String keyId);

    Optional<PersonalAccount> authenticateUser(PersonalAccount personalAccount);

    Optional<PersonalAccount> getPersonalAccountById(String accountId);

    List<PersonalAccount> searchPersonalAccounts(AccountSearchParams params);

    Optional<PersonalAccount> updatePersonalAccountRolesById(String accountId, List<String> roleNames);

    Optional<PersonalAccount> updatePersonalAccountLocalPermissionsById(String accountId, LocalPermissions localPermissions);

    void save(NonPersonalAccount nonPersonalAccount);

    Optional<NonPersonalAccount> findNonPersonalAccountById(String accountId);

    Optional<NonPersonalAccount> update(String accountId, NonPersonalAccount nonPersonalAccount);


}

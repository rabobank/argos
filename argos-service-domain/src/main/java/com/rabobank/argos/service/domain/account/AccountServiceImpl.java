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

import com.rabobank.argos.domain.account.Account;
import com.rabobank.argos.domain.key.KeyPair;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final NonPersonalAccountRepository nonPersonalAccountRepository;
    private final PersonalAccountRepository personalAccountRepository;

    @Override
    public Account activateNewKey(Account account, KeyPair newKeyPair) {
        deactivateKeyPair(account);
        account.setActiveKeyPair(newKeyPair);
        return account;
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

    private void deactivateKeyPair(Account account) {
        Optional.ofNullable(account.getActiveKeyPair()).ifPresent(keyPair -> {
            List<KeyPair> inactiveKeyPairs = new ArrayList<>(Optional.ofNullable(account.getInactiveKeyPairs()).orElse(Collections.emptyList()));
            inactiveKeyPairs.add(keyPair);
            account.setActiveKeyPair(null);
            account.setInactiveKeyPairs(inactiveKeyPairs);
        });
    }

}

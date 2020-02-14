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
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class AccountServiceImpl implements AccountService {

    @Override
    public <T extends Account<K>, K extends KeyPair> T activateNewKey(T account, K newKeyPair) {
        deactivateKeyPair(account);
        account.setActiveKeyPair(newKeyPair);
        return account;
    }

    private <T extends KeyPair> void deactivateKeyPair(Account<T> account) {
        Optional.ofNullable(account.getActiveKeyPair()).ifPresent(keyPair -> {
            List<T> inactiveKeyPairs = new ArrayList<>(Optional.ofNullable(account.getInactiveKeyPairs()).orElse(Collections.emptyList()));
            inactiveKeyPairs.add(keyPair);
            account.setActiveKeyPair(null);
            account.setInactiveKeyPairs(inactiveKeyPairs);
        });
    }

}

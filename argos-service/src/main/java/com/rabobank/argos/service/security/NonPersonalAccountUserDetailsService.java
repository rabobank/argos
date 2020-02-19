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
package com.rabobank.argos.service.security;

import com.rabobank.argos.domain.ArgosError;
import com.rabobank.argos.domain.account.NonPersonalAccount;
import com.rabobank.argos.service.domain.account.NonPersonalAccountRepository;
import com.rabobank.argos.service.domain.security.AccountUserDetailsAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NonPersonalAccountUserDetailsService {

    private final NonPersonalAccountRepository nonPersonalAccountRepository;

    public UserDetails loadUserById(String id) {
        NonPersonalAccount nonPersonalAccount = nonPersonalAccountRepository.findByActiveKeyId(id)
                .orElseThrow(() -> new ArgosError("Non personal account with keyid " + id + " not found"));
        return new AccountUserDetailsAdapter(nonPersonalAccount);
    }

}
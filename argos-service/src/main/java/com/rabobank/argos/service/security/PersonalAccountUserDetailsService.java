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
import com.rabobank.argos.domain.account.PersonalAccount;
import com.rabobank.argos.domain.permission.Permission;
import com.rabobank.argos.service.domain.account.PersonalAccountRepository;
import com.rabobank.argos.service.domain.permission.RoleRepository;
import com.rabobank.argos.service.domain.security.AccountUserDetailsAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PersonalAccountUserDetailsService {

    private final PersonalAccountRepository personalAccountRepository;

    private final RoleRepository roleRepository;

    public UserDetails loadUserById(String id) {
        PersonalAccount personalAccount = personalAccountRepository.findByAccountId(id)
                .orElseThrow(() -> new ArgosError("Personal account with id " + id + " not found"));
        Set<Permission> globalPermissions = roleRepository
                .findByIds(personalAccount.getRoleIds())
                .stream()
                .flatMap(role -> role.getPermissions().stream())
                .collect(Collectors.toSet());

        return new AccountUserDetailsAdapter(personalAccount, globalPermissions);
    }

}
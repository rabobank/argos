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
package com.rabobank.argos.integrationtest.service;

import com.rabobank.argos.domain.account.Account;
import com.rabobank.argos.domain.permission.Permission;
import com.rabobank.argos.service.domain.security.AccountSecurityContext;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class AccountSecurityContextMock implements AccountSecurityContext {
    @Override
    public Optional<Account> getAuthenticatedAccount() {
        return Optional.empty();
    }

    @Override
    public Set<Permission> getGlobalPermission() {
        return Collections.emptySet();
    }

    @Override
    public Set<Permission> allLocalPermissions(List<String> labelIds) {
        return Collections.emptySet();
    }
}

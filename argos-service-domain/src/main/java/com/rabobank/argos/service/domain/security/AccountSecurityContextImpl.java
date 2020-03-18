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
package com.rabobank.argos.service.domain.security;

import com.rabobank.argos.domain.account.Account;
import com.rabobank.argos.domain.permission.LocalPermissions;
import com.rabobank.argos.domain.permission.Permission;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;

@Component
public class AccountSecurityContextImpl implements AccountSecurityContext {

    @Override
    public Optional<Account> getAuthenticatedAccount() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getPrincipal)
                .map(authentication -> (AccountUserDetailsAdapter) authentication)
                .map(AccountUserDetailsAdapter::getAccount);
    }

    @Override
    public Set<Permission> getGlobalPermission() {
        AccountUserDetailsAdapter authentication = getAccountUserDetailsAdapter();
        if (authentication != null) {
            return authentication.getGlobalPermissions();
        } else {
            return emptySet();
        }

    }

    @Override
    public Set<Permission> allLocalPermissions(List<String> allLabelIdsUpTree) {
        AccountUserDetailsAdapter authentication = getAccountUserDetailsAdapter();
        if (authentication != null) {
            Map<String, List<LocalPermissions>> localPermissionsMap = authentication.getAccount().getLocalPermissions()
                    .stream()
                    .collect(Collectors.groupingBy(LocalPermissions::getLabelId));
            return allLabelIdsUpTree.stream()
                    .map(labelId -> localPermissionsMap.getOrDefault(labelId, emptyList()))
                    .flatMap(List::stream)
                    .map(LocalPermissions::getPermissions)
                    .flatMap(List::stream)
                    .collect(toSet());
        } else {
            return emptySet();
        }

    }

    private AccountUserDetailsAdapter getAccountUserDetailsAdapter() {
        return (AccountUserDetailsAdapter) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }
}


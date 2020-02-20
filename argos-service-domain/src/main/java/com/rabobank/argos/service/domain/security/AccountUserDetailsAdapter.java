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
import com.rabobank.argos.domain.account.PersonalAccount;
import lombok.EqualsAndHashCode;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
public class AccountUserDetailsAdapter extends org.springframework.security.core.userdetails.User {
    private final Account account;

    public AccountUserDetailsAdapter(PersonalAccount account) {
        super(account.getName(), "", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        this.account = account;
    }

    public String getId() {
        return account.getAccountId();
    }

    public Account getAccount() {
        return account;
    }
}

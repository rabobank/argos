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
package com.rabobank.argos.service.adapter.in.rest.personalaccount;


import com.rabobank.argos.service.adapter.in.rest.api.model.RestPersonalAccount;
import com.rabobank.argos.service.domain.account.PersonalAccount;
import com.rabobank.argos.service.domain.account.PersonalAccountRepository;
import com.rabobank.argos.service.domain.security.AccountUserDetailsAdapter;
import com.rabobank.argos.service.domain.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PersonalAccountRestServiceImpl {

    private final PersonalAccountRepository personalAccountRepository;

    @GetMapping("/user/me")
    @PreAuthorize("hasRole('USER')")
    public RestPersonalAccount getCurrentUser(@CurrentUser AccountUserDetailsAdapter accountUserDetailsAdapter) {
        return personalAccountRepository.findByUserId(accountUserDetailsAdapter.getId()).map(this::convert).orElseThrow(() -> profileNotFound(accountUserDetailsAdapter));
    }


    @PutMapping(value = "/user/me")
    @PreAuthorize("hasRole('USER')")
    public RestPersonalAccount updateUserProfile(@CurrentUser AccountUserDetailsAdapter accountUserDetailsAdapter, @Valid @RequestBody RestPersonalAccount restUserProfile) {
        return personalAccountRepository.findByUserId(accountUserDetailsAdapter.getId()).map(user -> {

            personalAccountRepository.update(user);
            return convert(user);
        }).orElseThrow(() -> profileNotFound(accountUserDetailsAdapter));
    }

    private RestPersonalAccount convert(PersonalAccount personalAccount) {
        return new RestPersonalAccount()
                .id(personalAccount.getAccountId())
                .name(personalAccount.getName())
                .email(personalAccount.getEmail());
    }

    private ResponseStatusException profileNotFound(AccountUserDetailsAdapter accountUserDetailsAdapter) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "profile not found for : " + accountUserDetailsAdapter.getId());
    }
}

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
package com.rabobank.argos.service.adapter.in.rest.account;

import com.rabobank.argos.domain.account.NonPersonalAccount;
import com.rabobank.argos.service.adapter.in.rest.api.handler.NonPersonalAccountApi;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestNonPersonalAccount;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestNonPersonalAccountKeyPair;
import com.rabobank.argos.service.domain.account.AccountService;
import com.rabobank.argos.service.domain.account.NonPersonalAccountRepository;
import com.rabobank.argos.service.domain.hierarchy.LabelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class NonPersonalAccountRestService implements NonPersonalAccountApi {

    private final NonPersonalAccountRepository accountRepository;

    private final NonPersonalAccountMapper accountMapper;

    private final LabelRepository labelRepository;

    private final AccountKeyPairMapper keyPairMapper;

    private final AccountService accountService;

    @Override
    public ResponseEntity<RestNonPersonalAccount> createNonPersonalAccount(RestNonPersonalAccount restNonPersonalAccount) {
        verifyParentLabelExists(restNonPersonalAccount.getParentLabelId());
        NonPersonalAccount nonPersonalAccount = accountMapper.convertFromRestNonPersonalAccount(restNonPersonalAccount);
        accountRepository.save(nonPersonalAccount);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{nonPersonalAccountId}")
                .buildAndExpand(nonPersonalAccount.getAccountId())
                .toUri();
        return ResponseEntity.created(location).body(accountMapper.convertToRestNonPersonalAccount(nonPersonalAccount));
    }

    @Override
    public ResponseEntity<RestNonPersonalAccountKeyPair> createNonPersonalAccountKeyById(String nonPersonalAccountId, RestNonPersonalAccountKeyPair restKeyPair) {
        NonPersonalAccount account = accountRepository.findById(nonPersonalAccountId).orElseThrow(() -> accountNotFound(nonPersonalAccountId));
        NonPersonalAccount updatedAccount = accountService.activateNewKey(account, keyPairMapper.convertFromRestKeyPair(restKeyPair));

        accountRepository.update(nonPersonalAccountId, account);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{nonPersonalAccountId}/key")
                .buildAndExpand(nonPersonalAccountId)
                .toUri();
        return ResponseEntity.created(location).body(keyPairMapper.convertToRestKeyPair(updatedAccount.getActiveKeyPair()));
    }

    @Override
    public ResponseEntity<RestNonPersonalAccountKeyPair> getNonPersonalAccountKeyById(String nonPersonalAccountId) {
        return accountRepository.findById(nonPersonalAccountId)
                .map(account -> Optional.ofNullable(account.getActiveKeyPair()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(keyPairMapper::convertToRestKeyPair)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> keyNotFound(nonPersonalAccountId));
    }

    @Override
    public ResponseEntity<RestNonPersonalAccount> getNonPersonalAccountById(String nonPersonalAccountId) {
        return accountRepository.findById(nonPersonalAccountId)
                .map(accountMapper::convertToRestNonPersonalAccount)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> accountNotFound(nonPersonalAccountId));
    }

    @Override
    public ResponseEntity<RestNonPersonalAccount> updateNonPersonalAccountById(String nonPersonalAccountId, RestNonPersonalAccount restNonPersonalAccount) {
        verifyParentLabelExists(restNonPersonalAccount.getParentLabelId());
        return accountRepository.update(nonPersonalAccountId, accountMapper.convertFromRestNonPersonalAccount(restNonPersonalAccount))
                .map(accountMapper::convertToRestNonPersonalAccount)
                .map(ResponseEntity::ok).orElseThrow(() -> accountNotFound(nonPersonalAccountId));
    }

    private void verifyParentLabelExists(String parentLabelId) {
        if (!labelRepository.exists(parentLabelId)) {
            throw parentLabelNotFound(parentLabelId);
        }
    }

    private ResponseStatusException keyNotFound(String accountId) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "no active personal account key with id : " + accountId + " not found");
    }

    private ResponseStatusException accountNotFound(String accountId) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "no personal account with id : " + accountId + " not found");
    }

    private ResponseStatusException parentLabelNotFound(String parentLabelId) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "parent label id not found : " + parentLabelId);
    }
}

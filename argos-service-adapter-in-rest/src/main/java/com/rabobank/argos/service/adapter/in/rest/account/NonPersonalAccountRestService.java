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

import com.rabobank.argos.domain.account.Account;
import com.rabobank.argos.domain.account.NonPersonalAccount;
import com.rabobank.argos.domain.account.NonPersonalAccountKeyPair;
import com.rabobank.argos.service.adapter.in.rest.api.handler.NonPersonalAccountApi;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestNonPersonalAccount;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestNonPersonalAccountKeyPair;
import com.rabobank.argos.service.domain.account.AccountService;
import com.rabobank.argos.service.domain.hierarchy.LabelRepository;
import com.rabobank.argos.service.domain.security.AccountSecurityContext;
import com.rabobank.argos.service.domain.security.LabelIdCheckParam;
import com.rabobank.argos.service.domain.security.PermissionCheck;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;

import static com.rabobank.argos.domain.permission.Permission.NPA_EDIT;
import static com.rabobank.argos.domain.permission.Permission.READ;
import static com.rabobank.argos.service.adapter.in.rest.account.NonPersonalAccountLabelIdExtractor.NPA_LABEL_ID_EXTRACTOR;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class NonPersonalAccountRestService implements NonPersonalAccountApi {

    private final NonPersonalAccountMapper accountMapper;

    private final LabelRepository labelRepository;

    private final AccountKeyPairMapper keyPairMapper;

    private final AccountService accountService;

    private final AccountSecurityContext accountSecurityContext;

    @Override
    @PermissionCheck(permissions = NPA_EDIT)
    public ResponseEntity<RestNonPersonalAccount> createNonPersonalAccount(@LabelIdCheckParam(propertyPath = "parentLabelId") RestNonPersonalAccount restNonPersonalAccount) {
        verifyParentLabelExists(restNonPersonalAccount.getParentLabelId());
        NonPersonalAccount nonPersonalAccount = accountMapper.convertFromRestNonPersonalAccount(restNonPersonalAccount);
        accountService.save(nonPersonalAccount);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{nonPersonalAccountId}")
                .buildAndExpand(nonPersonalAccount.getAccountId())
                .toUri();
        return ResponseEntity.created(location).body(accountMapper.convertToRestNonPersonalAccount(nonPersonalAccount));
    }

    @Override
    @PermissionCheck(permissions = NPA_EDIT)
    public ResponseEntity<RestNonPersonalAccountKeyPair> createNonPersonalAccountKeyById(@LabelIdCheckParam(dataExtractor = NPA_LABEL_ID_EXTRACTOR) String nonPersonalAccountId, RestNonPersonalAccountKeyPair restKeyPair) {
        NonPersonalAccount updatedAccount = accountService.activateNewKey(nonPersonalAccountId, keyPairMapper.convertFromRestKeyPair(restKeyPair))
                .orElseThrow(() -> accountNotFound(nonPersonalAccountId));
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{nonPersonalAccountId}/key")
                .buildAndExpand(nonPersonalAccountId)
                .toUri();
        return ResponseEntity.created(location).body(keyPairMapper.convertToRestKeyPair(((NonPersonalAccountKeyPair) updatedAccount.getActiveKeyPair())));
    }

    @Override
    @PermissionCheck(permissions = READ)
    public ResponseEntity<RestNonPersonalAccountKeyPair> getNonPersonalAccountKeyById(@LabelIdCheckParam(dataExtractor = NPA_LABEL_ID_EXTRACTOR) String nonPersonalAccountId) {
        return accountService.findNonPersonalAccountById(nonPersonalAccountId)
                .flatMap(account -> Optional.ofNullable(account.getActiveKeyPair()))
                .map(account -> (NonPersonalAccountKeyPair) account)
                .map(keyPairMapper::convertToRestKeyPair)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> keyNotFound(nonPersonalAccountId));
    }

    @Override
    @PermissionCheck(permissions = READ)
    public ResponseEntity<RestNonPersonalAccount> getNonPersonalAccountById(@LabelIdCheckParam(dataExtractor = NPA_LABEL_ID_EXTRACTOR) String nonPersonalAccountId) {
        return accountService.findNonPersonalAccountById(nonPersonalAccountId)
                .map(accountMapper::convertToRestNonPersonalAccount)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> accountNotFound(nonPersonalAccountId));
    }

    @Override
    @PermissionCheck(permissions = NPA_EDIT)
    public ResponseEntity<RestNonPersonalAccount> updateNonPersonalAccountById(@LabelIdCheckParam(dataExtractor = NPA_LABEL_ID_EXTRACTOR) String nonPersonalAccountId, RestNonPersonalAccount restNonPersonalAccount) {
        verifyParentLabelExists(restNonPersonalAccount.getParentLabelId());
        NonPersonalAccount nonPersonalAccount = accountMapper.convertFromRestNonPersonalAccount(restNonPersonalAccount);
        return accountService.update(nonPersonalAccountId, nonPersonalAccount)
                .map(accountMapper::convertToRestNonPersonalAccount)
                .map(ResponseEntity::ok).orElseThrow(() -> accountNotFound(nonPersonalAccountId));
    }

    @Override
    @PreAuthorize("hasRole('NONPERSONAL')")
    public ResponseEntity<RestNonPersonalAccountKeyPair> getNonPersonalAccountKey() {
        return accountSecurityContext.getAuthenticatedAccount()
                .map(Account::getActiveKeyPair).filter(Objects::nonNull)
                .map(keyPair -> (NonPersonalAccountKeyPair) keyPair)
                .map(keyPairMapper::convertToRestKeyPair)
                .map(ResponseEntity::ok).orElseThrow(this::keyNotFound);
    }

    private void verifyParentLabelExists(String parentLabelId) {
        if (!labelRepository.exists(parentLabelId)) {
            throw parentLabelNotFound(parentLabelId);
        }
    }

    private ResponseStatusException keyNotFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "no active non personal account key found");
    }

    private ResponseStatusException keyNotFound(String accountId) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "no active non personal account key with id : " + accountId + " found");
    }

    private ResponseStatusException accountNotFound(String accountId) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "no non personal account with id : " + accountId + " found");
    }

    private ResponseStatusException parentLabelNotFound(String parentLabelId) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "parent label id not found : " + parentLabelId);
    }
}

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
import com.rabobank.argos.domain.account.PersonalAccount;
import com.rabobank.argos.domain.key.KeyIdProvider;
import com.rabobank.argos.domain.key.KeyPair;
import com.rabobank.argos.domain.permission.LocalPermissions;
import com.rabobank.argos.domain.permission.Permission;
import com.rabobank.argos.service.adapter.in.rest.api.handler.PersonalAccountApi;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestKeyPair;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestLocalPermissions;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestPermission;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestPersonalAccount;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestProfile;
import com.rabobank.argos.service.domain.account.AccountSearchParams;
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

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PersonalAccountRestService implements PersonalAccountApi {

    private final AccountSecurityContext accountSecurityContext;
    private final AccountKeyPairMapper keyPairMapper;
    private final AccountService accountService;
    private final PersonalAccountMapper personalAccountMapper;
    private final LabelRepository labelRepository;


    @PreAuthorize("hasRole('USER')")
    @Override
    public ResponseEntity<RestProfile> getPersonalAccountOfAuthenticatedUser() {
        return accountSecurityContext.getAuthenticatedAccount()
                .map(account -> (PersonalAccount) account)
                .map(personalAccountMapper::convertToRestProfile)
                .map(ResponseEntity::ok).orElseThrow(this::accountNotFound);
    }

    @PreAuthorize("hasRole('USER')")
    @Override
    public ResponseEntity<Void> createKey(@Valid RestKeyPair restKeyPair) {
        Account account = accountSecurityContext.getAuthenticatedAccount().orElseThrow(this::accountNotFound);
        KeyPair keyPair = keyPairMapper.convertFromRestKeyPair(restKeyPair);
        validateKeyId(keyPair);
        accountService.activateNewKey(account.getAccountId(), keyPair);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    @PermissionCheck(permissions = {Permission.ASSIGN_ROLE})
    public ResponseEntity<RestPersonalAccount> getPersonalAccountById(String accountId) {
        return accountService.getPersonalAccountById(accountId)
                .map(personalAccountMapper::convertToRestPersonalAccount)
                .map(ResponseEntity::ok).orElseThrow(this::accountNotFound);
    }

    @Override
    @PermissionCheck(permissions = {Permission.PERSONAL_ACCOUNT_READ})
    public ResponseEntity<List<RestPersonalAccount>> searchPersonalAccounts(String roleName, String localPermissionsLabelId, String name) {
        return ResponseEntity.ok(accountService.searchPersonalAccounts(AccountSearchParams.builder()
                .roleId(personalAccountMapper.convertToRoleId(roleName))
                .localPermissionsLabelId(localPermissionsLabelId)
                .name(name)
                .build()).stream()
                .map(personalAccountMapper::convertToRestPersonalAccountWithoutRoles).collect(Collectors.toList()));
    }

    @Override
    @PermissionCheck(permissions = {Permission.ASSIGN_ROLE})
    public ResponseEntity<RestPersonalAccount> updatePersonalAccountRolesById(String accountId, List<String> roleNames) {
        return accountService.updatePersonalAccountRolesById(accountId, roleNames)
                .map(personalAccountMapper::convertToRestPersonalAccount)
                .map(ResponseEntity::ok).orElseThrow(this::accountNotFound);
    }

    @Override
    @PermissionCheck(permissions = {Permission.LOCAL_PERMISSION_EDIT})
    public ResponseEntity<List<RestLocalPermissions>> getAllLocalPermissions(String accountId) {
        return ResponseEntity.ok(accountService.getPersonalAccountById(accountId).map(PersonalAccount::getLocalPermissions)
                .map(personalAccountMapper::convertToRestLocalPermissions).orElse(Collections.emptyList()));
    }

    @Override
    @PermissionCheck(permissions = {Permission.LOCAL_PERMISSION_EDIT})
    public ResponseEntity<RestLocalPermissions> getLocalPermissionsForLabel(String accountId, @LabelIdCheckParam String labelId) {
        PersonalAccount personalAccount = accountService.getPersonalAccountById(accountId).orElseThrow(this::accountNotFound);
        return ResponseEntity.ok(personalAccount.getLocalPermissions().stream()
                .filter(localPermissions -> localPermissions.getLabelId().equals(labelId))
                .findFirst().map(personalAccountMapper::convertToRestLocalPermission)
                .orElseGet(() -> new RestLocalPermissions().labelId(labelId)));
    }

    @Override
    @PermissionCheck(permissions = {Permission.LOCAL_PERMISSION_EDIT})
    public ResponseEntity<RestLocalPermissions> updateLocalPermissionsForLabel(String accountId, @LabelIdCheckParam String labelId, List<RestPermission> restLocalPermission) {
        verifyParentLabelExists(labelId);
        LocalPermissions newLocalPermissions = LocalPermissions.builder().labelId(labelId).permissions(personalAccountMapper.convertToLocalPermissions(restLocalPermission)).build();
        PersonalAccount personalAccount = accountService.updatePersonalAccountLocalPermissionsById(accountId, newLocalPermissions).orElseThrow(this::accountNotFound);
        return personalAccount.getLocalPermissions().stream()
                .filter(localPermissions -> localPermissions.getLabelId().equals(labelId))
                .findFirst().map(personalAccountMapper::convertToRestLocalPermission).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @Override
    public ResponseEntity<RestKeyPair> getKeyPair() {
        Account account = accountSecurityContext
                .getAuthenticatedAccount().orElseThrow(this::accountNotFound);
        KeyPair keyPair = Optional.ofNullable(account.getActiveKeyPair()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "no active keypair found for account: " + account.getName()));
        return new ResponseEntity<>(keyPairMapper.convertToRestKeyPair(keyPair), HttpStatus.OK);
    }

    private void verifyParentLabelExists(String labelId) {
        if (!labelRepository.exists(labelId)) {
            throw labelNotFound(labelId);
        }
    }

    private ResponseStatusException labelNotFound(String labelId) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "label not found : " + labelId);
    }

    private void validateKeyId(KeyPair keyPair) {
        if (!keyPair.getKeyId().equals(KeyIdProvider.computeKeyId(keyPair.getPublicKey()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid key id : " + keyPair.getKeyId());
        }
    }

    private ResponseStatusException accountNotFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "personal account not found");
    }
}

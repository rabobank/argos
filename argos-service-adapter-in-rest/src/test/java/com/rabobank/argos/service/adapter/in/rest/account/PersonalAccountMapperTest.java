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

import com.rabobank.argos.domain.account.PersonalAccount;
import com.rabobank.argos.domain.permission.LocalPermissions;
import com.rabobank.argos.domain.permission.Permission;
import com.rabobank.argos.domain.permission.Role;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestLocalPermissions;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestPermission;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestPersonalAccount;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestRole;
import com.rabobank.argos.service.adapter.in.rest.permission.RoleMapper;
import com.rabobank.argos.service.domain.permission.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasLength;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonalAccountMapperTest {

    private static final String EMAIL = "email";
    private static final String NAME = "name";
    private static final String ROLE_ID = "roleId";
    private static final String LABEL_ID = "labelId";
    private static final String ROLE_NAME = "roleName";
    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RoleMapper roleMapper;


    private PersonalAccountMapperImpl mapper;

    @Mock
    private Role role;

    @Mock
    private RestRole restRole;


    @BeforeEach
    void setUp() {
        mapper = new PersonalAccountMapperImpl();
        ReflectionTestUtils.setField(mapper, "roleRepository", roleRepository);
        ReflectionTestUtils.setField(mapper, "roleMapper", roleMapper);
    }

    @Test
    void convertToRestPersonalAccount() {
        when(roleRepository.findByIds(List.of(ROLE_ID))).thenReturn(List.of(role));
        when(roleMapper.convertToRestRole(role)).thenReturn(restRole);
        RestPersonalAccount restPersonalAccount = mapper.convertToRestPersonalAccount(mockPersonalAccount());
        validate(restPersonalAccount);
        assertThat(restPersonalAccount.getRoles(), contains(restRole));
    }

    @Test
    void convertToRestPersonalAccountWithoutRoles() {
        RestPersonalAccount restPersonalAccount = mapper.convertToRestPersonalAccountWithoutRoles(mockPersonalAccount());
        validate(restPersonalAccount);
    }

    @Test
    void convertToRestLocalPermission() {
        LocalPermissions localPermissions = LocalPermissions.builder().labelId(LABEL_ID).permissions(List.of(Permission.values())).build();
        RestLocalPermissions restLocalPermissions = mapper.convertToRestLocalPermission(localPermissions);
        assertThat(restLocalPermissions.getLabelId(), is(LABEL_ID));
        assertThat(restLocalPermissions.getPermissions(), contains(RestPermission.values()));
    }

    @Test
    void localPermissionListToRestLocalPermissionList() {
        assertThat(mapper.permissionListToRestPermissionList(List.of(Permission.values())), contains(RestPermission.values()));
    }

    @Test
    void convertToRestLocalPermissions() {
        LocalPermissions localPermissions = LocalPermissions.builder().labelId(LABEL_ID).permissions(List.of(Permission.values())).build();
        List<RestLocalPermissions> restLocalPermissions = mapper.convertToRestLocalPermissions(List.of(localPermissions));
        assertThat(restLocalPermissions, contains(new RestLocalPermissions().labelId(LABEL_ID).permissions(List.of(RestPermission.values()))));
    }

    @Test
    void convertToLocalPermissions() {
        assertThat(mapper.convertToLocalPermissions(List.of(RestPermission.values())), contains(Permission.values()));
    }

    @Test
    void convertToRoleId() {
        when(role.getRoleId()).thenReturn(ROLE_ID);
        when(roleRepository.findByName(ROLE_NAME)).thenReturn(Optional.of(role));
        assertThat(mapper.convertToRoleId(ROLE_NAME), is(ROLE_ID));
    }

    @Test
    void convertToRoleIdNameNull() {
        assertThat(mapper.convertToRoleId(null), nullValue());
    }

    @Test
    void convertToRoleIdNotFound() {
        when(roleRepository.findByName(ROLE_NAME)).thenReturn(Optional.empty());
        assertThat(mapper.convertToRoleId(ROLE_NAME), nullValue());
    }

    private void validate(RestPersonalAccount restPersonalAccount) {
        assertThat(restPersonalAccount.getName(), is(NAME));
        assertThat(restPersonalAccount.getId(), hasLength(36));
    }

    private PersonalAccount mockPersonalAccount() {
        return PersonalAccount.builder().email(EMAIL).name(NAME).roleIds(List.of(ROLE_ID)).build();
    }

}
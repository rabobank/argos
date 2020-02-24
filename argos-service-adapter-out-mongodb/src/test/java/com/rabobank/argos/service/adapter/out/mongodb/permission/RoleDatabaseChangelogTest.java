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
package com.rabobank.argos.service.adapter.out.mongodb.permission;

import com.rabobank.argos.domain.permission.GlobalPermission;
import com.rabobank.argos.domain.permission.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexOperations;

import static com.rabobank.argos.service.adapter.out.mongodb.permission.RoleRepositoryImpl.COLLECTION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleDatabaseChangelogTest {

    private RoleDatabaseChangelog changelog;

    @Mock
    private MongoTemplate template;

    @Mock
    private IndexOperations operations;

    @Captor
    private ArgumentCaptor<Role> roleArgumentCaptor;

    @BeforeEach
    void setUp() {
        changelog = new RoleDatabaseChangelog();
    }

    @Test
    void addIndex() {
        when(template.indexOps(COLLECTION)).thenReturn(operations);
        changelog.addIndex(template);
        verify(operations, times(2)).ensureIndex(any());
    }

    @Test
    void addAdminRole() {
        changelog.addAdminRole(template);
        verify(template).save(roleArgumentCaptor.capture(), eq(COLLECTION));
        Role role = roleArgumentCaptor.getValue();
        assertThat(role.getName(), is(Role.ADMINISTRATOR_ROLE_NAME));
        assertThat(role.getPermissions(), contains(GlobalPermission.READ, GlobalPermission.EDIT_GLOBAL_PERMISSIONS));
    }
}
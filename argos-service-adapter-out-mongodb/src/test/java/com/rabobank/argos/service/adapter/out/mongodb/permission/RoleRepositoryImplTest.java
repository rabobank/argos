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

import com.rabobank.argos.domain.permission.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.rabobank.argos.service.adapter.out.mongodb.permission.RoleRepositoryImpl.COLLECTION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleRepositoryImplTest {

    private static final String ROLE_ID = "roleId";
    private static final String ROLE_NAME = "roleName";
    @Mock
    private MongoTemplate template;

    private RoleRepositoryImpl repository;

    @Mock
    private Role role;

    @Captor
    private ArgumentCaptor<Query> argumentCaptor;

    @BeforeEach
    void setUp() {
        repository = new RoleRepositoryImpl(template);
    }

    @Test
    void save() {
        repository.save(role);
        verify(template).save(role, COLLECTION);
    }

    @Test
    void findAll() {
        when(template.findAll(Role.class, COLLECTION)).thenReturn(List.of(role));
        assertThat(repository.findAll(), contains(role));
    }

    @Test
    void findByIds() {
        when(template.find(any(Query.class), eq(Role.class), eq(COLLECTION))).thenReturn(new ArrayList(Collections.singletonList(role)));
        assertThat(repository.findByIds(List.of(ROLE_ID)), contains(role));
        verify(template).find(argumentCaptor.capture(), eq(Role.class), eq(COLLECTION));
        assertThat(argumentCaptor.getValue().toString(), is("Query: { \"roleId\" : { \"$in\" : [\"roleId\"]}}, Fields: {}, Sort: {}"));
    }

    @Test
    void findByName() {
        when(template.findOne(any(Query.class), eq(Role.class), eq(COLLECTION))).thenReturn(role);
        assertThat(repository.findByName(ROLE_NAME), is(Optional.of(role)));
        verify(template).findOne(argumentCaptor.capture(), eq(Role.class), eq(COLLECTION));
        assertThat(argumentCaptor.getValue().toString(), is("Query: { \"name\" : \"roleName\"}, Fields: {}, Sort: {}"));
    }
}
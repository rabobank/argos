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
package com.rabobank.argos.service.adapter.out.mongodb.user;

import com.mongodb.client.result.UpdateResult;
import com.rabobank.argos.service.domain.user.User;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRepositoryImplTest {
    private static final String COLLECTION = "users";
    @Mock
    private MongoTemplate template;

    private UserRepositoryImpl repository;

    @Mock
    private User user;

    @Captor
    private ArgumentCaptor<Query> queryArgumentCaptor;

    @Mock
    private IndexOperations indexOperations;

    @Mock
    private MongoConverter converter;

    @Mock
    private UpdateResult updateResult;

    @Captor
    private ArgumentCaptor<Update> updateArgumentCaptor;

    @BeforeEach
    void setUp() {
        repository = new UserRepositoryImpl(template);
    }

    @Test
    void postConstructShouldConfigure() {
        when(template.indexOps(COLLECTION)).thenReturn(indexOperations);
        repository.postConstruct();
        verify(template, times(2)).indexOps(COLLECTION);
    }

    @Test
    void saveShouldUser() {
        repository.save(user);
        verify(template).save(user, COLLECTION);
    }

    @Test
    void findByUserId() {
        when(template.findOne(any(), eq(User.class), eq(COLLECTION))).thenReturn(user);
        assertThat(repository.findByUserId("userId"), is(Optional.of(user)));
        verify(template).findOne(queryArgumentCaptor.capture(), eq(User.class), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), is("Query: { \"userId\" : \"userId\"}, Fields: {}, Sort: {}"));
    }

    @Test
    void findByEmail() {
        when(template.findOne(any(), eq(User.class), eq(COLLECTION))).thenReturn(user);
        assertThat(repository.findByEmail("email"), is(Optional.of(user)));
        verify(template).findOne(queryArgumentCaptor.capture(), eq(User.class), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), is("Query: { \"email\" : \"email\"}, Fields: {}, Sort: {}"));
    }

    @Test
    void update() {
        when(user.getUserId()).thenReturn("userId");
        when(template.getConverter()).thenReturn(converter);
        when(template.updateFirst(any(Query.class), any(Update.class), eq(User.class), eq(COLLECTION))).thenReturn(updateResult);
        repository.update(user);

        verify(template).updateFirst(queryArgumentCaptor.capture(), updateArgumentCaptor.capture(), eq(User.class), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), is("Query: { \"userId\" : \"userId\"}, Fields: {}, Sort: {}"));
        assertThat(updateArgumentCaptor.getValue().toString(), is("{}"));
        verify(converter).write(eq(user), any(Document.class));
    }
}

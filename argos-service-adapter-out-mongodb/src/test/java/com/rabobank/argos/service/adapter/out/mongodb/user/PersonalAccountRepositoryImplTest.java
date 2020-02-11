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
import com.rabobank.argos.service.domain.account.PersonalAccount;
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
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Optional;

import static com.rabobank.argos.service.adapter.out.mongodb.user.PersonalAccountRepositoryImpl.COLLECTION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonalAccountRepositoryImplTest {


    @Mock
    private MongoTemplate template;

    private PersonalAccountRepositoryImpl repository;

    @Mock
    private PersonalAccount personalAccount;

    @Captor
    private ArgumentCaptor<Query> queryArgumentCaptor;

    @Mock
    private MongoConverter converter;

    @Mock
    private UpdateResult updateResult;

    @Captor
    private ArgumentCaptor<Update> updateArgumentCaptor;

    @BeforeEach
    void setUp() {
        repository = new PersonalAccountRepositoryImpl(template);
    }

    @Test
    void saveShouldUser() {
        repository.save(personalAccount);
        verify(template).save(personalAccount, COLLECTION);
    }

    @Test
    void findByUserId() {
        when(template.findOne(any(), eq(PersonalAccount.class), eq(COLLECTION))).thenReturn(personalAccount);
        assertThat(repository.findByUserId("userId"), is(Optional.of(personalAccount)));
        verify(template).findOne(queryArgumentCaptor.capture(), eq(PersonalAccount.class), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), is("Query: { \"userId\" : \"userId\"}, Fields: {}, Sort: {}"));
    }

    @Test
    void findByEmail() {
        when(template.findOne(any(), eq(PersonalAccount.class), eq(COLLECTION))).thenReturn(personalAccount);
        assertThat(repository.findByEmail("email"), is(Optional.of(personalAccount)));
        verify(template).findOne(queryArgumentCaptor.capture(), eq(PersonalAccount.class), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), is("Query: { \"email\" : \"email\"}, Fields: {}, Sort: {}"));
    }

    @Test
    void update() {
        when(personalAccount.getAccountId()).thenReturn("userId");
        when(template.getConverter()).thenReturn(converter);
        when(template.updateFirst(any(Query.class), any(Update.class), eq(PersonalAccount.class), eq(COLLECTION))).thenReturn(updateResult);
        repository.update(personalAccount);

        verify(template).updateFirst(queryArgumentCaptor.capture(), updateArgumentCaptor.capture(), eq(PersonalAccount.class), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), is("Query: { \"userId\" : \"userId\"}, Fields: {}, Sort: {}"));
        assertThat(updateArgumentCaptor.getValue().toString(), is("{}"));
        verify(converter).write(eq(personalAccount), any(Document.class));
    }
}

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
package com.rabobank.argos.service.adapter.out.mongodb.account;

import com.mongodb.client.result.UpdateResult;
import com.rabobank.argos.domain.account.PersonalAccount;
import com.rabobank.argos.service.domain.account.AccountSearchParams;
import org.bson.Document;
import org.hamcrest.Matchers;
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

import java.util.List;
import java.util.Optional;

import static com.rabobank.argos.service.adapter.out.mongodb.account.PersonalAccountRepositoryImpl.COLLECTION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonalAccountRepositoryImplTest {


    private static final String ACTIVE_KEY_ID = "activeKeyId";
    private static final long COUNT = 12334L;
    private static final String ROLE_ID = "roleId";
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
        assertThat(repository.findByAccountId("userId"), is(Optional.of(personalAccount)));
        verify(template).findOne(queryArgumentCaptor.capture(), eq(PersonalAccount.class), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), is("Query: { \"accountId\" : \"userId\"}, Fields: {}, Sort: {}"));
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
        assertThat(queryArgumentCaptor.getValue().toString(), is("Query: { \"accountId\" : \"userId\"}, Fields: {}, Sort: {}"));
        assertThat(updateArgumentCaptor.getValue().toString(), is("{}"));
        verify(converter).write(eq(personalAccount), any(Document.class));
    }

    @Test
    void activeKeyExists() {
        when(template.exists(any(Query.class), eq(PersonalAccount.class), eq(COLLECTION))).thenReturn(true);
        assertThat(repository.activeKeyExists(ACTIVE_KEY_ID), Matchers.is(true));
        verify(template).exists(queryArgumentCaptor.capture(), eq(PersonalAccount.class), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), Matchers.is("Query: { \"activeKeyPair.keyId\" : \"activeKeyId\"}, Fields: {}, Sort: {}"));
    }

    @Test
    void findByActiveKeyId() {
        when(template.findOne(any(Query.class), eq(PersonalAccount.class), eq(COLLECTION))).thenReturn(personalAccount);
        assertThat(repository.findByActiveKeyId(ACTIVE_KEY_ID), equalTo(Optional.of(personalAccount)));
        verify(template).findOne(queryArgumentCaptor.capture(), eq(PersonalAccount.class), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), Matchers.is("Query: { \"activeKeyPair.keyId\" : \"activeKeyId\"}, Fields: {}, Sort: {}"));
    }

    @Test
    void getTotalNumberOfAccounts() {
        when(template.count(any(Query.class), eq(PersonalAccount.class), eq(COLLECTION))).thenReturn(COUNT);
        assertThat(repository.getTotalNumberOfAccounts(), equalTo(COUNT));
        verify(template).count(queryArgumentCaptor.capture(), eq(PersonalAccount.class), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), Matchers.is("Query: {}, Fields: {}, Sort: {}"));
    }


    @Test
    void search() {
        when(template.find(any(Query.class), eq(PersonalAccount.class), eq(COLLECTION))).thenReturn(List.of(personalAccount));
        assertThat(repository.search(AccountSearchParams.builder().build()), contains(personalAccount));
        verify(template).find(queryArgumentCaptor.capture(), eq(PersonalAccount.class), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), Matchers.is("Query: {}, Fields: { \"accountId\" : 1, \"name\" : 1, \"email\" : 1}, Sort: { \"name\" : 1}"));
    }
}

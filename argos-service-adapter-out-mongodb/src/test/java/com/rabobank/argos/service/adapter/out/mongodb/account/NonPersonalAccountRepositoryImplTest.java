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
import com.rabobank.argos.domain.ArgosError;
import com.rabobank.argos.domain.account.NonPersonalAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Optional;

import static com.rabobank.argos.service.adapter.out.mongodb.account.NonPersonalAccountRepositoryImpl.COLLECTION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NonPersonalAccountRepositoryImplTest {

    private static final String ACCOUNT_ID = "accountId";
    private static final String ACCOUNT_NAME = "accountName";
    private static final String PARENT_LABEL_ID = "parentLabelId";
    private static final String ACTIVE_KEY_ID = "activeKeyId";

    @Mock
    private MongoTemplate template;
    private NonPersonalAccountRepositoryImpl repository;

    @Mock
    private NonPersonalAccount nonPersonalAccount;

    @Captor
    private ArgumentCaptor<Query> queryArgumentCaptor;

    @Captor
    private ArgumentCaptor<Update> updateArgumentCaptor;

    @Mock
    private MongoConverter converter;

    @Mock
    private UpdateResult updateResult;

    @Mock
    private DuplicateKeyException duplicateKeyException;

    @BeforeEach
    void setUp() {
        repository = new NonPersonalAccountRepositoryImpl(template);
    }

    @Test
    void save() {
        repository.save(nonPersonalAccount);
        verify(template).save(nonPersonalAccount, COLLECTION);
    }

    @Test
    void saveDuplicateKeyException() {
        when(nonPersonalAccount.getName()).thenReturn(ACCOUNT_NAME);
        when(nonPersonalAccount.getParentLabelId()).thenReturn(PARENT_LABEL_ID);
        doThrow(duplicateKeyException).when(template).save(nonPersonalAccount, COLLECTION);
        ArgosError argosError = assertThrows(ArgosError.class, () -> repository.save(nonPersonalAccount));
        assertThat(argosError.getMessage(), is("non personal account with name: accountName and parentLabelId: parentLabelId already exists"));
        assertThat(argosError.getCause(), sameInstance(duplicateKeyException));
        assertThat(argosError.getLevel(), is(ArgosError.Level.WARNING));
    }

    @Test
    void findById() {
        when(template.findOne(any(), eq(NonPersonalAccount.class), eq(COLLECTION))).thenReturn(nonPersonalAccount);
        assertThat(repository.findById(ACCOUNT_ID), is(Optional.of(nonPersonalAccount)));
        verify(template).findOne(queryArgumentCaptor.capture(), eq(NonPersonalAccount.class), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), is("Query: { \"accountId\" : \"accountId\"}, Fields: {}, Sort: {}"));
    }

    @Test
    void updateFound() {
        when(template.getConverter()).thenReturn(converter);
        when(template.updateFirst(any(), any(), eq(NonPersonalAccount.class), eq(COLLECTION))).thenReturn(updateResult);
        when(updateResult.getMatchedCount()).thenReturn(1L);
        Optional<NonPersonalAccount> update = repository.update(ACCOUNT_ID, nonPersonalAccount);
        assertThat(update, is(Optional.of(nonPersonalAccount)));
        verify(nonPersonalAccount).setAccountId(ACCOUNT_ID);
        verify(template).updateFirst(queryArgumentCaptor.capture(), updateArgumentCaptor.capture(), eq(NonPersonalAccount.class), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), is("Query: { \"accountId\" : \"accountId\"}, Fields: {}, Sort: {}"));
        verify(converter).write(eq(nonPersonalAccount), any());
        assertThat(updateArgumentCaptor.getValue().toString(), is("{}"));
    }

    @Test
    void updateNotFound() {
        when(template.getConverter()).thenReturn(converter);
        when(template.updateFirst(any(), any(), eq(NonPersonalAccount.class), eq(COLLECTION))).thenReturn(updateResult);
        when(updateResult.getMatchedCount()).thenReturn(0L);
        Optional<NonPersonalAccount> update = repository.update(ACCOUNT_ID, nonPersonalAccount);
        assertThat(update, is(Optional.empty()));
    }

    @Test
    void updateDuplicateKeyException() {
        when(template.getConverter()).thenReturn(converter);
        when(template.updateFirst(any(), any(), eq(NonPersonalAccount.class), eq(COLLECTION))).thenThrow(duplicateKeyException);
        ArgosError argosError = assertThrows(ArgosError.class, () -> repository.update(ACCOUNT_ID, nonPersonalAccount));
        assertThat(argosError.getCause(), sameInstance(duplicateKeyException));
    }

    @Test
    void activeKeyExists() {
        when(template.exists(any(Query.class), eq(NonPersonalAccount.class), eq(COLLECTION))).thenReturn(true);
        assertThat(repository.activeKeyExists(ACTIVE_KEY_ID), is(true));
        verify(template).exists(queryArgumentCaptor.capture(), eq(NonPersonalAccount.class), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), is("Query: { \"activeKeyPair.keyId\" : \"activeKeyId\"}, Fields: {}, Sort: {}"));
    }

    @Test
    void findByActiveKeyId() {
        when(template.findOne(any(Query.class), eq(NonPersonalAccount.class), eq(COLLECTION))).thenReturn(nonPersonalAccount);
        assertThat(repository.findByActiveKeyId(ACTIVE_KEY_ID), equalTo(Optional.of(nonPersonalAccount)));
        verify(template).findOne(queryArgumentCaptor.capture(), eq(NonPersonalAccount.class), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), is("Query: { \"activeKeyPair.keyId\" : \"activeKeyId\"}, Fields: {}, Sort: {}"));
    }
}
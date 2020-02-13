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
package com.rabobank.argos.service.adapter.out.mongodb.supplychain;

import com.mongodb.client.result.UpdateResult;
import com.rabobank.argos.domain.ArgosError;
import com.rabobank.argos.domain.supplychain.SupplyChain;
import org.hamcrest.Matchers;
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

import static com.rabobank.argos.service.adapter.out.mongodb.supplychain.SupplyChainRepositoryImpl.COLLECTION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupplyChainRepositoryImplTest {

    private static final String SUPPLY_CHAIN_ID = "supplyChainId";
    private static final String SUPPLY_CHAIN_NAME = "supplyChainName";
    private static final String PARENT_LABEL_ID = "parentLabelId";

    @Mock
    private MongoTemplate template;

    @Captor
    private ArgumentCaptor<Query> queryArgumentCaptor;

    private SupplyChainRepositoryImpl repository;

    @Mock
    private SupplyChain supplyChain;

    @Mock
    private DuplicateKeyException duplicateKeyException;

    @Captor
    private ArgumentCaptor<Update> updateArgumentCaptor;

    @Mock
    private MongoConverter converter;

    @Mock
    private UpdateResult updateResult;

    @BeforeEach
    void setUp() {
        repository = new SupplyChainRepositoryImpl(template);
    }

    @Test
    void findBySupplyChainId() {
        when(template.findOne(any(), eq(SupplyChain.class), eq(COLLECTION))).thenReturn(supplyChain);
        assertThat(repository.findBySupplyChainId(SUPPLY_CHAIN_ID), is(Optional.of(supplyChain)));
        verify(template).findOne(queryArgumentCaptor.capture(), eq(SupplyChain.class), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), is("Query: { \"supplyChainId\" : \"supplyChainId\"}, Fields: {}, Sort: {}"));
    }

    @Test
    void exists() {
        when(template.exists(any(), eq(SupplyChain.class), eq(COLLECTION))).thenReturn(true);
        assertThat(repository.exists(SUPPLY_CHAIN_ID), is(true));
        verify(template).exists(queryArgumentCaptor.capture(), eq(SupplyChain.class), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), is("Query: { \"supplyChainId\" : \"supplyChainId\"}, Fields: {}, Sort: {}"));
    }

    @Test
    void save() {
        repository.save(supplyChain);
        verify(template).save(supplyChain, COLLECTION);
    }

    @Test
    void saveDuplicateKeyException() {
        when(supplyChain.getName()).thenReturn(SUPPLY_CHAIN_NAME);
        when(supplyChain.getParentLabelId()).thenReturn(PARENT_LABEL_ID);
        doThrow(duplicateKeyException).when(template).save(supplyChain, COLLECTION);
        ArgosError argosError = assertThrows(ArgosError.class, () -> repository.save(supplyChain));
        assertThat(argosError.getMessage(), Matchers.is("supply chain with name: supplyChainName and parentLabelId: parentLabelId already exists"));
        assertThat(argosError.getCause(), sameInstance(duplicateKeyException));
        assertThat(argosError.getLevel(), Matchers.is(ArgosError.Level.WARNING));
    }

    @Test
    void updateFound() {
        when(template.getConverter()).thenReturn(converter);
        when(template.updateFirst(any(), any(), eq(SupplyChain.class), eq(COLLECTION))).thenReturn(updateResult);
        when(updateResult.getMatchedCount()).thenReturn(1L);
        Optional<SupplyChain> update = repository.update(SUPPLY_CHAIN_ID, supplyChain);
        assertThat(update, Matchers.is(Optional.of(supplyChain)));
        verify(supplyChain).setSupplyChainId(SUPPLY_CHAIN_ID);
        verify(template).updateFirst(queryArgumentCaptor.capture(), updateArgumentCaptor.capture(), eq(SupplyChain.class), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), Matchers.is("Query: { \"supplyChainId\" : \"supplyChainId\"}, Fields: {}, Sort: {}"));
        verify(converter).write(eq(supplyChain), any());
        assertThat(updateArgumentCaptor.getValue().toString(), Matchers.is("{}"));
    }

    @Test
    void updateNotFound() {
        when(template.getConverter()).thenReturn(converter);
        when(template.updateFirst(any(), any(), eq(SupplyChain.class), eq(COLLECTION))).thenReturn(updateResult);
        when(updateResult.getMatchedCount()).thenReturn(0L);
        Optional<SupplyChain> update = repository.update(SUPPLY_CHAIN_ID, supplyChain);
        assertThat(update, Matchers.is(Optional.empty()));
    }

    @Test
    void updateDuplicateKeyException() {
        when(template.getConverter()).thenReturn(converter);
        when(template.updateFirst(any(), any(), eq(SupplyChain.class), eq(COLLECTION))).thenThrow(duplicateKeyException);
        ArgosError argosError = assertThrows(ArgosError.class, () -> repository.update(SUPPLY_CHAIN_ID, supplyChain));
        assertThat(argosError.getCause(), sameInstance(duplicateKeyException));
    }


}

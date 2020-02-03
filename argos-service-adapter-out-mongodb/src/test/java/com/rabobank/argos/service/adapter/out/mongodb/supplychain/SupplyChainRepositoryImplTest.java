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

import com.rabobank.argos.domain.supplychain.SupplyChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.rabobank.argos.service.adapter.out.mongodb.supplychain.SupplyChainRepositoryImpl.COLLECTION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupplyChainRepositoryImplTest {

    private static final String SUPPLY_CHAIN_ID = "supplyChainId";

    @Mock
    private MongoTemplate template;

    @Captor
    private ArgumentCaptor<Query> queryArgumentCaptor;

    private SupplyChainRepositoryImpl repository;

    @Mock
    private SupplyChain supplyChain;

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
    void findByName_With_Name_Should_Return_SupplyChain_List() {
        when(template.find(any(), eq(SupplyChain.class), eq(COLLECTION))).thenReturn(Collections.singletonList(supplyChain));
        List<SupplyChain> result = repository.findByName("name");
        assertThat(result.isEmpty(), is(false));
        assertThat(result.get(0), is(supplyChain));
        verify(template).find(queryArgumentCaptor.capture(), eq(SupplyChain.class), eq(COLLECTION));
    }

    @Test
    void findByAll_Should_Return_SupplyChain_List() {
        when(template.findAll(eq(SupplyChain.class), eq(COLLECTION))).thenReturn(Collections.singletonList(supplyChain));
        List<SupplyChain> result = repository.findAll();
        assertThat(result.isEmpty(), is(false));
        assertThat(result.get(0), is(supplyChain));
        verify(template).findAll(eq(SupplyChain.class), eq(COLLECTION));
    }
}

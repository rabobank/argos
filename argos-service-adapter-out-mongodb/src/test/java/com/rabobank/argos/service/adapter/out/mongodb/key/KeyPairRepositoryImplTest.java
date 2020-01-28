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
package com.rabobank.argos.service.adapter.out.mongodb.key;

import com.rabobank.argos.domain.key.KeyPair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import static com.rabobank.argos.service.adapter.out.mongodb.key.KeyPairRepositoryImpl.COLLECTION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KeyPairRepositoryImplTest {

    @Mock
    private MongoTemplate template;

    private KeyPairRepositoryImpl keyPairRepository;

    @Mock
    private KeyPair keyPair;

    @Captor
    private ArgumentCaptor<Query> queryArgumentCaptor;

    @BeforeEach
    void setUp() {
        keyPairRepository = new KeyPairRepositoryImpl(template);
    }

    @Test
    void saveShouldStoreKeyPair() {
        keyPairRepository.save(keyPair);
        verify(template).save(keyPair, COLLECTION);
    }

    @Test
    void findByKeyIdShouldFindKeypair() {
        when(template.findOne(any(), eq(KeyPair.class), eq(COLLECTION))).thenReturn(keyPair);
        keyPairRepository.findByKeyId("keyId");
        verify(template).findOne(queryArgumentCaptor.capture(), eq(KeyPair.class), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), is("Query: { \"keyId\" : \"keyId\"}, Fields: {}, Sort: {}"));
    }
}

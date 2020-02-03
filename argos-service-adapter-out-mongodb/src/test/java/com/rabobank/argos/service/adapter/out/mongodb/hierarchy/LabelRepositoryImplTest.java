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
package com.rabobank.argos.service.adapter.out.mongodb.hierarchy;

import com.mongodb.client.result.UpdateResult;
import com.rabobank.argos.domain.ArgosError;
import com.rabobank.argos.domain.hierarchy.Label;
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

import static com.rabobank.argos.service.adapter.out.mongodb.hierarchy.LabelRepositoryImpl.COLLECTION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LabelRepositoryImplTest {

    private static final String LABEL_ID = "labelId";
    private static final String LABEL_NAME = "labelName";
    private static final String PARENT_LABEL_ID = "parentLabelId";

    @Mock
    private MongoTemplate template;
    private LabelRepositoryImpl repository;

    @Mock
    private Label label;

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
        repository = new LabelRepositoryImpl(template);
    }

    @Test
    void save() {
        repository.save(label);
        verify(template).save(label, COLLECTION);
    }

    @Test
    void saveDuplicateKeyException() {
        when(label.getName()).thenReturn(LABEL_NAME);
        when(label.getParentLabelId()).thenReturn(PARENT_LABEL_ID);
        doThrow(duplicateKeyException).when(template).save(label, COLLECTION);
        ArgosError argosError = assertThrows(ArgosError.class, () -> repository.save(label));
        assertThat(argosError.getMessage(), is("label with name: labelName and parentLabelId: parentLabelId already exists"));
        assertThat(argosError.getCause(), sameInstance(duplicateKeyException));
        assertThat(argosError.getLevel(), is(ArgosError.Level.WARNING));
    }

    @Test
    void findById() {
        when(template.findOne(any(), eq(Label.class), eq(COLLECTION))).thenReturn(label);
        assertThat(repository.findById(LABEL_ID), is(Optional.of(label)));
        verify(template).findOne(queryArgumentCaptor.capture(), eq(Label.class), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), is("Query: { \"labelId\" : \"labelId\"}, Fields: {}, Sort: {}"));
    }

    @Test
    void exists() {
        when(template.exists(any(), eq(Label.class), eq(COLLECTION))).thenReturn(true);
        assertThat(repository.exists(LABEL_ID), is(true));
        verify(template).exists(queryArgumentCaptor.capture(), eq(Label.class), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), is("Query: { \"labelId\" : \"labelId\"}, Fields: {}, Sort: {}"));
    }

    @Test
    void deleteById() {
        assertThat(repository.deleteById(LABEL_ID), is(false));
    }

    @Test
    void updateFound() {
        when(template.getConverter()).thenReturn(converter);
        when(template.updateFirst(any(), any(), eq(Label.class), eq(COLLECTION))).thenReturn(updateResult);
        when(updateResult.getMatchedCount()).thenReturn(1L);
        Optional<Label> update = repository.update(LABEL_ID, label);
        assertThat(update, is(Optional.of(label)));
        verify(label).setLabelId(LABEL_ID);
        verify(template).updateFirst(queryArgumentCaptor.capture(), updateArgumentCaptor.capture(), eq(Label.class), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), is("Query: { \"labelId\" : \"labelId\"}, Fields: {}, Sort: {}"));
        verify(converter).write(eq(label), any());
        assertThat(updateArgumentCaptor.getValue().toString(), is("{}"));
    }

    @Test
    void updateNotFound() {
        when(template.getConverter()).thenReturn(converter);
        when(template.updateFirst(any(), any(), eq(Label.class), eq(COLLECTION))).thenReturn(updateResult);
        when(updateResult.getMatchedCount()).thenReturn(0L);
        Optional<Label> update = repository.update(LABEL_ID, label);
        assertThat(update, is(Optional.empty()));
    }

    @Test
    void updateDuplicateKeyException() {
        when(template.getConverter()).thenReturn(converter);
        when(template.updateFirst(any(), any(), eq(Label.class), eq(COLLECTION))).thenThrow(duplicateKeyException);
        ArgosError argosError = assertThrows(ArgosError.class, () -> repository.update(LABEL_ID, label));
        assertThat(argosError.getCause(), sameInstance(duplicateKeyException));
    }
}
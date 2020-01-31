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
package com.rabobank.argos.service.adapter.in.rest.hierarchy;

import com.rabobank.argos.domain.hierarchy.Label;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestLabel;
import com.rabobank.argos.service.domain.hierarchy.LabelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HierarchyRestServiceTest {

    private static final String LABEL_ID = "labelId";
    @Mock
    private LabelRepository labelRepository;

    @Mock
    private LabelMapper labelMapper;

    private HierarchyRestService service;

    @Mock
    private RestLabel restLabel;

    @Mock
    private Label label;

    @BeforeEach
    void setUp() {
        service = new HierarchyRestService(labelRepository, labelMapper);
    }

    @Test
    void createLabel() {
        when(labelMapper.convertFromRestLabel(restLabel)).thenReturn(label);
        when(labelMapper.convertToRestLabel(label)).thenReturn(restLabel);
        ResponseEntity<RestLabel> response = service.createLabel(restLabel);
        assertThat(response.getStatusCodeValue(), is(200));
        assertThat(response.getBody(), sameInstance(restLabel));
        verify(labelRepository).save(label);
    }

    @Test
    void deleteLabelById() {
        when(labelRepository.deleteById(LABEL_ID)).thenReturn(true);
        ResponseEntity<Void> response = service.deleteLabelById(LABEL_ID);
        assertThat(response.getStatusCodeValue(), is(204));
    }

    @Test
    void deleteLabelByIdNotFound() {
        when(labelRepository.deleteById(LABEL_ID)).thenReturn(false);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.deleteLabelById(LABEL_ID));
        assertThat(exception.getMessage(), is("404 NOT_FOUND \"label not found : labelId\""));
    }

    @Test
    void getLabelById() {
        when(labelRepository.findById(LABEL_ID)).thenReturn(Optional.of(label));
        when(labelMapper.convertToRestLabel(label)).thenReturn(restLabel);
        ResponseEntity<RestLabel> response = service.getLabelById(LABEL_ID);
        assertThat(response.getStatusCodeValue(), is(200));
        assertThat(response.getBody(), sameInstance(restLabel));
    }

    @Test
    void getLabelByIdNotFound() {
        when(labelRepository.findById(LABEL_ID)).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.getLabelById(LABEL_ID));
        assertThat(exception.getMessage(), is("404 NOT_FOUND \"label not found : labelId\""));
    }

    @Test
    void updateLabelById() {
        when(labelMapper.convertFromRestLabel(restLabel)).thenReturn(label);
        when(labelMapper.convertToRestLabel(label)).thenReturn(restLabel);
        when(labelRepository.update(LABEL_ID, label)).thenReturn(Optional.of(label));
        ResponseEntity<RestLabel> response = service.updateLabelById(LABEL_ID, restLabel);
        assertThat(response.getStatusCodeValue(), is(200));
        assertThat(response.getBody(), sameInstance(restLabel));
    }

    @Test
    void updateLabelByIdNotFound() {
        when(labelMapper.convertFromRestLabel(restLabel)).thenReturn(label);
        when(labelRepository.update(LABEL_ID, label)).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.updateLabelById(LABEL_ID, restLabel));
        assertThat(exception.getMessage(), is("404 NOT_FOUND \"label not found : labelId\""));
    }
}
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

import com.rabobank.argos.domain.hierarchy.HierarchyMode;
import com.rabobank.argos.domain.hierarchy.Label;
import com.rabobank.argos.domain.hierarchy.TreeNode;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestHierarchyMode;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestLabel;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestTreeNode;
import com.rabobank.argos.service.domain.hierarchy.HierarchyRepository;
import com.rabobank.argos.service.domain.hierarchy.LabelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HierarchyRestServiceTest {

    private static final String LABEL_ID = "labelId";
    private static final String PARENT_LABEL_ID = "parentLabelId";
    private static final Integer MAX_DEPTH = 10;
    private static final String REFERENCE_ID = "referenceId";
    @Mock
    private LabelRepository labelRepository;

    @Mock
    private LabelMapper labelMapper;

    @Mock
    private HierarchyRepository hierarchyRepository;

    @Mock
    private TreeNodeMapper treeNodeMapper;

    private HierarchyRestService service;

    @Mock
    private RestLabel restLabel;

    @Mock
    private Label label;

    @Mock
    private TreeNode treeNode;

    @Mock
    private RestTreeNode restTreeNode;

    @Mock
    private HttpServletRequest httpServletRequest;

    @BeforeEach
    void setUp() {
        service = new HierarchyRestService(labelRepository, labelMapper, hierarchyRepository, treeNodeMapper);
    }

    @Test
    void createLabel() {
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        when(labelMapper.convertFromRestLabel(restLabel)).thenReturn(label);
        when(labelMapper.convertToRestLabel(label)).thenReturn(restLabel);
        ResponseEntity<RestLabel> response = service.createLabel(restLabel);
        assertThat(response.getStatusCodeValue(), is(201));
        assertThat(response.getBody(), sameInstance(restLabel));
        assertThat(response.getHeaders().getLocation(), notNullValue());
        verify(labelRepository).save(label);
    }


    @Test
    void createLabelParentNotExists() {
        when(restLabel.getParentLabelId()).thenReturn(PARENT_LABEL_ID);
        when(labelRepository.exists(PARENT_LABEL_ID)).thenReturn(false);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.createLabel(restLabel));
        assertThat(exception.getMessage(), is("404 NOT_FOUND \"label not found : parentLabelId\""));
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
    void updateLabelByIdSameId() {
        when(restLabel.getParentLabelId()).thenReturn(LABEL_ID);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.updateLabelById(LABEL_ID, restLabel));
        assertThat(exception.getMessage(), is("400 BAD_REQUEST \"labelId and parentLabelId are equal\""));
    }

    @Test
    void updateLabelByIdNotFound() {
        when(labelMapper.convertFromRestLabel(restLabel)).thenReturn(label);
        when(labelRepository.update(LABEL_ID, label)).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.updateLabelById(LABEL_ID, restLabel));
        assertThat(exception.getMessage(), is("404 NOT_FOUND \"label not found : labelId\""));
    }

    @Test
    void updateLabelByParentIdNotFound() {
        when(restLabel.getParentLabelId()).thenReturn(PARENT_LABEL_ID);
        when(labelRepository.exists(PARENT_LABEL_ID)).thenReturn(false);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.updateLabelById(LABEL_ID, restLabel));
        assertThat(exception.getMessage(), is("404 NOT_FOUND \"label not found : parentLabelId\""));
    }


    @Test
    void getRootNodes() {
        when(hierarchyRepository.getRootNodes(HierarchyMode.ALL, MAX_DEPTH)).thenReturn(List.of(treeNode));
        when(treeNodeMapper.convertToRestTreeNode(treeNode)).thenReturn(restTreeNode);
        ResponseEntity<List<RestTreeNode>> response = service.getRootNodes(RestHierarchyMode.ALL, MAX_DEPTH);
        assertThat(response.getBody(), contains(restTreeNode));
        assertThat(response.getStatusCodeValue(), is(200));
    }

    @Test
    void getSubTree() {
        when(hierarchyRepository.getSubTree(REFERENCE_ID, HierarchyMode.NONE, MAX_DEPTH)).thenReturn(Optional.of(treeNode));
        when(treeNodeMapper.convertToRestTreeNode(treeNode)).thenReturn(restTreeNode);
        ResponseEntity<RestTreeNode> response = service.getSubTree(REFERENCE_ID, RestHierarchyMode.NONE, MAX_DEPTH);
        assertThat(response.getBody(), sameInstance(restTreeNode));
        assertThat(response.getStatusCodeValue(), is(200));
    }

    @Test
    void getSubTreeNotFound() {
        when(hierarchyRepository.getSubTree(REFERENCE_ID, HierarchyMode.MAX_DEPTH, MAX_DEPTH)).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.getSubTree(REFERENCE_ID, RestHierarchyMode.MAX_DEPTH, MAX_DEPTH));
        assertThat(exception.getStatus().value(), is(404));
        assertThat(exception.getMessage(), is("404 NOT_FOUND \"subtree with referenceId: referenceId not found\""));
    }
}
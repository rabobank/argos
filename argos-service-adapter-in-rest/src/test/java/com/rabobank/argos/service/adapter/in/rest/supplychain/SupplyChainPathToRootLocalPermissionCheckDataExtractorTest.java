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
package com.rabobank.argos.service.adapter.in.rest.supplychain;

import com.rabobank.argos.domain.hierarchy.TreeNode;
import com.rabobank.argos.service.domain.hierarchy.HierarchyRepository;
import com.rabobank.argos.service.domain.security.LocalPermissionCheckData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupplyChainPathToRootLocalPermissionCheckDataExtractorTest {

    private static final String OBJECT_VALUE = "value";
    private static final String LABEL_ID = "labelId";
    @Mock
    private HierarchyRepository hierarchyRepository;

    @Mock
    private TreeNode treeNode;

    private SupplyChainPathToRootLocalPermissionCheckDataExtractor extractor;

    @Mock
    private Method method;

    @BeforeEach
    void setUp() {
        extractor = new SupplyChainPathToRootLocalPermissionCheckDataExtractor(hierarchyRepository);
    }

    @Test
    void extractLocalPermissionCheckData() {
        when(treeNode.getParentLabelId()).thenReturn(LABEL_ID);
        when(hierarchyRepository.findByNamePathToRootAndType(OBJECT_VALUE, List.of(OBJECT_VALUE), TreeNode.Type.SUPPLY_CHAIN)).thenReturn(Optional.of(treeNode));
        LocalPermissionCheckData checkData = extractor.extractLocalPermissionCheckData(method, new Object[]{OBJECT_VALUE, List.of(OBJECT_VALUE)});
        assertThat(checkData.getLabelIds(), contains(LABEL_ID));
    }
}
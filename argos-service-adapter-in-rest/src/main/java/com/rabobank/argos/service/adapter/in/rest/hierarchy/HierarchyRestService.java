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
import com.rabobank.argos.domain.permission.Permission;
import com.rabobank.argos.service.adapter.in.rest.api.handler.HierarchyApi;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestHierarchyMode;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestLabel;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestTreeNode;
import com.rabobank.argos.service.domain.hierarchy.HierarchyService;
import com.rabobank.argos.service.domain.hierarchy.LabelRepository;
import com.rabobank.argos.service.domain.security.LabelIdCheckParam;
import com.rabobank.argos.service.domain.security.PermissionCheck;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api")
public class HierarchyRestService implements HierarchyApi {

    private final LabelRepository labelRepository;

    private final LabelMapper labelMapper;

    private final HierarchyService hierarchyService;

    private final TreeNodeMapper treeNodeMapper;

    @Override
    @PermissionCheck(permissions = Permission.TREE_EDIT)
    public ResponseEntity<RestLabel> createLabel(@LabelIdCheckParam(propertyPath = "parentLabelId") RestLabel restLabel) {
        verifyParentLabelExists(restLabel.getParentLabelId());
        Label label = labelMapper.convertFromRestLabel(restLabel);
        labelRepository.save(label);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{labelId}")
                .buildAndExpand(label.getLabelId())
                .toUri();
        return ResponseEntity.created(location).body(labelMapper.convertToRestLabel(label));
    }

    private void verifyParentLabelExists(String parentLabelId) {
        Optional.ofNullable(parentLabelId)
                .filter(parentId -> !labelRepository.exists(parentId))
                .ifPresent(parentId -> {
            throw labelNotFound(parentId);
        });
    }

    @Override
    @PermissionCheck(permissions = Permission.TREE_EDIT)
    public ResponseEntity<Void> deleteLabelById(@LabelIdCheckParam String labelId) {
        if (labelRepository.deleteById(labelId)) {
            return ResponseEntity.noContent().build();
        } else {
            throw labelNotFound(labelId);
        }
    }

    @Override
    @PermissionCheck(permissions = Permission.READ)
    public ResponseEntity<RestLabel> getLabelById(@LabelIdCheckParam String labelId) {
        return labelRepository.findById(labelId).map(labelMapper::convertToRestLabel).map(ResponseEntity::ok)
                .orElseThrow(() -> labelNotFound(labelId));
    }


    @Override
    @PermissionCheck(permissions = Permission.TREE_EDIT)
    public ResponseEntity<RestLabel> updateLabelById(@LabelIdCheckParam String labelId, @LabelIdCheckParam(propertyPath = "parentLabelId") RestLabel restLabel) {
        verifyParentLabelIsDifferent(labelId, restLabel.getParentLabelId());
        verifyParentLabelExists(restLabel.getParentLabelId());
        Label label = labelMapper.convertFromRestLabel(restLabel);
        label.setLabelId(labelId);
        return labelRepository.update(labelId, label)
                .map(labelMapper::convertToRestLabel).map(ResponseEntity::ok)
                .orElseThrow(() -> labelNotFound(labelId));
    }

    @Override
    public ResponseEntity<List<RestTreeNode>> getRootNodes(RestHierarchyMode hierarchyMode, Integer maxDepth) {
        return ResponseEntity.ok(hierarchyService.getRootNodes(HierarchyMode.valueOf(hierarchyMode.name()), maxDepth).stream()
                .map(treeNodeMapper::convertToRestTreeNode).collect(Collectors.toList()));
    }

    @Override
    public ResponseEntity<RestTreeNode> getSubTree(String referenceId, RestHierarchyMode hierarchyMode, Integer maxDepth) {
        return hierarchyService.getSubTree(referenceId, HierarchyMode.valueOf(hierarchyMode.name()), maxDepth)
                .map(treeNodeMapper::convertToRestTreeNode).map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "subtree with referenceId: " + referenceId + " not found"));
    }

    private void verifyParentLabelIsDifferent(String labelId, String parentLabelId) {
        if (labelId.equals(parentLabelId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "labelId and parentLabelId are equal");
        }
    }

    private ResponseStatusException labelNotFound(String labelId) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "label not found : " + labelId);
    }
}

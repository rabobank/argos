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
import com.rabobank.argos.service.adapter.in.rest.api.handler.HierarchyApi;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestLabel;
import com.rabobank.argos.service.domain.hierarchy.LabelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api")
public class HierarchyRestService implements HierarchyApi {

    private final LabelRepository labelRepository;

    private final LabelMapper labelMapper;

    @Override
    public ResponseEntity<RestLabel> createLabel(RestLabel restLabel) {
        Label label = labelMapper.convertFromRestLabel(restLabel);
        labelRepository.save(label);
        return ResponseEntity.ok(labelMapper.convertToRestLabel(label));
    }

    @Override
    public ResponseEntity<Void> deleteLabelById(String labelId) {
        if (labelRepository.deleteById(labelId)) {
            return ResponseEntity.noContent().build();
        } else {
            throw labelNotFound(labelId);
        }
    }

    @Override
    public ResponseEntity<RestLabel> getLabelById(String labelId) {
        return labelRepository.findById(labelId).map(labelMapper::convertToRestLabel).map(ResponseEntity::ok)
                .orElseThrow(() -> labelNotFound(labelId));
    }

    @Override
    public ResponseEntity<RestLabel> updateLabelById(String labelId, RestLabel restLabel) {
        return labelRepository.update(labelId, labelMapper.convertFromRestLabel(restLabel))
                .map(labelMapper::convertToRestLabel).map(ResponseEntity::ok)
                .orElseThrow(() -> labelNotFound(labelId));
    }


    private ResponseStatusException labelNotFound(String labelId) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "label not found : " + labelId);
    }
}
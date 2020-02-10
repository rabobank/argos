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
package com.rabobank.argos.service.adapter.in.rest.layout;

import com.rabobank.argos.domain.layout.LayoutMetaBlock;
import com.rabobank.argos.service.adapter.in.rest.api.handler.LayoutApi;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestLayoutMetaBlock;
import com.rabobank.argos.service.domain.layout.LayoutMetaBlockRepository;
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
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api")
public class LayoutRestService implements LayoutApi {

    private final LayoutMetaBlockMapper converter;

    private final LayoutMetaBlockRepository repository;

    private final LayoutValidatorService validator;

    @Override
    public ResponseEntity<RestLayoutMetaBlock> createLayout(String supplyChainId, RestLayoutMetaBlock restLayoutMetaBlock) {
        log.info("createLayout for supplyChainId {}", supplyChainId);

        LayoutMetaBlock layoutMetaBlock = converter.convertFromRestLayoutMetaBlock(restLayoutMetaBlock);
        layoutMetaBlock.setSupplyChainId(supplyChainId);
        validator.validate(layoutMetaBlock);
        repository.save(layoutMetaBlock);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{layoutMetaBlockId}")
                .buildAndExpand(layoutMetaBlock.getLayoutMetaBlockId())
                .toUri();
        return ResponseEntity
                .created(location)
                .body(converter.convertToRestLayoutMetaBlock(layoutMetaBlock));
    }

    @Override
    public ResponseEntity<RestLayoutMetaBlock> updateLayout(String supplyChainId, String layoutId, RestLayoutMetaBlock restLayoutMetaBlock) {
        log.info("updateLayout for supplyChainId {}", supplyChainId);
        LayoutMetaBlock layoutMetaBlock = converter.convertFromRestLayoutMetaBlock(restLayoutMetaBlock);
        layoutMetaBlock.setSupplyChainId(supplyChainId);
        layoutMetaBlock.setLayoutMetaBlockId(layoutId);
        validator.validate(layoutMetaBlock);
        if (repository.update(supplyChainId, layoutId, layoutMetaBlock)) {
            return ResponseEntity.ok(converter.convertToRestLayoutMetaBlock(layoutMetaBlock));
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "layout not found");
        }
    }

    @Override
    public ResponseEntity<RestLayoutMetaBlock> getLayout(String supplyChainId, String layoutId) {
        return repository.findBySupplyChainAndId(supplyChainId, layoutId)
                .map(converter::convertToRestLayoutMetaBlock)
                .map(ResponseEntity::ok).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "layout not found"));
    }

    @Override
    public ResponseEntity<List<RestLayoutMetaBlock>> findLayout(String supplyChainId) {
        return ResponseEntity.ok(repository.findBySupplyChainId(supplyChainId).stream()
                .map(converter::convertToRestLayoutMetaBlock)
                .collect(Collectors.toList()));
    }
}

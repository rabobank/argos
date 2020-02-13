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
import com.rabobank.argos.domain.supplychain.SupplyChain;
import com.rabobank.argos.service.adapter.in.rest.api.handler.SupplychainApi;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestSupplyChain;
import com.rabobank.argos.service.domain.hierarchy.HierarchyRepository;
import com.rabobank.argos.service.domain.hierarchy.LabelRepository;
import com.rabobank.argos.service.domain.supplychain.SupplyChainRepository;
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

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api")
public class SupplyChainRestService implements SupplychainApi {

    private final SupplyChainRepository supplyChainRepository;
    private final HierarchyRepository hierarchyRepository;
    private final SupplyChainMapper converter;
    private final LabelRepository labelRepository;

    @Override
    public ResponseEntity<RestSupplyChain> createSupplyChain(RestSupplyChain restSupplyChain) {
        verifyParentLabelExists(restSupplyChain.getParentLabelId());
        SupplyChain supplyChain = converter.convertFromRestSupplyChainCommand(restSupplyChain);

        supplyChainRepository.save(supplyChain);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{supplyChainId}")
                .buildAndExpand(supplyChain.getSupplyChainId())
                .toUri();
        return ResponseEntity
                .created(location)
                .body(converter.convertToRestRestSupplyChainItem(supplyChain));
    }

    @Override
    public ResponseEntity<RestSupplyChain> getSupplyChain(String supplyChainId) {
        SupplyChain supplyChain = supplyChainRepository
                .findBySupplyChainId(supplyChainId)
                .orElseThrow(() -> supplyChainNotFound(supplyChainId));
        return ResponseEntity.ok(converter.convertToRestRestSupplyChainItem(supplyChain));
    }


    @Override
    public ResponseEntity<RestSupplyChain> getSupplyChainByPathToRoot(String supplyChainName, List<String> pathToRoot) {
        return hierarchyRepository.findByNamePathToRootAndType(supplyChainName, pathToRoot, TreeNode.Type.SUPPLY_CHAIN)
                .map(TreeNode::getReferenceId)
                .map(supplyChainRepository::findBySupplyChainId).filter(Optional::isPresent).map(Optional::get)
                .map(converter::convertToRestRestSupplyChainItem)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> supplyChainNotFound(supplyChainName, pathToRoot));
    }

    @Override
    public ResponseEntity<RestSupplyChain> updateSupplyChain(String supplyChainId, RestSupplyChain restSupplyChain) {
        verifyParentLabelExists(restSupplyChain.getParentLabelId());
        return supplyChainRepository.update(supplyChainId, converter.convertFromRestSupplyChainCommand(restSupplyChain))
                .map(converter::convertToRestRestSupplyChainItem)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> supplyChainNotFound(supplyChainId));
    }

    private void verifyParentLabelExists(String parentLabelId) {
        if(!labelRepository.exists(parentLabelId)) {
            throw parentLabelNotFound(parentLabelId);
        }
    }

    private ResponseStatusException parentLabelNotFound(String labelId) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "parent label not found : " + labelId);
    }

    private ResponseStatusException supplyChainNotFound(String supplyChainId) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "supply chain not found : " + supplyChainId);
    }

    private ResponseStatusException supplyChainNotFound(String supplyChainName, List<String> pathToRoot) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "supply chain not found : " + supplyChainName + " with path to root " + String.join(",", pathToRoot));
    }

}

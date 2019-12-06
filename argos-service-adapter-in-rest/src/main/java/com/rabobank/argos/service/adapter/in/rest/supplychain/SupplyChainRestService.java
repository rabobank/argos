/*
 * Copyright (C) 2019 Rabobank Nederland
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

import com.rabobank.argos.domain.supplychain.SupplyChain;
import com.rabobank.argos.service.adapter.in.rest.api.handler.SupplychainApi;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestCreateSupplyChainCommand;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestSupplyChainItem;
import com.rabobank.argos.service.domain.supplychain.SupplyChainRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
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
public class SupplyChainRestService implements SupplychainApi {

    private final SupplyChainRepository supplyChainRepository;
    private final SupplyChainMapper converter;

    @Override
    public ResponseEntity<RestSupplyChainItem> createSupplyChain(RestCreateSupplyChainCommand restCreateSupplyChainCommand) {
        validateIsUnique(restCreateSupplyChainCommand);
        SupplyChain supplyChain = converter
                .convertFromRestSupplyChainCommand(restCreateSupplyChainCommand);

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
    public ResponseEntity<RestSupplyChainItem> getSupplyChain(String supplyChainId) {
        SupplyChain supplyChain = supplyChainRepository
                .findBySupplyChainId(supplyChainId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "supply chain not found : " + supplyChainId));
        return ResponseEntity.ok(converter.convertToRestRestSupplyChainItem(supplyChain));
    }

    @Override
    public ResponseEntity<List<RestSupplyChainItem>> searchSupplyChains(String name) {
        List<SupplyChain> supplyChains;
        if (StringUtils.isEmpty(name)) {
            supplyChains = supplyChainRepository.findAll();
        } else {
            supplyChains = supplyChainRepository.findByName(name);
        }
        List<RestSupplyChainItem> supplyChainItems = supplyChains
                .stream()
                .map(converter::convertToRestRestSupplyChainItem)
                .collect(Collectors.toList());
        return ResponseEntity.ok(supplyChainItems);
    }

    private void validateIsUnique(RestCreateSupplyChainCommand restCreateSupplyChainCommand) {
        if (!supplyChainRepository.findByName(restCreateSupplyChainCommand.getName()).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "supply chain name must be unique");
        }
    }
}

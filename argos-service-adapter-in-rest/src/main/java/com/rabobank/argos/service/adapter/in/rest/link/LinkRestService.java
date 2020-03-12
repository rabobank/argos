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
package com.rabobank.argos.service.adapter.in.rest.link;


import com.rabobank.argos.domain.link.LinkMetaBlock;
import com.rabobank.argos.domain.permission.Permission;
import com.rabobank.argos.service.adapter.in.rest.SignatureValidatorService;
import com.rabobank.argos.service.adapter.in.rest.api.handler.LinkApi;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestLinkMetaBlock;
import com.rabobank.argos.service.domain.link.LinkMetaBlockRepository;
import com.rabobank.argos.service.domain.security.LabelIdCheckParam;
import com.rabobank.argos.service.domain.security.PermissionCheck;
import com.rabobank.argos.service.domain.supplychain.SupplyChainRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static com.rabobank.argos.service.adapter.in.rest.supplychain.SupplyChainLabelIdExtractor.SUPPLY_CHAIN_LABEL_ID_EXTRACTOR;
import static java.util.stream.Collectors.toList;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api")
public class LinkRestService implements LinkApi {

    private final LinkMetaBlockRepository linkMetaBlockRepository;

    private final SupplyChainRepository supplyChainRepository;

    private final LinkMetaBlockMapper converter;

    private final SignatureValidatorService signatureValidatorService;

    @Override
    @PermissionCheck(permissions = Permission.LINK_ADD)
    public ResponseEntity<Void> createLink(@LabelIdCheckParam(dataExtractor = SUPPLY_CHAIN_LABEL_ID_EXTRACTOR) String supplyChainId, RestLinkMetaBlock restLinkMetaBlock) {
        log.info("createLink supplyChainId : {}", supplyChainId);
        if (supplyChainRepository.findBySupplyChainId(supplyChainId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "supply chain not found : " + supplyChainId);
        }

        LinkMetaBlock linkMetaBlock = converter.convertFromRestLinkMetaBlock(restLinkMetaBlock);
        signatureValidatorService.validateSignature(linkMetaBlock.getLink(), linkMetaBlock.getSignature());
        linkMetaBlock.setSupplyChainId(supplyChainId);
        linkMetaBlockRepository.save(linkMetaBlock);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    @PermissionCheck(permissions = Permission.READ)
    public ResponseEntity<List<RestLinkMetaBlock>> findLink(@LabelIdCheckParam(dataExtractor = SUPPLY_CHAIN_LABEL_ID_EXTRACTOR) String supplyChainId, String optionalHash) {
        if (supplyChainRepository.findBySupplyChainId(supplyChainId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "supply chain not found : " + supplyChainId);
        }

        return new ResponseEntity<>(Optional.ofNullable(optionalHash).map(hash -> linkMetaBlockRepository.findBySupplyChainAndSha(supplyChainId, hash))
                .orElseGet(() -> linkMetaBlockRepository.findBySupplyChainId(supplyChainId))
                .stream().map(converter::convertToRestLinkMetaBlock).collect(toList()), HttpStatus.OK);
    }

}

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
package com.rabobank.argos.service.adapter.in.rest.layout;

import com.rabobank.argos.domain.Signature;
import com.rabobank.argos.domain.layout.Layout;
import com.rabobank.argos.domain.layout.LayoutMetaBlock;
import com.rabobank.argos.service.adapter.in.rest.SignatureValidatorService;
import com.rabobank.argos.service.domain.key.KeyPairRepository;
import com.rabobank.argos.service.domain.supplychain.SupplyChainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LayoutValidatorService {

    private final SupplyChainRepository supplyChainRepository;

    private final SignatureValidatorService signatureValidatorService;

    private final KeyPairRepository keyPairRepository;

    public void validate(LayoutMetaBlock layoutMetaBlock) {
        validateSupplyChain(layoutMetaBlock);
        validateAutorizationKeyIds(layoutMetaBlock.getLayout());
        validateSignatures(layoutMetaBlock);
    }

    private void validateSupplyChain(LayoutMetaBlock layoutMetaBlock) {
        if (!supplyChainRepository.exists(layoutMetaBlock.getSupplyChainId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "supply chain not found : " + layoutMetaBlock.getSupplyChainId());
        }
    }

    private void validateSignatures(LayoutMetaBlock layoutMetaBlock) {
        Set<String> uniqueKeyIds = layoutMetaBlock.getSignatures().stream().map(Signature::getKeyId).collect(Collectors.toSet());
        if (layoutMetaBlock.getSignatures().size() != uniqueKeyIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "layout can't be signed more than one time with the same keyId");
        }

        layoutMetaBlock.getSignatures().forEach(signature -> signatureValidatorService.validateSignature(layoutMetaBlock.getLayout(), signature));
    }

    private void validateAutorizationKeyIds(Layout layout) {
        layout.getAuthorizedKeyIds().forEach(this::keyExists);
        layout.getSteps().forEach(step -> step.getAuthorizedKeyIds().forEach(this::keyExists));
    }

    private void keyExists(String keyId) {
        if (!keyPairRepository.exists(keyId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "keyId " + keyId + " not found");
        }
    }
}

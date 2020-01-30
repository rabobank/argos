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

import com.rabobank.argos.domain.Signature;
import com.rabobank.argos.domain.layout.Layout;
import com.rabobank.argos.domain.layout.LayoutMetaBlock;
import com.rabobank.argos.domain.layout.LayoutSegment;
import com.rabobank.argos.domain.layout.MatchFilter;
import com.rabobank.argos.domain.layout.Step;
import com.rabobank.argos.service.adapter.in.rest.SignatureValidatorService;
import com.rabobank.argos.service.domain.key.KeyPairRepository;
import com.rabobank.argos.service.domain.supplychain.SupplyChainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Service
@RequiredArgsConstructor
public class LayoutValidatorService {

    private final SupplyChainRepository supplyChainRepository;

    private final SignatureValidatorService signatureValidatorService;

    private final KeyPairRepository keyPairRepository;

    public void validate(LayoutMetaBlock layoutMetaBlock) {
        validateSegmentNamesUnique(layoutMetaBlock.getLayout());
        validateStepNamesUnique(layoutMetaBlock.getLayout());
        validateMatchFilterDestinations(layoutMetaBlock.getLayout());
        validateSupplyChain(layoutMetaBlock);
        validateAutorizationKeyIds(layoutMetaBlock.getLayout());
        validateSignatures(layoutMetaBlock);
    }

    private void validateStepNamesUnique(Layout layout) {
        layout.getLayoutSegments().forEach(this::validateStepNamesUnique);
    }

    private void validateStepNamesUnique(LayoutSegment layoutSegment) {
        Set<String> stepNameSet = layoutSegment.getSteps().stream().map(Step::getStepName).collect(toSet());
        List<String> stepNameList = layoutSegment.getSteps().stream().map(Step::getStepName).collect(toList());
        if (stepNameSet.size() != stepNameList.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "step names are not unique");
        }
    }

    private void validateSegmentNamesUnique(Layout layout) {
        Set<String> segmentNameSet = layout.getLayoutSegments().stream().map(LayoutSegment::getName).collect(toSet());
        List<String> segmentNameList = layout.getLayoutSegments().stream().map(LayoutSegment::getName).collect(toList());
        if (segmentNameSet.size() != segmentNameList.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "segment names are not unique");
        }
    }

    private void validateMatchFilterDestinations(Layout layout) {
        if (!layout.getExpectedEndProducts().stream().allMatch(matchFilter -> hasFilterDestination(matchFilter, layout))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "expected product destination step name not found");
        }
    }

    private boolean hasFilterDestination(MatchFilter matchFilter, Layout layout) {
        return layout.getLayoutSegments().stream()
                .filter(layoutSegment -> layoutSegment.getName().equals(matchFilter.getDestinationSegmentName()))
                .map(LayoutSegment::getSteps)
                .anyMatch(steps -> hasDestinationStepName(steps, matchFilter.getDestinationStepName()));
    }

    private boolean hasDestinationStepName(List<Step> steps, String destinationStepName) {
        return steps.stream().anyMatch(step -> step.getStepName().equals(destinationStepName));
    }

    private void validateSupplyChain(LayoutMetaBlock layoutMetaBlock) {
        if (!supplyChainRepository.exists(layoutMetaBlock.getSupplyChainId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "supply chain not found : " + layoutMetaBlock.getSupplyChainId());
        }
    }

    private void validateSignatures(LayoutMetaBlock layoutMetaBlock) {
        Set<String> uniqueKeyIds = layoutMetaBlock.getSignatures().stream().map(Signature::getKeyId).collect(toSet());
        if (layoutMetaBlock.getSignatures().size() != uniqueKeyIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "layout can't be signed more than one time with the same keyId");
        }

        layoutMetaBlock.getSignatures().forEach(signature -> signatureValidatorService.validateSignature(layoutMetaBlock.getLayout(), signature));
    }

    private void validateAutorizationKeyIds(Layout layout) {
        layout.getAuthorizedKeyIds().forEach(this::keyExists);
        layout.getLayoutSegments().forEach(layoutSegment -> layoutSegment.getSteps().forEach(step -> step.getAuthorizedKeyIds().forEach(this::keyExists)));
    }

    private void keyExists(String keyId) {
        if (!keyPairRepository.exists(keyId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "keyId " + keyId + " not found");
        }
    }
}

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
package com.rabobank.argos.service.domain.verification;

import com.rabobank.argos.domain.layout.DestinationType;
import com.rabobank.argos.domain.layout.LayoutMetaBlock;
import com.rabobank.argos.domain.layout.LayoutSegment;
import com.rabobank.argos.domain.layout.MatchFilter;
import com.rabobank.argos.domain.link.Artifact;
import com.rabobank.argos.domain.link.LinkMetaBlock;
import com.rabobank.argos.service.domain.link.LinkMetaBlockRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Singular;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;

@Component
@RequiredArgsConstructor
@Slf4j
public class NewVerificationContextsPovider {

    private final LinkMetaBlockRepository linkMetaBlockRepository;

    public List<VerificationContext> createPossibleVerificationContexts(LayoutMetaBlock layoutMetaBlock, List<Artifact> productsToVerify) {
        ResolvedSegmentsWithLinkSets resolvedSegmentsWithLinkSets = processMatchFilters(layoutMetaBlock.getLayout().getExpectedEndProducts(), productsToVerify);
        return emptyList();
    }

    private ResolvedSegmentsWithLinkSets processMatchFilters(List<MatchFilter> matchFilters, List<Artifact> endProducts) {
        Map<String, EnumMap<DestinationType, List<Artifact>>> filteredArtifacts = filter(matchFilters, endProducts);

        return getLinks(matchFilters.iterator().next().getDestinationSegmentName(), filteredArtifacts, new ArrayList<>(), new HashSet<>());
    }

    private ResolvedSegmentsWithLinkSets getLinks(String destinationSegmentName, Map<String, EnumMap<DestinationType, List<Artifact>>> filteredArtifacts, ArrayList<LayoutSegment> resolvedSegments, Set<Set<LinkMetaBlock>> linkSets) {
        List<String> resolvedSteps = new ArrayList<>();
        filteredArtifacts
                .entrySet()
                .forEach(step -> {
                    linkSets.addAll(query(destinationSegmentName, step.getKey(), step.getValue(), resolvedSteps));
                    resolvedSteps.add(step.getKey());

                });

        return ResolvedSegmentsWithLinkSets
                .builder()
                .linkSets(linkSets)
                .resolvedSegment(destinationSegmentName)
                .build();
    }

    private Set<Set<LinkMetaBlock>> query(String destinationSegmentName, String destinationStepName, EnumMap<DestinationType, List<Artifact>> filteredArtifacts, List<String> resolvedSteps) {

        // query db here based on DestinationType linkMetaBlockRepository.findBySupplyChainAndSegmentNameAndStepNameAndProductHashes()
        return emptySet();
    }

    private Map<String, EnumMap<DestinationType, List<Artifact>>> filter(List<MatchFilter> matchFilters, List<Artifact> endProducts) {
        return emptyMap();
    }

    @Data
    @Builder
    private class ResolvedSegmentsWithLinkSets {
        @Singular
        List<String> resolvedSegments;
        Set<Set<LinkMetaBlock>> linkSets;
    }
}

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
import com.rabobank.argos.domain.layout.MatchFilter;
import com.rabobank.argos.domain.link.Artifact;
import com.rabobank.argos.domain.link.LinkMetaBlock;
import com.rabobank.argos.service.domain.link.LinkMetaBlockRepository;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
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
import java.util.stream.Collectors;

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

        GetLinkParameters getLinkParameters = GetLinkParameters
                .builder()
                .destinationSegmentName(matchFilters.iterator().next().getDestinationSegmentName())
                .linkSets(new HashSet<>())
                .filteredArtifacts(filteredArtifacts)
                .resolvedSegments(new ArrayList<>())
                .build();

        return getLinks(getLinkParameters);
    }

    private ResolvedSegmentsWithLinkSets getLinks(GetLinkParameters getLinkParameters) {

        List<String> resolvedSteps = new ArrayList<>();
        getLinkParameters.getFilteredArtifacts()
                .entrySet()
                .forEach(step -> {
                    getLinkParameters.getLinkSets().add(queryByArtifacts(getLinkParameters.getDestinationSegmentName(), step.getKey(), step.getValue()));
                    resolvedSteps.add(step.getKey());
                });

        getLinkParameters.getResolvedSegments().add(getLinkParameters.getDestinationSegmentName());
        Set<Set<LinkMetaBlock>> resolvedLinkSets = new HashSet<>(getLinkParameters.getLinkSets());

        Set<Set<LinkMetaBlock>> newLinkSetsByRunId = new HashSet<>(getLinkParameters.getLinkSets());
        Set<String> runIds = findRunIds(getLinkParameters.getLinkSets());
        runIds.forEach(runId ->
                newLinkSetsByRunId.add(queryByRunId(runId,
                        getLinkParameters.getDestinationSegmentName(),
                        resolvedSteps))
        );
        resolvedLinkSets = permutate(newLinkSetsByRunId, resolvedLinkSets);
        return ResolvedSegmentsWithLinkSets
                .builder()
                .linkSets(resolvedLinkSets)
                .resolvedSegments(getLinkParameters.getResolvedSegments())
                .build();
    }

    private Set<Set<LinkMetaBlock>> permutate(Set<Set<LinkMetaBlock>> newLinkSetsByRunId, Set<Set<LinkMetaBlock>> resolvedLinkSets) {
        return emptySet();
    }

    private Set<String> findRunIds(Set<Set<LinkMetaBlock>> linkSets) {
        return linkSets
                .stream()
                .flatMap(linkSet -> linkSet
                        .stream()
                        .map(linkMetaBlock -> linkMetaBlock.getLink().getRunId()))
                .collect(Collectors.toSet());
    }

    private Set<LinkMetaBlock> queryByArtifacts(String destinationSegmentName, String destinationStepName, EnumMap<DestinationType, List<Artifact>> filteredArtifacts) {

        // query db here based on DestinationType linkMetaBlockRepository.findBySupplyChainAndSegmentNameAndStepNameAndProductHashes()
        return emptySet();
    }

    private Set<LinkMetaBlock> queryByRunId(String runId, String destinationSegmentName, List<String> resolvedSteps) {

        // query db here based on DestinationType linkMetaBlockRepository.findBySupplyChainAndSegmentNameAndStepNameAndProductHashes()
        return emptySet();
    }
    private Map<String, EnumMap<DestinationType, List<Artifact>>> filter(List<MatchFilter> matchFilters, List<Artifact> endProducts) {
        return emptyMap();
    }

    @Data
    @Builder
    private static class ResolvedSegmentsWithLinkSets {
        @Singular
        private List<String> resolvedSegments;

        private Set<Set<LinkMetaBlock>> linkSets;
    }

    @Builder
    @Getter
    private static class GetLinkParameters {
        private final String destinationSegmentName;
        private final Map<String, EnumMap<DestinationType, List<Artifact>>> filteredArtifacts;
        private final List<String> resolvedSegments;
        private final Set<Set<LinkMetaBlock>> linkSets;

        private GetLinkParameters(String destinationSegmentName, Map<String, EnumMap<DestinationType, List<Artifact>>> filteredArtifacts, List<String> resolvedSegments, Set<Set<LinkMetaBlock>> linkSets) {
            this.destinationSegmentName = destinationSegmentName;
            this.filteredArtifacts = filteredArtifacts;
            this.resolvedSegments = resolvedSegments;
            this.linkSets = linkSets;
        }

    }
}

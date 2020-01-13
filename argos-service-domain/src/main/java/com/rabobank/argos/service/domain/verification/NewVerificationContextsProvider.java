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

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.groupingBy;

@Component
@RequiredArgsConstructor
@Slf4j
public class NewVerificationContextsProvider {

    private final LinkMetaBlockRepository linkMetaBlockRepository;

    public List<VerificationContext> createPossibleVerificationContexts(LayoutMetaBlock layoutMetaBlock, List<Artifact> productsToVerify) {

        ResolvedSegmentsWithLinkSets resolvedSegmentsWithLinkSets = processMatchFilters(layoutMetaBlock.getLayout().getExpectedEndProducts(), productsToVerify);
        //processMatchRules(resolvedSegmentsWithLinkSets);

        return resolvedSegmentsWithLinkSets
                .getLinkSets()
                .stream()
                .map(linkSet -> VerificationContext
                        .builder()
                        .layoutMetaBlock(layoutMetaBlock)
                        .linkMetaBlocks(new ArrayList<>(linkSet)).build())
                .collect(Collectors.toList());

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
        Set<LinkMetaBlock> links = new HashSet<>();
        getLinkParameters.getFilteredArtifacts()
                .forEach((key, value) -> {
                    links.addAll(queryByArtifacts(getLinkParameters.getDestinationSegmentName(), key, value));
                    resolvedSteps.add(key);
                });

        getLinkParameters.getResolvedSegments().add(getLinkParameters.getDestinationSegmentName());
        Set<Set<LinkMetaBlock>> resolvedLinkSets = new HashSet<>(getLinkParameters.getLinkSets());
        Set<LinkMetaBlock> newLinkSetByRunId = new HashSet<>(links);
        Set<String> runIds = findRunIds(links);
        runIds.forEach(runId ->
                newLinkSetByRunId.addAll(queryByRunId(runId,
                        getLinkParameters.getDestinationSegmentName(),
                        resolvedSteps))
        );
        resolvedLinkSets = permutate(newLinkSetByRunId, resolvedLinkSets);

        return ResolvedSegmentsWithLinkSets
                .builder()
                .linkSets(resolvedLinkSets)
                .resolvedSegments(getLinkParameters.getResolvedSegments())
                .build();
    }

    private Set<Set<LinkMetaBlock>> permutate(Set<LinkMetaBlock> newLinkSet, Set<Set<LinkMetaBlock>> resolvedLinkSets) {
        Set<Set<LinkMetaBlock>> temporaryLinkSet = new HashSet<>();
        Map<Integer, List<LinkMetaBlock>> tempEqualLinkSets = newLinkSet.stream()
                .collect(groupingBy(linkMetaBlock -> linkMetaBlock.getLink().hashCode()));

        resolvedLinkSets.forEach(linkSet ->
                tempEqualLinkSets.forEach((key, value) -> {
                    Set<Set<LinkMetaBlock>> clonedResolvedLinkSets = new HashSet<>(resolvedLinkSets);
                    clonedResolvedLinkSets.add(new HashSet<>(value));
                    temporaryLinkSet.addAll(clonedResolvedLinkSets);
                }));

        return temporaryLinkSet;
    }

    private Set<String> findRunIds(Set<LinkMetaBlock> linkSets) {
        return linkSets
                .stream()
                .map(linkSet -> linkSet.getLink().getRunId())
                .collect(Collectors.toSet());
    }

    private Set<LinkMetaBlock> queryByArtifacts(String destinationSegmentName, String destinationStepName, EnumMap<DestinationType, List<Artifact>> filteredArtifacts) {

        // query db here based on DestinationType linkMetaBlockRepository.findBySupplyChainAndSegmentNameAndStepNameAndProductHashes()
        return emptySet();
    }

    private Set<LinkMetaBlock> queryByRunId(String runId, String destinationSegmentName, List<String> resolvedSteps) {

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
    }
}

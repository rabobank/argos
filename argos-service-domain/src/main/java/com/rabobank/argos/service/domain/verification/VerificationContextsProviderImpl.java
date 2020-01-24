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
import com.rabobank.argos.domain.layout.rule.MatchRule;
import com.rabobank.argos.domain.link.Artifact;
import com.rabobank.argos.domain.link.LinkMetaBlock;
import com.rabobank.argos.service.domain.link.LinkMetaBlockRepository;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.rabobank.argos.domain.layout.DestinationType.MATERIALS;
import static com.rabobank.argos.domain.layout.DestinationType.PRODUCTS;
import static com.rabobank.argos.service.domain.verification.ArtifactMatcher.matches;
import static com.rabobank.argos.service.domain.verification.rules.RuleVerificationContext.filterArtifacts;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.groupingBy;

@Component
@RequiredArgsConstructor
@Slf4j
public class VerificationContextsProviderImpl implements VerificationContextsProvider {

    private final LinkMetaBlockRepository linkMetaBlockRepository;

    public List<VerificationContext> createPossibleVerificationContexts(LayoutMetaBlock layoutMetaBlock, List<Artifact> productsToVerify) {

        ResolvedSegmentsWithLinkSets resolvedSegmentsWithLinkSets = processMatchFilters(layoutMetaBlock, productsToVerify);
        log.info("processMatchFilters resulted in: {} possible verificationContexts", resolvedSegmentsWithLinkSets.linkSets.size());
        if (!layoutMetaBlock.allLayoutSegmentsAreResolved(resolvedSegmentsWithLinkSets.getResolvedSegments())) {
            processMatchRules(layoutMetaBlock, resolvedSegmentsWithLinkSets);
        }
        log.info("processMatchRules resulted in: {} possible verificationContexts", resolvedSegmentsWithLinkSets.linkSets.size());
        return resolvedSegmentsWithLinkSets
                .getLinkSets()
                .stream()
                .map(linkSet -> VerificationContext
                        .builder()
                        .layoutMetaBlock(layoutMetaBlock)
                        .linkMetaBlocks(new ArrayList<>(linkSet)).build())
                .collect(Collectors.toList());
    }

    private void processMatchRules(LayoutMetaBlock layoutMetaBlock, ResolvedSegmentsWithLinkSets resolvedSegmentsWithLinkSets) {

        /*  the matchrules are filtered for resolved source segments and for UNresolved destination segments
            the idea is to get artifacts hashes from links of already resolved segments and then query for
            links with these artifacts in the destination segment after the query the resolvedSegmentsWithLinkSets is updated
            so the list of matchrules shrinks
        */

        Map<String, Map<String, Map<DestinationType, List<MatchRuleWithSourceType>>>> matchRulesGrouped =
                createMatchRulesBySegmentNameStepNameAndDestinationType(layoutMetaBlock, resolvedSegmentsWithLinkSets);

        if (!matchRulesGrouped.isEmpty()) {
            Map<String, Map<DestinationType, List<MatchRuleWithSourceType>>> firstMatchRule = matchRulesGrouped.values().iterator().next();
            Set<Set<LinkMetaBlock>> sourceLinkSets = getPossibleLinkSetsForSourceSegment(resolvedSegmentsWithLinkSets, firstMatchRule);
            resolveArtifactsForAllPossibleLinkSets(firstMatchRule, sourceLinkSets);
            /*
              search for link files with the SOURCE artifacts gathered for each linkSet
              in the  destination segment specified in the matchrule
             */
            getLinksForMatchRules(layoutMetaBlock, resolvedSegmentsWithLinkSets, firstMatchRule);
            processMatchRules(layoutMetaBlock, resolvedSegmentsWithLinkSets);
        }
    }

    private void resolveArtifactsForAllPossibleLinkSets(Map<String, Map<DestinationType, List<MatchRuleWithSourceType>>> firstMatchRule, Set<Set<LinkMetaBlock>> sourceLinkSets) {
        for (Set<LinkMetaBlock> linkSet : sourceLinkSets) {
            firstMatchRule
                    .forEach((stepName, byDestinationType) -> byDestinationType
                            .forEach((destinationType, matchRuleWithSourceTypes) -> matchRuleWithSourceTypes
                                    //add SOURCE artifacts to MatchRuleWithSourceType for each linkSet
                                    .forEach(matchRuleWithSourceType ->
                                            matchRuleWithSourceType.resolveArtifacts(linkSet)
                                    )));

        }
    }

    private Set<Set<LinkMetaBlock>> getPossibleLinkSetsForSourceSegment(ResolvedSegmentsWithLinkSets resolvedSegmentsWithLinkSets, Map<String, Map<DestinationType, List<MatchRuleWithSourceType>>> firstMatchRule) {
        Set<Set<LinkMetaBlock>> sourceLinkSets = new HashSet<>();
        firstMatchRule
                .values()
                .forEach(destinationTypeListMap -> destinationTypeListMap
                        .values()
                        .forEach(matchRuleWithSourceTypes -> matchRuleWithSourceTypes
                                .forEach(matchRule -> sourceLinkSets
                                        .addAll(resolvedSegmentsWithLinkSets.getResolvedLinkSetsBySegmentName(matchRule.getSourceSegmentName())))));
        return sourceLinkSets;
    }

    private void getLinksForMatchRules(LayoutMetaBlock layoutMetaBlock, ResolvedSegmentsWithLinkSets resolvedSegmentsWithLinkSets, Map<String, Map<DestinationType, List<MatchRuleWithSourceType>>> firstMatchRule) {
        Map<String, Map<DestinationType, List<List<Artifact>>>> filteredArtifacts = new HashMap<>();
        AtomicReference<String> destinationSegmentName = new AtomicReference<>();
        firstMatchRule
                .values()
                .forEach(destinationTypeListMap ->
                        destinationSegmentName.set(destinationTypeListMap.
                                values().iterator()
                                .next()
                                .iterator()
                                .next()
                                .getMatchRule()
                                .getDestinationSegmentName())
                );
        firstMatchRule.forEach((stepName, destinationTypeMap) -> {
            filteredArtifacts.put(stepName, new HashMap<>());
            destinationTypeMap
                    .forEach((destinationType, matchRuleWithSourceTypes) -> matchRuleWithSourceTypes
                            .forEach(matchRuleWithSourceType -> filteredArtifacts.get(stepName).put(destinationType, matchRuleWithSourceType.getResolvedArtifacts())));
        });
        log.info("getting linksets from matchRule for destination segment: {}", destinationSegmentName.get());
        List<String> resolvedSteps = new ArrayList<>();
        Set<LinkMetaBlock> links = new HashSet<>();
        filteredArtifacts
                .forEach((stepName, destinationTypes) -> {
                    destinationTypes.forEach((destinationType, artifactLists) -> artifactLists
                            .forEach(artifacts ->
                                    links.addAll(queryByArtifacts(layoutMetaBlock.getSupplyChainId(),
                                            destinationSegmentName.get(),
                                            stepName,
                                            destinationType,
                                            artifacts)

                                    )));
                    resolvedSteps.add(stepName);
                });


        Set<Set<LinkMetaBlock>> resolvedLinkSets = new HashSet<>(resolvedSegmentsWithLinkSets.getLinkSets());

        Set<LinkMetaBlock> newLinkSetByRunId = getLinksForRemainingStepsByRunId(layoutMetaBlock.getSupplyChainId(), destinationSegmentName.get(), resolvedSteps, links);

        Set<Set<LinkMetaBlock>> updatedResolvedLinkSets = permutate(newLinkSetByRunId, resolvedLinkSets, resolvedSegmentsWithLinkSets, destinationSegmentName.get());

        resolvedSegmentsWithLinkSets.setLinkSets(updatedResolvedLinkSets);
        resolvedSegmentsWithLinkSets.addResolvedSegment(destinationSegmentName.get());
    }

    private Map<String, Map<String, Map<DestinationType, List<MatchRuleWithSourceType>>>> createMatchRulesBySegmentNameStepNameAndDestinationType(LayoutMetaBlock layoutMetaBlock, ResolvedSegmentsWithLinkSets resolvedSegmentsWithLinkSets) {
        List<MatchRuleWithSourceType> matchRules = productMatchRulesForUnResolvedSegments(layoutMetaBlock, resolvedSegmentsWithLinkSets.getResolvedSegments());
        List<MatchRuleWithSourceType> matchRulesForMaterials = materialMatchRulesForUnResolvedSegments(layoutMetaBlock, resolvedSegmentsWithLinkSets.getResolvedSegments());
        matchRules.addAll(matchRulesForMaterials);
        return matchRules.stream()
                .collect(groupingBy(m -> m.getMatchRule().getDestinationSegmentName(),
                        groupingBy(m -> m.getMatchRule().getDestinationStepName(),
                                groupingBy(m -> m.getMatchRule().getDestinationType()))));
    }

    private List<MatchRuleWithSourceType> productMatchRulesForUnResolvedSegments(LayoutMetaBlock layoutMetaBlock, Set<String> resolvedSegments) {
        List<MatchRuleWithSourceType> rules4Products = new ArrayList<>();
        layoutMetaBlock.getLayout().getLayoutSegments()
                .stream()
                .filter(layoutSegment -> resolvedSegments.contains(layoutSegment.getName()))
                .forEach(
                        segment -> segment.getSteps()
                                .forEach(step -> rules4Products.addAll(
                                        step
                                                .getExpectedProducts()
                                                .stream()
                                                .filter(rule -> rule instanceof MatchRule)
                                                .map(rule -> ((MatchRule) rule))
                                                .map(rule -> MatchRuleWithSourceType.builder()
                                                        .matchRule(rule)
                                                        .sourceType(PRODUCTS)
                                                        .sourceSegmentName(segment.getName())
                                                        .sourceStepName(step.getStepName())
                                                        .build())
                                                .collect(Collectors.toList()))
                                )
                );
        //resolvedSegments filtered by destination segment not resolved
        return rules4Products.stream()
                .filter(m -> !resolvedSegments.contains(m.getMatchRule().getDestinationSegmentName()))
                .collect(Collectors.toList());
    }

    private List<MatchRuleWithSourceType> materialMatchRulesForUnResolvedSegments(LayoutMetaBlock layoutMetaBlock, Set<String> resolvedSegments) {
        List<MatchRuleWithSourceType> rules4Materials = new ArrayList<>();
        layoutMetaBlock.getLayout().getLayoutSegments()
                .stream()
                .filter(layoutSegment -> resolvedSegments.contains(layoutSegment.getName()))
                .forEach(
                        segment -> segment.getSteps()
                                .forEach(step -> rules4Materials.addAll(
                                        step
                                                .getExpectedMaterials()
                                                .stream()
                                                .filter(rule -> rule instanceof MatchRule)
                                                .map(rule -> ((MatchRule) rule))
                                                .map(rule -> MatchRuleWithSourceType.builder()
                                                        .matchRule(rule)
                                                        .sourceType(MATERIALS)
                                                        .sourceSegmentName(segment.getName())
                                                        .sourceStepName(step.getStepName())
                                                        .build())
                                                .collect(Collectors.toList()))
                                )
                );
        //resolvedSegments filtered by destination segment not resolved
        return rules4Materials.stream()
                .filter(m -> !resolvedSegments.contains(m.getMatchRule().getDestinationSegmentName()))
                .collect(Collectors.toList());
    }

    private ResolvedSegmentsWithLinkSets processMatchFilters(LayoutMetaBlock layoutMetaBlock, List<Artifact> endProducts) {

        Map<String, Map<DestinationType, List<Artifact>>> filteredArtifacts = filterEndproductsByStepAndDestinationType(layoutMetaBlock.expectedEndProducts(), endProducts);
        String destinationSegmentName = layoutMetaBlock.expectedEndProducts().iterator().next().getDestinationSegmentName();
        GetLinkParameters getLinkParameters = GetLinkParameters
                .builder()
                .layoutMetaBlock(layoutMetaBlock)
                .linkSets(new HashSet<>())
                .filteredArtifacts(filteredArtifacts)
                .resolvedSegments(new HashSet<>())
                .destinationSegmentName(destinationSegmentName)
                .build();
        ResolvedSegmentsWithLinkSets resolvedSegmentsWithLinkSets = new ResolvedSegmentsWithLinkSets();
        resolvedSegmentsWithLinkSets.setLinkSets(getLinks(getLinkParameters, resolvedSegmentsWithLinkSets));
        resolvedSegmentsWithLinkSets.addResolvedSegment(destinationSegmentName);
        return resolvedSegmentsWithLinkSets;
    }


    private Set<Set<LinkMetaBlock>> getLinks(GetLinkParameters getLinkParameters, ResolvedSegmentsWithLinkSets resolvedSegmentsWithLinkSets) {

        List<String> resolvedSteps = new ArrayList<>();
        Set<LinkMetaBlock> links = new HashSet<>();
        getLinkParameters
                .getFilteredArtifacts()
                .forEach((stepName, destinationTypes) -> {
                    destinationTypes.forEach((destinationType, artifacts) ->
                            links.addAll(queryByArtifacts(getLinkParameters.supplyChainId(),
                                    getLinkParameters.destinationSegmentName(),
                                    stepName,
                                    destinationType,
                                    artifacts)));

                    resolvedSteps.add(stepName);
                });

        Set<Set<LinkMetaBlock>> resolvedLinkSets = new HashSet<>(getLinkParameters.getLinkSets());

        Set<LinkMetaBlock> newLinkSetByRunId = getLinksForRemainingStepsByRunId(getLinkParameters.supplyChainId(), getLinkParameters.destinationSegmentName(), resolvedSteps, links);

        return permutate(newLinkSetByRunId, resolvedLinkSets, resolvedSegmentsWithLinkSets, getLinkParameters.getDestinationSegmentName());
    }

    private Set<LinkMetaBlock> getLinksForRemainingStepsByRunId(String supplyChainId, String destinationSegmentName, List<String> resolvedSteps, Set<LinkMetaBlock> links) {
        Set<LinkMetaBlock> newLinkSetByRunId = new HashSet<>(links);
        Set<String> runIds = findRunIds(links);
        runIds.forEach(runId ->
                newLinkSetByRunId.addAll(queryByRunId(supplyChainId, runId,
                        destinationSegmentName,
                        resolvedSteps))
        );
        return newLinkSetByRunId;
    }

    private Set<Set<LinkMetaBlock>> permutate(Set<LinkMetaBlock> newLinkSet, Set<Set<LinkMetaBlock>> resolvedLinkSets, ResolvedSegmentsWithLinkSets resolvedSegmentsWithLinkSets, String destinationSegmentName) {
        Set<Set<LinkMetaBlock>> possibleNewLinkSets = permutateNewLinkSet(newLinkSet);
        resolvedSegmentsWithLinkSets.addResolvedLinkSetsBySegmentName(destinationSegmentName, possibleNewLinkSets);
        if (!resolvedLinkSets.isEmpty()) {
            Set<Set<LinkMetaBlock>> tempLinkSet = new HashSet<>();
            possibleNewLinkSets.forEach(nelink ->
                    resolvedLinkSets.forEach(linkSet -> {
                                Set<LinkMetaBlock> tmp = new HashSet<>(linkSet);
                                tmp.addAll(nelink);
                                tempLinkSet.add(tmp);
                            }
                    ));


            return tempLinkSet;
        } else {
            return possibleNewLinkSets;
        }
    }

    private Set<Set<LinkMetaBlock>> permutateNewLinkSet(Set<LinkMetaBlock> newLinkSet) {
        Map<String, List<StepWithEqualLinkSet>> stepsWithEqualLinksSetByStepName = createStepsWithEqualLinksSetByStepName(newLinkSet);
        if (stepsWithEqualLinksSetByStepName.size() > 1) {
            return createPossibleCombinationsForMultipleSteps(stepsWithEqualLinksSetByStepName);
        } else {
            return createPossibleCombinationsForSingleStep(stepsWithEqualLinksSetByStepName);
        }
    }

    private Map<String, List<StepWithEqualLinkSet>> createStepsWithEqualLinksSetByStepName(Set<LinkMetaBlock> newLinkSet) {
        return newLinkSet.stream()
                .collect(
                        groupingBy(linkMetaBlock -> linkMetaBlock.getLink().getStepName(),
                                groupingBy(linkMetaBlock -> linkMetaBlock.getLink().hashCode())
                        )
                ).entrySet()
                .stream()
                .flatMap(stepsByEqualSet ->
                        stepsByEqualSet.getValue().values()
                                .stream()
                                .map(linkMetaBlocks -> StepWithEqualLinkSet.builder()
                                        .stepName(stepsByEqualSet.getKey())
                                        .equalLinkMetaBlocks(new HashSet<>(linkMetaBlocks))
                                        .build()))
                .collect(groupingBy(n -> n.stepName, LinkedHashMap::new, Collectors.toList()));
    }

    private Set<Set<LinkMetaBlock>> createPossibleCombinationsForSingleStep(Map<String, List<StepWithEqualLinkSet>> nodesGroupedByStepName) {
        Set<Set<LinkMetaBlock>> allCombinations = new HashSet<>();
        nodesGroupedByStepName.forEach((stepName, stepWithEqualLinkSets) -> stepWithEqualLinkSets
                .forEach(stepWithEqualLinkSet -> allCombinations.add(stepWithEqualLinkSet.equalLinkMetaBlocks)));
        return allCombinations;
    }

    private Set<Set<LinkMetaBlock>> createPossibleCombinationsForMultipleSteps(Map<String, List<StepWithEqualLinkSet>> nodesGroupedByStepName) {
        LinkedList<List<StepWithEqualLinkSet>> listsToConnect = new LinkedList<>(nodesGroupedByStepName.values());
        Graph<StepWithEqualLinkSet> linkSetGraph = configurePossibleCombinations(listsToConnect);
        List<StepWithEqualLinkSet> startNodes = listsToConnect.getFirst();
        List<StepWithEqualLinkSet> endNodes = listsToConnect.getLast();
        calculatePossiblePathsBetweenStartEndNodes(linkSetGraph, startNodes, endNodes);
        List<LinkedList<StepWithEqualLinkSet>> possibleCombinations = linkSetGraph.getPossiblePaths();
        Set<Set<LinkMetaBlock>> allCombinations = new HashSet<>();
        possibleCombinations
                .forEach(combination ->
                        allCombinations.add(new HashSet<>(combination.stream()
                                .flatMap(stepWithEqualLinkSet -> stepWithEqualLinkSet
                                        .equalLinkMetaBlocks
                                        .stream()
                                ).collect(Collectors.toList())))

                );

        return allCombinations;
    }

    private Graph<StepWithEqualLinkSet> configurePossibleCombinations(LinkedList<List<StepWithEqualLinkSet>> listsToConnect) {
        //for each node connect all current StepWithEqualLinkSets with next StepWithEqualLinkSets
        Graph<StepWithEqualLinkSet> linkSetGraph = new Graph<>();
        for (int i = 0; i < listsToConnect.size(); i++) {
            if (i + 1 < listsToConnect.size()) {
                List<StepWithEqualLinkSet> currentNode = listsToConnect.get(i);
                List<StepWithEqualLinkSet> nextNode = listsToConnect.get(i + 1);
                addEdges(linkSetGraph, currentNode, nextNode);
            }
        }
        return linkSetGraph;
    }

    private void addEdges(Graph<StepWithEqualLinkSet> graph, List<StepWithEqualLinkSet> currentNode, List<StepWithEqualLinkSet> nextNode) {
        //perform x times y add edges
        currentNode.forEach(current ->
                nextNode.forEach(next -> graph.addEdge(current, next))
        );

    }

    private void calculatePossiblePathsBetweenStartEndNodes(Graph<StepWithEqualLinkSet> linkSetGraph, List<StepWithEqualLinkSet> startNodes, List<StepWithEqualLinkSet> endNodes) {
        // firstnodes  * lastnodes
        startNodes.forEach(current ->
                endNodes.forEach(next -> {
                    LinkedList<StepWithEqualLinkSet> visited = new LinkedList<>();
                    visited.add(current);
                    linkSetGraph.calculatePossiblePaths(visited, next);
                })
        );
    }

    private Set<String> findRunIds(Set<LinkMetaBlock> linkSets) {
        return linkSets
                .stream()
                .map(linkSet -> linkSet.getLink().getRunId())
                .collect(Collectors.toSet());
    }

    private Set<LinkMetaBlock> queryByArtifacts(String supplyChainId,
                                                String destinationSegmentName,
                                                String stepName, DestinationType destinationType,
                                                List<Artifact> filteredArtifacts) {
        if (!filteredArtifacts.isEmpty()) {
            if (PRODUCTS == destinationType) {
                return new HashSet<>(linkMetaBlockRepository
                        .findBySupplyChainAndSegmentNameAndStepNameAndProductHashes(
                                supplyChainId,
                                destinationSegmentName,
                                stepName,
                                filteredArtifacts
                                        .stream()
                                        .map(Artifact::getHash)
                                        .collect(Collectors.toList()))
                );
            } else if (DestinationType.MATERIALS == destinationType) {
                return new HashSet<>(linkMetaBlockRepository
                        .findBySupplyChainAndSegmentNameAndStepNameAndMaterialHash(
                                supplyChainId,
                                destinationSegmentName,
                                stepName,
                                filteredArtifacts
                                        .stream()
                                        .map(Artifact::getHash)
                                        .collect(Collectors.toList()))
                );
            }
        }
        return emptySet();
    }

    private Set<LinkMetaBlock> queryByRunId(String supplyChainId, String runId, String destinationSegmentName, List<String> resolvedSteps) {
        return new HashSet<>(linkMetaBlockRepository.findByRunId(supplyChainId, destinationSegmentName, runId, resolvedSteps));
    }

    private Map<String, Map<DestinationType, List<Artifact>>> filterEndproductsByStepAndDestinationType(List<MatchFilter> matchFilters, List<Artifact> endProducts) {

        Map<String, Map<DestinationType, List<Artifact>>> filteredArtifactsByStepNameByDestinationType = new HashMap<>();
        Map<String, Map<DestinationType, List<MatchFilter>>> matchFiltersByStepNameByDestinationType = matchFilters
                .stream()
                .collect(groupingBy(MatchFilter::getDestinationStepName, groupingBy(MatchFilter::getDestinationType)));

        matchFiltersByStepNameByDestinationType.forEach((stepName, destinationType) -> {
            filteredArtifactsByStepNameByDestinationType.put(stepName, new EnumMap<>(DestinationType.class));
                    destinationType.forEach((destination, filters) -> {

                                filteredArtifactsByStepNameByDestinationType
                                        .get(stepName)
                                        .put(destination, new ArrayList<>());

                                filters.forEach(filter -> filteredArtifactsByStepNameByDestinationType
                                        .get(stepName).get(destination)
                                        .addAll(endProducts.stream()
                                                .filter(endProduct -> matches(endProduct.getUri(), filter.getPattern()))
                                                .collect(Collectors.toList())));
                            }
                    );
                }
        );
        return filteredArtifactsByStepNameByDestinationType;
    }

    @Data
    private static class ResolvedSegmentsWithLinkSets {

        private Set<String> resolvedSegments;
        private Set<Set<LinkMetaBlock>> linkSets;
        private Map<String, Set<Set<LinkMetaBlock>>> resolvedLinkSetsBySegmentName;

        void addResolvedSegment(String resolvedSegmentName) {
            if (resolvedSegments == null) {
                resolvedSegments = new HashSet<>();
            }
            resolvedSegments.add(resolvedSegmentName);
        }

        void addResolvedLinkSetsBySegmentName(String resolvedSegmentName, Set<Set<LinkMetaBlock>> newLinkSets) {
            if (resolvedLinkSetsBySegmentName == null) {
                resolvedLinkSetsBySegmentName = new HashMap<>();
            }
            resolvedLinkSetsBySegmentName.put(resolvedSegmentName, newLinkSets);
        }

        Set<Set<LinkMetaBlock>> getResolvedLinkSetsBySegmentName(String resolvedSegmentName) {
            return resolvedLinkSetsBySegmentName.getOrDefault(resolvedSegmentName, emptySet());
        }

    }

    @Data
    @Builder
    private static class MatchRuleWithSourceType {
        private String sourceStepName;
        private String sourceSegmentName;
        private MatchRule matchRule;
        private DestinationType sourceType;
        private List<List<Artifact>> resolvedArtifacts;

        void resolveArtifacts(Set<LinkMetaBlock> linkMetaBlocks) {
            if (resolvedArtifacts == null) {
                resolvedArtifacts = new ArrayList<>();
            }
            List<LinkMetaBlock> sourceLinks = linkMetaBlocks
                    .stream()
                    .filter(linkMetaBlock -> linkMetaBlock.getLink().getLayoutSegmentName().equals(sourceSegmentName)
                            &&
                            linkMetaBlock.getLink().getStepName().equals(sourceStepName))
                    .collect(Collectors.toList());
            sourceLinks.forEach(linkMetaBlock -> {
                List<Artifact> artifacts;
                if (PRODUCTS == sourceType) {
                    artifacts = linkMetaBlock.getLink().getProducts();
                } else {
                    artifacts = linkMetaBlock.getLink().getMaterials();
                }


                resolvedArtifacts.add(filterArtifacts(artifacts,
                        matchRule.getPattern(),
                        matchRule.getSourcePathPrefix()).collect(Collectors.toList()));
            });
        }
    }

    @Builder
    @Getter
    private static class GetLinkParameters {
        private final LayoutMetaBlock layoutMetaBlock;
        private final Map<String, Map<DestinationType, List<Artifact>>> filteredArtifacts;
        private final Set<String> resolvedSegments;
        private final Set<Set<LinkMetaBlock>> linkSets;
        private final String destinationSegmentName;

        String destinationSegmentName() {
            if (destinationSegmentName == null) {
                return layoutMetaBlock.expectedEndProducts().iterator().next().getDestinationSegmentName();
            } else {
                return destinationSegmentName;
            }

        }

        String supplyChainId() {
            return layoutMetaBlock.getSupplyChainId();
        }
    }

    @EqualsAndHashCode
    private static class StepWithEqualLinkSet {

        private String stepName;
        private Set<LinkMetaBlock> equalLinkMetaBlocks;

        @Builder
        public StepWithEqualLinkSet(String stepName, Set<LinkMetaBlock> equalLinkMetaBlocks) {
            this.stepName = stepName;
            this.equalLinkMetaBlocks = equalLinkMetaBlocks;
        }
    }

    @Getter(value = AccessLevel.PRIVATE)
    private static class Graph<N> {
        private Map<N, LinkedHashSet<N>> map = new HashMap<>();
        private List<LinkedList<N>> possiblePaths = new ArrayList<>();

        private void addEdge(N node1, N node2) {
            LinkedHashSet<N> adjacent = map.computeIfAbsent(node1, adjacentLinkedHashSet -> new LinkedHashSet<>());
            adjacent.add(node2);
        }

        private LinkedList<N> adjacentNodes(N last) {
            LinkedHashSet<N> adjacent = map.get(last);
            if (adjacent == null) {
                return new LinkedList<>();
            }
            return new LinkedList<>(adjacent);
        }

        private void calculatePossiblePaths(LinkedList<N> visited, N endNode) {
            LinkedList<N> nodes = adjacentNodes(visited.getLast());
            // examine adjacent nodes
            for (N node : nodes) {
                if (!visited.contains(node) && node.equals(endNode)) {
                    visited.add(node);
                    //collect possible path
                    possiblePaths.add(new LinkedList<>(visited));
                    visited.removeLast();
                    break;
                }
            }

            for (N node : nodes) {
                if (!visited.contains(node) && !node.equals(endNode)) {
                    visited.addLast(node);
                    calculatePossiblePaths(visited, endNode);
                    visited.removeLast();
                }

            }
        }
    }
}

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
package com.rabobank.argos.service.domain;

import com.rabobank.argos.domain.layout.LayoutMetaBlock;
import com.rabobank.argos.domain.layout.LayoutSegment;
import com.rabobank.argos.domain.layout.Step;
import com.rabobank.argos.domain.link.LinkMetaBlock;
import com.rabobank.argos.service.domain.verification.VerificationContext;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.groupingBy;

public class VerificationContextsProvider {

    private final LayoutMetaBlock layoutMetaBlock;
    private final Map<String, List<StepWithEqualLinkSet>> nodesGroupedByStepName;
    private final LayoutSegment segment;

    public VerificationContextsProvider(List<LinkMetaBlock> linkMetaBlocks, LayoutSegment segment, LayoutMetaBlock layoutMetaBlock) {
        this.layoutMetaBlock = layoutMetaBlock;
        this.segment = segment;
        this.nodesGroupedByStepName = initializeNodesGroupedByStepName(linkMetaBlocks, segment)
                .stream()
                .collect(groupingBy(n -> n.getStep().getStepName(), LinkedHashMap::new, Collectors.toList()));

    }

    private List<StepWithEqualLinkSet> initializeNodesGroupedByStepName(List<LinkMetaBlock> linkMetaBlocks, LayoutSegment segment) {
        List<StepWithEqualLinkSet> nodes = new ArrayList<>();
        Map<String, List<LinkMetaBlock>> linksByStepName = linkMetaBlocks
                .stream()
                .collect(groupingBy(linkMetaBlock -> linkMetaBlock.getLink().getStepName()));
        for (Step step : segment.getSteps()) {
            Map<Integer, List<LinkMetaBlock>> linkMetaBlockMap = linksByStepName.get(step.getStepName()).stream()
                    .collect(groupingBy(linkMetaBlock -> linkMetaBlock.getLink().hashCode()));

            for (List<LinkMetaBlock> links : linkMetaBlockMap.values()) {
                StepWithEqualLinkSet stepWithEqualLinkSet = StepWithEqualLinkSet
                        .builder()
                        .step(step)
                        .equalLinkMetaBlocks(links)
                        .build();
                nodes.add(stepWithEqualLinkSet);
            }
        }
        return nodes;
    }

    public List<VerificationContext> calculatePossibleVerificationContexts() {
        if (nodesGroupedByStepName.values().isEmpty()) {
            return emptyList();
        } else if (segment.getSteps().size() == 1)
            return createVerificationContextsForOneStep();
        else {
            return createVerificationContextsForMultipleSteps();
        }

    }

    private List<VerificationContext> createVerificationContextsForMultipleSteps() {
        LinkedList<List<StepWithEqualLinkSet>> listsToConnect = new LinkedList<>(nodesGroupedByStepName.values());
        Graph<StepWithEqualLinkSet> linkSetGraph = configurePossibleCombinations(listsToConnect);
        List<StepWithEqualLinkSet> startNodes = listsToConnect.getFirst();
        List<StepWithEqualLinkSet> endNodes = listsToConnect.getLast();
        calculatePossiblePathsBetweenStartEndNodes(linkSetGraph, startNodes, endNodes);
        List<LinkedList<StepWithEqualLinkSet>> possibleCombinations = linkSetGraph.getPossiblePaths();
        return createPossibleVerificationContexts(possibleCombinations);
    }

    private List<VerificationContext> createVerificationContextsForOneStep() {
        return nodesGroupedByStepName.values()
                .stream()
                .flatMap(stepWithEqualLinkSets -> stepWithEqualLinkSets
                        .stream()
                        .map(stepWithEqualLinkSet -> VerificationContext.builder()
                                .linkMetaBlocks(stepWithEqualLinkSet.equalLinkMetaBlocks)
                                .layoutMetaBlock(layoutMetaBlock)
                                .segment(segment).build()))
                .collect(Collectors.toList());
    }

    private List<VerificationContext> createPossibleVerificationContexts(List<LinkedList<StepWithEqualLinkSet>> possibleCombinations) {
        return possibleCombinations.stream()
                .map(possibleSet ->
                        VerificationContext
                                .builder()
                                .layoutMetaBlock(layoutMetaBlock)
                                .segment(segment)
                                .linkMetaBlocks(possibleSet
                                        .stream()
                                        .flatMap(l -> l.getEqualLinkMetaBlocks().stream())
                                        .collect(Collectors.toList()))
                                .build()
                ).collect(Collectors.toList());
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

    @Getter
    @EqualsAndHashCode
    public static class StepWithEqualLinkSet {

        private Step step;
        private List<LinkMetaBlock> equalLinkMetaBlocks;

        @Builder
        public StepWithEqualLinkSet(Step step, List<LinkMetaBlock> equalLinkMetaBlocks) {
            this.step = step;
            this.equalLinkMetaBlocks = equalLinkMetaBlocks;
        }
    }

    @Slf4j
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
                if (visited.contains(node)) {
                    continue;
                }
                if (node.equals(endNode)) {
                    visited.add(node);
                    //collect possible path
                    possiblePaths.add(new LinkedList<>(visited));
                    visited.removeLast();
                    break;
                }
            }
            for (N node : nodes) {
                if (visited.contains(node) || node.equals(endNode)) {
                    continue;
                }
                visited.addLast(node);
                calculatePossiblePaths(visited, endNode);
                visited.removeLast();
            }
        }
    }
}


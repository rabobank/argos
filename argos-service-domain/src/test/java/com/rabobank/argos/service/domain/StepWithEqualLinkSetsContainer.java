package com.rabobank.argos.service.domain;

import com.rabobank.argos.domain.layout.LayoutMetaBlock;
import com.rabobank.argos.domain.layout.LayoutSegment;
import com.rabobank.argos.domain.layout.Step;
import com.rabobank.argos.domain.link.LinkMetaBlock;
import com.rabobank.argos.service.domain.verification.VerificationContext;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

public class StepWithEqualLinkSetsContainer {

    private final LayoutMetaBlock layoutMetaBlock;
    private final Map<String, List<StepWithEqualLinkSet>> nodesGroupedByStepName;
    private final LayoutSegment segment;

    public StepWithEqualLinkSetsContainer(List<LinkMetaBlock> linkMetaBlocks, LayoutSegment segment, LayoutMetaBlock layoutMetaBlock) {

        Map<String, List<LinkMetaBlock>> linksByStepName = linkMetaBlocks
                .stream()
                .collect(groupingBy(linkMetaBlock -> linkMetaBlock.getLink().getStepName()));
        List<StepWithEqualLinkSet> nodes = new ArrayList<>();
        this.layoutMetaBlock = layoutMetaBlock;
        this.segment = segment;

        for (Step step : segment.getSteps()) {
            Map<Integer, List<LinkMetaBlock>> linkMetaBlockMap = linksByStepName.get(step.getStepName()).stream()
                    .collect(groupingBy(f -> f.getLink().hashCode()));
            for (List<LinkMetaBlock> links : linkMetaBlockMap.values()) {
                StepWithEqualLinkSet stepWithEqualLinkSet = StepWithEqualLinkSet
                        .builder()
                        .step(step)
                        .equalLinkMetaBlocks(links)
                        .build();
                nodes.add(stepWithEqualLinkSet);
            }
        }

        this.nodesGroupedByStepName = nodes.stream()
                .collect(groupingBy(n -> n.getStep().getStepName(), LinkedHashMap::new, Collectors.toList()));
    }

    public List<VerificationContext> calculatePossibleVerificationContexts() {

        List<List<StepWithEqualLinkSet>> list = nodesGroupedByStepName.values().stream().collect(Collectors.toList());
        // voor elke is er een volgende ja connect alle huidige met volgende
        Graph<StepWithEqualLinkSet> linkSetGraph = new Graph<>();
        for (int i = 0; i < list.size(); i++) {
            if (i + 1 < list.size()) {
                List<StepWithEqualLinkSet> currentNode = list.get(i);
                List<StepWithEqualLinkSet> nextNode = list.get(i + 1);
                addEdges(linkSetGraph, currentNode, nextNode);
            }
        }
        List<StepWithEqualLinkSet> startNodes = nodesGroupedByStepName.entrySet().iterator().next().getValue();
        List<StepWithEqualLinkSet> endNodes = nodesGroupedByStepName.values().stream().collect(Collectors.toList())
                .get(nodesGroupedByStepName.values().size() - 1);
        // firstnodes  times lastnodes
        startNodes.forEach(current ->
                endNodes.forEach(next -> {
                    LinkedList<StepWithEqualLinkSet> visited = new LinkedList();
                    visited.add(current);
                    linkSetGraph.calculatePossiblePaths(visited, next);
                })
        );

        List<LinkedList<StepWithEqualLinkSet>> possibleCombinations = linkSetGraph.getPossiblePaths();

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
}

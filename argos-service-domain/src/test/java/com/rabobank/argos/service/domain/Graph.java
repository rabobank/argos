package com.rabobank.argos.service.domain;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Slf4j
@Getter
public class Graph<N> {
    private Map<N, LinkedHashSet<N>> map = new HashMap();
    private List<LinkedList<N>> possiblePaths = new ArrayList<>();

    public void addEdge(N node1, N node2) {
        LinkedHashSet<N> adjacent = map.get(node1);
        if (adjacent == null) {
            adjacent = new LinkedHashSet();
            map.put(node1, adjacent);
        }
        adjacent.add(node2);
    }

    public LinkedList<N> adjacentNodes(N last) {
        LinkedHashSet<N> adjacent = map.get(last);
        if (adjacent == null) {
            return new LinkedList();
        }
        return new LinkedList<>(adjacent);
    }

    public void calculatePossiblePaths(LinkedList<N> visited, N endNode) {
        LinkedList<N> nodes = adjacentNodes(visited.getLast());
        // examine adjacent nodes
        for (N node : nodes) {
            if (visited.contains(node)) {
                continue;
            }
            if (node.equals(endNode)) {
                visited.add(node);
                possiblePaths.add(new LinkedList<>(visited));
                log.info(String.valueOf(visited));
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
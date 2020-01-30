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
package com.rabobank.argos.domain.util.graph;

import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Getter
public class Graph<N> {
    private Map<N, LinkedHashSet<N>> map = new HashMap<>();
    private List<LinkedList<N>> possiblePaths = new ArrayList<>();

    public void addEdge(N node1, N node2) {
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

    public void calculatePossiblePaths(LinkedList<N> visited, N endNode) {
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
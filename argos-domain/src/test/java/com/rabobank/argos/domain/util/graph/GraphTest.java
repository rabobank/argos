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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

class GraphTest {

    private Graph<String> graph;

    @BeforeEach
    void setup() {
        graph = new Graph<>();
    }

    @Test
    void getPossiblePathsShouldReturn4() {
        //set 1
        String a = "a";
        String b = "b";
        //set 2
        String c = "c";
        String d = "d";
        graph.addEdge(a, c);
        graph.addEdge(a, d);
        graph.addEdge(b, c);
        graph.addEdge(b, d);
        LinkedList<String> visited = new LinkedList<>();
        visited.add(a);
        graph.calculatePossiblePaths(visited, c);
        graph.calculatePossiblePaths(visited, d);
        LinkedList<String> visited2 = new LinkedList<>();
        visited2.add(b);
        graph.calculatePossiblePaths(visited2, c);
        graph.calculatePossiblePaths(visited2, d);
        List<LinkedList<String>> result = graph.getPossiblePaths();
        assertThat(result, hasSize(4));
    }


}
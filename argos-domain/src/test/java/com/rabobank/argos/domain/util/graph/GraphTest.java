package com.rabobank.argos.domain.util.graph;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        // graph.calculatePossiblePaths(a,c);
    }


}
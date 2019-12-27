package com.rabobank.argos.service.domain;

import java.util.LinkedList;

public class Search {

    private static final String START = "A";
    private static final String END = "E";

    public static void main(String[] args) {
         /*
              A -- B -- C -- E
                   |    | -- F
                   |
                   | -- D -- E
                   | -- D -- F

                   */
        // this graph is directional
        Graph<String> graph = new Graph();
        graph.addEdge("A", "B");

        graph.addEdge("B", "C");
        graph.addEdge("B", "D");
        graph.addEdge("C", "E");
        graph.addEdge("C", "F");
        graph.addEdge("D", "E");
        graph.addEdge("D", "F");
        LinkedList<String> visited = new LinkedList();
        visited.add(START);
        new Search().depthFirst(graph, visited, "E");
        new Search().depthFirst(graph, visited, "F");
    }

    private void depthFirst(Graph graph, LinkedList<String> visited, String endNode) {
        LinkedList<String> nodes = graph.adjacentNodes(visited.getLast());
        // examine adjacent nodes
        for (String node : nodes) {
            if (visited.contains(node)) {
                continue;
            }
            if (node.equals(endNode)) {
                visited.add(node);
                printPath(visited);
                visited.removeLast();
                break;
            }
        }
        for (String node : nodes) {
            if (visited.contains(node) || node.equals(endNode)) {
                continue;
            }
            visited.addLast(node);
            depthFirst(graph, visited, endNode);
            visited.removeLast();
        }
    }

    private void printPath(LinkedList<String> visited) {
        for (String node : visited) {
            System.out.print(node);
            System.out.print(" ");
        }
        System.out.println();
    }
}
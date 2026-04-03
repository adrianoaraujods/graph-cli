package graph.api;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;

public class StaticGraphHelper {

    public static void assertN(StaticGraph graph, int expected) {
        assertEquals(expected, graph.n);
    }

    public static void assertM(StaticGraph graph, int expected) {
        assertEquals(expected, graph.m);
    }

    public static void assertVertices(StaticGraph graph, int... expected) {
        assertArrayEquals(expected, graph.getVertices());
    }

    public static void assertDegree(UndirectedGraph graph, int vertex, int expected) {
        assertEquals(expected, graph.getDegree(vertex));
    }

    public static void assertInDegree(DirectedGraph graph, int vertex, int expected) {
        assertEquals(expected, graph.getInDegree(vertex));
    }

    public static void assertOutDegree(DirectedGraph graph, int vertex, int expected) {
        assertEquals(expected, graph.getOutDegree(vertex));
    }

    public static void assertSuccessors(DirectedGraph graph, int vertex, int... expected) {
        int[] actual = graph.getSuccessors(vertex);
        assertEquals(expected.length, actual.length);

        Set<Integer> expectedSet = new HashSet<>();
        for (int v : expected)
            expectedSet.add(v);
        Set<Integer> actualSet = new HashSet<>();
        for (int v : actual)
            actualSet.add(v);

        assertEquals(expectedSet, actualSet);
    }

    public static void assertPredecessors(DirectedGraph graph, int vertex, int... expected) {
        int[] actual = graph.getPredecessors(vertex);
        assertEquals(expected.length, actual.length);

        Set<Integer> expectedSet = new HashSet<>();
        for (int v : expected)
            expectedSet.add(v);
        Set<Integer> actualSet = new HashSet<>();
        for (int v : actual)
            actualSet.add(v);

        assertEquals(expectedSet, actualSet);
    }

    public static void assertNeighbors(UndirectedGraph graph, int vertex, int... expected) {
        int[] actual = graph.getNeighbors(vertex);
        assertEquals(expected.length, actual.length);

        Set<Integer> expectedSet = new HashSet<>();
        for (int v : expected)
            expectedSet.add(v);
        Set<Integer> actualSet = new HashSet<>();
        for (int v : actual)
            actualSet.add(v);

        assertEquals(expectedSet, actualSet);
    }

    public static void assertIterateVertexCount(StaticGraph graph, int expected) {
        final int[] count = { 0 };
        graph.iterateGraph(new graph.api.Graph.IteratorVisitor() {
            @Override
            public void examineVertex(int vertex) {
                count[0]++;
            }

            @Override
            public void examineEdge(int source, int target) {
            }
        });
        assertEquals(expected, count[0]);
    }

    public static void assertIterateEdgeCount(StaticGraph graph, int expected) {
        final int[] count = { 0 };
        graph.iterateGraph(new graph.api.Graph.IteratorVisitor() {
            @Override
            public void examineVertex(int vertex) {
            }

            @Override
            public void examineEdge(int source, int target) {
                count[0]++;
            }
        });
        assertEquals(expected, count[0]);
    }

    public static void assertThrowsIAE(org.junit.jupiter.api.function.Executable executable) {
        assertThrows(IllegalArgumentException.class, executable);
    }

    public static void assertInducedSubgraphN(StaticGraph graph, int[] vertices, int expectedN) {
        StaticGraph subgraph = graph.getInducedSubgraph(vertices);
        assertEquals(expectedN, subgraph.n);
    }

    public static void assertInducedSubgraphM(StaticGraph graph, int[] vertices, int expectedM) {
        StaticGraph subgraph = graph.getInducedSubgraph(vertices);
        assertEquals(expectedM, subgraph.m);
    }

    public static void assertReversedM(DirectedGraph graph, int expectedM) {
        StaticGraph reversed = graph.getReversed();
        assertEquals(expectedM, reversed.m);
    }

    public static void assertReversedHasEdge(DirectedGraph graph, int source, int target) {
        StaticGraph reversed = graph.getReversed();
        assertTrue(reversed instanceof DirectedGraph, "Reversed graph should implement DirectedGraph");

        DirectedGraph directedReversed = (DirectedGraph) reversed;
        int[] successors = directedReversed.getSuccessors(source);

        boolean found = false;
        for (int s : successors) {
            if (s == target) {
                found = true;
                break;
            }
        }
        assertTrue(found, "Expected reversed graph to have edge " + source + " -> " + target);
    }
}
package graph.algorithms;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.Test;

import graph.api.Graph;

class NaiveBridgesTest {

    @Test
    void testEmptyGraph() {
        Graph graph = BridgesTestHelper.buildUndirected(0, 0, BridgesTestHelper.emptyGraph());

        Set<int[]> bridges = NaiveBridges.find(graph);
        assertTrue(bridges.isEmpty(), "Empty graph should have no bridges");
    }

    @Test
    void testSingleEdge() {
        Graph graph = BridgesTestHelper.buildUndirected(2, 1, BridgesTestHelper.singleEdge());

        Set<int[]> bridges = NaiveBridges.find(graph);
        assertEquals(1, bridges.size(), "Single edge graph should have one bridge");
    }

    @Test
    void testLinearChain() {
        Graph graph = BridgesTestHelper.buildUndirected(5, 4, BridgesTestHelper.linearChain());

        Set<int[]> bridges = NaiveBridges.find(graph);
        assertEquals(4, bridges.size(), "Linear chain of 5 vertices has 4 bridges");
    }

    @Test
    void testSimpleCycle() {
        Graph graph = BridgesTestHelper.buildUndirected(3, 3, BridgesTestHelper.simpleCycle());

        Set<int[]> bridges = NaiveBridges.find(graph);
        assertTrue(bridges.isEmpty(), "Simple cycle has no bridges");
    }

    @Test
    void testGraphWithOneBridge() {
        Graph graph = BridgesTestHelper.buildUndirected(6, 5, BridgesTestHelper.graphWithOneBridge());

        Set<int[]> bridges = NaiveBridges.find(graph);
        assertTrue(bridges.size() >= 1, "Graph should have at least one bridge");
    }

    @Test
    void testMultipleBridges() {
        Graph graph = BridgesTestHelper.buildUndirected(7, 6, BridgesTestHelper.multipleBridges());

        Set<int[]> bridges = NaiveBridges.find(graph);
        assertTrue(bridges.size() >= 1, "Graph should have bridges");
    }

    @Test
    void testDisconnectedGraph() {
        Graph graph = BridgesTestHelper.buildUndirected(6, 3, BridgesTestHelper.disconnectedGraph());

        Set<int[]> bridges = NaiveBridges.find(graph);
        assertEquals(3, bridges.size(), "Disconnected graph with 3 components should have 3 bridges");
    }

    @Test
    void testTree() {
        Graph graph = BridgesTestHelper.buildUndirected(5, 4, BridgesTestHelper.tree());

        Set<int[]> bridges = NaiveBridges.find(graph);
        assertEquals(4, bridges.size(), "Tree with 4 edges should have 4 bridges");
    }

    @Test
    void testGraphWithTwoCycles() {
        Graph graph = BridgesTestHelper.buildUndirected(5, 6, BridgesTestHelper.graphWithTwoCycles());

        Set<int[]> bridges = NaiveBridges.find(graph);
        assertTrue(bridges.isEmpty(), "Graph with cycles should have no bridges");
    }

    @Test
    void testBridgeInMiddle() {
        Graph graph = BridgesTestHelper.buildUndirected(9, 10, BridgesTestHelper.bridgeInMiddle());

        Set<int[]> bridges = NaiveBridges.find(graph);
        assertTrue(bridges.size() >= 1, "Graph should have at least one bridge");
    }

    @Test
    void testBridgesAreUndirected() {
        Graph graph = BridgesTestHelper.buildUndirected(2, 1, BridgesTestHelper.singleEdge());

        Set<int[]> bridges = NaiveBridges.find(graph);

        boolean foundEdge = false;
        for (int[] edge : bridges) {
            if ((edge[0] == 1 && edge[1] == 2) || (edge[0] == 2 && edge[1] == 1)) {
                foundEdge = true;
                break;
            }
        }
        assertTrue(foundEdge, "Bridge should represent the undirected edge");
    }
}
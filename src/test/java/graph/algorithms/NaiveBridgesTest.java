package graph.algorithms;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.Test;

import graph.api.Edges;
import graph.api.Graph;
import graph.api.UndirectedGraph;
import graph.util.TestGraphs;
import graph.representations.adjacencymatrix.AdjacencyMatrixGraphBuilder;

class NaiveBridgesTest {

    @Test
    void testSingleEdge() {
        Graph graph = TestGraphs.singleEdge(() -> new AdjacencyMatrixGraphBuilder(false));

        Set<Long> bridges = NaiveBridges.findAll((UndirectedGraph) graph);
        assertEquals(1, bridges.size(), "Single edge graph should have one bridge");
    }

    @Test
    void testLinearChain() {
        Graph graph = TestGraphs.linearChain(() -> new AdjacencyMatrixGraphBuilder(false));

        Set<Long> bridges = NaiveBridges.findAll((UndirectedGraph) graph);
        assertEquals(4, bridges.size(), "Linear chain of 5 vertices has 4 bridges");
    }

    @Test
    void testSimpleCycle() {
        Graph graph = TestGraphs.simpleCycle(() -> new AdjacencyMatrixGraphBuilder(false));

        Set<Long> bridges = NaiveBridges.findAll((UndirectedGraph) graph);
        assertTrue(bridges.isEmpty(), "Simple cycle has no bridges");
    }

    @Test
    void testGraphWithOneBridge() {
        Graph graph = TestGraphs.graphWithOneBridge(() -> new AdjacencyMatrixGraphBuilder(false));

        Set<Long> bridges = NaiveBridges.findAll((UndirectedGraph) graph);
        assertTrue(bridges.size() >= 1, "Graph should have at least one bridge");
    }

    @Test
    void testMultipleBridges() {
        Graph graph = TestGraphs.multipleBridges(() -> new AdjacencyMatrixGraphBuilder(false));

        Set<Long> bridges = NaiveBridges.findAll((UndirectedGraph) graph);
        assertTrue(bridges.size() >= 1, "Graph should have bridges");
    }

    @Test
    void testDisconnectedGraph() {
        Graph graph = TestGraphs.disconnectedGraph(() -> new AdjacencyMatrixGraphBuilder(false));

        Set<Long> bridges = NaiveBridges.findAll((UndirectedGraph) graph);
        assertEquals(3, bridges.size(), "Disconnected graph with 3 components should have 3 bridges");
    }

    @Test
    void testTree() {
        Graph graph = TestGraphs.tree(() -> new AdjacencyMatrixGraphBuilder(false));

        Set<Long> bridges = NaiveBridges.findAll((UndirectedGraph) graph);
        assertEquals(4, bridges.size(), "Tree with 4 edges should have 4 bridges");
    }

    @Test
    void testGraphWithTwoCycles() {
        Graph graph = TestGraphs.graphWithTwoCycles(() -> new AdjacencyMatrixGraphBuilder(false));

        Set<Long> bridges = NaiveBridges.findAll((UndirectedGraph) graph);
        assertTrue(bridges.isEmpty(), "Graph with cycles should have no bridges");
    }

    @Test
    void testBridgeInMiddle() {
        Graph graph = TestGraphs.bridgeInMiddle(() -> new AdjacencyMatrixGraphBuilder(false));

        Set<Long> bridges = NaiveBridges.findAll((UndirectedGraph) graph);
        assertTrue(bridges.size() >= 1, "Graph should have at least one bridge");
    }

    @Test
    void testBridgesAreUndirected() {
        Graph graph = TestGraphs.singleEdge(() -> new AdjacencyMatrixGraphBuilder(false));

        Set<Long> bridges = NaiveBridges.findAll((UndirectedGraph) graph);

        boolean foundEdge = false;
        for (long edge : bridges) {
            int v = Edges.getSource(edge);
            int w = Edges.getTarget(edge);

            if ((v == 1 && w == 2) || (v == 2 && w == 1)) {
                foundEdge = true;
                break;
            }
        }
        assertTrue(foundEdge, "Bridge should represent the undirected edge");
    }
}
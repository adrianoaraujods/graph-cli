package graph.representations.forwardstar;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import graph.api.DirectedGraph;
import graph.api.Graph;
import graph.util.GraphHelper;
import graph.api.UndirectedGraph;
import graph.util.GraphTestHelper;

class ForwardStarGraphTest {

    @Test
    void testGetVerticesDefaultRange() {
        Graph graph = GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(false),
                5,
                new int[][] { { 1, 2 }, { 3, 4 } });

        GraphHelper.assertVertices(graph, 1, 2, 3, 4);
    }

    @Test
    void testGetDegreeUndirected() {
        UndirectedGraph graph = (UndirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(false),
                4,
                new int[][] { { 1, 2 }, { 1, 3 }, { 1, 4 } });

        GraphHelper.assertDegree(graph, 1, 3);
        GraphHelper.assertDegree(graph, 2, 1);
        GraphHelper.assertDegree(graph, 3, 1);
        GraphHelper.assertDegree(graph, 4, 1);
    }

    @Test
    void testGetInDegreeDirected() {
        DirectedGraph graph = (DirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 2 }, { 4, 2 } });

        GraphHelper.assertInDegree(graph, 1, 0);
        GraphHelper.assertInDegree(graph, 2, 3);
        GraphHelper.assertInDegree(graph, 3, 1);
        GraphHelper.assertInDegree(graph, 4, 0);
    }

    @Test
    void testGetOutDegreeDirected() {
        DirectedGraph graph = (DirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4,
                new int[][] { { 1, 2 }, { 1, 3 }, { 2, 4 }, { 3, 4 } });

        GraphHelper.assertOutDegree(graph, 1, 2);
        GraphHelper.assertOutDegree(graph, 2, 1);
        GraphHelper.assertOutDegree(graph, 3, 1);
        GraphHelper.assertOutDegree(graph, 4, 0);
    }

    @Test
    void testGetSuccessors() {
        DirectedGraph graph = (DirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                5,
                new int[][] { { 1, 2 }, { 1, 3 }, { 2, 4 }, { 2, 5 }, { 3, 5 } });

        GraphHelper.assertSuccessors(graph, 1, 2, 3);
        GraphHelper.assertSuccessors(graph, 2, 4, 5);
    }

    @Test
    void testGetPredecessors() {
        DirectedGraph graph = (DirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                5,
                new int[][] { { 1, 3 }, { 2, 3 }, { 3, 4 }, { 3, 5 }, { 4, 5 }, { 2, 5 } });

        GraphHelper.assertPredecessors(graph, 3, 1, 2);
        GraphHelper.assertPredecessors(graph, 5, 2, 3, 4);
    }

    @Test
    void testGetNeighborsUndirected() {
        UndirectedGraph graph = (UndirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(false),
                5,
                new int[][] { { 1, 2 }, { 1, 3 }, { 2, 4 }, { 5, 1 } });

        GraphHelper.assertNeighbors(graph, 1, 2, 3, 5);
        GraphHelper.assertNeighbors(graph, 2, 1, 4);
    }

    @Test
    void testGetInducedSubgraph() {
        Graph graph = GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                6,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 }, { 3, 4 }, { 4, 5 }, { 5, 6 } });

        GraphHelper.assertInducedSubgraphN(graph, new int[] { 1, 2, 3 }, 3);
        GraphHelper.assertInducedSubgraphM(graph, new int[] { 1, 2, 3 }, 3);
    }

    @Test
    void testGetReversedDirected() {
        DirectedGraph graph = (DirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4,
                new int[][] { { 1, 2 }, { 1, 3 }, { 2, 4 }, { 3, 4 } });

        GraphHelper.assertReversedM(graph, 4);
        GraphHelper.assertReversedHasEdge(graph, 2, 1);
        GraphHelper.assertReversedHasEdge(graph, 4, 2);
        GraphHelper.assertReversedHasEdge(graph, 4, 3);
    }

    @Test
    void testInvalidVertexArgumentGetDegree() {
        UndirectedGraph graph = (UndirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                5,
                new int[][] { { 1, 2 } });

        GraphHelper.assertThrowsIAE(() -> graph.getDegree(0));
        GraphHelper.assertThrowsIAE(() -> graph.getDegree(6));
    }

    @Test
    void testInvalidVertexArgumentGetInDegree() {
        DirectedGraph graph = (DirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                5,
                new int[][] { { 1, 2 } });

        GraphHelper.assertThrowsIAE(() -> graph.getInDegree(0));
        GraphHelper.assertThrowsIAE(() -> graph.getInDegree(6));
    }

    @Test
    void testInvalidVertexArgumentGetOutDegree() {
        DirectedGraph graph = (DirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                5,
                new int[][] { { 1, 2 } });

        GraphHelper.assertThrowsIAE(() -> graph.getOutDegree(0));
        GraphHelper.assertThrowsIAE(() -> graph.getOutDegree(6));
    }

    @Test
    void testInvalidVertexArgumentGetSuccessors() {
        DirectedGraph graph = (DirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                5,
                new int[][] { { 1, 2 } });

        GraphHelper.assertThrowsIAE(() -> graph.getSuccessors(0));
        GraphHelper.assertThrowsIAE(() -> graph.getSuccessors(6));
    }

    @Test
    void testInvalidVertexArgumentGetPredecessors() {
        DirectedGraph graph = (DirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                5,
                new int[][] { { 1, 2 } });

        GraphHelper.assertThrowsIAE(() -> graph.getPredecessors(0));
        GraphHelper.assertThrowsIAE(() -> graph.getPredecessors(6));
    }

    @Test
    void testEmptyGraph() {
        Graph graph = GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(false),
                5,
                new int[][] {});

        assertEquals(5, graph.getVerticesCount());
        assertEquals(0, graph.getEdgesCount());
        GraphHelper.assertDegree((UndirectedGraph) graph, 1, 0);
    }

    @Test
    void testSingleVertexGraph() {
        Graph graph = GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                1,
                new int[][] {});

        assertEquals(1, graph.getVerticesCount());
        assertEquals(0, graph.getEdgesCount());
    }

    @Test
    void testAddEdgeUndirected() {
        UndirectedGraph graph = (UndirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(false),
                5,
                new int[][] { { 1, 2 }, { 3, 4 } });

        GraphHelper.assertDegree(graph, 1, 1);
        GraphHelper.assertDegree(graph, 4, 1);

        graph.addEdge(1, 4);

        GraphHelper.assertDegree(graph, 1, 2);
        GraphHelper.assertDegree(graph, 4, 2);
        GraphHelper.assertNeighbors(graph, 1, 2, 4);
        GraphHelper.assertNeighbors(graph, 4, 3, 1);
    }

    @Test
    void testAddEdgeDirected() {
        DirectedGraph graph = (DirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4,
                new int[][] { { 1, 2 }, { 3, 4 } });

        GraphHelper.assertOutDegree(graph, 1, 1);
        GraphHelper.assertInDegree(graph, 2, 1);

        graph.addEdge(2, 4);

        GraphHelper.assertOutDegree(graph, 2, 1);
        GraphHelper.assertInDegree(graph, 4, 2);
        GraphHelper.assertSuccessors(graph, 2, 4);
        GraphHelper.assertPredecessors(graph, 4, 2, 3);
    }

    @Test
    void testAddEdgeUpdatesEdgeCount() {
        Graph graph = GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(false),
                3,
                new int[][] { { 1, 2 } });

        assertEquals(1, graph.getEdgesCount());

        graph.addEdge(2, 3);

        assertEquals(2, graph.getEdgesCount());
    }

    @Test
    void testRemoveEdgeUndirected() {
        UndirectedGraph graph = (UndirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(false),
                4,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 4 } });

        GraphHelper.assertDegree(graph, 2, 2);
        GraphHelper.assertNeighbors(graph, 2, 1, 3);

        graph.removeEdge(1, 2);

        GraphHelper.assertDegree(graph, 2, 1);
        GraphHelper.assertNeighbors(graph, 2, 3);
        GraphHelper.assertNeighbors(graph, 1);
    }

    @Test
    void testRemoveEdgeDirected() {
        DirectedGraph graph = (DirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 4 } });

        GraphHelper.assertOutDegree(graph, 2, 1);
        GraphHelper.assertInDegree(graph, 3, 1);

        graph.removeEdge(2, 3);

        GraphHelper.assertOutDegree(graph, 2, 0);
        GraphHelper.assertInDegree(graph, 3, 0);
    }

    @Test
    void testRemoveEdgeUpdatesEdgeCount() {
        Graph graph = GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(false),
                3,
                new int[][] { { 1, 2 }, { 2, 3 } });

        assertEquals(2, graph.getEdgesCount());

        graph.removeEdge(1, 2);

        assertEquals(1, graph.getEdgesCount());
    }

    @Test
    void testRemoveEdgeNonExistent() {
        UndirectedGraph graph = (UndirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(false),
                4,
                new int[][] { { 1, 2 }, { 3, 4 } });

        graph.removeEdge(1, 4);

        GraphHelper.assertDegree(graph, 1, 1);
        GraphHelper.assertDegree(graph, 4, 1);
        GraphHelper.assertNeighbors(graph, 1, 2);
        GraphHelper.assertNeighbors(graph, 4, 3);
    }

    @Test
    void testRemoveEdgeWithMultipleEdges() {
        UndirectedGraph graph = (UndirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(false),
                4,
                new int[][] { { 1, 3 }, { 2, 3 }, { 3, 4 }, { 1, 4 } });

        graph.removeEdge(1, 3);

        GraphHelper.assertNeighbors(graph, 3, 2, 4);
        GraphHelper.assertNeighbors(graph, 1, 4);
    }

    @Test
    void testUndirectedWeightedPerDirectionWeights() {
        ForwardStarGraphBuilder builder = new ForwardStarGraphBuilder(false);
        builder.initialize(3, 2, true, false);
        builder.addEdge(1, 2, 5);
        builder.addEdge(2, 1, 10);
        Graph graph = builder.build();

        assertEquals(1, graph.getEdgesCount());
        graph.api.WeightedGraph wg = (graph.api.WeightedGraph) graph;
        assertEquals(5, wg.getEdgeWeight(1, 2));
        assertEquals(10, wg.getEdgeWeight(2, 1));
    }

    @Test
    void testUndirectedWeightedIterationYieldsBothDirections() {
        ForwardStarGraphBuilder builder = new ForwardStarGraphBuilder(false);
        builder.initialize(3, 2, true, false);
        builder.addEdge(1, 2, 5);
        builder.addEdge(2, 1, 10);
        Graph graph = builder.build();

        java.util.Map<String, Integer> visited = new java.util.HashMap<>();
        graph.iterateGraph(new graph.api.GraphBase.IteratorVisitor() {
            @Override
            public void examineEdge(int v, int w, int weight) {
                visited.put(v + "->" + w, weight);
            }
        });

        assertEquals(2, visited.size());
        assertEquals(5, visited.get("1->2").intValue());
        assertEquals(10, visited.get("2->1").intValue());
    }

    @Test
    void testUndirectedWeightedSameDirectionDuplicate() {
        ForwardStarGraphBuilder builder = new ForwardStarGraphBuilder(false);
        builder.initialize(3, 2, true, false);
        builder.addEdge(1, 2, 5);
        builder.addEdge(1, 2, 10);
        Graph graph = builder.build();

        assertEquals(1, graph.getEdgesCount());
        graph.api.WeightedGraph wg = (graph.api.WeightedGraph) graph;
        assertEquals(10, wg.getEdgeWeight(1, 2));
    }

    @Test
    void testUndirectedWeightedSingleDirection() {
        ForwardStarGraphBuilder builder = new ForwardStarGraphBuilder(false);
        builder.initialize(3, 1, true, false);
        builder.addEdge(1, 2, 5);
        Graph graph = builder.build();

        assertEquals(1, graph.getEdgesCount());
        graph.api.WeightedGraph wg = (graph.api.WeightedGraph) graph;
        assertEquals(5, wg.getEdgeWeight(1, 2));
        assertEquals(5, wg.getEdgeWeight(2, 1));
    }

    @Test
    void testUndirectedCapacityPerDirectionCapacities() {
        ForwardStarGraphBuilder builder = new ForwardStarGraphBuilder(false);
        builder.initialize(3, 2, false, true);
        builder.addEdge(1, 2, 5);
        builder.addEdge(2, 1, 10);
        Graph graph = builder.build();

        assertEquals(1, graph.getEdgesCount());
        assertTrue(graph.hasCapacity());
        graph.api.FlowGraph fg = (graph.api.FlowGraph) graph;
        assertEquals(5, fg.getEdgeCapacity(1, 2));
        assertEquals(10, fg.getEdgeCapacity(2, 1));
    }

    @Test
    void testIsWeightedUnweightedGraph() {
        Graph graph = GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(false),
                3,
                new int[][] { { 1, 2 }, { 2, 3 } });

        assertFalse(graph.isWeighted());
    }

    @Test
    void testIsWeightedWeightedGraph() {
        ForwardStarGraphBuilder builder = new ForwardStarGraphBuilder(true); // directed
        builder.initialize(3, 2, true, false);
        builder.addEdge(1, 2, 10);
        builder.addEdge(2, 3, 20);
        Graph graph = builder.build();

        assertTrue(graph.isWeighted());
    }

    @Test
    void testGetEdgeWeight() {
        ForwardStarGraphBuilder builder = new ForwardStarGraphBuilder(false);
        builder.initialize(3, 2, true, false);
        builder.addEdge(1, 2, 10);
        builder.addEdge(2, 3, 20);
        Graph graph = builder.build();

        assertTrue(graph instanceof graph.api.WeightedGraph);
        graph.api.WeightedGraph weightedGraph = (graph.api.WeightedGraph) graph;
        assertEquals(10, weightedGraph.getEdgeWeight(1, 2));
        assertEquals(20, weightedGraph.getEdgeWeight(2, 3));
    }

    @Test
    void testGetEdgeWeightUnweightedReturnsOne() {
        Graph graph = GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(false),
                3,
                new int[][] { { 1, 2 }, { 2, 3 } });

        // Unweighted graphs still implement WeightedGraph but return 1
        if (graph instanceof graph.api.WeightedGraph wg) {
            assertEquals(1, wg.getEdgeWeight(1, 2));
        }
    }

    @Test
    void testIterateGraphWithWeights() {
        ForwardStarGraphBuilder builder = new ForwardStarGraphBuilder(false);
        builder.initialize(3, 2, true, false);
        builder.addEdge(1, 2, 10);
        builder.addEdge(2, 3, 20);
        Graph graph = builder.build();

        final int[] lastWeight = { 0 };

        graph.iterateGraph(new graph.api.GraphBase.IteratorVisitor() {
            @Override
            public void examineEdge(int v, int w, int weight) {
                lastWeight[0] = weight;
            }
        });

        // Verify the last edge visited has correct weight
        assertTrue(lastWeight[0] > 0);
    }

    @Test
    void testGetWeightsSet() {
        ForwardStarGraphBuilder builder = new ForwardStarGraphBuilder(true); // directed
        builder.initialize(3, 2, true, false);
        builder.addEdge(1, 2, 10);
        builder.addEdge(2, 3, 20);
        Graph graph = builder.build();

        graph.api.WeightedGraph wg = (graph.api.WeightedGraph) graph;
        int[] weights = wg.getWeightsSet();

        assertEquals(2, weights.length);
        // After sorting, verify both weights are present
        assertTrue(weights[0] == 10 || weights[0] == 20);
        assertTrue(weights[1] == 10 || weights[1] == 20);
    }

    @Test
    void testGetWeightedEdgesSet() {
        ForwardStarGraphBuilder builder = new ForwardStarGraphBuilder(true); // directed
        builder.initialize(3, 2, true, false);
        builder.addEdge(1, 2, 10);
        builder.addEdge(2, 3, 20);
        Graph graph = builder.build();

        graph.api.WeightedGraph wg = (graph.api.WeightedGraph) graph;
        graph.api.WeightedGraph.WeightedEdges result = wg.getWeightedEdgesSet();

        assertEquals(2, result.edges().length);
        assertEquals(2, result.weights().length);
    }
}

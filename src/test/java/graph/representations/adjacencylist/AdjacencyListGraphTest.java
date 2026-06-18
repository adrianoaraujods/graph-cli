package graph.representations.adjacencylist;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import graph.algorithms.Fleury;
import graph.api.DirectedGraph;
import graph.api.Graph;
import graph.api.GraphBase;
import graph.api.WeightedGraph;
import graph.cli.read.GraphLoader;
import graph.util.GraphHelper;
import graph.api.UndirectedGraph;
import graph.util.GraphTestHelper;

class AdjacencyListGraphTest {

    @Test
    void testGetVerticesDefaultRange() {
        Graph graph = GraphTestHelper.build(
                () -> new AdjacencyListGraphBuilder(false),
                5,
                new int[][] { { 1, 2 }, { 3, 4 } });

        GraphHelper.assertVertices(graph, 1, 2, 3, 4);
    }

    @Test
    void testGetDegreeUndirected() {
        UndirectedGraph graph = (UndirectedGraph) GraphTestHelper.build(
                () -> new AdjacencyListGraphBuilder(false),
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
                () -> new AdjacencyListGraphBuilder(true),
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
                () -> new AdjacencyListGraphBuilder(true),
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
                () -> new AdjacencyListGraphBuilder(true),
                5,
                new int[][] { { 1, 2 }, { 1, 3 }, { 2, 4 }, { 2, 5 }, { 3, 5 } });

        GraphHelper.assertSuccessors(graph, 1, 2, 3);
        GraphHelper.assertSuccessors(graph, 2, 4, 5);
    }

    @Test
    void testGetPredecessors() {
        DirectedGraph graph = (DirectedGraph) GraphTestHelper.build(
                () -> new AdjacencyListGraphBuilder(true),
                5,
                new int[][] { { 1, 3 }, { 2, 3 }, { 3, 4 }, { 3, 5 }, { 4, 5 }, { 2, 5 } });

        GraphHelper.assertPredecessors(graph, 3, 1, 2);
        GraphHelper.assertPredecessors(graph, 5, 2, 3, 4);
    }

    @Test
    void testGetNeighborsUndirected() {
        UndirectedGraph graph = (UndirectedGraph) GraphTestHelper.build(
                () -> new AdjacencyListGraphBuilder(false),
                5,
                new int[][] { { 1, 2 }, { 1, 3 }, { 2, 4 }, { 2, 5 }, { 3, 5 } });

        GraphHelper.assertNeighbors(graph, 1, 2, 3);
        GraphHelper.assertNeighbors(graph, 2, 1, 4, 5);
    }

    @Test
    void testClone() {
        Graph graph = GraphTestHelper.build(
                () -> new AdjacencyListGraphBuilder(true),
                3,
                new int[][] { { 1, 2 }, { 2, 3 } });

        Graph cloned = graph.clone();

        assertNotSame(graph, cloned);
        assertEquals(3, cloned.getVerticesCount());
        assertEquals(2, cloned.getEdgesCount());
    }

    @Test
    void testGetReversed() {
        DirectedGraph graph = (DirectedGraph) GraphTestHelper.build(
                () -> new AdjacencyListGraphBuilder(true),
                3,
                new int[][] { { 1, 2 }, { 2, 3 } });

        DirectedGraph reversed = graph.getReversed();

        assertEquals(0, reversed.getSuccessors(1).length);
        assertEquals(1, reversed.getSuccessors(2).length);
    }

    @Test
    void testAddEdgeDuplicateDirected() {
        Graph graph = GraphTestHelper.build(
                () -> new AdjacencyListGraphBuilder(true),
                2,
                new int[][] { { 1, 2 }, { 1, 2 }, { 1, 2 } });

        GraphHelper.assertM(graph, 1);
    }

    @Test
    void testAddEdgeDuplicateUndirected() {
        Graph graph = GraphTestHelper.build(
                () -> new AdjacencyListGraphBuilder(false),
                2,
                new int[][] { { 1, 2 }, { 1, 2 }, { 2, 1 } });

        GraphHelper.assertM(graph, 1);
    }

    @Test
    void testGetVerticesCount() {
        Graph graph = GraphTestHelper.build(
                () -> new AdjacencyListGraphBuilder(false),
                5,
                new int[][] { { 1, 2 }, { 3, 4 } });

        assertEquals(5, graph.getVerticesCount());
    }

    @Test
    void testGetEdgesCount() {
        Graph graph = GraphTestHelper.build(
                () -> new AdjacencyListGraphBuilder(true),
                3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 } });

        assertEquals(3, graph.getEdgesCount());
    }

    @Test
    void testGetInducedSubgraph() {
        Graph graph = GraphTestHelper.build(
                () -> new AdjacencyListGraphBuilder(true),
                5,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 4 }, { 4, 5 } });

        Graph subgraph = graph.getInducedSubgraph(new int[] { 1, 2, 3 });

        assertEquals(3, subgraph.getVerticesCount());
    }

    @Test
    void testWeightedGraphIsWeighted() {
        AdjacencyListGraphBuilder builder = new AdjacencyListGraphBuilder(false);
        builder.initialize(3, 2, true, false);
        builder.addEdge(1, 2, 5);
        builder.addEdge(2, 3, 10);
        Graph graph = builder.build();

        assertTrue(graph.isWeighted());
    }

    @Test
    void testGetEdgeWeightReturnsCorrectWeight() {
        AdjacencyListGraphBuilder builder = new AdjacencyListGraphBuilder(false);
        builder.initialize(3, 2, true, false);
        builder.addEdge(1, 2, 5);
        builder.addEdge(2, 3, 10);
        WeightedGraph graph = (WeightedGraph) builder.build();

        assertEquals(5, graph.getEdgeWeight(1, 2));
        assertEquals(10, graph.getEdgeWeight(2, 3));
    }

    @Test
    void testGetEdgeWeightThrowsForMissingEdge() {
        AdjacencyListGraphBuilder builder = new AdjacencyListGraphBuilder(true);
        builder.initialize(3, 1, true, false);
        builder.addEdge(1, 2, 5);
        WeightedGraph graph = (WeightedGraph) builder.build();

        assertThrows(IllegalArgumentException.class, () -> graph.getEdgeWeight(1, 3));
        assertThrows(IllegalArgumentException.class, () -> graph.getEdgeWeight(2, 1));
    }

    @Test
    void testIterateGraphPassesWeightsToVisitor() {
        AdjacencyListGraphBuilder builder = new AdjacencyListGraphBuilder(false);
        builder.initialize(3, 2, true, false);
        builder.addEdge(1, 2, 5);
        builder.addEdge(2, 3, 10);
        WeightedGraph graph = (WeightedGraph) builder.build();

        java.util.Map<String, Integer> edgeWeights = new java.util.HashMap<>();
        GraphBase.IteratorVisitor visitor = new GraphBase.IteratorVisitor() {
            @Override
            public void examineEdge(int v, int w, int weight) {
                edgeWeights.put(v + "->" + w, weight);
            }
        };
        graph.iterateGraph(visitor);

        assertEquals(5, edgeWeights.get("1->2").intValue());
        assertEquals(10, edgeWeights.get("2->3").intValue());
    }

    @Test
    void testGetWeightsSetReturnsWeightArray() {
        AdjacencyListGraphBuilder builder = new AdjacencyListGraphBuilder(true);
        builder.initialize(3, 2, true, false);
        builder.addEdge(1, 2, 5);
        builder.addEdge(2, 3, 10);
        WeightedGraph graph = (WeightedGraph) builder.build();

        int[] weights = graph.getWeightsSet();
        assertEquals(2, weights.length);
        assertArrayEquals(new int[] { 5, 10 }, weights);
    }

    @Test
    void testGetWeightedEdgesSetReturnsEdgesAndWeights() {
        AdjacencyListGraphBuilder builder = new AdjacencyListGraphBuilder(true);
        builder.initialize(3, 2, true, false);
        builder.addEdge(1, 2, 5);
        builder.addEdge(2, 3, 10);
        WeightedGraph graph = (WeightedGraph) builder.build();

        WeightedGraph.WeightedEdges result = graph.getWeightedEdgesSet();
        assertEquals(2, result.edges().length);
        assertEquals(2, result.weights().length);
        assertEquals(5, result.weights()[0]);
        assertEquals(10, result.weights()[1]);
    }

    @Test
    void testGetReversedPreservesWeights() {
        AdjacencyListGraphBuilder builder = new AdjacencyListGraphBuilder(true);
        builder.initialize(3, 2, true, false);
        builder.addEdge(1, 2, 5);
        builder.addEdge(2, 3, 10);
        DirectedGraph graph = (DirectedGraph) builder.build();

        DirectedGraph reversed = graph.getReversed();
        WeightedGraph weightedReversed = (WeightedGraph) reversed;

        assertEquals(5, weightedReversed.getEdgeWeight(2, 1));
        assertEquals(10, weightedReversed.getEdgeWeight(3, 2));
    }

    @Test
    void testGetInducedSubgraphPreservesWeights() {
        AdjacencyListGraphBuilder builder = new AdjacencyListGraphBuilder(true);
        builder.initialize(4, 3, true, false);
        builder.addEdge(1, 2, 5);
        builder.addEdge(2, 3, 10);
        builder.addEdge(3, 4, 15);
        WeightedGraph graph = (WeightedGraph) builder.build();

        WeightedGraph subgraph = (WeightedGraph) graph.getInducedSubgraph(new int[] { 1, 2, 3 });

        assertEquals(5, subgraph.getEdgeWeight(1, 2));
        assertEquals(10, subgraph.getEdgeWeight(2, 3));
        assertThrows(IllegalArgumentException.class, () -> subgraph.getEdgeWeight(3, 4));
    }

    @Test
    void testIntegrationLoadWeightedFileIntoAdjacencyList() throws Exception {
        java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("weighted_graph", ".txt");
        String content = "3 2\n1 2 5\n2 3 10\n";
        java.nio.file.Files.writeString(tempFile, content);

        Graph graph = GraphLoader.load(tempFile.toString(), "Adjacency List", true, true).graph();

        assertTrue(graph.isWeighted());
        WeightedGraph weightedGraph = (WeightedGraph) graph;
        assertEquals(5, weightedGraph.getEdgeWeight(1, 2));
        assertEquals(10, weightedGraph.getEdgeWeight(2, 3));
        assertEquals(2, weightedGraph.getWeightsSet().length);

        java.nio.file.Files.deleteIfExists(tempFile);
    }

    @Test
    void testUndirectedWeightedPerDirectionWeights() {
        AdjacencyListGraphBuilder builder = new AdjacencyListGraphBuilder(false);
        builder.initialize(3, 2, true, false);
        builder.addEdge(1, 2, 5);
        builder.addEdge(2, 1, 10);
        WeightedGraph graph = (WeightedGraph) builder.build();

        assertEquals(1, graph.getEdgesCount());
        assertEquals(5, graph.getEdgeWeight(1, 2));
        assertEquals(10, graph.getEdgeWeight(2, 1));
    }

    @Test
    void testUndirectedWeightedSameDirectionDuplicate() {
        AdjacencyListGraphBuilder builder = new AdjacencyListGraphBuilder(false);
        builder.initialize(3, 2, true, false);
        builder.addEdge(1, 2, 5);
        builder.addEdge(1, 2, 10);
        WeightedGraph graph = (WeightedGraph) builder.build();

        assertEquals(1, graph.getEdgesCount());
        assertEquals(10, graph.getEdgeWeight(1, 2));
    }

    @Test
    void testUndirectedUnweightedOppositeDirectionsDedup() {
        Graph graph = GraphTestHelper.build(
                () -> new AdjacencyListGraphBuilder(false),
                2,
                new int[][] { { 1, 2 }, { 2, 1 } });

        assertEquals(1, graph.getEdgesCount());
    }

    @Test
    void testFleuryEulerianCircuit() {
        Graph graph = GraphTestHelper.build(
                () -> new AdjacencyListGraphBuilder(false),
                3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 } });

        Fleury.EulerianPath result = Fleury.findEulerianPath(graph);

        assertNotNull(result);
        assertEquals(Fleury.EulerianType.EULERIAN, result.type());
        assertEquals(4, result.path().length);
    }

    @Test
    void testCloneVsOriginal() {
        Graph graph = GraphTestHelper.build(
                () -> new AdjacencyListGraphBuilder(false),
                7,
                new int[][] {
                        { 1, 2 }, { 1, 3 }, { 2, 3 }, { 2, 4 }, { 2, 5 },
                        { 3, 4 }, { 3, 6 }, { 4, 5 }, { 4, 6 }, { 5, 6 },
                        { 5, 7 }, { 6, 7 } });

        UndirectedGraph clone = (UndirectedGraph) graph.clone();

        System.out.println("Original edges before: " + graph.getEdgesCount());
        System.out.println("Clone edges before: " + clone.getEdgesCount());

        clone.removeEdge(1, 2);

        System.out.println("Original edges after: " + graph.getEdgesCount());
        System.out.println("Clone edges after: " + clone.getEdgesCount());

        assertEquals(12, graph.getEdgesCount());
        assertEquals(11, clone.getEdgesCount());
    }
}

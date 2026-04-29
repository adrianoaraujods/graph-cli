package graph.representations.adjacencylist;

import org.junit.jupiter.api.Test;

import graph.api.GraphHelper;
import graph.api.Graph;
import graph.representations.GraphBuilderHelper;

class AdjacencyListBuilderTest {

    @Test
    void testInitializeBasic() {
        Graph graph = GraphBuilderHelper.build(
                () -> new AdjacencyListGraphBuilder(true),
                10, 5,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 4 } });

        GraphHelper.assertN(graph, 10);
        GraphHelper.assertM(graph, 3);
    }

    @Test
    void testBuildWithUnusedCapacity() {
        Graph graph = GraphBuilderHelper.build(
                () -> new AdjacencyListGraphBuilder(true),
                5, 10,
                new int[][] { { 1, 2 }, { 2, 3 } });

        GraphHelper.assertM(graph, 2);
    }

    @Test
    void testUndirectedEdgesCount() {
        Graph graph = GraphBuilderHelper.build(
                () -> new AdjacencyListGraphBuilder(false),
                3, 2,
                new int[][] { { 1, 2 }, { 2, 3 } });

        GraphHelper.assertM(graph, 2);
    }

    @Test
    void testDirectedEdgesPreserved() {
        Graph graph = GraphBuilderHelper.build(
                () -> new AdjacencyListGraphBuilder(true),
                3, 3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 } });

        GraphHelper.assertM(graph, 3);
    }

    @Test
    void testNExpandsBeyondInitial() {
        Graph graph = GraphBuilderHelper.build(
                () -> new AdjacencyListGraphBuilder(true),
                3, 2,
                new int[][] { { 1, 2 }, { 4, 5 } });

        GraphHelper.assertN(graph, 5);
    }

    @Test
    void testEmptyGraph() {
        Graph graph = GraphBuilderHelper.build(
                () -> new AdjacencyListGraphBuilder(true),
                5, 0,
                new int[][] {});

        GraphHelper.assertN(graph, 5);
        GraphHelper.assertM(graph, 0);
    }

    @Test
    void testZeroVertices() {
        Graph graph = GraphBuilderHelper.build(
                () -> new AdjacencyListGraphBuilder(true),
                0, 0,
                new int[][] {});

        GraphHelper.assertN(graph, 0);
        GraphHelper.assertM(graph, 0);
    }

    @Test
    void testSingleVertex() {
        Graph graph = GraphBuilderHelper.build(
                () -> new AdjacencyListGraphBuilder(true),
                1, 0,
                new int[][] {});

        GraphHelper.assertN(graph, 1);
        GraphHelper.assertM(graph, 0);
    }

    @Test
    void testVerticesUndirected() {
        Graph graph = GraphBuilderHelper.build(
                () -> new AdjacencyListGraphBuilder(false),
                5, 3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 } });

        GraphHelper.assertVertices(graph, 1, 2, 3);
    }
}
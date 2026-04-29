package graph.representations.adjacencymatrix;

import org.junit.jupiter.api.Test;

import graph.util.GraphHelper;
import graph.api.Graph;
import graph.util.GraphTestHelper;

class AdjacencyMatrixBuilderTest {

    @Test
    void testInitializeBasic() {
        Graph graph = GraphTestHelper.build(
                () -> new AdjacencyMatrixGraphBuilder(true),
                10,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 4 } });

        GraphHelper.assertN(graph, 10);
        GraphHelper.assertM(graph, 3);
    }

    @Test
    void testBuildWithUnusedCapacity() {
        Graph graph = GraphTestHelper.build(
                () -> new AdjacencyMatrixGraphBuilder(true),
                5,
                new int[][] { { 1, 2 }, { 2, 3 } });

        GraphHelper.assertM(graph, 2);
    }

    @Test
    void testUndirectedEdgesPreserved() {
        Graph graph = GraphTestHelper.build(
                () -> new AdjacencyMatrixGraphBuilder(false),
                3,
                new int[][] { { 1, 2 }, { 2, 3 } });

        GraphHelper.assertM(graph, 2);
    }

    @Test
    void testDirectedEdgesPreserved() {
        Graph graph = GraphTestHelper.build(
                () -> new AdjacencyMatrixGraphBuilder(true),
                3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 } });

        GraphHelper.assertM(graph, 3);
    }

    @Test
    void testEmptyGraph() {
        Graph graph = GraphTestHelper.build(
                () -> new AdjacencyMatrixGraphBuilder(true),
                5,
                new int[][] {});

        GraphHelper.assertN(graph, 5);
        GraphHelper.assertM(graph, 0);
    }

    @Test
    void testZeroVertices() {
        Graph graph = GraphTestHelper.build(
                () -> new AdjacencyMatrixGraphBuilder(true),
                0,
                new int[][] {});

        GraphHelper.assertN(graph, 0);
        GraphHelper.assertM(graph, 0);
    }

    @Test
    void testSingleVertex() {
        Graph graph = GraphTestHelper.build(
                () -> new AdjacencyMatrixGraphBuilder(true),
                1,
                new int[][] {});

        GraphHelper.assertN(graph, 1);
        GraphHelper.assertM(graph, 0);
    }

    @Test
    void testVerticesUndirected() {
        Graph graph = GraphTestHelper.build(
                () -> new AdjacencyMatrixGraphBuilder(false),
                3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 } });

        GraphHelper.assertVertices(graph, 1, 2, 3);
    }
}

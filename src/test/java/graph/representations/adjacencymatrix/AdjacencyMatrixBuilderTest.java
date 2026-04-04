package graph.representations.adjacencymatrix;

import org.junit.jupiter.api.Test;

import graph.api.StaticGraphHelper;
import graph.api.StaticGraph;
import graph.representations.GraphBuilderHelper;

class AdjacencyMatrixBuilderTest {

    @Test
    void testInitializeBasic() {
        StaticGraph graph = GraphBuilderHelper.build(
                () -> new AdjacencyMatrixGraphBuilder(true),
                10, 5,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 4 } });

        StaticGraphHelper.assertN(graph, 10);
        StaticGraphHelper.assertM(graph, 3);
    }

    @Test
    void testInitializeWithVertices() {
        StaticGraph graph = GraphBuilderHelper.build(
                () -> new AdjacencyMatrixGraphBuilder(true),
                7, 2,
                new int[][] { { 1, 3 }, { 5, 7 } },
                new int[] { 1, 3, 5, 7 });

        StaticGraphHelper.assertN(graph, 7);
        StaticGraphHelper.assertVertices(graph, 1, 3, 5, 7);
    }

    @Test
    void testBuildWithUnusedCapacity() {
        StaticGraph graph = GraphBuilderHelper.build(
                () -> new AdjacencyMatrixGraphBuilder(true),
                5, 10,
                new int[][] { { 1, 2 }, { 2, 3 } });

        StaticGraphHelper.assertM(graph, 2);
    }

    @Test
    void testUndirectedEdgesPreserved() {
        StaticGraph graph = GraphBuilderHelper.build(
                () -> new AdjacencyMatrixGraphBuilder(false),
                3, 2,
                new int[][] { { 1, 2 }, { 2, 3 } });

        StaticGraphHelper.assertM(graph, 2);
    }

    @Test
    void testDirectedEdgesPreserved() {
        StaticGraph graph = GraphBuilderHelper.build(
                () -> new AdjacencyMatrixGraphBuilder(true),
                3, 3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 } });

        StaticGraphHelper.assertM(graph, 3);
    }

    @Test
    void testEmptyGraph() {
        StaticGraph graph = GraphBuilderHelper.build(
                () -> new AdjacencyMatrixGraphBuilder(true),
                5, 0,
                new int[][] {});

        StaticGraphHelper.assertN(graph, 5);
        StaticGraphHelper.assertM(graph, 0);
    }

    @Test
    void testZeroVertices() {
        StaticGraph graph = GraphBuilderHelper.build(
                () -> new AdjacencyMatrixGraphBuilder(true),
                0, 0,
                new int[][] {});

        StaticGraphHelper.assertN(graph, 0);
        StaticGraphHelper.assertM(graph, 0);
    }

    @Test
    void testSingleVertex() {
        StaticGraph graph = GraphBuilderHelper.build(
                () -> new AdjacencyMatrixGraphBuilder(true),
                1, 0,
                new int[][] {});

        StaticGraphHelper.assertN(graph, 1);
        StaticGraphHelper.assertM(graph, 0);
    }

    @Test
    void testVerticesUndirected() {
        StaticGraph graph = GraphBuilderHelper.build(
                () -> new AdjacencyMatrixGraphBuilder(false),
                5, 3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 } });

        StaticGraphHelper.assertVertices(graph, 1, 2, 3, 4, 5);
    }
}

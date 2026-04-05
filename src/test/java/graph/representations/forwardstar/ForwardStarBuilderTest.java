package graph.representations.forwardstar;

import org.junit.jupiter.api.Test;

import graph.api.GraphHelper;
import graph.api.Graph;
import graph.representations.GraphBuilderHelper;

class ForwardStarBuilderTest {

    @Test
    void testInitializeBasic() {
        Graph graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                10, 5,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 4 } });

        GraphHelper.assertN(graph, 10);
        GraphHelper.assertM(graph, 3);
    }

    @Test
    void testInitializeWithVertices() {
        Graph graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                7, 2,
                new int[][] { { 1, 3 }, { 5, 7 } },
                new int[] { 1, 3, 5, 7 });

        GraphHelper.assertN(graph, 7);
        GraphHelper.assertVertices(graph, 1, 3, 5, 7);
    }

    @Test
    void testBuildWithUnusedCapacity() {
        Graph graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                5, 10,
                new int[][] { { 1, 2 }, { 2, 3 } });

        GraphHelper.assertM(graph, 2);
    }

    @Test
    void testUndirectedEdgesDoubled() {
        Graph graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(false),
                3, 2,
                new int[][] { { 1, 2 }, { 2, 3 } });

        GraphHelper.assertM(graph, 4);
    }

    @Test
    void testDirectedEdgesPreserved() {
        Graph graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                3, 3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 } });

        GraphHelper.assertM(graph, 3);
    }

    @Test
    void testNExpandsBeyondInitial() {
        Graph graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                3, 2,
                new int[][] { { 1, 2 }, { 4, 5 } });

        GraphHelper.assertN(graph, 5);
    }

    @Test
    void testEmptyGraph() {
        Graph graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                5, 0,
                new int[][] {});

        GraphHelper.assertN(graph, 5);
        GraphHelper.assertM(graph, 0);
    }

    @Test
    void testZeroVertices() {
        Graph graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                0, 0,
                new int[][] {});

        GraphHelper.assertN(graph, 0);
        GraphHelper.assertM(graph, 0);
    }

    @Test
    void testSingleVertex() {
        Graph graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                1, 0,
                new int[][] {});

        GraphHelper.assertN(graph, 1);
        GraphHelper.assertM(graph, 0);
    }

    @Test
    void testVerticesUndirected() {
        Graph graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(false),
                5, 3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 } });

        GraphHelper.assertVertices(graph, 1, 2, 3, 4, 5);
    }
}
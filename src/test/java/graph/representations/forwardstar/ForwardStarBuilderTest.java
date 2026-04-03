package graph.representations.forwardstar;

import org.junit.jupiter.api.Test;

import graph.api.StaticGraphHelper;
import graph.api.StaticGraph;
import graph.representations.GraphBuilderHelper;

class ForwardStarBuilderTest {

    @Test
    void testInitializeBasic() {
        StaticGraph graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                10, 5,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 4 } });

        StaticGraphHelper.assertN(graph, 10);
        StaticGraphHelper.assertM(graph, 3);
    }

    @Test
    void testInitializeWithVertices() {
        StaticGraph graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                7, 2,
                new int[][] { { 1, 3 }, { 5, 7 } },
                new int[] { 1, 3, 5, 7 });

        StaticGraphHelper.assertN(graph, 7);
        StaticGraphHelper.assertVertices(graph, 1, 3, 5, 7);
    }

    @Test
    void testBuildWithUnusedCapacity() {
        StaticGraph graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                5, 10,
                new int[][] { { 1, 2 }, { 2, 3 } });

        StaticGraphHelper.assertM(graph, 2);
    }

    @Test
    void testUndirectedEdgesDoubled() {
        StaticGraph graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(false),
                3, 2,
                new int[][] { { 1, 2 }, { 2, 3 } });

        StaticGraphHelper.assertM(graph, 4);
    }

    @Test
    void testDirectedEdgesPreserved() {
        StaticGraph graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                3, 3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 } });

        StaticGraphHelper.assertM(graph, 3);
    }

    @Test
    void testNExpandsBeyondInitial() {
        StaticGraph graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                3, 2,
                new int[][] { { 1, 2 }, { 4, 5 } });

        StaticGraphHelper.assertN(graph, 5);
    }

    @Test
    void testEmptyGraph() {
        StaticGraph graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                5, 0,
                new int[][] {});

        StaticGraphHelper.assertN(graph, 5);
        StaticGraphHelper.assertM(graph, 0);
    }

    @Test
    void testZeroVertices() {
        StaticGraph graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                0, 0,
                new int[][] {});

        StaticGraphHelper.assertN(graph, 0);
        StaticGraphHelper.assertM(graph, 0);
    }

    @Test
    void testSingleVertex() {
        StaticGraph graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                1, 0,
                new int[][] {});

        StaticGraphHelper.assertN(graph, 1);
        StaticGraphHelper.assertM(graph, 0);
    }

    @Test
    void testVerticesUndirected() {
        StaticGraph graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(false),
                5, 3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 } });

        StaticGraphHelper.assertVertices(graph, 1, 2, 3, 4, 5);
    }
}
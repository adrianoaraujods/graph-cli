package graph.representations.forwardstar;

import org.junit.jupiter.api.Test;

import graph.api.GraphRepresentation;
import graph.api.GraphRepresentationHelper;
import graph.representations.GraphBuilderHelper;

class ForwardStarBuilderTest {

    @Test
    void testInitializeBasic() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                10, 5,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 4 } });

        GraphRepresentationHelper.assertN(graph, 10);
        GraphRepresentationHelper.assertM(graph, 3);
    }

    @Test
    void testInitializeWithVertices() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                7, 2,
                new int[][] { { 1, 3 }, { 5, 7 } },
                new int[] { 1, 3, 5, 7 });

        GraphRepresentationHelper.assertN(graph, 7);
        GraphRepresentationHelper.assertVertices(graph, 1, 3, 5, 7);
    }

    @Test
    void testBuildWithUnusedCapacity() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                5, 10,
                new int[][] { { 1, 2 }, { 2, 3 } });

        GraphRepresentationHelper.assertM(graph, 2);
    }

    @Test
    void testUndirectedEdgesDoubled() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(false),
                3, 2,
                new int[][] { { 1, 2 }, { 2, 3 } });

        GraphRepresentationHelper.assertM(graph, 4);
    }

    @Test
    void testDirectedEdgesPreserved() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                3, 3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 } });

        GraphRepresentationHelper.assertM(graph, 3);
    }

    @Test
    void testNExpandsBeyondInitial() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                3, 2,
                new int[][] { { 1, 2 }, { 4, 5 } });

        GraphRepresentationHelper.assertN(graph, 5);
    }

    @Test
    void testEmptyGraph() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                5, 0,
                new int[][] {});

        GraphRepresentationHelper.assertN(graph, 5);
        GraphRepresentationHelper.assertM(graph, 0);
    }

    @Test
    void testZeroVertices() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                0, 0,
                new int[][] {});

        GraphRepresentationHelper.assertN(graph, 0);
        GraphRepresentationHelper.assertM(graph, 0);
    }

    @Test
    void testSingleVertex() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                1, 0,
                new int[][] {});

        GraphRepresentationHelper.assertN(graph, 1);
        GraphRepresentationHelper.assertM(graph, 0);
    }

    @Test
    void testVerticesUndirected() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(false),
                5, 3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 } });

        GraphRepresentationHelper.assertVertices(graph, 1, 2, 3, 4, 5);
    }
}
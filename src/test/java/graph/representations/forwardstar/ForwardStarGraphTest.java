package graph.representations.forwardstar;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import graph.api.GraphRepresentation;
import graph.api.GraphRepresentationHelper;
import graph.representations.GraphBuilderHelper;

class ForwardStarGraphTest {

    @Test
    void testGetVerticesDefaultRange() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(false),
                5, 2,
                new int[][] { { 1, 2 }, { 3, 4 } });

        GraphRepresentationHelper.assertVertices(graph, 1, 2, 3, 4, 5);
    }

    @Test
    void testIterateGraphAllEdgesVisited() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4, 3,
                new int[][] { { 1, 2 }, { 1, 3 }, { 2, 4 } });

        GraphRepresentationHelper.assertIterateVertexCount(graph, 4);
        GraphRepresentationHelper.assertIterateEdgeCount(graph, 3);
    }

    @Test
    void testGetDegreeUndirected() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(false),
                4, 3,
                new int[][] { { 1, 2 }, { 1, 3 }, { 1, 4 } });

        GraphRepresentationHelper.assertDegree(graph, 1, 3);
        GraphRepresentationHelper.assertDegree(graph, 2, 1);
        GraphRepresentationHelper.assertDegree(graph, 3, 1);
        GraphRepresentationHelper.assertDegree(graph, 4, 1);
    }

    @Test
    void testGetInDegreeDirected() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4, 4,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 2 }, { 4, 2 } });

        GraphRepresentationHelper.assertInDegree(graph, 1, 0);
        GraphRepresentationHelper.assertInDegree(graph, 2, 3);
        GraphRepresentationHelper.assertInDegree(graph, 3, 1);
        GraphRepresentationHelper.assertInDegree(graph, 4, 0);
    }

    @Test
    void testGetOutDegreeDirected() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4, 4,
                new int[][] { { 1, 2 }, { 1, 3 }, { 2, 4 }, { 3, 4 } });

        GraphRepresentationHelper.assertOutDegree(graph, 1, 2);
        GraphRepresentationHelper.assertOutDegree(graph, 2, 1);
        GraphRepresentationHelper.assertOutDegree(graph, 3, 1);
        GraphRepresentationHelper.assertOutDegree(graph, 4, 0);
    }

    @Test
    void testGetSuccessors() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                5, 5,
                new int[][] { { 1, 2 }, { 1, 3 }, { 2, 4 }, { 2, 5 }, { 3, 5 } });

        GraphRepresentationHelper.assertSuccessors(graph, 1, 2, 3);
        GraphRepresentationHelper.assertSuccessors(graph, 2, 4, 5);
    }

    @Test
    void testGetPredecessors() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                5, 6,
                new int[][] { { 1, 3 }, { 2, 3 }, { 3, 4 }, { 3, 5 }, { 4, 5 }, { 2, 5 } });

        GraphRepresentationHelper.assertPredecessors(graph, 3, 1, 2);
        GraphRepresentationHelper.assertPredecessors(graph, 5, 2, 3, 4);
    }

    @Test
    void testGetNeighborsUndirected() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(false),
                5, 4,
                new int[][] { { 1, 2 }, { 1, 3 }, { 2, 4 }, { 5, 1 } });

        GraphRepresentationHelper.assertNeighbors(graph, 1, 2, 3, 5);
        GraphRepresentationHelper.assertNeighbors(graph, 2, 1, 4);
    }

    @Test
    void testGetInducedSubgraph() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                6, 6,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 }, { 3, 4 }, { 4, 5 }, { 5, 6 } });

        GraphRepresentationHelper.assertInducedSubgraphN(graph, new int[] { 1, 2, 3 }, 3);
        GraphRepresentationHelper.assertInducedSubgraphM(graph, new int[] { 1, 2, 3 }, 3);
    }

    @Test
    void testGetReversedDirected() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4, 4,
                new int[][] { { 1, 2 }, { 1, 3 }, { 2, 4 }, { 3, 4 } });

        GraphRepresentationHelper.assertReversedM(graph, 4);
        GraphRepresentationHelper.assertReversedHasEdge(graph, 2, 1);
        GraphRepresentationHelper.assertReversedHasEdge(graph, 4, 2);
        GraphRepresentationHelper.assertReversedHasEdge(graph, 4, 3);
    }

    @Test
    void testInvalidVertexArgumentGetDegree() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                5, 1,
                new int[][] { { 1, 2 } });

        GraphRepresentationHelper.assertThrowsIAE(() -> graph.getDegree(0));
        GraphRepresentationHelper.assertThrowsIAE(() -> graph.getDegree(6));
    }

    @Test
    void testInvalidVertexArgumentGetInDegree() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                5, 1,
                new int[][] { { 1, 2 } });

        GraphRepresentationHelper.assertThrowsIAE(() -> graph.getInDegree(0));
        GraphRepresentationHelper.assertThrowsIAE(() -> graph.getInDegree(6));
    }

    @Test
    void testInvalidVertexArgumentGetOutDegree() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                5, 1,
                new int[][] { { 1, 2 } });

        GraphRepresentationHelper.assertThrowsIAE(() -> graph.getOutDegree(0));
        GraphRepresentationHelper.assertThrowsIAE(() -> graph.getOutDegree(6));
    }

    @Test
    void testInvalidVertexArgumentGetSuccessors() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                5, 1,
                new int[][] { { 1, 2 } });

        GraphRepresentationHelper.assertThrowsIAE(() -> graph.getSuccessors(0));
        GraphRepresentationHelper.assertThrowsIAE(() -> graph.getSuccessors(6));
    }

    @Test
    void testInvalidVertexArgumentGetPredecessors() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                5, 1,
                new int[][] { { 1, 2 } });

        GraphRepresentationHelper.assertThrowsIAE(() -> graph.getPredecessors(0));
        GraphRepresentationHelper.assertThrowsIAE(() -> graph.getPredecessors(6));
    }

    @Test
    void testEmptyGraph() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(false),
                5, 0,
                new int[][] {});

        assertEquals(5, graph.n);
        assertEquals(0, graph.m);
        GraphRepresentationHelper.assertDegree(graph, 1, 0);
    }

    @Test
    void testSingleVertexGraph() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                1, 0,
                new int[][] {});

        assertEquals(1, graph.n);
        assertEquals(0, graph.m);
    }

    @Test
    void testIterateGraphWithNoEdges() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(false),
                5, 0,
                new int[][] {});

        GraphRepresentationHelper.assertIterateVertexCount(graph, 5);
        GraphRepresentationHelper.assertIterateEdgeCount(graph, 0);
    }

    @Test
    void testGetVerticesWithCustomArray() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                7, 2,
                new int[][] { { 1, 3 }, { 5, 7 } },
                new int[] { 1, 3, 5, 7 });

        GraphRepresentationHelper.assertVertices(graph, 1, 3, 5, 7);
    }
}
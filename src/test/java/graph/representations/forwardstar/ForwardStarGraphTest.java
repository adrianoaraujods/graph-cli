package graph.representations.forwardstar;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import graph.api.DirectedGraph;
import graph.api.StaticGraph;
import graph.api.StaticGraphHelper;
import graph.api.UndirectedGraph;
import graph.representations.GraphBuilderHelper;

class ForwardStarGraphTest {

    @Test
    void testGetVerticesDefaultRange() {
        StaticGraph graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(false),
                5, 2,
                new int[][] { { 1, 2 }, { 3, 4 } });

        StaticGraphHelper.assertVertices(graph, 1, 2, 3, 4, 5);
    }

    @Test
    void testIterateGraphAllEdgesVisited() {
        StaticGraph graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4, 3,
                new int[][] { { 1, 2 }, { 1, 3 }, { 2, 4 } });

        StaticGraphHelper.assertIterateVertexCount(graph, 4);
        StaticGraphHelper.assertIterateEdgeCount(graph, 3);
    }

    @Test
    void testGetDegreeUndirected() {
        UndirectedGraph graph = (UndirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(false),
                4, 3,
                new int[][] { { 1, 2 }, { 1, 3 }, { 1, 4 } });

        StaticGraphHelper.assertDegree(graph, 1, 3);
        StaticGraphHelper.assertDegree(graph, 2, 1);
        StaticGraphHelper.assertDegree(graph, 3, 1);
        StaticGraphHelper.assertDegree(graph, 4, 1);
    }

    @Test
    void testGetInDegreeDirected() {
        DirectedGraph graph = (DirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4, 4,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 2 }, { 4, 2 } });

        StaticGraphHelper.assertInDegree(graph, 1, 0);
        StaticGraphHelper.assertInDegree(graph, 2, 3);
        StaticGraphHelper.assertInDegree(graph, 3, 1);
        StaticGraphHelper.assertInDegree(graph, 4, 0);
    }

    @Test
    void testGetOutDegreeDirected() {
        DirectedGraph graph = (DirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4, 4,
                new int[][] { { 1, 2 }, { 1, 3 }, { 2, 4 }, { 3, 4 } });

        StaticGraphHelper.assertOutDegree(graph, 1, 2);
        StaticGraphHelper.assertOutDegree(graph, 2, 1);
        StaticGraphHelper.assertOutDegree(graph, 3, 1);
        StaticGraphHelper.assertOutDegree(graph, 4, 0);
    }

    @Test
    void testGetSuccessors() {
        DirectedGraph graph = (DirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                5, 5,
                new int[][] { { 1, 2 }, { 1, 3 }, { 2, 4 }, { 2, 5 }, { 3, 5 } });

        StaticGraphHelper.assertSuccessors(graph, 1, 2, 3);
        StaticGraphHelper.assertSuccessors(graph, 2, 4, 5);
    }

    @Test
    void testGetPredecessors() {
        DirectedGraph graph = (DirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                5, 6,
                new int[][] { { 1, 3 }, { 2, 3 }, { 3, 4 }, { 3, 5 }, { 4, 5 }, { 2, 5 } });

        StaticGraphHelper.assertPredecessors(graph, 3, 1, 2);
        StaticGraphHelper.assertPredecessors(graph, 5, 2, 3, 4);
    }

    @Test
    void testGetNeighborsUndirected() {
        UndirectedGraph graph = (UndirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(false),
                5, 4,
                new int[][] { { 1, 2 }, { 1, 3 }, { 2, 4 }, { 5, 1 } });

        StaticGraphHelper.assertNeighbors(graph, 1, 2, 3, 5);
        StaticGraphHelper.assertNeighbors(graph, 2, 1, 4);
    }

    @Test
    void testGetInducedSubgraph() {
        StaticGraph graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                6, 6,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 }, { 3, 4 }, { 4, 5 }, { 5, 6 } });

        StaticGraphHelper.assertInducedSubgraphN(graph, new int[] { 1, 2, 3 }, 3);
        StaticGraphHelper.assertInducedSubgraphM(graph, new int[] { 1, 2, 3 }, 3);
    }

    @Test
    void testGetReversedDirected() {
        DirectedGraph graph = (DirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4, 4,
                new int[][] { { 1, 2 }, { 1, 3 }, { 2, 4 }, { 3, 4 } });

        StaticGraphHelper.assertReversedM(graph, 4);
        StaticGraphHelper.assertReversedHasEdge(graph, 2, 1);
        StaticGraphHelper.assertReversedHasEdge(graph, 4, 2);
        StaticGraphHelper.assertReversedHasEdge(graph, 4, 3);
    }

    @Test
    void testInvalidVertexArgumentGetDegree() {
        UndirectedGraph graph = (UndirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                5, 1,
                new int[][] { { 1, 2 } });

        StaticGraphHelper.assertThrowsIAE(() -> graph.getDegree(0));
        StaticGraphHelper.assertThrowsIAE(() -> graph.getDegree(6));
    }

    @Test
    void testInvalidVertexArgumentGetInDegree() {
        DirectedGraph graph = (DirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                5, 1,
                new int[][] { { 1, 2 } });

        StaticGraphHelper.assertThrowsIAE(() -> graph.getInDegree(0));
        StaticGraphHelper.assertThrowsIAE(() -> graph.getInDegree(6));
    }

    @Test
    void testInvalidVertexArgumentGetOutDegree() {
        DirectedGraph graph = (DirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                5, 1,
                new int[][] { { 1, 2 } });

        StaticGraphHelper.assertThrowsIAE(() -> graph.getOutDegree(0));
        StaticGraphHelper.assertThrowsIAE(() -> graph.getOutDegree(6));
    }

    @Test
    void testInvalidVertexArgumentGetSuccessors() {
        DirectedGraph graph = (DirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                5, 1,
                new int[][] { { 1, 2 } });

        StaticGraphHelper.assertThrowsIAE(() -> graph.getSuccessors(0));
        StaticGraphHelper.assertThrowsIAE(() -> graph.getSuccessors(6));
    }

    @Test
    void testInvalidVertexArgumentGetPredecessors() {
        DirectedGraph graph = (DirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                5, 1,
                new int[][] { { 1, 2 } });

        StaticGraphHelper.assertThrowsIAE(() -> graph.getPredecessors(0));
        StaticGraphHelper.assertThrowsIAE(() -> graph.getPredecessors(6));
    }

    @Test
    void testEmptyGraph() {
        StaticGraph graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(false),
                5, 0,
                new int[][] {});

        assertEquals(5, graph.n);
        assertEquals(0, graph.m);
        StaticGraphHelper.assertDegree((UndirectedGraph) graph, 1, 0);
    }

    @Test
    void testSingleVertexGraph() {
        StaticGraph graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                1, 0,
                new int[][] {});

        assertEquals(1, graph.n);
        assertEquals(0, graph.m);
    }

    @Test
    void testIterateGraphWithNoEdges() {
        StaticGraph graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(false),
                5, 0,
                new int[][] {});

        StaticGraphHelper.assertIterateVertexCount(graph, 5);
        StaticGraphHelper.assertIterateEdgeCount(graph, 0);
    }

    @Test
    void testGetVerticesWithCustomArray() {
        StaticGraph graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                7, 2,
                new int[][] { { 1, 3 }, { 5, 7 } },
                new int[] { 1, 3, 5, 7 });

        StaticGraphHelper.assertVertices(graph, 1, 3, 5, 7);
    }
}
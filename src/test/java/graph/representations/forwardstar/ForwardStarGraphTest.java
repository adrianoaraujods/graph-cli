package graph.representations.forwardstar;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import graph.api.DirectedGraph;
import graph.api.Graph;
import graph.api.GraphHelper;
import graph.api.UndirectedGraph;
import graph.representations.GraphBuilderHelper;

class ForwardStarGraphTest {

    @Test
    void testGetVerticesDefaultRange() {
        Graph graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(false),
                5, 2,
                new int[][] { { 1, 2 }, { 3, 4 } });

        GraphHelper.assertVertices(graph, 1, 2, 3, 4);
    }

    @Test
    void testGetDegreeUndirected() {
        UndirectedGraph graph = (UndirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(false),
                4, 3,
                new int[][] { { 1, 2 }, { 1, 3 }, { 1, 4 } });

        GraphHelper.assertDegree(graph, 1, 3);
        GraphHelper.assertDegree(graph, 2, 1);
        GraphHelper.assertDegree(graph, 3, 1);
        GraphHelper.assertDegree(graph, 4, 1);
    }

    @Test
    void testGetInDegreeDirected() {
        DirectedGraph graph = (DirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4, 4,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 2 }, { 4, 2 } });

        GraphHelper.assertInDegree(graph, 1, 0);
        GraphHelper.assertInDegree(graph, 2, 3);
        GraphHelper.assertInDegree(graph, 3, 1);
        GraphHelper.assertInDegree(graph, 4, 0);
    }

    @Test
    void testGetOutDegreeDirected() {
        DirectedGraph graph = (DirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4, 4,
                new int[][] { { 1, 2 }, { 1, 3 }, { 2, 4 }, { 3, 4 } });

        GraphHelper.assertOutDegree(graph, 1, 2);
        GraphHelper.assertOutDegree(graph, 2, 1);
        GraphHelper.assertOutDegree(graph, 3, 1);
        GraphHelper.assertOutDegree(graph, 4, 0);
    }

    @Test
    void testGetSuccessors() {
        DirectedGraph graph = (DirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                5, 5,
                new int[][] { { 1, 2 }, { 1, 3 }, { 2, 4 }, { 2, 5 }, { 3, 5 } });

        GraphHelper.assertSuccessors(graph, 1, 2, 3);
        GraphHelper.assertSuccessors(graph, 2, 4, 5);
    }

    @Test
    void testGetPredecessors() {
        DirectedGraph graph = (DirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                5, 6,
                new int[][] { { 1, 3 }, { 2, 3 }, { 3, 4 }, { 3, 5 }, { 4, 5 }, { 2, 5 } });

        GraphHelper.assertPredecessors(graph, 3, 1, 2);
        GraphHelper.assertPredecessors(graph, 5, 2, 3, 4);
    }

    @Test
    void testGetNeighborsUndirected() {
        UndirectedGraph graph = (UndirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(false),
                5, 4,
                new int[][] { { 1, 2 }, { 1, 3 }, { 2, 4 }, { 5, 1 } });

        GraphHelper.assertNeighbors(graph, 1, 2, 3, 5);
        GraphHelper.assertNeighbors(graph, 2, 1, 4);
    }

    @Test
    void testGetInducedSubgraph() {
        Graph graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                6, 6,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 }, { 3, 4 }, { 4, 5 }, { 5, 6 } });

        GraphHelper.assertInducedSubgraphN(graph, new int[] { 1, 2, 3 }, 3);
        GraphHelper.assertInducedSubgraphM(graph, new int[] { 1, 2, 3 }, 3);
    }

    @Test
    void testGetReversedDirected() {
        DirectedGraph graph = (DirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4, 4,
                new int[][] { { 1, 2 }, { 1, 3 }, { 2, 4 }, { 3, 4 } });

        GraphHelper.assertReversedM(graph, 4);
        GraphHelper.assertReversedHasEdge(graph, 2, 1);
        GraphHelper.assertReversedHasEdge(graph, 4, 2);
        GraphHelper.assertReversedHasEdge(graph, 4, 3);
    }

    @Test
    void testInvalidVertexArgumentGetDegree() {
        UndirectedGraph graph = (UndirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                5, 1,
                new int[][] { { 1, 2 } });

        GraphHelper.assertThrowsIAE(() -> graph.getDegree(0));
        GraphHelper.assertThrowsIAE(() -> graph.getDegree(6));
    }

    @Test
    void testInvalidVertexArgumentGetInDegree() {
        DirectedGraph graph = (DirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                5, 1,
                new int[][] { { 1, 2 } });

        GraphHelper.assertThrowsIAE(() -> graph.getInDegree(0));
        GraphHelper.assertThrowsIAE(() -> graph.getInDegree(6));
    }

    @Test
    void testInvalidVertexArgumentGetOutDegree() {
        DirectedGraph graph = (DirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                5, 1,
                new int[][] { { 1, 2 } });

        GraphHelper.assertThrowsIAE(() -> graph.getOutDegree(0));
        GraphHelper.assertThrowsIAE(() -> graph.getOutDegree(6));
    }

    @Test
    void testInvalidVertexArgumentGetSuccessors() {
        DirectedGraph graph = (DirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                5, 1,
                new int[][] { { 1, 2 } });

        GraphHelper.assertThrowsIAE(() -> graph.getSuccessors(0));
        GraphHelper.assertThrowsIAE(() -> graph.getSuccessors(6));
    }

    @Test
    void testInvalidVertexArgumentGetPredecessors() {
        DirectedGraph graph = (DirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                5, 1,
                new int[][] { { 1, 2 } });

        GraphHelper.assertThrowsIAE(() -> graph.getPredecessors(0));
        GraphHelper.assertThrowsIAE(() -> graph.getPredecessors(6));
    }

    @Test
    void testEmptyGraph() {
        Graph graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(false),
                5, 0,
                new int[][] {});

        assertEquals(5, graph.getVerticesCount());
        assertEquals(0, graph.getEdgesCount());
        GraphHelper.assertDegree((UndirectedGraph) graph, 1, 0);
    }

    @Test
    void testSingleVertexGraph() {
        Graph graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                1, 0,
                new int[][] {});

        assertEquals(1, graph.getVerticesCount());
        assertEquals(0, graph.getEdgesCount());
    }

    @Test
    void testAddEdgeUndirected() {
        UndirectedGraph graph = (UndirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(false),
                4, 2,
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
        DirectedGraph graph = (DirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4, 2,
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
        Graph graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(false),
                3, 1,
                new int[][] { { 1, 2 } });

        assertEquals(1, graph.getEdgesCount());

        graph.addEdge(2, 3);

        assertEquals(2, graph.getEdgesCount());
    }

    @Test
    void testRemoveEdgeUndirected() {
        UndirectedGraph graph = (UndirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(false),
                4, 3,
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
        DirectedGraph graph = (DirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4, 3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 4 } });

        GraphHelper.assertOutDegree(graph, 2, 1);
        GraphHelper.assertInDegree(graph, 3, 1);

        graph.removeEdge(2, 3);

        GraphHelper.assertOutDegree(graph, 2, 0);
        GraphHelper.assertInDegree(graph, 3, 0);
    }

    @Test
    void testRemoveEdgeUpdatesEdgeCount() {
        Graph graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(false),
                3, 2,
                new int[][] { { 1, 2 }, { 2, 3 } });

        assertEquals(2, graph.getEdgesCount());

        graph.removeEdge(1, 2);

        assertEquals(1, graph.getEdgesCount());
    }

    @Test
    void testRemoveEdgeNonExistent() {
        UndirectedGraph graph = (UndirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(false),
                4, 2,
                new int[][] { { 1, 2 }, { 3, 4 } });

        graph.removeEdge(1, 4);

        GraphHelper.assertDegree(graph, 1, 1);
        GraphHelper.assertDegree(graph, 4, 1);
        GraphHelper.assertNeighbors(graph, 1, 2);
        GraphHelper.assertNeighbors(graph, 4, 3);
    }

    @Test
    void testRemoveEdgeWithMultipleEdges() {
        UndirectedGraph graph = (UndirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(false),
                4, 4,
                new int[][] { { 1, 3 }, { 2, 3 }, { 3, 4 }, { 1, 4 } });

        graph.removeEdge(1, 3);

        GraphHelper.assertNeighbors(graph, 3, 2, 4);
        GraphHelper.assertNeighbors(graph, 1, 4);
    }
}
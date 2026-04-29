package graph.representations.adjacencymatrix;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import graph.api.DirectedGraph;
import graph.api.Graph;
import graph.util.GraphHelper;
import graph.api.UndirectedGraph;
import graph.util.GraphTestHelper;

class AdjacencyMatrixGraphTest {

    @Test
    void testGetVerticesDefaultRange() {
        Graph graph = GraphTestHelper.build(
                () -> new AdjacencyMatrixGraphBuilder(false),
                5,
                new int[][] { { 1, 2 }, { 3, 4 } });

        GraphHelper.assertVertices(graph, 1, 2, 3, 4);
    }

    @Test
    void testGetDegreeUndirected() {
        UndirectedGraph graph = (UndirectedGraph) GraphTestHelper.build(
                () -> new AdjacencyMatrixGraphBuilder(false),
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
                () -> new AdjacencyMatrixGraphBuilder(true),
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
                () -> new AdjacencyMatrixGraphBuilder(true),
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
                () -> new AdjacencyMatrixGraphBuilder(true),
                5,
                new int[][] { { 1, 2 }, { 1, 3 }, { 2, 4 }, { 2, 5 }, { 3, 5 } });

        GraphHelper.assertSuccessors(graph, 1, 2, 3);
        GraphHelper.assertSuccessors(graph, 2, 4, 5);
}

    @Test
    void testGetNeighborsUndirected() {
        UndirectedGraph graph = (UndirectedGraph) GraphTestHelper.build(
                () -> new AdjacencyMatrixGraphBuilder(false),
                5,
                new int[][] { { 1, 2 }, { 1, 3 }, { 2, 4 }, { 5, 1 } });

        GraphHelper.assertNeighbors(graph, 1, 2, 3, 5);
        GraphHelper.assertNeighbors(graph, 2, 1, 4);
    }

    @Test
    void testGetInducedSubgraph() {
        Graph graph = GraphTestHelper.build(
                () -> new AdjacencyMatrixGraphBuilder(true),
                6,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 }, { 3, 4 }, { 4, 5 }, { 5, 6 } });

        GraphHelper.assertInducedSubgraphN(graph, new int[] { 1, 2, 3 }, 3);
        GraphHelper.assertInducedSubgraphM(graph, new int[] { 1, 2, 3 }, 3);
    }

    @Test
    void testGetReversedDirected() {
        DirectedGraph graph = (DirectedGraph) GraphTestHelper.build(
                () -> new AdjacencyMatrixGraphBuilder(true),
                4,
                new int[][] { { 1, 2 }, { 1, 3 }, { 2, 4 }, { 3, 4 } });

        GraphHelper.assertReversedM(graph, 4);
        GraphHelper.assertReversedHasEdge(graph, 2, 1);
        GraphHelper.assertReversedHasEdge(graph, 4, 2);
        GraphHelper.assertReversedHasEdge(graph, 4, 3);
    }

    @Test
    void testInvalidVertexArgumentGetDegree() {
        UndirectedGraph graph = (UndirectedGraph) GraphTestHelper.build(
                () -> new AdjacencyMatrixGraphBuilder(true),
                5,
                new int[][] { { 1, 2 } });

        GraphHelper.assertThrowsIAE(() -> graph.getDegree(0));
        GraphHelper.assertThrowsIAE(() -> graph.getDegree(6));
    }

    @Test
    void testInvalidVertexArgumentGetInDegree() {
        DirectedGraph graph = (DirectedGraph) GraphTestHelper.build(
                () -> new AdjacencyMatrixGraphBuilder(true),
                5,
                new int[][] { { 1, 2 } });

        GraphHelper.assertThrowsIAE(() -> graph.getInDegree(0));
        GraphHelper.assertThrowsIAE(() -> graph.getInDegree(6));
    }

    @Test
    void testInvalidVertexArgumentGetOutDegree() {
        DirectedGraph graph = (DirectedGraph) GraphTestHelper.build(
                () -> new AdjacencyMatrixGraphBuilder(true),
                5,
                new int[][] { { 1, 2 } });

        GraphHelper.assertThrowsIAE(() -> graph.getOutDegree(0));
        GraphHelper.assertThrowsIAE(() -> graph.getOutDegree(6));
    }

    @Test
    void testInvalidVertexArgumentGetSuccessors() {
        DirectedGraph graph = (DirectedGraph) GraphTestHelper.build(
                () -> new AdjacencyMatrixGraphBuilder(true),
                5,
                new int[][] { { 1, 2 } });

        GraphHelper.assertThrowsIAE(() -> graph.getSuccessors(0));
        GraphHelper.assertThrowsIAE(() -> graph.getSuccessors(6));
    }

    @Test
    void testInvalidVertexArgumentGetPredecessors() {
        DirectedGraph graph = (DirectedGraph) GraphTestHelper.build(
                () -> new AdjacencyMatrixGraphBuilder(true),
                5,
                new int[][] { { 1, 2 } });

        GraphHelper.assertThrowsIAE(() -> graph.getPredecessors(0));
        GraphHelper.assertThrowsIAE(() -> graph.getPredecessors(6));
    }

    @Test
    void testEmptyGraph() {
        Graph graph = GraphTestHelper.build(
                () -> new AdjacencyMatrixGraphBuilder(false),
                5,
                new int[][] {});

        assertEquals(5, graph.getVerticesCount());
        assertEquals(0, graph.getEdgesCount());
        GraphHelper.assertDegree((UndirectedGraph) graph, 1, 0);
    }

    @Test
    void testSingleVertexGraph() {
        Graph graph = GraphTestHelper.build(
                () -> new AdjacencyMatrixGraphBuilder(true),
                1,
                new int[][] {});

        assertEquals(1, graph.getVerticesCount());
        assertEquals(0, graph.getEdgesCount());
    }
}

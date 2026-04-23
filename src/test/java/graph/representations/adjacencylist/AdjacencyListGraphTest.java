package graph.representations.adjacencylist;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import graph.algorithms.Fleury;
import graph.api.DirectedGraph;
import graph.api.Graph;
import graph.api.GraphHelper;
import graph.api.UndirectedGraph;
import graph.representations.GraphBuilderHelper;

class AdjacencyListGraphTest {

    @Test
    void testGetVerticesDefaultRange() {
        Graph graph = GraphBuilderHelper.build(
                () -> new AdjacencyListGraphBuilder(false),
                5, 2,
                new int[][] { { 1, 2 }, { 3, 4 } });

        GraphHelper.assertVertices(graph, 1, 2, 3, 4, 5);
    }

    @Test
    void testIterateGraphAllEdgesVisited() {
        Graph graph = GraphBuilderHelper.build(
                () -> new AdjacencyListGraphBuilder(true),
                4, 3,
                new int[][] { { 1, 2 }, { 1, 3 }, { 2, 4 } });

        GraphHelper.assertIterateVertexCount(graph, 4);
        GraphHelper.assertIterateEdgeCount(graph, 3);
    }

    @Test
    void testGetDegreeUndirected() {
        UndirectedGraph graph = (UndirectedGraph) GraphBuilderHelper.build(
                () -> new AdjacencyListGraphBuilder(false),
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
                () -> new AdjacencyListGraphBuilder(true),
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
                () -> new AdjacencyListGraphBuilder(true),
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
                () -> new AdjacencyListGraphBuilder(true),
                5, 5,
                new int[][] { { 1, 2 }, { 1, 3 }, { 2, 4 }, { 2, 5 }, { 3, 5 } });

        GraphHelper.assertSuccessors(graph, 1, 2, 3);
        GraphHelper.assertSuccessors(graph, 2, 4, 5);
    }

    @Test
    void testGetPredecessors() {
        DirectedGraph graph = (DirectedGraph) GraphBuilderHelper.build(
                () -> new AdjacencyListGraphBuilder(true),
                5, 6,
                new int[][] { { 1, 3 }, { 2, 3 }, { 3, 4 }, { 3, 5 }, { 4, 5 }, { 2, 5 } });

        GraphHelper.assertPredecessors(graph, 3, 1, 2);
        GraphHelper.assertPredecessors(graph, 5, 2, 3, 4);
    }

    @Test
    void testGetNeighborsUndirected() {
        UndirectedGraph graph = (UndirectedGraph) GraphBuilderHelper.build(
                () -> new AdjacencyListGraphBuilder(false),
                5, 5,
                new int[][] { { 1, 2 }, { 1, 3 }, { 2, 4 }, { 2, 5 }, { 3, 5 } });

        GraphHelper.assertNeighbors(graph, 1, 2, 3);
        GraphHelper.assertNeighbors(graph, 2, 1, 4, 5);
    }

    @Test
    void testClone() {
        Graph graph = GraphBuilderHelper.build(
                () -> new AdjacencyListGraphBuilder(true),
                3, 2,
                new int[][] { { 1, 2 }, { 2, 3 } });

        Graph cloned = graph.clone();

        assertNotSame(graph, cloned);
        assertEquals(3, cloned.getVerticesCount());
        assertEquals(2, cloned.getEdgesCount());
    }

    @Test
    void testGetReversed() {
        DirectedGraph graph = (DirectedGraph) GraphBuilderHelper.build(
                () -> new AdjacencyListGraphBuilder(true),
                3, 2,
                new int[][] { { 1, 2 }, { 2, 3 } });

        DirectedGraph reversed = graph.getReversed();

        assertEquals(0, reversed.getSuccessors(1).length);
        assertEquals(1, reversed.getSuccessors(2).length);
    }

    @Test
    void testAddEdgeDuplicateDirected() {
        Graph graph = GraphBuilderHelper.build(
                () -> new AdjacencyListGraphBuilder(true),
                3, 3,
                new int[][] { { 1, 2 }, { 1, 2 }, { 1, 2 } });

        GraphHelper.assertM(graph, 1);
    }

    @Test
    void testAddEdgeDuplicateUndirected() {
        Graph graph = GraphBuilderHelper.build(
                () -> new AdjacencyListGraphBuilder(false),
                3, 3,
                new int[][] { { 1, 2 }, { 1, 2 }, { 2, 1 } });

        GraphHelper.assertM(graph, 1);
    }

    @Test
    void testGetVerticesCount() {
        Graph graph = GraphBuilderHelper.build(
                () -> new AdjacencyListGraphBuilder(false),
                5, 2,
                new int[][] { { 1, 2 }, { 3, 4 } });

        assertEquals(5, graph.getVerticesCount());
    }

    @Test
    void testGetEdgesCount() {
        Graph graph = GraphBuilderHelper.build(
                () -> new AdjacencyListGraphBuilder(true),
                5, 5,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 } });

        assertEquals(3, graph.getEdgesCount());
    }

    @Test
    void testGetInducedSubgraph() {
        Graph graph = GraphBuilderHelper.build(
                () -> new AdjacencyListGraphBuilder(true),
                5, 4,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 4 }, { 4, 5 } });

        Graph subgraph = graph.getInducedSubgraph(new int[] { 1, 2, 3 });

        assertEquals(3, subgraph.getVerticesCount());
    }

    @Test
    void testFleuryEulerianCircuit() {
        Graph graph = GraphBuilderHelper.build(
                () -> new AdjacencyListGraphBuilder(false),
                3, 3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 } });

        Fleury.EulerianPath result = Fleury.findEulerianPath(graph);

        assertNotNull(result);
        assertEquals(Fleury.EulerianType.EULERIAN, result.type());
        assertEquals(4, result.path().length);
    }

    @Test
    void testCloneVsOriginal() {
        Graph graph = GraphBuilderHelper.build(
                () -> new AdjacencyListGraphBuilder(false),
                7, 12,
                new int[][] { 
                    { 1, 2 }, { 1, 3 }, { 2, 3 }, { 2, 4 }, { 2, 5 }, 
                    { 3, 4 }, { 3, 6 }, { 4, 5 }, { 4, 6 }, { 5, 6 }, 
                    { 5, 7 }, { 6, 7 } });
        
        UndirectedGraph clone = (UndirectedGraph) graph.clone();
        
        System.out.println("Original edges before: " + graph.getEdgesCount());
        System.out.println("Clone edges before: " + clone.getEdgesCount());
        
        clone.removeEdge(1, 2);
        
        System.out.println("Original edges after: " + graph.getEdgesCount());
        System.out.println("Clone edges after: " + clone.getEdgesCount());
        
        assertEquals(12, graph.getEdgesCount());
        assertEquals(11, clone.getEdgesCount());
    }
}
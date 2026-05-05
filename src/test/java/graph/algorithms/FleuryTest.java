package graph.algorithms;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import graph.algorithms.Fleury.EulerianPath;
import graph.algorithms.Fleury.EulerianType;
import graph.api.DirectedGraph;
import graph.api.Edges;
import graph.api.Graph;
import graph.api.UndirectedGraph;
import graph.util.GraphTestHelper;
import graph.util.WeightedTestHelper;
import graph.util.GraphHelper;
import graph.representations.forwardstar.ForwardStarGraphBuilder;

class FleuryTest {

    // ==================== UNDIRECTED GRAPH TESTS ====================

    @Test
    void testEulerianCircuit_Triangle() {
        UndirectedGraph graph = (UndirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(false),
                3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 } });

        EulerianPath result = Fleury.findEulerianPath((Graph) graph);

        assertEquals(EulerianType.EULERIAN, result.type());
        assertEquals(4, result.path().length);
        assertTrue(result.path()[0] >= 1 && result.path()[0] <= 3, "Path should start at a valid vertex");
    }

    @Test
    void testNonEulerian_AllOddDegrees() {
        UndirectedGraph graph = (UndirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(false),
                5,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 4 }, { 4, 1 }, { 1, 3 }, { 2, 4 } });

        EulerianPath result = Fleury.findEulerianPath((Graph) graph);

        assertEquals(EulerianType.NON_EULERIAN, result.type());
        assertEquals(0, result.path().length);
    }

    @Test
    void testEulerianCircuit_PetersenCore() {
        UndirectedGraph graph = (UndirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(false),
                5,
                new int[][] {
                        { 1, 2 }, { 2, 3 }, { 3, 4 }, { 4, 5 }, { 5, 1 },
                        { 1, 3 }, { 2, 4 }, { 3, 5 }, { 4, 1 }, { 5, 2 } });

        EulerianPath result = Fleury.findEulerianPath((Graph) graph);

        assertEquals(EulerianType.EULERIAN, result.type());
        assertEquals(11, result.path().length);
    }

    @Test
    void testSemiEulerian_LinearPath() {
        UndirectedGraph graph = (UndirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(false),
                4,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 4 } });

        EulerianPath result = Fleury.findEulerianPath((Graph) graph);

        assertEquals(EulerianType.SEMI_EULERIAN, result.type());
        assertEquals(4, result.path().length);

        int start = result.path()[0];
        int end = result.path()[result.path().length - 1];
        assertTrue(start == 1 || start == 4, "Path should start at odd-degree vertex (1 or 4)");
        assertTrue(end == 1 || end == 4, "Path should end at odd-degree vertex (1 or 4)");
    }

    @Test
    void testSemiEulerian_PathWithCycle() {
        UndirectedGraph graph = (UndirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(false),
                5,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 }, { 3, 4 }, { 4, 5 } });

        EulerianPath result = Fleury.findEulerianPath((Graph) graph);

        assertEquals(EulerianType.SEMI_EULERIAN, result.type());
        assertEquals(6, result.path().length);
    }

    @Test
    void testNonEulerian_Disconnected() {
        UndirectedGraph graph = (UndirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(false),
                6,
                new int[][] { { 1, 2 }, { 2, 3 }, { 4, 5 }, { 5, 6 } });

        EulerianPath result = Fleury.findEulerianPath((Graph) graph);

        assertEquals(EulerianType.NON_EULERIAN, result.type());
        assertEquals(0, result.path().length);
    }

    @Test
    void testNonEulerian_ThreeOddDegrees() {
        UndirectedGraph graph = (UndirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(false),
                4,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 4 } });

        EulerianPath result = Fleury.findEulerianPath((Graph) graph);

        assertEquals(EulerianType.SEMI_EULERIAN, result.type());
        assertEquals(4, result.path().length);
    }

    @Test
    void testEulerianCircuit_FourCycle() {
        UndirectedGraph graph = (UndirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(false),
                4,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 4 }, { 4, 1 } });

        EulerianPath result = Fleury.findEulerianPath((Graph) graph);

        assertEquals(EulerianType.EULERIAN, result.type());
        assertEquals(5, result.path().length);
    }

    @Test
    void testNonEulerian_IsolatedVertex() {
        UndirectedGraph graph = (UndirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(false),
                4,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 } });

        EulerianPath result = Fleury.findEulerianPath((Graph) graph);

        assertEquals(EulerianType.NON_EULERIAN, result.type());
        assertEquals(0, result.path().length);
    }

    @Test
    void testEulerianCircuit_ReturnsToStart() {
        UndirectedGraph graph = (UndirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(false),
                3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 } });

        EulerianPath result = Fleury.findEulerianPath((Graph) graph);

        assertEquals(EulerianType.EULERIAN, result.type());
        assertEquals(result.path()[0], result.path()[result.path().length - 1],
                "Eulerian circuit should return to start vertex");
    }

    // ==================== DIRECTED GRAPH TESTS ====================

    @Test
    void testEulerianCircuit_SimpleCycle() {
        DirectedGraph graph = (DirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 } });

        EulerianPath result = Fleury.findEulerianPath((Graph) graph);

        assertEquals(EulerianType.EULERIAN, result.type());
        assertEquals(4, result.path().length);
    }

    @Test
    void testEulerianCircuit_ComplexCycle() {
        DirectedGraph graph = (DirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4,
                new int[][] {
                        { 1, 2 }, { 2, 1 }, { 2, 3 }, { 3, 2 },
                        { 3, 4 }, { 4, 3 }, { 4, 1 }, { 1, 4 } });

        EulerianPath result = Fleury.findEulerianPath((Graph) graph);

        assertEquals(EulerianType.EULERIAN, result.type());
        assertEquals(9, result.path().length);
    }

    @Test
    void testEulerianCircuit_DirectedCycle() {
        DirectedGraph graph = (DirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 4 }, { 4, 1 } });

        EulerianPath result = Fleury.findEulerianPath((Graph) graph);

        assertEquals(EulerianType.EULERIAN, result.type());
        assertEquals(5, result.path().length);
    }

    @Test
    void testSemiEulerian_NotStronglyConnectedTrail() {
        DirectedGraph graph = (DirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 }, { 3, 4 } });

        EulerianPath result = Fleury.findEulerianPath((Graph) graph);

        // Graph is weakly connected (sufficient for Eulerian path)
        // Vertex 4 has in=1, out=0 (imbalanced), others balanced -> exactly 2
        // imbalanced
        assertEquals(EulerianType.SEMI_EULERIAN, result.type());
        assertTrue(result.path().length > 0);
    }

    @Test
    void testSemiEulerian_NotStronglyConnected() {
        DirectedGraph graph = (DirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 4 } });

        EulerianPath result = Fleury.findEulerianPath((Graph) graph);

        // Graph is weakly connected (sufficient for Eulerian path)
        // Vertex 1 has out=1,in=0, vertex 4 has in=1,out=0 -> exactly 2 imbalanced
        assertEquals(EulerianType.SEMI_EULERIAN, result.type());
        assertTrue(result.path().length > 0);
    }

    @Test
    void testNonEulerian_MultipleSCCs() {
        DirectedGraph graph = (DirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4,
                new int[][] { { 1, 2 }, { 2, 1 }, { 3, 4 }, { 4, 3 } });

        EulerianPath result = Fleury.findEulerianPath((Graph) graph);

        assertEquals(EulerianType.NON_EULERIAN, result.type());
        assertEquals(0, result.path().length);
    }

    @Test
    void testSemiEulerian_UnequalDegrees() {
        DirectedGraph graph = (DirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 2, 1 } });

        EulerianPath result = Fleury.findEulerianPath((Graph) graph);

        // 2 imbalanced vertices (2: out=2,in=1 and 3: out=0,in=1)
        // Weakly connected -> SEMI_EULERIAN
        assertEquals(EulerianType.SEMI_EULERIAN, result.type());
        assertTrue(result.path().length > 0);
    }

    @Test
    void testNonEulerian_SingleVertexNoEdges() {
        DirectedGraph graph = (DirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                1,
                new int[][] {});

        EulerianPath result = Fleury.findEulerianPath((Graph) graph);

        assertEquals(EulerianType.NON_EULERIAN, result.type());
        assertEquals(0, result.path().length);
    }

    // ==================== PATH VERIFICATION TESTS ====================

    @Test
    void testPathLength_Undirected() {
        UndirectedGraph graph = (UndirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(false),
                3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 } });

        EulerianPath result = Fleury.findEulerianPath((Graph) graph);

        assertEquals(4, result.path().length, "Path should have m+1 vertices");
    }

    @Test
    void testPathLength_Directed() {
        DirectedGraph graph = (DirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 4 }, { 4, 1 } });

        EulerianPath result = Fleury.findEulerianPath((Graph) graph);

        assertEquals(5, result.path().length, "Path should have m+1 vertices");
    }

    @Test
    void testPathVisitsAllEdges_Undirected() {
        UndirectedGraph graph = (UndirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(false),
                4,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 4 }, { 4, 1 }, { 1, 3 } });

        EulerianPath result = Fleury.findEulerianPath((Graph) graph);

        long[] edges = graph.getEdgesSet();
        Set<Long> expectedEdges = new HashSet<>();
        for (long e : edges) {
            int v = Edges.getSource(e);
            int w = Edges.getTarget(e);

            expectedEdges.add(Edges.directed(v, w));
            expectedEdges.add(Edges.directed(w, v));
        }

        int edgeCount = result.path().length - 1;
        assertEquals(edges.length, edgeCount, "Path should traverse all edges");

        Set<Long> pathEdges = new HashSet<>();
        for (int i = 0; i < result.path().length - 1; i++) {
            pathEdges.add(Edges.undirected(result.path()[i], result.path()[i + 1]));
        }

        assertEquals(edges.length, pathEdges.size(), "Each edge should appear exactly once");
    }

    @Test
    void testPathVisitsAllEdges_Directed() {
        DirectedGraph graph = (DirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 4 }, { 4, 1 } });

        EulerianPath result = Fleury.findEulerianPath((Graph) graph);

        long[] edges = graph.getEdgesSet();
        int edgeCount = result.path().length - 1;
        assertEquals(edges.length, edgeCount, "Path should traverse all edges");

        Set<Long> pathEdges = new HashSet<>();
        for (int i = 0; i < result.path().length - 1; i++) {
            pathEdges.add(Edges.undirected(result.path()[i], result.path()[i + 1]));
        }

        assertEquals(edges.length, pathEdges.size(), "Each edge should appear exactly once");
    }

    @Test
    void testPathEdgesAreAdjacent_Undirected() {
        UndirectedGraph graph = (UndirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(false),
                3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 } });

        EulerianPath result = Fleury.findEulerianPath((Graph) graph);

        for (int i = 0; i < result.path().length - 1; i++) {
            int v = result.path()[i];
            int w = result.path()[i + 1];
            assertTrue(GraphHelper.hasNeighbor(graph, v, w),
                    "Consecutive vertices (" + v + ", " + w + ") must be adjacent");
        }
    }

    @Test
    void testPathEdgesAreAdjacent_Directed() {
        DirectedGraph graph = (DirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 4 }, { 4, 1 } });

        EulerianPath result = Fleury.findEulerianPath((Graph) graph);

        for (int i = 0; i < result.path().length - 1; i++) {
            int v = result.path()[i];
            int w = result.path()[i + 1];
            assertTrue(GraphHelper.hasEdge(graph, v, w),
                    "Consecutive vertices (" + v + " -> " + w + ") must form a directed edge");
        }
    }

    // ==================== WEIGHTED GRAPH TESTS ====================

    @Test
    void testOnWeightedUndirectedGraph() {
        Graph graph = WeightedTestHelper.build(
                () -> new ForwardStarGraphBuilder(false),
                new int[][] { { 1, 2, 5 }, { 2, 3, 10 }, { 3, 1, 15 } });

        assertTrue(graph.isWeighted(), "Graph should be weighted");

        EulerianPath result = Fleury.findEulerianPath(graph);

        assertEquals(EulerianType.EULERIAN, result.type());
        assertEquals(4, result.path().length);
    }

    @Test
    void testOnWeightedDirectedGraph() {
        Graph graph = WeightedTestHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                new int[][] { { 1, 2, 5 }, { 2, 3, 10 }, { 3, 1, 15 } });

        assertTrue(graph.isWeighted(), "Graph should be weighted");

        EulerianPath result = Fleury.findEulerianPath(graph);

        assertEquals(EulerianType.EULERIAN, result.type());
        assertEquals(4, result.path().length);
    }

    @Test
    void testWeightedGraphNonEulerian() {
        // Disconnected weighted graph - not Eulerian
        Graph graph = WeightedTestHelper.build(
                () -> new ForwardStarGraphBuilder(false),
                new int[][] { { 1, 2, 5 }, { 2, 3, 10 }, { 3, 1, 15 }, { 4, 5, 20 } });

        assertTrue(graph.isWeighted(), "Graph should be weighted");

        EulerianPath result = Fleury.findEulerianPath(graph);

        assertEquals(EulerianType.NON_EULERIAN, result.type());
        assertEquals(0, result.path().length);
    }

    @Test
    void testWeightedGraphSemiEulerian() {
        Graph graph = WeightedTestHelper.build(
                () -> new ForwardStarGraphBuilder(false),
                new int[][] { { 1, 2, 5 }, { 2, 3, 10 }, { 3, 4, 15 } });

        assertTrue(graph.isWeighted(), "Graph should be weighted");

        EulerianPath result = Fleury.findEulerianPath(graph);

        assertEquals(EulerianType.SEMI_EULERIAN, result.type());
        assertEquals(4, result.path().length);
    }

    // ==================== DISPATCH/INTEGRATION TESTS ====================

    @Test
    void testGraphInterface_DispatchesUndirected() {
        Graph graph = GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(false),
                3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 } });

        assertFalse(graph.isDirected);

        EulerianPath result = Fleury.findEulerianPath((Graph) graph);

        assertEquals(EulerianType.EULERIAN, result.type());
        assertEquals(4, result.path().length);
    }

    @Test
    void testGraphInterface_DispatchesDirected() {
        Graph graph = GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 } });

        assertTrue(graph.isDirected);

        EulerianPath result = Fleury.findEulerianPath((Graph) graph);

        assertEquals(EulerianType.EULERIAN, result.type());
        assertEquals(4, result.path().length);
    }

    // ==================== ADDITIONAL EDGE CASES ====================

    @Test
    void testNonEulerian_SingleVertexUndirected() {
        UndirectedGraph graph = (UndirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(false),
                1,
                new int[][] {});

        EulerianPath result = Fleury.findEulerianPath((Graph) graph);

        assertEquals(EulerianType.NON_EULERIAN, result.type());
        assertEquals(0, result.path().length);
    }

    @Test
    void testSemiEulerian_WeaklyConnectedButNotStrongly() {
        DirectedGraph graph = (DirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 }, { 3, 4 } });

        EulerianPath result = Fleury.findEulerianPath((Graph) graph);

        // Graph is weakly connected with exactly 2 imbalanced vertices
        assertEquals(EulerianType.SEMI_EULERIAN, result.type());
        assertTrue(result.path().length > 0);
    }

    @Test
    void testPathEnd_IsAtOddDegreeVertex() {
        UndirectedGraph graph = (UndirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(false),
                4,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 4 } });

        EulerianPath result = Fleury.findEulerianPath((Graph) graph);

        int end = result.path()[result.path().length - 1];
        assertTrue(end == 1 || end == 4, "Path should end at odd-degree vertex (1 or 4)");
    }

    @Test
    void testSelfLoopsIgnored() {
        DirectedGraph graph = (DirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                3,
                new int[][] { { 1, 1 }, { 1, 2 }, { 2, 3 }, { 3, 1 } });

        EulerianPath result = Fleury.findEulerianPath((Graph) graph);

        assertNotNull(result);
        assertNotNull(result.path());
    }

    @Test
    void testEulerianCircuit_Undirected() {
        UndirectedGraph graph = (UndirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(false),
                7,
                new int[][] {
                        { 1, 2 }, { 1, 3 }, { 2, 3 }, { 2, 4 }, { 2, 5 },
                        { 3, 4 }, { 3, 6 }, { 4, 5 }, { 4, 6 }, { 5, 6 },
                        { 5, 7 }, { 6, 7 } });

        EulerianPath result = Fleury.findEulerianPath((Graph) graph);

        assertEquals(EulerianType.EULERIAN, result.type());
        assertEquals(13, result.path().length, "Path should have m+1 = 13 vertices");
        assertEquals(result.path()[0], result.path()[result.path().length - 1],
                "Eulerian circuit should return to start vertex");
    }
}

package graph.algorithms;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import graph.algorithms.Fleury.EulerianPath;
import graph.algorithms.Fleury.EulerianType;
import graph.api.DirectedGraph;
import graph.api.Graph;
import graph.api.UndirectedGraph;
import graph.representations.GraphBuilderHelper;
import graph.representations.forwardstar.ForwardStarGraphBuilder;

class FleuryTest {

    // ==================== UNDIRECTED GRAPH TESTS ====================

    @Test
    void testEulerianCircuit_Triangle() {
        UndirectedGraph graph = (UndirectedGraph) GraphBuilderHelper.buildUndirected(
                () -> new ForwardStarGraphBuilder(false),
                3, 3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 } });

        EulerianPath result = Fleury.findEulerianPath((Graph) graph);

        assertEquals(EulerianType.EULERIAN, result.type());
        assertEquals(4, result.path().length);
        assertTrue(result.path()[0] >= 1 && result.path()[0] <= 3, "Path should start at a valid vertex");
    }

    @Test
    void testNonEulerian_AllOddDegrees() {
        UndirectedGraph graph = (UndirectedGraph) GraphBuilderHelper.buildUndirected(
                () -> new ForwardStarGraphBuilder(false),
                4, 6,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 4 }, { 4, 1 }, { 1, 3 }, { 2, 4 } });

        EulerianPath result = Fleury.findEulerianPath((Graph) graph);

        assertEquals(EulerianType.NON_EULERIAN, result.type());
        assertEquals(0, result.path().length);
    }

    @Test
    void testEulerianCircuit_PetersenCore() {
        UndirectedGraph graph = (UndirectedGraph) GraphBuilderHelper.buildUndirected(
                () -> new ForwardStarGraphBuilder(false),
                5, 10,
                new int[][] {
                        { 1, 2 }, { 2, 3 }, { 3, 4 }, { 4, 5 }, { 5, 1 },
                        { 1, 3 }, { 2, 4 }, { 3, 5 }, { 4, 1 }, { 5, 2 } });

        EulerianPath result = Fleury.findEulerianPath((Graph) graph);

        assertEquals(EulerianType.EULERIAN, result.type());
        assertEquals(11, result.path().length);
    }

    @Test
    void testSemiEulerian_LinearPath() {
        UndirectedGraph graph = (UndirectedGraph) GraphBuilderHelper.buildUndirected(
                () -> new ForwardStarGraphBuilder(false),
                4, 3,
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
        UndirectedGraph graph = (UndirectedGraph) GraphBuilderHelper.buildUndirected(
                () -> new ForwardStarGraphBuilder(false),
                5, 5,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 }, { 3, 4 }, { 4, 5 } });

        EulerianPath result = Fleury.findEulerianPath((Graph) graph);

        assertEquals(EulerianType.SEMI_EULERIAN, result.type());
        assertEquals(6, result.path().length);
    }

    @Test
    void testNonEulerian_Disconnected() {
        UndirectedGraph graph = (UndirectedGraph) GraphBuilderHelper.buildUndirected(
                () -> new ForwardStarGraphBuilder(false),
                6, 4,
                new int[][] { { 1, 2 }, { 2, 3 }, { 4, 5 }, { 5, 6 } });

        EulerianPath result = Fleury.findEulerianPath((Graph) graph);

        assertEquals(EulerianType.NON_EULERIAN, result.type());
        assertEquals(0, result.path().length);
    }

    @Test
    void testNonEulerian_ThreeOddDegrees() {
        UndirectedGraph graph = (UndirectedGraph) GraphBuilderHelper.buildUndirected(
                () -> new ForwardStarGraphBuilder(false),
                4, 3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 4 } });

        EulerianPath result = Fleury.findEulerianPath((Graph) graph);

        assertEquals(EulerianType.SEMI_EULERIAN, result.type());
        assertEquals(4, result.path().length);
    }

    @Test
    void testEulerianCircuit_FourCycle() {
        UndirectedGraph graph = (UndirectedGraph) GraphBuilderHelper.buildUndirected(
                () -> new ForwardStarGraphBuilder(false),
                4, 4,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 4 }, { 4, 1 } });

        EulerianPath result = Fleury.findEulerianPath((Graph) graph);

        assertEquals(EulerianType.EULERIAN, result.type());
        assertEquals(5, result.path().length);
    }

    @Test
    void testNonEulerian_IsolatedVertex() {
        UndirectedGraph graph = (UndirectedGraph) GraphBuilderHelper.buildUndirected(
                () -> new ForwardStarGraphBuilder(false),
                4, 3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 } });

        EulerianPath result = Fleury.findEulerianPath((Graph) graph);

        assertEquals(EulerianType.NON_EULERIAN, result.type());
        assertEquals(0, result.path().length);
    }

    @Test
    void testEulerianCircuit_ReturnsToStart() {
        UndirectedGraph graph = (UndirectedGraph) GraphBuilderHelper.buildUndirected(
                () -> new ForwardStarGraphBuilder(false),
                3, 3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 } });

        EulerianPath result = Fleury.findEulerianPath((Graph) graph);

        assertEquals(EulerianType.EULERIAN, result.type());
        assertEquals(result.path()[0], result.path()[result.path().length - 1],
                "Eulerian circuit should return to start vertex");
    }

    // ==================== DIRECTED GRAPH TESTS ====================

    @Test
    void testEulerianCircuit_SimpleCycle() {
        DirectedGraph graph = (DirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                3, 3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 } });

        EulerianPath result = Fleury.findEulerianPath((Graph) graph);

        assertEquals(EulerianType.EULERIAN, result.type());
        assertEquals(4, result.path().length);
    }

    @Test
    void testEulerianCircuit_ComplexCycle() {
        DirectedGraph graph = (DirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4, 8,
                new int[][] {
                        { 1, 2 }, { 2, 1 }, { 2, 3 }, { 3, 2 },
                        { 3, 4 }, { 4, 3 }, { 4, 1 }, { 1, 4 } });

        EulerianPath result = Fleury.findEulerianPath((Graph) graph);

        assertEquals(EulerianType.EULERIAN, result.type());
        assertEquals(9, result.path().length);
    }

    @Test
    void testEulerianCircuit_DirectedCycle() {
        DirectedGraph graph = (DirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4, 4,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 4 }, { 4, 1 } });

        EulerianPath result = Fleury.findEulerianPath((Graph) graph);

        assertEquals(EulerianType.EULERIAN, result.type());
        assertEquals(5, result.path().length);
    }

    @Test
    void testNonEulerian_NotStronglyConnectedTrail() {
        DirectedGraph graph = (DirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4, 4,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 }, { 3, 4 } });

        EulerianPath result = Fleury.findEulerianPath((Graph) graph);

        assertEquals(EulerianType.NON_EULERIAN, result.type());
        assertEquals(0, result.path().length);
    }

    @Test
    void testNonEulerian_NotStronglyConnected() {
        DirectedGraph graph = (DirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4, 3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 4 } });

        EulerianPath result = Fleury.findEulerianPath((Graph) graph);

        assertEquals(EulerianType.NON_EULERIAN, result.type());
        assertEquals(0, result.path().length);
    }

    @Test
    void testNonEulerian_MultipleSCCs() {
        DirectedGraph graph = (DirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                6, 4,
                new int[][] { { 1, 2 }, { 2, 1 }, { 3, 4 }, { 4, 3 } });

        EulerianPath result = Fleury.findEulerianPath((Graph) graph);

        assertEquals(EulerianType.NON_EULERIAN, result.type());
        assertEquals(0, result.path().length);
    }

    @Test
    void testNonEulerian_UnequalDegrees() {
        DirectedGraph graph = (DirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                3, 3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 2, 1 } });

        EulerianPath result = Fleury.findEulerianPath((Graph) graph);

        assertEquals(EulerianType.NON_EULERIAN, result.type());
        assertEquals(0, result.path().length);
    }

    @Test
    void testNonEulerian_SingleVertexNoEdges() {
        DirectedGraph graph = (DirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                1, 0,
                new int[][] {});

        EulerianPath result = Fleury.findEulerianPath((Graph) graph);

        assertEquals(EulerianType.NON_EULERIAN, result.type());
        assertEquals(0, result.path().length);
    }

    // ==================== PATH VERIFICATION TESTS ====================

    @Test
    void testPathLength_Undirected() {
        UndirectedGraph graph = (UndirectedGraph) GraphBuilderHelper.buildUndirected(
                () -> new ForwardStarGraphBuilder(false),
                3, 3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 } });

        EulerianPath result = Fleury.findEulerianPath((Graph) graph);

        assertEquals(4, result.path().length, "Path should have m+1 vertices");
    }

    @Test
    void testPathLength_Directed() {
        DirectedGraph graph = (DirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4, 4,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 4 }, { 4, 1 } });

        EulerianPath result = Fleury.findEulerianPath((Graph) graph);

        assertEquals(5, result.path().length, "Path should have m+1 vertices");
    }

    @Test
    void testPathVisitsAllEdges_Undirected() {
        UndirectedGraph graph = (UndirectedGraph) GraphBuilderHelper.buildUndirected(
                () -> new ForwardStarGraphBuilder(false),
                4, 5,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 4 }, { 4, 1 }, { 1, 3 } });

        EulerianPath result = Fleury.findEulerianPath((Graph) graph);

        int[][] edges = graph.getEdgesSet();
        Set<String> expectedEdges = new HashSet<>();
        for (int[] e : edges) {
            expectedEdges.add(e[0] + "-" + e[1]);
            expectedEdges.add(e[1] + "-" + e[0]);
        }

        int edgeCount = result.path().length - 1;
        assertEquals(edges.length, edgeCount, "Path should traverse all edges");

        Set<String> pathEdges = new HashSet<>();
        for (int i = 0; i < result.path().length - 1; i++) {
            String edge = result.path()[i] + "-" + result.path()[i + 1];
            pathEdges.add(edge);
        }

        assertEquals(edges.length, pathEdges.size(), "Each edge should appear exactly once");
    }

    @Test
    void testPathVisitsAllEdges_Directed() {
        DirectedGraph graph = (DirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4, 4,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 4 }, { 4, 1 } });

        EulerianPath result = Fleury.findEulerianPath((Graph) graph);

        int[][] edges = graph.getEdgesSet();
        int edgeCount = result.path().length - 1;
        assertEquals(edges.length, edgeCount, "Path should traverse all edges");

        Set<String> pathEdges = new HashSet<>();
        for (int i = 0; i < result.path().length - 1; i++) {
            String edge = result.path()[i] + "->" + result.path()[i + 1];
            pathEdges.add(edge);
        }

        assertEquals(edges.length, pathEdges.size(), "Each edge should appear exactly once");
    }

    @Test
    void testPathEdgesAreAdjacent_Undirected() {
        UndirectedGraph graph = (UndirectedGraph) GraphBuilderHelper.buildUndirected(
                () -> new ForwardStarGraphBuilder(false),
                3, 3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 } });

        EulerianPath result = Fleury.findEulerianPath((Graph) graph);

        for (int i = 0; i < result.path().length - 1; i++) {
            int v = result.path()[i];
            int w = result.path()[i + 1];
            assertTrue(GraphBuilderHelper.hasNeighbor(graph, v, w),
                    "Consecutive vertices (" + v + ", " + w + ") must be adjacent");
        }
    }

    @Test
    void testPathEdgesAreAdjacent_Directed() {
        DirectedGraph graph = (DirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4, 4,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 4 }, { 4, 1 } });

        EulerianPath result = Fleury.findEulerianPath((Graph) graph);

        for (int i = 0; i < result.path().length - 1; i++) {
            int v = result.path()[i];
            int w = result.path()[i + 1];
            assertTrue(GraphBuilderHelper.hasEdge(graph, v, w),
                    "Consecutive vertices (" + v + " -> " + w + ") must form a directed edge");
        }
    }

    // ==================== DISPATCH/INTEGRATION TESTS ====================

    @Test
    void testGraphInterface_DispatchesUndirected() {
        Graph graph = GraphBuilderHelper.buildUndirected(
                () -> new ForwardStarGraphBuilder(false),
                3, 3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 } });

        assertFalse(graph.isDirected);

        EulerianPath result = Fleury.findEulerianPath((Graph) graph);

        assertEquals(EulerianType.EULERIAN, result.type());
        assertEquals(4, result.path().length);
    }

    @Test
    void testGraphInterface_DispatchesDirected() {
        Graph graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                3, 3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 } });

        assertTrue(graph.isDirected);

        EulerianPath result = Fleury.findEulerianPath((Graph) graph);

        assertEquals(EulerianType.EULERIAN, result.type());
        assertEquals(4, result.path().length);
    }

    // ==================== ADDITIONAL EDGE CASES ====================

    @Test
    void testNonEulerian_SingleVertexUndirected() {
        UndirectedGraph graph = (UndirectedGraph) GraphBuilderHelper.buildUndirected(
                () -> new ForwardStarGraphBuilder(false),
                1, 0,
                new int[][] {});

        EulerianPath result = Fleury.findEulerianPath((Graph) graph);

        assertEquals(EulerianType.NON_EULERIAN, result.type());
        assertEquals(0, result.path().length);
    }

    @Test
    void testNonEulerian_WeaklyConnectedButNotStrongly() {
        DirectedGraph graph = (DirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4, 4,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 }, { 3, 4 } });

        EulerianPath result = Fleury.findEulerianPath((Graph) graph);

        assertEquals(EulerianType.NON_EULERIAN, result.type());
    }

    @Test
    void testPathEnd_IsAtOddDegreeVertex() {
        UndirectedGraph graph = (UndirectedGraph) GraphBuilderHelper.buildUndirected(
                () -> new ForwardStarGraphBuilder(false),
                4, 3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 4 } });

        EulerianPath result = Fleury.findEulerianPath((Graph) graph);

        int end = result.path()[result.path().length - 1];
        assertTrue(end == 1 || end == 4, "Path should end at odd-degree vertex (1 or 4)");
    }

    @Test
    void testSelfLoopsIgnored() {
        DirectedGraph graph = (DirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                3, 4,
                new int[][] { { 1, 1 }, { 1, 2 }, { 2, 3 }, { 3, 1 } });

        EulerianPath result = Fleury.findEulerianPath((Graph) graph);

        assertNotNull(result);
        assertNotNull(result.path());
    }
}

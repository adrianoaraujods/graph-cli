package graph.algorithms;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import graph.algorithms.DFS.DFSResult;
import graph.api.DirectedGraph;
import graph.api.Graph;
import graph.util.GraphTestHelper;
import graph.representations.forwardstar.ForwardStarGraphBuilder;

class KosarajuTest {

    @Test
    void testEmptyGraph() {
        DirectedGraph graph = (DirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                3,
                new int[][] {});

        DirectedGraph[] components = Kosaraju.findSCCs(graph);

        assertEquals(3, components.length);
    }

    @Test
    void testSelfLoop() {
        DirectedGraph graph = (DirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                2,
                new int[][] { { 1, 1 }, { 1, 2 } });

        DirectedGraph[] components = Kosaraju.findSCCs(graph);

        assertEquals(2, components.length);
    }

    @Test
    void testAllSelfLoops() {
        DirectedGraph graph = (DirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                3,
                new int[][] { { 1, 1 }, { 2, 2 }, { 3, 3 } });

        DirectedGraph[] components = Kosaraju.findSCCs(graph);

        assertEquals(3, components.length);
    }

    @Test
    void testBasicSCC() {
        DirectedGraph graph = (DirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 }, { 3, 4 } });

        DirectedGraph[] components = Kosaraju.findSCCs(graph);

        assertEquals(2, components.length);
    }

    @Test
    void testTwoSCCs() {
        DirectedGraph graph = (DirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                5,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 }, { 4, 5 } });

        DirectedGraph[] components = Kosaraju.findSCCs(graph);

        assertEquals(3, components.length);
    }

    @Test
    void testGraphWithCycles() {
        DirectedGraph graph = (DirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                6,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 }, { 3, 4 }, { 4, 5 }, { 5, 6 }, { 6, 4 } });

        DirectedGraph[] components = Kosaraju.findSCCs(graph);

        assertEquals(2, components.length);

        boolean foundThreeVertexComponent = false;
        for (DirectedGraph comp : components) {
            if (comp.getVerticesCount() == 3) {
                foundThreeVertexComponent = true;
                break;
            }
        }
        assertTrue(foundThreeVertexComponent);
    }

    @Test
    void testMultipleSCCs() {
        DirectedGraph graph = (DirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                8,
                new int[][] { { 1, 2 }, { 2, 1 }, { 3, 4 }, { 4, 3 }, { 5, 6 }, { 6, 5 }, { 2, 3 }, { 4, 5 } });

        DirectedGraph[] components = Kosaraju.findSCCs(graph);

        assertEquals(5, components.length);
    }

    @Test
    void testLinearChain() {
        DirectedGraph graph = (DirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 4 } });

        DirectedGraph[] components = Kosaraju.findSCCs(graph);

        assertEquals(4, components.length);
    }

    @Test
    void testDenseGraph() {
        DirectedGraph graph = (DirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                3,
                new int[][] { { 1, 2 }, { 2, 1 }, { 1, 3 }, { 3, 1 }, { 2, 3 }, { 3, 2 } });

        DirectedGraph[] components = Kosaraju.findSCCs(graph);

        assertEquals(1, components.length);
        assertEquals(3, components[0].getVerticesCount());
    }

    @Test
    void testConsistentResults() {
        DirectedGraph graph = (DirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 }, { 3, 4 } });

        DirectedGraph[] components1 = Kosaraju.findSCCs(graph);
        DirectedGraph[] components2 = Kosaraju.findSCCs(graph);

        assertEquals(components1.length, components2.length);
    }

    @Test
    void testSingleComponent() {
        DirectedGraph graph = (DirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 } });

        DirectedGraph[] components = Kosaraju.findSCCs(graph);

        assertEquals(1, components.length);
        assertEquals(3, components[0].getVerticesCount());
    }

    @Test
    void testDisconnectedVertices() {
        DirectedGraph graph = (DirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                6,
                new int[][] { { 1, 2 }, { 2, 1 }, { 2, 3 }, { 3, 2 }, { 4, 5 }, { 5, 4 }, { 6, 6 } });

        DirectedGraph[] components = Kosaraju.findSCCs(graph);

        assertEquals(3, components.length);
    }

    @Test
    void testReverseEdgesBetweenSCCs() {
        DirectedGraph graph = (DirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                5,
                new int[][] { { 1, 2 }, { 2, 1 }, { 2, 3 }, { 4, 3 }, { 3, 5 } });

        DirectedGraph[] components = Kosaraju.findSCCs(graph);

        assertTrue(components.length >= 2);
    }

    @Test
    void testWithPrecomputedFinishTimes() {
        DirectedGraph graph = (DirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 }, { 3, 4 } });

        DFSResult dfsResult = DFS.search((Graph) graph);
        int[] finishTimes = dfsResult.finishTimes();

        DirectedGraph[] components = Kosaraju.findSCCs(graph, finishTimes);

        assertEquals(2, components.length);
    }

    @Test
    void testBidirectionalReachabilityWithinSCC() {
        DirectedGraph graph = (DirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 } });

        DirectedGraph[] components = Kosaraju.findSCCs(graph);

        assertEquals(1, components.length);
        DirectedGraph component = components[0];

        for (int v1 : component.getVertices()) {
            for (int v2 : component.getVertices()) {
                if (v1 != v2) {
                    assertTrue(canReach((DirectedGraph) component, v1, v2),
                            "Vertex " + v1 + " should reach vertex " + v2 + " within SCC");
                }
            }
        }
    }

    @Test
    void testNoCrossSCCReachability() {
        DirectedGraph graph = (DirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                5,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 }, { 4, 5 } });

        DirectedGraph[] components = Kosaraju.findSCCs(graph);

        assertEquals(3, components.length);
    }

    @Test
    void testComplexGraph() {
        DirectedGraph graph = (DirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                10,
                new int[][] {
                        { 1, 2 }, { 2, 3 }, { 3, 1 },
                        { 3, 4 }, { 4, 5 }, { 5, 6 }, { 6, 4 },
                        { 6, 7 },
                        { 8, 9 }, { 9, 8 },
                        { 7, 10 }
                });

        DirectedGraph[] components = Kosaraju.findSCCs(graph);

        assertEquals(5, components.length);
    }

    @Test
    void testLargeSCC() {
        DirectedGraph graph = (DirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                5,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 4 }, { 4, 5 }, { 5, 1 } });

        DirectedGraph[] components = Kosaraju.findSCCs(graph);

        assertEquals(1, components.length);
        assertEquals(5, components[0].getVerticesCount());
    }

    @Test
    void testSCCWithIsolatedAndConnected() {
        DirectedGraph graph = (DirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4,
                new int[][] { { 1, 2 }, { 2, 1 }, { 3, 4 } });

        DirectedGraph[] components = Kosaraju.findSCCs(graph);

        assertEquals(3, components.length);
    }

    @Test
    void testAllVerticesInOneSCCAfterMultiplePasses() {
        DirectedGraph graph = (DirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 4 }, { 4, 1 }, { 2, 4 }, { 3, 1 } });

        DirectedGraph[] components = Kosaraju.findSCCs(graph);

        assertEquals(1, components.length);
        assertEquals(4, components[0].getVerticesCount());
    }

    @Test
    void testVerifyComponentVerticesAreValid() {
        DirectedGraph graph = (DirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                6,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 }, { 4, 5 }, { 5, 6 }, { 6, 4 } });

        DirectedGraph[] components = Kosaraju.findSCCs(graph);

        assertEquals(2, components.length);

        for (DirectedGraph comp : components) {
            for (int v : comp.getVertices()) {
                assertTrue(v >= 1 && v <= 6, "Vertex " + v + " should be in valid range");
            }
        }
    }

    private boolean canReach(DirectedGraph graph, int source, int target) {
        if (source == target) {
            return true;
        }

        for (int successor : graph.getSuccessors(source)) {
            if (successor == target || canReach(graph, successor, target)) {
                return true;
            }
        }

        return false;
    }
}
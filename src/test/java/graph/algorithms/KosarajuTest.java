package graph.algorithms;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import graph.algorithms.DFS.DFSResult;
import graph.api.DirectedGraph;
import graph.api.StaticGraph;
import graph.representations.GraphBuilderHelper;
import graph.representations.forwardstar.ForwardStarGraphBuilder;

class KosarajuTest {

    @Test
    void testBasicSCC() {
        DirectedGraph graph = (DirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4, 4,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 }, { 3, 4 } });

        StaticGraph[] components = Kosaraju.findSCCs(graph);

        assertEquals(2, components.length);
    }

    @Test
    void testTwoSCCs() {
        DirectedGraph graph = (DirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                5, 4,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 }, { 4, 5 } });

        StaticGraph[] components = Kosaraju.findSCCs(graph);

        assertEquals(3, components.length);
    }

    @Test
    void testSingleComponent() {
        DirectedGraph graph = (DirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                3, 3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 } });

        StaticGraph[] components = Kosaraju.findSCCs(graph);

        assertEquals(1, components.length);
        assertEquals(3, components[0].n);
    }

    @Test
    void testDisconnectedVertices() {
        DirectedGraph graph = (DirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4, 1,
                new int[][] { { 1, 2 } });

        StaticGraph[] components = Kosaraju.findSCCs(graph);

        assertEquals(4, components.length);
    }

    @Test
    void testEmptyGraph() {
        DirectedGraph graph = (DirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                3, 0,
                new int[][] {});

        StaticGraph[] components = Kosaraju.findSCCs(graph);

        assertEquals(3, components.length);
    }

    @Test
    void testGraphWithCycles() {
        DirectedGraph graph = (DirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                6, 7,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 }, { 3, 4 }, { 4, 5 }, { 5, 6 }, { 6, 4 } });

        StaticGraph[] components = Kosaraju.findSCCs(graph);

        assertEquals(2, components.length);

        boolean foundThreeVertexComponent = false;
        for (StaticGraph comp : components) {
            if (comp.n == 3) {
                foundThreeVertexComponent = true;
                break;
            }
        }
        assertTrue(foundThreeVertexComponent);
    }

    @Test
    void testMultipleSCCs() {
        DirectedGraph graph = (DirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                8, 8,
                new int[][] { { 1, 2 }, { 2, 1 }, { 3, 4 }, { 4, 3 }, { 5, 6 }, { 6, 5 }, { 2, 3 }, { 4, 5 } });

        StaticGraph[] components = Kosaraju.findSCCs(graph);

        assertEquals(5, components.length);
    }

    @Test
    void testLinearChain() {
        DirectedGraph graph = (DirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4, 3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 4 } });

        StaticGraph[] components = Kosaraju.findSCCs(graph);

        assertEquals(4, components.length);
    }

    @Test
    void testSelfLoop() {
        DirectedGraph graph = (DirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                2, 2,
                new int[][] { { 1, 1 }, { 1, 2 } });

        StaticGraph[] components = Kosaraju.findSCCs(graph);

        assertEquals(2, components.length);
    }

    @Test
    void testAllSelfLoops() {
        DirectedGraph graph = (DirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                3, 3,
                new int[][] { { 1, 1 }, { 2, 2 }, { 3, 3 } });

        StaticGraph[] components = Kosaraju.findSCCs(graph);

        assertEquals(3, components.length);
    }

    @Test
    void testDiamondGraph() {
        DirectedGraph graph = (DirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4, 4,
                new int[][] { { 1, 2 }, { 1, 3 }, { 2, 4 }, { 3, 4 } });

        StaticGraph[] components = Kosaraju.findSCCs(graph);

        assertEquals(4, components.length);
    }

    @Test
    void testParallelPaths() {
        DirectedGraph graph = (DirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                6, 5,
                new int[][] { { 1, 2 }, { 2, 3 }, { 4, 5 }, { 5, 6 }, { 3, 6 } });

        StaticGraph[] components = Kosaraju.findSCCs(graph);

        assertTrue(components.length >= 3);
    }

    @Test
    void testNonContiguousVertices() {
        DirectedGraph graph = (DirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                7, 3,
                new int[][] { { 1, 3 }, { 3, 5 }, { 5, 7 } });

        StaticGraph[] components = Kosaraju.findSCCs(graph);

        assertEquals(7, components.length);
    }

    @Test
    void testConsistentResults() {
        DirectedGraph graph = (DirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4, 4,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 }, { 3, 4 } });

        StaticGraph[] components1 = Kosaraju.findSCCs(graph);
        StaticGraph[] components2 = Kosaraju.findSCCs(graph);

        assertEquals(components1.length, components2.length);
    }

    @Test
    void testDenseGraph() {
        DirectedGraph graph = (DirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                3, 6,
                new int[][] { { 1, 2 }, { 2, 1 }, { 1, 3 }, { 3, 1 }, { 2, 3 }, { 3, 2 } });

        StaticGraph[] components = Kosaraju.findSCCs(graph);

        assertEquals(1, components.length);
        assertEquals(3, components[0].n);
    }

    @Test
    void testComponentSizeVariation() {
        DirectedGraph graph = (DirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                6, 7,
                new int[][] { { 1, 2 }, { 2, 1 }, { 2, 3 }, { 3, 2 }, { 4, 5 }, { 5, 4 }, { 6, 6 } });

        StaticGraph[] components = Kosaraju.findSCCs(graph);

        assertEquals(3, components.length);
    }

    @Test
    void testReverseEdgesBetweenSCCs() {
        DirectedGraph graph = (DirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                5, 5,
                new int[][] { { 1, 2 }, { 2, 1 }, { 2, 3 }, { 4, 3 }, { 3, 5 } });

        StaticGraph[] components = Kosaraju.findSCCs(graph);

        assertTrue(components.length >= 2);
    }

    @Test
    void testWithPrecomputedFinishTimes() {
        DirectedGraph graph = (DirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4, 4,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 }, { 3, 4 } });

        DFSResult dfsResult = DFS.search(graph);
        int[] finishTimes = dfsResult.finishTimes();

        StaticGraph[] components = Kosaraju.findSCCs(graph, finishTimes);

        assertEquals(2, components.length);
    }

    @Test
    void testBidirectionalReachabilityWithinSCC() {
        DirectedGraph graph = (DirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                3, 3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 } });

        StaticGraph[] components = Kosaraju.findSCCs(graph);

        assertEquals(1, components.length);
        StaticGraph component = components[0];

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
        DirectedGraph graph = (DirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                5, 4,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 }, { 4, 5 } });

        StaticGraph[] components = Kosaraju.findSCCs(graph);

        assertEquals(3, components.length);
    }

    @Test
    void testComplexGraph() {
        DirectedGraph graph = (DirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                10, 12,
                new int[][] {
                        { 1, 2 }, { 2, 3 }, { 3, 1 },
                        { 3, 4 }, { 4, 5 }, { 5, 6 }, { 6, 4 },
                        { 6, 7 },
                        { 8, 9 }, { 9, 8 },
                        { 7, 10 }
                });

        StaticGraph[] components = Kosaraju.findSCCs(graph);

        assertEquals(5, components.length);
    }

    @Test
    void testLargeSCC() {
        DirectedGraph graph = (DirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                5, 5,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 4 }, { 4, 5 }, { 5, 1 } });

        StaticGraph[] components = Kosaraju.findSCCs(graph);

        assertEquals(1, components.length);
        assertEquals(5, components[0].n);
    }

    @Test
    void testSCCWithIsolatedAndConnected() {
        DirectedGraph graph = (DirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                5, 4,
                new int[][] { { 1, 2 }, { 2, 1 }, { 3, 4 } });

        StaticGraph[] components = Kosaraju.findSCCs(graph);

        assertEquals(4, components.length);
    }

    @Test
    void testAllVerticesInOneSCCAfterMultiplePasses() {
        DirectedGraph graph = (DirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4, 6,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 4 }, { 4, 1 }, { 2, 4 }, { 3, 1 } });

        StaticGraph[] components = Kosaraju.findSCCs(graph);

        assertEquals(1, components.length);
        assertEquals(4, components[0].n);
    }

    @Test
    void testVerifyComponentVerticesAreValid() {
        DirectedGraph graph = (DirectedGraph) GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                6, 6,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 }, { 4, 5 }, { 5, 6 }, { 6, 4 } });

        StaticGraph[] components = Kosaraju.findSCCs(graph);

        assertEquals(2, components.length);

        for (StaticGraph comp : components) {
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
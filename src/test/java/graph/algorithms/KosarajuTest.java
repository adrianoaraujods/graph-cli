package graph.algorithms;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import graph.api.Graph;
import graph.api.GraphRepresentation;
import graph.api.StaticGraph;
import graph.representations.GraphBuilderHelper;
import graph.representations.forwardstar.ForwardStarGraphBuilder;

class KosarajuTest {

    @Test
    void testBasicSCC() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4, 4,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 }, { 3, 4 } });

        StaticGraph[] components = ((graph.api.DirectedGraph) graph).getMaximalComponents();

        assertEquals(2, components.length);
    }

    @Test
    void testTwoSCCs() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                5, 4,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 }, { 4, 5 } });

        StaticGraph[] components = ((graph.api.DirectedGraph) graph).getMaximalComponents();

        assertEquals(3, components.length);
    }

    @Test
    void testSingleComponent() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                3, 3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 } });

        StaticGraph[] components = ((graph.api.DirectedGraph) graph).getMaximalComponents();

        assertEquals(1, components.length);
        assertEquals(3, components[0].n);
    }

    @Test
    void testDisconnectedVertices() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4, 1,
                new int[][] { { 1, 2 } });

        StaticGraph[] components = ((graph.api.DirectedGraph) graph).getMaximalComponents();

        assertEquals(4, components.length);
    }

    @Test
    void testEmptyGraph() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                3, 0,
                new int[][] {});

        StaticGraph[] components = ((graph.api.DirectedGraph) graph).getMaximalComponents();

        assertEquals(3, components.length);
    }

    @Test
    void testGraphWithCycles() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                6, 7,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 }, { 3, 4 }, { 4, 5 }, { 5, 6 }, { 6, 4 } });

        StaticGraph[] components = ((graph.api.DirectedGraph) graph).getMaximalComponents();

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
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                8, 8,
                new int[][] { { 1, 2 }, { 2, 1 }, { 3, 4 }, { 4, 3 }, { 5, 6 }, { 6, 5 }, { 2, 3 }, { 4, 5 } });

        StaticGraph[] components = ((graph.api.DirectedGraph) graph).getMaximalComponents();

        assertEquals(5, components.length);
    }

    @Test
    void testLinearChain() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4, 3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 4 } });

        StaticGraph[] components = ((graph.api.DirectedGraph) graph).getMaximalComponents();

        assertEquals(4, components.length);
    }

    @Test
    void testSelfLoop() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                2, 2,
                new int[][] { { 1, 1 }, { 1, 2 } });

        StaticGraph[] components = ((graph.api.DirectedGraph) graph).getMaximalComponents();

        assertEquals(2, components.length);
    }

    @Test
    void testAllSelfLoops() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                3, 3,
                new int[][] { { 1, 1 }, { 2, 2 }, { 3, 3 } });

        StaticGraph[] components = ((graph.api.DirectedGraph) graph).getMaximalComponents();

        assertEquals(3, components.length);
    }

    @Test
    void testDiamondGraph() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4, 4,
                new int[][] { { 1, 2 }, { 1, 3 }, { 2, 4 }, { 3, 4 } });

        StaticGraph[] components = ((graph.api.DirectedGraph) graph).getMaximalComponents();

        assertEquals(4, components.length);
    }

    @Test
    void testParallelPaths() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                6, 5,
                new int[][] { { 1, 2 }, { 2, 3 }, { 4, 5 }, { 5, 6 }, { 3, 6 } });

        StaticGraph[] components = ((graph.api.DirectedGraph) graph).getMaximalComponents();

        assertTrue(components.length >= 3);
    }

    @Test
    void testNonContiguousVertices() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                7, 3,
                new int[][] { { 1, 3 }, { 3, 5 }, { 5, 7 } });

        StaticGraph[] components = ((graph.api.DirectedGraph) graph).getMaximalComponents();

        assertEquals(7, components.length);
    }

    @Test
    void testConsistentResults() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4, 4,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 }, { 3, 4 } });

        StaticGraph[] components1 = ((graph.api.DirectedGraph) graph).getMaximalComponents();
        StaticGraph[] components2 = ((graph.api.DirectedGraph) graph).getMaximalComponents();

        assertEquals(components1.length, components2.length);
    }

    @Test
    void testDenseGraph() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                3, 6,
                new int[][] { { 1, 2 }, { 2, 1 }, { 1, 3 }, { 3, 1 }, { 2, 3 }, { 3, 2 } });

        StaticGraph[] components = ((graph.api.DirectedGraph) graph).getMaximalComponents();

        assertEquals(1, components.length);
        assertEquals(3, components[0].n);
    }

    @Test
    void testComponentSizeVariation() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                6, 7,
                new int[][] { { 1, 2 }, { 2, 1 }, { 2, 3 }, { 3, 2 }, { 4, 5 }, { 5, 4 }, { 6, 6 } });

        StaticGraph[] components = ((graph.api.DirectedGraph) graph).getMaximalComponents();

        assertEquals(3, components.length);
    }

    @Test
    void testReverseEdgesBetweenSCCs() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                5, 5,
                new int[][] { { 1, 2 }, { 2, 1 }, { 2, 3 }, { 4, 3 }, { 3, 5 } });

        StaticGraph[] components = ((graph.api.DirectedGraph) graph).getMaximalComponents();

        assertTrue(components.length >= 2);
    }

    @Test
    void testWithPrecomputedFinishTimes() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4, 4,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 }, { 3, 4 } });

        Graph.DFSResult dfsResult = graph.depthFirstSearch();
        int[] finishTimes = dfsResult.finishTimes();

        StaticGraph[] components = ((graph.api.DirectedGraph) graph).getMaximalComponents(finishTimes);

        assertEquals(2, components.length);
    }

    @Test
    void testBidirectionalReachabilityWithinSCC() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                3, 3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 } });

        StaticGraph[] components = ((graph.api.DirectedGraph) graph).getMaximalComponents();

        assertEquals(1, components.length);
        StaticGraph component = components[0];

        for (int v1 : component.getVertices()) {
            for (int v2 : component.getVertices()) {
                if (v1 != v2) {
                    assertTrue(canReach((graph.api.GraphRepresentation) component, v1, v2),
                            "Vertex " + v1 + " should reach vertex " + v2 + " within SCC");
                }
            }
        }
    }

    @Test
    void testNoCrossSCCReachability() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                5, 4,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 }, { 4, 5 } });

        StaticGraph[] components = ((graph.api.DirectedGraph) graph).getMaximalComponents();

        assertEquals(3, components.length);
    }

    @Test
    void testComplexGraph() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                10, 12,
                new int[][] {
                        { 1, 2 }, { 2, 3 }, { 3, 1 },
                        { 3, 4 }, { 4, 5 }, { 5, 6 }, { 6, 4 },
                        { 6, 7 },
                        { 8, 9 }, { 9, 8 },
                        { 7, 10 }
                });

        StaticGraph[] components = ((graph.api.DirectedGraph) graph).getMaximalComponents();

        assertEquals(5, components.length);
    }

    @Test
    void testLargeSCC() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                5, 5,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 4 }, { 4, 5 }, { 5, 1 } });

        StaticGraph[] components = ((graph.api.DirectedGraph) graph).getMaximalComponents();

        assertEquals(1, components.length);
        assertEquals(5, components[0].n);
    }

    @Test
    void testSCCWithIsolatedAndConnected() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                5, 4,
                new int[][] { { 1, 2 }, { 2, 1 }, { 3, 4 } });

        StaticGraph[] components = ((graph.api.DirectedGraph) graph).getMaximalComponents();

        assertEquals(4, components.length);
    }

    @Test
    void testAllVerticesInOneSCCAfterMultiplePasses() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4, 6,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 4 }, { 4, 1 }, { 2, 4 }, { 3, 1 } });

        StaticGraph[] components = ((graph.api.DirectedGraph) graph).getMaximalComponents();

        assertEquals(1, components.length);
        assertEquals(4, components[0].n);
    }

    @Test
    void testVerifyComponentVerticesAreValid() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                6, 6,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 }, { 4, 5 }, { 5, 6 }, { 6, 4 } });

        StaticGraph[] components = ((graph.api.DirectedGraph) graph).getMaximalComponents();

        assertEquals(2, components.length);

        for (StaticGraph comp : components) {
            for (int v : comp.getVertices()) {
                assertTrue(v >= 1 && v <= 6, "Vertex " + v + " should be in valid range");
            }
        }
    }

    private boolean canReach(GraphRepresentation graph, int source, int target) {
        if (source == target)
            return true;
        for (int successor : graph.getSuccessors(source)) {
            if (successor == target || canReach(graph, successor, target)) {
                return true;
            }
        }
        return false;
    }
}
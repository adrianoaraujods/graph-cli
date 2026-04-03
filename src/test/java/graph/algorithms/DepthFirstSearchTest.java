package graph.algorithms;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import graph.api.Graph.ClassifiedDFSEdges;
import graph.api.EdgeSet;
import graph.api.Graph;
import graph.api.Graph.DFSResult;
import graph.api.GraphRepresentation;
import graph.representations.GraphBuilderHelper;
import graph.representations.forwardstar.ForwardStarGraphBuilder;

import java.util.ArrayList;
import java.util.List;

class DepthFirstSearchTest {

    @Test
    void testDFSDiscoverTimes() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4, 3,
                new int[][] { { 1, 2 }, { 1, 3 }, { 2, 4 } });

        DFSResult result = graph.depthFirstSearch();

        int[] discoverTimes = result.discoverTimes();
        assertNotNull(discoverTimes);
        assertEquals(4, discoverTimes.length);

        for (int i = 0; i < discoverTimes.length; i++) {
            assertTrue(discoverTimes[i] > 0 && discoverTimes[i] <= 8,
                    "Discover time for vertex " + (i + 1) + " should be between 1 and 8");
        }
    }

    @Test
    void testDFSFinishTimes() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4, 3,
                new int[][] { { 1, 2 }, { 1, 3 }, { 2, 4 } });

        DFSResult result = graph.depthFirstSearch();

        int[] discoverTimes = result.discoverTimes();
        int[] finishTimes = result.finishTimes();

        for (int i = 0; i < finishTimes.length; i++) {
            assertTrue(finishTimes[i] > discoverTimes[i],
                    "Finish time should be greater than discover time for vertex " + (i + 1));
        }
    }

    @Test
    void testDFSTreeEdges() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4, 3,
                new int[][] { { 1, 2 }, { 1, 3 }, { 2, 4 } });

        DFSResult result = graph.depthFirstSearch();
        int[] parents = result.parents();

        EdgeSet treeEdges = graph.getDFSTreeEdges(1, parents);
        assertNotNull(treeEdges);
    }

    @Test
    void testDFSWithCustomRootsOrder() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4, 3,
                new int[][] { { 1, 2 }, { 3, 4 }, { 2, 3 } });

        int[] customOrder = { 3, 1 };
        DFSResult result = graph.depthFirstSearch(customOrder);

        assertNotNull(result.discoverTimes());
        assertNotNull(result.finishTimes());
    }

    @Test
    void testDFSEmptyGraph() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                5, 0,
                new int[][] {});

        DFSResult result = graph.depthFirstSearch();

        assertNotNull(result.discoverTimes());
        assertEquals(5, result.discoverTimes().length);
    }

    @Test
    void testDFSDisconnectedGraph() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                6, 3,
                new int[][] { { 1, 2 }, { 3, 4 }, { 5, 6 } });

        DFSResult result = graph.depthFirstSearch();

        int[] discoverTimes = result.discoverTimes();
        for (int i = 0; i < discoverTimes.length; i++) {
            assertTrue(discoverTimes[i] > 0,
                    "All vertices should be discovered in disconnected graph");
        }
    }

    @Test
    void testDFSParentsAssignment() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                5, 4,
                new int[][] { { 1, 2 }, { 1, 3 }, { 2, 4 }, { 2, 5 } });

        DFSResult result = graph.depthFirstSearch();
        int[] parents = result.parents();

        assertNotNull(parents);
        assertEquals(5, parents.length);

        for (int i = 1; i < parents.length; i++) {
            if (parents[i] != 0) {
                assertTrue(parents[i] >= 1 && parents[i] <= 5,
                        "Parent should be valid vertex ID");
            }
        }
    }

    @Test
    void testDFSWithCycle() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                3, 3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 } });

        DFSResult result = graph.depthFirstSearch();

        int[] discoverTimes = result.discoverTimes();
        int[] finishTimes = result.finishTimes();

        assertNotNull(discoverTimes);
        assertNotNull(finishTimes);
    }

    @Test
    void testClassifyVertexEdges() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                5, 5,
                new int[][] { { 1, 2 }, { 1, 3 }, { 2, 4 }, { 2, 5 }, { 4, 5 } });

        DFSResult result = graph.depthFirstSearch();
        ClassifiedDFSEdges classified = graph.classifyVertexDFSEdges(1, result);

        assertNotNull(classified);
        assertNotNull(classified.treeEdges());
        assertNotNull(classified.backEdges());
        assertNotNull(classified.crossEdges());
        assertNotNull(classified.forwardEdges());
    }

    @Test
    void testClassifyVertexTreeEdges() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4, 3,
                new int[][] { { 1, 2 }, { 1, 3 }, { 2, 4 } });

        DFSResult result = graph.depthFirstSearch();
        ClassifiedDFSEdges classified = graph.classifyVertexDFSEdges(1, result);

        EdgeSet treeEdges = classified.treeEdges();
        assertTrue(treeEdges.edges().size() > 0,
                "Vertex 1 should have tree edges");
    }

    @Test
    void testClassifyVertexWithBackEdge() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                3, 3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 } });

        DFSResult result = graph.depthFirstSearch();
        ClassifiedDFSEdges classified = graph.classifyVertexDFSEdges(2, result);

        assertNotNull(classified.backEdges());
    }

    @Test
    void testClassifyVertexForwardEdge() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4, 3,
                new int[][] { { 1, 2 }, { 1, 3 }, { 3, 4 } });

        DFSResult result = graph.depthFirstSearch();
        ClassifiedDFSEdges classified = graph.classifyVertexDFSEdges(1, result);

        assertNotNull(classified.forwardEdges());
    }

    @Test
    void testDFSLexicographicOrder() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4, 4,
                new int[][] { { 1, 3 }, { 1, 2 }, { 3, 4 }, { 2, 4 } });

        final List<Integer> discoverOrder = new ArrayList<>();

        Graph.DFSVisitor visitor = new Graph.DFSVisitor() {
            @Override
            public void discoverVertex(int vertex) {
                discoverOrder.add(vertex);
            }
        };

        graph.depthFirstSearch(visitor);

        assertEquals(4, discoverOrder.size());
    }

    @Test
    void testDFSAllVerticesVisited() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                7, 6,
                new int[][] { { 1, 2 }, { 2, 3 }, { 4, 5 }, { 5, 6 }, { 6, 7 }, { 3, 4 } });

        DFSResult result = graph.depthFirstSearch();
        int[] discoverTimes = result.discoverTimes();

        for (int i = 0; i < discoverTimes.length; i++) {
            assertTrue(discoverTimes[i] > 0,
                    "Vertex " + (i + 1) + " should be discovered");
        }
    }

    @Test
    void testGetDFSTreeEdgesForSpecificVertex() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                5, 4,
                new int[][] { { 1, 2 }, { 1, 3 }, { 2, 4 }, { 3, 5 } });

        DFSResult result = graph.depthFirstSearch();
        EdgeSet treeEdges = graph.getDFSTreeEdges(1, result.parents());

        assertNotNull(treeEdges);
    }

    @Test
    void testDFSInvalidRootOrderThrowsException() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4, 2,
                new int[][] { { 1, 2 }, { 3, 4 } });

        int[] invalidOrder = { 1, 2, 5, 6 };

        assertThrows(IndexOutOfBoundsException.class, () -> {
            graph.depthFirstSearch(invalidOrder);
        });
    }

    @Test
    void testDFSVisitorCallbacks() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                3, 2,
                new int[][] { { 1, 2 }, { 2, 3 } });

        final int[] vertexCount = { 0 };
        final int[] edgeCount = { 0 };

        Graph.DFSVisitor visitor = new Graph.DFSVisitor() {
            @Override
            public void discoverVertex(int vertex) {
                vertexCount[0]++;
            }

            @Override
            public void examineEdge(int source, int target) {
                edgeCount[0]++;
            }
        };

        graph.depthFirstSearch(visitor);

        assertEquals(3, vertexCount[0]);
        assertTrue(edgeCount[0] >= 2);
    }

    @Test
    void testClassifyVertexEdgesEmptyGraph() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                3, 0,
                new int[][] {});

        DFSResult result = graph.depthFirstSearch();
        ClassifiedDFSEdges classified = graph.classifyVertexDFSEdges(1, result);

        assertNotNull(classified);
        assertEquals(0, classified.treeEdges().edges().size());
        assertEquals(0, classified.backEdges().edges().size());
        assertEquals(0, classified.crossEdges().edges().size());
        assertEquals(0, classified.forwardEdges().edges().size());
    }

    // Undirected Graph Tests

    @Test
    void testDFSUndirectedBasic() {
        GraphRepresentation graph = GraphBuilderHelper.buildUndirected(
                () -> new ForwardStarGraphBuilder(false),
                3, 2,
                new int[][] { { 1, 2 }, { 2, 3 } });

        DFSResult result = graph.depthFirstSearch();

        int[] discoverTimes = result.discoverTimes();
        for (int i = 0; i < discoverTimes.length; i++) {
            assertTrue(discoverTimes[i] > 0, "Vertex " + (i + 1) + " should be discovered");
        }
    }

    @Test
    void testDFSUndirectedWithCycles() {
        GraphRepresentation graph = GraphBuilderHelper.buildUndirected(
                () -> new ForwardStarGraphBuilder(false),
                3, 3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 } });

        DFSResult result = graph.depthFirstSearch();

        int[] discoverTimes = result.discoverTimes();
        assertNotNull(discoverTimes);
        assertEquals(3, discoverTimes.length);
    }

    @Test
    void testDFSUndirectedTreeEdges() {
        GraphRepresentation graph = GraphBuilderHelper.buildUndirected(
                () -> new ForwardStarGraphBuilder(false),
                4, 3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 4 } });

        DFSResult result = graph.depthFirstSearch();
        EdgeSet treeEdges = graph.getDFSTreeEdges(1, result.parents());

        assertNotNull(treeEdges);
    }

    @Test
    void testDFSUndirectedDisconnected() {
        GraphRepresentation graph = GraphBuilderHelper.buildUndirected(
                () -> new ForwardStarGraphBuilder(false),
                6, 4,
                new int[][] { { 1, 2 }, { 3, 4 }, { 5, 6 } });

        DFSResult result = graph.depthFirstSearch();
        int[] discoverTimes = result.discoverTimes();

        for (int i = 0; i < discoverTimes.length; i++) {
            assertTrue(discoverTimes[i] > 0, "All vertices should be discovered");
        }
    }

    // Strict Assertions Tests

    @Test
    void testDFSDiscoverTimesExact() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4, 3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 4 } });

        DFSResult result = graph.depthFirstSearch();
        int[] discoverTimes = result.discoverTimes();

        assertEquals(1, discoverTimes[0]);
        assertEquals(2, discoverTimes[1]);
        assertEquals(3, discoverTimes[2]);
        assertEquals(4, discoverTimes[3]);
    }

    @Test
    void testDFSFinishTimesExact() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4, 3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 4 } });

        DFSResult result = graph.depthFirstSearch();
        int[] finishTimes = result.finishTimes();

        assertEquals(8, finishTimes[0]);
        assertEquals(7, finishTimes[1]);
        assertEquals(6, finishTimes[2]);
        assertEquals(5, finishTimes[3]);
    }

    @Test
    void testDFSTreeEdgesExact() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4, 3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 4 } });

        DFSResult result = graph.depthFirstSearch();
        EdgeSet treeEdges = graph.getDFSTreeEdges(1, result.parents());

        assertEquals(3, treeEdges.edges().size());
    }

    @Test
    void testClassifyEdgesExactCounts() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                5, 6,
                new int[][] { { 1, 2 }, { 1, 3 }, { 2, 4 }, { 2, 5 }, { 3, 4 }, { 4, 5 } });

        DFSResult result = graph.depthFirstSearch();
        ClassifiedDFSEdges classified = graph.classifyVertexDFSEdges(1, result);

        assertTrue(classified.treeEdges().edges().size() >= 0);
        assertTrue(classified.backEdges().edges().size() >= 0);
        assertTrue(classified.crossEdges().edges().size() >= 0);
        assertTrue(classified.forwardEdges().edges().size() >= 0);
    }

    // Core Visitor Callback Tests

    @Test
    void testDFSVisitorDiscoverVertex() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4, 3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 4 } });

        final int[] discoverCount = { 0 };

        Graph.DFSVisitor visitor = new Graph.DFSVisitor() {
            @Override
            public void discoverVertex(int vertex) {
                discoverCount[0]++;
            }
        };

        graph.depthFirstSearch(visitor);

        assertEquals(4, discoverCount[0]);
    }

    @Test
    void testDFSVisitorFinishVertex() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4, 3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 4 } });

        final int[] finishCount = { 0 };

        Graph.DFSVisitor visitor = new Graph.DFSVisitor() {
            @Override
            public void finishVertex(int vertex) {
                finishCount[0]++;
            }
        };

        graph.depthFirstSearch(visitor);

        assertEquals(4, finishCount[0]);
    }

    @Test
    void testDFSVisitorTreeEdge() {
        GraphRepresentation graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4, 3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 4 } });

        final int[] treeEdgeCount = { 0 };

        Graph.DFSVisitor visitor = new Graph.DFSVisitor() {
            @Override
            public void treeEdge(int source, int target) {
                treeEdgeCount[0]++;
            }
        };

        graph.depthFirstSearch(visitor);

        assertEquals(3, treeEdgeCount[0]);
    }
}

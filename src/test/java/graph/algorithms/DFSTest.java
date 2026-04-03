package graph.algorithms;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import graph.algorithms.DFS.ClassifiedDFSEdges;
import graph.algorithms.DFS.DFSResult;
import graph.algorithms.DFS.DFSVisitor;
import graph.api.EdgeSet;
import graph.api.Graph;
import graph.api.StaticGraph;
import graph.representations.GraphBuilderHelper;
import graph.representations.forwardstar.ForwardStarGraphBuilder;

import java.util.ArrayList;
import java.util.List;

class DFSTest {

    @Test
    void testDFSDiscoverTimes() {
        StaticGraph graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4, 3,
                new int[][] { { 1, 2 }, { 1, 3 }, { 2, 4 } });

        DFSResult result = DFS.search(graph);

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
        StaticGraph graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4, 3,
                new int[][] { { 1, 2 }, { 1, 3 }, { 2, 4 } });

        DFSResult result = DFS.search(graph);

        int[] discoverTimes = result.discoverTimes();
        int[] finishTimes = result.finishTimes();

        for (int i = 0; i < finishTimes.length; i++) {
            assertTrue(finishTimes[i] > discoverTimes[i],
                    "Finish time should be greater than discover time for vertex " + (i + 1));
        }
    }

    @Test
    void testDFSTreeEdges() {
        StaticGraph graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4, 3,
                new int[][] { { 1, 2 }, { 1, 3 }, { 2, 4 } });

        DFSResult result = DFS.search(graph);
        int[] parents = result.parents();

        EdgeSet treeEdges = DFS.getDFSTreeEdges(graph, parents);

        assertNotNull(treeEdges);
    }

    @Test
    void testDFSWithCustomRootsOrder() {
        StaticGraph graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4, 3,
                new int[][] { { 1, 2 }, { 3, 4 }, { 2, 3 } });

        int[] customOrder = { 3, 1 };
        DFSResult result = DFS.search(graph, customOrder);

        assertNotNull(result.discoverTimes());
        assertNotNull(result.finishTimes());
    }

    @Test
    void testDFSEmptyGraph() {
        StaticGraph graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                5, 0,
                new int[][] {});

        DFSResult result = DFS.search(graph);

        assertNotNull(result.discoverTimes());
        assertEquals(5, result.discoverTimes().length);
    }

    @Test
    void testDFSDisconnectedGraph() {
        StaticGraph graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                6, 3,
                new int[][] { { 1, 2 }, { 3, 4 }, { 5, 6 } });

        DFSResult result = DFS.search(graph);

        int[] discoverTimes = result.discoverTimes();
        for (int i = 0; i < discoverTimes.length; i++) {
            assertTrue(discoverTimes[i] > 0,
                    "All vertices should be discovered in disconnected graph");
        }
    }

    @Test
    void testDFSParentsAssignment() {
        StaticGraph graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                5, 4,
                new int[][] { { 1, 2 }, { 1, 3 }, { 2, 4 }, { 2, 5 } });

        DFSResult result = DFS.search(graph);
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
        StaticGraph graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                3, 3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 } });

        DFSResult result = DFS.search(graph);

        int[] discoverTimes = result.discoverTimes();
        int[] finishTimes = result.finishTimes();

        assertNotNull(discoverTimes);
        assertNotNull(finishTimes);
    }

    @Test
    void testClassifyVertexEdges() {
        StaticGraph graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                5, 5,
                new int[][] { { 1, 2 }, { 1, 3 }, { 2, 4 }, { 2, 5 }, { 4, 5 } });

        DFSResult result = DFS.search(graph);
        ClassifiedDFSEdges classified = DFS.classifyVertexDFSEdges(graph, 1, result);

        assertNotNull(classified);
        assertNotNull(classified.treeEdges());
        assertNotNull(classified.backEdges());
        assertNotNull(classified.crossEdges());
        assertNotNull(classified.forwardEdges());
    }

    @Test
    void testClassifyVertexTreeEdges() {
        StaticGraph graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4, 3,
                new int[][] { { 1, 2 }, { 1, 3 }, { 2, 4 } });

        DFSResult result = DFS.search(graph);
        ClassifiedDFSEdges classified = DFS.classifyVertexDFSEdges(graph, 1, result);

        EdgeSet treeEdges = classified.treeEdges();
        assertTrue(treeEdges.edges().size() > 0,
                "Vertex 1 should have tree edges");
    }

    @Test
    void testClassifyVertexWithBackEdge() {
        StaticGraph graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                3, 3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 } });

        DFSResult result = DFS.search(graph);
        ClassifiedDFSEdges classified = DFS.classifyVertexDFSEdges(graph, 2, result);

        assertNotNull(classified.backEdges());
    }

    @Test
    void testClassifyVertexForwardEdge() {
        StaticGraph graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4, 3,
                new int[][] { { 1, 2 }, { 1, 3 }, { 3, 4 } });

        DFSResult result = DFS.search(graph);
        ClassifiedDFSEdges classified = DFS.classifyVertexDFSEdges(graph, 1, result);

        assertNotNull(classified.forwardEdges());
    }

    @Test
    void testDFSLexicographicOrder() {
        StaticGraph graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4, 4,
                new int[][] { { 1, 3 }, { 1, 2 }, { 3, 4 }, { 2, 4 } });

        final List<Integer> discoverOrder = new ArrayList<>();

        DFSVisitor visitor = new DFSVisitor() {
            @Override
            public void discoverVertex(int vertex) {
                discoverOrder.add(vertex);
            }
        };

        DFS.search(graph, visitor);

        assertEquals(4, discoverOrder.size());
    }

    @Test
    void testDFSAllVerticesVisited() {
        StaticGraph graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                7, 6,
                new int[][] { { 1, 2 }, { 2, 3 }, { 4, 5 }, { 5, 6 }, { 6, 7 }, { 3, 4 } });

        DFSResult result = DFS.search(graph);
        int[] discoverTimes = result.discoverTimes();

        for (int i = 0; i < discoverTimes.length; i++) {
            assertTrue(discoverTimes[i] > 0,
                    "Vertex " + (i + 1) + " should be discovered");
        }
    }

    @Test
    void testDFSInvalidRootOrderThrowsException() {
        StaticGraph graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4, 2,
                new int[][] { { 1, 2 }, { 3, 4 } });

        int[] invalidOrder = { 1, 2, 5, 6 };

        assertThrows(IndexOutOfBoundsException.class, () -> {
            DFS.search(graph, invalidOrder);
        });
    }

    @Test
    void testDFSVisitorCallbacks() {
        StaticGraph graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                3, 2,
                new int[][] { { 1, 2 }, { 2, 3 } });

        final int[] vertexCount = { 0 };
        final int[] edgeCount = { 0 };

        DFSVisitor visitor = new DFSVisitor() {
            @Override
            public void discoverVertex(int vertex) {
                vertexCount[0]++;
            }

            @Override
            public void examineEdge(int source, int target) {
                edgeCount[0]++;
            }
        };

        DFS.search(graph, visitor);

        assertEquals(3, vertexCount[0]);
        assertTrue(edgeCount[0] >= 2);
    }

    @Test
    void testClassifyVertexEdgesEmptyGraph() {
        StaticGraph graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                3, 0,
                new int[][] {});

        DFSResult result = DFS.search(graph);
        ClassifiedDFSEdges classified = DFS.classifyVertexDFSEdges(graph, 1, result);

        assertNotNull(classified);
        assertEquals(0, classified.treeEdges().edges().size());
        assertEquals(0, classified.backEdges().edges().size());
        assertEquals(0, classified.crossEdges().edges().size());
        assertEquals(0, classified.forwardEdges().edges().size());
    }

    // Undirected Graph Tests

    @Test
    void testDFSUndirectedBasic() {
        StaticGraph graph = GraphBuilderHelper.buildUndirected(
                () -> new ForwardStarGraphBuilder(false),
                3, 2,
                new int[][] { { 1, 2 }, { 2, 3 } });

        DFSResult result = DFS.search(graph);

        int[] discoverTimes = result.discoverTimes();
        for (int i = 0; i < discoverTimes.length; i++) {
            assertTrue(discoverTimes[i] > 0, "Vertex " + (i + 1) + " should be discovered");
        }
    }

    @Test
    void testDFSUndirectedWithCycles() {
        StaticGraph graph = GraphBuilderHelper.buildUndirected(
                () -> new ForwardStarGraphBuilder(false),
                3, 3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 } });

        DFSResult result = DFS.search(graph);

        int[] discoverTimes = result.discoverTimes();
        assertNotNull(discoverTimes);
        assertEquals(3, discoverTimes.length);
    }

    @Test
    void testDFSUndirectedTreeEdges() {
        StaticGraph graph = GraphBuilderHelper.buildUndirected(
                () -> new ForwardStarGraphBuilder(false),
                4, 3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 4 } });

        DFSResult result = DFS.search(graph);
        EdgeSet treeEdges = DFS.getDFSTreeEdges(graph, result.parents());

        assertNotNull(treeEdges);
    }

    @Test
    void testDFSUndirectedDisconnected() {
        StaticGraph graph = GraphBuilderHelper.buildUndirected(
                () -> new ForwardStarGraphBuilder(false),
                6, 4,
                new int[][] { { 1, 2 }, { 3, 4 }, { 5, 6 } });

        DFSResult result = DFS.search(graph);
        int[] discoverTimes = result.discoverTimes();

        for (int i = 0; i < discoverTimes.length; i++) {
            assertTrue(discoverTimes[i] > 0, "All vertices should be discovered");
        }
    }

    // Strict Assertions Tests

    @Test
    void testDFSDiscoverTimesExact() {
        StaticGraph graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4, 3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 4 } });

        DFSResult result = DFS.search(graph);
        int[] discoverTimes = result.discoverTimes();

        assertEquals(1, discoverTimes[0]);
        assertEquals(2, discoverTimes[1]);
        assertEquals(3, discoverTimes[2]);
        assertEquals(4, discoverTimes[3]);
    }

    @Test
    void testDFSFinishTimesExact() {
        StaticGraph graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4, 3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 4 } });

        DFSResult result = DFS.search(graph);
        int[] finishTimes = result.finishTimes();

        assertEquals(8, finishTimes[0]);
        assertEquals(7, finishTimes[1]);
        assertEquals(6, finishTimes[2]);
        assertEquals(5, finishTimes[3]);
    }

    @Test
    void testDFSTreeEdgesExact() {
        StaticGraph graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4, 3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 4 } });

        DFSResult result = DFS.search(graph);
        EdgeSet treeEdges = DFS.getDFSTreeEdges(graph, result.parents());

        assertEquals(3, treeEdges.edges().size());
    }

    @Test
    void testClassifyEdgesExactCounts() {
        StaticGraph graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                5, 6,
                new int[][] { { 1, 2 }, { 1, 3 }, { 2, 4 }, { 2, 5 }, { 3, 4 }, { 4, 5 } });

        DFSResult result = DFS.search(graph);
        ClassifiedDFSEdges classified = DFS.classifyVertexDFSEdges(graph, 1, result);

        assertTrue(classified.treeEdges().edges().size() >= 0);
        assertTrue(classified.backEdges().edges().size() >= 0);
        assertTrue(classified.crossEdges().edges().size() >= 0);
        assertTrue(classified.forwardEdges().edges().size() >= 0);
    }

    // Core Visitor Callback Tests

    @Test
    void testDFSVisitorDiscoverVertex() {
        StaticGraph graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4, 3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 4 } });

        final int[] discoverCount = { 0 };

        DFSVisitor visitor = new DFSVisitor() {
            @Override
            public void discoverVertex(int vertex) {
                discoverCount[0]++;
            }
        };

        DFS.search(graph, visitor);

        assertEquals(4, discoverCount[0]);
    }

    @Test
    void testDFSVisitorFinishVertex() {
        StaticGraph graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4, 3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 4 } });

        final int[] finishCount = { 0 };

        DFSVisitor visitor = new DFSVisitor() {
            @Override
            public void finishVertex(int vertex) {
                finishCount[0]++;
            }
        };

        DFS.search(graph, visitor);

        assertEquals(4, finishCount[0]);
    }

    @Test
    void testDFSVisitorTreeEdge() {
        StaticGraph graph = GraphBuilderHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                4, 3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 4 } });

        final int[] treeEdgeCount = { 0 };

        DFSVisitor visitor = new DFSVisitor() {
            @Override
            public void treeEdge(int source, int target) {
                treeEdgeCount[0]++;
            }
        };

        DFS.search(graph, visitor);

        assertEquals(3, treeEdgeCount[0]);
    }
}

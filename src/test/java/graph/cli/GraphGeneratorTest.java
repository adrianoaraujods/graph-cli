package graph.cli;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GraphGeneratorTest {

    @TempDir
    Path tempDir;

    @Test
    void testSimpleUndirectedGraph() throws IOException {
        Path outputPath = tempDir.resolve("undirected_graph.txt");

        GraphGenerator.GraphConfig config = GraphGenerator.builder()
                .vertices(100)
                .edges(50)
                .directed(false)
                .connectivity(GraphGenerator.ConnectivityType.WEAKLY)
                .outputPath(outputPath.toString())
                .build();

        GraphGenerator.generateGraph(config);

        List<int[]> edges = parseGraphFile(outputPath);

        assertFalse(edges.isEmpty(), "Graph should have edges");
        verifyNoSelfLoops(edges);
        verifyNoDuplicateEdges(edges, false);
        verifyCorrectEdgeCount(edges, 50, false);
        verifyValidVertexRange(edges, 100);
    }

    @Test
    void testSimpleDirectedGraph() throws IOException {
        Path outputPath = tempDir.resolve("directed_graph.txt");

        GraphGenerator.GraphConfig config = GraphGenerator.builder()
                .vertices(100)
                .edges(50)
                .directed(true)
                .connectivity(GraphGenerator.ConnectivityType.WEAKLY)
                .outputPath(outputPath.toString())
                .build();

        GraphGenerator.generateGraph(config);

        List<int[]> edges = parseGraphFile(outputPath);

        assertFalse(edges.isEmpty(), "Graph should have edges");
        verifyNoSelfLoops(edges);
        verifyNoDuplicateEdges(edges, true);
        verifyCorrectEdgeCount(edges, 50, true);
        verifyValidVertexRange(edges, 100);
    }

    @Test
    void testUndirectedEdgeBidirectionality() throws IOException {
        Path outputPath = tempDir.resolve("bidirectional_graph.txt");

        GraphGenerator.GraphConfig config = GraphGenerator.builder()
                .vertices(100)
                .edges(50)
                .directed(false)
                .connectivity(GraphGenerator.ConnectivityType.WEAKLY)
                .outputPath(outputPath.toString())
                .build();

        GraphGenerator.generateGraph(config);

        List<int[]> edges = parseGraphFile(outputPath);

        verifyBidirectionalEdges(edges, 50);
    }

    @Test
    void testBasicFormatValidation() throws IOException {
        Path outputPath = tempDir.resolve("format_test.txt");

        GraphGenerator.GraphConfig config = GraphGenerator.builder()
                .vertices(500)
                .density(0.3)
                .directed(true)
                .connectivity(GraphGenerator.ConnectivityType.WEAKLY)
                .outputPath(outputPath.toString())
                .build();

        GraphGenerator.generateGraph(config);

        verifyFormatValidation(outputPath);
    }

    @Test
    void testDirectedGraphHeaderMatchesEdgeCount() throws IOException {
        Path outputPath = tempDir.resolve("directed_header_test.txt");

        GraphGenerator.GraphConfig config = GraphGenerator.builder()
                .vertices(500)
                .edges(200)
                .directed(true)
                .connectivity(GraphGenerator.ConnectivityType.WEAKLY)
                .outputPath(outputPath.toString())
                .build();

        GraphGenerator.generateGraph(config);

        List<String> lines = Files.readAllLines(outputPath);
        String[] header = lines.get(0).trim().split("\\s+");
        int headerM = Integer.parseInt(header[1]);

        int actualEdgeLines = lines.size() - 1;

        assertEquals(headerM, actualEdgeLines,
                "Directed graph: header edge count should match actual lines in file");
    }

    @Test
    void testUndirectedGraphHeaderMatchesUniqueEdges() throws IOException {
        Path outputPath = tempDir.resolve("undirected_header_test.txt");

        GraphGenerator.GraphConfig config = GraphGenerator.builder()
                .vertices(500)
                .edges(200)
                .directed(false)
                .connectivity(GraphGenerator.ConnectivityType.WEAKLY)
                .outputPath(outputPath.toString())
                .build();

        GraphGenerator.generateGraph(config);

        List<String> lines = Files.readAllLines(outputPath);
        String[] header = lines.get(0).trim().split("\\s+");
        int headerM = Integer.parseInt(header[1]);

        Set<String> uniqueEdgePairs = new HashSet<>();
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty())
                continue;
            String[] parts = line.split("\\s+");
            int v = Integer.parseInt(parts[0]);
            int w = Integer.parseInt(parts[1]);
            int min = Math.min(v, w);
            int max = Math.max(v, w);
            uniqueEdgePairs.add(min + "-" + max);
        }

        assertEquals(headerM, uniqueEdgePairs.size(),
                "Undirected graph: header edge count should match unique edge pairs");
    }

    @Test
    void testNoSelfLoopsWithLargeGraph() throws IOException {
        Path outputPath = tempDir.resolve("selfloop_test.txt");

        GraphGenerator.GraphConfig config = GraphGenerator.builder()
                .vertices(1000)
                .density(0.8)
                .directed(true)
                .connectivity(GraphGenerator.ConnectivityType.WEAKLY)
                .outputPath(outputPath.toString())
                .build();

        GraphGenerator.generateGraph(config);

        List<int[]> edges = parseGraphFile(outputPath);
        verifyNoSelfLoops(edges);
    }

    @Test
    void testUndirectedNoSelfLoops() throws IOException {
        Path outputPath = tempDir.resolve("undirected_selfloop_test.txt");

        GraphGenerator.GraphConfig config = GraphGenerator.builder()
                .vertices(500)
                .density(0.9)
                .directed(false)
                .connectivity(GraphGenerator.ConnectivityType.WEAKLY)
                .outputPath(outputPath.toString())
                .build();

        GraphGenerator.generateGraph(config);

        List<int[]> edges = parseGraphFile(outputPath);
        verifyNoSelfLoops(edges);
    }

    @Test
    void testSparseGraphNoDuplicates() throws IOException {
        Path outputPath = tempDir.resolve("sparse_dup_test.txt");

        GraphGenerator.GraphConfig config = GraphGenerator.builder()
                .vertices(500)
                .density(0.05)
                .directed(true)
                .connectivity(GraphGenerator.ConnectivityType.WEAKLY)
                .outputPath(outputPath.toString())
                .build();

        GraphGenerator.generateGraph(config);

        List<int[]> edges = parseGraphFile(outputPath);
        verifyNoDuplicateEdges(edges, true);
    }

    @Test
    void testDenseGraphNoDuplicates() throws IOException {
        Path outputPath = tempDir.resolve("dense_dup_test.txt");

        GraphGenerator.GraphConfig config = GraphGenerator.builder()
                .vertices(500)
                .density(0.8)
                .directed(true)
                .connectivity(GraphGenerator.ConnectivityType.WEAKLY)
                .outputPath(outputPath.toString())
                .build();

        GraphGenerator.generateGraph(config);

        List<int[]> edges = parseGraphFile(outputPath);
        verifyNoDuplicateEdges(edges, true);
    }

    @Test
    void testUndirectedDenseNoDuplicates() throws IOException {
        Path outputPath = tempDir.resolve("undirected_dense_dup_test.txt");

        GraphGenerator.GraphConfig config = GraphGenerator.builder()
                .vertices(500)
                .density(0.8)
                .directed(false)
                .connectivity(GraphGenerator.ConnectivityType.WEAKLY)
                .outputPath(outputPath.toString())
                .build();

        GraphGenerator.generateGraph(config);

        List<int[]> edges = parseGraphFile(outputPath);
        verifyNoDuplicateEdges(edges, false);
    }

    @Test
    void testLargeScaleDirected() throws IOException {
        Path outputPath = tempDir.resolve("large_directed.txt");

        GraphGenerator.GraphConfig config = GraphGenerator.builder()
                .vertices(1000)
                .edges(10000)
                .directed(true)
                .connectivity(GraphGenerator.ConnectivityType.WEAKLY)
                .outputPath(outputPath.toString())
                .build();

        GraphGenerator.generateGraph(config);

        List<int[]> edges = parseGraphFile(outputPath);
        verifyNoSelfLoops(edges);
        verifyNoDuplicateEdges(edges, true);
    }

    @Test
    void testLargeScaleUndirected() throws IOException {
        Path outputPath = tempDir.resolve("large_undirected.txt");

        GraphGenerator.GraphConfig config = GraphGenerator.builder()
                .vertices(1000)
                .edges(10000)
                .directed(false)
                .connectivity(GraphGenerator.ConnectivityType.WEAKLY)
                .outputPath(outputPath.toString())
                .build();

        GraphGenerator.generateGraph(config);

        List<int[]> edges = parseGraphFile(outputPath);
        verifyNoSelfLoops(edges);
        verifyNoDuplicateEdges(edges, false);
    }

    private List<int[]> parseGraphFile(Path path) throws IOException {
        List<String> lines = Files.readAllLines(path);
        List<int[]> edges = new ArrayList<>();

        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty())
                continue;

            String[] parts = line.split("\\s+");
            int v = Integer.parseInt(parts[0]);
            int w = Integer.parseInt(parts[1]);
            edges.add(new int[] { v, w });
        }

        return edges;
    }

    private void verifyNoSelfLoops(List<int[]> edges) {
        for (int[] edge : edges) {
            assertNotEquals(edge[0], edge[1], "Graph should not contain self-loops");
        }
    }

    private void verifyNoDuplicateEdges(List<int[]> edges, boolean directed) {
        Set<String> seenEdges = new HashSet<>();

        for (int[] edge : edges) {
            String edgeKey;
            if (directed) {
                edgeKey = edge[0] + "-" + edge[1];
            } else {
                int min = Math.min(edge[0], edge[1]);
                int max = Math.max(edge[0], edge[1]);
                edgeKey = min + "-" + max;
            }

            assertFalse(seenEdges.contains(edgeKey), "Graph should not contain duplicate edges");
            seenEdges.add(edgeKey);
        }
    }

    private void verifyCorrectEdgeCount(List<int[]> edges, int expectedEdges, boolean directed) {
        int actualCount = edges.size();
        int expectedCount = directed ? expectedEdges : expectedEdges * 2;
        assertEquals(expectedCount, actualCount,
                directed ? "Directed graph should have exactly m edges"
                        : "Undirected graph should have 2m edges (both directions)");
    }

    private void verifyValidVertexRange(List<int[]> edges, int n) {
        for (int[] edge : edges) {
            assertTrue(edge[0] >= 1 && edge[0] <= n, "Vertex " + edge[0] + " out of range [1, " + n + "]");
            assertTrue(edge[1] >= 1 && edge[1] <= n, "Vertex " + edge[1] + " out of range [1, " + n + "]");
        }
    }

    private void verifyBidirectionalEdges(List<int[]> edges, int expectedPairs) {
        Set<String> edgePairs = new HashSet<>();

        for (int[] edge : edges) {
            int min = Math.min(edge[0], edge[1]);
            int max = Math.max(edge[0], edge[1]);
            edgePairs.add(min + "-" + max);
        }

        assertEquals(expectedPairs, edgePairs.size(), "Undirected graph should have exactly m unique edge pairs");
    }

    private void verifyFormatValidation(Path path) throws IOException {
        List<String> lines = Files.readAllLines(path);
        assertTrue(lines.size() >= 2, "File should have header and at least one edge");

        String[] header = lines.get(0).trim().split("\\s+");
        assertEquals(2, header.length, "Header should have two integers (n m)");
        int n = Integer.parseInt(header[0]);
        int m = Integer.parseInt(header[1]);

        assertTrue(n > 0, "Number of vertices should be positive");
        assertTrue(m >= 0, "Number of edges should be non-negative");

        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty())
                continue;

            String[] parts = line.split("\\s+");
            assertEquals(2, parts.length, "Edge line " + i + " should have exactly two integers");

            int v = Integer.parseInt(parts[0]);
            int w = Integer.parseInt(parts[1]);
            assertTrue(v >= 1 && v <= n, "Vertex v in line " + i + " out of range");
            assertTrue(w >= 1 && w <= n, "Vertex w in line " + i + " out of range");
        }
    }
}
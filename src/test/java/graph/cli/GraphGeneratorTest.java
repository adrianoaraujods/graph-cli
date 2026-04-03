package graph.cli;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
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
        verifyCorrectEdgeCount(edges, 50);
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
        verifyCorrectEdgeCount(edges, 50);
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

    @Test
    void testUndirectedSingleDirection() throws IOException {
        Path outputPath = tempDir.resolve("undirected_single_direction.txt");

        GraphGenerator.GraphConfig config = GraphGenerator.builder()
                .vertices(100)
                .edges(50)
                .directed(false)
                .connectivity(GraphGenerator.ConnectivityType.WEAKLY)
                .outputPath(outputPath.toString())
                .build();

        GraphGenerator.generateGraph(config);

        List<String> lines = Files.readAllLines(outputPath);
        String[] header = lines.get(0).trim().split("\\s+");
        int headerM = Integer.parseInt(header[1]);
        int edgeLines = lines.size() - 1;

        assertEquals(50, headerM, "Header should specify 50 edges");
        assertEquals(50, edgeLines, "File should have exactly 50 edge lines");
        verifyNoDuplicateEdges(parseGraphFile(outputPath), false);
    }

    @Test
    void testWeaklyConnected() throws IOException {
        Path outputPath = tempDir.resolve("weakly_connected.txt");

        GraphGenerator.GraphConfig config = GraphGenerator.builder()
                .vertices(100)
                .edges(150)
                .directed(true)
                .connectivity(GraphGenerator.ConnectivityType.WEAKLY)
                .outputPath(outputPath.toString())
                .build();

        GraphGenerator.generateGraph(config);

        List<int[]> edges = parseGraphFile(outputPath);
        Set<Integer>[] adj = buildAdjacencyList(100, edges, false);

        Set<Integer> visited = new HashSet<>();
        Queue<Integer> queue = new ArrayDeque<>();
        queue.add(1);
        visited.add(1);

        while (!queue.isEmpty()) {
            int v = queue.poll();
            for (int neighbor : adj[v]) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }

        assertEquals(100, visited.size(), "All vertices should be reachable");
    }

    @Test
    void testEulerianDegreeParity() throws IOException {
        Path outputPath = tempDir.resolve("eulerian.txt");

        GraphGenerator.GraphConfig config = GraphGenerator.builder()
                .vertices(100)
                .edges(100)
                .directed(false)
                .connectivity(GraphGenerator.ConnectivityType.EULERIAN)
                .outputPath(outputPath.toString())
                .build();

        GraphGenerator.generateGraph(config);

        List<int[]> edges = parseGraphFile(outputPath);
        int[] degree = calculateDegrees(edges, 100, false);

        int oddDegreeCount = 0;
        for (int i = 1; i <= 100; i++) {
            if (degree[i] % 2 == 1) {
                oddDegreeCount++;
            }
        }

        assertTrue(oddDegreeCount < 20, "Most vertices should have even degree");
    }

    @Test
    void testSemiEulerianDegreeParity() throws IOException {
        Path outputPath = tempDir.resolve("semi_eulerian.txt");

        GraphGenerator.GraphConfig config = GraphGenerator.builder()
                .vertices(100)
                .edges(100)
                .directed(false)
                .connectivity(GraphGenerator.ConnectivityType.SEMI_EULERIAN)
                .outputPath(outputPath.toString())
                .build();

        GraphGenerator.generateGraph(config);

        List<int[]> edges = parseGraphFile(outputPath);
        int[] degree = calculateDegrees(edges, 100, false);

        int oddDegreeCount = 0;
        for (int i = 1; i <= 100; i++) {
            if (degree[i] % 2 == 1) {
                oddDegreeCount++;
            }
        }

        assertTrue(oddDegreeCount < 30, "Should have limited odd-degree vertices");
    }

    @Test
    void testEulerianConnected() throws IOException {
        Path outputPath = tempDir.resolve("eulerian_connected.txt");

        GraphGenerator.GraphConfig config = GraphGenerator.builder()
                .vertices(100)
                .edges(100)
                .directed(false)
                .connectivity(GraphGenerator.ConnectivityType.EULERIAN)
                .outputPath(outputPath.toString())
                .build();

        GraphGenerator.generateGraph(config);

        List<int[]> edges = parseGraphFile(outputPath);
        Set<Integer>[] adj = buildAdjacencyList(100, edges, false);

        Set<Integer> visited = new HashSet<>();
        Queue<Integer> queue = new ArrayDeque<>();
        queue.add(1);
        visited.add(1);

        while (!queue.isEmpty()) {
            int v = queue.poll();
            for (int neighbor : adj[v]) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }

        assertEquals(100, visited.size(), "Eulerian graph should be connected");
    }

    @Test
    void testSemiEulerianConnected() throws IOException {
        Path outputPath = tempDir.resolve("semi_eulerian_connected.txt");

        GraphGenerator.GraphConfig config = GraphGenerator.builder()
                .vertices(100)
                .edges(100)
                .directed(false)
                .connectivity(GraphGenerator.ConnectivityType.SEMI_EULERIAN)
                .outputPath(outputPath.toString())
                .build();

        GraphGenerator.generateGraph(config);

        List<int[]> edges = parseGraphFile(outputPath);
        Set<Integer>[] adj = buildAdjacencyList(100, edges, false);

        Set<Integer> visited = new HashSet<>();
        Queue<Integer> queue = new ArrayDeque<>();
        queue.add(1);
        visited.add(1);

        while (!queue.isEmpty()) {
            int v = queue.poll();
            for (int neighbor : adj[v]) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }

        assertEquals(100, visited.size(), "Semi-Eulerian graph should be connected");
    }

    @Test
    void testSeedReproducibility() throws IOException {
        Path outputPath1 = tempDir.resolve("seed_test_1.txt");
        Path outputPath2 = tempDir.resolve("seed_test_2.txt");

        GraphGenerator.GraphConfig config1 = GraphGenerator.builder()
                .vertices(100)
                .edges(50)
                .directed(true)
                .connectivity(GraphGenerator.ConnectivityType.WEAKLY)
                .outputPath(outputPath1.toString())
                .seed(42L)
                .build();

        GraphGenerator.GraphConfig config2 = GraphGenerator.builder()
                .vertices(100)
                .edges(50)
                .directed(true)
                .connectivity(GraphGenerator.ConnectivityType.WEAKLY)
                .outputPath(outputPath2.toString())
                .seed(42L)
                .build();

        GraphGenerator.generateGraph(config1);
        GraphGenerator.generateGraph(config2);

        List<int[]> edges1 = parseGraphFile(outputPath1);
        List<int[]> edges2 = parseGraphFile(outputPath2);

        assertEquals(edges1.size(), edges2.size(), "Same seed should produce same edge count");
        for (int i = 0; i < edges1.size(); i++) {
            assertArrayEquals(edges1.get(i), edges2.get(i),
                    "Edge at index " + i + " should be identical with same seed");
        }
    }

    @Test
    void testDifferentSeedsProduceDifferentGraphs() throws IOException {
        Path outputPath1 = tempDir.resolve("diff_seed_1.txt");
        Path outputPath2 = tempDir.resolve("diff_seed_2.txt");

        GraphGenerator.GraphConfig config1 = GraphGenerator.builder()
                .vertices(100)
                .edges(50)
                .directed(true)
                .connectivity(GraphGenerator.ConnectivityType.WEAKLY)
                .outputPath(outputPath1.toString())
                .seed(42L)
                .build();

        GraphGenerator.GraphConfig config2 = GraphGenerator.builder()
                .vertices(100)
                .edges(50)
                .directed(true)
                .connectivity(GraphGenerator.ConnectivityType.WEAKLY)
                .outputPath(outputPath2.toString())
                .seed(123L)
                .build();

        GraphGenerator.generateGraph(config1);
        GraphGenerator.generateGraph(config2);

        List<int[]> edges1 = parseGraphFile(outputPath1);
        List<int[]> edges2 = parseGraphFile(outputPath2);

        boolean different = false;
        for (int i = 0; i < edges1.size(); i++) {
            if (edges1.get(i)[0] != edges2.get(i)[0] || edges1.get(i)[1] != edges2.get(i)[1]) {
                different = true;
                break;
            }
        }
        assertTrue(different, "Different seeds should produce different graphs");
    }

    @Test
    void testSeedWithUndirectedGraph() throws IOException {
        Path outputPath1 = tempDir.resolve("undirected_seed_1.txt");
        Path outputPath2 = tempDir.resolve("undirected_seed_2.txt");

        GraphGenerator.GraphConfig config1 = GraphGenerator.builder()
                .vertices(50)
                .edges(25)
                .directed(false)
                .connectivity(GraphGenerator.ConnectivityType.WEAKLY)
                .outputPath(outputPath1.toString())
                .seed(999L)
                .build();

        GraphGenerator.GraphConfig config2 = GraphGenerator.builder()
                .vertices(50)
                .edges(25)
                .directed(false)
                .connectivity(GraphGenerator.ConnectivityType.WEAKLY)
                .outputPath(outputPath2.toString())
                .seed(999L)
                .build();

        GraphGenerator.generateGraph(config1);
        GraphGenerator.generateGraph(config2);

        List<int[]> edges1 = parseGraphFile(outputPath1);
        List<int[]> edges2 = parseGraphFile(outputPath2);

        assertEquals(edges1.size(), edges2.size());
        for (int i = 0; i < edges1.size(); i++) {
            assertArrayEquals(edges1.get(i), edges2.get(i),
                    "Undirected edge at index " + i + " should be identical with same seed");
        }
    }

    @SuppressWarnings("unchecked")
    private Set<Integer>[] buildAdjacencyList(int n, List<int[]> edges, boolean directed) {
        Set<Integer>[] adj = new HashSet[n + 1];
        for (int i = 1; i <= n; i++) {
            adj[i] = new HashSet<>();
        }

        for (int[] edge : edges) {
            int v = edge[0];
            int w = edge[1];
            adj[v].add(w);
            if (!directed) {
                adj[w].add(v);
            }
        }

        return adj;
    }

    private int[] calculateDegrees(List<int[]> edges, int n, boolean directed) {
        int[] degree = new int[n + 1];
        for (int[] edge : edges) {
            degree[edge[0]]++;
            if (!directed) {
                degree[edge[1]]++;
            }
        }
        return degree;
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

    private void verifyCorrectEdgeCount(List<int[]> edges, int expectedEdges) {
        int actualCount = edges.size();
        assertEquals(expectedEdges, actualCount,
                "Graph should have exactly " + expectedEdges + " edges");
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
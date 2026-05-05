package graph.cli.create;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.ArrayDeque;

import graph.algorithms.Fleury;
import graph.algorithms.Fleury.EulerianType;
import graph.api.UndirectedGraph;
import graph.api.DirectedGraph;
import graph.api.ConnectivityType;
import graph.cli.CliParser;
import graph.util.GraphTestHelper;
import graph.representations.forwardstar.ForwardStarGraphBuilder;

class GraphGeneratorTest {

    @TempDir
    Path tempDir;

    @Test
    void testSimpleUndirectedGraph() throws Exception {
        Path outputPath = tempDir.resolve("undirected_graph.txt");

        GraphGenerator generator = new GraphGenerator(100, false, outputPath.toString());
        generator.setConnectivity(ConnectivityType.DISCONNECTED);
        generator.setEdges(50);
        generator.create();

        List<int[]> edges = parseGraphFile(outputPath);

        assertFalse(edges.isEmpty(), "Graph should have edges");
        verifyNoSelfLoops(edges);
        verifyNoDuplicateEdges(edges, false);
        verifyValidVertexRange(edges, 100);
        assertTrue(edges.size() >= 50, "Graph should have at least 50 edges");
    }

    @Test
    void testUndirectedEdgeBidirectionality() throws Exception {
        Path outputPath = tempDir.resolve("bidirectional_graph.txt");

        GraphGenerator generator = new GraphGenerator(100, false, outputPath.toString());
        generator.setConnectivity(ConnectivityType.DISCONNECTED);
        generator.setEdges(50);
        generator.create();

        List<int[]> edges = parseGraphFile(outputPath);

        verifyBidirectionalEdges(edges, 50);
    }

    @Test
    void testUndirectedGraphHeaderMatchesUniqueEdges() throws Exception {
        Path outputPath = tempDir.resolve("undirected_header_test.txt");

        GraphGenerator generator = new GraphGenerator(500, false, outputPath.toString());
        generator.setConnectivity(ConnectivityType.EULERIAN);
        generator.setEdges(500);
        generator.create();

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
    void testUndirectedNoSelfLoops() throws Exception {
        Path outputPath = tempDir.resolve("undirected_selfloop_test.txt");

        GraphGenerator generator = new GraphGenerator(500, false, outputPath.toString());
        generator.setConnectivity(ConnectivityType.DISCONNECTED);
        generator.setDensity(0.9);
        generator.create();

        List<int[]> edges = parseGraphFile(outputPath);
        verifyNoSelfLoops(edges);
    }

    @Test
    void testUndirectedDenseNoDuplicates() throws Exception {
        Path outputPath = tempDir.resolve("undirected_dense_dup_test.txt");

        GraphGenerator generator = new GraphGenerator(500, false, outputPath.toString());
        generator.setConnectivity(ConnectivityType.CONNECTED);
        generator.setDensity(0.8);
        generator.create();

        List<int[]> edges = parseGraphFile(outputPath);
        verifyNoDuplicateEdges(edges, false);
    }

    @Test
    void testLargeScaleUndirected() throws Exception {
        Path outputPath = tempDir.resolve("large_undirected.txt");

        GraphGenerator generator = new GraphGenerator(1000, false, outputPath.toString());
        generator.setConnectivity(ConnectivityType.CONNECTED);
        generator.setEdges(10000);
        generator.create();

        List<int[]> edges = parseGraphFile(outputPath);
        verifyNoSelfLoops(edges);
        verifyNoDuplicateEdges(edges, false);
        assertTrue(edges.size() >= 10000, "Should have at least 10000 edges");
    }

    @Test
    void testUndirectedEulerianMinimumEdges() throws Exception {
        Path outputPath = tempDir.resolve("eulerian_min_edges.txt");

        GraphGenerator generator = new GraphGenerator(100, false, outputPath.toString());
        generator.setConnectivity(ConnectivityType.EULERIAN);
        generator.setEdges(100);
        generator.create();

        List<int[]> edges = parseGraphFile(outputPath);

        assertTrue(edges.size() >= 100,
                "Eulerian graph with n=100 should have at least 100 edges (a cycle)");
    }

    @Test
    void testUndirectedSemiEulerianMinimumEdges() throws Exception {
        Path outputPath = tempDir.resolve("semi_eulerian_min_edges.txt");

        GraphGenerator generator = new GraphGenerator(100, false, outputPath.toString());
        generator.setConnectivity(ConnectivityType.SEMI_EULERIAN);
        generator.setEdges(99);
        generator.create();

        List<int[]> edges = parseGraphFile(outputPath);

        assertTrue(edges.size() >= 99,
                "Semi-Eulerian graph with n=100 should have at least 99 edges (a path)");
    }

    @Test
    void testEulerianDegreeParity() throws Exception {
        Path outputPath = tempDir.resolve("eulerian.txt");

        GraphGenerator generator = new GraphGenerator(100, false, outputPath.toString());
        generator.setConnectivity(ConnectivityType.EULERIAN);
        generator.setEdges(100);
        generator.create();

        List<int[]> edges = parseGraphFile(outputPath);
        int[] degree = calculateDegrees(edges, 100, false);

        int oddDegreeCount = 0;
        for (int i = 1; i <= 100; i++) {
            if (degree[i] % 2 == 1) {
                oddDegreeCount++;
            }
        }

        assertTrue(oddDegreeCount <= 2, "Eulerian graph should have 0 or 2 odd-degree vertices");
    }

    @Test
    void testSemiEulerianDegreeParity() throws Exception {
        Path outputPath = tempDir.resolve("semi_eulerian.txt");

        GraphGenerator generator = new GraphGenerator(100, false, outputPath.toString());
        generator.setConnectivity(ConnectivityType.SEMI_EULERIAN);
        generator.setEdges(100);
        generator.create();

        List<int[]> edges = parseGraphFile(outputPath);
        int[] degree = calculateDegrees(edges, 100, false);

        int oddDegreeCount = 0;
        for (int i = 1; i <= 100; i++) {
            if (degree[i] % 2 == 1) {
                oddDegreeCount++;
            }
        }

        assertTrue(oddDegreeCount <= 2, "Semi-Eulerian graph should have 0 or 2 odd-degree vertices");
    }

    @Test
    void testEulerianConnected() throws Exception {
        Path outputPath = tempDir.resolve("eulerian_connected.txt");

        GraphGenerator generator = new GraphGenerator(100, false, outputPath.toString());
        generator.setConnectivity(ConnectivityType.EULERIAN);
        generator.setEdges(100);
        generator.create();

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
    void testSemiEulerianConnected() throws Exception {
        Path outputPath = tempDir.resolve("semi_eulerian_connected.txt");

        GraphGenerator generator = new GraphGenerator(100, false, outputPath.toString());
        generator.setConnectivity(ConnectivityType.SEMI_EULERIAN);
        generator.setEdges(100);
        generator.create();

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
    void testSeedWithUndirectedGraph() throws Exception {
        Path outputPath1 = tempDir.resolve("undirected_seed_1.txt");
        Path outputPath2 = tempDir.resolve("undirected_seed_2.txt");

        GraphGenerator generator1 = new GraphGenerator(50, false, outputPath1.toString());
        generator1.setConnectivity(ConnectivityType.EULERIAN);
        generator1.setEdges(50);
        generator1.setSeed(999L);
        generator1.create();

        GraphGenerator generator2 = new GraphGenerator(50, false, outputPath2.toString());
        generator2.setConnectivity(ConnectivityType.EULERIAN);
        generator2.setEdges(50);
        generator2.setSeed(999L);
        generator2.create();

        List<int[]> edges1 = parseGraphFile(outputPath1);
        List<int[]> edges2 = parseGraphFile(outputPath2);

        assertEquals(edges1.size(), edges2.size());
        for (int i = 0; i < edges1.size(); i++) {
            assertArrayEquals(edges1.get(i), edges2.get(i),
                    "Undirected edge at index " + i + " should be identical with same seed");
        }
    }

    @Test
    void testEulerianGraphWithFleury() throws Exception {
        Path outputPath = tempDir.resolve("eulerian_fleury_test.txt");

        GraphGenerator generator = new GraphGenerator(100, false, outputPath.toString());
        generator.setConnectivity(ConnectivityType.EULERIAN);
        generator.setEdges(100);
        generator.create();

        List<int[]> edges = parseGraphFile(outputPath);
        UndirectedGraph graph = parseToUndirectedGraph(edges, 100);

        Fleury.EulerianPath result = Fleury.findEulerianPath(graph);

        assertTrue(result.type() == EulerianType.EULERIAN || result.type() == EulerianType.SEMI_EULERIAN,
                "EULERIAN connectivity graph should have an Eulerian path");
        assertTrue(result.path().length > 0, "Eulerian path should not be empty");
    }

    @Test
    void testSemiEulerianGraphWithFleury() throws Exception {
        Path outputPath = tempDir.resolve("semi_eulerian_fleury_test.txt");

        GraphGenerator generator = new GraphGenerator(100, false, outputPath.toString());
        generator.setConnectivity(ConnectivityType.SEMI_EULERIAN);
        generator.setEdges(100);
        generator.create();

        List<int[]> edges = parseGraphFile(outputPath);
        UndirectedGraph graph = parseToUndirectedGraph(edges, 100);

        Fleury.EulerianPath result = Fleury.findEulerianPath(graph);

        assertEquals(EulerianType.SEMI_EULERIAN, result.type(),
                "SEMI_EULERIAN connectivity graph should be detected as SEMI_EULERIAN by Fleury");
        assertTrue(result.path().length > 0, "Semi-Eulerian path should not be empty");
    }

    @Test
    void testEulerianPathUsesAllEdges() throws Exception {
        Path outputPath = tempDir.resolve("eulerian_path_edges.txt");

        GraphGenerator generator = new GraphGenerator(50, false, outputPath.toString());
        generator.setConnectivity(ConnectivityType.EULERIAN);
        generator.setEdges(50);
        generator.setSeed(12345L);
        generator.create();

        List<int[]> edges = parseGraphFile(outputPath);

        UndirectedGraph graph = parseToUndirectedGraph(edges, 50);

        Fleury.EulerianPath result = Fleury.findEulerianPath(graph);

        assertTrue(result.type() == EulerianType.EULERIAN || result.type() == EulerianType.SEMI_EULERIAN,
                "Graph should have Eulerian path");

        int m = edges.size();
        assertEquals(m + 1, result.path().length,
                "Eulerian path should have exactly m+1 vertices");
    }

    @Test
    void testEulerianPathEdgesAreValid() throws Exception {
        Path outputPath = tempDir.resolve("eulerian_path_valid.txt");

        GraphGenerator generator = new GraphGenerator(50, false, outputPath.toString());
        generator.setConnectivity(ConnectivityType.EULERIAN);
        generator.setEdges(50);
        generator.setSeed(12345L);
        generator.create();

        List<int[]> edges = parseGraphFile(outputPath);
        UndirectedGraph graph = parseToUndirectedGraph(edges, 50);

        Fleury.EulerianPath result = Fleury.findEulerianPath(graph);

        assertTrue(result.type() == EulerianType.EULERIAN || result.type() == EulerianType.SEMI_EULERIAN,
                "Graph should have Eulerian path");

        Set<String> edgeSet = new HashSet<>();
        for (int[] edge : edges) {
            int min = Math.min(edge[0], edge[1]);
            int max = Math.max(edge[0], edge[1]);
            edgeSet.add(min + "-" + max);
        }

        int[] path = result.path();
        for (int i = 0; i < path.length - 1; i++) {
            int a = path[i];
            int b = path[i + 1];
            String edgeKey = Math.min(a, b) + "-" + Math.max(a, b);
            assertTrue(edgeSet.contains(edgeKey),
                    "Edge between " + a + " and " + b + " at position " + i + " must exist in graph");
        }
    }

    @Test
    void testEulerianPathNoDuplicateEdges() throws Exception {
        Path outputPath = tempDir.resolve("eulerian_path_no_dup.txt");

        GraphGenerator generator = new GraphGenerator(50, false, outputPath.toString());
        generator.setConnectivity(ConnectivityType.EULERIAN);
        generator.setEdges(50);
        generator.create();

        List<int[]> edges = parseGraphFile(outputPath);
        UndirectedGraph graph = parseToUndirectedGraph(edges, 50);

        Fleury.EulerianPath result = Fleury.findEulerianPath(graph);

        assertTrue(result.type() == EulerianType.EULERIAN || result.type() == EulerianType.SEMI_EULERIAN,
                "Graph should have Eulerian path, but got: " + result.type());

        Set<String> pathEdges = new HashSet<>();
        int[] path = result.path();
        for (int i = 0; i < path.length - 1; i++) {
            int a = path[i];
            int b = path[i + 1];
            String edgeKey = Math.min(a, b) + "-" + Math.max(a, b);
            assertFalse(pathEdges.contains(edgeKey),
                    "Edge between " + a + " and " + b + " at position " + i + " should not be repeated");
            pathEdges.add(edgeKey);
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

    private List<int[]> parseGraphFile(Path path) throws Exception {
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

        assertTrue(edgePairs.size() >= expectedPairs,
                "Undirected graph should have at least " + expectedPairs + " unique edge pairs");
    }

    private UndirectedGraph parseToUndirectedGraph(List<int[]> edges, int n) {
        int[][] edgesArray = edges.toArray(new int[edges.size()][]);
        return (UndirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(false),
                n,
                edgesArray);
    }

    @Test
    void testSimpleDirectedDisconnectedGraph() throws Exception {
        Path outputPath = tempDir.resolve("directed_disconnected_graph.txt");

        GraphGenerator generator = new GraphGenerator(100, true, outputPath.toString());
        generator.setConnectivity(ConnectivityType.DISCONNECTED);
        generator.setEdges(50);
        generator.create();

        List<int[]> edges = parseGraphFile(outputPath);

        assertFalse(edges.isEmpty(), "Graph should have edges");
        verifyNoSelfLoopsDirected(edges);
        verifyNoDuplicateEdges(edges, true);
        verifyValidVertexRange(edges, 100);
        assertTrue(edges.size() >= 50, "Graph should have at least 50 edges");
    }

    @Test
    void testDirectedNoSelfLoops() throws Exception {
        Path outputPath = tempDir.resolve("directed_selfloop_test.txt");

        GraphGenerator generator = new GraphGenerator(500, true, outputPath.toString());
        generator.setConnectivity(ConnectivityType.DISCONNECTED);
        generator.setDensity(0.9);
        generator.create();

        List<int[]> edges = parseGraphFile(outputPath);
        verifyNoSelfLoopsDirected(edges);
    }

    @Test
    void testDirectedWeaklyConnected() throws Exception {
        Path outputPath = tempDir.resolve("directed_weakly_connected.txt");

        GraphGenerator generator = new GraphGenerator(100, true, outputPath.toString());
        generator.setConnectivity(ConnectivityType.CONNECTED);
        generator.setEdges(200);
        generator.create();

        List<int[]> edges = parseGraphFile(outputPath);

        // Verify weak connectivity (treat as undirected)
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

        assertEquals(100, visited.size(),
                "Directed graph should be weakly connected (underlying undirected is connected)");
    }

    @Test
    void testDirectedStronglyConnected() throws Exception {
        Path outputPath = tempDir.resolve("directed_strongly_connected.txt");

        GraphGenerator generator = new GraphGenerator(50, true, outputPath.toString());
        generator.setConnectivity(ConnectivityType.STRONGLY_CONNECTED);
        generator.setEdges(100);
        generator.create();

        List<int[]> edges = parseGraphFile(outputPath);

        // Build directed adjacency list
        Set<Integer>[] adj = buildAdjacencyList(50, edges, true);

        // Verify strong connectivity: every vertex can reach every other
        for (int start = 1; start <= 50; start++) {
            Set<Integer> visited = new HashSet<>();
            Queue<Integer> queue = new ArrayDeque<>();
            queue.add(start);
            visited.add(start);

            while (!queue.isEmpty()) {
                int v = queue.poll();
                for (int neighbor : adj[v]) {
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }

            assertEquals(50, visited.size(), "Vertex " + start + " should be able to reach all 50 vertices");
        }
    }

    @Test
    void testDirectedEulerianDegrees() throws Exception {
        Path outputPath = tempDir.resolve("directed_eulerian.txt");

        GraphGenerator generator = new GraphGenerator(100, true, outputPath.toString());
        generator.setConnectivity(ConnectivityType.EULERIAN);
        generator.setEdges(200);
        generator.create();

        List<int[]> edges = parseGraphFile(outputPath);

        // Calculate in-degree and out-degree for each vertex
        int n = 100;
        int[] inDegree = new int[n + 1];
        int[] outDegree = new int[n + 1];

        for (int[] edge : edges) {
            int v = edge[0];
            int w = edge[1];
            outDegree[v]++;
            inDegree[w]++;
        }

        // All vertices should have in-degree == out-degree for Eulerian circuit
        for (int i = 1; i <= n; i++) {
            assertEquals(inDegree[i], outDegree[i],
                    "Vertex " + i + " should have in-degree == out-degree for Eulerian graph");
        }
    }

    @Test
    void testDirectedEulerianWithFleury() throws Exception {
        Path outputPath = tempDir.resolve("directed_eulerian_fleury.txt");

        GraphGenerator generator = new GraphGenerator(50, true, outputPath.toString());
        generator.setConnectivity(ConnectivityType.EULERIAN);
        generator.setEdges(100);
        generator.create();

        List<int[]> edges = parseGraphFile(outputPath);
        DirectedGraph graph = parseToDirectedGraph(edges, 50);

        Fleury.EulerianPath result = Fleury.findEulerianPath(graph);

        assertEquals(Fleury.EulerianType.EULERIAN, result.type(),
                "Directed Eulerian graph should have an Eulerian circuit");
        assertTrue(result.path().length > 0, "Eulerian path should not be empty");
    }

    @Test
    void testDirectedSemiEulerianDegrees() throws Exception {
        Path outputPath = tempDir.resolve("directed_semi_eulerian.txt");

        GraphGenerator generator = new GraphGenerator(100, true, outputPath.toString());
        generator.setConnectivity(ConnectivityType.SEMI_EULERIAN);
        generator.setEdges(200);
        generator.create();

        List<int[]> edges = parseGraphFile(outputPath);

        // Calculate in-degree and out-degree for each vertex
        int n = 100;
        int[] inDegree = new int[n + 1];
        int[] outDegree = new int[n + 1];

        for (int[] edge : edges) {
            int v = edge[0];
            int w = edge[1];
            outDegree[v]++;
            inDegree[w]++;
        }

        // Count vertices where in-degree != out-degree
        int imbalancedCount = 0;
        for (int i = 1; i <= n; i++) {
            if (inDegree[i] != outDegree[i]) {
                imbalancedCount++;
            }
        }

        assertEquals(2, imbalancedCount,
                "Semi-Eulerian directed graph should have exactly 2 imbalanced vertices");
    }

    @Test
    void testDirectedSemiEulerianWithFleury() throws Exception {
        Path outputPath = tempDir.resolve("directed_semi_eulerian_fleury.txt");

        GraphGenerator generator = new GraphGenerator(50, true, outputPath.toString());
        generator.setConnectivity(ConnectivityType.SEMI_EULERIAN);
        generator.setEdges(100);
        generator.create();

        List<int[]> edges = parseGraphFile(outputPath);

        DirectedGraph graph = parseToDirectedGraph(edges, 50);
        Fleury.EulerianPath result = Fleury.findEulerianPath(graph);

        assertEquals(Fleury.EulerianType.SEMI_EULERIAN, result.type(),
                "Directed Semi-Eulerian graph should have an Eulerian path (not circuit)");
        assertTrue(result.path().length > 0, "Eulerian path should not be empty");
    }

    @Test
    void testStronglyWithUndirectedThrows() {
        String[] args = { "create", "test.txt", "-n", "100", "-m", "200", "--undirected", "--strongly" };
        assertThrows(Exception.class, () -> CliParser.parse(args),
                "Should throw exception when --strongly used with --undirected");
    }

    @Test
    void testConnectedAndStronglyThrows() {
        String[] args = { "create", "test.txt", "-n", "100", "-m", "200", "--directed", "--connected", "--strongly" };
        assertThrows(Exception.class, () -> CliParser.parse(args),
                "Should throw exception when --connected and --strongly both specified");
    }

    private DirectedGraph parseToDirectedGraph(List<int[]> edges, int n) {
        int[][] edgesArray = edges.toArray(new int[edges.size()][]);
        return (DirectedGraph) GraphTestHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                n,
                edgesArray);
    }

    private void verifyNoSelfLoopsDirected(List<int[]> edges) {
        for (int[] edge : edges) {
            assertNotEquals(edge[0], edge[1], "Directed graph should not contain self-loops");
        }
    }
}
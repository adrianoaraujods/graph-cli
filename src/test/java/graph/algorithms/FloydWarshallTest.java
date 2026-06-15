package graph.algorithms;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import graph.api.Graph;
import graph.api.WeightedGraph;
import graph.cli.CliCommand;
import graph.cli.CliParser;
import graph.cli.ReadCommand;
import graph.cli.read.AlgorithmRequest;
import graph.cli.read.AlgorithmRunner;
import graph.cli.read.result.AlgorithmResult;
import graph.cli.read.result.AllPairsShortestPathResult;
import graph.util.WeightedTestHelper;
import graph.representations.forwardstar.ForwardStarGraphBuilder;

class FloydWarshallTest {

    @Test
    void testNegativeCycleRejection() {
        WeightedGraph graph = (WeightedGraph) WeightedTestHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                new int[][] { { 1, 2, 1 }, { 2, 3, -3 }, { 3, 1, 1 } });

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            FloydWarshall.compute(graph);
        });

        assertTrue(exception.getMessage().contains("negative cycle"));
    }

    @Test
    void testNegativeWeightsWithoutCycle() {
        WeightedGraph graph = (WeightedGraph) WeightedTestHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                new int[][] { { 1, 2, 4 }, { 2, 3, -2 }, { 1, 3, 3 } });

        AllPairsShortestPathResult result = FloydWarshall.compute(graph);
        int[][] dist = result.distances();

        assertEquals(0, dist[0][0]);
        assertEquals(4, dist[0][1]);
        assertEquals(2, dist[0][2]);
        assertEquals(Integer.MAX_VALUE, dist[1][0]);
        assertEquals(0, dist[1][1]);
        assertEquals(-2, dist[1][2]);
        assertEquals(Integer.MAX_VALUE, dist[2][0]);
        assertEquals(Integer.MAX_VALUE, dist[2][1]);
        assertEquals(0, dist[2][2]);
    }

    @Test
    void testUnreachableVertices() {
        WeightedGraph graph = (WeightedGraph) WeightedTestHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                new int[][] { { 1, 2, 5 }, { 3, 4, 2 } });

        AllPairsShortestPathResult result = FloydWarshall.compute(graph);
        int[][] dist = result.distances();

        assertEquals(0, dist[0][0]);
        assertEquals(5, dist[0][1]);
        assertEquals(Integer.MAX_VALUE, dist[0][2]);
        assertEquals(Integer.MAX_VALUE, dist[0][3]);
        assertEquals(Integer.MAX_VALUE, dist[1][0]);
        assertEquals(0, dist[1][1]);
        assertEquals(Integer.MAX_VALUE, dist[1][2]);
        assertEquals(Integer.MAX_VALUE, dist[1][3]);
        assertEquals(Integer.MAX_VALUE, dist[2][0]);
        assertEquals(Integer.MAX_VALUE, dist[2][1]);
        assertEquals(0, dist[2][2]);
        assertEquals(2, dist[2][3]);
        assertEquals(Integer.MAX_VALUE, dist[3][0]);
        assertEquals(Integer.MAX_VALUE, dist[3][1]);
        assertEquals(Integer.MAX_VALUE, dist[3][2]);
        assertEquals(0, dist[3][3]);
    }

    @Test
    void testUndirectedGraph() {
        WeightedGraph graph = (WeightedGraph) WeightedTestHelper.build(
                () -> new ForwardStarGraphBuilder(false),
                new int[][] { { 1, 2, 4 }, { 2, 3, 5 }, { 1, 3, 10 } });

        AllPairsShortestPathResult result = FloydWarshall.compute(graph);
        int[][] dist = result.distances();

        assertEquals(0, dist[0][0]);
        assertEquals(4, dist[0][1]);
        assertEquals(9, dist[0][2]);
        assertEquals(4, dist[1][0]);
        assertEquals(0, dist[1][1]);
        assertEquals(5, dist[1][2]);
        assertEquals(9, dist[2][0]);
        assertEquals(5, dist[2][1]);
        assertEquals(0, dist[2][2]);
    }

    @Test
    void testAlgorithmRequestFactory() {
        AlgorithmRequest request = AlgorithmRequest.floydWarshall();

        assertEquals("--floyd-warshall", request.name());
    }

    @Test
    void testAlgorithmRunnerHandlesFloydWarshall() {
        WeightedGraph graph = (WeightedGraph) WeightedTestHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                new int[][] { { 1, 2, 5 }, { 2, 3, 3 } });

        AlgorithmRequest request = AlgorithmRequest.floydWarshall();
        AlgorithmResult result = AlgorithmRunner.runSingle((Graph) graph, request, false, false, false);

        assertTrue(result instanceof AllPairsShortestPathResult);
        AllPairsShortestPathResult fwResult = (AllPairsShortestPathResult) result;
        assertEquals(8, fwResult.distances()[0][2]);
    }

    @Test
    void testCliParserRecognizesFlag() throws Exception {
        String[] args = { "read", "graph.txt", "--weighted", "--floyd-warshall" };
        CliCommand command = CliParser.parse(args);

        assertTrue(command instanceof ReadCommand);
        ReadCommand readCmd = (ReadCommand) command;
        assertTrue(readCmd.config().isWeighted());
        assertEquals(1, readCmd.config().algorithms().size());
        assertEquals("--floyd-warshall", readCmd.config().algorithms().get(0).name());
    }

    @Test
    void testCliParserRequiresWeighted() {
        String[] args = { "read", "graph.txt", "--floyd-warshall" };

        Exception exception = assertThrows(Exception.class, () -> CliParser.parse(args));
        assertTrue(exception.getMessage().contains("weighted"),
                "Error should mention weighted is required");
    }

    @Test
    void testAllPairsShortestPathResultOutput() {
        int[][] distances = { { 0, 5, 8 }, { Integer.MAX_VALUE, 0, 3 }, { Integer.MAX_VALUE, Integer.MAX_VALUE, 0 } };
        int[][] next = { { -1, 1, 1 }, { -1, -1, 2 }, { -1, -1, -1 } };
        AllPairsShortestPathResult result = new AllPairsShortestPathResult(distances, next, 3);

        String output = result.toString();
        assertTrue(output.contains("Floyd-Warshall"),
                "Output should mention Floyd-Warshall");
        assertTrue(output.contains("0, 5, 8"),
                "Output should show first row of distances");
        assertTrue(output.contains("INF"),
                "Output should show INF for unreachable");
    }

    @Test
    void testSimpleDirectedChain() {
        WeightedGraph graph = (WeightedGraph) WeightedTestHelper.build(
                () -> new ForwardStarGraphBuilder(true),
                new int[][] { { 1, 2, 5 }, { 2, 3, 3 } });

        AllPairsShortestPathResult result = FloydWarshall.compute(graph);
        int[][] dist = result.distances();

        assertEquals(3, result.verticesCount());
        assertEquals(0, dist[0][0]);
        assertEquals(5, dist[0][1]);
        assertEquals(8, dist[0][2]);
        assertEquals(Integer.MAX_VALUE, dist[1][0]);
        assertEquals(0, dist[1][1]);
        assertEquals(3, dist[1][2]);
        assertEquals(Integer.MAX_VALUE, dist[2][0]);
        assertEquals(Integer.MAX_VALUE, dist[2][1]);
        assertEquals(0, dist[2][2]);
    }
}

package graph.algorithms;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import graph.api.Graph;
import graph.api.WeightedGraph;
import graph.cli.CliCommand;
import graph.cli.CliParser;
import graph.cli.ReadCommand;
import graph.util.Usage;
import graph.cli.read.AlgorithmRequest;
import graph.cli.read.AlgorithmRunner;
import graph.cli.read.result.AlgorithmResult;
import graph.cli.read.result.ShortestPathResult;
import graph.util.WeightedTestHelper;
import graph.representations.forwardstar.ForwardStarGraphBuilder;

class DijkstraTest {

        @Test
        void testSimpleChain() {
                WeightedGraph graph = (WeightedGraph) WeightedTestHelper.build(
                                () -> new ForwardStarGraphBuilder(true),
                                new int[][] { { 1, 2, 5 }, { 2, 3, 3 } });

                ShortestPathResult result = Dijkstra.compute(graph, 1, null, false);

                assertEquals(0, result.distances()[0]);
                assertEquals(5, result.distances()[1]);
                assertEquals(8, result.distances()[2]);
        }

        @Test
        void testMultipleRoutesChooseShortest() {
                WeightedGraph graph = (WeightedGraph) WeightedTestHelper.build(
                                () -> new ForwardStarGraphBuilder(true),
                                new int[][] { { 1, 2, 10 }, { 1, 3, 2 }, { 3, 2, 3 } });

                ShortestPathResult result = Dijkstra.compute(graph, 1, null, false);

                assertEquals(5, result.distances()[1]);
                assertEquals(2, result.distances()[2]);
        }

        @Test
        void testUnreachableVertices() {
                WeightedGraph graph = (WeightedGraph) WeightedTestHelper.build(
                                () -> new ForwardStarGraphBuilder(true),
                                new int[][] { { 1, 2, 5 }, { 3, 4, 2 } });

                ShortestPathResult result = Dijkstra.compute(graph, 1, null, false);

                assertEquals(0, result.distances()[0]);
                assertEquals(5, result.distances()[1]);
                assertEquals(Integer.MAX_VALUE, result.distances()[2]);
                assertEquals(Integer.MAX_VALUE, result.distances()[3]);
        }

        @Test
        void testNegativeWeightRejection() {
                WeightedGraph graph = (WeightedGraph) WeightedTestHelper.build(
                                () -> new ForwardStarGraphBuilder(true),
                                new int[][] { { 1, 2, 5 }, { 2, 3, -3 } });

                Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                        Dijkstra.compute(graph, 1, null, false);
                });

                assertTrue(exception.getMessage().contains("negative weight"));
        }

        @Test
        void testUndirectedGraph() {
                WeightedGraph graph = (WeightedGraph) WeightedTestHelper.build(
                                () -> new ForwardStarGraphBuilder(false),
                                new int[][] { { 1, 2, 4 }, { 2, 3, 5 }, { 1, 3, 10 } });

                ShortestPathResult result = Dijkstra.compute(graph, 1, null, false);

                assertEquals(0, result.distances()[0]);
                assertEquals(4, result.distances()[1]);
                assertEquals(9, result.distances()[2]);
        }

        @Test
        void testPathReconstruction() {
                WeightedGraph graph = (WeightedGraph) WeightedTestHelper.build(
                                () -> new ForwardStarGraphBuilder(true),
                                new int[][] { { 1, 2, 5 }, { 2, 3, 3 }, { 1, 3, 10 } });

                ShortestPathResult result = Dijkstra.compute(graph, 1, 3, true);

                assertEquals(0, result.parents()[1]);
                assertEquals(1, result.parents()[2]);
        }

        @Test
        void testAlgorithmRequestFactory() {
                AlgorithmRequest request = AlgorithmRequest.dijkstra(1, 5, true);

                assertEquals("--dijkstra", request.name());
                assertEquals(1, request.params().get("source"));
                assertEquals(5, request.params().get("target"));
                assertEquals(true, request.params().get("findPath"));
        }

        @Test
        void testAlgorithmRunnerHandlesDijkstra() {
                WeightedGraph graph = (WeightedGraph) WeightedTestHelper.build(
                                () -> new ForwardStarGraphBuilder(true),
                                new int[][] { { 1, 2, 5 }, { 2, 3, 3 } });

                AlgorithmRequest request = AlgorithmRequest.dijkstra(1, 3, false);
                AlgorithmResult result = AlgorithmRunner.runSingle((Graph) graph, request, false, false, false);

                assertTrue(result instanceof ShortestPathResult);
                ShortestPathResult spResult = (ShortestPathResult) result;
                assertEquals(8, spResult.distances()[2]);
                assertFalse(spResult.showPath(), "showPath should be false");
        }

        @Test
        void testCliParserRecognizesDijkstraFlag() throws Exception {
                String[] args = { "read", "graph.txt", "--weighted", "--dijkstra", "--source", "1", "--target", "5" };
                CliCommand command = CliParser.parse(args);

                assertTrue(command instanceof ReadCommand);
                ReadCommand readCmd = (ReadCommand) command;
                assertTrue(readCmd.config().isWeighted());
                assertEquals(1, readCmd.config().algorithms().size());
                assertEquals("--dijkstra", readCmd.config().algorithms().get(0).name());
        }

    @Test
    void testCliParserDoesNotRequireTargetWithoutPath() throws Exception {
        // --target is NOT required when --path is NOT used
        String[] args = { "read", "graph.txt", "--weighted", "--dijkstra", "--source", "1" };

        // Should NOT throw
        CliCommand command = CliParser.parse(args);
        assertTrue(command instanceof ReadCommand);
    }

    @Test
    void testCliParserShowsAllPathsWhenPathWithoutTarget() throws Exception {
        // When --path is used without --target, all paths should be shown
        String[] args = { "read", "graph.txt", "--weighted", "--dijkstra", "--source", "1", "--path" };

        // Should NOT throw - all paths will be shown
        CliCommand command = CliParser.parse(args);
        assertTrue(command instanceof ReadCommand);

        ReadCommand rc = (ReadCommand) command;
        AlgorithmRequest req = rc.config().algorithms().get(0);
        assertNull(req.params().get("target"), "Target should be null");
        assertTrue((boolean) req.params().get("findPath"), "findPath should be true");
    }

    @Test
    void testCliParserAllowsDijkstraWithoutTarget() throws Exception {
        // --dijkstra without --target should work (no --path flag)
        String[] args = { "read", "graph.txt", "--weighted", "--dijkstra", "--source", "1" };

        // Should NOT throw - target is optional when --path is not used
        CliCommand command = CliParser.parse(args);
        assertTrue(command instanceof ReadCommand);
    }

        @Test
        void testCliParserPathFlag() throws Exception {
                String[] args = { "read", "graph.txt", "--weighted", "--dijkstra",
                                "--source", "1", "--target", "5", "--path" };
                CliCommand command = CliParser.parse(args);

                ReadCommand readCmd = (ReadCommand) command;
                AlgorithmRequest req = readCmd.config().algorithms().get(0);
                assertEquals(true, req.params().get("findPath"));
        }

        @Test
        void testCliParserRequiresWeightedForDijkstra() {
                String[] args = { "read", "graph.txt", "--dijkstra", "--source", "1", "--target", "5" };

                Exception exception = assertThrows(Exception.class, () -> CliParser.parse(args));
                assertTrue(exception.getMessage().contains("weighted"),
                                "Error should mention weighted is required");
        }

        @Test
        void testUsageShowsDijkstra() {
                // Verify Usage output contains --dijkstra
                // We'll test by invoking printReadOptions and capturing output
                java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
                System.setOut(new java.io.PrintStream(out));

                Usage.printReadOptions();

                String output = out.toString();
                assertTrue(output.contains("--dijkstra"),
                                "Usage should show --dijkstra option");
                assertTrue(output.contains("--source"),
                                "Usage should show --source option");
                assertTrue(output.contains("--target"),
                                "Usage should show --target option");
                assertTrue(output.contains("--path"),
                                "Usage should show --path option");
        }

        @Test
        void testShortestPathResultOutputWithTarget() {
                int[] distances = { 0, 5, 8 };
                int[] parents = { -1, 0, 1 };
                ShortestPathResult result = new ShortestPathResult(distances, parents, 1, null, false);

                String output = result.toString();
                assertTrue(output.contains("Dijkstra Shortest Path"),
                                "Output should mention Dijkstra");
                assertTrue(output.contains("Source: 1"),
                                "Output should show source");
                assertTrue(output.contains("0, 5, 8"),
                                "Output should show distances array");
                assertFalse(output.contains("Paths from source:"),
                                "Output should NOT show path when showPath=false");
        }

        @Test
        void testShortestPathResultOutputWithPath() {
                int[] distances = { 0, 5, 8 };
                int[] parents = { -1, 0, 1 };
                ShortestPathResult result = new ShortestPathResult(distances, parents, 1, null, true);

                String output = result.toString();
                // Should be able to reconstruct path from 1 to 3: 1->2->3
                assertTrue(output.contains("Path") || output.contains("path"),
                                "Output should mention path when showPath=true");
        }

        @Test
        void testIntegrationEndToEnd() throws Exception {
                // Create a weighted graph file
                String graphFile = "target/test-weighted-graph.txt";
                try (java.io.FileWriter fw = new java.io.FileWriter(graphFile)) {
                        fw.write("3\n");
                        fw.write("4\n");
                        fw.write("1 2 5\n");
                        fw.write("2 3 3\n");
                }

                // Build command
                String[] args = { "read", graphFile, "--weighted", "--dijkstra",
                                "--source", "1", "--target", "3" };

                CliCommand command = CliParser.parse(args);
                assertTrue(command instanceof ReadCommand);

                ReadCommand rc = (ReadCommand) command;

                // Redirect output to capture it
                java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
                System.setOut(new java.io.PrintStream(out));

                // Run the read handler
                graph.cli.read.ReadHandler.run(rc.config());

                String output = out.toString();
                assertTrue(output.contains("Dijkstra") || output.contains("Shortest Path"),
                                "Output should contain Dijkstra results");
                assertFalse(output.contains("Path from source:"),
                                "Path should NOT be shown when --path not specified");
        }
}

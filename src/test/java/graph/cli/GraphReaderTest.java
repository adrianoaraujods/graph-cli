package graph.cli;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import graph.api.Graph;
import graph.representations.forwardstar.ForwardStarGraphBuilder;

class GraphReaderTest {

    @TempDir
    Path tempDir;

    @Test
    void testReadSimpleDirectedGraph() throws Exception {
        Path inputPath = tempDir.resolve("directed_input.txt");
        String content = "5 3\n1 2\n2 3\n3 5\n";
        Files.writeString(inputPath, content);

        ForwardStarGraphBuilder builder = new ForwardStarGraphBuilder(true);
        GraphReader.readFile(inputPath.toString(), builder);
        Graph graph = builder.build();

        assertEquals(5, graph.getVerticesCount());
        assertEquals(3, graph.getEdgesCount());
        assertTrue(graph.isDirected);
    }

    @Test
    void testReadSimpleUndirectedGraph() throws Exception {
        Path inputPath = tempDir.resolve("undirected_input.txt");
        String content = "4 2\n1 2\n2 3\n";
        Files.writeString(inputPath, content);

        ForwardStarGraphBuilder builder = new ForwardStarGraphBuilder(false);
        GraphReader.readFile(inputPath.toString(), builder);
        Graph graph = builder.build();

        assertEquals(4, graph.getVerticesCount());
        assertEquals(2, graph.getEdgesCount());
        assertFalse(graph.isDirected);
    }

    @Test
    void testReadEmptyFileThrows() {
        Path inputPath = tempDir.resolve("empty.txt");
        assertThrows(Exception.class, () -> {
            ForwardStarGraphBuilder builder = new ForwardStarGraphBuilder(true);
            GraphReader.readFile(inputPath.toString(), builder);
        });
    }

    @Test
    void testReadIncompleteFile() throws Exception {
        Path inputPath = tempDir.resolve("incomplete.txt");
        String content = "5 5\n1 2\n2 3\n";
        Files.writeString(inputPath, content);

        ForwardStarGraphBuilder builder = new ForwardStarGraphBuilder(true);
        GraphReader.readFile(inputPath.toString(), builder);
        Graph graph = builder.build();

        assertEquals(5, graph.getVerticesCount());
        assertEquals(2, graph.getEdgesCount());
    }

    @Test
    void testReadLargeFile() throws Exception {
        Path inputPath = tempDir.resolve("large.txt");
        StringBuilder sb = new StringBuilder();
        sb.append("1000 500\n");
        for (int i = 1; i <= 500; i++) {
            sb.append(i).append(" ").append(i + 1).append("\n");
        }
        Files.writeString(inputPath, sb.toString());

        ForwardStarGraphBuilder builder = new ForwardStarGraphBuilder(true);
        GraphReader.readFile(inputPath.toString(), builder);
        Graph graph = builder.build();

        assertEquals(1000, graph.getVerticesCount());
        assertEquals(500, graph.getEdgesCount());
    }

    @Test
    void testReadGraphWithZeroEdges() throws Exception {
        Path inputPath = tempDir.resolve("no_edges.txt");
        String content = "5 0\n";
        Files.writeString(inputPath, content);

        ForwardStarGraphBuilder builder = new ForwardStarGraphBuilder(true);
        GraphReader.readFile(inputPath.toString(), builder);
        Graph graph = builder.build();

        assertEquals(5, graph.getVerticesCount());
        assertEquals(0, graph.getEdgesCount());
    }

    @Test
    void testReadGraphWithSelfLoops() throws Exception {
        Path inputPath = tempDir.resolve("selfloops.txt");
        String content = "3 2\n1 1\n2 3\n";
        Files.writeString(inputPath, content);

        ForwardStarGraphBuilder builder = new ForwardStarGraphBuilder(true);
        GraphReader.readFile(inputPath.toString(), builder);
        Graph graph = builder.build();

        assertEquals(3, graph.getVerticesCount());
        assertEquals(2, graph.getEdgesCount());
    }

    @Test
    void testReadLargeVertexIds() throws Exception {
        Path inputPath = tempDir.resolve("large_vertices.txt");
        String content = "10000 3\n5000 8000\n3000 10000\n100 600\n";
        Files.writeString(inputPath, content);

        ForwardStarGraphBuilder builder = new ForwardStarGraphBuilder(true);
        GraphReader.readFile(inputPath.toString(), builder);
        Graph graph = builder.build();

        assertEquals(10000, graph.getVerticesCount());
        assertEquals(3, graph.getEdgesCount());
    }

    @Test
    void testReadUndirectedWeightedFileWithOppositeDirectionEdges() throws Exception {
        Path inputPath = tempDir.resolve("undirected_weighted_opposite.txt");
        String content = "2 2\n1 2 5\n2 1 10\n";
        Files.writeString(inputPath, content);

        ForwardStarGraphBuilder builder = new ForwardStarGraphBuilder(false);
        GraphReader.readFile(inputPath.toString(), builder, true, false, false);
        Graph graph = builder.build();

        assertEquals(2, graph.getVerticesCount());
        assertEquals(1, graph.getEdgesCount());

        graph.api.WeightedGraph wg = (graph.api.WeightedGraph) graph;
        assertEquals(5, wg.getEdgeWeight(1, 2));
        assertEquals(10, wg.getEdgeWeight(2, 1));
    }

    @Test
    void testReadMultipleSpacesBetweenNumbers() throws Exception {
        Path inputPath = tempDir.resolve("whitespace.txt");
        String content = "4   3\n   1   2  \n2    3\n3     4\n";
        Files.writeString(inputPath, content);

        ForwardStarGraphBuilder builder = new ForwardStarGraphBuilder(true);
        GraphReader.readFile(inputPath.toString(), builder);
        Graph graph = builder.build();

        assertEquals(4, graph.getVerticesCount());
        assertEquals(3, graph.getEdgesCount());
    }
}
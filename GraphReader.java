import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.time.Instant;
import java.util.Arrays;

/**
 * Utility class to read graph data from a text file and display structural
 * information about an specific vertex.
 * <p>
 * It uses buffered I/O channels for high-performance reading.
 */
public class GraphReader {
  /** Size of the byte buffer used for reading files (64 KB). */
  static int CHUNK_SIZE = 64 * 1024;

  /**
   * Main entry point for the GraphReader program.
   *
   * @param args Command-line arguments. Requires exactly two arguments:
   *             1. The path to the graph data file;
   *             2. The target vertex Id to analyze.
   */
  public static void main(String[] args) {
    if (args.length < 2) {
      System.err.println(
          "Missing argument.\n\t1. The path to the graph data file;\n\t2. The target vertex Id to analyze.");
      return;
    }

    String pathName = args[0];
    int target;

    try {
      target = Integer.parseInt(args[1]);
    } catch (NumberFormatException e) {
      System.err.println("The target vertex Id should be a valid integer.");
      return;
    }

    GraphBuilder builder = new ForwardStarGraphBuilder();

    try {
      long startReading = Instant.now().toEpochMilli();

      readGraph(pathName, builder);

      long finishReading = Instant.now().toEpochMilli();
      System.out.println("Finished Reading File in " + (finishReading - startReading) + " ms.");
    } catch (Exception e) {
      System.err.println(e);
      return;
    }

    long startBuilding = Instant.now().toEpochMilli();

    Graph graph = builder.build();

    long finishBuilding = Instant.now().toEpochMilli();
    System.out.println("Finished Building Graph in " + (finishBuilding - startBuilding) + " ms.");

    if (target < 1 || target > graph.m) {
      System.err.printf(
          "The target vertex Id should be between 1 and %,d. Selected vertex: '%,d'.",
          graph.m, target);
      return;
    }

    printDetails(graph, target);
  }

  /**
   * @param graph  The graph to analyze
   * @param vertex The target vertex Id to analyze.
   */
  static void printDetails(Graph graph, int target) {
    long start = Instant.now().toEpochMilli();

    int[] successors = graph.getSuccessors(target);
    int[] predecessors = graph.getPredecessors(target);

    long finish = Instant.now().toEpochMilli();
    System.out.printf("Finished Processing Graph in %d ms.\n", finish - start);

    System.out.printf("\nTarget vertex Id: %d", target);
    System.out.printf("\nVertex out degree: %d", successors.length);
    System.out.printf("\nVertex in degree: ", predecessors.length);
    System.out.printf("\nVertex successors: %s", Arrays.toString(successors));
    System.out.printf("\nVertex predecessors: %s", Arrays.toString(predecessors));

    start = Instant.now().toEpochMilli();
    System.out.print("\n\nDepth First Search Edges:");
    graph.classifyGraph(target);
    finish = Instant.now().toEpochMilli();
    System.out.printf("\n\nFinished DFS in %d ms.", finish - start);

    start = Instant.now().toEpochMilli();
    Graph[] components = graph.getComponents();
    finish = Instant.now().toEpochMilli();
    System.out.printf("\n\nFinished Kosaraju Algorithm in %d ms.", finish - start);

    System.out.print("\n\nComponents Trees:");
    for (int c = 0; c < components.length; c++) {
      System.out.printf("\nComponent: %d", c + 1);
      System.out.printf("\n\tVertices: %s", Arrays.toString(components[c].getVertices()));
      System.out.printf("\n\tEdges: %s", components[c].getEdgesSet());
    }
  }

  /**
   * Reads the graph structure from the specified file. The file is expected to
   * start with the number of vertices and edges, followed by pairs of source
   * and target vertices representing edges.
   *
   * @param pathName The path to the text file containing the graph data.
   * @param builder  The builder for the desired graph structure.
   * @throws IOException If an I/O error occurs reading from the file.
   * @throws Exception   If the file is empty or malformed.
   */
  static void readGraph(String pathName, GraphBuilder builder) throws IOException, Exception {
    try (RandomAccessFile file = new RandomAccessFile(pathName, "r");
        FileChannel channel = file.getChannel()) {

      ByteBuffer buffer = ByteBuffer.allocateDirect(CHUNK_SIZE);

      // Recover the first chunk and process header
      int n, m;
      if (channel.read(buffer) != -1) {
        buffer.flip(); // Switch bucket to reading mode

        n = readNextInt(channel, buffer);
        m = readNextInt(channel, buffer);

        builder.initialize(n, m);
      } else {
        throw new Exception("The input file is empty.");
      }

      for (int i = 0; i < m; i++) {
        Integer source = readNextInt(channel, buffer);
        Integer target = readNextInt(channel, buffer);

        if (source == null || target == null) {
          System.err.println("Warning: End of the file reached before reading all 'm' edges.");
          break;
        }

        builder.addEdge(source, target);
      }
    }
  }

  /**
   * Reads the next integer from the file channel via the byte buffer.
   * This method automatically skips whitespace and line breaks.
   *
   * @param channel The {@link FileChannel} being read from.
   * @param buffer  The {@link ByteBuffer} acting as an intermediary chunk store.
   * @return The next parsed integer, or {@code null} if the End of File (EOF) is
   *         reached.
   * @throws IOException If an I/O error occurs reading the chunk.
   */
  public static Integer readNextInt(FileChannel channel, ByteBuffer buffer) throws IOException {
    int result = 0;
    boolean foundDigit = false;

    while (true) {
      // If empty, refill the buffer with the next chunk
      if (!buffer.hasRemaining()) {
        buffer.clear(); // Reset bucket markers for filling
        int bytesRead = channel.read(buffer); // Pour new data in

        // Check for file end
        if (bytesRead == -1) {
          return foundDigit ? result : null;
        }

        buffer.flip(); // Switch bucket to reading mode
      }

      byte b = buffer.get();

      // Check for valid digit
      if (b >= '0' && b <= '9') {
        result = (result * 10) + (b - '0');
        foundDigit = true;
      } else {
        // Next digit not found, so the number is complete
        if (foundDigit) {
          return result;
        }
      }
    }
  }
}

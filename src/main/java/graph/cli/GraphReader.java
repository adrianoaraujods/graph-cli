package graph.cli;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.LinkedHashMap;
import java.util.Map;

import graph.representations.GraphBuilder;

/**
 * Provides graph file reading capabilities.
 * <p>
 * This class reads graph files in the standard format: first line contains
 * "n m" (vertices and edge count), followed by m lines of "source target"
 * pairs.
 * Uses buffered I/O with direct ByteBuffer for efficient large file processing.
 * <p>
 * Parallel (duplicate) edges are handled according to the chosen
 * {@link ParallelEdgeStrategy}. The default strategy for all simplified
 * overloads is {@link ParallelEdgeStrategy#KEEP_LAST}.
 */
public class GraphReader {

  /** Size of the byte buffer used for reading files (64 KB). */
  static int CHUNK_SIZE = 64 * 1024;

  /**
   * Defines how parallel (duplicate) edges are handled during file reading.
   * <p>
   * Two edges are considered parallel when they share the same source and
   * target. For undirected graphs, (u, v) and (v, u) are treated as the same
   * edge.
   */
  public enum ParallelEdgeStrategy {
    /** All parallel edges are added as-is; no deduplication is performed. */
    ALLOW_PARALLEL,
    /** When a duplicate edge is encountered, the latest value overwrites the previous one. */
    KEEP_LAST,
    /** When a duplicate edge is encountered, the smaller of the two values is kept. */
    KEEP_MIN
  }

  /**
   * Reads a graph from a file and populates the provided builder.
   * <p>
   * The file format expects: first line with "n m" (number of vertices and
   * edges), followed by m lines each containing "source target" edge pairs.
   * If {@code isWeighted} is true, expects "source target weight" format.
   * If {@code hasCapacity} is true, expects "source target capacity" format.
   * <p>
   * When {@code strategy} is not {@link ParallelEdgeStrategy#ALLOW_PARALLEL},
   * duplicate edges are collected into a map before being added to the builder,
   * so the builder receives at most one edge per (source, target) pair.
   *
   * @param pathName    The path to the graph file.
   * @param builder     The GraphBuilder to populate with edges.
   * @param isWeighted  If true, parse the third column as a weight.
   * @param hasCapacity If true, parse the third column as a capacity.
   * @param hasFlag     If true, read a third header value and return it as a flag.
   * @param isDirected  If true, (u, v) and (v, u) are treated as distinct edges
   *                    when deduplicating. Has no effect with {@link ParallelEdgeStrategy#ALLOW_PARALLEL}.
   * @param strategy    How to handle parallel (duplicate) edges.
   * @return The flag value from the header, or {@code -1} if {@code hasFlag} is false.
   * @throws IOException If the file cannot be read.
   * @throws Exception   If the file format is invalid.
   */
  public static int readFile(String pathName, GraphBuilder builder, boolean isWeighted, boolean hasCapacity,
      boolean hasFlag, boolean isDirected, ParallelEdgeStrategy strategy) throws IOException, Exception {
    try (RandomAccessFile file = new RandomAccessFile(pathName, "r");
        FileChannel channel = file.getChannel()) {

      // Parse header (n, m, optional flag) from the first line(s). Handles both
      // single-line ("n m flag") and multi-line ("n\nm") header formats.
      int needed = hasFlag ? 3 : 2;
      int[] headerValues = new int[needed];
      int valuesRead = 0;
      while (valuesRead < needed) {
        String line = file.readLine();
        if (line == null) {
          throw new Exception("The input file is empty.");
        }
        String[] parts = line.trim().split("\\s+");
        for (String part : parts) {
          if (valuesRead < needed) {
            headerValues[valuesRead++] = Integer.parseInt(part);
          }
        }
      }
      int n = headerValues[0];
      int m = headerValues[1];
      int flag = hasFlag ? headerValues[2] : -1;

      builder.initialize(n, m, isWeighted, hasCapacity);

      ByteBuffer buffer = ByteBuffer.allocateDirect(CHUNK_SIZE);
      // Fill buffer with remaining data after the header line(s)
      channel.read(buffer);
      buffer.flip();

      boolean readThirdColumn = isWeighted || hasCapacity;

      if (strategy == ParallelEdgeStrategy.ALLOW_PARALLEL) {
        // add every edge directly without any concerns.
        for (int i = 0; i < m; i++) {
          Integer source = readNextInt(channel, buffer);
          Integer target = readNextInt(channel, buffer);
          Integer thirdValue = readThirdColumn ? readNextInt(channel, buffer) : null;

          if (source == null || target == null) {
            System.err.println("Warning: End of the file reached before reading all 'm' edges.");
            break;
          }

          if (thirdValue != null) {
            builder.addEdge(source, target, thirdValue);
          } else {
            builder.addEdge(source, target);
          }
        }
      } else {
        // collect edges into a map first, then flush to builder.
        // The key encodes (source, target) as a single long so lookups are O(1).
        Map<Long, Integer> edgeMap = new LinkedHashMap<>();

        for (int i = 0; i < m; i++) {
          Integer source = readNextInt(channel, buffer);
          Integer target = readNextInt(channel, buffer);
          Integer thirdValue = readThirdColumn ? readNextInt(channel, buffer) : null;

          if (source == null || target == null) {
            System.err.println("Warning: End of the file reached before reading all 'm' edges.");
            break;
          }

          long u = source;
          long v = target;
          // For undirected graphs, canonicalise the key so (u,v) == (v,u).
          long edgeKey = isDirected
              ? (u << 32) | (v & 0xFFFFFFFFL)
              : (Math.min(u, v) << 32) | (Math.max(u, v) & 0xFFFFFFFFL);

          int val = thirdValue != null ? thirdValue : 1;

          if (!edgeMap.containsKey(edgeKey)) {
            edgeMap.put(edgeKey, val);
          } else if (strategy == ParallelEdgeStrategy.KEEP_LAST) {
            edgeMap.put(edgeKey, val);
          } else if (strategy == ParallelEdgeStrategy.KEEP_MIN) {
            edgeMap.put(edgeKey, Math.min(edgeMap.get(edgeKey), val));
          }
        }

        // Flush deduplicated edges to the builder.
        for (Map.Entry<Long, Integer> entry : edgeMap.entrySet()) {
          int u = (int) (entry.getKey() >> 32);
          int v = (int) (entry.getKey() & 0xFFFFFFFFL);

          if (readThirdColumn) {
            builder.addEdge(u, v, entry.getValue());
          } else {
            builder.addEdge(u, v);
          }
        }
      }

      return flag;
    }
  }

  /**
   * Reads a graph from a file, with {@link ParallelEdgeStrategy#KEEP_LAST} as
   * the default deduplication strategy for an undirected graph.
   *
   * @param pathName    The path to the graph file.
   * @param builder     The GraphBuilder to populate with edges.
   * @param isWeighted  If true, parse the third column as a weight.
   * @param hasCapacity If true, parse the third column as a capacity.
   * @param hasFlag     If true, read a third header value and return it as a flag.
   * @return The flag value from the header, or {@code -1} if {@code hasFlag} is false.
   * @throws IOException If the file cannot be read.
   * @throws Exception   If the file format is invalid.
   */
  public static int readFile(String pathName, GraphBuilder builder, boolean isWeighted, boolean hasCapacity,
      boolean hasFlag) throws IOException, Exception {
    return readFile(pathName, builder, isWeighted, hasCapacity, hasFlag, false, ParallelEdgeStrategy.KEEP_LAST);
  }

  /**
   * Reads a graph from a file, with {@link ParallelEdgeStrategy#KEEP_LAST} as
   * the default deduplication strategy.
   *
   * @param pathName   The path to the graph file.
   * @param builder    The GraphBuilder to populate with edges.
   * @param isWeighted If true, parse the third column as a weight.
   * @throws IOException If the file cannot be read.
   * @throws Exception   If the file format is invalid.
   */
  public static void readFile(String pathName, GraphBuilder builder, boolean isWeighted) throws IOException, Exception {
    readFile(pathName, builder, isWeighted, false, false, false, ParallelEdgeStrategy.KEEP_LAST);
  }

  /**
   * Reads an unweighted graph from a file, with {@link ParallelEdgeStrategy#KEEP_LAST}
   * as the default deduplication strategy.
   *
   * @param pathName The path to the graph file.
   * @param builder  The GraphBuilder to populate with edges.
   * @throws IOException If the file cannot be read.
   * @throws Exception   If the file format is invalid.
   */
  public static void readFile(String pathName, GraphBuilder builder) throws IOException, Exception {
    readFile(pathName, builder, false, false, false, false, ParallelEdgeStrategy.KEEP_LAST);
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
  private static Integer readNextInt(FileChannel channel, ByteBuffer buffer) throws IOException {
    int result = 0;
    boolean foundDigit = false;
    boolean negative = false;

    while (true) {
      // If empty, refill the buffer with the next chunk
      if (!buffer.hasRemaining()) {
        buffer.clear(); // Reset bucket markers for filling
        int bytesRead = channel.read(buffer); // Pour new data in

        // Check for file end
        if (bytesRead == -1) {
          return foundDigit ? (negative ? -result : result) : null;
        }

        buffer.flip(); // Switch bucket to reading mode
      }

      byte b = buffer.get();

      if (b == '-' && !foundDigit) {
        negative = true;
      } else if (b >= '0' && b <= '9') {
        result = (result * 10) + (b - '0');
        foundDigit = true;
      } else {
        // Next digit not found, so the number is complete
        if (foundDigit) {
          return negative ? -result : result;
        }
        // '-' followed by non-digit is not a negative number; reset
        negative = false;
      }
    }
  }
}

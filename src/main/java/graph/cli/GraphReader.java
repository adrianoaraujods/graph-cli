package graph.cli;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import graph.representations.GraphBuilder;

/**
 * Provides graph file reading capabilities.
 * <p>
 * This class reads graph files in the standard format: first line contains
 * "n m" (vertices and edge count), followed by m lines of "source target"
 * pairs.
 * Uses buffered I/O with direct ByteBuffer for efficient large file processing.
 */
public class GraphReader {
  /** Size of the byte buffer used for reading files (64 KB). */
  static int CHUNK_SIZE = 64 * 1024;

  /**
   * Reads a graph from a file and populates the provided builder.
   * <p>
   * The file format expects: first line with "n m" (number of vertices and
   * edges), followed by m lines each containing "source target" edge pairs.
   * If isWeighted is true, expects "source target weight" format.
   * If hasCapacity is true, expects "source target capacity" format.
   *
   * @param pathName    The path to the graph file.
   * @param builder     The GraphBuilder to populate with edges.
   * @param isWeighted  If true, parse third int as weight.
   * @param hasCapacity If true, parse third int as capacity.
   * @throws IOException If the file cannot be read.
   * @throws Exception   If the file format is invalid.
   */
  public static int readFile(String pathName, GraphBuilder builder, boolean isWeighted, boolean hasCapacity,
      boolean hasFlag) throws IOException, Exception {
    try (RandomAccessFile file = new RandomAccessFile(pathName, "r");
        FileChannel channel = file.getChannel()) {

      ByteBuffer buffer = ByteBuffer.allocateDirect(CHUNK_SIZE);

      // Recover the first chunk and process header
      int n, m, flag = -1;
      if (channel.read(buffer) != -1) {
        buffer.flip(); // Switch bucket to reading mode

        n = readNextInt(channel, buffer);
        m = readNextInt(channel, buffer);

        if (hasFlag) {
          flag = readNextInt(channel, buffer);
        }

        builder.initialize(n, m, isWeighted, hasCapacity);
      } else {
        throw new Exception("The input file is empty.");
      }

      boolean readThirdColumn = isWeighted || hasCapacity;

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

      return flag;
    }
  }

  /**
   * Reads a graph from a file and populates the provided builder.
   * <p>
   * The file format expects: first line with "n m" (number of vertices and
   * edges), followed by m lines each containing "source target" edge pairs.
   *
   * @param pathName The path to the graph file.
   * @param builder  The GraphBuilder to populate with edges.
   * @throws IOException If the file cannot be read.
   * @throws Exception   If the file format is invalid.
   */
  public static void readFile(String pathName, GraphBuilder builder, boolean isWeighted) throws IOException, Exception {
    readFile(pathName, builder, isWeighted, false, false);
  }

  public static void readFile(String pathName, GraphBuilder builder) throws IOException, Exception {
    readFile(pathName, builder, false, false, false);
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

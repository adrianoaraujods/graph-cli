package graph.cli;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import graph.representations.GraphBuilder;

public class GraphReader {
  /** Size of the byte buffer used for reading files (64 KB). */
  static int CHUNK_SIZE = 64 * 1024;

  public static void readFile(String pathName, GraphBuilder builder) throws IOException, Exception {
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
  private static Integer readNextInt(FileChannel channel, ByteBuffer buffer) throws IOException {
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

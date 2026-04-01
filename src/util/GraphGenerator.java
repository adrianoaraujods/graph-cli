package src.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

/**
 * Utility class to generate random simple directed graphs (no self-loops).
 * <p>
 * Uses a highly optimized memoryless geometric jump algorithm to generate
 * edges uniformly at random in O(m) time without holding the graph in memory.
 */
public class GraphGenerator {
  /** Size of the byte buffer used for writing files (64 KB). */
  private static final int CHUNK_SIZE = 64 * 1024;

  /** The time interval for logging the current progress (2 seconds). */
  private static final int LOGGING_INTERVAL = 2000;

  /**
   * Generates and writes the random graph to disk using a geometric jump
   * distribution.
   *
   * @param outputPath The file path where the graph will be saved.
   * @param n          The total number of vertices.
   * @param m          The target number of edges to generate.
   * @param maxEdges   The maximum possible edges (n * (n - 1)).
   */
  public static void generateAndWriteGraph(String outputPath, long n, long m, long maxEdges) {
    System.out.printf("Starting memoryless edge generation for %,d edges...\n", m);

    long startTime = System.currentTimeMillis();
    long lastLogTime = startTime;
    long generated = 0;
    long index = 0; // Represents the 1D position in the total space of possible edges

    Random random = new Random();

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath), CHUNK_SIZE)) {
      // Write the header
      writer.write(Long.toString(n));
      writer.write(' ');
      writer.write(Long.toString(m));
      writer.newLine();

      while (generated < m && index < maxEdges) {
        // Calculate the dynamic probability of picking the next available edge
        double p = (double) (m - generated) / (maxEdges - index);
        long jump;

        if (p >= 1.0) {
          jump = 0; // We must pick every remaining edge to fulfill 'm'
        } else {
          // Mathematically calculate how many non-edges to skip.
          jump = (long) (Math.log(1.0 - random.nextDouble()) / Math.log(1.0 - p));
        }

        index += jump;

        // Failsafe if the jump pushes us out of bounds
        if (index >= maxEdges) {
          break;
        }

        // Decode the 1D index directly into a 2D (v, w) edge coordinate
        long v = (index / (n - 1)) + 1;
        long w = (index % (n - 1)) + 1;

        // Shift 'w' to completely prevent self-loops
        if (w >= v) {
          w++;
        }

        writer.write(Long.toString(v));
        writer.write(' ');
        writer.write(Long.toString(w));
        writer.newLine();

        generated++;
        index++;

        // Progress logging
        if ((generated & 0xFFFFF) == 0) {
          long now = System.currentTimeMillis();
          if (now - lastLogTime > LOGGING_INTERVAL) {
            System.out.printf("[%d ms] ... written %,d / %,d edges\n", (now - startTime), generated, m);
            lastLogTime = now;
          }
        }
      }

      // Ensure the geometric jump didn't under-generate by a fraction
      if (generated < m) {
        System.err.printf(
            "\nWarning: Target was %,d edges, but only %,d were generated due to probability boundary limits.\n", m,
            generated);
      } else {
        System.out.printf("[%d ms] Graph successfully generated! Available at: ./%s\n",
            (System.currentTimeMillis() - startTime), outputPath);
      }

    } catch (IOException e) {
      System.err.println("An error occurred while writing to the file: " + e.getMessage());
    }
  }
}

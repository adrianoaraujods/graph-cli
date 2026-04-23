package graph.cli;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;
import java.util.Set;

import graph.algorithms.ErdosGallai;
import graph.algorithms.HavelHakimi;

/**
 * Provides graph generation capabilities for creating synthetic graph data.
 * <p>
 * This class supports generating various graph types including random graphs,
 * Eulerian graphs, semi-Eulerian graphs, and weakly connected graphs.
 * Graphs can be specified by edge count or density.
 */
public class GraphGenerator {

  // Config
  private static final int CHUNK_SIZE = 64 * 1_024;
  private static final int LOGGING_INTERVAL = 5_000;
  private static final long LARGE_EDGE_THRESHOLD = 50_000_000L;

  private String outputPath;
  private Random random;

  // Graph details
  private ConnectivityType connectivity = ConnectivityType.CONNECTED;
  private boolean isDirected = true;
  private int n = -1;
  private long m = -1;
  private double density = -1;

  // Logging
  private boolean enableLog = false;
  private long startTime;
  private long lastLogTime;
  private long written;

  /**
   * Specifies the connectivity type for generated graphs.
   */
  public enum ConnectivityType {
    DISCONNECTED,
    CONNECTED,
    EULERIAN,
    SEMI_EULERIAN
  }

  /**
   * Immutable configuration for graph generation.
   *
   * @param n          The number of vertices in the graph.
   * @param isDirected Whether the graph is directed.
   * @param outputPath The file path to write the generated graph.
   */
  public GraphGenerator(int n, boolean isDirected, String outputPath) {
    this.n = n;
    this.isDirected = isDirected;
    this.outputPath = outputPath;
    random = new Random();
  }

  public void enableLog() {
    enableLog = true;
  }

  public void setSeed(Long seed) {
    random = new Random(seed);
  }

  public void setConnectivity(ConnectivityType connectivity) {
    this.connectivity = connectivity;
  }

  public void setEdges(long m) {
    this.m = m;
    this.density = (double) m / maxEdges();
    ensureEvenEdges();
  }

  public void setDensity(double density) {
    this.density = density;
    this.m = (long) (isDirected ? maxEdges() * density : (maxEdges() * density) / 2);
    ensureEvenEdges();
  }

  private void ensureEvenEdges() {
    if (connectivity != ConnectivityType.EULERIAN &&
        connectivity != ConnectivityType.SEMI_EULERIAN) {
      return; // No change needed non-Eulerian
    }

    System.out.println((m % 2) != 0);

    if ((m % 2) != 0) {
      if (maxEdges() % 2 != 0) {
        m--; // Round DOWN
      } else {
        m++; // Round UP
      }
    }
  }

  public double getDensity() {
    return density;
  }

  public long getEdges() {
    return m;
  }

  public long maxEdges() {
    return isDirected ? n * (n - 1) : n * (n - 1) / 2;
  }

  private boolean confirmLargeGraph() {
    if (m <= LARGE_EDGE_THRESHOLD) {
      return true;
    }

    System.out.printf("\n[WARNING] You are about to generate a graph with %,d edges.", m);
    System.out.printf("\nThis may take a very long time and produce a very large file. Continue? [y/N] ");
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    try {
      String line = reader.readLine();
      return "y".equalsIgnoreCase(line) || "Y".equalsIgnoreCase(line);
    } catch (IOException e) {
      return false;
    }
  }

  /**
   * Builds the GraphConfig from the current builder state.
   *
   * @return The constructed GraphConfig.
   * @throws IllegalStateException    If required fields are missing or invalid.
   * @throws IllegalArgumentException If edge/connectivity constraints are
   *                                  violated.
   */
  public void create() {
    if (n <= 0) {
      throw new IllegalStateException("Vertices must be specified and greater than 0");
    }

    if (n == 1 && m > 0) {
      throw new IllegalStateException("Single vertex graph can only have 0 edges");
    }

    if (outputPath == null || outputPath.isEmpty()) {
      throw new IllegalStateException("Output path must be specified");
    }

    if (m < 0 && density < 0) {
      throw new IllegalStateException("Must specify either edges or density");
    }

    if (density >= 0 && (density < 0.0 || density > 1.0)) {
      throw new IllegalStateException("Density must be between 0.0 and 1.0");
    }

    if (m >= 0 && m > maxEdges()) {
      throw new IllegalStateException("Edges must be between 0 and maxEdges");
    }

    if (connectivity == ConnectivityType.EULERIAN && m < n) {
      throw new IllegalArgumentException(
          "Eulerian graph with " + n + " vertices requires at least " + n + " edges");
    }

    if (connectivity == ConnectivityType.SEMI_EULERIAN && m < n - 1) {
      throw new IllegalArgumentException(
          "Semi-Eulerian graph with " + m + " vertices requires at least " + (m - 1) + " edges");
    }

    if (!isDirected && m < n - 1 && connectivity != ConnectivityType.DISCONNECTED) {
      throw new IllegalArgumentException(
          "Undirected " + connectivity + " graph with " + n + " vertices requires at least " + (n - 1)
              + " edges, but only " + m + " specified");
    }

    if (!confirmLargeGraph()) {
      System.out.println("Graph generation cancelled.");
      return;
    }

    System.out.printf("Starting %s graph generation with %,d edges...\n", connectivity.toString().toLowerCase(), m);

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath), CHUNK_SIZE)) {
      // Write header
      writer.write(n + " " + m);
      writer.newLine();

      startTime = System.currentTimeMillis();
      lastLogTime = startTime; // Add this line

      if (density >= 1) {
        generateCompleteGraph(writer);
      }

      if (isDirected) {
        generateDirectedGraph(writer);

      } else {
        generateUndirectedGraph(writer);
      }

      long elapsed = System.currentTimeMillis() - startTime;
      System.out.printf("[%d ms] Graph successfully generated! Available at: ./%s\n", elapsed, outputPath);

    } catch (IOException e) {
      System.err.println("[Error] An error occurred while writing to the file: " + e.getMessage());
    }
  }

  private void generateCompleteGraph(BufferedWriter writer) {
    // TODO
  }

  private void generateDirectedGraph(BufferedWriter writer) {
    // TODO
  }

  private void generateUndirectedGraph(BufferedWriter writer) {
    int[] sequence = createDegreeSequence();

    Set<String> edges = HavelHakimi.generateEdges(sequence, connectivity != ConnectivityType.DISCONNECTED);

    if (edges == null) {
      System.err.println("[Error] Failed to construct graph from degree sequence");
      return;
    }

    try {
      for (String edge : edges) {
        String[] e = edge.split("-");
        int v = Integer.parseInt(e[0]) + 1;
        int w = Integer.parseInt(e[1]) + 1;

        writer.write(v + " " + w);
        writer.newLine();
        written++;
        logWritten();
      }
    } catch (IOException e) {
      System.err.println("[Error] Failed to write edges: " + e.getMessage());
    }
  }

  private int[] createDegreeSequence() {
    int[] sequence = new int[n];
    long targetSum = 2 * m;

    int maxDegree = n - 1;
    int avgDegree = (int) (targetSum / n);
    int minDegree = 0;

    boolean evenDegrees = false;

    if (connectivity == ConnectivityType.CONNECTED) {
      minDegree = 1;
    } else if (connectivity == ConnectivityType.EULERIAN || connectivity == ConnectivityType.SEMI_EULERIAN) {
      minDegree = 2;
      avgDegree = (int) ((targetSum / n) / 2) * 2; // Ensure is even
      evenDegrees = true;
    }

    for (int v = 0; v < n; v++) {
      sequence[v] = avgDegree;
    }

    int remainder = (int) (targetSum - (avgDegree * n));
    for (int i = 0; i < remainder / 2; i++) {
      sequence[random.nextInt(n)] += 2;
    }

    // make two vertices have even degree
    if (connectivity == ConnectivityType.SEMI_EULERIAN) {
      while (true) {
        int v = random.nextInt(n);
        int w = random.nextInt(n);

        if (v != w) {
          if (v - 1 > minDegree && w + 1 < maxDegree) {
            sequence[v] -= 1;
            sequence[w] += 1;
            break;

          } else if (w - 1 > minDegree && sequence[w] + 1 <= maxDegree) {
            sequence[w] -= 1;
            sequence[v] += 1;
            break;
          }
        }
      }
    }

    for (int v = n - 1; v > 0; v--) {
      int w = random.nextInt(v + 1);

      int maxPossibleDiff = sequence[v] - minDegree;
      if (maxPossibleDiff <= 0) {
        continue;
      }

      int diff;
      if (evenDegrees) {
        int halfDiff = Math.max(1, maxPossibleDiff / 2);
        diff = random.nextInt(1, halfDiff + 1) * 2;
      } else {
        diff = random.nextInt(1, maxPossibleDiff + 1);
      }

      if (sequence[v] - diff >= minDegree && sequence[w] + diff <= maxDegree) {
        sequence[v] -= diff;
        sequence[w] += diff;
      } else if (sequence[w] - diff >= minDegree && sequence[v] + diff <= maxDegree) {
        sequence[w] -= diff;
        sequence[v] += diff;
      }
    }

    return ErdosGallai.ensureValidSequence(sequence, evenDegrees);
  }

  private void logWritten() {
    if (enableLog) {
      long now = System.currentTimeMillis();

      if (now - lastLogTime > LOGGING_INTERVAL) {
        System.out.printf("\n[%d ms] Written %,d / %,d edges", (now - startTime), written, m);
        lastLogTime = now;
      }
    }
  }
}

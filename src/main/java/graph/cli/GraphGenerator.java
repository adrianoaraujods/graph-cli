package graph.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import graph.algorithms.FisherYates;
import graph.api.ConnectivityType;
import graph.api.Edges;

/**
 * Provides graph generation capabilities for creating synthetic graph data.
 * <p>
 * This class supports generating various graph types including random graphs,
 * Eulerian graphs, semi-Eulerian graphs, and weakly connected graphs.
 * Graphs can be specified by edge count or density.
 */
public class GraphGenerator {

  // Config
  private static final long LARGE_EDGE_THRESHOLD = 50_000_000L;
  private String outputPath;
  private Random random;

  // Graph details
  private ConnectivityType connectivity = ConnectivityType.CONNECTED;
  private boolean isDirected = true;
  private int n = -1;
  private long m = -1;
  private double density = -1;
  Set<Long> edges = null;

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

    if ((m % 2) != 0) {
      m++; // Round UP
    }
  }

  public double getDensity() {
    return density;
  }

  public long getEdges() {
    return m;
  }

  public long maxEdges() {
    return isDirected ? (long) (n) * ((long) (n - 1)) : (long) (n) * ((long) (n - 1)) / 2;
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

  public void validateGraph() {
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
  }

  /**
   * Builds the GraphConfig from the current builder state.
   *
   * @return The constructed GraphConfig.
   * @throws IOException
   * @throws IllegalStateException    If required fields are missing or invalid.
   * @throws IllegalArgumentException If edge/connectivity constraints are
   *                                  violated.
   */
  public long create() throws Exception {
    validateGraph();

    if (!confirmLargeGraph()) {
      System.out.println("Graph generation cancelled.");
      return -1;
    }

    edges = new HashSet<>(m > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) m);

    if (isDirected) {
      // TODO: implement directed method
      throw new Exception("[ERROR] Graph generation for directed not supported.");
    } else {
      generateUndirected();
    }

    m = edges.size();
    try (GraphWritter writer = new GraphWritter(n, m, outputPath)) {
      writer.writeHeader();
      writer.writeEdge(edges);
    } catch (IOException e) {
      System.err.println("[Error] An error occurred while writing to the file: " + e.getMessage());
    }

    return m;
  }

  private void generateUndirected() throws Exception {
    if (connectivity == ConnectivityType.DISCONNECTED) {
      fillWithRandomEdges();
      return;
    }

    if (connectivity == ConnectivityType.EULERIAN) {
      addPath(true);
    } else {
      addPath(false);
    }

    if (connectivity == ConnectivityType.CONNECTED) {
      fillWithRandomEdges();
    } else {
      fillWithRandomCycles();
    }
  }

  private void addPath(boolean cycle) {
    if (edges == null) {
      edges = new HashSet<>(n);
    }

    int[] path = new int[n];

    for (int i = 0; i < n; i++) {
      path[i] = i;
    }

    FisherYates.shuffle(path, random);

    for (int i = 0; i < (n - 1); i++) {
      edges.add(Edges.undirected(path[i], path[i + 1]));
    }

    if (cycle) {
      edges.add(Edges.undirected(path[n - 1], path[0]));
    }
  }

  private void fillWithRandomEdges() {
    if (edges == null) {
      edges = new HashSet<>(m > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) m);
    }

    long maxAttempts = m * 10; // Prevent infinite loops in highly dense graphs
    int attempts = 0;

    while (edges.size() < m && attempts < maxAttempts) {
      int u = random.nextInt(n);
      int v = random.nextInt(n);

      if (u != v)
        edges.add(Edges.undirected(u, v));
      attempts++;
    }
  }

  private void fillWithRandomCycles() {
    if (edges == null) {
      edges = new HashSet<>(m > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) m);
    }

    long maxAttempts = m * 10;
    int attempts = 0;

    // Note: Because we add edges 3 at a time, we might slightly overshoot the
    // targetEdge count.
    while (edges.size() < m && attempts < maxAttempts) {
      int u = random.nextInt(n);
      int v = random.nextInt(n);
      int w = random.nextInt(n);

      if (u != v && v != w && u != w) {
        long e1 = Edges.undirected(u, v);
        long e2 = Edges.undirected(v, w);
        long e3 = Edges.undirected(w, u);

        // Only add the cycle if NONE of the edges already exist,
        // preventing duplicate edge logic from breaking degree parity
        if (!edges.contains(e1) && !edges.contains(e2) && !edges.contains(e3)) {
          edges.add(e1);
          edges.add(e2);
          edges.add(e3);
        }
      }

      attempts++;
    }
  }
}

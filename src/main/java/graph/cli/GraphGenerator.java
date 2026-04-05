package graph.cli;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Provides graph generation capabilities for creating synthetic graph data.
 * <p>
 * This class supports generating various graph types including random graphs,
 * Eulerian graphs, semi-Eulerian graphs, and weakly connected graphs.
 * Graphs can be specified by edge count or density.
 */
public class GraphGenerator {
  private static final int CHUNK_SIZE = 64 * 1024;
  private static final int LOGGING_INTERVAL = 5_000;
  private static final double DENSE_THRESHOLD = 0.3;
  private static final int MAX_SAMPLING_ATTEMPTS = 10;

  /**
   * Specifies the connectivity type for generated graphs.
   */
  public enum ConnectivityType {

    /** No connectivity requirement - simple random graph. */
    NONE,

    /** Graph is weakly connected (one connected component). */
    WEAKLY,

    /**
     * Graph has an Eulerian circuit (all vertices have even degree).
     * Requires at least n edges for n vertices.
     */
    EULERIAN,

    /**
     * Graph has an Eulerian path but not circuit (exactly two vertices with odd
     * degree).
     * Requires at least n-1 edges for n vertices.
     */
    SEMI_EULERIAN
  }

  /**
   * Immutable configuration for graph generation.
   *
   * @param vertices     The number of vertices in the graph.
   * @param directed     Whether the graph is directed.
   * @param connectivity The connectivity type requirement.
   * @param edges        The target number of edges (-1 if using density).
   * @param density      The target edge density (-1 if using edge count).
   * @param outputPath   The file path to write the generated graph.
   * @param seed         The random seed for reproducibility (null for random).
   */
  public record GraphConfig(
      int vertices,
      boolean directed,
      ConnectivityType connectivity,
      long edges,
      double density,
      String outputPath,
      Long seed) {

    public long maxEdges() {
      return directed ? (long) vertices * (vertices - 1) : (long) vertices * (vertices - 1) / 2;
    }

    public long resolvedEdges() {
      if (edges >= 0) {
        return edges;
      }
      if (density >= 0) {
        return Math.round(maxEdges() * density);
      }
      throw new IllegalStateException("Neither edges nor density specified");
    }

    public double resolvedDensity() {
      if (density >= 0) {
        return density;
      }
      if (edges >= 0) {
        return (double) edges / maxEdges();
      }
      throw new IllegalStateException("Neither edges nor density specified");
    }
  }

  public static class Builder {
    private int vertices = -1;
    private boolean directed = true;
    private ConnectivityType connectivity = ConnectivityType.WEAKLY;
    private long edges = -1;
    private double density = -1;
    private String outputPath = null;
    private Long seed = null;

    /**
     * Sets whether the graph should be directed.
     *
     * @param directed True for directed graph, false for undirected.
     * @return This builder for chaining.
     */
    public Builder directed(boolean directed) {
      this.directed = directed;
      return this;
    }

    /**
     * Sets the graph as undirected (shorthand for directed(false)).
     *
     * @return This builder for chaining.
     */
    public Builder undirected() {
      this.directed = false;
      return this;
    }

    /**
     * Sets the connectivity type.
     *
     * @param connectivity The desired connectivity type.
     * @return This builder for chaining.
     */
    public Builder connectivity(ConnectivityType connectivity) {
      this.connectivity = connectivity;
      return this;
    }

    /**
     * Sets the number of vertices.
     *
     * @param vertices The number of vertices (must be > 0).
     * @return This builder for chaining.
     */
    public Builder vertices(int vertices) {
      this.vertices = vertices;
      return this;
    }

    /**
     * Sets the exact number of edges to generate.
     *
     * @param edges The target edge count.
     * @return This builder for chaining.
     */
    public Builder edges(long edges) {
      this.edges = edges;
      return this;
    }

    /**
     * Sets the edge density for generation.
     *
     * @param density The edge density between 0.0 and 1.0.
     * @return This builder for chaining.
     */
    public Builder density(double density) {
      this.density = density;
      return this;
    }

    /**
     * Sets the output file path.
     *
     * @param outputPath The path where the graph will be written.
     * @return This builder for chaining.
     */
    public Builder outputPath(String outputPath) {
      this.outputPath = outputPath;
      return this;
    }

    /**
     * Sets the random seed for reproducible generation.
     *
     * @param seed The random seed value.
     * @return This builder for chaining.
     */
    public Builder seed(long seed) {
      this.seed = seed;
      return this;
    }

    /**
     * Sets the random seed for reproducible generation.
     *
     * @param seed The random seed value (can be null for random).
     * @return This builder for chaining.
     */
    public Builder seed(Long seed) {
      this.seed = seed;
      return this;
    }

    /**
     * Builds the GraphConfig from the current builder state.
     *
     * @return The constructed GraphConfig.
     * @throws IllegalStateException    If required fields are missing or invalid.
     * @throws IllegalArgumentException If edge/connectivity constraints are
     *                                  violated.
     */
    public GraphConfig build() {
      if (vertices <= 0) {
        throw new IllegalStateException("Vertices must be specified and > 0");
      }
      if (vertices == 1 && edges > 0) {
        throw new IllegalArgumentException("Single vertex graph can only have 0 edges");
      }
      if (outputPath == null || outputPath.isEmpty()) {
        throw new IllegalStateException("Output path must be specified");
      }
      if (edges >= 0 && density >= 0) {
        throw new IllegalStateException("Cannot specify both edges and density");
      }
      if (edges < 0 && density < 0) {
        throw new IllegalStateException("Must specify either edges or density");
      }
      if (density >= 0 && (density < 0.0 || density > 1.0)) {
        throw new IllegalStateException("Density must be between 0.0 and 1.0");
      }

      long maxEdges = directed ? (long) vertices * (vertices - 1) : (long) vertices * (vertices - 1) / 2;
      long resolvedEdges = (edges >= 0) ? edges : Math.round(maxEdges * density);

      if (edges >= 0 && (edges < 0 || edges > maxEdges)) {
        throw new IllegalStateException("Edges must be between 0 and maxEdges");
      }

      if (connectivity == ConnectivityType.EULERIAN && resolvedEdges < vertices) {
        throw new IllegalArgumentException(
            "Eulerian graph requires at least " + vertices + " edges");
      }
      if (connectivity == ConnectivityType.SEMI_EULERIAN && resolvedEdges < vertices - 1) {
        throw new IllegalArgumentException(
            "Semi-Eulerian graph requires at least " + (vertices - 1) + " edges");
      }

      return new GraphConfig(vertices, directed, connectivity, edges, density, outputPath, seed);
    }
  }

  /**
   * Creates a new GraphGenerator builder.
   *
   * @return A new Builder instance.
   */
  public static Builder builder() {
    return new Builder();
  }

  private static final long LARGE_EDGE_THRESHOLD = 50_000_000L;

  private static boolean confirmLargeGraph(long edges) {
    if (edges <= LARGE_EDGE_THRESHOLD) {
      return true;
    }

    System.out.printf("%nWARNING: You are about to generate %,d edges.%n", edges);
    System.out.print("This may take a very long time and produce a very large file. Continue? [y/N] ");
    java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));

    try {
      String line = reader.readLine();
      return "y".equalsIgnoreCase(line) || "Y".equalsIgnoreCase(line);
    } catch (java.io.IOException e) {
      return false;
    }
  }

  /**
   * Generates a graph according to the provided configuration.
   * <p>
   * This method creates a graph file in the format: first line contains
   * "n m" (vertices and edge count), followed by m lines of "source target"
   * pairs.
   *
   * @param config The graph configuration specifying all generation parameters.
   */
  public static void generateGraph(GraphConfig config) {
    int n = config.vertices();
    long m = config.resolvedEdges();
    double density = config.resolvedDensity();
    boolean isDirected = config.directed();
    ConnectivityType connectivity = config.connectivity();
    String outputPath = config.outputPath();

    Random random = config.seed() != null ? new Random(config.seed()) : new Random();

    if (!confirmLargeGraph(m)) {
      System.out.println("Graph generation cancelled.");
      return;
    }

    long startTime = System.currentTimeMillis();

    System.out.printf("Starting %s graph generation for %,d edges...%n",
        connectivity == ConnectivityType.NONE ? "simple" : connectivity.toString().toLowerCase(), m);

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath), CHUNK_SIZE)) {
      writer.write(n + " " + m);
      writer.newLine();

      if (connectivity == ConnectivityType.EULERIAN) {
        generateEulerianGraph(writer, n, m, isDirected, random, startTime);
      } else if (connectivity == ConnectivityType.SEMI_EULERIAN) {
        generateSemiEulerianGraph(writer, n, m, random, startTime);
      } else {
        if (density > DENSE_THRESHOLD) {
          generateDenseGraph(writer, n, m, isDirected, random, startTime);
        } else {
          generateSparseGraph(writer, n, m, isDirected, connectivity, random, startTime);
        }
      }

      long elapsed = System.currentTimeMillis() - startTime;
      System.out.printf("[%d ms] Graph successfully generated! Available at: ./%s%n", elapsed, outputPath);

    } catch (IOException e) {
      System.err.println("An error occurred while writing to the file: " + e.getMessage());
    }
  }

  private static void generateSparseGraph(BufferedWriter writer, int n, long m, boolean isDirected,
      ConnectivityType connectivity, Random random, long startTime)
      throws IOException {
    Set<Long> edgeSet = new HashSet<>();

    if (connectivity == ConnectivityType.WEAKLY && m >= n - 1) {
      int treeEdges = (int) Math.min(m, n - 1);
      generateSpanningTreeLimited(writer, edgeSet, n, random, isDirected, treeEdges);
    }

    long generated = edgeSet.size();

    long maxAttempts = m * MAX_SAMPLING_ATTEMPTS;
    long attempts = 0;
    long lastLogTime = startTime;

    while (generated < m && attempts < maxAttempts) {
      int v = random.nextInt(n) + 1;
      int w = random.nextInt(n) + 1;

      if (v == w) {
        attempts++;
        continue;
      }

      long edgeKey = edgeKey(v, w, isDirected);
      if (edgeSet.contains(edgeKey)) {
        attempts++;
        continue;
      }

      writer.write(v + " " + w);
      writer.newLine();
      edgeSet.add(edgeKey);
      generated++;
      attempts = 0;

      if ((generated & 0xFFFF) == 0) {
        long now = System.currentTimeMillis();
        if (now - lastLogTime > LOGGING_INTERVAL) {
          System.out.printf("[%d ms] ... written %,d / %,d edges%n", (now - startTime), generated, m);
          lastLogTime = now;
        }
      }
    }

    if (generated < m) {
      System.err.printf("%nWarning: Target was %,d edges, but only %,d were generated.%n", m, generated);
    }

    if (connectivity == ConnectivityType.WEAKLY && m >= n - 1 && generated >= n - 1) {
      ensureConnectivitySparse(writer, edgeSet, n, random, startTime);
    }
  }

  private static void generateSpanningTreeLimited(BufferedWriter writer, Set<Long> edgeSet, int n, Random random,
      boolean isDirected, int targetEdges)
      throws IOException {
    if (targetEdges <= 0)
      return;

    ArrayList<Integer> vertices = new ArrayList<>();
    for (int i = 1; i <= n; i++) {
      vertices.add(i);
    }
    Collections.shuffle(vertices, random);

    ArrayList<Integer> connected = new ArrayList<>();
    ArrayList<Integer> remaining = new ArrayList<>(vertices);

    connected.add(remaining.remove(0));

    int edgesAdded = 0;
    while (!remaining.isEmpty() && edgesAdded < targetEdges) {
      int v = connected.get(random.nextInt(connected.size()));
      int w = remaining.get(random.nextInt(remaining.size()));

      writer.write(v + " " + w);
      writer.newLine();

      edgeSet.add(edgeKey(v, w, isDirected));
      connected.add(w);
      remaining.remove(Integer.valueOf(w));
      edgesAdded++;
    }
  }

  private static void generateDenseGraph(BufferedWriter writer, int n, long m, boolean isDirected, Random random,
      long startTime)
      throws IOException {
    long maxEdges = isDirected ? (long) n * (n - 1) : (long) n * (n - 1) / 2;
    if (maxEdges > Integer.MAX_VALUE) {
      throw new IllegalArgumentException("Graph too dense for available memory");
    }

    int[] indices = new int[(int) maxEdges];
    for (int i = 0; i < maxEdges; i++) {
      indices[i] = i;
    }

    for (int i = indices.length - 1; i > 0; i--) {
      int j = random.nextInt(i + 1);
      int temp = indices[i];
      indices[i] = indices[j];
      indices[j] = temp;
    }

    long lastLogTime = startTime;
    Set<Long> edgeSet = new HashSet<>();
    long written = 0;

    for (int idx = 0; idx < indices.length && written < m; idx++) {
      int edgeIdx = indices[idx];

      int v, w;
      if (isDirected) {
        v = edgeIdx / (n - 1) + 1;
        w = edgeIdx % (n - 1) + 1;
        if (w >= v)
          w++;
      } else {
        long t = (long) Math.ceil((Math.sqrt(8.0 * edgeIdx + 1) - 1) / 2);
        long row = t * (t - 1) / 2;
        v = (int) t;
        w = (int) (edgeIdx - row + v + 1);
      }

      long key = edgeKey(v, w, isDirected);
      edgeSet.add(key);
      writer.write(v + " " + w);
      writer.newLine();
      written++;

      if ((written & 0xFFFF) == 0) {
        long now = System.currentTimeMillis();
        if (now - lastLogTime > LOGGING_INTERVAL) {
          System.out.printf("[%d ms] ... written %,d / %,d edges%n", (now - startTime), written, m);
          lastLogTime = now;
        }
      }
    }

    if (written < m) {
      System.err.printf("%nWarning: Target was %,d edges, but only %,d were generated.%n", m, written);
    }

    ensureConnectivitySparse(writer, edgeSet, n, random, startTime);
  }

  private static void generateEulerianGraph(BufferedWriter writer, int n, long m, boolean isDirected, Random random,
      long startTime)
      throws IOException {
    long lastLogTime = startTime;

    if (m < n - 1) {
      System.err.println("Warning: Eulerian graph requires at least n-1 edges");
      return;
    }

    Set<Long> writtenEdges = new HashSet<>();
    int[] degree = new int[n + 1];

    generateSpanningTreeLimited(writer, writtenEdges, n, random, false, n - 1);
    for (long key : writtenEdges) {
      int v = (int) (key >> 32);
      int w = (int) key;
      degree[v]++;
      degree[w]++;
    }
    long generated = writtenEdges.size();

    ArrayList<Integer> oddVertices = new ArrayList<>();
    for (int i = 1; i <= n; i++) {
      if (degree[i] % 2 == 1) {
        oddVertices.add(i);
      }
    }

    while (generated < m) {
      if (oddVertices.size() >= 2) {
        int v = oddVertices.remove(0);
        int w = oddVertices.remove(0);

        long key = edgeKey(v, w, false);
        if (!writtenEdges.contains(key)) {
          writer.write(v + " " + w);
          writer.newLine();
          writtenEdges.add(key);
          degree[v]++;
          degree[w]++;
          generated++;

          if (degree[v] % 2 == 1)
            oddVertices.add(v);
          if (degree[w] % 2 == 1)
            oddVertices.add(w);
        }
      } else {
        int v = random.nextInt(n) + 1;
        int w = random.nextInt(n) + 1;
        if (v == w)
          continue;

        long key = edgeKey(v, w, false);
        if (writtenEdges.contains(key))
          continue;

        writer.write(v + " " + w);
        writer.newLine();
        writtenEdges.add(key);
        degree[v]++;
        degree[w]++;
        generated++;

        if (degree[v] % 2 == 1)
          oddVertices.add(v);
        if (degree[w] % 2 == 1)
          oddVertices.add(w);
      }

      if ((generated & 0xFFFF) == 0) {
        long now = System.currentTimeMillis();
        if (now - lastLogTime > LOGGING_INTERVAL) {
          System.out.printf("[%d ms] ... written %,d / %,d edges%n", (now - startTime), generated, m);
          lastLogTime = now;
        }
      }
    }

    if (generated >= m) {
      while (oddVertices.size() >= 2) {
        int v = oddVertices.remove(0);
        int w = oddVertices.remove(0);
        long key = edgeKey(v, w, false);
        if (!writtenEdges.contains(key)) {
          writer.write(v + " " + w);
          writer.newLine();
          writtenEdges.add(key);
          degree[v]++;
          degree[w]++;
        }
      }
    }

    ensureConnectivitySparse(writer, writtenEdges, n, random, startTime);
  }

  private static void generateSemiEulerianGraph(BufferedWriter writer, int n, long m, Random random, long startTime)
      throws IOException {
    long lastLogTime = startTime;

    if (m < n - 1) {
      System.err.println("Warning: Semi-Eulerian graph requires at least n-1 edges");
      return;
    }

    Set<Long> writtenEdges = new HashSet<>();
    int[] degree = new int[n + 1];

    int startVertex = random.nextInt(n) + 1;
    int endVertex;
    do {
      endVertex = random.nextInt(n) + 1;
    } while (endVertex == startVertex);

    ArrayList<Integer> vertices = new ArrayList<>();
    for (int i = 1; i <= n; i++) {
      vertices.add(i);
    }
    Collections.shuffle(vertices, random);

    int current = startVertex;
    for (int i = 0; i < vertices.size(); i++) {
      int next = vertices.get(i);
      if (next == current || next == endVertex && current == endVertex)
        continue;

      long key = edgeKey(Math.min(current, next), Math.max(current, next), false);
      if (!writtenEdges.contains(key)) {
        writer.write(current + " " + next);
        writer.newLine();
        writtenEdges.add(key);
        degree[current]++;
        degree[next]++;
      }
      current = next;
    }

    long key = edgeKey(Math.min(current, endVertex), Math.max(current, endVertex), false);
    if (!writtenEdges.contains(key)) {
      writer.write(current + " " + endVertex);
      writer.newLine();
      writtenEdges.add(key);
      degree[current]++;
      degree[endVertex]++;
    }

    long generated = writtenEdges.size();

    ArrayList<Integer> oddVertices = new ArrayList<>();
    for (int i = 1; i <= n; i++) {
      if (degree[i] % 2 == 1) {
        oddVertices.add(i);
      }
    }

    while (generated < m) {
      int v = random.nextInt(n) + 1;
      int w = random.nextInt(n) + 1;
      if (v == w)
        continue;

      long edgeKey = edgeKey(v, w, false);
      if (writtenEdges.contains(edgeKey))
        continue;

      writer.write(v + " " + w);
      writer.newLine();
      writtenEdges.add(edgeKey);
      degree[v]++;
      degree[w]++;
      generated++;

      if (degree[v] % 2 == 1 && !oddVertices.contains(v))
        oddVertices.add(v);
      if (degree[w] % 2 == 1 && !oddVertices.contains(w))
        oddVertices.add(w);

      while (oddVertices.size() > 2) {
        int ov = oddVertices.remove(0);
        int ow = oddVertices.remove(0);
        long oddKey = edgeKey(ov, ow, false);
        if (!writtenEdges.contains(oddKey)) {
          writer.write(ov + " " + ow);
          writer.newLine();
          writtenEdges.add(oddKey);
          degree[ov]++;
          degree[ow]++;
          generated++;
        }
      }

      if ((generated & 0xFFFF) == 0) {
        long now = System.currentTimeMillis();
        if (now - lastLogTime > LOGGING_INTERVAL) {
          System.out.printf("[%d ms] ... written %,d edges%n", (now - startTime), generated);
          lastLogTime = now;
        }
      }
    }

    ensureConnectivitySparse(writer, writtenEdges, n, random, startTime);
  }

  private static void ensureConnectivitySparse(BufferedWriter writer, Set<Long> existingEdges, int n,
      Random random, long startTime) throws IOException {
    if (existingEdges.isEmpty())
      return;

    boolean[] visited = new boolean[n + 1];
    ArrayDeque<Integer> queue = new ArrayDeque<>();

    int startVertex = -1;
    for (long key : existingEdges) {
      int v = (int) (key >>> 32);
      int w = (int) key;
      if (v >= 1 && v <= n && w >= 1 && w <= n) {
        startVertex = v;
        break;
      }
    }
    if (startVertex == -1)
      return;

    queue.add(startVertex);
    visited[startVertex] = true;
    int visitedCount = 1;

    while (!queue.isEmpty()) {
      int v = queue.poll();
      for (long key : existingEdges) {
        int ev = (int) (key >>> 32);
        int ew = (int) key;
        if (ev >= 1 && ev <= n && ew >= 1 && ew <= n) {
          if (ev == v && !visited[ew]) {
            visited[ew] = true;
            visitedCount++;
            queue.add(ew);
          } else if (ew == v && !visited[ev]) {
            visited[ev] = true;
            visitedCount++;
            queue.add(ev);
          }
        }
      }
    }

    if (visitedCount == n)
      return;

    ArrayList<Integer> component = new ArrayList<>();
    ArrayList<Integer> other = new ArrayList<>();
    for (int i = 1; i <= n; i++) {
      if (visited[i]) {
        component.add(i);
      } else {
        other.add(i);
      }
    }

    while (!other.isEmpty()) {
      int vIndex = random.nextInt(other.size());
      int v = other.remove(vIndex);
      int w = component.get(random.nextInt(component.size()));

      long key = edgeKey(Math.min(v, w), Math.max(v, w), false);
      if (!existingEdges.contains(key)) {
        writer.write(v + " " + w);
        writer.newLine();
        existingEdges.add(key);
      }

      visited[v] = true;
      component.add(v);
    }
  }

  private static long edgeKey(int v, int w, boolean directed) {
    if (directed) {
      return ((long) v << 32) | w;
    } else {
      int min = Math.min(v, w);
      int max = Math.max(v, w);
      return ((long) min << 32) | max;
    }
  }
}

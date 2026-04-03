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

public class GraphGenerator {
  private static final int CHUNK_SIZE = 64 * 1024;
  private static final int LOGGING_INTERVAL = 5_000;
  private static final double DENSE_THRESHOLD = 0.3;
  private static final int MAX_SAMPLING_ATTEMPTS = 10;

  public enum ConnectivityType {
    NONE,
    WEAKLY,
    EULERIAN,
    SEMI_EULERIAN
  }

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

    public Builder directed(boolean directed) {
      this.directed = directed;
      return this;
    }

    public Builder undirected() {
      this.directed = false;
      return this;
    }

    public Builder connectivity(ConnectivityType connectivity) {
      this.connectivity = connectivity;
      return this;
    }

    public Builder vertices(int vertices) {
      this.vertices = vertices;
      return this;
    }

    public Builder edges(long edges) {
      this.edges = edges;
      return this;
    }

    public Builder density(double density) {
      this.density = density;
      return this;
    }

    public Builder outputPath(String outputPath) {
      this.outputPath = outputPath;
      return this;
    }

    public Builder seed(long seed) {
      this.seed = seed;
      return this;
    }

    public Builder seed(Long seed) {
      this.seed = seed;
      return this;
    }

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

  public static Builder builder() {
    return new Builder();
  }

  public static void generateGraph(GraphConfig config) {
    int n = config.vertices();
    long m = config.resolvedEdges();
    double density = config.resolvedDensity();
    boolean isDirected = config.directed();
    ConnectivityType connectivity = config.connectivity();
    String outputPath = config.outputPath();

    Random random = config.seed() != null ? new Random(config.seed()) : new Random();

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

  private static void generateSparseGraph(BufferedWriter writer, int n, long m, boolean isDirected, ConnectivityType connectivity, Random random, long startTime)
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
      ensureConnectivity(writer, edgeSet, n, random, startTime);
    }
  }

  private static void generateSpanningTreeLimited(BufferedWriter writer, Set<Long> edgeSet, int n, Random random, boolean isDirected, int targetEdges)
      throws IOException {
    if (targetEdges <= 0) return;

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

  private static void generateDenseGraph(BufferedWriter writer, int n, long m, boolean isDirected, Random random, long startTime)
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
        if (w >= v) w++;
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

    ensureConnectivity(writer, edgeSet, n, random, startTime);
  }

  private static void generateEulerianGraph(BufferedWriter writer, int n, long m, boolean isDirected, Random random, long startTime)
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

          if (degree[v] % 2 == 1) oddVertices.add(v);
          if (degree[w] % 2 == 1) oddVertices.add(w);
        }
      } else {
        int v = random.nextInt(n) + 1;
        int w = random.nextInt(n) + 1;
        if (v == w) continue;

        long key = edgeKey(v, w, false);
        if (writtenEdges.contains(key)) continue;

        writer.write(v + " " + w);
        writer.newLine();
        writtenEdges.add(key);
        degree[v]++;
        degree[w]++;
        generated++;

        if (degree[v] % 2 == 1) oddVertices.add(v);
        if (degree[w] % 2 == 1) oddVertices.add(w);
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

    ensureConnectivity(writer, writtenEdges, n, random, startTime);
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
      if (next == current || next == endVertex && current == endVertex) continue;

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
      if (v == w) continue;

      long edgeKey = edgeKey(v, w, false);
      if (writtenEdges.contains(edgeKey)) continue;

      writer.write(v + " " + w);
      writer.newLine();
      writtenEdges.add(edgeKey);
      degree[v]++;
      degree[w]++;
      generated++;

      if (degree[v] % 2 == 1 && !oddVertices.contains(v)) oddVertices.add(v);
      if (degree[w] % 2 == 1 && !oddVertices.contains(w)) oddVertices.add(w);

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

    ensureConnectivity(writer, writtenEdges, n, random, startTime);
  }

  private static int[] generateDegreeSequence(int n, int targetDegreeSum, Random random, boolean semiEulerian, int startVertex, int endVertex) {
    int[] degree = new int[n + 1];

    if (semiEulerian) {
      degree[startVertex] = 1 + random.nextInt(3) * 2;
      degree[endVertex] = 1 + random.nextInt(3) * 2;

      int remainingSum = targetDegreeSum - degree[startVertex] - degree[endVertex];
      int perVertex = remainingSum / (n - 2);
      int remainder = remainingSum % (n - 2);

      for (int i = 1; i <= n; i++) {
        if (i == startVertex || i == endVertex) continue;
        degree[i] = Math.max(2, perVertex);
        if (remainder > 0) {
          degree[i] += 2;
          remainder -= 2;
        }
        degree[i] = degree[i] / 2 * 2;
      }
    } else {
      int perVertex = targetDegreeSum / n;
      int remainder = targetDegreeSum % n;

      for (int i = 1; i <= n; i++) {
        degree[i] = Math.max(2, perVertex);
        if (i <= remainder) {
          degree[i] += 2;
        }
        degree[i] = degree[i] / 2 * 2;
      }
    }

    int actualSum = 0;
    for (int i = 1; i <= n; i++) {
      actualSum += degree[i];
    }

    while (actualSum % 4 != 0) {
      degree[random.nextInt(n) + 1] += 2;
      actualSum += 2;
    }

    return degree;
  }

  private static int[] createStubs(int[] degree, int n) {
    int totalStubs = 0;
    for (int i = 1; i <= n; i++) {
      totalStubs += degree[i];
    }

    int[] stubs = new int[totalStubs];
    int idx = 0;
    for (int i = 1; i <= n; i++) {
      for (int j = 0; j < degree[i]; j++) {
        stubs[idx++] = i;
      }
    }
    return stubs;
  }

  private static void shuffleArray(int[] array, Random random) {
    for (int i = array.length - 1; i > 0; i--) {
      int j = random.nextInt(i + 1);
      int temp = array[i];
      array[i] = array[j];
      array[j] = temp;
    }
  }

  private static void ensureConnectivity(BufferedWriter writer, Set<Long> existingEdges, int n,
      Random random, long startTime) throws IOException {
    boolean[][] adj = new boolean[n + 1][n + 1];
    for (long key : existingEdges) {
      int v = (int) (key >> 32);
      int w = (int) key;
      if (v >= 1 && v <= n && w >= 1 && w <= n) {
        adj[v][w] = true;
        adj[w][v] = true;
      }
    }

    int startVertex = -1;
    for (int i = 1; i <= n; i++) {
      for (int j = 1; j <= n; j++) {
        if (adj[i][j]) {
          startVertex = i;
          break;
        }
      }
      if (startVertex != -1) break;
    }

    if (startVertex == -1) return;

    boolean[] visited = new boolean[n + 1];
    ArrayDeque<Integer> queue = new ArrayDeque<>();
    queue.add(startVertex);
    visited[startVertex] = true;

    int visitedCount = 1;
    while (!queue.isEmpty()) {
      int v = queue.poll();
      for (int w = 1; w <= n; w++) {
        if (adj[v][w] && !visited[w]) {
          visited[w] = true;
          visitedCount++;
          queue.add(w);
        }
      }
    }

    if (visitedCount == n) return;

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
        adj[v][w] = true;
        adj[w][v] = true;
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
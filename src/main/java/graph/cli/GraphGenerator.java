package graph.cli;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * A utility class for generating random simple graphs.
 * 
 * <p>
 * Supports directed and undirected graphs with various connectivity options:
 * disconnected, weakly connected, Eulerian, and semi-Eulerian.
 * Uses the geometric jump algorithm for uniform edge distribution.
 * 
 * <p>
 * Example usage:
 * 
 * <pre>{@code
 * GraphGenerator.GraphConfig config = GraphGenerator.builder()
 *     .vertices(1000)
 *     .density(0.5)
 *     .directed(true)
 *     .connectivity(GraphGenerator.ConnectivityType.WEAKLY)
 *     .outputPath("graph.txt")
 *     .build();
 * GraphGenerator.generate(config);
 * }</pre>
 */
public class GraphGenerator {
  private static final int CHUNK_SIZE = 64 * 1_024;
  private static final int LOGGING_INTERVAL = 5_000;

  /**
   * Specifies the connectivity type of the graph to be generated.
   * 
   * <ul>
   * <li>{@code NONE} - The graph may be disconnected</li>
   * <li>{@code WEAKLY} - The graph is weakly connected (default)</li>
   * <li>{@code EULERIAN} - All vertices have even degree (undirected only)</li>
   * <li>{@code SEMI_EULERIAN} - Exactly two vertices have odd degree (undirected
   * only)</li>
   * </ul>
   */
  public enum ConnectivityType {
    /** The graph may be disconnected. */
    NONE,

    /** The graph is weakly connected (default). */
    WEAKLY,

    /** The graph has all vertices with even degree (Eulerian cicle exists). */
    EULERIAN,

    /**
     * The graph has exactly two vertices with odd degree (Eulerian path exists).
     */
    SEMI_EULERIAN
  }

  /**
   * Immutable configuration object holding all parameters for graph generation.
   * 
   * @param vertices     the number of vertices in the graph
   * @param directed     whether the graph is directed
   * @param connectivity the connectivity type (none, weakly, Eulerian,
   *                     semi-Eulerian)
   * @param edges        the exact number of edges (-1 if using density)
   * @param density      the edge density as a decimal 0.0 to 1.0 (-1 if using
   *                     edges)
   * @param outputPath   the path to write the graph file
   */
  public record GraphConfig(
      int vertices,
      boolean directed,
      ConnectivityType connectivity,
      long edges,
      double density,
      String outputPath) {

    /**
     * Calculates the maximum possible number of edges for this graph configuration.
     * 
     * <ul>
     * <li>For directed graphs: n * (n - 1)</li>
     * <li>For undirected graphs: n * (n - 1) / 2</li>
     * </ul>
     * 
     * @return the maximum number of edges possible
     */
    public long maxEdges() {
      return directed ? vertices * (vertices - 1) : vertices * (vertices - 1) / 2;
    }

    /**
     * Resolves the actual number of edges to generate.
     * 
     * <p>
     * If edges is specified (>= 0), returns that value.
     * Otherwise, calculates edges from density: maxEdges * density.
     * 
     * @return the number of edges to generate
     * @throws IllegalStateException if neither edges nor density is specified
     */
    public long resolvedEdges() {
      if (edges >= 0) {
        return edges;
      }

      if (density >= 0) {
        return Math.round(maxEdges() * density);
      }

      throw new IllegalStateException("Neither edges nor density specified");
    }
  }

  /**
   * Builder for creating GraphConfig instances with a fluent API.
   * 
   * <p>
   * Example usage:
   * 
   * <pre>{@code
   * GraphGenerator.GraphConfig config = GraphGenerator.builder()
   *     .vertices(1000)
   *     .edges(500)
   *     .directed(false)
   *     .connectivity(ConnectivityType.EULERIAN)
   *     .outputPath("output.log")
   *     .build();
   * }</pre>
   */
  public static class Builder {
    private int vertices = -1;
    private boolean directed = true;
    private ConnectivityType connectivity = ConnectivityType.WEAKLY;
    private long edges = -1;
    private double density = -1;
    private String outputPath = null;

    /**
     * Sets whether the graph should be directed.
     * 
     * @param directed true for directed graph, false for undirected
     * @return this builder for chaining
     */
    public Builder directed(boolean directed) {
      this.directed = directed;
      return this;
    }

    /**
     * Sets the graph as undirected (shorthand for directed(false)).
     * 
     * @return this builder for chaining
     */
    public Builder undirected() {
      this.directed = false;
      return this;
    }

    /**
     * Sets the connectivity type for the graph.
     * 
     * @param connectivity the ConnectivityType (NONE, WEAKLY, EULERIAN,
     *                     SEMI_EULERIAN)
     * @return this builder for chaining
     */
    public Builder connectivity(ConnectivityType connectivity) {
      this.connectivity = connectivity;
      return this;
    }

    /**
     * Sets the number of vertices in the graph.
     * 
     * @param vertices the number of vertices (must be > 0)
     * @return this builder for chaining
     */
    public Builder vertices(int vertices) {
      this.vertices = vertices;
      return this;
    }

    /**
     * Sets the exact number of edges to generate.
     * 
     * <p>
     * Cannot be used together with density().
     * 
     * @param edges the number of edges (must be between 0 and maxEdges)
     * @return this builder for chaining
     */
    public Builder edges(long edges) {
      this.edges = edges;
      return this;
    }

    /**
     * Sets the edge density as a decimal between 0.0 and 1.0.
     * 
     * <p>
     * Cannot be used together with edges().
     * 
     * @param density the edge density (0.0 to 1.0)
     * @return this builder for chaining
     */
    public Builder density(double density) {
      this.density = density;
      return this;
    }

    /**
     * Sets the output file path for the generated graph.
     * 
     * @param outputPath the path to write the graph file
     * @return this builder for chaining
     */
    public Builder outputPath(String outputPath) {
      this.outputPath = outputPath;
      return this;
    }

    /**
     * Builds the GraphConfig instance with the current builder settings.
     * 
     * @return the constructed GraphConfig
     * @throws IllegalStateException if required parameters are missing or invalid
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
      if (density < 0
          && (edges < 0 || edges > (directed ? vertices * (vertices - 1) : vertices * (vertices - 1) / 2))) {
        throw new IllegalStateException("Edges must be between 0 and maxEdges");
      }
      if (density >= 0 && (density < 0.0 || density > 1.0)) {
        throw new IllegalStateException("Density must be between 0.0 and 1.0");
      }

      // Compute max edges and resolve edges from density if needed for validation
      long maxEdges = directed ? (long) vertices * (vertices - 1) : (long) vertices * (vertices - 1) / 2;
      long resolvedEdges = (edges >= 0) ? edges : Math.round(maxEdges * density);

      if (connectivity == ConnectivityType.EULERIAN && !directed && resolvedEdges < vertices) {
        throw new IllegalArgumentException(
            "Eulerian graph requires at least " + vertices + " edges (one per vertex for cycle)");
      }
      if (connectivity == ConnectivityType.SEMI_EULERIAN && !directed && resolvedEdges < vertices - 1) {
        throw new IllegalArgumentException(
            "Semi-Eulerian graph requires at least " + (vertices - 1) + " edges (n-1 for path)");
      }

      return new GraphConfig(vertices, directed, connectivity, edges, density, outputPath);
    }
  }

  /**
   * Creates a new Builder instance for constructing GraphConfig.
   * 
   * @return a new Builder instance
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Generates a graph based on the provided configuration.
   * 
   * <ul>
   * <li>NONE/WEAKLY: No degree constraints</li>
   * <li>EULERIAN: All vertices have even degree</li>
   * <li>SEMI_EULERIAN: Exactly two vertices have odd degree</li>
   * </ul>
   * 
   * @param config the GraphConfig containing all generation parameters
   * @throws IllegalArgumentException if the configuration is invalid
   */
  public static void generateGraph(GraphConfig config) {
    int n = config.vertices();
    long m = config.resolvedEdges();
    long maxEdges = config.maxEdges();
    boolean isDirected = config.directed();
    ConnectivityType connectivity = config.connectivity();
    String outputPath = config.outputPath();

    long startTime = System.currentTimeMillis();
    long lastLogTime = startTime;
    long generated = 0;

    System.out.printf("Starting %s graph generation for %,d edges...%n",
        connectivity == ConnectivityType.NONE ? "simple" : connectivity.toString().toLowerCase(), m);

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath), CHUNK_SIZE)) {
      // Write the header
      writer.write(Integer.toString(n));
      writer.write(' ');
      writer.write(Long.toString(m));
      writer.newLine();

      int[] degree = new int[n + 1]; // Degree array tracks vertex degrees
      Random random = new Random(); // Random for edge selection

      int[] specialVertices = initGraph(writer, degree, n, connectivity, random,
          isDirected);
      generated = countEdges(degree, n);

      long index = 0; // Linear index for edge enumeration
      while (generated < m && index < maxEdges) {
        index += geometricJump(random, m - generated, maxEdges - index);

        if (index >= maxEdges) {
          break;
        }

        int v, w;
        if (isDirected) {
          // Directed: map index to (source, target) using offset formula
          v = (int) ((index / (n - 1)) + 1);
          w = (int) (index % (n - 1)) + 1;
          if (w >= v) {
            w++;
          }

        } else {
          // Undirected: map index to {v,w} where v < w using triangular matrix indexing
          int[] edge = findEdgeUndirected(index, n);
          v = edge[0];
          w = edge[1];
        }

        boolean accept = shouldAcceptEdge(v, w, degree, connectivity, specialVertices);

        if (accept) {
          // Write edge in specified direction
          writer.write(Integer.toString(v));
          writer.write(' ');
          writer.write(Integer.toString(w));
          writer.newLine();
          degree[v]++;

          if (!isDirected) {
            // For undirected: write reverse edge and track degree
            writer.write(Integer.toString(w));
            writer.write(' ');
            writer.write(Integer.toString(v));
            writer.newLine();
            degree[w]++;
          }

          generated += isDirected ? 1 : 2;
        } else {
          // Reject: move to next potential edge index
          index++;
        }

        if ((generated & 0xFFFFF) == 0) {
          long now = System.currentTimeMillis();
          if (now - lastLogTime > LOGGING_INTERVAL) {
            System.out.printf("[%d ms] ... written %,d / %,d edges%n", (now - startTime), generated, m);
            lastLogTime = now;
          }
        }
      }

      if (generated < m) {
        System.err.printf("%nWarning: Target was %,d edges, but only %,d were generated.%n", m, generated);
      } else {
        System.out.printf("[%d ms] Graph successfully generated! Available at: ./%s%n",
            (System.currentTimeMillis() - startTime), outputPath);
      }

    } catch (IOException e) {
      System.err.println("An error occurred while writing to the file: " + e.getMessage());
    }
  }

  private static int[] initGraph(BufferedWriter writer, int[] degree, int n,
      ConnectivityType connectivity, Random random, boolean isDirected) throws IOException {
    if (connectivity == ConnectivityType.EULERIAN) {
      initEulerianCycle(writer, degree, n, random);
      return null;

    } else if (connectivity == ConnectivityType.SEMI_EULERIAN) {
      return initEulerianPath(writer, degree, n, random);
    }

    return null;
  }

  private static void initEulerianCycle(BufferedWriter writer, int[] degree,
      int n, Random random) throws IOException {
    int[] vertices = createShuffledVertices(n, random);

    for (int i = 0; i < n; i++) {
      int v = vertices[i];
      int w = vertices[(i + 1) % n];

      writer.write(Integer.toString(v));
      writer.write(' ');
      writer.write(Integer.toString(w));
      writer.newLine();
      writer.write(Integer.toString(w));
      writer.write(' ');
      writer.write(Integer.toString(v));
      writer.newLine();

      degree[v]++;
      degree[w]++;
    }
  }

  private static int[] initEulerianPath(BufferedWriter writer, int[] degree, int n, Random random) throws IOException {
    // Gets two diffrent vertices
    int startVertex = random.nextInt(n) + 1;
    int endVertex;
    do {
      endVertex = random.nextInt(n) + 1;
    } while (endVertex == startVertex);

    int[] vertices = createShuffledVertices(n, random);

    Set<Integer> visited = new HashSet<>();
    visited.add(startVertex);

    int current = startVertex;
    int visitedCount = 1;

    while (current != endVertex && visitedCount < n) {
      int next;
      if (visited.size() == n - 1) {
        next = endVertex;
      } else {
        do {
          next = vertices[random.nextInt(n)];
        } while (next == current || (next == endVertex && current != endVertex));
      }

      writer.write(Integer.toString(current));
      writer.write(' ');
      writer.write(Integer.toString(next));
      writer.newLine();
      writer.write(Integer.toString(next));
      writer.write(' ');
      writer.write(Integer.toString(current));
      writer.newLine();

      degree[current]++;
      degree[next]++;

      visited.add(next);
      visitedCount++;
      current = next;
    }

    if (current != endVertex) {
      writer.write(Integer.toString(current));
      writer.write(' ');
      writer.write(Integer.toString(endVertex));
      writer.newLine();
      writer.write(Integer.toString(endVertex));
      writer.write(' ');
      writer.write(Integer.toString(current));
      writer.newLine();

      degree[current]++;
      degree[endVertex]++;
    }

    return new int[] { startVertex, endVertex };
  }

  private static int[] createShuffledVertices(int n, Random random) {
    // Initialize vertices 1 to n
    int[] vertices = new int[n];
    for (int i = 0; i < n; i++) {
      vertices[i] = i + 1;
    }

    // Fisher-Yates shuffle for random ordering
    for (int i = vertices.length - 1; i > 0; i--) {
      int j = random.nextInt(i + 1);
      int temp = vertices[i];
      vertices[i] = vertices[j];
      vertices[j] = temp;
    }

    return vertices;
  }

  private static long geometricJump(Random random, long remainingEdges, long remainingSlots) {
    // Skip edges with probability (1 - p) using inverse CDF of geometric
    // distribution
    double p = (double) remainingEdges / remainingSlots;
    if (p >= 1.0) {
      return 0;
    }

    return (long) (Math.log(1.0 - random.nextDouble()) / Math.log(1.0 - p));
  }

  private static boolean shouldAcceptEdge(int v, int w, int[] degree,
      ConnectivityType connectivity, int[] specialVertices) {
    // Simple graphs: accept all edges
    if (connectivity == ConnectivityType.NONE || connectivity == ConnectivityType.WEAKLY) {
      return true;
    }

    // Eulerian: accept only if degrees are both even or both odd
    if (connectivity == ConnectivityType.EULERIAN) {
      int degV = degree[v];
      int degW = degree[w];
      return (degV % 2 == 0 && degW % 2 == 0) || (degV % 2 == 1 && degW % 2 == 1);
    }

    // Semi-Eulerian: always accept start/end vertices, check parity for others
    if (connectivity == ConnectivityType.SEMI_EULERIAN) {
      boolean isSpecialV = (specialVertices != null && (v == specialVertices[0] || v == specialVertices[1]));
      boolean isSpecialW = (specialVertices != null && (w == specialVertices[0] || w == specialVertices[1]));

      if (isSpecialV || isSpecialW) {
        return true;
      }

      int degV = degree[v];
      int degW = degree[w];
      return (degV % 2 == 0 && degW % 2 == 0) || (degV % 2 == 1 && degW % 2 == 1);
    }

    return true;
  }

  /**
   * Uses the handshake lemma to calculate the number of edges.
   * 
   * @param degree an array representing the degree of each vertex
   * @param n      the number of vertices in the graph
   */
  private static long countEdges(int[] degree, int n) {
    long count = 0;

    for (int i = 1; i <= n; i++) {
      count += degree[i];
    }

    return count / 2;
  }

  /**
   * Converts a linear index to an undirected edge using triangular matrix
   * indexing.
   * <p>
   * Maps index in range [0, n*(n-1)/2 - 1] to edge (v, w) where 1 <= v < w <= n.
   * Each row v contains (n-v) edges: (v,v+1), (v,v+2), ..., (v,n).
   * 
   * @param index the linear edge index (0-based)
   * @param n     the number of vertices in the graph
   * @return an int array [v, w] representing the edge vertices where v < w
   */
  private static int[] findEdgeUndirected(long index, int n) {
    // Clamp index to valid range
    long maxIndex = n * (n - 1) / 2 - 1;
    if (index > maxIndex) {
      index = maxIndex;
    }

    // Solve v from: index = v*(v-1)/2 + (w-v-1) -> v = ceil((sqrt(8*index+1)-1)/2)
    double dv = (1 + Math.sqrt(1 + 8.0 * index)) / 2;
    int v = (int) dv;

    if (v < 1) {
      v = 1;
    }
    if (v >= n) {
      v = n - 1;
    }

    // Calculate w: edges before row v is v*(v-1)/2, remaining is index - that
    long pairsBeforeRow = v * (v - 1) / 2;
    int w = (int) (index - pairsBeforeRow + v + 1);

    if (w > n) {
      w = n;
    }

    return new int[] { v, w };
  }
}

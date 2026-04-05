package graph.api;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import graph.representations.GraphBuilder;

public abstract class Graph implements GraphBase {

  /** If the graph has directed edges. */
  public final boolean isDirected;

  /** Total number of vertices in the graph. */
  protected int n;

  /** Total number of edges in the graph. */
  protected int m;

  /**
   * Constructor called by the concrete implementations.
   */
  protected Graph(boolean isDirected, int n, int m) {
    this.isDirected = isDirected;
    this.n = n;
    this.m = m;
  }

  @Override
  public long getVerticesCount() {
    return n;
  }

  @Override
  public long getEdgesCount() {
    return m;
  }

  /**
   * Creates a new graph with all the edges reversed.
   * 
   * @return The {@link Graph} with the reversed edges.
   */
  protected DirectedGraph getReversed(GraphBuilder builder) {
    builder.initialize(n, m);

    IteratorVisitor iterator = new IteratorVisitor() {
      @Override
      public void examineEdge(int source, int target) {
        builder.addEdge(target, source);
      }
    };

    iterateGraph(iterator);
    return (DirectedGraph) builder.build();
  }

  /**
   * Creates an induced subgraph containing only the specified vertices.
   *
   * @param vertices The array of vertex IDs to include in the subgraph.
   * @param builder  The graph builder used to construct the subgraph.
   * @return The induced {@link Graph} containing only the specified vertices.
   */
  protected Graph getInducedSubgraph(int[] vertices, GraphBuilder builder) {
    int maxVertex = Arrays.stream(vertices).max().orElse(0);

    builder.initialize(maxVertex, m, vertices);

    Set<Integer> uniqueVertices = Arrays.stream(vertices).boxed().collect(Collectors.toSet());

    IteratorVisitor iterator = new IteratorVisitor() {
      @Override
      public void examineEdge(int v, int w) {
        if (uniqueVertices.contains(v) && uniqueVertices.contains(w)) {
          builder.addEdge(v, w);
        }
      }
    };

    iterateGraph(iterator);
    return builder.build();
  }

  @Override
  public int[][] getEdgesSet() {
    int[][] edges = new int[n][2];
    final int[] index = { 0 };

    IteratorVisitor iterator = new IteratorVisitor() {
      @Override
      public void examineEdge(int v, int w) {
        edges[index[0]][0] = v;
        edges[index[0]][1] = w;
        index[0]++;
      }
    };

    iterateGraph(iterator);

    return java.util.Arrays.copyOf(edges, index[0]);
  }
}

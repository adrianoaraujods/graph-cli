package graph.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import graph.representations.GraphBuilder;

public abstract class Graph implements GraphBase {

  /** If the graph has directed edges. */
  public final boolean isDirected;

  /** If the graph has weighted edges. */
  protected final boolean isWeighted;

  /** Total number of vertices in the graph. */
  protected int n;

  /** Total number of edges in the graph. */
  protected long m;

  /**
   * Constructor called by the concrete implementations.
   */
  protected Graph(boolean isDirected, int n, long m, boolean isWeighted) {
    this.isDirected = isDirected;
    this.n = n;
    this.m = m;
    this.isWeighted = isWeighted;
  }

  /**
   * Returns whether this graph has weighted edges.
   *
   * @return true if the graph is weighted, false otherwise.
   */
  public boolean isWeighted() {
    return isWeighted;
  }

  /**
   * Returns the total number of vertices in the graph.
   *
   * @return The number of vertices (n).
   */
  @Override
  public abstract Graph clone();

  @Override
  public int getVerticesCount() {
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

    builder.initialize(maxVertex, m);

    // Track all specified vertices (including isolated ones)
    for (int v : vertices) {
      builder.addVertex(v);
    }

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
  public long[] getEdgesSet() {
    List<Long> set = new ArrayList<>(n);

    IteratorVisitor iterator = new IteratorVisitor() {
      @Override
      public void examineEdge(int v, int w) {
        set.add(Edges.directed(v, w));
      }
    };

    iterateGraph(iterator);

    return set.stream().mapToLong(e -> e).toArray();
  }
}

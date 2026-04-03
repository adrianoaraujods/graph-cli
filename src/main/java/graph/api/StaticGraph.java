package graph.api;

import graph.representations.GraphBuilder;

/**
 * Abstract base class defining the read-only algorithms for a directed graph.
 */
public abstract class StaticGraph implements Graph {

  /** If the graph has directed edges. */
  public final boolean isDirected;

  /** Total number of vertices in the graph. */
  public final int n;

  /** Total number of edges in the graph. */
  public final int m;

  /**
   * Constructor called by the concrete implementations.
   */
  protected StaticGraph(boolean isDirected, int n, int m) {
    this.isDirected = isDirected;
    this.n = n;
    this.m = m;
  }

  /**
   * Creates a new graph with all the edges reversed.
   * 
   * @return The {@link StaticGraph} with the reversed edges.
   */
  protected StaticGraph getReversed(GraphBuilder builder) {
    builder.initialize(n, m);

    IteratorVisitor iterator = new IteratorVisitor() {
      @Override
      public void examineEdge(int source, int target) {
        builder.addEdge(target, source);
      }
    };

    iterateGraph(iterator);
    return builder.build();
  }
}

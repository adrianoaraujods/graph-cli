package src.api;

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
}

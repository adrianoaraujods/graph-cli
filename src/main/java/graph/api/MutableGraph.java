package graph.api;

public abstract class MutableGraph extends StaticGraph {

  /** Total number of vertices in the graph. */
  public int n;

  /** Total number of edges in the graph. */
  public int m;

  protected MutableGraph(boolean isDirected, int n, int m) {
    super(isDirected, n, m);
    this.n = n;
    this.m = m;
  }

  public abstract void removeEdge(int source, int target);

  public abstract void addEdge(int source, int target);
}

package graph.api;

public interface GraphBase {

  /**
   * Returns the total number of vertices in the graph.
   *
   * @return The number of vertices (n).
   */
  public abstract int getVerticesCount();

  /**
   * Returns the total number of edges in the graph.
   *
   * @return The number of edges (m).
   */
  public abstract long getEdgesCount();

  /**
   * Returns all edges in the graph as a 2D array.
   *
   * @return A 2D array where each row is an edge {v, w}.
   */
  public abstract long[] getEdgesSet();

  /**
   * Removes an edge from the graph.
   *
   * @param v The source vertex of the edge.
   * @param w The target vertex of the edge.
   */
  public abstract void removeEdge(int v, int w);

  /**
   * Adds an edge to the graph.
   *
   * @param v The source vertex of the edge.
   * @param w The target vertex of the edge.
   */
  public abstract void addEdge(int v, int w);

  /**
   * Interface for visiting vertices and edges during graph traversal.
   */
  public interface IteratorVisitor {

    /**
     * Called when visiting a vertex.
     *
     * @param v The vertex being visited.
     */
    default void examineVertex(int v) {
    }

    /**
     * Called when visiting an edge.
     *
     * @param v The source vertex of the edge.
     * @param w The target vertex of the edge.
     */
    default void examineEdge(int v, int w) {
    }

    /**
     * Called when visiting a weighted edge.
     * Default implementation calls examineEdge(v, w) for backward compatibility.
     *
     * @param v      The source vertex of the edge.
     * @param w      The target vertex of the edge.
     * @param weight The edge weight.
     */
    default void examineEdge(int v, int w, int weightOrCapacity) {
      examineEdge(v, w);
    }

    /**
     * Called before visiting an edge or vertex.
     */
    default boolean shouldStop() {
      return false;
    }
  }

  /**
   * Iterates over all vertices and edges in the graph using the provided visitor.
   *
   * @param visitor The visitor to use for traversing the graph.
   */
  public abstract void iterateGraph(IteratorVisitor visitor);

  /**
   * Returns all vertices in the graph.
   * 
   * @return An array containing all valid vertex IDs.
   */
  public abstract int[] getVertices();

  /**
   * Returns all vertices in the graph, including isolated vertices (no edges).
   * Unlike {@link #getVertices()}, this returns every vertex present in the
   * graph.
   *
   * @return Array of all vertex IDs in the graph.
   */
  public abstract int[] getAllVertices();

  /**
   * Builds the induced subgraph based on the provided vertices.
   * 
   * @param vertices The vertices that are included in the subgraph.
   */
  public abstract Graph getInducedSubgraph(int[] vertices);
}

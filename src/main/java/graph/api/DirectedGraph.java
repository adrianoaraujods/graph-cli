package graph.api;

public interface DirectedGraph extends Graph {

  /**
   * Calculates the in-degree (number of incoming edges) of a given vertex.
   *
   * @param vertex The target vertex to analyze.
   * @return The number of edges pointing to the vertex.
   * @throws IllegalArgumentException if the vertex is less than 1 or greater than
   *                                  total number of vertices (n).
   */
  public abstract int getInDegree(int vertex);

  /**
   * Calculates the out-degree (number of outgoing edges) of a given vertex.
   *
   * @param vertex The target vertex to analyze.
   * @return The number of edges originating from the vertex.
   * @throws IllegalArgumentException if the vertex is less than 1 or greater than
   *                                  total number of vertices (n).
   */
  public abstract int getOutDegree(int vertex);

  /**
   * Retrieves all predecessors (vertices with an edge pointing to the given
   * vertex).
   *
   * @param vertex The target vertex to analyze.
   * @return An array of integers representing the predecessor vertices.
   * @throws IllegalArgumentException if the vertex is less than 1 or greater than
   *                                  total number of vertices (n).
   */
  public abstract int[] getPredecessors(int vertex);

  /**
   * Retrieves all successors (vertices that the given vertex points to).
   *
   * @param vertex The target vertex to analyze.
   * @return An array of integers representing the successor vertices.
   * @throws IllegalArgumentException if the vertex is less than 1 or greater than
   *                                  total number of vertices (n).
   */
  public abstract int[] getSuccessors(int vertex);

  public abstract StaticGraph getReversed();
}

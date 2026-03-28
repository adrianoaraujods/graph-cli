public interface UndirectedGraph extends Graph {

  /**
   * Calculates the degree (number of adjacent vertices) of a given vertex.
   *
   * @param vertex The target vertex to analyze.
   * @return The number of edges adjacent of the vertex.
   * @throws IllegalArgumentException if the vertex is less than 1 or greater than
   *                                  total number of vertices (n).
   */
  public abstract int getDegree(int vertex);

  /**
   * Retrieves all neighbors (vertices that the given vertex is adjacent to).
   *
   * @param vertex The target vertex to analyze.
   * @return An array of integers representing the neighbors vertices.
   * @throws IllegalArgumentException if the vertex is less than 1 or greater than
   *                                  total number of vertices (n).
   */
  public abstract int[] getNeighbors(int vertex);
}

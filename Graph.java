public interface Graph {

  /**
   * Retrieves all adjacent vertices that the given vertex points to.
   *
   * @param vertex The target vertex to analyze.
   * @return An array of integers representing the successor vertices.
   * @throws IllegalArgumentException if the vertex is less than 1 or greater than
   *                                  total number of vertices (n).
   */
  abstract int[] getAdjacency(int vertex);

  /**
   * Returns all vertices in the graph.
   * 
   * @return An array containing all vertex IDs from 1 to n.
   */
  abstract int[] getVertices();

  /**
   * Builds the induced subgraph based on the provided vertices.
   * 
   * @param vertices The vertices that are included in the subgraph.
   */
  abstract StaticGraph getInducedSubgraph(int[] vertices);

  interface DFSVisitor {
    default void examineRoot(int vertex) {
    }

    default void discoverVertex(int vertex) {
    }

    default void orderAdjacency(int[] adjacency) {
    }

    default void finishVertex(int vertex) {
    }

    default void examineEdge(int source, int target) {
    }

    default void finish(int[] discoverTimes, int[] finishTimes, int[] predecessors) {
    }

    default void treeEdge(int source, int target) {
    }

    default void backEdge(int source, int target) {
    }

    default void forwardEdge(int source, int target) {
    }

    default void crossEdge(int source, int target) {
    }
  }

  record DFSResult() {
  }

  /**
   * 
   * @param rootsOrder An array of vertex IDs that will be used to pick the roots
   *                   order. Uses lexicographical if null.
   * @param visitor
   * @throws IllegalArgumentException  if the rootsOrder length is greater than n.
   * @throws IndexOutOfBoundsException if any of the roots in the rootsOrder is
   *                                   outisde the possible vertex ID range.
   */
  abstract void depthFirstSearch(int[] rootsOrder, DFSVisitor visitor);
}

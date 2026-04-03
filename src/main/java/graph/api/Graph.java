package graph.api;

public interface Graph {

  public interface IteratorVisitor {
    default void examineVertex(int vertex) {
    }

    default void examineEdge(int source, int target) {
    }
  }

  public abstract void iterateGraph(IteratorVisitor visitor);

  /**
   * Returns all vertices in the graph.
   * 
   * @return An array containing all vertex IDs from 1 to n.
   */
  public abstract int[] getVertices();

  /**
   * Builds the induced subgraph based on the provided vertices.
   * 
   * @param vertices The vertices that are included in the subgraph.
   */
  public abstract StaticGraph getInducedSubgraph(int[] vertices);

  public default int[][] getEdgesSet(boolean isDirected, int n) {
    int[][] edges = new int[n][2];
    final int[] index = { 0 };

    IteratorVisitor iterator = new IteratorVisitor() {
      @Override
      public void examineEdge(int source, int target) {
        edges[index[0]][0] = source;
        edges[index[0]][1] = target;
        index[0]++;
      }
    };

    iterateGraph(iterator);

    return java.util.Arrays.copyOf(edges, index[0]);
  }

  public default int[][] getEdgesSet(boolean isDirected) {
    return getEdgesSet(isDirected, 10);
  }
}

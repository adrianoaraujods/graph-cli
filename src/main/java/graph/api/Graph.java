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

  public default EdgeSet getEdgesSet(boolean isDirected, int n) {
    EdgeSet set = new EdgeSet(isDirected, n);

    IteratorVisitor iterator = new IteratorVisitor() {
      @Override
      public void examineEdge(int source, int target) {
        set.add(source, target);
      }
    };

    iterateGraph(iterator);

    return set;
  }

  public default EdgeSet getEdgesSet(boolean isDirected) {
    return getEdgesSet(isDirected, 4);
  }
}

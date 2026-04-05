package graph.api;

public interface GraphBase {

  public abstract long getVerticesCount();

  public abstract long getEdgesCount();

  public abstract int[][] getEdgesSet();

  public abstract void removeEdge(int source, int target);

  public abstract void addEdge(int source, int target);

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
  public abstract Graph getInducedSubgraph(int[] vertices);
}

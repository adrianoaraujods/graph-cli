package graph.api;

/**
 * Interface for graphs that have capacity edges.
 */
public interface FlowGraph {

  /**
   * Returns the weight of the edge from v to w.
   *
   * @param v The source vertex.
   * @param w The target vertex.
   * @return The capacity of the edge.
   * @throws IllegalArgumentException if the edge doesn't exist.
   */
  int getEdgeCapacity(int v, int w);

  /**
   * Returns an array of all edge capacities.
   * The order corresponds to the edges returned by getEdgesSet().
   *
   * @return Array of edge capacities.
   */
  int[] getCapacitiesSet();

  /**
   * A record containing both edges and their capacities.
   *
   * @param edges    The packed edges as long[].
   * @param capacity The corresponding capacity as int[].
   */
  record CapacityEdges(long[] edges, int[] capacity) {
  }

  /**
   * Returns both the edges and their capacity in aligned arrays.
   *
   * @return A record containing both edges and capacities.
   */
  CapacityEdges getCapacitiesEdgesSet();
}

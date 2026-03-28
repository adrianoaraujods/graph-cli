/**
 * Interface defining the contract for constructing a Graph.
 */
public interface GraphBuilder {

  /**
   * Prepares the builder's internal temporary structures.
   *
   * @param n The total number of vertices.
   * @param m The total number of edges.
   */
  void initialize(int n, int m);

  /**
   * Prepares the builder's internal temporary structures with explicit vertices.
   *
   * @param n        The maximum vertex ID.
   * @param m        The total number of edges.
   * @param vertices The array of vertex IDs included in the graph.
   */
  void initialize(int n, int m, int[] vertices);

  /**
   * Registers a single directed edge into the builder's temporary state.
   *
   * @param source The origin vertex ID.
   * @param target The destination vertex ID.
   */
  void addEdge(int source, int target);

  /**
   * Compiles the temporary state into a final, immutable Graph object.
   *
   * @return The fully constructed Graph instance.
   */
  StaticGraph build();

  String getRepresentation();
}

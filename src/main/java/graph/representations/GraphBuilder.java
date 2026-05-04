package graph.representations;

import graph.api.Graph;

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
  void initialize(int n, long m);

  /**
   * Prepares the builder's internal temporary structures.
   *
   * @param n The total number of vertices.
   * @param m The total number of edges.
   * @param weighted If the graph should have weighted edges.
   */
  void initialize(int n, long m, boolean weighted);

  /**
   * Registers a single directed edge into the builder's temporary state.
   *
   * @param v The origin vertex ID.
   * @param w The destination vertex ID.
   */
  void addEdge(int v, int w);

  /**
   * Registers a single weighted edge into the builder's temporary state.
   *
   * @param v The origin vertex ID.
   * @param w The destination vertex ID.
   * @param weight The edge weight.
   */
  void addEdge(int v, int w, int weight);

  /**
   * Adds an isolated vertex (no edges) to the graph.
   * 
   * @param v Vertex ID to add.
   */
  void addVertex(int v);

  /**
   * Compiles the temporary state into a final, immutable Graph object.
   *
   * @return The fully constructed Graph instance.
   */
  Graph build();
}

package graph.representations.adjacencymatrix;

import java.security.InvalidParameterException;

import graph.api.Graph;
import graph.representations.GraphBuilder;

public class AdjacencyMatrixGraphBuilder implements GraphBuilder {

  /** If the graph has directed edges. */
  private final boolean isDirected;

  /** Total number of vertices in the graph. */
  private int n;

  /** Total number of edges in the graph. */
  private int m;

  // int[n columns][n rows]
  private boolean[][] matrix;
  private int[] vertices;

  public AdjacencyMatrixGraphBuilder(boolean isDirected) {
    this.isDirected = isDirected;
  }

  @Override
  public void initialize(int n, int m, int[] vertices) {
    this.n = n;
    this.m = 0;
    this.vertices = vertices;

    matrix = new boolean[n][n];
  }

  @Override
  public void addEdge(int v, int w) {
    if (v > n || w > n) {
      throw new InvalidParameterException();
    }

    // prevent increase m if edge already exists
    if (!matrix[w - 1][v - 1]) {
      matrix[w - 1][v - 1] = true;

      if (!isDirected) {
        matrix[v - 1][w - 1] = true;
      }

      m++;
    }
  }

  @Override
  public Graph build() {
    return new AdjacencyMatrixGraph(isDirected, n, m, matrix, vertices);
  }
}

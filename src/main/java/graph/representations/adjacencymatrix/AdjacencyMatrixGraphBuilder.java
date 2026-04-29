package graph.representations.adjacencymatrix;

import java.security.InvalidParameterException;
import java.util.HashSet;
import java.util.Set;

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
  private Set<Integer> isolatedVertices;

  public AdjacencyMatrixGraphBuilder(boolean isDirected) {
    this.isDirected = isDirected;
  }

  @Override
  public void initialize(int n, long m) {
    this.n = n;
    this.m = 0;
    matrix = new boolean[n][n];
    this.isolatedVertices = new HashSet<>(n);
  }

  @Override
  public void addEdge(int v, int w) {
    // Remove from isolated if present (now has an edge)
    isolatedVertices.remove(v);
    isolatedVertices.remove(w);

    if (v > n || w > n) {
      throw new InvalidParameterException();
    }

    // prevent increase m if edge already exists
    if (isDirected) {
      if (matrix[w - 1][v - 1]) {
        return;
      }
    } else {
      if (matrix[w - 1][v - 1] || matrix[v - 1][w - 1]) {
        return;
      }
    }

    matrix[w - 1][v - 1] = true;

    if (!isDirected) {
      matrix[v - 1][w - 1] = true;
    }

    m++;
  }

  @Override
  public void addVertex(int v) {
    isolatedVertices.add(v);
  }

  @Override
  public Graph build() {
    return new AdjacencyMatrixGraph(isDirected, n, m, matrix, isolatedVertices);
  }
}

package graph.representations.adjacencymatrix;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import graph.api.DirectedGraph;
import graph.api.Graph;
import graph.api.UndirectedGraph;

/**
 * Row: vertex successors
 * Column: vertex predecessors
 */
public class AdjacencyMatrixGraph extends Graph implements DirectedGraph, UndirectedGraph {

  // int[n columns][n rows]
  private boolean[][] matrix;
  private final Set<Integer> isolatedVertices;

  /**
   * Package-private constructor. Should only be called by the
   * {@link AdjacencyMatrixGraphBuilder}.
   */
  AdjacencyMatrixGraph(boolean isDirected, int n, int m, boolean[][] matrix, Set<Integer> isolatedVertices) {
    super(isDirected, n, m);
    this.matrix = matrix;
    this.isolatedVertices = isolatedVertices;
  }

  /**
   * Copy constructor - creates a deep copy for full independence.
   */
  private AdjacencyMatrixGraph(AdjacencyMatrixGraph other) {
    super(other.isDirected, other.n, other.m);
    this.matrix = new boolean[other.n][other.n];
    this.isolatedVertices = new HashSet<>(other.isolatedVertices);
    for (int i = 0; i < other.n; i++) {
      System.arraycopy(other.matrix[i], 0, this.matrix[i], 0, other.n);
    }
  }

  @Override
  public Graph clone() {
    return new AdjacencyMatrixGraph((AdjacencyMatrixGraph) this);
  }

  // Mutable Graph Methods

  @Override
  public void addEdge(int v, int w) {
    isolatedVertices.remove(v);
    isolatedVertices.remove(w);

    // increase matrix size
    if (v > n || w > n) {
      int newN = Math.max(v, w);

      boolean[][] newMatrix = new boolean[newN][newN];

      for (int i = 0; i < n; i++) {
        System.arraycopy(matrix[i], 0, newMatrix[i], 0, n);
      }

      matrix = newMatrix;
      n = newN;
    }

    if (matrix[w - 1][v - 1]) {
      return;
    }

    if (!isDirected) {
      if (v != w && matrix[v - 1][w - 1]) {
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
  public void removeEdge(int v, int w) {
    // prevent decrease m if edge doesn't exists
    if (isDirected) {
      if (!matrix[w - 1][v - 1]) {
        return;
      }
    } else {
      if (!matrix[w - 1][v - 1] && !matrix[v - 1][w - 1]) {
        return;
      }
    }

    matrix[w - 1][v - 1] = false;

    if (!isDirected) {
      matrix[v - 1][w - 1] = false;
    }

    // Check if v or w became isolated
    if (!hasEdges(v)) {
      isolatedVertices.add(v);
    }
    if (!hasEdges(w)) {
      isolatedVertices.add(w);
    }

    m--;
  }

  // Util methods

  /**
   * Checks if a vertex has any edges (incoming or outgoing).
   */
  private boolean hasEdges(int v) {
    for (int i = 0; i < n; i++) {
      if (matrix[v - 1][i] || matrix[i][v - 1]) {
        return true;
      }
    }

    return false;
  }

  @Override
  public void iterateGraph(IteratorVisitor visitor) {
    for (int v = 1; v <= n; v++) {
      if (visitor.shouldStop()) {
        return;
      }

      int[] successors = getSuccessors(v);

      if (successors.length == 0) {
        continue;
      }

      visitor.examineVertex(v);

      for (int w : successors) {
        if (visitor.shouldStop()) {
          return;
        }

        visitor.examineEdge(v, w);
      }
    }
  }

  @Override
  public Graph getInducedSubgraph(int[] vertices) {
    return getInducedSubgraph(vertices, new AdjacencyMatrixGraphBuilder(isDirected));
  }

  @Override
  public int[] getVertices() {
    Set<Integer> verticesWithEdges = new HashSet<>();

    // Check all matrix entries for edges
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < n; j++) {
        if (matrix[i][j]) {
          // i+1 and j+1 are vertices with edges
          verticesWithEdges.add(i + 1);
          verticesWithEdges.add(j + 1);
        }
      }
    }

    return verticesWithEdges.stream().mapToInt(Integer::intValue).sorted().toArray();
  }

  @Override
  public int[] getAllVertices() {
    Set<Integer> all = new HashSet<>(isolatedVertices);

    // Add vertices that have edges
    int[] verticesWithEdges = getVertices();
    for (int v : verticesWithEdges) {
      all.add(v);
    }

    return all.stream().mapToInt(Integer::intValue).sorted().toArray();
  }

  // Directed Methods

  @Override
  public int getInDegree(int v) {
    if (v < 1 || v > n) {
      throw new IllegalArgumentException();
    }

    int inDegree = 0;
    for (int i = 0; i < n; i++) {
      if (matrix[v - 1][i]) {
        inDegree++;
      }
    }

    return inDegree;
  }

  @Override
  public int getOutDegree(int v) {
    if (v < 1 || v > n) {
      throw new IllegalArgumentException();
    }

    int outDegree = 0;
    for (int i = 0; i < n; i++) {
      if (matrix[i][v - 1]) {
        outDegree++;
      }
    }

    return outDegree;
  }

  @Override
  public int[] getPredecessors(int v) {
    if (v < 1 || v > n) {
      throw new IllegalArgumentException();
    }

    int[] predecessors = new int[n];
    int head = 0;

    for (int i = 0; i < n; i++) {
      if (matrix[v - 1][i]) {
        predecessors[head++] = i + 1;
      }
    }

    return Arrays.copyOfRange(predecessors, 0, head);
  }

  @Override
  public int[] getSuccessors(int v) {
    if (v < 1 || v > n) {
      throw new IllegalArgumentException();
    }

    int[] successors = new int[n];
    int head = 0;

    for (int i = 0; i < n; i++) {
      if (matrix[i][v - 1]) {
        successors[head++] = i + 1;
      }
    }

    return Arrays.copyOfRange(successors, 0, head);
  }

  @Override
  public DirectedGraph getReversed() {
    return getReversed(new AdjacencyMatrixGraphBuilder(isDirected));
  }

  // Undirected Methods

  @Override
  public int getDegree(int v) {
    return getInDegree(v);
  }

  @Override
  public int[] getNeighbors(int v) {
    return getSuccessors(v);
  }
}

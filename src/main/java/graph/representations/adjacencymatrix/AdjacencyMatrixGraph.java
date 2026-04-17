package graph.representations.adjacencymatrix;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
  private Set<Integer> vertices;

  /**
   * Package-private constructor. Should only be called by the
   * {@link AdjacencyMatrixGraphBuilder}.
   */
  AdjacencyMatrixGraph(boolean isDirected, int n, int m, boolean[][] matrix, int[] vertices) {
    super(isDirected, n, m);
    this.matrix = matrix;

    if (vertices != null) {
      this.vertices = Arrays.stream(vertices).boxed().collect(Collectors.toSet());
    } else {
      this.vertices = null;
    }

  }

  /**
   * Package-private constructor. Should only be called by the
   * {@link AdjacencyMatrixGraphBuilder}.
   */
  AdjacencyMatrixGraph(boolean isDirected, int n, int m, boolean[][] matrix) {
    super(isDirected, n, m);
    this.matrix = matrix;
    this.vertices = null;
  }

  /**
   * Copy constructor - creates a deep copy for full independence.
   */
  private AdjacencyMatrixGraph(AdjacencyMatrixGraph other) {
    super(other.isDirected, other.n, other.m);

    this.matrix = new boolean[other.n][other.n];
    for (int i = 0; i < other.n; i++) {
      System.arraycopy(other.matrix[i], 0, this.matrix[i], 0, other.n);
    }

    this.vertices = other.vertices != null
        ? new java.util.HashSet<>(other.vertices)
        : null;
  }

  @Override
  public Graph clone(Graph graph) {
    return new AdjacencyMatrixGraph((AdjacencyMatrixGraph) graph);
  }

  @Override
  public int[] getVertices() {
    if (vertices == null) {
      return IntStream.rangeClosed(1, n).toArray();
    }

    return vertices.stream().mapToInt(Integer::intValue).toArray();
  }

  @Override
  public void iterateGraph(IteratorVisitor visitor) {
    if (vertices != null) {
      vertices.forEach(v -> {
        if (!visitor.shouldStop()) {
          return;
        }

        visitor.examineVertex(v);

        for (int i = 0; i < n; i++) {
          if (!visitor.shouldStop()) {
            return;
          }

          if (matrix[i][v - 1]) {
            // Skip reverse direction for undirected edges
            if (!isDirected && v > i + 1) {
              continue;
            }

            visitor.examineEdge(v, i + 1);
          }
        }
      });
    } else {
      for (int v = 0; v < n; v++) {
        if (!visitor.shouldStop()) {
          return;
        }

        visitor.examineVertex(v + 1);

        for (int i = 0; i < n; i++) {
          if (!visitor.shouldStop()) {
            return;
          }

          if (matrix[i][v]) {
            // Skip reverse direction for undirected edges
            if (!isDirected && (v + 1) > i + 1) {
              continue;
            }

            visitor.examineEdge(v + 1, i + 1);
          }
        }
      }
    }
  }

  @Override
  public Graph getInducedSubgraph(int[] vertices) {
    return getInducedSubgraph(vertices, new AdjacencyMatrixGraphBuilder(isDirected));
  }

  // Mutable Graph Methods

  @Override
  public void addEdge(int v, int w) {
    if (v > n || w > n) {
      int newN = Math.max(v, w);

      if (newN != n + 1) {
        vertices = Arrays.stream(getVertices()).boxed().collect(Collectors.toSet());

        if (!vertices.contains(v)) {
          vertices.add(v);
        }

        if (!vertices.contains(w)) {
          vertices.add(w);
        }
      }

      boolean[][] newMatrix = new boolean[newN][newN];

      for (int i = 0; i < n; i++) {
        System.arraycopy(matrix[i], 0, newMatrix[i], 0, n);
      }

      matrix = newMatrix;
      n = newN;
    }

    matrix[w - 1][v - 1] = true;
  }

  @Override
  public void removeEdge(int v, int w) {
    matrix[w - 1][v - 1] = false;
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

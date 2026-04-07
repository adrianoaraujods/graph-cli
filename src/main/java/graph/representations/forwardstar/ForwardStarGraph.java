package graph.representations.forwardstar;

import java.util.Arrays;
import java.util.stream.IntStream;

import graph.api.DirectedGraph;
import graph.api.Graph;
import graph.api.UndirectedGraph;
import graph.util.Sort;

/**
 * Concrete and static (immutable) implementation of the Graph using the Forward
 * Star structure.
 */
public class ForwardStarGraph extends Graph implements DirectedGraph, UndirectedGraph {
  private final int[] targets;
  private final int[] pointers;
  private final int[] vertices;

  /**
   * Package-private constructor. Should only be called by the
   * {@link ForwardStarGraphBuilder}.
   */
  ForwardStarGraph(boolean isDirected, int n, int m, int[] targets, int[] pointers, int[] vertices) {
    super(isDirected, n, m);

    this.targets = targets;
    this.pointers = pointers;
    this.vertices = vertices;
  }

  /**
   * Package-private constructor. Should only be called by the
   * {@link ForwardStarGraphBuilder}.
   */
  ForwardStarGraph(boolean isDirected, int n, int m, int[] targets, int[] pointers) {
    this(isDirected, n, m, targets, pointers, null);
  }

  @Override
  public void addEdge(int v, int w) {
    // TODO Auto-generated method stub
  }

  @Override
  public void removeEdge(int v, int w) {
    // TODO Auto-generated method stub
  }

  @Override
  public void iterateGraph(IteratorVisitor visitor) {
    for (int v = 0; v < pointers.length - 1; v++) {
      visitor.examineVertex(v + 1);

      int endIndex = pointers[v + 1];
      for (int w = pointers[v]; w < endIndex; w++) {
        visitor.examineEdge((v + 1), targets[w]);
      }
    }
  }

  @Override
  public int[] getVertices() {
    if (vertices == null || vertices.length == 0) {
      return IntStream.rangeClosed(1, n).toArray();
    }

    return vertices;
  }

  @Override
  public Graph getInducedSubgraph(int[] vertices) {
    return getInducedSubgraph(vertices, new ForwardStarGraphBuilder(isDirected));
  }

  // Directed Methods

  @Override
  public int getInDegree(int v) {
    if (v < 1 || v > n) {
      throw new IllegalArgumentException();
    }

    int degree = 0;
    for (int w : targets) {
      if (w == v) {
        degree++;
      }
    }

    return degree;
  }

  @Override
  public int getOutDegree(int v) {
    if (v < 1 || v > n) {
      throw new IllegalArgumentException();
    }

    return pointers[v] - pointers[v - 1];
  }

  @Override
  public int[] getPredecessors(int target) {
    if (target < 1 || target > n) {
      throw new IllegalArgumentException();
    }

    IntStream.Builder builder = IntStream.builder();

    Graph.IteratorVisitor iterator = new IteratorVisitor() {
      @Override
      public void examineEdge(int v, int w) {
        if (w == target) {
          builder.add(v);
        }
      }
    };

    iterateGraph(iterator);
    return builder.build().toArray();
  }

  @Override
  public int[] getSuccessors(int v) {
    if (v < 1 || v > n) {
      throw new IllegalArgumentException();
    }

    int[] successors = Arrays.copyOfRange(targets, pointers[v - 1], pointers[v]);
    Sort.quick(successors);

    return successors;
  }

  @Override
  public DirectedGraph getReversed() {
    return getReversed(new ForwardStarGraphBuilder(isDirected));
  }

  // Undirected Methods

  @Override
  public int getDegree(int v) {
    if (v < 1 || v > n) {
      throw new IllegalArgumentException();
    }

    return pointers[v] - pointers[v - 1];
  }

  @Override
  public int[] getNeighbors(int v) {
    return getSuccessors(v);
  }
}

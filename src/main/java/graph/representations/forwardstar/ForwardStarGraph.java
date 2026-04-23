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
  private int[] targets;
  private int[] pointers;
  private int[] vertices;

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

  /**
   * Copy constructor - creates a deep copy for full independence.
   */
  private ForwardStarGraph(ForwardStarGraph graph) {
    super(graph.isDirected, graph.n, graph.m);
    this.targets = Arrays.copyOf(graph.targets, graph.targets.length);
    this.pointers = Arrays.copyOf(graph.pointers, graph.pointers.length);
    this.vertices = graph.vertices != null
        ? Arrays.copyOf(graph.vertices, graph.vertices.length)
        : null;
  }

  @Override
  public Graph clone() {
    return new ForwardStarGraph((ForwardStarGraph) this);
  }

  @Override
  public void addEdge(int v, int w) {
    ForwardStarGraphBuilder builder = new ForwardStarGraphBuilder(isDirected);

    int newN = Math.max(n, Math.max(v, w));
    long newM = m + 1;
    long estimatedPairs = isDirected ? newM : newM * 2;

    builder.initialize(newN, estimatedPairs);
    builder.addEdge(v, w);

    IteratorVisitor iterator = new IteratorVisitor() {
      @Override
      public void examineEdge(int source, int target) {
        builder.addEdge(source, target);
      }
    };

    iterateGraph(iterator);

    ForwardStarGraph updated = builder.build();

    targets = updated.targets;
    pointers = updated.pointers;
    vertices = updated.vertices;
    n = updated.n;
    m = newM;
  }

  @Override
  public void removeEdge(int v, int w) {
    ForwardStarGraphBuilder builder = new ForwardStarGraphBuilder(isDirected);

    long estimatedPairs = isDirected ? m : m * 2;

    builder.initialize(n, estimatedPairs);

    IteratorVisitor iterator = new IteratorVisitor() {
      @Override
      public void examineEdge(int source, int target) {
        boolean shouldRemove = isDirected
            ? (source == v && target == w)
            : ((source == v && target == w) || (source == w && target == v));

        if (!shouldRemove) {
          builder.addEdge(source, target);
        }
      }
    };

    iterateGraph(iterator);

    ForwardStarGraph updated = builder.build();

    targets = updated.targets;
    pointers = updated.pointers;
    vertices = updated.vertices;
    n = updated.n;
    m = m - 1;
  }

  @Override
  public void iterateGraph(IteratorVisitor visitor) {
    for (int v = 0; v < pointers.length - 1; v++) {
      if (visitor.shouldStop()) {
        return;
      }

      visitor.examineVertex(v + 1);

      int endIndex = pointers[v + 1];
      for (int w = pointers[v]; w < endIndex; w++) {
        if (visitor.shouldStop()) {
          return;
        }

        // Skip reverse direction for undirected edges
        if (!isDirected && (v + 1) > targets[w]) {
          continue;
        }

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

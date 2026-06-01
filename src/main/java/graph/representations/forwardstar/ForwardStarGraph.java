package graph.representations.forwardstar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import graph.api.DirectedGraph;
import graph.api.Edges;
import graph.api.FlowGraph;
import graph.api.Graph;
import graph.api.UndirectedGraph;
import graph.api.WeightedGraph;

/**
 * Concrete and static (immutable) implementation of the Graph using the Forward
 * Star structure.
 */
public class ForwardStarGraph extends Graph implements DirectedGraph, UndirectedGraph, WeightedGraph, FlowGraph {
  private int[] targets;
  private int[] pointers;
  private int[] weightsOrCapacities;
  private final Set<Integer> isolatedVertices;

  /**
   * Package-private constructor. Should only be called by the
   * {@link ForwardStarGraphBuilder}.
   */
  ForwardStarGraph(boolean isDirected, int n, int m, int[] targets, int[] pointers, Set<Integer> isolatedVertices,
      boolean isWeighted, boolean hasCapacity, int[] weightsOrCapacities) {
    super(isDirected, n, m, isWeighted, hasCapacity);

    this.targets = targets;
    this.pointers = pointers;
    this.isolatedVertices = isolatedVertices;
    this.weightsOrCapacities = weightsOrCapacities;
  }

  /**
   * Copy constructor - creates a deep copy for full independence.
   */
  private ForwardStarGraph(ForwardStarGraph graph) {
    super(graph.isDirected, graph.n, graph.m, graph.isWeighted(), graph.hasCapacity());

    this.targets = Arrays.copyOf(graph.targets, graph.targets.length);
    this.pointers = Arrays.copyOf(graph.pointers, graph.pointers.length);
    this.isolatedVertices = new HashSet<>(graph.isolatedVertices);
  }

  @Override
  public Graph clone() {
    return new ForwardStarGraph((ForwardStarGraph) this);
  }

  @Override
  public void addEdge(int v, int w) {
    // Remove from isolated if present (now has an edge)
    isolatedVertices.remove(v);
    isolatedVertices.remove(w);

    // TODO: verify if the v adjacency has a zero, if so, put w in that index
    // instead of rebuilding the graph.

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
    isolatedVertices.addAll(updated.isolatedVertices);
    n = updated.n;
    m = newM;
  }

  @Override
  public void removeEdge(int v, int w) {
    if (v < 1 || v > n || w < 1 || w > n) {
      return;
    }

    int endIndex = pointers[v];
    for (int i = pointers[v - 1]; i < endIndex; i++) {
      if (targets[i] == w) {
        targets[i] = 0;
        break;
      }
    }

    if (!isDirected) {
      endIndex = pointers[w];
      for (int i = pointers[w - 1]; i < endIndex; i++) {
        if (targets[i] == v) {
          targets[i] = 0;
          break;
        }
      }
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

  /**
   * Checks if a vertex has any edges (incoming or outgoing).
   */
  private boolean hasEdges(int v) {
    // Check outgoing edges
    for (int i = pointers[v - 1]; i < pointers[v]; i++) {
      if (targets[i] != 0) {
        return true;
      }
    }

    // Check incoming edges
    for (int i = 0; i < targets.length; i++) {
      if (targets[i] == v) {
        return true;
      }
    }

    return false;
  }

  @Override
  public void iterateGraph(IteratorVisitor visitor) {
    for (int v = 0; v < pointers.length - 1; v++) {
      if (visitor.shouldStop()) {
        return;
      }

      int endIndex = pointers[v + 1];
      if ((endIndex - pointers[v]) == 0) {
        continue;
      }

      visitor.examineVertex(v + 1);

      for (int w = pointers[v]; w < endIndex; w++) {
        if (visitor.shouldStop()) {
          return;
        }

        if (targets[w] == 0) {
          continue;
        }

        // Skip reverse direction for undirected edges
        if (!isDirected && (v + 1) > targets[w]) {
          continue;
        }

        if (isWeighted() && weightsOrCapacities != null) {
          visitor.examineEdge((v + 1), targets[w], weightsOrCapacities[w]);
        } else {
          visitor.examineEdge((v + 1), targets[w]);
        }
      }
    }
  }

  @Override
  public int[] getVertices() {
    Set<Integer> verticesWithEdges = new HashSet<>();

    // Add vertices with outgoing edges
    for (int v = 0; v < pointers.length - 1; v++) {
      if ((pointers[v + 1] - pointers[v]) > 0) {
        verticesWithEdges.add(v + 1);
      }
    }

    // Add vertices with incoming edges (targets)
    for (int target : targets) {
      if (target != 0) {
        verticesWithEdges.add(target);
      }
    }

    return verticesWithEdges.stream().mapToInt(Integer::intValue).sorted().toArray();
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

    int count = 0;
    for (int i = pointers[v - 1]; i < pointers[v]; i++) {
      if (targets[i] != 0) {
        count++;
      }
    }

    return count;
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

    int nonZeroCount = 0;
    for (int s : successors) {
      if (s != 0) {
        nonZeroCount++;
      }
    }

    int[] result = new int[nonZeroCount];

    int index = 0;
    for (int s : successors) {
      if (s != 0) {
        result[index++] = s;
      }
    }

    return result;
  }

  @Override
  public DirectedGraph getReversed() {
    return getReversed(new ForwardStarGraphBuilder(isDirected));
  }

  // Undirected Methods

  @Override
  public int getDegree(int v) {
    return getOutDegree(v);
  }

  @Override
  public int[] getNeighbors(int v) {
    return getSuccessors(v);
  }

  // WeightedGraph Methods

  @Override
  public int getEdgeWeight(int v, int w) {
    if (v < 1 || v > n || w < 1 || w > n) {
      throw new IllegalArgumentException("Vertex out of range");
    }

    int start = pointers[v - 1];
    int end = (v < n) ? pointers[v] : targets.length;

    for (int i = start; i < end; i++) {
      if (targets[i] == w) {
        if (isWeighted() && weightsOrCapacities != null) {
          return weightsOrCapacities[i];
        }
        return 1; // Unweighted graph default
      }
    }

    throw new IllegalArgumentException("Edge " + v + " -> " + w + " not found");
  }

  @Override
  public int getEdgeCapacity(int v, int w) {
    return getEdgeWeight(v, w);
  }

  @Override
  public int[] getWeightsSet() {
    if (!isWeighted() || weightsOrCapacities == null) {
      return new int[0];
    }
    return Arrays.copyOf(weightsOrCapacities, weightsOrCapacities.length);
  }

  @Override
  public int[] getCapacitiesSet() {
    return getCapacitiesSet();
  }

  @Override
  public WeightedEdges getWeightedEdgesSet() {
    List<Long> edges = new ArrayList<>();
    List<Integer> weights = new ArrayList<>();
    iterateGraph(new IteratorVisitor() {
      @Override
      public void examineEdge(int v, int w, int weight) {
        edges.add(Edges.directed(v, w));
        weights.add(weight);
      }
    });
    return new WeightedEdges(
        edges.stream().mapToLong(Long::longValue).toArray(),
        weights.stream().mapToInt(Integer::intValue).toArray());
  }

  @Override
  public CapacityEdges getCapacitiesEdgesSet() {
    List<Long> edges = new ArrayList<>();
    List<Integer> capacities = new ArrayList<>();
    iterateGraph(new IteratorVisitor() {
      @Override
      public void examineEdge(int v, int w, int capacity) {
        edges.add(Edges.directed(v, w));
        capacities.add(capacity);
      }
    });
    return new CapacityEdges(
        edges.stream().mapToLong(Long::longValue).toArray(),
        capacities.stream().mapToInt(Integer::intValue).toArray());
  }
}

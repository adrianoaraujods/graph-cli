package graph.representations.adjacencylist;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import graph.representations.GraphBuilder;

/**
 * Concrete implementation of the GraphBuilder for the Adjacency List structure.
 */
public class AdjacencyListGraphBuilder implements GraphBuilder {

  /** If the graph has directed edges. */
  private boolean isDirected;

  /** If the graph has weighted edges. */
  private boolean isWeighted;

  /** Total number of vertices in the graph. */
  private int n;

  /** Total number of logical edges. */
  private long m;

  private Map<Integer, Set<Integer>> adjacency;
  private Map<Integer, Map<Integer, Integer>> weightedAdjacency;
  private Set<Integer> isolatedVertices;

  public AdjacencyListGraphBuilder(boolean isDirected) {
    this.isDirected = isDirected;
  }

  @Override
  public void initialize(int n, long m) {
    initialize(n, m, false, false);
  }

  @Override
  public void initialize(int n, long m, boolean isWeighted, boolean hasCapacity) {
    this.n = n;
    this.m = 0;
    this.isWeighted = isWeighted;
    this.adjacency = new HashMap<>(n);
    this.weightedAdjacency = isWeighted ? new HashMap<>(n) : null;
    this.isolatedVertices = new HashSet<>(n);
  }

  @Override
  public void addEdge(int v, int w) {
    addEdge(v, w, 1);
  }

  @Override
  public void addEdge(int v, int w, int weight) {
    // Remove from isolated if present (now has an edge)
    isolatedVertices.remove(v);
    isolatedVertices.remove(w);

    if (v > n) {
      n = v;
    }
    if (w > n) {
      n = w;
    }

    Set<Integer> vAdjacency = adjacency.get(v);

    if (vAdjacency == null) {
      vAdjacency = new HashSet<>();
      adjacency.put(v, vAdjacency);
    }

    if (isDirected) {
      if (vAdjacency.contains(w)) {
        return;
      }
    } else {
      Set<Integer> wAdjacency = adjacency.get(w);

      if (wAdjacency == null) {
        wAdjacency = new HashSet<>();
        adjacency.put(w, wAdjacency);
      }

      if (wAdjacency.contains(v)) {
        return;
      }

      wAdjacency.add(v);
    }

    vAdjacency.add(w);

    if (isWeighted && weightedAdjacency != null) {
      weightedAdjacency.computeIfAbsent(v, k -> new HashMap<>()).put(w, weight);
      if (!isDirected) {
        weightedAdjacency.computeIfAbsent(w, k -> new HashMap<>()).put(v, weight);
      }
    }

    m++;
  }

  @Override
  public void addVertex(int v) {
    isolatedVertices.add(v);
  }

  @Override
  public AdjacencyListGraph build() {
    return new AdjacencyListGraph(isDirected, n, m, isWeighted, adjacency, weightedAdjacency, isolatedVertices);
  }
}

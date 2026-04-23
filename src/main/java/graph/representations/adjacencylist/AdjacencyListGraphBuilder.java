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

  /** Total number of vertices in the graph. */
  private int n;

  /** Total number of logical edges. */
  private long m;

  private Map<Integer, Set<Integer>> vertices;

  public AdjacencyListGraphBuilder(boolean isDirected) {
    this.isDirected = isDirected;
  }

  @Override
  public void initialize(int n, long m, int[] vertices) {
    this.n = n;
    this.m = 0;
    this.vertices = new HashMap<>(n);

    if (vertices == null) {
      for (int v = 1; v <= n; v++) {
        this.vertices.put(v, new HashSet<>((int) (m / n)));
      }
    } else {
      for (int v : vertices) {
        this.vertices.put(v, new HashSet<>((int) (m / n)));
      }
    }
  }

  @Override
  public void addEdge(int v, int w) {
    if (v < 1 || w < 1) {
      return;
    }

    if (v > n) {
      n = v;
    }
    if (w > n) {
      n = w;
    }

    Set<Integer> vAdjacency = vertices.get(v);

    if (vAdjacency == null) {
      vAdjacency = new HashSet<>();
      vertices.put(v, vAdjacency);
    }

    if (isDirected) {
      if (vAdjacency.contains(w)) {
        return;
      }
    } else {
      Set<Integer> wAdjacency = vertices.get(w);

      if (wAdjacency == null) {
        wAdjacency = new HashSet<>();
        vertices.put(w, wAdjacency);
      }

      if (wAdjacency.contains(v)) {
        return;
      }

      wAdjacency.add(v);
    }

    vAdjacency.add(w);
    m++;
  }

  @Override
  public AdjacencyListGraph build() {
    return new AdjacencyListGraph(isDirected, n, m, vertices);
  }
}

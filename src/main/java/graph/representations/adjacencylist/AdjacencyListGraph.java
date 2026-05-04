package graph.representations.adjacencylist;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import graph.api.DirectedGraph;
import graph.api.Graph;
import graph.api.UndirectedGraph;
import graph.representations.forwardstar.ForwardStarGraphBuilder;

public class AdjacencyListGraph extends Graph implements DirectedGraph, UndirectedGraph {
  private Map<Integer, Set<Integer>> vertices;
  private final Set<Integer> isolatedVertices;

  /**
   * Package-private constructor. Should only be called by the
   * {@link ForwardStarGraphBuilder}.
   */
  AdjacencyListGraph(boolean isDirected, int n, long m, Map<Integer, Set<Integer>> vertices,
      Set<Integer> isolatedVertices) {
    super(isDirected, n, m, false);
    this.vertices = vertices;
    this.isolatedVertices = isolatedVertices;
  }

  /**
   * Copy constructor - creates a deep copy for full independence.
   */
  private AdjacencyListGraph(AdjacencyListGraph graph) {
    super(graph.isDirected, graph.n, graph.m, false);
    vertices = new HashMap<>(graph.vertices);
    isolatedVertices = new HashSet<>(graph.isolatedVertices);

    for (Map.Entry<Integer, Set<Integer>> entry : graph.vertices.entrySet()) {
      vertices.put(entry.getKey(), new HashSet<>(entry.getValue()));
    }
  }

  @Override
  public Graph clone() {
    return new AdjacencyListGraph(this);
  }

  @Override
  public void addEdge(int v, int w) {
    // Remove from isolated if present (now has an edge)
    isolatedVertices.remove(v);
    isolatedVertices.remove(w);

    Set<Integer> vAdjacency = vertices.get(v);

    if (vAdjacency == null) {
      vAdjacency = new HashSet<>();
      vertices.put(v, vAdjacency);
      n++;
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
        n++;
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
  public void removeEdge(int v, int w) {
    Set<Integer> vAdjacency = vertices.get(v);

    if (vAdjacency == null) {
      return;
    }

    if (isDirected) {
      if (!vAdjacency.contains(w)) {
        return;
      }
    } else {
      Set<Integer> wAdjacency = vertices.get(w);

      if (wAdjacency == null) {
        return;
      }

      if (!wAdjacency.contains(v)) {
        return;
      }

      wAdjacency.remove(v);

      if (wAdjacency.isEmpty()) {
        vertices.remove(w);
        // Check if w has any incoming edges
        if (!hasIncomingEdges(w)) {
          isolatedVertices.add(w);
        }
      }
    }

    vAdjacency.remove(w);
    if (vAdjacency.isEmpty()) {
      vertices.remove(v);
      // Check if v has any incoming edges
      if (!hasIncomingEdges(v)) {
        isolatedVertices.add(v);
      }
    }

    m--;
  }

  /**
   * Checks if a vertex has any incoming edges.
   */
  private boolean hasIncomingEdges(int v) {
    for (Set<Integer> adjacency : vertices.values()) {
      if (adjacency.contains(v)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void iterateGraph(IteratorVisitor visitor) {
    for (Entry<Integer, Set<Integer>> entry : vertices.entrySet()) {
      int v = entry.getKey();

      if (visitor.shouldStop()) {
        return;
      }

      visitor.examineVertex(v);

      Set<Integer> adjacency = entry.getValue();

      for (int w : adjacency) {
        if (visitor.shouldStop()) {
          return;
        }

        visitor.examineEdge(v, w);
      }
    }
  }

  @Override
  public int[] getVertices() {
    Set<Integer> allVerticesWithEdges = new HashSet<>(vertices.keySet());

    // Add vertices that only have incoming edges
    for (Set<Integer> adjacency : vertices.values()) {
      allVerticesWithEdges.addAll(adjacency);
    }

    return allVerticesWithEdges.stream().mapToInt(Integer::intValue).sorted().toArray();
  }

  @Override
  public Graph getInducedSubgraph(int[] vertices) {
    return getInducedSubgraph(vertices, new AdjacencyListGraphBuilder(isDirected));
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

    for (Map.Entry<Integer, Set<Integer>> entry : vertices.entrySet()) {
      for (int w : entry.getValue()) {
        if (w == v) {
          inDegree++;
        }
      }
    }

    return inDegree;
  }

  @Override
  public int getOutDegree(int v) {
    if (v < 1 || v > n) {
      throw new IllegalArgumentException();
    }

    Set<Integer> adjacency = vertices.get(v);

    return adjacency == null ? 0 : adjacency.size();
  }

  @Override
  public int[] getPredecessors(int v) {
    if (v < 1 || v > n) {
      throw new IllegalArgumentException();
    }

    int count = 0;
    for (Map.Entry<Integer, Set<Integer>> entry : vertices.entrySet()) {
      if (entry.getValue().contains(v)) {
        count++;
      }
    }

    int[] predecessors = new int[count];
    int i = 0;

    for (Map.Entry<Integer, Set<Integer>> entry : vertices.entrySet()) {
      if (entry.getValue().contains(v)) {
        predecessors[i++] = entry.getKey();
      }
    }

    return predecessors;
  }

  @Override
  public int[] getSuccessors(int v) {
    if (v < 1 || v > n) {
      throw new IllegalArgumentException();
    }

    Set<Integer> adjacency = vertices.get(v);

    if (adjacency == null || adjacency.isEmpty()) {
      return new int[0];
    }

    int[] successors = new int[adjacency.size()];
    int i = 0;

    for (int w : adjacency) {
      successors[i++] = w;
    }

    return successors;
  }

  @Override
  public DirectedGraph getReversed() {
    return getReversed(new AdjacencyListGraphBuilder(isDirected));
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
}

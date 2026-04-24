package graph.algorithms;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import graph.api.Edges;
import graph.util.Sort;

/**
 * @deprecated
 */
public class HavelHakimi {

  /**
   * Constructs graph edges based on a degree sequence using Havel-Hakimi.
   *
   * @param sequence  Array representing the degree sequence.
   * @param connected Whether to force the resulting graph to be a single
   *                  component.
   * @return A Set of strings where each string is an edge "v-w", or null if
   *         the sequence is not graphical.
   */
  public static Set<Long> generateEdges(int[] sequence, boolean connected) {
    int n = sequence.length;

    int[] degrees = new int[n];
    int[] indices = new int[n];
    for (int i = 0; i < n; i++) {
      degrees[i] = sequence[i];
      indices[i] = i;
    }

    int totalDegree = 0;
    for (int d : sequence) {
      totalDegree += d;
    }

    if (totalDegree % 2 != 0) {
      return null;
    }

    int m = totalDegree / 2;
    Set<Long> edges = new HashSet<>();

    UnionFind uf = null;
    if (connected) {
      uf = new UnionFind(n);
    }

    while (edges.size() < m) {
      Sort.quick(degrees, indices, false);

      int d = degrees[0];
      if (d == 0) {
        break;
      }

      if (d < 0 || d > n - 1) {
        return null;
      }

      for (int i = 1; i <= d && i < n; i++) {
        if (degrees[i] <= 0) {
          return null;
        }

        int v = indices[0];
        int w = indices[i];

        edges.add(Edges.undirected(v, w));

        if (connected) {
          uf.union(v, w);
        }

        degrees[i]--;
      }

      degrees[0] = 0;
    }

    if (connected) {
      connectComponents(edges, uf, n);
    }

    return edges;
  }

  // ==========================================================================
  // COMPONENT MERGING LOGIC
  // ==========================================================================

  /**
   * Merges disjoint components by swapping edges from cycles with edges in
   * disconnected components, preserving the exact degree sequence.
   */
  public static void connectComponents(Set<Long> edges, UnionFind uf, int n) {
    if (edges == null || uf == null || uf.isConnected()) {
      return;
    }

    do {
      List<Long> cycleEdges = findCycleEdges(edges, n);

      // Try to use one of these cycle edges to bridge to another component
      for (Long cycleEdge : cycleEdges) {
        if (trySwapWithDisjointComponent(edges, uf, cycleEdge)) {
          continue;
        }
      }

      break; // prevent infinity loops when no merge was possible
    } while (!uf.isConnected());
  }

  /**
   * Scans the current edges to find those that form cycles (redundant edges)
   * within their respective components.
   */
  private static List<Long> findCycleEdges(Set<Long> edges, int n) {
    UnionFind cycleUf = new UnionFind(n);
    List<Long> cycleEdges = new ArrayList<>();

    for (Long edge : edges) {
      int v = Edges.getSource(edge);
      int w = Edges.getTarget(edge);

      if (cycleUf.find(v) == cycleUf.find(w)) {
        cycleEdges.add(edge);
      } else {
        cycleUf.union(v, w);
      }
    }

    return cycleEdges;
  }

  /**
   * Looks for an edge in a completely different component to swap with the
   * provided cycle edge.
   */
  private static boolean trySwapWithDisjointComponent(Set<Long> edges, UnionFind uf, Long cycleEdge) {
    int v = Edges.getSource(cycleEdge);
    int w = Edges.getTarget(cycleEdge);
    int componentId = uf.find(v);

    for (Long otherEdge : edges) {
      int s = Edges.getSource(otherEdge);
      int t = Edges.getTarget(otherEdge);

      // Must belong to a completely different component
      if (uf.find(s) != componentId) {

        // Attempt Option A: cross-wire to (u, s) and (v, t)
        if (executeSwapIfValid(edges, uf, cycleEdge, otherEdge, v, s, w, t)) {
          return true;
        }

        // Attempt Option B: cross-wire to (u, t) and (v, s)
        if (executeSwapIfValid(edges, uf, cycleEdge, otherEdge, v, t, w, s)) {
          return true;
        }
      }
    }

    return false;
  }

  /**
   * Checks if the new cross-component edges already exist. If they don't,
   * it applies the swap to the Set and updates the master UnionFind.
   */
  private static boolean executeSwapIfValid(Set<Long> edges, UnionFind uf,
      Long oldEdge1, Long oldEdge2, int newU1, int newV1, int newU2, int newV2) {

    Long newEdge1 = Edges.undirected(newU1, newV1);
    Long newEdge2 = Edges.undirected(newU2, newV2);

    if (!edges.contains(newEdge1) && !edges.contains(newEdge2)) {
      edges.remove(oldEdge1);
      edges.remove(oldEdge2);

      edges.add(newEdge1);
      edges.add(newEdge2);

      // Bridge the components in our master UnionFind tracking
      uf.union(newU1, newV1);
      return true;
    }

    return false;
  }
}
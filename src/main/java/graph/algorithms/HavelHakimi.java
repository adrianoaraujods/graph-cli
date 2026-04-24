package graph.algorithms;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import graph.util.EdgeFormatter;
import graph.util.Sort;
import graph.util.UnionFind;

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
  public static Set<String> generateEdges(int[] sequence, boolean connected) {
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
    Set<String> edges = new HashSet<>();

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

        edges.add(EdgeFormatter.toKey(v, w));

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
  public static void connectComponents(Set<String> edges, UnionFind uf, int n) {
    if (edges == null || uf == null || uf.isConnected()) {
      return;
    }

    do {
      List<String> cycleEdges = findCycleEdges(edges, n);

      // Try to use one of these cycle edges to bridge to another component
      for (String cycleEdge : cycleEdges) {
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
  private static List<String> findCycleEdges(Set<String> edges, int n) {
    UnionFind cycleUf = new UnionFind(n);
    List<String> cycleEdges = new ArrayList<>();

    for (String edge : edges) {
      int u = getU(edge);
      int v = getV(edge);

      if (cycleUf.find(u) == cycleUf.find(v)) {
        cycleEdges.add(edge);
      } else {
        cycleUf.union(u, v);
      }
    }

    return cycleEdges;
  }

  /**
   * Looks for an edge in a completely different component to swap with the
   * provided cycle edge.
   */
  private static boolean trySwapWithDisjointComponent(Set<String> edges, UnionFind uf, String cycleEdge) {
    int u = getU(cycleEdge);
    int v = getV(cycleEdge);
    int componentId = uf.find(u);

    for (String otherEdge : edges) {
      int s = getU(otherEdge);
      int t = getV(otherEdge);

      // Must belong to a completely different component
      if (uf.find(s) != componentId) {

        // Attempt Option A: cross-wire to (u, s) and (v, t)
        if (executeSwapIfValid(edges, uf, cycleEdge, otherEdge, u, s, v, t)) {
          return true;
        }

        // Attempt Option B: cross-wire to (u, t) and (v, s)
        if (executeSwapIfValid(edges, uf, cycleEdge, otherEdge, u, t, v, s)) {
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
  private static boolean executeSwapIfValid(Set<String> edges, UnionFind uf,
      String oldEdge1, String oldEdge2,
      int newU1, int newV1, int newU2, int newV2) {

    String newEdge1 = EdgeFormatter.toKey(newU1, newV1);
    String newEdge2 = EdgeFormatter.toKey(newU2, newV2);

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

  // --- String Parsing Helpers ---

  private static int getU(String edge) {
    return Integer.parseInt(edge.split("-")[0]);
  }

  private static int getV(String edge) {
    return Integer.parseInt(edge.split("-")[1]);
  }
}
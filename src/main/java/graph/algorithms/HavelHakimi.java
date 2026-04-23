package graph.algorithms;

import graph.util.Sort;

public class HavelHakimi {

  /**
   * Constructs graph edges based on a degree sequence using Havel-Hakimi.
   *
   * @param sequence Array representing the degree sequence.
   * @return A 2D array where each inner array is an edge [u, v], or null if
   *         the sequence is not graphical.
   */
  public static int[][] generateEdges(int[] sequence) {
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
    int[][] edges = new int[m][2];
    int e = 0;

    while (e < m) {
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

        edges[e][0] = indices[0];
        edges[e][1] = indices[i];
        degrees[i]--;
        e++;
      }

      degrees[0] = 0;
    }

    return edges;
  }
}
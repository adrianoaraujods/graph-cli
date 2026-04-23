package graph.algorithms;

import java.util.HashSet;
import java.util.Set;

import graph.api.Graph;
import graph.api.UndirectedGraph;
import graph.util.EdgeFormatter;

public class Tarjan {

  public static Set<String> findAll(Graph graph) {
    if (graph.isDirected) {
      throw new IllegalArgumentException("Tarjan algorithm only works for undirected graphs");
    }

    return findAll((UndirectedGraph) graph);
  }

  public static Set<String> findAll(UndirectedGraph graph) {
    Set<String> bridges = new HashSet<>();

    int n = graph.getVerticesCount();
    if (n == 0) {
      return bridges;
    }

    DFS.DFSVisitor visitor = new DFS.DFSVisitor() {
      int[] lowestPath = new int[n];
      int[] discoverTimes;
      int[] parents;

      @Override
      public void start(int[] discoverTimes, int[] finishTimes, int[] parents) {
        this.discoverTimes = discoverTimes;
        this.parents = parents;
      }

      @Override
      public void discoverVertex(int v) {
        lowestPath[v - 1] = discoverTimes[v - 1];
      }

      @Override
      public void backEdge(int v, int w) {
        lowestPath[v - 1] = Math.min(lowestPath[v - 1], discoverTimes[w - 1]);
      }

      @Override
      public void finishVertex(int v) {
        int p = parents[v - 1];

        if (p > 0) {
          lowestPath[p - 1] = Math.min(lowestPath[p - 1], lowestPath[v - 1]);
        }
      }

      @Override
      public DFS.DFSResult finish(int[] discoverTimes, int[] finishTimes, int[] parents) {
        for (int v = 1; v <= parents.length; v++) {
          int parent = parents[v - 1];

          if (parent > 0 && lowestPath[v - 1] > discoverTimes[parent - 1]) {
            bridges.add(EdgeFormatter.toKey(parent, v));
          }
        }

        return new DFS.DFSResult(null, null, null);
      }
    };

    DFS.search((Graph) graph, visitor);

    return bridges;
  }
}
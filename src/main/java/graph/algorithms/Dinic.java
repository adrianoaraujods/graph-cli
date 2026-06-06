package graph.algorithms;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import graph.api.Edges;
import graph.api.FlowGraph;
import graph.api.Graph;
import graph.cli.read.result.MaximumFlowResult;

public class Dinic {

  public static MaximumFlowResult compute(Graph graph, int source, int target) {
    if (!graph.isDirected) {
      throw new IllegalArgumentException("Dinic requires a directed graph");
    }
    if (source < 1 || source > graph.getVerticesCount() ||
        target < 1 || target > graph.getVerticesCount()) {
      throw new IllegalArgumentException("Source or target out of range");
    }

    int n = graph.getVerticesCount();
    int m = (int) graph.getEdgesCount();

    long[] originalEdges = graph.getEdgesSet();
    int[] capacities;
    if (graph instanceof FlowGraph fg) {
      capacities = fg.getCapacitiesSet();
    } else {
      throw new IllegalArgumentException("Graph does not support flow capacities");
    }

    if (capacities.length == 0) {
      throw new IllegalArgumentException("Graph has no edge capacities. Use --capacities to load capacities from file.");
    }
    if (capacities.length != m) {
      throw new IllegalArgumentException("Capacity array size mismatch");
    }

    Map<Long, Integer> edgeIndex = new HashMap<>();
    for (int i = 0; i < m; i++) {
      edgeIndex.put(originalEdges[i], i);
    }

    int extendedM = 2 * m;
    int[] cap = new int[extendedM];
    int[] flo = new int[extendedM];
    int[] to = new int[extendedM];

    List<List<Integer>> adj = new ArrayList<>(n + 1);
    for (int v = 0; v <= n; v++) {
      adj.add(new ArrayList<>());
    }

    for (int i = 0; i < m; i++) {
      int v = Edges.getSource(originalEdges[i]);
      int w = Edges.getTarget(originalEdges[i]);
      int capVal = capacities[i];

      int forwardIdx = 2 * i;
      int reverseIdx = 2 * i + 1;

      adj.get(v).add(forwardIdx);
      cap[forwardIdx] = capVal;
      flo[forwardIdx] = 0;
      to[forwardIdx] = w;

      adj.get(w).add(reverseIdx);
      cap[reverseIdx] = 0;
      flo[reverseIdx] = 0;
      to[reverseIdx] = v;
    }

    int maxFlow = 0;
    int[] level = new int[n + 1];
    int[] parent = new int[n + 1];
    int[] it = new int[n + 1];

    while (bfs(source, target, n, adj, cap, flo, to, level)) {
      Arrays.fill(it, 0);
      while (dfs(source, target, cap, flo, to, level, it, parent, adj)) {
        maxFlow++;
        int v = target;
        while (v != source) {
          int e = parent[v];
          flo[e]++;
          flo[e ^ 1]--;
          v = to[e ^ 1];
        }
      }
    }

    int[] originalFlow = new int[m];
    for (int i = 0; i < m; i++) {
      originalFlow[i] = flo[2 * i];
    }

    return new MaximumFlowResult(source, target, maxFlow, null, originalFlow, originalEdges);
  }

  private static boolean bfs(int source, int target, int n,
      List<List<Integer>> adj, int[] cap, int[] flo, int[] to, int[] level) {
    Arrays.fill(level, -1);
    Queue<Integer> queue = new ArrayDeque<>();
    level[source] = 0;
    queue.offer(source);

    while (!queue.isEmpty()) {
      int u = queue.poll();
      for (int e : adj.get(u)) {
        if (cap[e] - flo[e] > 0) {
          int v = to[e];
          if (level[v] == -1) {
            level[v] = level[u] + 1;
            queue.offer(v);
          }
        }
      }
    }
    return level[target] != -1;
  }

  private static boolean dfs(int u, int target, int[] cap, int[] flo, int[] to,
      int[] level, int[] it, int[] parent, List<List<Integer>> adj) {
    if (u == target)
      return true;
    for (; it[u] < adj.get(u).size(); it[u]++) {
      int e = adj.get(u).get(it[u]);
      if (cap[e] - flo[e] > 0) {
        int v = to[e];
        if (level[v] == level[u] + 1) {
          parent[v] = e;
          if (dfs(v, target, cap, flo, to, level, it, parent, adj)) {
            return true;
          }
        }
      }
    }
    return false;
  }
}

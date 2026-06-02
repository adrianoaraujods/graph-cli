package graph.algorithms;

import java.util.ArrayList;
import java.util.List;

import graph.api.DirectedGraph;
import graph.api.Edges;
import graph.cli.read.result.MaximumFlowResult;
import graph.representations.forwardstar.ForwardStarGraphBuilder;

public class DisjointPaths {

  public static MaximumFlowResult compute(DirectedGraph graph, int source, int target) {
    int n = graph.getVerticesCount();
    long m = graph.getEdgesCount();

    ForwardStarGraphBuilder builder = new ForwardStarGraphBuilder(true);
    builder.initialize(n, m, false, true);

    long[] originalEdges = graph.getEdgesSet();
    for (long edge : originalEdges) {
      int v = Edges.getSource(edge);
      int w = Edges.getTarget(edge);
      builder.addEdge(v, w, 1);
    }

    var flowGraph = builder.build();
    MaximumFlowResult result = Dinic.compute(flowGraph, source, target);

    int maxFlow = result.maxFlow();
    int[] flow = result.flow();
    long[] edges = result.edges();
    int edgeCount = edges.length;

    List<int[]> pathList = new ArrayList<>();
    boolean[] used = new boolean[edgeCount];

    for (int f = 0; f < maxFlow; f++) {
      List<Integer> path = new ArrayList<>();
      int u = source;
      path.add(u);
      while (u != target) {
        for (int i = 0; i < edgeCount; i++) {
          if (!used[i] && flow[i] > 0) {
            int v = Edges.getSource(edges[i]);
            int w = Edges.getTarget(edges[i]);
            if (v == u) {
              path.add(w);
              u = w;
              used[i] = true;
              break;
            }
          }
        }
      }
      pathList.add(path.stream().mapToInt(Integer::intValue).toArray());
    }

    int[][] paths = pathList.toArray(int[][]::new);
    return new MaximumFlowResult(source, target, maxFlow, paths, flow, edges);
  }
}

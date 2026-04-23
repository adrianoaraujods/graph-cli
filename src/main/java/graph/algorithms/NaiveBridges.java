package graph.algorithms;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import graph.api.Graph;
import graph.api.GraphBase.IteratorVisitor;
import graph.api.UndirectedGraph;
import graph.util.EdgeFormatter;
import graph.util.UnionFind;

public class NaiveBridges {

  private static class EdgeBatch {
    private final int[][] edges;
    private final int componentsCount;

    EdgeBatch(int[][] edges, int start, int end, int componentsCount) {
      this.edges = Arrays.copyOfRange(edges, start, end);
      this.componentsCount = componentsCount;
    }

    Set<String> compute(Graph graph) {
      Set<String> localBridges = new HashSet<>();

      for (int[] edge : edges) {
        int v = edge[0];
        int w = edge[1];

        UnionFind uf = new UnionFind(graph.getVerticesCount());

        IteratorVisitor iterator = new IteratorVisitor() {
          @Override
          public void examineEdge(int edgeV, int edgeW) {
            if (edgeV == v && edgeW == w) {
              return;
            }
            uf.union(edgeV - 1, edgeW - 1);
          }
        };

        graph.iterateGraph(iterator);

        if (uf.getCount() != componentsCount) {
          localBridges.add(EdgeFormatter.toKey(v, w));
        }
      }

      return localBridges;
    }
  }

  public static Set<String> findAll(Graph graph) {
    int componentsCount = ConnectedComponents.getCount(graph);

    int[][] edges = graph.getEdgesSet();
    int m = edges.length;

    if (m == 0) {
      return new HashSet<>();
    }

    int numThreads = Runtime.getRuntime().availableProcessors();
    int batchSize = Math.max(1, m / numThreads);

    Set<String> bridges = Arrays.stream(IntStream.range(0, numThreads).toArray())
        .parallel()
        .mapToObj(i -> {
          int start = i * batchSize;
          int end = (i == numThreads - 1) ? m : Math.min((i + 1) * batchSize, m);
          if (start >= m) {
            return new HashSet<String>();
          }
          return new EdgeBatch(edges, start, end, componentsCount).compute(graph);
        })
        .flatMap(Set::stream)
        .collect(Collectors.toSet());

    return bridges;
  }

  public static Set<String> findAll(UndirectedGraph graph) {
    return findAll((Graph) graph);
  }
}
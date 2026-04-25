package graph.algorithms;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import graph.api.Edges;
import graph.api.Graph;
import graph.api.GraphBase.IteratorVisitor;
import graph.api.UndirectedGraph;

public class NaiveBridges {

  private static class EdgeBatch {
    private final long[] edges;
    private final int componentsCount;

    EdgeBatch(long[] edges, int start, int end, int componentsCount) {
      this.edges = Arrays.copyOfRange(edges, start, end);
      this.componentsCount = componentsCount;
    }

    Set<Long> compute(Graph graph) {
      Set<Long> localBridges = new HashSet<>();

      for (long edge : edges) {
        int v = Edges.getSource(edge);
        int w = Edges.getTarget(edge);

        UnionFind uf = new UnionFind(graph.getVerticesCount());

        IteratorVisitor iterator = new IteratorVisitor() {
          @Override
          public void examineEdge(int edgeV, int edgeW) {
            if ((edgeV == v && edgeW == w) || (!graph.isDirected && (edgeV == w && edgeW == v))) {
              return;
            }
            uf.union(edgeV - 1, edgeW - 1);
          }
        };

        graph.iterateGraph(iterator);

        if (uf.getCount() != componentsCount) {
          localBridges.add(Edges.undirected(v, w));
        }
      }

      return localBridges;
    }
  }

  public static Set<Long> findAll(Graph graph) {
    int componentsCount = ConnectedComponents.getCount(graph);

    long[] edges = graph.getEdgesSet();
    int m = edges.length;

    if (m == 0) {
      return new HashSet<>();
    }

    int numThreads = Runtime.getRuntime().availableProcessors();
    int batchSize = Math.max(1, m / numThreads);

    Set<Long> bridges = Arrays.stream(IntStream.range(0, numThreads).toArray())
        .parallel()
        .mapToObj(i -> {
          int start = i * batchSize;
          int end = (i == numThreads - 1) ? m : Math.min((i + 1) * batchSize, m);
          if (start >= m) {
            return new HashSet<Long>();
          }
          return new EdgeBatch(edges, start, end, componentsCount).compute(graph);
        })
        .flatMap(Set::stream)
        .collect(Collectors.toSet());

    return bridges;
  }

  public static Set<Long> findAll(UndirectedGraph graph) {
    return findAll((Graph) graph);
  }

  public static boolean isBridge(int v, int w, Graph graph, int componentsCount) {
    UnionFind uf = new UnionFind(graph.getVerticesCount());

    IteratorVisitor iterator = new IteratorVisitor() {
      @Override
      public void examineEdge(int edgeV, int edgeW) {
        if ((edgeV == v && edgeW == w) || (!graph.isDirected && (edgeV == w && edgeW == v))) {
          return;
        }
        uf.union(edgeV - 1, edgeW - 1);
      }
    };

    graph.iterateGraph(iterator);

    return uf.getCount() != componentsCount;
  }
}
package graph.algorithms;

import graph.api.Graph;
import graph.api.GraphBase;
import graph.api.GraphBase.IteratorVisitor;

public class ConnectedComponents {
  public static int getCount(Graph graph, int[] ignoredEdge) {
    if (ignoredEdge == null) {
      return getCount(graph);
    }

    UnionFind uf = new UnionFind(graph.getVerticesCount());

    GraphBase.IteratorVisitor iterator = new IteratorVisitor() {
      @Override
      public void examineEdge(int v, int w) {
        if (ignoredEdge[0] == v && ignoredEdge[1] == w) {
          return;
        }

        uf.union(v - 1, w - 1);
      }
    };

    graph.iterateGraph(iterator);

    return uf.getCount();
  }

  public static int getCount(Graph graph) {
    UnionFind uf = new UnionFind(graph.getVerticesCount());

    GraphBase.IteratorVisitor iterator = new IteratorVisitor() {
      @Override
      public void examineEdge(int v, int w) {
        uf.union(v - 1, w - 1);
      }
    };

    graph.iterateGraph(iterator);

    return uf.getCount();
  }
}

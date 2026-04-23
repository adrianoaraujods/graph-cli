package graph.algorithms;

import graph.api.Graph;
import graph.api.GraphBase;
import graph.api.GraphBase.IteratorVisitor;
import graph.util.UnionFind;

public class ConnectedComponents {
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

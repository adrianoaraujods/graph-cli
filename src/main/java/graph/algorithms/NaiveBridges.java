package graph.algorithms;

import java.util.HashSet;
import java.util.Set;

import graph.api.Graph;
import graph.api.GraphBase;
import graph.api.GraphBase.IteratorVisitor;
import graph.api.UndirectedGraph;
import graph.util.EdgeFormatter;

public class NaiveBridges {

  public static Set<String> findAll(Graph graph) {
    Set<String> bridges = new HashSet<>();

    int componentsCount = ConnectedComponents.getCount(graph);

    GraphBase.IteratorVisitor iterator = new IteratorVisitor() {
      @Override
      public void examineEdge(int v, int w) {
        graph.removeEdge(v, w);

        if (ConnectedComponents.getCount(graph) != componentsCount) {
          bridges.add(EdgeFormatter.toKey(v, w));
        }

        graph.addEdge(v, w);
      }
    };

    graph.iterateGraph(iterator);

    return bridges;
  }

  public static Set<String> findAll(UndirectedGraph graph) {
    return findAll((Graph) graph);
  }
}
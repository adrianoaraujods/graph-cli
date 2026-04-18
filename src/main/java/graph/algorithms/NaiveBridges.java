package graph.algorithms;

import java.util.HashSet;
import java.util.Set;

import graph.algorithms.DFS.DFSVisitor;
import graph.api.DirectedGraph;
import graph.api.Graph;
import graph.api.GraphBase.IteratorVisitor;
import graph.api.UndirectedGraph;

public class NaiveBridges {

  private static class IterateComponent implements IteratorVisitor {
    public final Set<int[]> bridges;
    private final UndirectedGraph component;

    IterateComponent(Set<int[]> bridges, UndirectedGraph component) {
      this.bridges = bridges;
      this.component = component;
    }

    private static class DisconnectedVisitor implements DFSVisitor {
      public boolean disconnected = false;

      @Override
      public boolean shouldStop() {
        return disconnected;
      }

      @Override
      public void examineRoot(int root) {
        if (root != 1) {
          disconnected = true;
        }
      }
    };

    @Override
    public void examineEdge(int v, int w) {
      component.removeEdge(v, w);

      DisconnectedVisitor visitor = new DisconnectedVisitor();
      DFS.search((Graph) component, visitor);

      if (visitor.disconnected) {
        bridges.add(new int[] { v, w });
      }

      component.addEdge(v, w);
    }
  }

  public static Set<int[]> findAll(UndirectedGraph graph) {
    Set<int[]> bridges = new HashSet<>();

    UndirectedGraph[] components = (UndirectedGraph[]) ConnectedComponents.find(graph);

    for (UndirectedGraph component : components) {
      IterateComponent iterator = new IterateComponent(bridges, component);
      component.iterateGraph(iterator);
    }

    return bridges;
  }

  public static Set<int[]> findAllWeak(DirectedGraph graph) {
    return findAll((UndirectedGraph) graph);
  }
}

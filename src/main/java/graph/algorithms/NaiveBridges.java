package graph.algorithms;

import java.security.InvalidAlgorithmParameterException;
import java.util.HashSet;
import java.util.Set;

import graph.algorithms.DFS.DFSVisitor;
import graph.api.Graph;
import graph.api.GraphBase;
import graph.api.GraphBase.IteratorVisitor;

public class NaiveBridges {

  private static class IterateComponent implements IteratorVisitor {
    public final Set<int[]> bridges;
    private final GraphBase component;

    IterateComponent(Set<int[]> bridges, GraphBase component) {
      this.bridges = bridges;
      this.component = component;
    }

    private static class DisconnectedVisitor implements DFSVisitor {
      public boolean disconnected = false;

      private int root = 0;

      @Override
      public boolean shouldStop() {
        return disconnected;
      }

      @Override
      public void examineRoot(int vertex) {
        if (root == 0) {
          root = vertex;
          return;
        }

        disconnected = true;
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

  public static Set<int[]> find(Graph graph) throws InvalidAlgorithmParameterException {
    Set<int[]> bridges = new HashSet<>();

    Graph[] components = ConnectedComponents.find(graph);
    for (Graph component : components) {
      IterateComponent iterator = new IterateComponent(bridges, component);
      component.iterateGraph(iterator);
    }

    return bridges;
  }
}

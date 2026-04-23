package graph.algorithms;

import java.util.HashSet;
import java.util.Set;

import graph.algorithms.DFS.DFSVisitor;
import graph.api.DirectedGraph;
import graph.api.Graph;
import graph.api.GraphBase.IteratorVisitor;
import graph.api.UndirectedGraph;
import graph.util.EdgeFormatter;

public class NaiveBridges {

  private static class IterateComponent implements IteratorVisitor {
    public final Set<String> bridges;
    private final UndirectedGraph component;
    private Set<Integer> trivialVertices;

    IterateComponent(Set<String> bridges, UndirectedGraph component, Set<Integer> trivialVertices) {
      this.bridges = bridges;
      this.component = component;
      this.trivialVertices = trivialVertices;
    }

    IterateComponent(Set<String> bridges, UndirectedGraph component) {
      Set<Integer> trivialVertices = new HashSet<>();
      this(bridges, component, trivialVertices);
    }

    private static class DisconnectedVisitor implements DFSVisitor {
      private Set<Integer> trivialVertices;
      public int roots = 0;

      DisconnectedVisitor(Set<Integer> trivialVertices) {
        this.trivialVertices = trivialVertices;
      }

      @Override
      public boolean shouldStop() {
        return roots > 1;
      }

      @Override
      public void examineRoot(int root) {
        if (!trivialVertices.contains(root)) {
          roots++;
        }
      }
    };

    @Override
    public void examineEdge(int v, int w) {
      component.removeEdge(v, w);

      DisconnectedVisitor visitor = new DisconnectedVisitor(trivialVertices);
      DFS.search((Graph) component, visitor);

      if (visitor.roots > 1) {
        bridges.add(EdgeFormatter.toKey(v, w));
      }

      component.addEdge(v, w);
    }
  }

  public static Set<String> findAll(UndirectedGraph graph, Set<Integer> trivialVertices) {
    Set<String> bridges = new HashSet<>();

    IterateComponent iterator = new IterateComponent(bridges, graph, trivialVertices);
    graph.iterateGraph(iterator);

    return bridges;
  }

  public static Set<String> findAll(UndirectedGraph graph) {
    Set<String> bridges = new HashSet<>();

    UndirectedGraph[] components = (UndirectedGraph[]) ConnectedComponents.find(graph);

    for (UndirectedGraph component : components) {
      if (component.getEdgesCount() < 1) {
        continue;
      }

      IterateComponent iterator = new IterateComponent(bridges, component);
      component.iterateGraph(iterator);
    }

    return bridges;
  }

  public static Set<String> findAllWeak(DirectedGraph graph) {
    return findAll((UndirectedGraph) graph);
  }
}

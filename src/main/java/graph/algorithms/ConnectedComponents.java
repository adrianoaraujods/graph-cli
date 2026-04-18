package graph.algorithms;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import graph.algorithms.DFS.DFSResult;
import graph.algorithms.DFS.DFSVisitor;
import graph.api.Graph;
import graph.api.UndirectedGraph;

public class ConnectedComponents {
  public static UndirectedGraph[] find(UndirectedGraph graph) {
    List<UndirectedGraph> components = new ArrayList<>();

    DFSVisitor visitor = new DFSVisitor() {
      Set<Integer> componentVertices = new HashSet<>();

      private void addComponent() {
        int[] vertices = new int[componentVertices.size()];
        int index = 0;
        for (Integer v : componentVertices) {
          vertices[index++] = v;
        }
        components.add((UndirectedGraph) graph.getInducedSubgraph(vertices));
        componentVertices = new HashSet<>();
      }

      @Override
      public void examineRoot(int vertex) {
        if (componentVertices.isEmpty()) {
          componentVertices.add(vertex);
        } else {
          addComponent();
          componentVertices.add(vertex);
        }
      }

      @Override
      public void examineEdge(int source, int target) {
        componentVertices.add(target);
      }

      @Override
      public DFSResult finish(int[] discoverTimes, int[] finishTimes, int[] predecessors) {
        if (!componentVertices.isEmpty()) {
          addComponent();
        }
        return null;
      }
    };

    DFS.search((Graph) graph, visitor);

    return components.toArray(new UndirectedGraph[0]);
  }
}

package graph.algorithms;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import graph.algorithms.DFS.DFSResult;
import graph.algorithms.DFS.DFSVisitor;
import graph.api.Graph;

public class ConnectedComponents {
  public static Graph[] find(Graph graph) {
    List<Graph> components = new ArrayList<>();

    DFSVisitor visitor = new DFSVisitor() {
      Set<Integer> componentVertices = new HashSet<>();

      private void addComponent() {
        int[] vertices = new int[componentVertices.size()];
        int index = 0;
        for (Integer v : componentVertices) {
          vertices[index++] = v;
        }
        components.add(graph.getInducedSubgraph(vertices));
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

    DFS.search(graph, visitor);

    return components.toArray(new Graph[0]);
  }
}

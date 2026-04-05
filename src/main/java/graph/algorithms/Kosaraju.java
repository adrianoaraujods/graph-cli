package graph.algorithms;

import java.util.ArrayList;

import graph.algorithms.DFS.DFSResult;
import graph.algorithms.DFS.DFSVisitor;
import graph.api.DirectedGraph;
import graph.api.Graph;
import graph.util.Sort;

public class Kosaraju {

  public static DirectedGraph[] findSCCs(DirectedGraph graph) {
    DFSResult dfsResult = DFS.search((Graph) graph, new DFSVisitor() {
      @Override
      public DFSResult finish(int[] discoverTimes, int[] finishTimes, int[] predecessors) {
        return new DFSResult(null, finishTimes, null);
      }
    });

    return findSCCs(graph, dfsResult.finishTimes());
  }

  public static DirectedGraph[] findSCCs(DirectedGraph graph, int[] finishTimes) {
    int[] rootsOrder = graph.getVertices();
    Sort.quick(finishTimes, rootsOrder, false);

    DirectedGraph reversedGraph = graph.getReversed();

    ArrayList<ArrayList<Integer>> componentsVerticesList = new ArrayList<>();

    DFSVisitor getComponentsVisitor = new DFSVisitor() {
      @Override
      public void examineRoot(int vertex) {
        ArrayList<Integer> verticesList = new ArrayList<>();
        verticesList.add(vertex);

        componentsVerticesList.add(verticesList);
      }

      @Override
      public void treeEdge(int source, int target) {
        componentsVerticesList.get(componentsVerticesList.size() - 1).add(target);
      }
    };

    DFS.search((Graph) reversedGraph, rootsOrder, getComponentsVisitor);

    DirectedGraph[] components = new DirectedGraph[componentsVerticesList.size()];

    for (int i = 0; i < components.length; i++) {
      int[] vertices = componentsVerticesList.get(i).stream().mapToInt(v -> v).toArray();
      components[i] = (DirectedGraph) graph.getInducedSubgraph(vertices);
    }

    return components;
  }
}

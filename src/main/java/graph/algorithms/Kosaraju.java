package graph.algorithms;

import java.util.ArrayList;

import graph.algorithms.DFS.DFSResult;
import graph.algorithms.DFS.DFSVisitor;
import graph.api.DirectedGraph;
import graph.api.StaticGraph;
import graph.util.Sort;

public class Kosaraju {

  public static StaticGraph[] findSCCs(DirectedGraph graph) {
    DFSResult dfsResult = DFS.search(graph, new DFSVisitor() {
      @Override
      public DFSResult finish(int[] discoverTimes, int[] finishTimes, int[] predecessors) {
        return new DFSResult(null, finishTimes, null);
      }
    });

    return findSCCs(graph, dfsResult.finishTimes());
  }

  public static StaticGraph[] findSCCs(DirectedGraph graph, int[] finishTimes) {
    int[] rootsOrder = graph.getVertices();
    Sort.quick(finishTimes, rootsOrder, false);

    StaticGraph reversedGraph = graph.getReversed();

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

    DFS.search(reversedGraph, rootsOrder, getComponentsVisitor);

    StaticGraph[] components = new StaticGraph[componentsVerticesList.size()];

    for (int i = 0; i < components.length; i++) {
      int[] vertices = componentsVerticesList.get(i).stream().mapToInt(v -> v).toArray();
      components[i] = graph.getInducedSubgraph(vertices);
    }

    return components;
  }
}

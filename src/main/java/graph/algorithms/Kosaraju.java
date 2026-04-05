package graph.algorithms;

import java.util.ArrayList;

import graph.algorithms.DFS.DFSResult;
import graph.algorithms.DFS.DFSVisitor;
import graph.api.DirectedGraph;
import graph.api.Graph;
import graph.util.Sort;

/**
 * Provides Kosaraju's algorithm for finding Strongly Connected Components (SCCs)
 * in a directed graph.
 * <p>
 * Kosaraju's algorithm uses two Depth-First Search passes: first to compute
 * finish times on the original graph, then on the reversed graph to extract SCCs.
 */
public class Kosaraju {

  /**
   * Finds all Strongly Connected Components in the given directed graph.
   * <p>
   * This method performs a complete Kosaraju algorithm execution:
   * <ol>
   *   <li>Runs DFS on the original graph to compute finish times</li>
   *   <li>Transposes the graph (reverses all edges)</li>
   *   <li>Runs DFS on the transposed graph in reverse finish time order</li>
   * </ol>
   *
   * @param graph The directed graph to analyze.
   * @return An array of DirectedGraph, each representing one SCC.
   */
  public static DirectedGraph[] findSCCs(DirectedGraph graph) {
    DFSResult dfsResult = DFS.search((Graph) graph, new DFSVisitor() {
      @Override
      public DFSResult finish(int[] discoverTimes, int[] finishTimes, int[] predecessors) {
        return new DFSResult(null, finishTimes, null);
      }
    });

    return findSCCs(graph, dfsResult.finishTimes());
  }

  /**
   * Finds SCCs using a pre-computed finish times array.
   * <p>
   * This is the second phase of Kosaraju's algorithm: given the finish times
   * from the first DFS pass, it runs DFS on the reversed graph to extract components.
   *
   * @param graph        The original directed graph.
   * @param finishTimes  The finish times from the first DFS pass (used for root ordering).
   * @return An array of DirectedGraph, each representing one SCC.
   */
  public static DirectedGraph[] findSCCs(DirectedGraph graph, int[] finishTimes) {
    int[] rootsOrder = graph.getVertices();
    Sort.quick(finishTimes, rootsOrder, false);

    DirectedGraph reversedGraph = graph.getReversed();

    ArrayList<ArrayList<Integer>> componentsVerticesList = new ArrayList<>();

    DFSVisitor getComponentsVisitor = new DFSVisitor() {
      @Override
      public void examineRoot(int v) {
        ArrayList<Integer> verticesList = new ArrayList<>();
        verticesList.add(v);

        componentsVerticesList.add(verticesList);
      }

      @Override
      public void treeEdge(int v, int w) {
        componentsVerticesList.get(componentsVerticesList.size() - 1).add(w);
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

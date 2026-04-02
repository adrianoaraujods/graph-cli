package graph.api;

import java.util.ArrayList;

import graph.util.Sort;

public interface DirectedGraph extends Graph {

  /**
   * Calculates the in-degree (number of incoming edges) of a given vertex.
   *
   * @param vertex The target vertex to analyze.
   * @return The number of edges pointing to the vertex.
   * @throws IllegalArgumentException if the vertex is less than 1 or greater than
   *                                  total number of vertices (n).
   */
  public abstract int getInDegree(int vertex);

  /**
   * Calculates the out-degree (number of outgoing edges) of a given vertex.
   *
   * @param vertex The target vertex to analyze.
   * @return The number of edges originating from the vertex.
   * @throws IllegalArgumentException if the vertex is less than 1 or greater than
   *                                  total number of vertices (n).
   */
  public abstract int getOutDegree(int vertex);

  /**
   * Retrieves all predecessors (vertices with an edge pointing to the given
   * vertex).
   *
   * @param vertex The target vertex to analyze.
   * @return An array of integers representing the predecessor vertices.
   * @throws IllegalArgumentException if the vertex is less than 1 or greater than
   *                                  total number of vertices (n).
   */
  public abstract int[] getPredecessors(int vertex);

  /**
   * Retrieves all successors (vertices that the given vertex points to).
   *
   * @param vertex The target vertex to analyze.
   * @return An array of integers representing the successor vertices.
   * @throws IllegalArgumentException if the vertex is less than 1 or greater than
   *                                  total number of vertices (n).
   */
  public abstract int[] getSuccessors(int vertex);

  public abstract StaticGraph getReversed();

  /**
   * Uses the Kosaraju Algorithm
   */
  public default StaticGraph[] getMaximalComponents(int[] finishTimes) {
    int[] rootsOrder = getVertices();
    Sort.quick(finishTimes, rootsOrder, false);

    StaticGraph reversedGraph = getReversed();

    ArrayList<ArrayList<Integer>> componentsVerticesList = new ArrayList<>();

    Graph.DFSVisitor getComponentsVisitor = new Graph.DFSVisitor() {
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

    reversedGraph.depthFirstSearch(rootsOrder, getComponentsVisitor);

    StaticGraph[] components = new StaticGraph[componentsVerticesList.size()];

    for (int i = 0; i < components.length; i++) {
      int[] vertices = componentsVerticesList.get(i).stream().mapToInt(v -> v).toArray();
      components[i] = getInducedSubgraph(vertices);
    }

    return components;
  }

  public default StaticGraph[] getMaximalComponents() {
    class DFSFinishTimes implements Graph.DFSVisitor {
      @Override
      public DFSResult finish(int[] discoverTimes, int[] finishTimes, int[] predecessors) {
        return new DFSResult(null, finishTimes, null);
      }
    }

    DFSFinishTimes visitor = new DFSFinishTimes();
    DFSResult dfsResult = depthFirstSearch(visitor);

    return getMaximalComponents(dfsResult.finishTimes());
  }
}

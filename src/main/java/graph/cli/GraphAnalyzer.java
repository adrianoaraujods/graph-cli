package graph.cli;

import java.security.InvalidAlgorithmParameterException;
import java.util.Arrays;
import java.util.Set;

import graph.algorithms.DFS;
import graph.algorithms.DFS.ClassifiedDFSEdges;
import graph.algorithms.DFS.DFSResult;
import graph.algorithms.Fleury;
import graph.algorithms.Fleury.BridgeFinder;
import graph.algorithms.Fleury.EulerianPath;
import graph.algorithms.Kosaraju;
import graph.algorithms.NaiveBridges;
import graph.algorithms.Tarjan;
import graph.api.DirectedGraph;
import graph.api.Edges;
import graph.api.Graph;
import graph.api.UndirectedGraph;

public class GraphAnalyzer {

  public static String runDFS(Graph graph, int target, String outputFile) throws InvalidAlgorithmParameterException {
    if (target < 1 || target > graph.getVerticesCount()) {
      throw new InvalidAlgorithmParameterException(
          "Invalid target: must be between 1 and " + graph.getVerticesCount() + ".");
    }

    StringBuilder sb = new StringBuilder();
    sb.append("\nDepth First Search:\n");
    sb.append("  Target Vertex: ").append(target).append("\n");

    if (graph.isDirected) {
      int[] predecessors = ((DirectedGraph) graph).getPredecessors(target);
      int[] successors = ((DirectedGraph) graph).getSuccessors(target);

      sb.append("  Out degree: ").append(successors.length).append("\n");
      sb.append("  In degree: ").append(predecessors.length).append("\n");
      sb.append("  Successors: ").append(Arrays.toString(successors)).append("\n");
      sb.append("  Predecessors: ").append(Arrays.toString(predecessors)).append("\n");

    } else {
      int[] neighbors = ((UndirectedGraph) graph).getNeighbors(target);

      sb.append("  Degree: ").append(neighbors.length).append("\n");
      sb.append("  Neighbors: ").append(Arrays.toString(neighbors)).append("\n");
    }

    DFSResult dfsResult = DFS.search(graph);
    ClassifiedDFSEdges classifiedEdges = DFS.classifyVertexDFSEdges(graph, target, dfsResult);
    long[] treeEdges = DFS.getDFSTreeEdges(graph, dfsResult.parents());

    sb.append("  Tree Edges: ").append(Edges.toString(treeEdges, graph.isDirected)).append("\n");
    sb.append("  Edges adjacent to vertex ").append(target).append(":\n");
    sb.append("    Tree Edges: ").append(Edges.toString(classifiedEdges.treeEdges(), graph.isDirected))
        .append("\n");
    sb.append("    Back Edges: ").append(Edges.toString(classifiedEdges.backEdges(), graph.isDirected))
        .append("\n");
    sb.append("    Cross Edges: ").append(Edges.toString(classifiedEdges.crossEdges(), graph.isDirected))
        .append("\n");
    sb.append("    Forward Edges: ")
        .append(Edges.toString(classifiedEdges.forwardEdges(), graph.isDirected)).append("\n");

    return sb.toString();
  }

  public static String runKosaraju(Graph graph, String outputFile) {
    StringBuilder sb = new StringBuilder();
    sb.append("\nStrongly Connected Components (Kosaraju):\n");
    sb.append("  Graph Type: ").append(graph.isDirected ? "Directed" : "Undirected").append("\n");

    if (!graph.isDirected) {
      sb.append("  Note: SCC requires directed graph\n");
      return sb.toString();
    }

    DFSResult dfsResult = DFS.search(graph);
    DirectedGraph[] components = Kosaraju.findSCCs((DirectedGraph) graph, dfsResult.finishTimes());

    sb.append("  Component Count: ").append(components.length).append("\n");
    for (int c = 0; c < components.length; c++) {
      sb.append("  [").append(c + 1).append("/").append(components.length).append("] Component:\n");
      sb.append("    Vertices: ").append(Arrays.toString(components[c].getAllVertices())).append("\n");
      sb.append("    Edges: ").append(Edges.toString(components[c].getEdgesSet(), graph.isDirected))
          .append("\n");
    }

    return sb.toString();
  }

  public static String runFleury(Graph graph, String outputFile, BridgeFinder method) {
    StringBuilder sb = new StringBuilder();
    sb.append("\nEulerian Path (Fleury):\n");
    sb.append("  Graph Type: ").append(graph.isDirected ? "Directed" : "Undirected").append("\n");

    EulerianPath eulerianPath = graph.isDirected
        ? Fleury.findEulerianPath((DirectedGraph) graph)
        : Fleury.findEulerianPath((UndirectedGraph) graph, method, true);

    sb.append("  Eulerian Type: ").append(eulerianPath.type()).append("\n");
    sb.append(" Eulerian Path: ").append(Arrays.toString(eulerianPath.path())).append("\n");

    return sb.toString();
  }

  public static String runNaiveBridges(Graph graph, String outputFile) {
    StringBuilder sb = new StringBuilder();
    sb.append("\nBridges (Naive):\n");
    sb.append("  Graph Type: ").append(graph.isDirected ? "Directed" : "Undirected").append("\n");

    Set<Long> bridges = NaiveBridges.findAll(graph);

    sb.append("  Bridge Count: ").append(bridges.size()).append("\n");
    sb.append("  Bridges: ").append(Edges.toString(bridges.stream().mapToLong(l -> l).toArray(), graph.isDirected))
        .append("\n");

    return sb.toString();
  }

  public static String runTarjan(Graph graph, String outputFile) {
    StringBuilder sb = new StringBuilder();
    sb.append("\nBridges (Tarjan):\n");
    sb.append("  Graph Type: ").append(graph.isDirected ? "Directed" : "Undirected").append("\n");

    Set<Long> bridges = Tarjan.findAll((graph));

    sb.append("  Bridge Count: ").append(bridges.size()).append("\n");
    sb.append("  Bridges: ").append(Edges.toString(bridges.stream().mapToLong(l -> l).toArray(), graph.isDirected))
        .append("\n");

    return sb.toString();
  }
}

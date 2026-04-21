package graph.cli;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.InvalidAlgorithmParameterException;
import java.util.Arrays;
import java.util.Set;

import graph.algorithms.DFS;
import graph.algorithms.Fleury;
import graph.algorithms.Fleury.EulerianPath;
import graph.algorithms.Kosaraju;
import graph.algorithms.NaiveBridges;
import graph.algorithms.DFS.ClassifiedDFSEdges;
import graph.algorithms.DFS.DFSResult;
import graph.api.DirectedGraph;
import graph.api.Graph;
import graph.api.UndirectedGraph;
import graph.util.EdgeFormatter;

public class GraphLogger {

    public static void logTime(Runnable block) {
        long start = System.currentTimeMillis();
        block.run();
        System.out.printf(" (✓ %d ms)\n", System.currentTimeMillis() - start);
    }

    public static void writeToFile(String path, String content) throws IOException {
        if (path != null) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(path))) {
                writer.print(content);
            }
        }
    }

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
        int[][] treeEdges = DFS.getDFSTreeEdges(graph, dfsResult.parents());

        sb.append("  Tree Edges: ").append(EdgeFormatter.toString(treeEdges, graph.isDirected)).append("\n");
        sb.append("  Edges adjacent to vertex ").append(target).append(":\n");
        sb.append("    Tree Edges: ").append(EdgeFormatter.toString(classifiedEdges.treeEdges(), graph.isDirected))
                .append("\n");
        sb.append("    Back Edges: ").append(EdgeFormatter.toString(classifiedEdges.backEdges(), graph.isDirected))
                .append("\n");
        sb.append("    Cross Edges: ").append(EdgeFormatter.toString(classifiedEdges.crossEdges(), graph.isDirected))
                .append("\n");
        sb.append("    Forward Edges: ")
                .append(EdgeFormatter.toString(classifiedEdges.forwardEdges(), graph.isDirected)).append("\n");

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
            sb.append("    Vertices: ").append(Arrays.toString(components[c].getVertices())).append("\n");
            sb.append("    Edges: ").append(EdgeFormatter.toString(components[c].getEdgesSet(), graph.isDirected))
                    .append("\n");
        }

        return sb.toString();
    }

    public static String runFleury(Graph graph, String outputFile) {
        StringBuilder sb = new StringBuilder();
        sb.append("\nEulerian Path (Fleury):\n");
        sb.append("  Graph Type: ").append(graph.isDirected ? "Directed" : "Undirected").append("\n");

        EulerianPath eulerianPath = Fleury.findEulerianPath(graph);
        sb.append("  Eulerian Type: ").append(eulerianPath.type()).append("\n");
        sb.append("  Eulerian Path: ").append(Arrays.toString(eulerianPath.path())).append("\n");

        return sb.toString();
    }

    public static String runNaiveBridges(Graph graph, String outputFile) {
        StringBuilder sb = new StringBuilder();
        sb.append("\nBridges (Naive):\n");
        sb.append("  Graph Type: ").append(graph.isDirected ? "Directed" : "Undirected").append("\n");

        Set<int[]> bridges;
        if (graph.isDirected) {
            bridges = NaiveBridges.findAllWeak((DirectedGraph) graph);
        } else {
            bridges = NaiveBridges.findAll((UndirectedGraph) graph);
        }

        sb.append("  Bridge Count: ").append(bridges.size()).append("\n");
        sb.append("  Bridges: ").append(EdgeFormatter.toString(bridges.toArray(new int[0][]), graph.isDirected))
                .append("\n");

        return sb.toString();
    }
}
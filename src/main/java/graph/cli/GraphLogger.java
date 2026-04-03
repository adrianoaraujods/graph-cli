package graph.cli;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.function.Supplier;

import graph.algorithms.DFS;
import graph.algorithms.Kosaraju;
import graph.algorithms.DFS.ClassifiedDFSEdges;
import graph.algorithms.DFS.DFSResult;
import graph.api.DirectedGraph;
import graph.api.EdgeSet;
import graph.api.StaticGraph;
import graph.api.UndirectedGraph;

public class GraphLogger {

    public static <T> T logTime(Supplier<T> block) {
        long start = System.currentTimeMillis();
        T result = block.get();
        System.out.printf(" (✓ %d ms)\n", System.currentTimeMillis() - start);
        return result;
    }

    public static void logTime(Runnable block) {
        long start = System.currentTimeMillis();
        block.run();
        System.out.printf(" (✓ %d ms)\n", System.currentTimeMillis() - start);
    }

    public static String defaultLogPath(String graphPath) {
        int dotIndex = graphPath.lastIndexOf('.');
        if (dotIndex > 0) {
            return graphPath.substring(0, dotIndex) + ".log";
        }
        return graphPath + ".log";
    }

    public static void writeLog(String path, StaticGraph graph, int target) throws IOException {
        int[] neighbors = null;
        int[] predecessors = null;
        int[] successors = null;
        if (graph.isDirected) {
            predecessors = ((DirectedGraph) graph).getPredecessors(target);
            successors = ((DirectedGraph) graph).getSuccessors(target);
        } else {
            neighbors = ((UndirectedGraph) graph).getNeighbors(target);
        }

        DFSResult dfsResult = DFS.search(graph);

        StaticGraph[] components = null;
        if (graph.isDirected) {
            components = Kosaraju.findSCCs((DirectedGraph) graph, dfsResult.finishTimes());
        }

        StringBuilder logContent = new StringBuilder();

        logContent.append(String.format("\nTarget vertex %d details:\n", target));
        if (graph.isDirected) {
            logContent.append(String.format("  Out degree: %d\n", successors.length));
            logContent.append(String.format("  In degree: %d\n", predecessors.length));
            logContent.append(String.format("  Successors: %s\n", Arrays.toString(successors)));
            logContent.append(String.format("  Predecessors: %s\n", Arrays.toString(predecessors)));
        } else {
            logContent.append(String.format("  Degree: %d\n", neighbors.length));
            logContent.append(String.format("  Neighbors: %s\n", Arrays.toString(neighbors)));
        }

        ClassifiedDFSEdges classifiedEdges = DFS.classifyVertexDFSEdges(graph, target, dfsResult);
        EdgeSet treeEdges = DFS.getDFSTreeEdges(graph, dfsResult.parents());

        logContent.append("\nDepth First Search:\n");
        logContent.append(String.format("  Tree Edges: %s\n", treeEdges.toString()));
        logContent.append("\n");
        logContent.append(String.format("  Edges adjacent to vertex %d:\n", target));
        logContent.append(String.format("    Tree Edges: %s\n", classifiedEdges.treeEdges()));
        logContent.append(String.format("    Back Edges: %s\n", classifiedEdges.backEdges()));
        logContent.append(String.format("    Cross Edges: %s\n", classifiedEdges.crossEdges()));
        logContent.append(String.format("    Forward Edges: %s\n", classifiedEdges.forwardEdges()));

        if (graph.isDirected) {
            logContent.append("\nComponents Trees:\n");
            for (int c = 0; c < components.length; c++) {
                logContent.append(String.format("[%d/%d] Component:\n", c + 1, components.length));
                logContent.append(String.format("  Vertices: %s\n", Arrays.toString(components[c].getVertices())));
                logContent.append(String.format("  Edges: %s\n\n",
                        components[c].getEdgesSet(graph.isDirected, components[c].n).toString()));
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            writer.write(logContent.toString());
        }
    }
}

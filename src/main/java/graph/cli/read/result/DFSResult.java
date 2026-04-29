package graph.cli.read.result;

import graph.algorithms.DFS.ClassifiedDFSEdges;
import graph.api.Edges;

public record DFSResult(
        int target,
        int outDegree,
        int inDegree,
        int[] successors,
        int[] predecessors,
        long[] treeEdges,
        ClassifiedDFSEdges classifiedEdges) implements AlgorithmResult {

    @Override
    public String toString() {
        boolean isDirected = inDegree != -1;
        StringBuilder sb = new StringBuilder();
        sb.append("\nDepth First Search:\n");
        sb.append("  Target Vertex: ").append(target).append("\n");

        if (isDirected) {
            sb.append("  Out degree: ").append(outDegree).append("\n");
            sb.append("  In degree: ").append(inDegree).append("\n");
            sb.append("  Successors: ").append(java.util.Arrays.toString(successors)).append("\n");
            sb.append("  Predecessors: ").append(java.util.Arrays.toString(predecessors)).append("\n");
        } else {
            sb.append("  Degree: ").append(outDegree).append("\n");
            sb.append("  Neighbors: ").append(java.util.Arrays.toString(successors)).append("\n");
        }

        sb.append("  Tree Edges: ").append(Edges.toString(treeEdges, isDirected)).append("\n");
        sb.append("  Edges adjacent to vertex ").append(target).append(":\n");
        sb.append("    Tree Edges: ").append(Edges.toString(classifiedEdges.treeEdges(), isDirected)).append("\n");
        sb.append("    Back Edges: ").append(Edges.toString(classifiedEdges.backEdges(), isDirected)).append("\n");
        sb.append("    Cross Edges: ").append(Edges.toString(classifiedEdges.crossEdges(), isDirected)).append("\n");
        sb.append("    Forward Edges: ").append(Edges.toString(classifiedEdges.forwardEdges(), isDirected))
                .append("\n");
        return sb.toString();
    }
}

package graph.cli.read.result;

import graph.api.DirectedGraph;
import graph.api.Edges;

public record SCCResult(
        DirectedGraph[] components) implements AlgorithmResult {

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\nStrongly Connected Components (Kosaraju):\n");
        sb.append("  Graph Type: Directed\n");
        sb.append("  Component Count: ").append(components.length).append("\n");
        for (int c = 0; c < components.length; c++) {
            sb.append("  [").append(c + 1).append("/").append(components.length).append("] Component:\n");
            sb.append("    Vertices: ").append(java.util.Arrays.toString(components[c].getAllVertices())).append("\n");
            sb.append("    Edges: ").append(Edges.toString(components[c].getEdgesSet(), true)).append("\n");
        }
        return sb.toString();
    }
}

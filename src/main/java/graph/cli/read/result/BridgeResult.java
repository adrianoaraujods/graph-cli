package graph.cli.read.result;

import graph.api.Edges;

public record BridgeResult(
        String algorithmName,
        java.util.Set<Long> bridges) implements AlgorithmResult {

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String label = algorithmName.equals("--tarjan") ? "Bridges (Tarjan)" : "Bridges (Naive)";
        sb.append("\n").append(label).append(":\n");
        sb.append("  Graph Type: Undirected\n");
        sb.append("  Bridge Count: ").append(bridges.size()).append("\n");
        sb.append("  Bridges: ").append(Edges.toString(bridges.stream().mapToLong(l -> l).toArray(), false))
                .append("\n");
        return sb.toString();
    }
}

package graph.cli.read.result;

import java.util.Arrays;

import graph.algorithms.Fleury.EulerianPath;

public record EulerianResult(
        EulerianPath path,
        String bridgeFinderName) implements AlgorithmResult {

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\nEulerian Path (Fleury)");
        if (bridgeFinderName != null) {
            sb.append(" with ").append(bridgeFinderName);
        }
        sb.append(":\n");
        sb.append("  Eulerian Type: ").append(path.type()).append("\n");
        // sb.append(" Eulerian Path:
        // ").append(Arrays.toString(path.path())).append("\n");
        return sb.toString();
    }
}

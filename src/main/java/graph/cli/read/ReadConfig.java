package graph.cli.read;

import java.util.List;

public record ReadConfig(
        String graphPath,
        String representation,
        boolean isDirected,
        List<AlgorithmRequest> algorithms,
        String outputPath) {
}

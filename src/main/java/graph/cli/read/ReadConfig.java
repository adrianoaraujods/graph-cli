package graph.cli.read;

import java.util.List;

public record ReadConfig(
        String graphPath,
        String representation,
        boolean isDirected,
        boolean isWeighted,
        boolean hasCapacity,
        List<AlgorithmRequest> algorithms,
        String outputPath) {
}

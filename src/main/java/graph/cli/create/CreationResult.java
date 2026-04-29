package graph.cli.create;

public record CreationResult(
        long edgesCreated,
        double actualDensity,
        String outputPath) {
}

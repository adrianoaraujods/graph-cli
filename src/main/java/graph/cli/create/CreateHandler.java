package graph.cli.create;

public class CreateHandler {

    public static CreationResult run(CreateConfig config) throws Exception {
        System.out.printf("\nGraph Configuration:\n");
        System.out.printf("  Subcommand: create\n");
        System.out.printf("  Graph File Path: %s\n", config.graphPath());
        System.out.printf("  Direction Type: %s\n", config.isDirected() ? "Directed" : "Undirected");

        GraphGenerator generator = new GraphGenerator(config.vertices(), config.isDirected(), config.graphPath());
        generator.setConnectivity(config.connectivity());

        if (config.edges() >= 0) {
            generator.setEdges(config.edges());
        } else {
            generator.setDensity(config.density());
        }

        if (config.seed() != null) {
            generator.setSeed(config.seed());
            System.out.printf("  Seed: %d\n", config.seed());
        }

        System.out.printf("  Connectivity: %s\n", config.connectivity().toString().toLowerCase());
        System.out.printf("  Vertices: %,d\n", config.vertices());
        System.out.printf("  Edges: %,d\n", generator.getEdges());
        System.out.printf("  Density: %.2f\n", generator.getDensity());
        System.out.printf("  Max Edges: %,d\n", generator.maxEdges());

        System.out.printf("\n[Info] Generating graph with %,d edges...", config.edges());
        long createdEdges;
        try {
            createdEdges = generator.create();
        } catch (Exception e) {
            System.err.printf("[Error] An error occurred while generating the graph: " + e.getMessage());
            throw e;
        }

        if (createdEdges != config.edges()) {
            System.out.printf("\n[Warning] Graph was generated with %,d edges instead of %,d targeted.",
                    createdEdges, config.edges());
        }

        System.out.printf("\nGraph successfully generated! File available at: %s\n", config.graphPath());

        return new CreationResult(createdEdges, generator.getDensity(), config.graphPath());
    }
}

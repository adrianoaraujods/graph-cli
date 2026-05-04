package graph.cli.create;

import graph.api.ConnectivityType;

public record CreateConfig(
        String graphPath,
        int vertices,
        long edges,
        double density,
        Long seed,
        ConnectivityType connectivity,
        boolean isDirected,
        Integer minWeight,
        Integer maxWeight) {
}

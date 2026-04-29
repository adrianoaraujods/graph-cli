package graph.util;

import java.util.function.Supplier;
import graph.api.Graph;
import graph.representations.GraphBuilder;

public class GraphTestHelper {

    public static Graph build(Supplier<GraphBuilder> builderSupplier, int[][] edges) {
        int n = inferVertexCount(edges);
        long m = edges.length;

        GraphBuilder builder = builderSupplier.get();
        builder.initialize(n, m);

        for (int[] edge : edges) {
            builder.addEdge(edge[0], edge[1]);
        }

        return builder.build();
    }

    public static Graph build(Supplier<GraphBuilder> builderSupplier, int n, int[][] edges) {
        long m = edges.length;

        GraphBuilder builder = builderSupplier.get();
        builder.initialize(n, m);

        for (int[] edge : edges) {
            builder.addEdge(edge[0], edge[1]);
        }

        return builder.build();
    }

    private static int inferVertexCount(int[][] edges) {
        int maxVertex = 0;

        for (int[] edge : edges) {
            if (edge[0] > maxVertex)
                maxVertex = edge[0];
            if (edge[1] > maxVertex)
                maxVertex = edge[1];
        }

        return maxVertex;
    }
}

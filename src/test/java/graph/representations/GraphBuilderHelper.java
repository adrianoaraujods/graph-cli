package graph.representations;

import java.util.function.Supplier;

import graph.api.DirectedGraph;
import graph.api.StaticGraph;
import graph.api.UndirectedGraph;

public class GraphBuilderHelper {

    public static StaticGraph build(Supplier<GraphBuilder> builderSupplier, int n, int m, int[][] edges) {
        GraphBuilder builder = builderSupplier.get();
        builder.initialize(n, m);

        for (int[] edge : edges) {
            builder.addEdge(edge[0], edge[1]);
        }

        return builder.build();
    }

    public static StaticGraph build(Supplier<GraphBuilder> builderSupplier, int n, int m, int[][] edges,
            int[] vertices) {
        GraphBuilder builder = builderSupplier.get();
        builder.initialize(n, m, vertices);

        for (int[] edge : edges) {
            builder.addEdge(edge[0], edge[1]);
        }

        return builder.build();
    }

    public static StaticGraph buildUndirected(Supplier<GraphBuilder> builderSupplier, int n, int m, int[][] edges) {
        GraphBuilder builder = builderSupplier.get();
        builder.initialize(n, m);

        for (int[] edge : edges) {
            builder.addEdge(edge[0], edge[1]);
        }

        return builder.build();
    }

    public static boolean hasEdge(DirectedGraph graph, int source, int target) {
        int[] successors = graph.getSuccessors(source);
        for (int s : successors) {
            if (s == target) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasNeighbor(UndirectedGraph graph, int source, int target) {
        int[] neighbors = graph.getNeighbors(source);
        for (int n : neighbors) {
            if (n == target) {
                return true;
            }
        }
        return false;
    }
}
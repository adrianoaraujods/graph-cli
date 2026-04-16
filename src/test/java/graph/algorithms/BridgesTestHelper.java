package graph.algorithms;

import graph.api.Graph;
import graph.representations.GraphBuilder;
import graph.representations.adjacencymatrix.AdjacencyMatrixGraphBuilder;

import java.util.HashSet;
import java.util.Set;

public class BridgesTestHelper {

    public static Graph buildUndirected(int n, int m, int[][] edges) {
        GraphBuilder builder = new AdjacencyMatrixGraphBuilder(false);
        builder.initialize(n, m);
        for (int[] edge : edges) {
            builder.addEdge(edge[0], edge[1]);
        }
        return builder.build();
    }

    public static Set<int[]> toEdgeSet(int[][] edges) {
        Set<int[]> set = new HashSet<>();
        for (int[] edge : edges) {
            set.add(new int[] { Math.min(edge[0], edge[1]), Math.max(edge[0], edge[1]) });
        }
        return set;
    }

    public static boolean edgeSetsEqual(Set<int[]> expected, Set<int[]> actual) {
        return expected.equals(actual);
    }

    public static int[][] emptyGraph() {
        return new int[0][0];
    }

    public static int[][] singleEdge() {
        return new int[][] { { 1, 2 } };
    }

    public static int[][] linearChain() {
        return new int[][] { { 1, 2 }, { 2, 3 }, { 3, 4 }, { 4, 5 } };
    }

    public static int[][] simpleCycle() {
        return new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 } };
    }

    public static int[][] graphWithOneBridge() {
        return new int[][] { { 1, 2 }, { 2, 3 }, { 3, 4 }, { 2, 5 }, { 5, 6 } };
    }

    public static int[][] multipleBridges() {
        return new int[][] { { 1, 2 }, { 2, 3 }, { 3, 4 }, { 4, 5 }, { 3, 6 }, { 6, 7 } };
    }

    public static int[][] disconnectedGraph() {
        return new int[][] { { 1, 2 }, { 3, 4 }, { 5, 6 } };
    }

    public static int[][] tree() {
        return new int[][] { { 1, 2 }, { 1, 3 }, { 2, 4 }, { 2, 5 } };
    }

    public static int[][] graphWithTwoCycles() {
        return new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 }, { 3, 4 }, { 4, 5 }, { 5, 3 } };
    }

    public static int[][] bridgeInMiddle() {
        return new int[][] {
                { 1, 2 }, { 2, 3 }, { 1, 3 },
                { 3, 4 },
                { 4, 5 }, { 5, 6 }, { 6, 4 }
        };
    }
}
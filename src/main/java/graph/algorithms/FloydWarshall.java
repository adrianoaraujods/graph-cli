package graph.algorithms;

import graph.api.DirectedGraph;
import graph.api.Graph;
import graph.api.UndirectedGraph;
import graph.api.WeightedGraph;
import graph.cli.read.result.AllPairsShortestPathResult;

public class FloydWarshall {

    public static AllPairsShortestPathResult compute(WeightedGraph graph) {
        int n = graph.getVerticesCount();
        int[][] dist = new int[n][n];
        int[][] next = new int[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                dist[i][j] = (i == j) ? 0 : Integer.MAX_VALUE;
                next[i][j] = -1;
            }
        }

        WeightedGraph.WeightedEdges edges = graph.getWeightedEdgesSet();
        long[] edgeList = edges.edges();
        int[] weights = edges.weights();
        for (int e = 0; e < edgeList.length; e++) {
            int v = (int) (edgeList[e] >> 32);
            int w = (int) (edgeList[e] & 0xFFFFFFFFL);
            dist[v - 1][w - 1] = weights[e];
            next[v - 1][w - 1] = w - 1;
            if (!((Graph) graph).isDirected) {
                dist[w - 1][v - 1] = weights[e];
                next[w - 1][v - 1] = v - 1;
            }
        }

        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                if (dist[i][k] == Integer.MAX_VALUE) continue;
                for (int j = 0; j < n; j++) {
                    if (dist[k][j] == Integer.MAX_VALUE) continue;
                    long newDist = (long) dist[i][k] + (long) dist[k][j];
                    if (newDist < dist[i][j]) {
                        dist[i][j] = (int) newDist;
                        next[i][j] = next[i][k];
                    }
                }
            }
        }

        for (int i = 0; i < n; i++) {
            if (dist[i][i] < 0) {
                throw new IllegalArgumentException(
                        "Graph contains a negative cycle, which Floyd-Warshall cannot handle");
            }
        }

        return new AllPairsShortestPathResult(dist, next, n);
    }
}

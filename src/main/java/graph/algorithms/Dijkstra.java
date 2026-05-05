package graph.algorithms;

import java.util.Arrays;
import java.util.PriorityQueue;

import graph.api.WeightedGraph;
import graph.cli.read.result.ShortestPathResult;

public class Dijkstra {

    public static ShortestPathResult compute(WeightedGraph graph, int source, Integer target, boolean findPath) {
        // Validate no negative weights
        WeightedGraph.WeightedEdges weightedEdges = graph.getWeightedEdgesSet();
        for (int weight : weightedEdges.weights()) {
            if (weight < 0) {
                throw new IllegalArgumentException(
                        "Graph contains negative weight edges, which Dijkstra's algorithm cannot handle");
            }
        }

        int n = graph.getVerticesCount();
        int[] distances = new int[n];
        int[] parents = new int[n];

        Arrays.fill(distances, Integer.MAX_VALUE);
        Arrays.fill(parents, -1);

        distances[source - 1] = 0;

        PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> Integer.compare(a[1], b[1]));
        pq.offer(new int[] { source - 1, 0 });

        while (!pq.isEmpty()) {
            int[] current = pq.poll();
            int u = current[0];
            int distU = current[1];

            if (distU > distances[u])
                continue;

            int[] vertices = graph.getVertices();
            for (int v : vertices) {
                if (v == u + 1)
                    continue;
                try {
                    int weight = graph.getEdgeWeight(u + 1, v);
                    int newDist = distU + weight;
                    if (newDist < distances[v - 1]) {
                        distances[v - 1] = newDist;
                        parents[v - 1] = u;
                        pq.offer(new int[] { v - 1, newDist });
                    }
                } catch (IllegalArgumentException e) {
                    // no edge
                }
            }
        }

        return new ShortestPathResult(distances, parents, source, null, false);
    }
}

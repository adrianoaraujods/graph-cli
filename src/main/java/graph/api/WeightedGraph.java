package graph.api;

/**
 * Interface for graphs that have weighted edges.
 */
public interface WeightedGraph extends GraphBase {

    /**
     * Returns the weight of the edge from v to w.
     *
     * @param v The source vertex.
     * @param w The target vertex.
     * @return The weight of the edge.
     * @throws IllegalArgumentException if the edge doesn't exist.
     */
    int getEdgeWeight(int v, int w);

    /**
     * Returns an array of all edge weights.
     * The order corresponds to the edges returned by getEdgesSet().
     *
     * @return Array of edge weights.
     */
    int[] getWeightsSet();

    /**
     * A record containing both edges and their weights.
     *
     * @param edges The packed edges as long[].
     * @param weights The corresponding weights as int[].
     */
    record WeightedEdges(long[] edges, int[] weights) {}

    /**
     * Returns both the edges and their weights in aligned arrays.
     *
     * @return A record containing both edges and weights.
     */
    WeightedEdges getWeightedEdgesSet();
}

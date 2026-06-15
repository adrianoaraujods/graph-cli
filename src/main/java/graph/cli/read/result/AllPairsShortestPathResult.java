package graph.cli.read.result;

public record AllPairsShortestPathResult(int[][] distances, int[][] next, int verticesCount)
        implements AlgorithmResult {

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\nFloyd-Warshall All-Pairs Shortest Path:\n");
        sb.append("  Distance Matrix:\n");
        for (int i = 0; i < verticesCount; i++) {
            sb.append("    ");
            for (int j = 0; j < verticesCount; j++) {
                if (j > 0) sb.append(", ");
                if (distances[i][j] == Integer.MAX_VALUE) {
                    sb.append("INF");
                } else {
                    sb.append(distances[i][j]);
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}

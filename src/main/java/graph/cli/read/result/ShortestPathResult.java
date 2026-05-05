package graph.cli.read.result;

public record ShortestPathResult(int[] distances, int[] parents, int source, Integer target, boolean showPath)
        implements AlgorithmResult {

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\nDijkstra Shortest Path:\n");
        sb.append("  Source: ").append(source).append("\n");
        sb.append("  Distances: [");
        for (int i = 0; i < distances.length; i++) {
            if (i > 0)
                sb.append(", ");
            if (distances[i] == Integer.MAX_VALUE) {
                sb.append("INF");
            } else {
                sb.append(distances[i]);
            }
        }
        sb.append("]\n");

        if (showPath) {
            if (target != null) {
                // Show only path to target
                sb.append("  Path from ").append(source).append(" to ").append(target).append(":\n");
                if (distances[target - 1] == Integer.MAX_VALUE) {
                    sb.append("    INF (unreachable)\n");
                } else {
                    StringBuilder path = new StringBuilder();
                    int current = target - 1;
                    while (current != -1) {
                        if (path.length() > 0)
                            path.insert(0, " -> ");
                        path.insert(0, current + 1);
                        current = parents[current];
                    }
                    sb.append("    ").append(path).append("\n");
                }
            } else {
                // Show all paths
                sb.append("  Paths from source:\n");
                for (int i = 0; i < parents.length; i++) {
                    if (i == source - 1)
                        continue; // skip source
                    if (distances[i] == Integer.MAX_VALUE) {
                        sb.append("    Vertex ").append(i + 1).append(": INF\n");
                    } else {
                        StringBuilder path = new StringBuilder();
                        int current = i;
                        while (current != -1) {
                            if (path.length() > 0)
                                path.insert(0, " -> ");
                            path.insert(0, current + 1);
                            current = parents[current];
                        }
                        sb.append("    Vertex ").append(i + 1).append(": ").append(path).append("\n");
                    }
                }
            }
        }

        return sb.toString();
    }
}

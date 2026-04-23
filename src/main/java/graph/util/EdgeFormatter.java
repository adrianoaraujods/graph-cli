package graph.util;

public class EdgeFormatter {

    public static String toString(int[][] edges, boolean isDirected) {
        if (edges == null || edges.length == 0) {
            return isDirected ? "()" : "{}";
        }

        StringBuilder builder = new StringBuilder("{");
        String prefix = isDirected ? "(" : "{";
        String suffix = isDirected ? ")" : "}";

        for (int i = 0; i < edges.length; i++) {
            if (i > 0) {
                builder.append(", ");
            }

            builder.append(prefix);
            builder.append(edges[i][0]);
            builder.append(", ");
            builder.append(edges[i][1]);
            builder.append(suffix);
        }

        builder.append("}");

        return builder.toString();
    }

    /**
     * Creates a consistent edge key `v-w` where the smaller vertex is always first.
     */
    public static String toKey(int v, int w) {
        return v < w ? (v + "-" + w) : (w + "-" + v);
    }
}

package graph.util;

public class EdgeFormatter {

    /**
     * Packs two integer vertices into a single 64-bit long.
     * Automatically normalizes the edge (v < w) so direction doesn't matter.
     *
     * @param v The first vertex
     * @param w The second vertex
     * @return A 64-bit long representing the undirected edge
     */
    public static long undirected(int v, int w) {
        // Normalize: smaller vertex always goes first
        int min = Math.min(v, w);
        int max = Math.max(v, w);

        // Shift 'min' to the upper 32 bits.
        // Use bitwise AND with 0xFFFFFFFFL on 'max' to prevent sign-extension
        // in case you are working with negative vertex IDs.
        return ((long) min << 32) | (max & 0xFFFFFFFFL);
    }

    /**
     * Extracts the smaller vertex (from the upper 32 bits).
     */
    public static int getV(long edge) {
        return (int) (edge >>> 32); // Unsigned right shift
    }

    /**
     * Extracts the larger vertex (from the lower 32 bits).
     */
    public static int getW(long edge) {
        return (int) edge; // Casting to int naturally truncates the upper 32 bits
    }

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

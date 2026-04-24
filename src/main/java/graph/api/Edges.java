package graph.api;

public class Edges {

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
     * Packs two integer vertices into a single 64-bit long.
     *
     * @param v The first vertex
     * @param w The second vertex
     * @return A 64-bit long representing the undirected edge
     */
    public static long directed(int v, int w) {
        return ((long) v << 32) | (w & 0xFFFFFFFFL);
    }

    /**
     * Extracts the source vertex (from the upper 32 bits).
     */
    public static int getSource(long edge) {
        return (int) (edge >>> 32); // Unsigned right shift
    }

    /**
     * Extracts the target vertex (from the lower 32 bits).
     */
    public static int getTarget(long edge) {
        return (int) edge; // Casting to int naturally truncates the upper 32 bits
    }

    public static String toString(long[] edges, boolean isDirected) {
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
            builder.append(getSource(edges[i]));
            builder.append(", ");
            builder.append(getTarget(edges[i]));
            builder.append(suffix);
        }

        builder.append("}");

        return builder.toString();
    }
}

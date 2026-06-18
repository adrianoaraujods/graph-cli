package graph.util;

public class KCenterUtils {

    public static final int INF = Integer.MAX_VALUE / 2;

    public static int evaluateRadius(int[][] dist, int n, int k, int[] centers) {
        int radius = 0;
        for (int i = 0; i < n; i++) {
            int minD = INF;
            for (int c = 0; c < k; c++) {
                if (dist[i][centers[c]] < minD) {
                    minD = dist[i][centers[c]];
                }
            }
            if (minD > radius) {
                radius = minD;
            }
        }
        return radius;
    }
}

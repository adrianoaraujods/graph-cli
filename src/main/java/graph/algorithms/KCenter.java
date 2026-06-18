package graph.algorithms;

import java.util.Arrays;
import java.util.Random;

public class KCenter {

    private static final int INF = Integer.MAX_VALUE / 2;

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

    public static int[] solveGonzalez(int[][] dist, int n, int k) {
        int[] outCenters = new int[k];
        int[] minDist = new int[n];
        Arrays.fill(minDist, INF);

        outCenters[0] = 0;
        minDist[0] = 0;
        for (int i = 1; i < n; i++) {
            minDist[i] = dist[i][0];
        }

        for (int c = 1; c < k; c++) {
            int farthest = -1;
            int maxD = -1;
            for (int i = 0; i < n; i++) {
                if (minDist[i] > maxD) {
                    maxD = minDist[i];
                    farthest = i;
                }
            }
            outCenters[c] = farthest;
            for (int i = 0; i < n; i++) {
                if (dist[i][farthest] < minDist[i]) {
                    minDist[i] = dist[i][farthest];
                }
            }
        }
        return outCenters;
    }

    public static int[] solveFastMapKMeans(int[][] dist, int n, int k, long seed) {
        int[] outCenters = new int[k];
        double[] coords = new double[n * 2];
        Random rand = new Random(seed);

        for (int dim = 0; dim < 2; dim++) {
            int o1 = 0;
            int o2 = 0;
            double maxD = -1;

            for (int i = 0; i < n; i++) {
                double d = (dim == 0) ? dist[o1][i]
                        : Math.sqrt(
                                Math.pow(dist[o1][i], 2)
                                        - Math.pow(coords[o1 * 2 + dim - 1] - coords[i * 2 + dim - 1], 2));
                if (Double.isNaN(d))
                    d = 0;

                if (d > maxD) {
                    maxD = d;
                    o2 = i;
                }
            }

            double bigD = (dim == 0) ? dist[o1][o2]
                    : Math.sqrt(
                            Math.pow(dist[o1][o2], 2)
                                    - Math.pow(coords[o1 * 2 + dim - 1] - coords[o2 * 2 + dim - 1], 2));
            if (Double.isNaN(bigD))
                bigD = 0;

            if (bigD == 0)
                continue;

            for (int i = 0; i < n; i++) {
                double dO1i = (dim == 0) ? dist[o1][i]
                        : Math.sqrt(
                                Math.pow(dist[o1][i], 2)
                                        - Math.pow(coords[o1 * 2 + dim - 1] - coords[i * 2 + dim - 1], 2));
                if (Double.isNaN(dO1i))
                    dO1i = 0;

                double dO2i = (dim == 0) ? dist[o2][i]
                        : Math.sqrt(
                                Math.pow(dist[o2][i], 2)
                                        - Math.pow(coords[o2 * 2 + dim - 1] - coords[i * 2 + dim - 1], 2));
                if (Double.isNaN(dO2i))
                    dO2i = 0;

                coords[i * 2 + dim] = (Math.pow(dO1i, 2) + Math.pow(bigD, 2) - Math.pow(dO2i, 2)) / (2.0 * bigD);
            }
        }

        outCenters[0] = rand.nextInt(n);
        double[] minDistSq = new double[n];

        for (int i = 0; i < n; i++) {
            double dx = coords[i * 2] - coords[outCenters[0] * 2];
            double dy = coords[i * 2 + 1] - coords[outCenters[0] * 2 + 1];
            minDistSq[i] = dx * dx + dy * dy;
        }

        for (int c = 1; c < k; c++) {
            double sum = 0;
            for (int i = 0; i < n; i++) {
                sum += minDistSq[i];
            }

            double r = (sum == 0) ? 0 : rand.nextDouble() * sum;
            double acc = 0;
            int next = -1;

            for (int i = 0; i < n; i++) {
                acc += minDistSq[i];
                if (acc >= r) {
                    next = i;
                    break;
                }
            }
            if (next == -1)
                next = rand.nextInt(n);
            outCenters[c] = next;

            for (int i = 0; i < n; i++) {
                double dx = coords[i * 2] - coords[next * 2];
                double dy = coords[i * 2 + 1] - coords[next * 2 + 1];
                double dSq = dx * dx + dy * dy;
                if (dSq < minDistSq[i]) {
                    minDistSq[i] = dSq;
                }
            }
        }

        return outCenters;
    }

    private static void localSearchSwap(int[][] dist, int n, int k, int[] centers, int[] currentRadiusObj) {
        boolean improved = true;
        while (improved) {
            improved = false;
            for (int c = 0; c < k; c++) {
                int orig = centers[c];
                for (int v = 0; v < n; v++) {
                    boolean isCenter = false;
                    for (int j = 0; j < k; j++) {
                        if (centers[j] == v) {
                            isCenter = true;
                            break;
                        }
                    }
                    if (isCenter)
                        continue;

                    centers[c] = v;
                    int newR = evaluateRadius(dist, n, k, centers);
                    if (newR < currentRadiusObj[0]) {
                        currentRadiusObj[0] = newR;
                        improved = true;
                        break;
                    }
                    centers[c] = orig;
                }
                if (improved)
                    break;
            }
        }
    }

    public static int[] solveWvaIg(int[][] dist, int n, int k, long seed) {
        Random rand = new Random(seed);
        int[] currentC = solveGonzalez(dist, n, k);
        int[] bestC = Arrays.copyOf(currentC, k);
        int[] tempC = new int[k + 1];

        int currentR = evaluateRadius(dist, n, k, currentC);
        int bestR = currentR;

        int stuckCount = 0;
        int maxIters = 200;

        for (int iter = 0; iter < maxIters; iter++) {
            int vStar = -1;
            int maxD = -1;

            for (int i = 0; i < n; i++) {
                int minD = INF;
                for (int c = 0; c < k; c++) {
                    if (dist[i][currentC[c]] < minD) {
                        minD = dist[i][currentC[c]];
                    }
                }
                if (minD > maxD) {
                    maxD = minD;
                    vStar = i;
                }
            }

            System.arraycopy(currentC, 0, tempC, 0, k);
            tempC[k] = vStar;

            int bestPruneR = INF;
            int bestPruneIdx = 0;

            for (int c = 0; c <= k; c++) {
                int tempR = 0;
                for (int i = 0; i < n; i++) {
                    int minD = INF;
                    for (int j = 0; j <= k; j++) {
                        if (j == c)
                            continue;
                        if (dist[i][tempC[j]] < minD) {
                            minD = dist[i][tempC[j]];
                        }
                    }
                    if (minD > tempR)
                        tempR = minD;
                }
                if (tempR < bestPruneR) {
                    bestPruneR = tempR;
                    bestPruneIdx = c;
                }
            }

            int idx = 0;
            for (int c = 0; c <= k; c++) {
                if (c == bestPruneIdx)
                    continue;
                currentC[idx++] = tempC[c];
            }
            currentR = bestPruneR;

            int[] rObj = new int[] { currentR };
            localSearchSwap(dist, n, k, currentC, rObj);
            currentR = rObj[0];

            if (stuckCount >= 3) {
                int vStar2 = -1;
                maxD = -1;
                for (int i = 0; i < n; i++) {
                    if (i == vStar)
                        continue;
                    int minD = INF;
                    for (int c = 0; c < k; c++) {
                        if (dist[i][currentC[c]] < minD) {
                            minD = dist[i][currentC[c]];
                        }
                    }
                    if (minD > maxD) {
                        maxD = minD;
                        vStar2 = i;
                    }
                }

                currentC[rand.nextInt(k)] = vStar2;
                currentC[rand.nextInt(k)] = rand.nextInt(n);
                rObj[0] = evaluateRadius(dist, n, k, currentC);
                localSearchSwap(dist, n, k, currentC, rObj);
                currentR = rObj[0];
                stuckCount = 0;
            }

            if (currentR < bestR) {
                bestR = currentR;
                System.arraycopy(currentC, 0, bestC, 0, k);
                stuckCount = 0;
            } else {
                stuckCount++;
                if (stuckCount > 5) {
                    System.arraycopy(bestC, 0, currentC, 0, k);
                    currentR = bestR;
                }
            }
        }

        return bestC;
    }

}

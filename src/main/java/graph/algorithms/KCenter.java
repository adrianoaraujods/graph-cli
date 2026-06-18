package graph.algorithms;

import java.util.Arrays;
import java.util.Random;
import graph.util.Timer;

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

    private static class ExactSolver {
        int words; // Number of 64-bit longs needed
        long[] fullMask;
        long[][] covers; // covers[vertex][word]
        int[][] centersCovering;
        int n, k;

        boolean verbose;
        int[] currentSelection;
        int[] bestSelection;
        long[][] stateMask; // Pre-allocated memory for recursion states
        boolean[] dominatedCenter;

        int nodesExplored;
        int currentIter, maxIter;
        long startTimeMillis;
        int currentRadius;
        long currentIterStartTime;
        long lastIterDuration;

        void printProgress() {
            if (!verbose)
                return;
            long elapsed = System.currentTimeMillis() - startTimeMillis;
            long remainingIters = maxIter - currentIter;
            long currentIterElapsed = System.currentTimeMillis() - currentIterStartTime;
            long estimatePerIter = currentIter == 1
                    ? currentIterElapsed
                    : Math.max(lastIterDuration, currentIterElapsed);
            long eta = estimatePerIter * remainingIters;
            System.out.printf("\r[Info] Exact solver [%d/~%d] R=%-6d | %-8d nodes | elapsed %-8s, ETA %-8s",
                    currentIter, maxIter, currentRadius, nodesExplored,
                    Timer.formatTime(elapsed), Timer.formatTime(eta));
            System.out.flush();
        }

        boolean greedyCheck() {
            long[] cov = new long[words];
            int used = 0;
            for (; used < k; used++) {
                if (Arrays.equals(cov, fullMask))
                    break;

                int best = -1;
                int bestGain = 0;

                int curCount = 0;
                for (int w = 0; w < words; w++) {
                    curCount += Long.bitCount(cov[w]);
                }

                for (int v = 0; v < n; v++) {
                    if (dominatedCenter[v])
                        continue;
                    int nextCount = 0;
                    for (int w = 0; w < words; w++) {
                        nextCount += Long.bitCount(cov[w] | covers[v][w]);
                    }
                    int gain = nextCount - curCount;
                    if (gain > bestGain) {
                        bestGain = gain;
                        best = v;
                    }
                }

                if (bestGain <= 0)
                    break;

                currentSelection[used] = best;
                for (int w = 0; w < words; w++) {
                    cov[w] |= covers[best][w];
                }
            }

            if (Arrays.equals(cov, fullMask)) {
                System.arraycopy(currentSelection, 0, bestSelection, 0, used);
                for (int i = used; i < k; i++)
                    bestSelection[i] = 0;
                return true;
            }
            return false;
        }

        boolean solveRecursive(int depth, long[] cov) {
            nodesExplored++;
            if (verbose && (nodesExplored & 1_048_575) == 0)
                printProgress();

            // Base cases
            if (Arrays.equals(cov, fullMask)) {
                System.arraycopy(currentSelection, 0, bestSelection, 0, depth);
                for (int i = depth; i < k; i++)
                    bestSelection[i] = 0;
                return true;
            }
            if (depth == k)
                return false;

            int bestU = -1;
            int minCandidates = Integer.MAX_VALUE;

            for (int i = 0; i < n; i++) {
                // Check if vertex i is covered using array arithmetic
                boolean isCovered = (cov[i / 64] & (1L << (i % 64))) != 0;

                if (!isCovered) {
                    int candidates = centersCovering[i].length;

                    if (candidates < minCandidates) {
                        minCandidates = candidates;
                        bestU = i;
                        if (minCandidates <= 1)
                            break;
                    }
                }
            }

            if (minCandidates == 0)
                return false;

            long[] nextCov = stateMask[depth + 1]; // Use pre-allocated array for next depth

            for (int c : centersCovering[bestU]) {
                currentSelection[depth] = c;

                // Fast bitwise OR for the next state
                for (int w = 0; w < words; w++) {
                    nextCov[w] = cov[w] | covers[c][w];
                }

                if (solveRecursive(depth + 1, nextCov)) {
                    return true;
                }
            }

            return false;
        }
    }

    public static int[] solveExact(int[][] dist, int n, int k) {
        return solveExact(dist, n, k, false);
    }

    public static int[] solveExact(int[][] dist, int n, int k, boolean verbose) {
        int[] dists = new int[n * n];
        int distCount = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (dist[i][j] != INF && dist[i][j] != Integer.MAX_VALUE) {
                    dists[distCount++] = dist[i][j];
                }
            }
        }

        Arrays.sort(dists, 0, distCount);
        int[] uniqueDists = new int[distCount];
        int uCount = 0;
        for (int i = 0; i < distCount; i++) {
            if (i == 0 || dists[i] != dists[i - 1]) {
                uniqueDists[uCount++] = dists[i];
            }
        }

        ExactSolver solver = new ExactSolver();
        solver.n = n;
        solver.k = k;
        solver.verbose = verbose;

        // Calculate dynamic words requirement
        solver.words = (n + 63) / 64;
        solver.fullMask = new long[solver.words];
        for (int i = 0; i < n; i++) {
            solver.fullMask[i / 64] |= (1L << (i % 64));
        }

        solver.covers = new long[n][solver.words];
        solver.currentSelection = new int[k];
        solver.bestSelection = new int[k];
        solver.stateMask = new long[k + 2][solver.words]; // Pre-allocate recursion memory

        int[] finalBestCenters = new int[k];
        int low = 0;
        int high = uCount - 1;

        int maxIter = (int) (Math.log(uCount) / Math.log(2)) + 1;
        long startTime = System.currentTimeMillis();
        solver.startTimeMillis = startTime;
        solver.maxIter = maxIter;
        int iter = 0;

        while (low <= high) {
            int mid = (low + high) / 2;
            int R = uniqueDists[mid];
            iter++;

            if (verbose) {
                solver.currentIterStartTime = System.currentTimeMillis();
                solver.currentIter = iter;
                solver.currentRadius = R;
                solver.printProgress();
            }

            // 1. Build covers array using dynamic words
            for (int i = 0; i < n; i++) {
                Arrays.fill(solver.covers[i], 0L);
                for (int j = 0; j < n; j++) {
                    if (dist[i][j] <= R) {
                        solver.covers[i][j / 64] |= (1L << (j % 64));
                    }
                }
            }

            // 2. Compute domination: mark vertex j dominated if covers[j] ⊆ covers[i]
            solver.dominatedCenter = new boolean[n];
            for (int i = 0; i < n; i++) {
                if (solver.dominatedCenter[i])
                    continue;

                for (int j = i + 1; j < n; j++) {
                    if (solver.dominatedCenter[j])
                        continue;

                    boolean iSubsetJ = true; // covers[i] ⊆ covers[j] (j is better)
                    boolean jSubsetI = true; // covers[j] ⊆ covers[i] (i is better)

                    for (int w = 0; w < solver.words; w++) {
                        if ((solver.covers[i][w] & ~solver.covers[j][w]) != 0L) {
                            iSubsetJ = false; // i has bits j lacks
                        }
                        if ((solver.covers[j][w] & ~solver.covers[i][w]) != 0L) {
                            jSubsetI = false; // j has bits i lacks
                        }
                    }

                    if (iSubsetJ && jSubsetI) {
                        // Equal coverage: safely prune the higher index
                        solver.dominatedCenter[j] = true;
                    } else if (iSubsetJ) {
                        // j strictly dominates i. Prune i and stop checking i against others.
                        solver.dominatedCenter[i] = true;
                        break;
                    } else if (jSubsetI) {
                        // i strictly dominates j. Prune j.
                        solver.dominatedCenter[j] = true;
                    }
                }
            }

            // 3. Build centersCovering strictly from the bitmasks
            solver.centersCovering = new int[n][];
            for (int i = 0; i < n; i++) {
                Integer[] temp = new Integer[n];
                int count = 0;
                for (int j = 0; j < n; j++) {
                    if (solver.dominatedCenter[j])
                        continue;
                    boolean jCoversI = (solver.covers[j][i / 64] & (1L << (i % 64))) != 0;
                    if (jCoversI) {
                        temp[count++] = j;
                    }
                }

                Integer[] validCandidates = Arrays.copyOf(temp, count);
                Arrays.sort(validCandidates, (a, b) -> {
                    int ca = 0, cb = 0;
                    for (int w = 0; w < solver.words; w++) {
                        ca += Long.bitCount(solver.covers[a][w]);
                        cb += Long.bitCount(solver.covers[b][w]);
                    }
                    return Integer.compare(cb, ca);
                });

                solver.centersCovering[i] = new int[count];
                for (int c = 0; c < count; c++) {
                    solver.centersCovering[i][c] = validCandidates[c];
                }
            }

            // Fast-path: if any vertex has no covering center, radius is infeasible
            boolean impossible = false;
            for (int i = 0; i < n; i++) {
                if (solver.centersCovering[i].length == 0) {
                    impossible = true;
                    break;
                }
            }

            boolean feasible;
            if (impossible) {
                feasible = false;
            } else if (solver.greedyCheck()) {
                feasible = true;

            } else {
                // Pass depth 0, and a zeroed array for the initial coverage state
                long[] initialCov = solver.stateMask[0];
                Arrays.fill(initialCov, 0L);
                if (solver.solveRecursive(0, initialCov)) {
                    feasible = true;
                } else {
                    feasible = false;
                }
            }

            if (feasible) {
                if (verbose) {
                    System.out.printf("  ✓%n");
                    System.out.flush();
                }
                System.arraycopy(solver.bestSelection, 0, finalBestCenters, 0, k);
                high = mid - 1;
            } else {
                if (verbose) {
                    System.out.printf("  ✗%n");
                    System.out.flush();
                }
                low = mid + 1;
            }

            if (verbose)
                solver.lastIterDuration = System.currentTimeMillis() - solver.currentIterStartTime;
        }

        if (verbose) {
            long totalTime = System.currentTimeMillis() - startTime;
            System.out.println("[Info] Exact solver complete. Best radius="
                    + evaluateRadius(dist, n, k, finalBestCenters) + " (total " + Timer.formatTime(totalTime) + ")");
        }

        return finalBestCenters;
    }
}

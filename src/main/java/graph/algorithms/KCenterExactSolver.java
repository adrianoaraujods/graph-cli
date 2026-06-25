package graph.algorithms;

import java.util.Arrays;
import graph.util.KCenterUtils;
import graph.util.Timer;

public class KCenterExactSolver {

    private static class ExactSolver {
        int words;
        long[] fullMask;
        long[][] covers;
        int[] coverSize;
        int[][] centersCovering;
        int n, k;

        boolean verbose;
        int[] currentSelection;
        int[] bestSelection;
        long[][] stateMask;
        boolean[] dominatedCenter;
        int[] sortedCenterSizes;

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

            // 1. Calculate Uncovered Count using Bitwise ops
            int uncoveredCount = 0;
            for (int w = 0; w < words; w++) {
                uncoveredCount += Long.bitCount(~cov[w] & fullMask[w]);
            }

            if (uncoveredCount == 0) {
                System.arraycopy(currentSelection, 0, bestSelection, 0, depth);
                for (int i = depth; i < k; i++)
                    bestSelection[i] = 0;
                return true;
            }

            if (depth == k)
                return false;

            // 2. VOLUME PRUNING: Can we mathematically cover the rest?
            int maxPossibleGain = 0;
            int remainingPicks = k - depth;
            for (int i = 0; i < remainingPicks && i < n; i++) {
                maxPossibleGain += sortedCenterSizes[i];
            }
            if (maxPossibleGain < uncoveredCount) {
                return false; // Prune this entire branch!
            }

            int bestU = -1;
            int minCandidates = Integer.MAX_VALUE;

            // 3. O(1) BITWISE MRV JUMPS
            for (int w = 0; w < words; w++) {
                // Invert the cover mask and mask against fullMask to get ONLY uncovered bits
                long uncoveredBits = ~cov[w] & fullMask[w];

                while (uncoveredBits != 0) {
                    // Find the index of the first uncovered bit
                    int bit = Long.numberOfTrailingZeros(uncoveredBits);
                    int i = w * 64 + bit;

                    int candidates = centersCovering[i].length;

                    if (candidates < minCandidates) {
                        minCandidates = candidates;
                        bestU = i;
                        if (minCandidates <= 1)
                            break;
                    }

                    // Clear the lowest set bit to move to the next uncovered vertex
                    uncoveredBits &= (uncoveredBits - 1);
                }
                if (minCandidates <= 1)
                    break;
            }

            if (minCandidates == 0)
                return false;

            long[] nextCov = stateMask[depth + 1];

            for (int c : centersCovering[bestU]) {
                currentSelection[depth] = c;

                for (int w = 0; w < words; w++) {
                    nextCov[w] = cov[w] | covers[c][w];
                }

                if (solveRecursive(depth + 1, nextCov)) {
                    return true;
                }
            }

            return false;
        }

        void buildCoverMasks(int R, int[][] dist) {
            for (int i = 0; i < n; i++) {
                Arrays.fill(covers[i], 0L);
                for (int j = 0; j < n; j++) {
                    if (dist[i][j] <= R) {
                        covers[i][j / 64] |= (1L << (j % 64));
                    }
                }
            }
        }

        void computeCoverSizes() {
            coverSize = new int[n];
            for (int i = 0; i < n; i++) {
                int s = 0;
                for (int w = 0; w < words; w++) {
                    s += Long.bitCount(covers[i][w]);
                }
                coverSize[i] = s;
            }
        }

        void pruneDominatedCenters() {
            dominatedCenter = new boolean[n];
            for (int i = 0; i < n; i++) {
                if (dominatedCenter[i])
                    continue;
                for (int j = i + 1; j < n; j++) {
                    if (dominatedCenter[j])
                        continue;
                    boolean iSubsetJ = true;
                    boolean jSubsetI = true;
                    for (int w = 0; w < words; w++) {
                        if ((covers[i][w] & ~covers[j][w]) != 0L)
                            iSubsetJ = false;
                        if ((covers[j][w] & ~covers[i][w]) != 0L)
                            jSubsetI = false;
                    }
                    if (iSubsetJ && jSubsetI) {
                        dominatedCenter[j] = true;
                    } else if (iSubsetJ) {
                        dominatedCenter[i] = true;
                        break;
                    } else if (jSubsetI) {
                        dominatedCenter[j] = true;
                    }
                }
            }
        }

        void buildCandidateLists() {
            centersCovering = new int[n][];
            int[] countingBucket = new int[n + 1];
            int[] countingPos = new int[n];

            for (int i = 0; i < n; i++) {
                int[] temp = new int[n];
                int count = 0;
                for (int j = 0; j < n; j++) {
                    if (dominatedCenter[j])
                        continue;
                    if ((covers[j][i / 64] & (1L << (i % 64))) != 0) {
                        temp[count++] = j;
                    }
                }
                if (count == 0) {
                    centersCovering[i] = new int[0];
                    continue;
                }

                int maxSize = 0;
                for (int t = 0; t < count; t++) {
                    int sz = coverSize[temp[t]];
                    if (sz > maxSize) maxSize = sz;
                    countingBucket[sz]++;
                }

                int pos = 0;
                for (int s = maxSize; s >= 0; s--) {
                    int freq = countingBucket[s];
                    countingBucket[s] = pos;
                    pos += freq;
                }

                for (int t = 0; t < count; t++) {
                    int sz = coverSize[temp[t]];
                    countingPos[countingBucket[sz]++] = temp[t];
                }

                centersCovering[i] = Arrays.copyOf(countingPos, count);
                Arrays.fill(countingBucket, 0, maxSize + 1, 0);
            }

            sortedCenterSizes = new int[n];
            for (int i = 0; i < n; i++) {
                if (!dominatedCenter[i]) {
                    sortedCenterSizes[i] = coverSize[i];
                }
            }

            Arrays.sort(sortedCenterSizes);
            for (int i = 0; i < n / 2; i++) {
                int temp = sortedCenterSizes[i];
                sortedCenterSizes[i] = sortedCenterSizes[n - 1 - i];
                sortedCenterSizes[n - 1 - i] = temp;
            }
        }

        boolean anyVertexUncovered() {
            for (int i = 0; i < n; i++) {
                if (centersCovering[i].length == 0)
                    return true;
            }
            return false;
        }

        boolean checkFeasibility(int R, int[][] dist) {
            buildCoverMasks(R, dist);
            computeCoverSizes();
            pruneDominatedCenters();
            buildCandidateLists();
            if (anyVertexUncovered())
                return false;
            if (greedyCheck())
                return true;
            long[] initialCov = stateMask[0];
            Arrays.fill(initialCov, 0L);
            return solveRecursive(0, initialCov);
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
                if (dist[i][j] != KCenterUtils.INF && dist[i][j] != Integer.MAX_VALUE) {
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

        solver.words = (n + 63) / 64;
        solver.fullMask = new long[solver.words];
        for (int i = 0; i < n; i++) {
            solver.fullMask[i / 64] |= (1L << (i % 64));
        }

        solver.covers = new long[n][solver.words];
        solver.currentSelection = new int[k];
        solver.bestSelection = new int[k];
        solver.stateMask = new long[k + 2][solver.words];

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

            boolean feasible = solver.checkFeasibility(R, dist);

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
                    + KCenterUtils.evaluateRadius(dist, n, k, finalBestCenters) + " (total "
                    + Timer.formatTime(totalTime) + ")");
        }

        return finalBestCenters;
    }
}

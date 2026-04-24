package graph.algorithms;

import java.util.Random;

import graph.api.ConnectivityType;

/**
 * @deprecated
 */
public class DegreeSequence {

  public static int[] generate(ConnectivityType connectivity, int n, long m, Random random) {
    int[] sequence = new int[n];
    long targetSum = 2 * m;

    int maxDegree = n - 1;
    int avgDegree = (int) (targetSum / n);
    int minDegree = 0;

    boolean evenDegrees = false;

    if (connectivity == ConnectivityType.CONNECTED) {
      minDegree = 1;
    } else if (connectivity == ConnectivityType.EULERIAN || connectivity == ConnectivityType.SEMI_EULERIAN) {
      minDegree = 2;
      avgDegree = (int) ((targetSum / n) / 2) * 2; // Ensure is even
      evenDegrees = true;
    }

    for (int v = 0; v < n; v++) {
      sequence[v] = avgDegree;
    }

    int remainder = (int) (targetSum - (avgDegree * n));
    for (int i = 0; i < remainder / 2; i++) {
      sequence[random.nextInt(n)] += 2;
    }

    // make two vertices have even degree
    if (connectivity == ConnectivityType.SEMI_EULERIAN) {
      while (true) {
        int v = random.nextInt(n);
        int w = random.nextInt(n);

        if (v != w) {
          if (v - 1 > minDegree && w + 1 < maxDegree) {
            sequence[v] -= 1;
            sequence[w] += 1;
            break;

          } else if (w - 1 > minDegree && sequence[w] + 1 <= maxDegree) {
            sequence[w] -= 1;
            sequence[v] += 1;
            break;
          }
        }
      }
    }

    for (int v = n - 1; v > 0; v--) {
      int w = random.nextInt(v + 1);

      int maxPossibleDiff = sequence[v] - minDegree;
      if (maxPossibleDiff <= 0) {
        continue;
      }

      int diff;
      if (evenDegrees) {
        int halfDiff = Math.max(1, maxPossibleDiff / 2);
        diff = random.nextInt(1, halfDiff + 1) * 2;
      } else {
        diff = random.nextInt(1, maxPossibleDiff + 1);
      }

      if (sequence[v] - diff >= minDegree && sequence[w] + diff <= maxDegree) {
        sequence[v] -= diff;
        sequence[w] += diff;
      } else if (sequence[w] - diff >= minDegree && sequence[v] + diff <= maxDegree) {
        sequence[w] -= diff;
        sequence[v] += diff;
      }
    }

    return ErdosGallai.ensureValidSequence(sequence, evenDegrees);
  }

}

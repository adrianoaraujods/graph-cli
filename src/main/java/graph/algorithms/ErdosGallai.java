package graph.algorithms;

import graph.util.Sort;

public class ErdosGallai {

  /**
   * Evaluates a degree sequence to determine if it is graphical.
   * * @param degrees An array of integers representing the degree sequence.
   * 
   * @return true if the sequence can form a simple graph, false otherwise.
   */
  public static boolean isGraphical(int[] degrees) {
    int n = degrees.length;
    if (n == 0) {
      return true;
    }

    // 1. Clone and sort the array in descending order
    int[] sequence = degrees.clone();
    Sort.quick(sequence, false);

    // 2. Validate non-negative degrees and check for an even sum
    long totalSum = 0;
    for (int degree : sequence) {
      if (degree < 0) {
        throw new IllegalArgumentException("Degrees cannot be negative.");
      }

      totalSum += degree;
    }

    if (totalSum % 2 != 0) {
      throw new IllegalArgumentException("Total sum is odd.");
    }

    long maxSum = (long) n * (n - 1);
    if (totalSum > maxSum) {
      throw new IllegalArgumentException("Total sum exceeds the maximum possible for a simple graph of size " + n);
    }

    // 3. Evaluate the Erdős-Gallai inequality for each k
    long sumFirstK = 0;
    for (int k = 1; k <= n; k++) {
      // Left Hand Side: Sum of the first k degrees
      sumFirstK += sequence[k - 1];

      // Right Hand Side: k(k-1) + sum of min(d_i, k) for the remaining vertices
      long sumRhs = (long) k * (k - 1);
      for (int i = k; i < n; i++) {
        sumRhs += Math.min(sequence[i], k);
      }

      // If the demand (LHS) exceeds the capacity (RHS), it fails
      if (sumFirstK > sumRhs) {
        return false;
      }
    }

    // Passed all checks
    return true;
  }

  public static int[] ensureValidSequence(int[] invalidSequence, boolean evenDegrees) {
    int[] sequence = invalidSequence.clone();

    int n = invalidSequence.length;

    int diff = evenDegrees ? 2 : 1;

    // Keep adjusting the numbers is graphical
    while (!isGraphical(sequence)) {
      Sort.quick(sequence, false);

      // 1. Find the highest degree we can safely decrement (must be > 0)
      int highest = 0;
      while (highest < n && sequence[highest] > diff) {
        highest++;
      }

      // 2. Find the lowest degree we can safely increment (must not exceed n - 1)
      int lowest = n - 1;
      while (lowest >= 0 && sequence[lowest] < n - diff - 1) {
        lowest--;
      }

      // Failsafe: If we can't find valid indices, break to avoid infinite loops
      if (highest >= n || lowest < 0) {
        throw new IllegalStateException("Cannot balance the sequence any further.");
      }

      // Execute the transfer
      sequence[highest] -= diff;
      sequence[lowest] += diff;
    }

    return sequence;
  }
}

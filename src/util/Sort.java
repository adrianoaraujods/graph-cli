package src.util;

/**
 * A utility class providing high-performance sorting algorithms.
 */
public class Sort {

  /**
   * Internal Visitor interface acting as a callback for the sorting engine.
   * <p>
   * It defines what happens when the algorithm decides two elements must be
   * swapped.
   */
  private interface SortVisitor {
    void swap(int a, int b);
  }

  /**
   * Sorts a single array in ascending order using Quicksort.
   *
   * @param array The array to be sorted.
   */
  public static void quick(int[] array) {
    quick(array, true);
  }

  /**
   * Sorts a single array in the specified order using Quicksort.
   *
   * @param array     The array to be sorted.
   * @param ascending True to sort in ascending order, false for descending.
   */
  public static void quick(int[] array, boolean ascending) {
    if (array == null || array.length <= 1) {
      return;
    }

    SortVisitor singleSwapper = new SortVisitor() {
      @Override
      public void swap(int a, int b) {
        int temp = array[a];
        array[a] = array[b];
        array[b] = temp;
      }
    };

    quicksort(array, 0, array.length - 1, singleSwapper, ascending);
  }

  /**
   * Sorts the primary array in ascending order using Quicksort, while
   * simultaneously applying the exact same swaps to the secondary array.
   *
   * @param primary   The array guiding the sort.
   * @param secondary The array mirroring the swaps.
   * @throws IllegalArgumentException If the primary and secondary arrays are not
   *                                  the same length.
   */
  public static void quick(int[] primary, int[] secondary) throws IllegalArgumentException {
    quick(primary, secondary, true);
  }

  /**
   * Sorts the primary array in the specified order using Quicksort, while
   * simultaneously applying the exact same swaps to the secondary array.
   *
   * @param primary   The array guiding the sort.
   * @param secondary The array mirroring the swaps.
   * @param ascending True to sort in ascending order, false for descending.
   * @throws IllegalArgumentException If the primary and secondary arrays are not
   *                                  the same length.
   */
  public static void quick(int[] primary, int[] secondary, boolean ascending) throws IllegalArgumentException {
    if (primary == null || secondary == null) {
      return;
    }

    if (primary.length != secondary.length) {
      throw new IllegalArgumentException("Both arrays should have the same size.");
    }

    if (primary.length <= 1) {
      return;
    }

    SortVisitor dualSwapper = new SortVisitor() {
      @Override
      public void swap(int a, int b) {
        // Swap primary
        int temp1 = primary[a];
        primary[a] = primary[b];
        primary[b] = temp1;

        // Swap secondary
        int temp2 = secondary[a];
        secondary[a] = secondary[b];
        secondary[b] = temp2;
      }
    };

    quicksort(primary, 0, primary.length - 1, dualSwapper, ascending);
  }

  /**
   * Universal Recursive Quicksort engine. It dictates the traversal and
   * partitioning, but delegates the actual data mutation to the provided
   * {@link SortVisitor}.
   *
   * @param primary   The array guiding the sort comparisons.
   * @param start     The starting index of the partition.
   * @param end       The ending index of the partition.
   * @param visitor   The callback that handles the swapping mechanism.
   * @param ascending True to sort ascending, false to sort descending.
   */
  private static void quicksort(int[] primary, int start, int end, SortVisitor visitor, boolean ascending) {
    int left = start, right = end;
    int pivot = primary[(start + end) / 2];

    while (left <= right) {
      while (ascending ? primary[left] < pivot : primary[left] > pivot) {
        left++;
      }

      while (ascending ? primary[right] > pivot : primary[right] < pivot) {
        right--;
      }

      if (left <= right) {
        visitor.swap(left, right);
        left++;
        right--;
      }
    }

    if (start < right) {
      quicksort(primary, start, right, visitor, ascending);
    }

    if (end > left) {
      quicksort(primary, left, end, visitor, ascending);
    }
  }
}

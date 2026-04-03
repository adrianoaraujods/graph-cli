package graph.util;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class SortTest {

    @Test
    void quick_intArrayNull() {
        int[] array = null;
        assertDoesNotThrow(() -> Sort.quick(array));
    }

    @Test
    void quick_intArrayEmpty() {
        int[] array = new int[0];
        int[] expected = new int[0];
        Sort.quick(array);
        assertArrayEquals(expected, array);
    }

    @Test
    void quick_intArraySingleElement() {
        int[] array = { 5 };
        int[] expected = { 5 };
        Sort.quick(array);
        assertArrayEquals(expected, array);
    }

    @Test
    void quick_intArrayAlreadySorted() {
        int[] array = { 1, 2, 3, 4, 5 };
        int[] expected = { 1, 2, 3, 4, 5 };
        Sort.quick(array);
        assertArrayEquals(expected, array);
    }

    @Test
    void quick_intArrayReverseSorted() {
        int[] array = { 5, 4, 3, 2, 1 };
        int[] expected = { 1, 2, 3, 4, 5 };
        Sort.quick(array);
        assertArrayEquals(expected, array);
    }

    @Test
    void quick_intArrayWithDuplicates() {
        int[] array = { 3, 1, 4, 1, 5, 9, 2, 6 };
        int[] expected = { 1, 1, 2, 3, 4, 5, 6, 9 };
        Sort.quick(array);
        assertArrayEquals(expected, array);
    }

    @Test
    void quick_intArrayDescending() {
        int[] array = { 1, 2, 3, 4, 5 };
        int[] expected = { 5, 4, 3, 2, 1 };
        Sort.quick(array, false);
        assertArrayEquals(expected, array);
    }

    @Test
    void quick_intArrayDescendingReverseSorted() {
        int[] array = { 5, 4, 3, 2, 1 };
        int[] expected = { 5, 4, 3, 2, 1 };
        Sort.quick(array, false);
        assertArrayEquals(expected, array);
    }

    @Test
    void quick_dualArraysNullPrimary() {
        int[] primary = null;
        int[] secondary = { 1, 2, 3 };
        assertDoesNotThrow(() -> Sort.quick(primary, secondary));
    }

    @Test
    void quick_dualArraysNullSecondary() {
        int[] primary = { 1, 2, 3 };
        int[] secondary = null;
        assertDoesNotThrow(() -> Sort.quick(primary, secondary));
    }

    @Test
    void quick_dualArraysMismatchLength() {
        int[] primary = { 1, 2, 3 };
        int[] secondary = { 1, 2 };
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> Sort.quick(primary, secondary));
        assertEquals("Both arrays should have the same size.", exception.getMessage());
    }

    @Test
    void quick_dualArraysBothNull() {
        int[] primary = null;
        int[] secondary = null;
        assertDoesNotThrow(() -> Sort.quick(primary, secondary));
    }

    @Test
    void quick_dualArraysBothEmpty() {
        int[] primary = new int[0];
        int[] secondary = new int[0];
        int[] expectedPrimary = new int[0];
        int[] expectedSecondary = new int[0];
        Sort.quick(primary, secondary);
        assertArrayEquals(expectedPrimary, primary);
        assertArrayEquals(expectedSecondary, secondary);
    }

    @Test
    void quick_dualArraysBothSingleElement() {
        int[] primary = { 1 };
        int[] secondary = { 10 };
        int[] expectedPrimary = { 1 };
        int[] expectedSecondary = { 10 };
        Sort.quick(primary, secondary);
        assertArrayEquals(expectedPrimary, primary);
        assertArrayEquals(expectedSecondary, secondary);
    }

    @Test
    void quick_dualArraysBothAlreadySorted() {
        int[] primary = { 1, 2, 3, 4, 5 };
        int[] secondary = { 10, 20, 30, 40, 50 };
        int[] expectedPrimary = { 1, 2, 3, 4, 5 };
        int[] expectedSecondary = { 10, 20, 30, 40, 50 };
        Sort.quick(primary, secondary);
        assertArrayEquals(expectedPrimary, primary);
        assertArrayEquals(expectedSecondary, secondary);
    }

    @Test
    void quick_dualArraysUnsorted() {
        int[] primary = { 5, 2, 8, 1, 9 };
        int[] secondary = { 50, 20, 80, 10, 90 };
        int[] expectedPrimary = { 1, 2, 5, 8, 9 };
        int[] expectedSecondary = { 10, 20, 50, 80, 90 };
        Sort.quick(primary, secondary);
        assertArrayEquals(expectedPrimary, primary);
        assertArrayEquals(expectedSecondary, secondary);
    }

    @Test
    void quick_dualArraysDescending() {
        int[] primary = { 1, 2, 3, 4, 5 };
        int[] secondary = { 10, 20, 30, 40, 50 };
        int[] expectedPrimary = { 5, 4, 3, 2, 1 };
        int[] expectedSecondary = { 50, 40, 30, 20, 10 };
        Sort.quick(primary, secondary, false);
        assertArrayEquals(expectedPrimary, primary);
        assertArrayEquals(expectedSecondary, secondary);
    }

    @Test
    void quick_dualArraysDescendingUnsorted() {
        int[] primary = { 5, 2, 8, 1, 9 };
        int[] secondary = { 50, 20, 80, 10, 90 };
        int[] expectedPrimary = { 9, 8, 5, 2, 1 };
        int[] expectedSecondary = { 90, 80, 50, 20, 10 };
        Sort.quick(primary, secondary, false);
        assertArrayEquals(expectedPrimary, primary);
        assertArrayEquals(expectedSecondary, secondary);
    }

    @Test
    void quick_dualArraysWithDuplicates() {
        int[] primary = { 3, 1, 4, 1, 5 };
        int[] secondary = { 30, 10, 40, 10, 50 };
        int[] expectedPrimary = { 1, 1, 3, 4, 5 };
        int[] expectedSecondary = { 10, 10, 30, 40, 50 };
        Sort.quick(primary, secondary);
        assertArrayEquals(expectedPrimary, primary);
        assertArrayEquals(expectedSecondary, secondary);
    }
}

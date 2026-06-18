package graph.algorithms;

import graph.algorithms.KCenterExactSolver;
import graph.util.KCenterUtils;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class KCenterTest {

    @Test
    void testSolveExactTrivialTwoVertices() {
        int n = 2;
        int k = 1;
        int[][] dist = {
            { 0, 5 },
            { 5, 0 }
        };

        int[] centers = KCenterExactSolver.solveExact(dist, n, k);
        assertEquals(k, centers.length);

        int radius = KCenterUtils.evaluateRadius(dist, n, k, centers);
        assertEquals(5, radius);
    }

    @Test
    void testSolveExactLineThreeVerticesK1() {
        int n = 3;
        int k = 1;
        int[][] dist = {
            { 0, 1, 2 },
            { 1, 0, 1 },
            { 2, 1, 0 }
        };

        int[] centers = KCenterExactSolver.solveExact(dist, n, k);
        assertEquals(k, centers.length);

        int radius = KCenterUtils.evaluateRadius(dist, n, k, centers);
        assertEquals(1, radius);

        assertTrue(centers[0] == 1,
                "Optimal center for 3-vertex line should be vertex 1");
    }

    @Test
    void testSolveExactLineFourVerticesK2() {
        int n = 4;
        int k = 2;
        int[][] dist = {
            { 0, 1, 2, 3 },
            { 1, 0, 1, 2 },
            { 2, 1, 0, 1 },
            { 3, 2, 1, 0 }
        };

        int[] centers = KCenterExactSolver.solveExact(dist, n, k);
        assertEquals(k, centers.length);

        int radius = KCenterUtils.evaluateRadius(dist, n, k, centers);
        assertEquals(1, radius,
                "Optimal radius for 4-vertex line with k=2 should be 1");
    }
}

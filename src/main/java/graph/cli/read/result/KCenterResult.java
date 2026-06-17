package graph.cli.read.result;

import java.util.Arrays;

public record KCenterResult(
        String algorithmName,
        int k,
        int radius,
        int[] centers) implements AlgorithmResult {

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\nk-Center (").append(algorithmName).append("):\n");
        sb.append("  k: ").append(k).append("\n");
        sb.append("  Radius: ").append(radius).append("\n");

        int[] oneIndexedCenters = new int[centers.length];
        for (int i = 0; i < centers.length; i++) {
            oneIndexedCenters[i] = centers[i] + 1;
        }
        sb.append("  Centers: ").append(Arrays.toString(oneIndexedCenters)).append("\n");

        return sb.toString();
    }
}

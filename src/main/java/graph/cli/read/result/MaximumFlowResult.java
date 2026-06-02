package graph.cli.read.result;

import java.util.Arrays;

public record MaximumFlowResult(
    int source,
    int target,
    int maxFlow,
    int[][] paths,
    int[] flow,
    long[] edges) implements AlgorithmResult {

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("\nMaximum Flow:\n");
    sb.append("  Source: ").append(source).append("\n");
    sb.append("  Target: ").append(target).append("\n");
    sb.append("  Max Flow: ").append(maxFlow).append("\n");

    if (paths != null) {
      for (int i = 0; i < paths.length; i++) {
        sb.append("  Path ");
        sb.append(i + 1);
        sb.append(" (flow=");
        sb.append(flow[i]);
        sb.append("): ");
        sb.append((Arrays.toString(paths[i])));
        sb.append("\n");
      }
    }

    sb.append("}\n");

    return sb.toString();
  }
}

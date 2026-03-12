import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Concrete and immutable implementation of the Graph using the Forward Star
 * structure.
 */
public class ForwardStarGraph extends Graph {
  private final int[] targets;
  private final int[] pointers;

  /**
   * Package-private constructor. Should only be called by the
   * {@link ForwardStarGraphBuilder}.
   */
  ForwardStarGraph(int n, int m, int[] targets, int[] pointers) {
    super(n, m);
    this.targets = targets;
    this.pointers = pointers;
  }

  @Override
  public Graph reverse() {
    GraphBuilder builder = new ForwardStarGraphBuilder();
    builder.initialize(n, m);

    for (int p = 0; p < pointers.length - 1; p++) {
      int endIndex = pointers[p + 1];
      for (int t = pointers[p]; t < endIndex; t++) {
        builder.addEdge(targets[t], (p + 1));
      }
    }

    return builder.build();
  }

  @Override
  public void printAllEdges() {
    for (int p = 0; p < pointers.length - 1; p++) {
      int endIndex = pointers[p + 1];

      for (int t = pointers[p]; t < endIndex; t++) {
        printEdge((p + 1), targets[t]);
      }
    }
  }

  @Override
  public int getInDegree(int vertex) {
    if (vertex < 1 || vertex > n)
      throw new IllegalArgumentException();
    int degree = 0;
    for (int v : targets) {
      if (v == vertex)
        degree++;
    }
    return degree;
  }

  @Override
  public int getOutDegree(int vertex) {
    if (vertex < 1 || vertex > n)
      throw new IllegalArgumentException();
    return pointers[vertex] - pointers[vertex - 1];
  }

  @Override
  public int[] getPredecessors(int vertex) {
    if (vertex < 1 || vertex > n)
      throw new IllegalArgumentException();
    IntStream.Builder builder = IntStream.builder();

    for (int p = 0; p < pointers.length - 1; p++) {
      int endIndex = pointers[p + 1];
      for (int t = pointers[p]; t < endIndex; t++) {
        if (targets[t] == vertex)
          builder.add(p + 1);
      }
    }
    return builder.build().toArray();
  }

  @Override
  public int[] getSuccessors(int vertex) {
    if (vertex < 1 || vertex > n)
      throw new IllegalArgumentException();
    int[] successors = Arrays.copyOfRange(targets, pointers[vertex - 1], pointers[vertex]);
    Sort.quick(successors);
    return successors;
  }
}

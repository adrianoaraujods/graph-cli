import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Concrete and immutable implementation of the Graph using the Forward Star
 * structure.
 */
public class ForwardStarGraph extends Graph {
  private final int[] targets;
  private final int[] pointers;
  private final int[] vertices;

  /**
   * Package-private constructor. Should only be called by the
   * {@link ForwardStarGraphBuilder}.
   */
  ForwardStarGraph(int n, int m, int[] targets, int[] pointers, int[] vertices) {
    super(n, m);
    this.targets = targets;
    this.pointers = pointers;
    this.vertices = vertices;
  }

  /**
   * Package-private constructor. Should only be called by the
   * {@link ForwardStarGraphBuilder}.
   */
  ForwardStarGraph(int n, int m, int[] targets, int[] pointers) {
    super(n, m);
    this.targets = targets;
    this.pointers = pointers;
    this.vertices = null;
  }

  @Override
  public int[] getVertices() {
    if (vertices == null || vertices.length == 0) {
      return IntStream.rangeClosed(1, n).toArray();
    }

    return vertices;
  }

  @Override
  protected void iterateGraph(IteratorVisitor visitor) {
    for (int p = 0; p < pointers.length - 1; p++) {
      visitor.examineVertex(p + 1);

      int endIndex = pointers[p + 1];
      for (int t = pointers[p]; t < endIndex; t++) {
        visitor.examineEdge((p + 1), targets[t]);
      }
    }
  }

  @Override
  public Graph getInducedSubgraph(int[] vertices) {
    int maxVertex = Arrays.stream(vertices).max().orElse(0);

    GraphBuilder builder = new ForwardStarGraphBuilder();
    builder.initialize(maxVertex, m, vertices);

    Set<Integer> uniqueVertices = Arrays.stream(vertices).boxed().collect(Collectors.toSet());

    IteratorVisitor iterator = new IteratorVisitor() {
      @Override
      public void examineEdge(int source, int target) {
        if (uniqueVertices.contains(source) && uniqueVertices.contains(target)) {
          builder.addEdge(source, target);
        }
      }
    };

    iterateGraph(iterator);
    return builder.build();
  }

  @Override
  public int getInDegree(int vertex) {
    if (vertex < 1 || vertex > n) {
      throw new IllegalArgumentException();
    }

    int degree = 0;
    for (int v : targets) {
      if (v == vertex) {
        degree++;
      }
    }

    return degree;
  }

  @Override
  public int getOutDegree(int vertex) {
    if (vertex < 1 || vertex > n) {
      throw new IllegalArgumentException();
    }

    return pointers[vertex] - pointers[vertex - 1];
  }

  @Override
  public int[] getPredecessors(int vertex) {
    if (vertex < 1 || vertex > n) {
      throw new IllegalArgumentException();
    }

    IntStream.Builder builder = IntStream.builder();

    Graph.IteratorVisitor iterator = new IteratorVisitor() {
      @Override
      public void examineEdge(int source, int target) {
        if (target == vertex) {
          builder.add(source);
        }
      }
    };

    iterateGraph(iterator);
    return builder.build().toArray();
  }

  @Override
  public int[] getSuccessors(int vertex) {
    if (vertex < 1 || vertex > n) {
      throw new IllegalArgumentException();
    }

    int[] successors = Arrays.copyOfRange(targets, pointers[vertex - 1], pointers[vertex]);
    Sort.quick(successors);

    return successors;
  }
}

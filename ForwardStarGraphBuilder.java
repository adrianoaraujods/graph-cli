/**
 * Concrete implementation of the GraphBuilder for the Forward Star structure.
 */
public class ForwardStarGraphBuilder implements GraphBuilder {

  /** Total number of vertices in the graph. */
  private int n;

  /** Total number of edges in the graph. */
  private int m;

  private int[] sources;
  private int[] targets;

  /**
   * Controls the index for filling both the {@link #sources} and
   * {@link #targets} arrays in the {@link #addEdge}.
   */
  private int head;

  @Override
  public void initialize(int n, int m) {
    this.sources = new int[m];
    this.targets = new int[m];

    this.n = n;
    this.m = m;

    head = 0;
  }

  @Override
  public void addEdge(int source, int target) {
    sources[head] = source;
    targets[head] = target;
    head++;
  }

  @Override
  public Graph build() {
    Sort.quick(sources, targets);

    int[] pointers = new int[n + 1];
    pointers[0] = 0;

    // Count the exact out-degree of each vertex
    for (int i = 0; i < m; i++) {
      pointers[sources[i]]++;
    }

    // Accumulate degrees to create contiguous boundaries (Prefix Sums)
    for (int i = 1; i <= n; i++) {
      pointers[i] += pointers[i - 1];
    }

    int[] finalTargets = this.targets;

    // Delete builder reference
    this.sources = null;
    this.targets = null;

    // Pass the finalized arrays to the immutable graph
    return new ForwardStarGraph(n, m, finalTargets, pointers);
  }
}

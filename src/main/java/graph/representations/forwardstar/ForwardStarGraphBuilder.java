package graph.representations.forwardstar;

import java.util.Arrays;

import graph.representations.GraphBuilder;
import graph.api.Graph;
import graph.util.Sort;

/**
 * Concrete implementation of the GraphBuilder for the Forward Star structure.
 */
public class ForwardStarGraphBuilder implements GraphBuilder {

  /** If the graph has directed edges. */
  private boolean isDirected;

  /** Total number of vertices in the graph. */
  private int n;

  /** Total number of edges in the graph. */
  private int m;

  private int[] sources;
  private int[] targets;
  private int[] vertices;

  public ForwardStarGraphBuilder(boolean isDirected) {
    this.isDirected = isDirected;
  }

  /**
   * Controls the index for filling both the {@link #sources} and
   * {@link #targets} arrays in the {@link #addEdge}.
   */
  private int head;

  @Override
  public void initialize(int n, int m, int[] vertices) {
    this.n = n;
    this.m = isDirected ? m : m * 2;
    this.vertices = vertices;

    this.sources = new int[this.m];
    this.targets = new int[this.m];

    head = 0;
  }

  @Override
  public void addEdge(int v, int w) {
    sources[head] = v;
    targets[head] = w;
    head++;

    if (!isDirected) {
      sources[head] = w;
      targets[head] = v;
      head++;
    }
  }

  @Override
  public Graph build() {
    if (head != sources.length) {
      sources = Arrays.copyOf(sources, head);
      targets = Arrays.copyOf(targets, head);
      m = head;
    }

    if (m > 0) {
      Sort.quick(sources, targets);

      for (int i = 0; i < m; i++) {
        if (sources[i] > n) {
          n = sources[i];
        }
        if (targets[i] > n) {
          n = targets[i];
        }
      }
    }

    int[] pointers = new int[n + 1];
    pointers[0] = 0;

    if (m > 0) {
      for (int i = 0; i < m; i++) {
        pointers[sources[i]]++;
      }
    }

    for (int i = 1; i <= n; i++) {
      pointers[i] += pointers[i - 1];
    }

    int[] finalTargets = this.targets;

    // Delete builder reference
    this.sources = null;
    this.targets = null;

    return new ForwardStarGraph(isDirected, n, m, finalTargets, pointers, vertices);
  }
}

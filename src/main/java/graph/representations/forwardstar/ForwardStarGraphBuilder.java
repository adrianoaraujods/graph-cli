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
  public void initialize(int n, int m) {
    this.sources = new int[m];
    this.targets = new int[m];

    this.n = n;
    this.m = m;

    this.vertices = null;
    head = 0;
  }

  @Override
  public void initialize(int n, int m, int[] vertices) {
    this.sources = new int[m];
    this.targets = new int[m];

    this.n = n;
    this.m = m;
    this.vertices = vertices;

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

    if (!isDirected) {
      int originalM = m;
      int[] newSources = new int[m * 2];
      int[] newTargets = new int[m * 2];

      System.arraycopy(sources, 0, newSources, 0, m);
      System.arraycopy(targets, 0, newTargets, 0, m);

      for (int i = 0; i < originalM; i++) {
        newSources[m + i] = targets[i];
        newTargets[m + i] = sources[i];
      }

      sources = newSources;
      targets = newTargets;
      m = m * 2;

      Sort.quick(sources, targets);
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

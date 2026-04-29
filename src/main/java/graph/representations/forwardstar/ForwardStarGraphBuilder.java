package graph.representations.forwardstar;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import graph.representations.GraphBuilder;
import graph.util.Sort;

/**
 * Concrete implementation of the GraphBuilder for the Forward Star structure.
 */
public class ForwardStarGraphBuilder implements GraphBuilder {

  /** If the graph has directed edges. */
  private boolean isDirected;

  /** Total number of vertices in the graph. */
  private int n;

  /** Total number of logical edges. */
  private int m;

  private int[] sources;
  private int[] targets;
  private Set<Integer> isolatedVertices;

  public ForwardStarGraphBuilder(boolean isDirected) {
    this.isDirected = isDirected;
  }

  @Override
  public void initialize(int n, long m) {
    this.n = n;
    this.m = 0;
    this.sources = new int[(int) (isDirected ? m : m * 2)];
    this.targets = new int[(int) (isDirected ? m : m * 2)];
    this.isolatedVertices = new HashSet<>(n);
  }

  @Override
  public void addEdge(int v, int w) {
    // Remove from isolated if present (now has an edge)
    isolatedVertices.remove(v);
    isolatedVertices.remove(w);

    int head = (int) (isDirected ? m : m * 2);

    if (head >= sources.length) {
      int newCapacity = Math.max(4, sources.length * 2);
      sources = Arrays.copyOf(sources, newCapacity);
      targets = Arrays.copyOf(targets, newCapacity);
    }

    sources[head] = v;
    targets[head] = w;

    if (!isDirected) {
      sources[head + 1] = w;
      targets[head + 1] = v;
    }

    m++;
  }

  @Override
  public void addVertex(int v) {
    isolatedVertices.add(v);
  }

  @Override
  public ForwardStarGraph build() {
    int edgesCount = (isDirected ? m : m * 2);

    // Trim the arrays to the final size
    if (edgesCount != sources.length) {
      sources = Arrays.copyOf(sources, edgesCount);
      targets = Arrays.copyOf(targets, edgesCount);
    }

    if (edgesCount > 0) {
      Sort.quick(sources, targets);

      for (int i = 0; i < sources.length; i++) {
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

    if (sources.length > 0) {
      for (int i = 0; i < sources.length; i++) {
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

    return new ForwardStarGraph(isDirected, n, m, finalTargets, pointers, isolatedVertices);
  }
}

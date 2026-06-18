package graph.representations.forwardstar;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import graph.representations.GraphBuilder;
import graph.util.Sort;

/**
 * Concrete implementation of the GraphBuilder for the Forward Star structure.
 */
public class ForwardStarGraphBuilder implements GraphBuilder {

  /** If the graph has directed edges. */
  private boolean isDirected;

  /** If the graph has weighted edges. */
  private boolean isWeighted;

  /** If the graph has capacity edges. */
  private boolean hasCapacity;

  /** Total number of vertices in the graph. */
  private int n;

  /** Total number of logical edges. */
  private int m;

  private int[] sources;
  private int[] targets;
  private int[] weightsOrCapacities;
  private Set<Integer> isolatedVertices;

  /** Maps undirected edge (min,max) → {indexForMinMax, indexForMaxMin} for quick weight updates. */
  private Map<Long, int[]> undirectedEdgeIndices;

  public ForwardStarGraphBuilder(boolean isDirected) {
    this.isDirected = isDirected;
  }

  @Override
  public void initialize(int n, long m) {
    initialize(n, m, false, false);
  }

  @Override
  public void initialize(int n, long m, boolean isWeighted, boolean hasCapacity) {
    this.n = n;
    this.m = 0;
    this.isWeighted = isWeighted;
    this.hasCapacity = hasCapacity;
    int maximumEdges = (int) (isDirected ? m : m * 2);
    this.sources = new int[maximumEdges];
    this.targets = new int[maximumEdges];
    if (isWeighted || hasCapacity) {
      this.weightsOrCapacities = new int[maximumEdges];
    } else {
      this.weightsOrCapacities = null;
    }
    this.isolatedVertices = new HashSet<>(n);
    if (!isDirected && (isWeighted || hasCapacity)) {
      this.undirectedEdgeIndices = new HashMap<>();
    } else {
      this.undirectedEdgeIndices = null;
    }
  }

  @Override
  public void addEdge(int v, int w) {
    addEdge(v, w, 1);
  }

  @Override
  public void addEdge(int v, int w, int weight) {
    // Remove from isolated if present (now has an edge)
    isolatedVertices.remove(v);
    isolatedVertices.remove(w);

    // For undirected weighted/capacitated graphs, check if this edge already
    // exists structurally and update the weight for the current direction.
    if (!isDirected && undirectedEdgeIndices != null) {
      int min = Math.min(v, w);
      int max = Math.max(v, w);
      long key = ((long) min << 32) | max;
      int[] indices = undirectedEdgeIndices.get(key);
      if (indices != null) {
        int idx = (v < w) ? indices[0] : indices[1];
        if (isWeighted || hasCapacity) {
          weightsOrCapacities[idx] = weight;
        }
        return;
      }
    }

    int head = (int) (isDirected ? m : m * 2);

    if (head >= sources.length) {
      int newCapacity = Math.max(4, sources.length * 2);
      sources = Arrays.copyOf(sources, newCapacity);
      targets = Arrays.copyOf(targets, newCapacity);
      if (isWeighted || hasCapacity) {
        weightsOrCapacities = Arrays.copyOf(weightsOrCapacities, newCapacity);
      }
    }

    sources[head] = v;
    targets[head] = w;
    if (isWeighted || hasCapacity) {
      weightsOrCapacities[head] = weight;
    }

    if (!isDirected) {
      sources[head + 1] = w;
      targets[head + 1] = v;
      if (isWeighted || hasCapacity) {
        weightsOrCapacities[head + 1] = weight;
      }

      if (undirectedEdgeIndices != null) {
        long key = ((long) Math.min(v, w) << 32) | Math.max(v, w);
        if (v < w) {
          undirectedEdgeIndices.put(key, new int[] { head, head + 1 });
        } else {
          undirectedEdgeIndices.put(key, new int[] { head + 1, head });
        }
      }
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
      if ((isWeighted || hasCapacity) && weightsOrCapacities != null) {
        weightsOrCapacities = Arrays.copyOf(weightsOrCapacities, edgesCount);
      }
    }

    if (edgesCount > 0) {
      if ((isWeighted || hasCapacity) && weightsOrCapacities != null) {
        Sort.quick(sources, targets, weightsOrCapacities);
      } else {
        Sort.quick(sources, targets);
      }

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
    int[] finalWeights = this.weightsOrCapacities;

    // Delete builder reference
    this.sources = null;
    this.targets = null;
    this.weightsOrCapacities = null;

    return new ForwardStarGraph(isDirected, n, m, finalTargets, pointers, isolatedVertices, isWeighted, hasCapacity,
        finalWeights);
  }
}

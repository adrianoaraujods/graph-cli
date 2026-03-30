package src.api;

import java.util.ArrayList;
import java.util.Stack;

import src.util.Sort;

/**
 * Abstract base class defining the read-only algorithms for a directed graph.
 */
public abstract class StaticGraph implements Graph {

  /** If the graph has directed edges. */
  public final boolean isDirected;

  /** Total number of vertices in the graph. */
  public final int n;

  /** Total number of edges in the graph. */
  public final int m;

  /**
   * Constructor called by the concrete implementations.
   */
  protected StaticGraph(boolean isDirected, int n, int m) {
    this.isDirected = isDirected;
    this.n = n;
    this.m = m;
  }

  /**
   * Constructor called by the concrete implementations.
   */
  protected StaticGraph(int n, int m) {
    this(true, n, m);
  }

  protected interface IteratorVisitor {
    default void examineVertex(int vertex) {
    }

    default void examineEdge(int source, int target) {
    }
  }

  protected abstract void iterateGraph(IteratorVisitor visitor);

  /**
   * Builds the induced subgraph based on the provided vertices.
   * 
   * @param vertices The vertices that are included in the subgraph.
   */
  public abstract StaticGraph getInducedSubgraph(int[] vertices);

  /**
   * Returns all vertices in the graph.
   * 
   * @return An array containing all vertex IDs from 1 to n.
   */
  public abstract int[] getVertices();

  public DFSResult depthFirstSearch(int[] rootsOrder, Graph.DFSVisitor visitor) {
    if (rootsOrder == null) {
      rootsOrder = new int[n];
      for (int i = 1; i <= n; i++) {
        rootsOrder[i - 1] = i;
      }
    } else if (rootsOrder.length > n) {
      throw new IllegalArgumentException("The rootsOrder array has more elements than the number os possible roots.");
    }

    int t = 0;
    int[] discoverTimes = new int[n];
    int[] finishTimes = new int[n];
    int[] parents = new int[n];
    int[] adjacencyIndex = new int[n];

    Stack<Integer> stack = new Stack<Integer>();

    for (int root : rootsOrder) {
      if (root < 1 || root > n) {
        throw new IndexOutOfBoundsException("The root: '" + root + "' is outside the possible vertex ID range.");
      }

      if (discoverTimes[root - 1] != 0) {
        continue;
      }

      stack.add(root);
      visitor.examineRoot(root);

      while (!stack.isEmpty()) {
        int v = stack.peek();

        if (discoverTimes[v - 1] == 0) {
          discoverTimes[v - 1] = ++t;
          visitor.discoverVertex(v);
        }

        int[] adjacency = getAdjacency(v);
        visitor.orderAdjacency(adjacency);

        int index = adjacencyIndex[v - 1];

        if (index < adjacency.length) {
          int w = adjacency[index];
          visitor.examineEdge(v, w);

          if (discoverTimes[w - 1] == 0) {
            visitor.treeEdge(v, w);
            parents[w - 1] = v;
            stack.add(w);
          } else if (finishTimes[w - 1] == 0) {
            visitor.backEdge(v, w);
          } else if (discoverTimes[v - 1] < discoverTimes[w - 1]) {
            visitor.forwardEdge(v, w);
          } else {
            visitor.crossEdge(v, w);
          }
          adjacencyIndex[v - 1]++;
        } else {
          finishTimes[v - 1] = ++t;
          visitor.finishVertex(v);
          stack.pop();
        }
      }
    }

    return visitor.finish(discoverTimes, finishTimes, adjacencyIndex);
  }

  /**
   * Creates a new graph with all the edges reversed.
   * 
   * @return The {@link StaticGraph} with the reversed edges.
   */
  protected StaticGraph getReversed(GraphBuilder builder) {
    builder.initialize(n, m);

    IteratorVisitor iterator = new IteratorVisitor() {
      @Override
      public void examineEdge(int source, int target) {
        builder.addEdge(target, source);
      }
    };

    iterateGraph(iterator);
    return builder.build();
  }

  public abstract StaticGraph getReversed();

  /**
   * Uses the Kosaraju Algorithm
   */
  public StaticGraph[] getMaximalComponents(int[] finishTimes) {
    int[] rootsOrder = getVertices();
    Sort.quick(finishTimes, rootsOrder, false);

    StaticGraph reversedGraph = getReversed();

    ArrayList<ArrayList<Integer>> componentsVerticesList = new ArrayList<>();

    Graph.DFSVisitor getComponentsVisitor = new Graph.DFSVisitor() {
      @Override
      public void examineRoot(int vertex) {
        ArrayList<Integer> verticesList = new ArrayList<>();
        verticesList.add(vertex);

        componentsVerticesList.add(verticesList);
      }

      @Override
      public void treeEdge(int source, int target) {
        componentsVerticesList.get(componentsVerticesList.size() - 1).add(target);
      }
    };

    reversedGraph.depthFirstSearch(rootsOrder, getComponentsVisitor);

    StaticGraph[] components = new StaticGraph[componentsVerticesList.size()];

    for (int i = 0; i < components.length; i++) {
      int[] vertices = componentsVerticesList.get(i).stream().mapToInt(v -> v).toArray();
      components[i] = getInducedSubgraph(vertices);
    }

    return components;
  }

  public StaticGraph[] getMaximalComponents() {
    class DFSFinishTimes implements Graph.DFSVisitor {
      @Override
      public DFSResult finish(int[] discoverTimes, int[] finishTimes, int[] predecessors) {
        return new DFSResult(null, finishTimes, null);
      }
    }

    DFSFinishTimes visitor = new DFSFinishTimes();
    DFSResult dfsResult = depthFirstSearch(visitor);

    return getMaximalComponents(dfsResult.finishTimes());
  }

  public record ClassifiedEdges(String treeEdges, String backEdges, String crossEdges, String forwardEdges) {
  }

  public ClassifiedEdges classifyEdges(int vertex) {
    EdgeSet treeEdgesSet = new EdgeSet();
    EdgeSet backEdgesSet = new EdgeSet();
    EdgeSet crossEdgesSet = new EdgeSet();
    EdgeSet forwardEdgesSet = new EdgeSet();

    DFSVisitor treePrinter = new DFSVisitor() {
      @Override
      public void treeEdge(int source, int target) {
        treeEdgesSet.append(source, target);
      }

      @Override
      public void backEdge(int source, int target) {
        if (source == vertex) {
          backEdgesSet.append(source, target);
        }
      }

      @Override
      public void crossEdge(int source, int target) {
        if (source == vertex) {
          crossEdgesSet.append(source, target);
        }
      }

      @Override
      public void forwardEdge(int source, int target) {
        if (source == vertex) {
          forwardEdgesSet.append(source, target);
        }
      }
    };

    depthFirstSearch(treePrinter);

    return new ClassifiedEdges(
        treeEdgesSet.toString(),
        backEdgesSet.toString(),
        crossEdgesSet.toString(),
        forwardEdgesSet.toString());
  }

  // Graph to string methods
  protected class EdgeSet {
    StringBuilder builder;

    EdgeSet() {
      builder = new StringBuilder("{");
    };

    public void append(int v, int w) {
      builder.append(isDirected ? "(" : "{");
      builder.append(v);
      builder.append(", ");
      builder.append(w);
      builder.append(isDirected ? "), " : "{, ");
    }

    public String toString() {
      if (builder.length() > 1) {
        // remove trailling `, `
        builder.replace(builder.length() - 2, builder.length() - 1, "}");
        return builder.toString();
      }

      return "{}";
    };
  }

  public String getEdgesSet() {
    EdgeSet allEdgesSet = new EdgeSet();

    IteratorVisitor iterator = new IteratorVisitor() {
      @Override
      public void examineEdge(int source, int target) {
        allEdgesSet.append(source, target);
      }
    };

    iterateGraph(iterator);

    return allEdgesSet.toString();
  }
}

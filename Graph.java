import java.util.ArrayList;
import java.util.Stack;

/**
 * Abstract base class defining the read-only algorithms for a directed graph.
 */
public abstract class Graph {

  /** Total number of vertices in the graph. */
  public final int n;

  /** Total number of edges in the graph. */
  public final int m;

  /**
   * Constructor called by the concrete implementations.
   */
  protected Graph(int n, int m) {
    this.n = n;
    this.m = m;
  }

  protected interface IteratorVisitor {
    default void examineVertex(int vertex) {
    }

    default void examineEdge(int source, int target) {
    }
  }

  protected abstract void iterateGraph(IteratorVisitor visitor);

  /**
   * Calculates the in-degree (number of incoming archs) of a given vertex.
   *
   * @param vertex The target vertex to analyze.
   * @return The number of edges pointing to the vertex.
   * @throws IllegalArgumentException if the vertex is less than 1 or greater than
   *                                  total number of vertices (n).
   */
  public abstract int getInDegree(int vertex);

  /**
   * Calculates the out-degree (number of outgoing edges) of a given vertex.
   *
   * @param vertex The target vertex to analyze.
   * @return The number of edges originating from the vertex.
   * @throws IllegalArgumentException if the vertex is less than 1 or greater than
   *                                  total number of vertices (n).
   */
  public abstract int getOutDegree(int vertex);

  /**
   * Retrieves all predecessors (vertices with an edge pointing to the given
   * vertex).
   *
   * @param vertex The target vertex to analyze.
   * @return An array of integers representing the predecessor vertices.
   * @throws IllegalArgumentException if the vertex is less than 1 or greater than
   *                                  total number of vertices (n).
   */
  public abstract int[] getPredecessors(int vertex);

  /**
   * Retrieves all successors (vertices that the given vertex points to).
   *
   * @param vertex The target vertex to analyze.
   * @return An array of integers representing the successor vertices.
   * @throws IllegalArgumentException if the vertex is less than 1 or greater than
   *                                  total number of vertices (n).
   */
  public abstract int[] getSuccessors(int vertex);

  /**
   * Creates a new graph with all the edges reversed.
   * 
   * @return The {@link Graph} with the reversed edges.
   */
  public Graph reverse() {
    GraphBuilder builder = new ForwardStarGraphBuilder();
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

  /**
   * Builds the induced subgraph based on the provided vertices.
   * 
   * @param vertices The vertices that are included in the subgraph.
   */
  public abstract Graph getInducedSubgraph(int[] vertices);

  /**
   * Returns all vertices in the graph.
   * 
   * @return An array containing all vertex IDs from 1 to n.
   */
  public abstract int[] getVertices();

  private interface DFSVisitor {
    default void examineRoot(int vertex) {
    }

    default void discoverVertex(int vertex) {
    }

    default void orderSucessors(int[] successors) {
    }

    default void finishVertex(int vertex) {
    }

    default void examineEdge(int source, int target) {
    }

    default void finish(int[] discoverTimes, int[] finishTimes, int[] predecessors) {
    }

    default void treeEdge(int source, int target) {
    }

    default void backEdge(int source, int target) {
    }

    default void forwardEdge(int source, int target) {
    }

    default void crossEdge(int source, int target) {
    }
  }

  /**
   * 
   * @param rootsOrder An array of vertex IDs that will be used to pick the roots
   *                   order. Uses lexicographical if null.
   * @param visitor
   * @throws IllegalArgumentException  if the rootsOrder length is greater than n.
   * @throws IndexOutOfBoundsException if any of the roots in the rootsOrder is
   *                                   outisde the possible vertex ID range.
   */
  private void depthFirstSearch(int[] rootsOrder, DFSVisitor visitor) {
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
    int[] predecessors = new int[n];
    int[] successorsIndex = new int[n];

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

        int[] successors = getSuccessors(v);
        visitor.orderSucessors(successors);

        int successorIndex = successorsIndex[v - 1];

        if (successorIndex < successors.length) {
          int w = successors[successorIndex];
          visitor.examineEdge(v, w);

          if (discoverTimes[w - 1] == 0) {
            visitor.treeEdge(v, w);
            predecessors[w - 1] = v;
            stack.add(w);
          } else if (finishTimes[w - 1] == 0) {
            visitor.backEdge(v, w);
          } else if (discoverTimes[v - 1] < discoverTimes[w - 1]) {
            visitor.forwardEdge(v, w);
          } else {
            visitor.crossEdge(v, w);
          }
          successorsIndex[v - 1]++;
        } else {
          finishTimes[v - 1] = ++t;
          visitor.finishVertex(v);
          stack.pop();
        }
      }
    }

    visitor.finish(discoverTimes, finishTimes, predecessors);
  }

  /**
   * Uses the Kosaraju Algorithm
   */
  public Graph[] getComponents() {
    class FinishTimesOrder implements DFSVisitor {
      public int[] rootsOrder;

      @Override
      public void finish(int[] discoverTimes, int[] finishTimes, int[] predecessors) {
        int[] vertices = new int[n];
        for (int i = 1; i <= n; i++) {
          vertices[i - 1] = i;
        }

        Sort.quick(finishTimes, vertices, false);
        this.rootsOrder = vertices;
      }
    }

    FinishTimesOrder finishTimesVisitor = new FinishTimesOrder();
    depthFirstSearch(null, finishTimesVisitor);

    Graph reversedGraph = reverse();

    ArrayList<ArrayList<Integer>> componentsVerticesList = new ArrayList<>();

    DFSVisitor getComponentsVisitor = new DFSVisitor() {
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

    reversedGraph.depthFirstSearch(
        finishTimesVisitor.rootsOrder,
        getComponentsVisitor);

    Graph[] components = new Graph[componentsVerticesList.size()];

    for (int i = 0; i < components.length; i++) {
      int[] vertices = componentsVerticesList.get(i).stream().mapToInt(v -> v).toArray();
      components[i] = getInducedSubgraph(vertices);
    }

    return components;
  }

  public void classifyGraph(int vertex) {
    DFSVisitor treePrinter = new DFSVisitor() {
      EdgeSet treeEdgesSet = new EdgeSet();
      EdgeSet backEdgesSet = new EdgeSet();
      EdgeSet crossEdgesSet = new EdgeSet();
      EdgeSet forwardEdgesSet = new EdgeSet();

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

      @Override
      public void finish(int[] discoverTimes, int[] finishTimes, int[] predecessors) {
        System.out.printf("\n\nTree Edges: %s", treeEdgesSet.toString());
        System.out.printf("\n\nBack Edges adjacent to vertex %d: %s", vertex, backEdgesSet.toString());
        System.out.printf("\nCross Edges adjacent to vertex %d: %s", vertex, crossEdgesSet.toString());
        System.out.printf("\nForward Edges adjacent to vertex %d: %s", vertex, forwardEdgesSet.toString());
      }

    };

    depthFirstSearch(null, treePrinter);
  }

  // Graph to string methods
  private class EdgeSet {
    StringBuilder builder;

    EdgeSet() {
      builder = new StringBuilder("{");
    };

    public void append(int v, int w) {
      builder.append("(");
      builder.append(v);
      builder.append(", ");
      builder.append(w);
      builder.append("), ");
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

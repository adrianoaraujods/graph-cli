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

  /**
   * Creates a new graph with all the edges reversed.
   * 
   * @return The {@link Graph} with the reversed edges.
   */
  public abstract Graph reverse();

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
   * Prints all the edges in the graph.
   */
  public abstract void printAllEdges();

  /**
   * Prints one edge in the graph.
   */
  public void printEdge(int v, int w) {
    System.out.print("\n{");
    System.out.print(v);
    System.out.print(", ");
    System.out.print(w);
    System.out.print("}");
  };

  private interface DFSVisitor {
    default void examineRoot(int vertex) {
    }

    default void discoverVertex(int vertex) {
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

  private void depthFirstSearch(DFSVisitor visitor) {
    int t = 0;
    int[] discoverTimes = new int[n];
    int[] finishTimes = new int[n];
    int[] predecessors = new int[n];
    int[] successorsIndex = new int[n];

    Stack<Integer> stack = new Stack<Integer>();

    for (int root = 1; root <= n; root++) {
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
   * Prints all the DFS tree edges found.
   */
  public void printTreeEdges() {
    DFSVisitor treePrinter = new DFSVisitor() {
      @Override
      public void examineRoot(int vertex) {
        System.out.println();
        System.out.println();
        System.out.print(vertex);
      }

      @Override
      public void treeEdge(int source, int target) {
        System.out.print(" -> ");
        System.out.print(target);
      }
    };
    depthFirstSearch(treePrinter);
  }
}

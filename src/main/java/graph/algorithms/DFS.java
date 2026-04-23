package graph.algorithms;

import java.util.Stack;

import graph.api.DirectedGraph;
import graph.api.Graph;
import graph.api.UndirectedGraph;

/**
 * Provides Depth-First Search (DFS) algorithm implementations for graph
 * traversal and analysis.
 * <p>
 * This class offers multiple variants of DFS including standard graph
 * traversal, root-based traversal, and edge classification capabilities.
 */
public class DFS {
  /**
   * Represents the result of a Depth-First Search operation.
   *
   * @param discoverTimes Array storing the discovery time (visit order) for each
   *                      vertex.
   * @param finishTimes   Array storing the finish time for each vertex.
   * @param parents       Array storing the parent vertex of each vertex in the
   *                      DFS tree.
   */
  public record DFSResult(int[] discoverTimes, int[] finishTimes, int[] parents) {
  }

  /**
   * Callback interface for receiving DFS traversal events.
   * <p>
   * Implementations can override any of the default methods to capture specific
   * events during the DFS traversal. All methods are no-ops by default.
   */
  public interface DFSVisitor {

    /**
     * Called at the start of DFS after construct the initial arrays.
     *
     * @param discoverTimes The discovery times array.
     * @param finishTimes   The finish times array.
     * @param parents       The parents array.
     * @return The constructed DFSResult.
     */
    default void start(int[] discoverTimes, int[] finishTimes, int[] parents) {
    }

    /**
     * Called when a visiting each vertex.
     */
    default boolean shouldStop() {
      return false;
    }

    /**
     * Called when a new DFS tree root is examined.
     *
     * @param v The root vertex being examined.
     */
    default void examineRoot(int v) {

    }

    /**
     * Called when a vertex is first discovered (visited for the first time).
     *
     * @param v The vertex being discovered.
     */
    default void discoverVertex(int v) {
    }

    /**
     * Called to provide the adjacency list for ordering during traversal.
     *
     * @param adjacency The array of adjacent vertices.
     */
    default void orderAdjacency(int[] adjacency) {
    }

    /**
     * Called when all adjacent vertices of a vertex have been processed.
     *
     * @param v The vertex being finished.
     */
    default void finishVertex(int v) {
    }

    /**
     * Called when an edge (v, w) is examined.
     *
     * @param v The source vertex.
     * @param w The target vertex.
     */
    default void examineEdge(int v, int w) {
    }

    /**
     * Called at the end of DFS to construct the final result.
     *
     * @param discoverTimes The discovery times array.
     * @param finishTimes   The finish times array.
     * @param parents       The parents array.
     * @return The constructed DFSResult.
     */
    default DFSResult finish(int[] discoverTimes, int[] finishTimes, int[] parents) {
      return new DFSResult(discoverTimes, finishTimes, parents);
    }

    /**
     * Called when a tree edge (v, w) is found (edge to an undiscovered vertex).
     *
     * @param v The source vertex.
     * @param w The target vertex.
     */
    default void treeEdge(int v, int w) {
    }

    /**
     * Called when a back edge (v, w) is found (edge to an ancestor in directed
     * graph).
     *
     * @param v The source vertex.
     * @param w The target vertex.
     */
    default void backEdge(int v, int w) {
    }

    /**
     * Called when a forward edge (v, w) is found (edge to a descendant in directed
     * graph).
     *
     * @param v The source vertex.
     * @param w The target vertex.
     */
    default void forwardEdge(int v, int w) {
    }

    /**
     * Called when a cross edge (v, w) is found (edge to a vertex in another
     * branch).
     *
     * @param v The source vertex.
     * @param w The target vertex.
     */
    default void crossEdge(int v, int w) {
    }
  }

  /**
   * Performs a Depth-First Search traversal on the given graph.
   *
   * @param graph      The graph to traverse.
   * @param rootsOrder The order in which to process root vertices (null for
   *                   default 1..n).
   * @param visitor    The visitor callback to receive DFS events.
   * @return The DFSResult containing discover times, finish times, and parent
   *         vertices.
   */
  public static DFSResult search(Graph graph, int[] rootsOrder, DFSVisitor visitor) {
    int n = graph.getVertices().length;

    if (rootsOrder == null) {
      rootsOrder = new int[n];
      for (int i = 1; i <= n; i++) {
        rootsOrder[i - 1] = i;
      }
    } else if (rootsOrder.length > n) {
      throw new IllegalArgumentException("The rootsOrder array has more elements than the number of possible roots.");
    }

    int t = 0;
    int[] discoverTimes = new int[n];
    int[] finishTimes = new int[n];
    int[] parents = new int[n];
    int[] adjacencyIndex = new int[n];

    visitor.start(discoverTimes, finishTimes, parents);

    Stack<Integer> stack = new Stack<Integer>();

    for (int root : rootsOrder) {
      if (visitor.shouldStop())
        break;

      if (root < 1 || root > n) {
        throw new IndexOutOfBoundsException("The root: '" + root + "' is outside the possible vertex ID range.");
      }

      if (discoverTimes[root - 1] != 0) {
        continue;
      }

      stack.add(root);
      visitor.examineRoot(root);

      while (!stack.isEmpty()) {
        if (visitor.shouldStop())
          break;

        int v = stack.peek();

        if (discoverTimes[v - 1] == 0) {
          discoverTimes[v - 1] = ++t;
          visitor.discoverVertex(v);
        }

        int[] adjacency;
        if (graph.isDirected) {
          adjacency = ((DirectedGraph) graph).getSuccessors(v);
        } else {
          adjacency = ((UndirectedGraph) graph).getNeighbors(v);
        }
        visitor.orderAdjacency(adjacency);

        int index = adjacencyIndex[v - 1];

        if (index < adjacency.length) {
          int w = adjacency[index];
          visitor.examineEdge(v, w);

          if (discoverTimes[w - 1] == 0) {
            visitor.treeEdge(v, w);
            parents[w - 1] = v;
            stack.add(w);

          } else if (w != parents[v - 1] && finishTimes[w - 1] == 0) {
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

    return visitor.finish(discoverTimes, finishTimes, parents);
  }

  /**
   * Performs a Depth-First Search traversal with a custom visitor.
   *
   * @param graph   The graph to traverse.
   * @param visitor The visitor callback to receive DFS events.
   * @return The DFSResult containing discover times, finish times, and parent
   *         vertices.
   */
  public static DFSResult search(Graph graph, DFSVisitor visitor) {
    return search(graph, null, visitor);
  }

  /**
   * Performs a Depth-First Search traversal on the graph with custom root order.
   *
   * @param graph      The graph to traverse.
   * @param rootsOrder The order in which to process root vertices.
   * @return The DFSResult containing discover times, finish times, and parent
   *         vertices.
   */
  public static DFSResult search(Graph graph, int[] rootsOrder) {
    return search(graph, rootsOrder, new DFSVisitor() {
      @Override
      public DFSResult finish(int[] discoverTimes, int[] finishTimes, int[] parents) {
        return new DFSResult(discoverTimes, finishTimes, parents);
      }
    });
  }

  /**
   * Performs a Depth-First Search traversal on the graph.
   *
   * @param graph The graph to traverse.
   * @return The DFSResult containing discover times, finish times, and parent
   *         vertices.
   */
  public static DFSResult search(Graph graph) {
    return search(graph, null, new DFSVisitor() {
    });
  }

  /**
   * Extracts the tree edges from a DFS result using the parent array.
   *
   * @param graph   The graph (unused, kept for API consistency).
   * @param parents The parent array from DFSResult.
   * @return A 2D array of tree edges where each row is {parent, child}.
   */
  public static int[][] getDFSTreeEdges(Graph graph, int[] parents) {
    int count = 0;
    for (int p : parents) {
      if (p != 0)
        count++;
    }

    int[][] treeEdges = new int[count][2];
    int index = 0;
    for (int v = 1; v <= parents.length; v++) {
      if (parents[v - 1] != 0) {
        treeEdges[index][0] = parents[v - 1];
        treeEdges[index][1] = v;
        index++;
      }
    }

    return treeEdges;
  }

  /**
   * Represents the classification of edges from a vertex in a DFS traversal.
   *
   * @param treeEdges    Edges to undiscovered vertices (DFS tree edges).
   * @param backEdges    Edges to ancestors (in directed graphs).
   * @param crossEdges   Edges to vertices in other branches.
   * @param forwardEdges Edges to descendants (in directed graphs).
   */
  public record ClassifiedDFSEdges(int[][] treeEdges, int[][] backEdges, int[][] crossEdges, int[][] forwardEdges) {
  }

  /**
   * Classifies all edges from a given vertex based on DFS timing.
   *
   * @param graph     The graph to analyze.
   * @param v         The source vertex.
   * @param dfsResult The result of a previous DFS traversal.
   * @return A ClassifiedDFSEdges containing all classified edge types.
   */
  public static ClassifiedDFSEdges classifyVertexDFSEdges(Graph graph, int v, DFSResult dfsResult) {
    boolean isDirected = graph instanceof DirectedGraph;

    int[] discoverTimes = dfsResult.discoverTimes();
    int[] finishTimes = dfsResult.finishTimes();
    int[] parents = dfsResult.parents();

    int[][] treeEdges = new int[parents.length][2];
    int[][] backEdges = new int[parents.length][2];
    int[][] crossEdges = new int[parents.length][2];
    int[][] forwardEdges = new int[parents.length][2];

    int treeIdx = 0, backIdx = 0, crossIdx = 0, forwardIdx = 0;

    int[] adjacency;
    if (isDirected) {
      adjacency = ((DirectedGraph) graph).getSuccessors(v);
    } else {
      adjacency = ((UndirectedGraph) graph).getNeighbors(v);
    }

    for (int w : adjacency) {
      if (parents[w - 1] == v) {
        treeEdges[treeIdx][0] = v;
        treeEdges[treeIdx][1] = w;
        treeIdx++;
      } else if (finishTimes[v - 1] > finishTimes[w - 1]) {
        crossEdges[crossIdx][0] = v;
        crossEdges[crossIdx][1] = w;
        crossIdx++;
      } else if (discoverTimes[v - 1] < discoverTimes[w - 1]) {
        forwardEdges[forwardIdx][0] = v;
        forwardEdges[forwardIdx][1] = w;
        forwardIdx++;
      } else {
        backEdges[backIdx][0] = v;
        backEdges[backIdx][1] = w;
        backIdx++;
      }
    }

    return new ClassifiedDFSEdges(
        java.util.Arrays.copyOf(treeEdges, treeIdx),
        java.util.Arrays.copyOf(backEdges, backIdx),
        java.util.Arrays.copyOf(crossEdges, crossIdx),
        java.util.Arrays.copyOf(forwardEdges, forwardIdx));
  }
}

package graph.algorithms;

import java.util.Stack;

import graph.api.DirectedGraph;
import graph.api.Graph;
import graph.api.UndirectedGraph;

public class DFS {
  public record DFSResult(int[] discoverTimes, int[] finishTimes, int[] parents) {
  }

  public interface DFSVisitor {
    default void examineRoot(int vertex) {
    }

    default void discoverVertex(int vertex) {
    }

    default void orderAdjacency(int[] adjacency) {
    }

    default void finishVertex(int vertex) {
    }

    default void examineEdge(int source, int target) {
    }

    default DFSResult finish(int[] discoverTimes, int[] finishTimes, int[] predecessors) {
      return new DFSResult(discoverTimes, finishTimes, predecessors);
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

    return visitor.finish(discoverTimes, finishTimes, parents);
  }

  public static DFSResult search(Graph graph, DFSVisitor visitor) {
    return search(graph, null, visitor);
  }

  public static DFSResult search(Graph graph, int[] rootsOrder) {
    return search(graph, rootsOrder, new DFSVisitor() {
    });
  }

  public static DFSResult search(Graph graph) {
    return search(graph, null, new DFSVisitor() {
    });
  }

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

  public record ClassifiedDFSEdges(int[][] treeEdges, int[][] backEdges, int[][] crossEdges, int[][] forwardEdges) {
  }

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

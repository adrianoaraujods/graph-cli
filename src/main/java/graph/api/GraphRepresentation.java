package graph.api;

import java.util.Stack;

import graph.representations.GraphBuilder;

public abstract class GraphRepresentation extends StaticGraph implements DirectedGraph, UndirectedGraph {

  protected GraphRepresentation(boolean isDirected, int n, int m) {
    super(isDirected, n, m);
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

  @Override
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

        int[] adjacency = isDirected ? getSuccessors(v) : getNeighbors(v);
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

  @Override
  public EdgeSet getDFSTreeEdges(int vertex, int[] parents) {
    EdgeSet treeEdges = new EdgeSet(parents.length - 1);

    for (int v = 1; v <= parents.length; v++) {
      if (parents[v - 1] != 0) {
        treeEdges.add(parents[v - 1], v);
      }
    }

    return treeEdges;
  }

  @Override
  public ClassifiedDFSEdges classifyVertexDFSEdges(int v, Graph.DFSResult dfsResult) {
    int[] discoverTimes = dfsResult.discoverTimes();
    int[] finishTimes = dfsResult.finishTimes();
    int[] parents = dfsResult.parents();

    EdgeSet treeEdgesSet = new EdgeSet();
    EdgeSet backEdgesSet = new EdgeSet();
    EdgeSet crossEdgesSet = new EdgeSet();
    EdgeSet forwardEdgesSet = new EdgeSet();

    int[] adjacency = isDirected ? getSuccessors(v) : getNeighbors(v);

    for (int w : adjacency) {
      if (parents[w - 1] == v) {
        treeEdgesSet.add(v, w);

      } else if (finishTimes[v - 1] > finishTimes[w - 1]) {
        crossEdgesSet.add(v, w);

      } else if (discoverTimes[v - 1] < discoverTimes[w - 1]) {
        forwardEdgesSet.add(v, w);

      } else {
        backEdgesSet.add(v, w);
      }
    }

    return new ClassifiedDFSEdges(treeEdgesSet, backEdgesSet, crossEdgesSet, forwardEdgesSet);
  }
}

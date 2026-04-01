package src.api;

public interface Graph {

  public interface IteratorVisitor {
    default void examineVertex(int vertex) {
    }

    default void examineEdge(int source, int target) {
    }
  }

  public abstract void iterateGraph(IteratorVisitor visitor);

  /**
   * Returns all vertices in the graph.
   * 
   * @return An array containing all vertex IDs from 1 to n.
   */
  public abstract int[] getVertices();

  /**
   * Builds the induced subgraph based on the provided vertices.
   * 
   * @param vertices The vertices that are included in the subgraph.
   */
  public abstract StaticGraph getInducedSubgraph(int[] vertices);

  public record DFSResult(int[] discoverTimes, int[] finishTimes, int[] parents) {
  }

  interface DFSVisitor {
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

  /**
   * 
   * @param rootsOrder An array of vertex IDs that will be used to pick the roots
   *                   order. Uses lexicographical if null.
   * @param visitor
   * @throws IllegalArgumentException  if the rootsOrder length is greater than n.
   * @throws IndexOutOfBoundsException if any of the roots in the rootsOrder is
   *                                   outisde the possible vertex ID range.
   */
  public abstract DFSResult depthFirstSearch(int[] rootsOrder, Graph.DFSVisitor visitor);

  public default DFSResult depthFirstSearch(Graph.DFSVisitor visitor) {
    return depthFirstSearch(null, visitor);
  }

  public default DFSResult depthFirstSearch(int[] rootsOrder) {
    return depthFirstSearch(rootsOrder, new DFSVisitor() {
    });
  }

  public default DFSResult depthFirstSearch() {
    return depthFirstSearch(null, new DFSVisitor() {
    });
  }

  public abstract EdgeSet getDFSTreeEdges(int vertex, int[] parents);

  public record ClassifiedDFSEdges(EdgeSet treeEdges, EdgeSet backEdges, EdgeSet crossEdges, EdgeSet forwardEdges) {
  }

  public abstract ClassifiedDFSEdges classifyVertexDFSEdges(int vertex, Graph.DFSResult dfsResult);

  public default EdgeSet getEdgesSet(boolean isDirected, int n) {
    EdgeSet set = new EdgeSet(isDirected, n);

    IteratorVisitor iterator = new IteratorVisitor() {
      @Override
      public void examineEdge(int source, int target) {
        set.add(source, target);
      }
    };

    iterateGraph(iterator);

    return set;
  }

  public default EdgeSet getEdgesSet(boolean isDirected) {
    return getEdgesSet(isDirected, 4);
  }
}

package graph.algorithms;

import java.util.HashSet;
import java.util.Set;

import graph.api.DirectedGraph;
import graph.api.Graph;
import graph.api.GraphBase.IteratorVisitor;
import graph.util.EdgeFormatter;
import graph.api.UndirectedGraph;

public class Fleury {

  public enum EulerianType {
    EULERIAN,
    SEMI_EULERIAN,
    NON_EULERIAN
  }

  /**
   * The path is the vertices sequence
   */
  public record EulerianPath(int[] path, EulerianType type) {
  }

  private static class CheckDegreesIterator implements IteratorVisitor {
    public EulerianType type = EulerianType.EULERIAN;

    /**
     * On directed graphs, represents the vertices where the in degree and out
     * degree are diferent.
     * <p>
     * On undirected graphs, represents the vertices with odd degree.
     */
    public int[] specialVertices = null;

    private Graph graph;

    CheckDegreesIterator(Graph graph) {
      this.graph = graph;
    }

    @Override
    public void examineVertex(int v) {
      boolean isSpecial = false;

      if (graph.isDirected) {
        int inDegree = ((DirectedGraph) graph).getInDegree(v);
        int outDegree = ((DirectedGraph) graph).getOutDegree(v);
        isSpecial = inDegree != outDegree;
      } else {
        int degree = ((UndirectedGraph) graph).getDegree(v);
        isSpecial = (degree % 2) != 0;
      }

      if (isSpecial) {
        if (specialVertices == null) {
          specialVertices = new int[] { v, 0 };
          type = EulerianType.SEMI_EULERIAN;

        } else if (specialVertices[1] == 0) {
          specialVertices[1] = v;

        } else {
          return;
        }
      }
    }

    @Override
    public boolean shouldStop() {
      return type == EulerianType.NON_EULERIAN;
    }
  }

  public static EulerianPath findEulerianPath(UndirectedGraph graph) {
    if (graph.getEdgesCount() < 1) {
      return new EulerianPath(new int[0], EulerianType.NON_EULERIAN);
    }

    int m = (int) graph.getEdgesCount();

    UndirectedGraph[] components = ConnectedComponents.find((UndirectedGraph) graph);

    if (components.length > 1) {
      return new EulerianPath(new int[0], EulerianType.NON_EULERIAN);
    }

    CheckDegreesIterator checkDegreesIterator = new CheckDegreesIterator((Graph) graph);
    graph.iterateGraph(checkDegreesIterator);

    EulerianType type = checkDegreesIterator.type;
    if (type == EulerianType.NON_EULERIAN) {
      return new EulerianPath(new int[0], type);
    }

    UndirectedGraph clone = (UndirectedGraph) ((Graph) graph).clone();

    Set<Integer> trivialVertices = new HashSet<>(clone.getVerticesCount());

    int v = checkDegreesIterator.specialVertices == null
        ? 1
        : checkDegreesIterator.specialVertices[0];

    int[] path = new int[m + 1];
    int pathIndex = 0;

    while (clone.getEdgesCount() > 0) {
      path[pathIndex++] = v;

      int[] neighbors = clone.getNeighbors(v);
      if (neighbors.length == 0) {
        return new EulerianPath(new int[0], EulerianType.NON_EULERIAN);
      }

      int w = neighbors[0];

      if (neighbors.length > 1) {
        Set<String> bridges = NaiveBridges.findAll(clone, trivialVertices);

        int i = 1;
        String edge;
        do {
          w = neighbors[i++];
          edge = EdgeFormatter.toKey(v, w);
        } while (i < neighbors.length && bridges.contains(edge));
      } else {
        trivialVertices.add(v);
      }

      clone.removeEdge(v, w);
      v = w;
    }

    path[m] = checkDegreesIterator.specialVertices == null
        ? 1
        : checkDegreesIterator.specialVertices[1];

    return new EulerianPath(path, type);
  }

  public static EulerianPath findEulerianPath(DirectedGraph graph) {
    if (graph.getEdgesCount() < 1) {
      return new EulerianPath(new int[0], EulerianType.NON_EULERIAN);
    }

    EulerianType type = EulerianType.EULERIAN;

    int m = (int) graph.getEdgesCount();

    DirectedGraph[] components = Kosaraju.findSCCs((DirectedGraph) graph);

    if (components.length > 1) {
      return new EulerianPath(new int[0], EulerianType.NON_EULERIAN);
    }

    CheckDegreesIterator checkDegreesIterator = new CheckDegreesIterator((Graph) graph);
    graph.iterateGraph(checkDegreesIterator);

    type = checkDegreesIterator.type;
    if (type == EulerianType.NON_EULERIAN) {
      return new EulerianPath(new int[0], type);
    }

    DirectedGraph clone = (DirectedGraph) ((Graph) graph).clone();

    int v = checkDegreesIterator.specialVertices == null
        ? 1
        : checkDegreesIterator.specialVertices[0];

    int pathIndex = 0;
    int[] path = new int[m + 1];
    path[pathIndex++] = v;

    while (clone.getEdgesCount() > 0) {
      int[] successors = clone.getSuccessors(v);
      if (successors.length == 0) {
        break;
      }
      int w = successors[0];

      clone.removeEdge(v, w);
      path[pathIndex++] = w;
      v = w;
    }

    return new EulerianPath(path, type);
  }

  public static EulerianPath findEulerianPath(Graph graph) {
    if (graph.isDirected) {
      return findEulerianPath((DirectedGraph) graph);
    } else {
      return findEulerianPath((UndirectedGraph) graph);
    }
  }
}

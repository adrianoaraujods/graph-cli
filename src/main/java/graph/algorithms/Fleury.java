package graph.algorithms;

import java.util.Arrays;
import java.util.Set;

import graph.api.DirectedGraph;
import graph.api.Edges;
import graph.api.Graph;
import graph.api.GraphBase.IteratorVisitor;
import graph.api.UndirectedGraph;
import graph.util.Timer;

public class Fleury {

  public enum BridgeFinder {
    TARJAN,
    NAIVE_LOCAL,
    NAIVE_GLOBAL
  }

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
          type = EulerianType.NON_EULERIAN;
          return;
        }
      }
    }

    @Override
    public boolean shouldStop() {
      return type == EulerianType.NON_EULERIAN;
    }
  }

  public static EulerianPath findEulerianPath(UndirectedGraph graph, BridgeFinder method, boolean enableLog) {
    if (graph.getEdgesCount() < 1) {
      return new EulerianPath(new int[0], EulerianType.NON_EULERIAN);
    }

    int componentsCount = ConnectedComponents.getCount((Graph) graph);
    if (componentsCount > 1) {
      return new EulerianPath(new int[0], EulerianType.NON_EULERIAN);
    }

    CheckDegreesIterator checkDegreesIterator = new CheckDegreesIterator((Graph) graph);
    graph.iterateGraph(checkDegreesIterator);

    EulerianType type = checkDegreesIterator.type;
    if (type == EulerianType.NON_EULERIAN) {
      return new EulerianPath(new int[0], type);
    }

    long start = System.currentTimeMillis();

    UndirectedGraph clone = (UndirectedGraph) ((Graph) graph).clone();
    int m = (int) clone.getEdgesCount();

    int[] path = new int[m + 1];
    int pathIndex = 0;

    int v = checkDegreesIterator.specialVertices == null
        ? 1
        : checkDegreesIterator.specialVertices[0];

    path[pathIndex++] = v;

    System.out.println();
    while (clone.getEdgesCount() > 0) {
      int[] neighbors = clone.getNeighbors(v);
      if (neighbors.length == 0) {
        System.out.println("\n[Error] Final path is " + pathIndex + " long, but it should be" + (m + 1) + ".");
        return new EulerianPath(new int[0], EulerianType.NON_EULERIAN);
      }

      int w = neighbors[0];

      if (neighbors.length > 1) {
        if (method == BridgeFinder.NAIVE_LOCAL) {
          componentsCount = ConnectedComponents.getCount((Graph) clone);

          int i = 0;
          boolean isBridge;
          do {
            w = neighbors[i++];
            isBridge = NaiveBridges.isBridge(v, w, (Graph) clone, componentsCount);
          } while (i < neighbors.length && isBridge);
        } else {
          Set<Long> bridges;

          if (method == BridgeFinder.TARJAN) {
            bridges = Tarjan.findAll(clone);
          } else {
            componentsCount = ConnectedComponents.getCount((Graph) clone);
            bridges = NaiveBridges.findAll(clone, componentsCount, start);
          }

          if (bridges != null) {
            int i = 0;
            long edge;
            do {
              w = neighbors[i++];
              edge = Edges.undirected(v, w);
            } while (i < neighbors.length && bridges.contains(edge));
          }
        }
      }

      clone.removeEdge(v, w);
      path[pathIndex++] = w;
      v = w;

      // Stop after 60 min and return path progress
      long end = System.currentTimeMillis() - start;
      if (end > 3_600_000) {
        System.out.printf("\n[Warning] Execution aborted after %s. Path length: %,d / %,d.", Timer.formatTime(end), pathIndex,
            path.length);

        path = Arrays.copyOf(path, pathIndex); // trim path
        return new EulerianPath(path, type);
      }

      if (enableLog && (pathIndex % 100) == 0) {
        System.out.print("\r[Info] Progress " + pathIndex + "/" + path.length);
        System.out.printf(" (… %s / %s) ", Timer.formatTime(end), Timer.formatTime((end / pathIndex) * path.length));
      }
    }

    if (checkDegreesIterator.specialVertices != null && v != checkDegreesIterator.specialVertices[1]) {
      System.out.println("\n[Warning] Final vertice of path is diffrent from expected.");
    }

    return new EulerianPath(path, type);
  }

  public static EulerianPath findEulerianPath(UndirectedGraph graph) {
    return findEulerianPath(graph, BridgeFinder.NAIVE_LOCAL, false);
  }

  public static EulerianPath findEulerianPath(DirectedGraph graph) {
    if (graph.getEdgesCount() < 1) {
      return new EulerianPath(new int[0], EulerianType.NON_EULERIAN);
    }

    // First check degrees to determine EULERIAN vs SEMI_EULERIAN
    CheckDegreesIterator checkDegreesIterator = new CheckDegreesIterator((Graph) graph);
    graph.iterateGraph(checkDegreesIterator);

    EulerianType type = checkDegreesIterator.type;
    if (type == EulerianType.NON_EULERIAN) {
      return new EulerianPath(new int[0], type);
    }

    // For EULERIAN (all vertices balanced): need STRONG connectivity
    // For SEMI_EULERIAN (2 imbalanced vertices): weak connectivity is sufficient
    if (type == EulerianType.EULERIAN) {
      // Check strong connectivity using Kosaraju
      DirectedGraph[] sccs = Kosaraju.findSCCs((DirectedGraph) graph);
      if (sccs.length > 1) {
        return new EulerianPath(new int[0], EulerianType.NON_EULERIAN);
      }
    } else {
      // SEMI_EULERIAN: check weak connectivity
      int componentsCount = ConnectedComponents.getCount((Graph) graph);
      if (componentsCount > 1) {
        return new EulerianPath(new int[0], EulerianType.NON_EULERIAN);
      }
    }

    int m = (int) graph.getEdgesCount();
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

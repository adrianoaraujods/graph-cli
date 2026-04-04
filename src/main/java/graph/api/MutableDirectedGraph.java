package graph.api;

public abstract class MutableDirectedGraph extends MutableGraph implements DirectedGraph {

  MutableDirectedGraph(boolean isDirected, int n, int m) {
    super(isDirected, n, m);
  };
}

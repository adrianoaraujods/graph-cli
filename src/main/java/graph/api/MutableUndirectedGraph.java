package graph.api;

public abstract class MutableUndirectedGraph extends MutableGraph implements UndirectedGraph {

  MutableUndirectedGraph(boolean isDirected, int n, int m) {
    super(isDirected, n, m);
  };
}

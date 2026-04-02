package graph.api;

public abstract class MutableUndirectedGraph extends StaticUndirectedGraph implements MutableGraph {
  MutableUndirectedGraph(boolean isDirected, int n, int m) {
    super(isDirected, n, m);
  };
}

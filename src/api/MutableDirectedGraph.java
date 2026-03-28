package src.api;

public abstract class MutableDirectedGraph extends StaticDirectedGraph implements MutableGraph {
  MutableDirectedGraph(boolean isDirected, int n, int m) {
    super(isDirected, n, m);
  };
}

package graph.api;

import java.util.ArrayList;

public class EdgeSet {
  private boolean isDirected;
  private ArrayList<Edge> edges;

  public EdgeSet(boolean isDirected, int n) {
    this.isDirected = isDirected;
    edges = new ArrayList<Edge>(n);
  }

  public EdgeSet(boolean isDirected) {
    this(isDirected, 4);
  }

  public EdgeSet(int n) {
    this(true, n);
  }

  public EdgeSet() {
    this(true);
  }

  public void add(int v, int w) {
    edges.add(new Edge(v, w));
  }

  public ArrayList<Edge> edges() {
    return edges;
  }

  public int size() {
    return edges.size();
  }

  public record Edge(int v, int w) {
    public final String toString(boolean isDirected) {
      StringBuilder builder = new StringBuilder(isDirected ? "(" : "{");
      builder.append(v);
      builder.append(", ");
      builder.append(w);
      builder.append(isDirected ? ")" : "}");
      return builder.toString();
    }
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder("{");

    if (edges.size() >= 1) {
      builder.append(edges.get(0).toString(isDirected));

      for (int i = 1; i < edges.size(); i++) {
        builder.append(", ");
        builder.append(edges.get(i).toString(isDirected));
      }
    }

    builder.append("}");
    return builder.toString();
  };

}

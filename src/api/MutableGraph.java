package src.api;

public interface MutableGraph {

  public abstract void removeEdge(int source, int target);

  public abstract void addEdge(int source, int target);

  public abstract void removeVertex(int vertex);

  public abstract void addVertex(int vertex);
}

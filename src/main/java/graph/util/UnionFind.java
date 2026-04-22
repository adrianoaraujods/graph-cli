package graph.util;

public class UnionFind {
  private int[] parent;
  private int[] rank;
  private int n;

  public UnionFind(int n) {
    parent = new int[n];
    rank = new int[n];
    this.n = n;

    for (int i = 0; i < n; i++) {
      parent[i] = i;
    }
  }

  public int find(int v) {
    if (parent[v] != v) {
      parent[v] = find(parent[v]);
    }

    return parent[v];
  }

  public void union(int v, int w) {
    int rootV = find(v);
    int rootW = find(w);

    if (rootV == rootW) {
      return;
    }

    n--;

    if (rank[rootV] < rank[rootW]) {
      parent[rootV] = rootW;
    } else if (rank[rootV] > rank[rootW]) {
      parent[rootW] = rootV;
    } else {
      parent[rootW] = rootV;
      rank[rootV]++;
    }
  }

  public boolean isConnected() {
    return n == 1;
  }
}
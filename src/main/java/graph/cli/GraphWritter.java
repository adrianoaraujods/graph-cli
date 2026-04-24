package graph.cli;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

import graph.util.EdgeFormatter;

public class GraphWritter implements AutoCloseable {
  private static final int CHUNK_SIZE = 64 * 1_024;

  private BufferedWriter writer;
  private int n;
  private long m;

  public GraphWritter(int n, long m, String outputPath) throws IOException {
    this.n = n;
    this.m = m;

    writer = new BufferedWriter(new FileWriter(outputPath), CHUNK_SIZE);
  }

  public void writeHeader() throws IOException {
    writer.write(n + " " + m);
    writer.newLine();
  }

  public void writePath(int[] path) throws IOException {
    for (int i = 0; i < (path.length - 1); i++) {
      int v = path[i];
      int w = path[i + 1];

      writer.write((v + 1) + " " + (w + 1)); // adding one to make 1-indexed
      writer.newLine();
    }
  }

  public void writeEdges(int v, int[] adjacency) throws IOException {
    for (int w : adjacency) {
      writer.write((v + 1) + " " + (w + 1));
      writer.newLine();
    }
  }

  public void writeEdges(Set<String> edges) throws IOException {
    for (String edge : edges) {
      String[] e = edge.split("-");
      int v = Integer.parseInt(e[0]) + 1;
      int w = Integer.parseInt(e[1]) + 1;

      writer.write(v + " " + w);
      writer.newLine();
    }
  }

  public void writeEdge(Set<Long> edges) throws IOException {
    for (long edge : edges) {
      int v = EdgeFormatter.getV(edge) + 1;
      int w = EdgeFormatter.getW(edge) + 1;

      writer.write(v + " " + w);
      writer.newLine();
    }
  }

  @Override
  public void close() throws IOException {
    writer.close();
  }
}

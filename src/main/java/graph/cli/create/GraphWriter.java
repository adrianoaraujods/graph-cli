package graph.cli.create;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

import graph.api.Edges;

public class GraphWriter implements AutoCloseable {
  private static final int CHUNK_SIZE = 64 * 1_024;

  private BufferedWriter writer;
  private int n;
  private long m;
  private boolean isWeighted;
  private int[] weights;
  private int weightIndex;

  public GraphWriter(int n, long m, String outputPath) throws IOException {
    this(n, m, outputPath, false);
  }

  public GraphWriter(int n, long m, String outputPath, boolean isWeighted) throws IOException {
    this.n = n;
    this.m = m;
    this.isWeighted = isWeighted;
    writer = new BufferedWriter(new FileWriter(outputPath), CHUNK_SIZE);
  }

  public void setWeights(int[] weights) {
    this.weights = weights;
    this.weightIndex = 0;
  }

  public void writeHeader() throws IOException {
    writer.write(n + " " + m);
    writer.newLine();
  }

  public void writePath(int[] path) throws IOException {
    for (int i = 0; i < (path.length - 1); i++) {
      int v = path[i];
      int w = path[i + 1];

      writer.write((v + 1) + " " + (w + 1));
      if (isWeighted && weights != null) {
        writer.write(" " + weights[weightIndex++]);
      }
      writer.newLine();
    }
  }

  public void writeEdges(int v, int[] adjacency) throws IOException {
    for (int w : adjacency) {
      writer.write((v + 1) + " " + (w + 1));
      if (isWeighted && weights != null) {
        writer.write(" " + weights[weightIndex++]);
      }
      writer.newLine();
    }
  }

  public void writeEdges(Set<String> edges) throws IOException {
    for (String edge : edges) {
      String[] e = edge.split("-");
      int v = Integer.parseInt(e[0]) + 1;
      int w = Integer.parseInt(e[1]) + 1;

      writer.write(v + " " + w);
      if (isWeighted && weights != null) {
        writer.write(" " + weights[weightIndex++]);
      }
      writer.newLine();
    }
  }

  public void writeEdge(Set<Long> edges) throws IOException {
    for (long edge : edges) {
      int v = Edges.getSource(edge) + 1;
      int w = Edges.getTarget(edge) + 1;

      writer.write(v + " " + w);
      if (isWeighted && weights != null) {
        writer.write(" " + weights[weightIndex++]);
      }
      writer.newLine();
    }
  }

  @Override
  public void close() throws IOException {
    writer.close();
  }
}

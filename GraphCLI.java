import java.security.InvalidAlgorithmParameterException;
import java.util.Arrays;
import java.util.function.Supplier;

import src.api.DirectedGraph;
import src.api.EdgeSet;
import src.api.GraphBuilder;
import src.api.StaticGraph;
import src.api.UndirectedGraph;
import src.api.Graph.ClassifiedDFSEdges;
import src.api.Graph.DFSResult;
import src.representations.forwardstar.ForwardStarGraphBuilder;
import src.util.GraphGenerator;
import src.util.GraphReader;

public class GraphCLI {
  static StaticGraph graph = null;
  static GraphBuilder builder;

  // Default Args
  static boolean isCreate = false;
  static boolean isRead = true;
  static String graphPath = null;
  static String logPath = null;
  static boolean isDirected = true;
  static long vertices = -1;
  static double density = -1.0;
  static int target = -1;
  static String representation = "Forward Star";

  static void printUsage() {
    System.out.println("Usage:");
    System.out.println("  java GraphCLI -c [options] <graph-path> <log-path> <vertices> <density>");
    System.out.println("  java GraphCLI -r [options] <graph-path> <log-path> <target-vertex>");
    System.out.println("  java GraphCLI -c -r [options] <graph-path> <log-path> <vertices> <density> <target-vertex>");
    System.out.println();
    System.out.println("Options:");
    System.out.println("  --create, -c          Generate a new graph file");
    System.out.println("  --read, -r            Read and analyze an existing graph file (default)");
    System.out.println("  --directed            Treat graph as directed (default)");
    System.out.println("  -d                    Short for --directed");
    System.out.println("  --undirected          Treat graph as undirected");
    System.out.println("  -u                    Short for --undirected");
    System.out.println("  --forward-star        Use Forward Star representation (default)");
    System.out.println("  --incidence-matrix    Use Incidence Matrix representation");
    System.out.println("  --adjacency-matrix    Use Adjacency Matrix representation");
    System.out.println("  --adjacency-list      Use Adjacency List representation");
    System.out.println("  --help, -h            Show this help message");
    System.out.println();
    System.out.println("Arguments:");
    System.out.println("  <graph-path>          Path for the graph (input/output) data file");
    System.out.println("  <log-path>            Path for the result logs");
    System.out.println("  <vertices>            Number of vertices (positive integer)");
    System.out.println("  <density>             Edge density as a decimal (0.0 to 1.0)");
    System.out.println("  <target-vertex>       Target vertex Id to analyze");
    System.out.println();
    System.out.println("Examples:");
    System.out.println("  java GraphCLI -c graph.txt 1000 0.5");
    System.out.println("  java GraphCLI -r graph.txt 5");
    System.out.println("  java GraphCLI -c -r graph.txt 1000 0.5 5");
    System.out.println("  java GraphCLI -c --undirected graph.txt 1000 0.5");
  }

  static void processArguments(String[] args) throws InvalidAlgorithmParameterException {
    for (int i = 0; i < args.length; i++) {
      String arg = args[i];

      if (arg.equals("--help") || arg.equals("-h")) {
        printUsage();
        return;

      } else if (arg.equals("--create") || arg.equals("-c")) {
        isCreate = true;
        isRead = false;

      } else if (arg.equals("--read") || arg.equals("-r")) {
        isRead = true;

      } else if (arg.equals("--directed") || arg.equals("-d")) {
        isDirected = true;

      } else if (arg.equals("--undirected") || arg.equals("-u")) {
        isDirected = false;

      } else if (arg.equals("--forward-star")) {
        representation = "Forward Star";

      } else if (arg.equals("--incidence-matrix")) {
        throw new InvalidAlgorithmParameterException("Invalid argument: Incidence Matrix not implement yet.");

      } else if (arg.equals("--adjacency-matrix")) {
        throw new InvalidAlgorithmParameterException("Invalid argument: Adjacency Matrix not implement yet.");

      } else if (arg.equals("--adjacency-list")) {
        throw new InvalidAlgorithmParameterException("Invalid argument: Adjacency List not implement yet.");

      } else if (!arg.startsWith("-")) {
        if (isCreate && graphPath == null) {
          graphPath = arg;

        } else if (isCreate && vertices == -1) {
          try {
            vertices = Long.parseLong(arg);
          } catch (NumberFormatException e) {
            throw new InvalidAlgorithmParameterException("Vertices should be a valid integer: " + arg);
          }

        } else if (isCreate && density < 0) {
          try {
            density = Double.parseDouble(arg);
          } catch (NumberFormatException e) {
            throw new InvalidAlgorithmParameterException("Density should be a valid decimal: " + arg);
          }

        } else if (isRead && graphPath == null) {
          graphPath = arg;

        } else if (isRead && target == -1) {
          try {
            target = Integer.parseInt(arg);
          } catch (NumberFormatException e) {
            throw new InvalidAlgorithmParameterException("The target vertex Id should be a valid integer: " + arg);
          }

        } else {
          throw new InvalidAlgorithmParameterException("Unexpected extra argument: " + arg);
        }
      }
    }

    switch (representation) {
      default:
        builder = new ForwardStarGraphBuilder(isDirected);
        break;
    }

    if (isRead && target == -1) {
      throw new InvalidAlgorithmParameterException("Missing argument: target vertex Id to analyze.");
    }

    if (isCreate) {
      if (graphPath == null) {
        throw new InvalidAlgorithmParameterException("Missing argument: output file path.");
      }

      if (vertices <= 0) {
        throw new InvalidAlgorithmParameterException("Missing or invalid argument: vertices must be > 0.");
      }

      if (density < 0.0 || density > 1.0) {
        throw new InvalidAlgorithmParameterException(
            "Missing or invalid argument: density must be between 0.0 and 1.0.");
      }
    }

  }

  static <T> T logTime(Supplier<T> block) {
    long start = System.currentTimeMillis();
    T result = block.get();
    System.out.printf(" (✓ %d ms)\n", System.currentTimeMillis() - start);
    return result;
  }

  static void logTime(Runnable block) {
    long start = System.currentTimeMillis();
    block.run();
    System.out.printf(" (✓ %d ms)\n", System.currentTimeMillis() - start);
  }

  public static void main(String[] args) {
    int step = 0;

    try {
      processArguments(args);

      System.out.printf("\nGraph Configuration:\n");
      System.out.printf("  Directed: %s\n", isDirected);
      System.out.printf("  Representation: %s\n", representation);

      if (isCreate) {
        if (isRead) {
          System.out.printf("  Mode: Create & Analyze\n");
        } else {
          System.out.printf("  Mode: Create\n");
        }
        System.out.printf("  Output File: %s\n", graphPath);

      } else {
        System.out.printf("  Mode: Read\n");
        System.out.printf("  Input File: %s\n", graphPath);
      }

      if (isCreate) {
        long maxEdges = vertices * (vertices - 1);
        long edges = Math.round(vertices * (vertices - 1) * density);
        System.out.printf("  Output File: %s\n", graphPath);
        System.out.printf("  Vertices: %,d\n", vertices);
        System.out.printf("  Density: %.2f\n", density);
        System.out.printf("  Edges: %,d / %,d\n", edges, maxEdges);

        System.out.printf("\nGenerating Graph...\n");
        logTime(() -> {
          GraphGenerator.generateAndWriteGraph(graphPath, vertices, edges, maxEdges);
        });

        if (!isRead) {
          System.out.println("\nGraph generation complete.");
          return;
        }
      }

      if (isRead) {
        System.out.printf("  Target Vertex: %d\n", target);
        System.out.printf("\n[%d/00] Reading File...", ++step);
        logTime(() -> {
          try {
            GraphReader.readFile(graphPath, builder);
          } catch (Exception e) {
            System.err.println(e);
          }
        });

        System.out.printf("[%d/00] Building Graph...", ++step);
        logTime(() -> {
          graph = builder.build();
        });

        if (target < 1 || target > graph.m) {
          throw new InvalidAlgorithmParameterException(
              "Invalid argument: target vertex Id should be between 1 and " + graph.m + ".");
        }

        int[] neighbors = null;
        int[] predecessors = null;
        int[] successors = null;
        if (isDirected) {
          System.out.printf("[%d/00] Processing target vertex predecessors...", ++step);
          predecessors = logTime(() -> ((DirectedGraph) graph).getPredecessors(target));

          System.out.printf("[%d/00] Processing target vertex successors...", ++step);
          successors = logTime(() -> ((DirectedGraph) graph).getSuccessors(target));

        } else {
          System.out.printf("[%d/00] Processing target vertex neighbors...", ++step);
          neighbors = logTime(() -> ((UndirectedGraph) graph).getNeighbors(target));
        }

        System.out.printf("[%d/00] Running Depth First Search in the graph...", ++step);
        DFSResult dfsResult = logTime(() -> graph.depthFirstSearch());

        StaticGraph[] components = null;
        if (isDirected) {
          System.out.printf("[%d/00] Finding maximal connected components...", ++step);
          components = logTime(() -> ((DirectedGraph) graph).getMaximalComponents(dfsResult.finishTimes()));
        }

        System.out.printf("\nTarget vertex %d details:\n", target);
        if (isDirected) {
          System.out.printf("  Out degree: %d\n", successors.length);
          System.out.printf("  In degree: %d\n", predecessors.length);
          System.out.printf("  Successors: %s\n", Arrays.toString(successors));
          System.out.printf("  Predecessors: %s\n", Arrays.toString(predecessors));
        } else {
          System.out.printf("  Degree: %d\n", neighbors.length);
          System.out.printf("  Neighbors: %s\n", Arrays.toString(neighbors));
        }

        ClassifiedDFSEdges classifiedEdges = graph.classifyVertexDFSEdges(target, dfsResult);
        EdgeSet treeEdges = graph.getDFSTreeEdges(target, dfsResult.parents());

        System.out.print("\nDepth First Search:\n");
        System.out.printf("  Tree Edges: %s\n", treeEdges.toString());
        System.out.println();
        System.out.printf("  Edges adjacent to vertex %d:\n", target);
        System.out.printf("    Tree Edges: %s\n", classifiedEdges.treeEdges());
        System.out.printf("    Back Edges: %s\n", classifiedEdges.backEdges());
        System.out.printf("    Cross Edges: %s\n", classifiedEdges.crossEdges());
        System.out.printf("    Forward Edges: %s\n", classifiedEdges.forwardEdges());

        if (isDirected) {
          System.out.print("\nComponents Trees:\n");
          for (int c = 0; c < components.length; c++) {
            System.out.printf("[%d/%d] Component:\n", c + 1, components.length);
            System.out.printf("  Vertices: %s\n", Arrays.toString(components[c].getVertices()));
            System.out.printf("  Edges: %s\n\n", components[c].getEdgesSet(isDirected, components[c].n).toString());
          }
        }
      }
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }
  }
}

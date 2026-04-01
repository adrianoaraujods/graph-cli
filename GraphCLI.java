import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;

import src.api.DirectedGraph;
import src.api.GraphBuilder;
import src.api.StaticGraph;
import src.api.UndirectedGraph;
import src.representations.forwardstar.ForwardStarGraphBuilder;
import src.util.GraphGenerator;
import src.util.GraphLogger;
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
    System.out.println("  java GraphCLI -c [options] <graph-path> <vertices> <density>");
    System.out.println("  java GraphCLI -r [options] <graph-path> <target-vertex>");
    System.out.println("  java GraphCLI -c -r [options] <graph-path> <vertices> <density> <target-vertex>");
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
        GraphLogger.logTime(() -> {
          GraphGenerator.generateAndWriteGraph(graphPath, vertices, edges, maxEdges);
        });

        if (!isRead) {
          System.out.println("\nGraph generation complete.");
          return;
        }
      }

      if (isRead) {
        System.out.printf("  Target Vertex: %d\n", target);
        System.out.printf("\n[%d/3] Reading File...", ++step);
        GraphLogger.logTime(() -> {
          try {
            GraphReader.readFile(graphPath, builder);
          } catch (Exception e) {
            System.err.println(e);
          }
        });

        System.out.printf("[%d/3] Building Graph...", ++step);
        GraphLogger.logTime(() -> {
          graph = builder.build();
        });

        if (target < 1 || target > graph.n) {
          throw new InvalidAlgorithmParameterException(
              "Invalid argument: target vertex Id should be between 1 and " + graph.n + ".");
        }

        String logPath = GraphLogger.defaultLogPath(graphPath);
        System.out.printf("[%d/3] Analyzing Graph...", ++step);
        GraphLogger.logTime(() -> {
          try {
            GraphLogger.writeLog(logPath, graph, target);
          } catch (IOException e) {
            System.err.printf("Error writing log file: " + e.getMessage());
          }
          return null;
        });
      }
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }
  }
}

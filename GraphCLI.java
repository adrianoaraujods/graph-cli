import java.security.InvalidAlgorithmParameterException;
import java.util.Arrays;
import java.util.function.Supplier;

import src.api.DirectedGraph;
import src.api.GraphBuilder;
import src.api.StaticDirectedGraph;
import src.api.StaticGraph;
import src.api.UndirectedGraph;
import src.api.Graph.DFSResult;
import src.representations.forwardstar.ForwardStarGraphBuilder;
import src.util.GraphReader;

public class GraphCLI {
  static StaticGraph graph = null;

  static boolean isDirected = true;
  static GraphBuilder builder = new ForwardStarGraphBuilder(isDirected);
  static String pathName = null;
  static int target = -1;

  static void printUsage() {
    System.out.println("Usage: java GraphCLI [options] <input-file> <target-vertex>");
    System.out.println();
    System.out.println("Options:");
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
    System.out.println("  <input-file>          Path to the graph data file");
    System.out.println("  <target-vertex>       Target vertex Id to analyze");
    System.out.println();
    System.out.println("Examples:");
    System.out.println("  java GraphCLI input.txt 5");
    System.out.println("  java GraphCLI --undirected --adjacency-list input.txt 5");
    System.out.println("  java GraphCLI input.txt 5 --forward-star");
  }

  static void processArguments(String[] args) throws InvalidAlgorithmParameterException {
    for (int i = 0; i < args.length; i++) {
      String arg = args[i];

      if (arg.equals("--help") || arg.equals("-h")) {
        printUsage();
        return;

      } else if (arg.equals("--directed") || arg.equals("-d")) {
        isDirected = true;

      } else if (arg.equals("--undirected") || arg.equals("-u")) {
        isDirected = false;

      } else if (arg.equals("--forward-star")) {
        builder = new ForwardStarGraphBuilder(isDirected);

      } else if (arg.equals("--incidence-matrix")) {
        throw new InvalidAlgorithmParameterException("Invalid argument: Incidence Matrix not implement yet.");

      } else if (arg.equals("--adjacency-matrix")) {
        throw new InvalidAlgorithmParameterException("Invalid argument: Adjacency Matrix not implement yet.");

      } else if (arg.equals("--adjacency-list")) {
        throw new InvalidAlgorithmParameterException("Invalid argument: Adjacency List not implement yet.");

      } else if (!arg.startsWith("-")) {
        if (pathName == null) {
          pathName = arg;

        } else if (target == -1) {
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

    if (pathName == null) {
      throw new InvalidAlgorithmParameterException("Missing argument: path to the graph data file.");
    }

    if (target == -1) {
      throw new InvalidAlgorithmParameterException("Missing argument: target vertex Id to analyze.");
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
      System.out.printf("  Representation: %s\n", builder.getRepresentation());
      System.out.printf("  Input File: %s\n", pathName);
      System.out.printf("  Target Vertex: %d\n", target);

      System.out.printf("\n[%d/00] Reading File...", ++step);
      logTime(() -> {
        try {
          GraphReader.readFile(pathName, builder);
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

      System.out.printf("[%d/00] Running DFS Classifying edges...", ++step);
      StaticGraph.ClassifiedEdges classifiedEdges = logTime(() -> graph.classifyEdges(target));

      StaticGraph[] components = null;
      if (isDirected) {
        System.out.printf("[%d/00] Finding maximal connected components...", ++step);
        components = logTime(() -> graph.getMaximalComponents(dfsResult.finishTimes()));
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

      System.out.print("\nDepth First Search Edges:");
      System.out.printf("\n\nTree Edges: %s", classifiedEdges.treeEdges());
      System.out.printf("\n\nBack Edges adjacent to vertex %d: %s", target, classifiedEdges.backEdges());
      System.out.printf("\nCross Edges adjacent to vertex %d: %s", target, classifiedEdges.crossEdges());
      System.out.printf("\nForward Edges adjacent to vertex %d: %s", target, classifiedEdges.forwardEdges());

      if (isDirected) {
        System.out.print("\n\nComponents Trees:");
        for (int c = 0; c < components.length; c++) {
          System.out.printf("\nComponent: %d", c + 1);
          System.out.printf("\n\tVertices: %s",
              Arrays.toString(components[c].getVertices()));
          System.out.printf("\n\tEdges: %s", components[c].getEdgesSet());
        }
      }
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }
  }
}

import java.security.InvalidAlgorithmParameterException;
import java.util.Arrays;
import java.util.function.Supplier;

public class GraphCLI {
  static Graph graph = null;

  static boolean isDirected = true;
  static GraphBuilder builder = new ForwardStarGraphBuilder();;
  static String pathName = null;
  static int target = -1;

  static void printUsage() {
    System.out.println("Usage: java GraphReader [options] <input-file> <target-vertex>");
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
    System.out.println("  java GraphReader input.txt 5");
    System.out.println("  java GraphReader --undirected --adjacency-list input.txt 5");
    System.out.println("  java GraphReader input.txt 5 --forward-star");
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
        builder = new ForwardStarGraphBuilder();

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
    try {
      processArguments(args);

      System.out.printf("Graph Configuration:\n");
      System.out.printf("  Directed: %s\n", isDirected);
      System.out.printf("  Representation: %s\n", builder.getRepresentation());
      System.out.printf("  Input File: %s\n", pathName);
      System.out.printf("  Target Vertex: %d\n", target);

      System.out.printf("\n[1/00] Reading File...");
      logTime(() -> {
        try {
          GraphReader.readFile(pathName, builder);
        } catch (Exception e) {
          System.err.println(e);
        }
      });

      System.out.printf("[2/00] Building Graph...");
      logTime(() -> {
        graph = builder.build();
      });

      if (target < 1 || target > graph.m) {
        throw new InvalidAlgorithmParameterException(
            "Invalid argument: target vertex Id should be between 1 and " + graph.m + ".");
      }

      System.out.printf("[3/00] Processing target vertex predecessors...");
      int[] predecessors = logTime(() -> graph.getPredecessors(target));

      System.out.printf("[4/00] Processing target vertex successors...");
      int[] successors = logTime(() -> graph.getSuccessors(target));

      System.out.printf("[5/00] Running DFS Classifying edges...");
      Graph.ClassifiedEdges classifiedEdges = logTime(() -> graph.classifyEdges(target));

      System.out.printf("[6/00] Finding maximal connected components...");
      Graph[] components = logTime(() -> graph.getComponents());

      System.out.printf("\nTarget vertex %d details:\n", target);
      System.out.printf("  Out degree: %d\n", successors.length);
      System.out.printf("  In degree: %d\n", predecessors.length);
      System.out.printf("  Successors: %s\n", Arrays.toString(successors));
      System.out.printf("  Predecessors: %s\n", Arrays.toString(predecessors));

      System.out.print("\nDepth First Search Edges:");
      System.out.printf("\n\nTree Edges: %s", classifiedEdges.treeEdges());
      System.out.printf("\n\nBack Edges adjacent to vertex %d: %s", target, classifiedEdges.backEdges());
      System.out.printf("\nCross Edges adjacent to vertex %d: %s", target, classifiedEdges.crossEdges());
      System.out.printf("\nForward Edges adjacent to vertex %d: %s", target, classifiedEdges.forwardEdges());

      System.out.print("\nComponents Trees:");
      for (int c = 0; c < components.length; c++) {
        System.out.printf("\nComponent: %d", c + 1);
        System.out.printf("\n\tVertices: %s", Arrays.toString(components[c].getVertices()));
        System.out.printf("\n\tEdges: %s", components[c].getEdgesSet());
      }
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }
  }
}

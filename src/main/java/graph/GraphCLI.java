package graph;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;

import graph.api.StaticGraph;
import graph.cli.GraphGenerator;
import graph.cli.GraphLogger;
import graph.cli.GraphReader;
import graph.cli.GraphGenerator.ConnectivityType;
import graph.representations.GraphBuilder;
import graph.representations.forwardstar.ForwardStarGraphBuilder;

/**
 * Command-line interface for generating and analyzing graphs.
 * 
 * <p>
 * This CLI provides tools for:
 * <ul>
 * <li>Generating random simple graphs with configurable properties</li>
 * <li>Reading and analyzing existing graph files</li>
 * <li>Logging analysis results for specific target vertices</li>
 * </ul>
 * 
 * <p>
 * Usage examples:
 * 
 * <pre>{@code
 * # Generate a directed weakly connected graph with 50% density
 * java GraphCLI -c graph.txt 1000 0.5
 * 
 * # Generate an undirected Eulerian graph with specific edge count
 * java GraphCLI -c --eulerian --undirected graph.txt 1000 --edges 450
 * 
 * # Read and analyze an existing graph
 * java GraphCLI -r graph.txt 5
 * 
 * # Generate and then analyze a graph
 * java GraphCLI -c -r graph.txt 1000 0.5 5
 * }</pre>
 */
public class GraphCLI {
    static StaticGraph graph = null;
    static GraphBuilder builder;

    static boolean isCreate = false;
    static boolean isRead = true;
    static String graphPath = null;
    static String logPath = null;
    static boolean isDirected = true;
    static ConnectivityType connectivity = ConnectivityType.WEAKLY;
    static int vertices = -1;
    static double density = -1.0;
    static long edges = -1;
    static int target = -1;
    static String representation = "Forward Star";

    /**
     * Prints the usage information and help message to stdout.
     */
    static void printUsage() {
        System.out.println("Usage:");
        System.out.println("  java GraphCLI -c [options] <graph-path> <vertices> <density>");
        System.out.println("  java GraphCLI -c [options] <graph-path> <vertices> --edges <count>");
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
        System.out.println("  --connected           Graph is weakly connected (default)");
        System.out.println("  --disconnected        Graph may be disconnected");
        System.out.println("  --eulerian            Graph has all vertices with even degree");
        System.out.println("  --semi-eulerian       Graph has exactly two vertices of odd degree");
        System.out.println("  --edges <count>       Number of edges (alternative to density)");
        System.out.println("  --density <value>     Edge density as a decimal (0.0 to 1.0)");
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
        System.out.println("  java GraphCLI -c graph.txt 1000 --edges 500");
        System.out.println("  java GraphCLI -r graph.txt 5");
        System.out.println("  java GraphCLI -c -r graph.txt 1000 0.5 5");
        System.out.println("  java GraphCLI -c --undirected graph.txt 1000 0.5");
        System.out.println("  java GraphCLI -c --eulerian graph.txt 1000 0.5");
    }

    /**
     * Parses and processes command-line arguments.
     * 
     * @param args the command-line arguments
     * @throws InvalidAlgorithmParameterException if arguments are invalid or
     *                                            missing
     */
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

            } else if (arg.equals("--connected")) {
                connectivity = ConnectivityType.WEAKLY;

            } else if (arg.equals("--disconnected")) {
                connectivity = ConnectivityType.NONE;

            } else if (arg.equals("--eulerian")) {
                connectivity = ConnectivityType.EULERIAN;

            } else if (arg.equals("--semi-eulerian")) {
                connectivity = ConnectivityType.SEMI_EULERIAN;

            } else if (arg.equals("--edges")) {
                if (i + 1 >= args.length) {
                    throw new InvalidAlgorithmParameterException("Missing argument: --edges requires a value.");
                }
                try {
                    edges = Long.parseLong(args[++i]);
                } catch (NumberFormatException e) {
                    throw new InvalidAlgorithmParameterException("Edges should be a valid integer: " + args[i]);
                }

            } else if (arg.equals("--density")) {
                if (i + 1 >= args.length) {
                    throw new InvalidAlgorithmParameterException("Missing argument: --density requires a value.");
                }
                try {
                    density = Double.parseDouble(args[++i]);
                } catch (NumberFormatException e) {
                    throw new InvalidAlgorithmParameterException("Density should be a valid decimal: " + args[i]);
                }

            } else if (arg.equals("--forward-star")) {
                representation = "Forward Star";

            } else if (arg.equals("--incidence-matrix")) {
                throw new InvalidAlgorithmParameterException("Invalid argument: Incidence Matrix not implement yet.");

            } else if (arg.equals("--adjacency-matrix")) {
                throw new InvalidAlgorithmParameterException("Invalid argument: Adjidence Matrix not implement yet.");

            } else if (arg.equals("--adjacency-list")) {
                throw new InvalidAlgorithmParameterException("Invalid argument: Adjacency List not implement yet.");

            } else if (!arg.startsWith("-")) {
                if (isCreate && graphPath == null) {
                    graphPath = arg;

                } else if (isCreate && vertices == -1) {
                    try {
                        vertices = Integer.parseInt(arg);
                    } catch (NumberFormatException e) {
                        throw new InvalidAlgorithmParameterException("Vertices should be a valid integer: " + arg);
                    }

                } else if (isCreate && density < 0 && edges < 0) {
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
                        throw new InvalidAlgorithmParameterException(
                                "The target vertex Id should be a valid integer: " + arg);
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

            if (edges >= 0 && density >= 0) {
                throw new InvalidAlgorithmParameterException("Cannot specify both --edges and density.");
            }

            if (edges < 0 && density < 0) {
                throw new InvalidAlgorithmParameterException(
                        "Must specify either --edges or density (positional argument).");
            }

            if (density >= 0 && (density < 0.0 || density > 1.0)) {
                throw new InvalidAlgorithmParameterException("Density must be between 0.0 and 1.0.");
            }

            long maxEdges = isDirected ? vertices * (vertices - 1) : vertices * (vertices - 1) / 2;
            if (edges >= 0 && edges > maxEdges) {
                throw new InvalidAlgorithmParameterException("Edges must be between 0 and " + maxEdges + ".");
            }
        }

    }

    /**
     * Main entry point for the Graph CLI application.
     * 
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        int step = 0;

        try {
            processArguments(args);

            System.out.printf("\nGraph Configuration:\n");
            System.out.printf("  Directed: %s\n", isDirected);
            System.out.printf("  Connectivity: %s\n", connectivity);

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
                GraphGenerator.GraphConfig config;
                if (edges >= 0) {
                    config = GraphGenerator.builder()
                            .directed(isDirected)
                            .connectivity(connectivity)
                            .vertices(vertices)
                            .edges(edges)
                            .outputPath(graphPath)
                            .build();
                } else {
                    config = GraphGenerator.builder()
                            .directed(isDirected)
                            .connectivity(connectivity)
                            .vertices(vertices)
                            .density(density)
                            .outputPath(graphPath)
                            .build();
                }

                long maxEdges = config.maxEdges();
                System.out.printf("  Vertices: %,d\n", vertices);
                if (density >= 0) {
                    System.out.printf("  Density: %.2f\n", density);
                }
                System.out.printf("  Edges: %,d / %,d\n", config.resolvedEdges(), maxEdges);

                System.out.printf("\nGenerating Graph...\n");
                GraphLogger.logTime(() -> {
                    GraphGenerator.generateGraph(config);
                });

                if (!isRead) {
                    System.out.println("\nGraph generation complete.");
                    return;
                }
            }

            if (isRead) {
                String logPath = GraphLogger.defaultLogPath(graphPath);
                System.out.printf("  Log File Path: %s\n", logPath);
                System.out.printf("  Target Vertex: %d\n", target);
                System.out.printf("  Representation: %s\n", representation);
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
        } catch (InvalidAlgorithmParameterException e) {
            System.err.println(e.getMessage());
            System.out.println();
            printUsage();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}

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
    static Long seed = null;
    static String representation = "Forward Star";

    static void printUsage() {
        System.out.println("Usage:");
        System.out.println("  java GraphCLI -c -f <file> -n <vertices> -d <density>");
        System.out.println("  java GraphCLI -c -f <file> -n <vertices> -m <edges>");
        System.out.println("  java GraphCLI -c -f <file> -n <vertices> -d <density> -s <seed>");
        System.out.println("  java GraphCLI -r -f <file> -t <target>");
        System.out.println("  java GraphCLI -c -r -f <file> -n <vertices> -d <density> -t <target>");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  --create, -c           Generate a new graph file");
        System.out.println("  --read, -r             Read and analyze an existing graph file");
        System.out.println("  --file, -f <path>      Graph file path (required)");
        System.out.println("  --vertices, -n <n>     Number of vertices (required for create)");
        System.out.println("  --edges, -m <count>    Number of edges (alternative to density)");
        System.out.println("  --density, -d <val>    Edge density 0.0-1.0 (alternative to edges)");
        System.out.println("  --seed, -s <n>         Random seed for reproducible graphs");
        System.out.println("  --directed             Treat graph as directed (default)");
        System.out.println("  --undirected, -u       Treat graph as undirected");
        System.out.println("  --connected            Graph is weakly connected (default)");
        System.out.println("  --disconnected         Graph may be disconnected");
        System.out.println("  --eulerian             Graph has all vertices with even degree");
        System.out.println("  --semi-eulerian        Graph has exactly two vertices of odd degree");
        System.out.println("  --target, -t <n>       Target vertex for analysis (required for read)");
        System.out.println("  --forward-star         Use Forward Star representation (default)");
        System.out.println("  --help, -h             Show this help message");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  java GraphCLI -c -f graph.txt -n 1000 -d 0.5");
        System.out.println("  java GraphCLI -c -f graph.txt -n 1000 -m 500");
        System.out.println("  java GraphCLI -c -f graph.txt -n 1000 -d 0.5 -s 42");
        System.out.println("  java GraphCLI -r -f graph.txt -t 5");
        System.out.println("  java GraphCLI -c -r -f graph.txt -n 1000 -d 0.5 -t 5");
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

            } else if (arg.equals("--file") || arg.equals("-f")) {
                if (i + 1 >= args.length) {
                    throw new InvalidAlgorithmParameterException("Missing argument: -f/--file requires a path.");
                }
                graphPath = args[++i];

            } else if (arg.equals("--vertices") || arg.equals("-n")) {
                if (i + 1 >= args.length) {
                    throw new InvalidAlgorithmParameterException("Missing argument: -n/--vertices requires a value.");
                }
                try {
                    vertices = Integer.parseInt(args[++i]);
                } catch (NumberFormatException e) {
                    throw new InvalidAlgorithmParameterException("Vertices should be a valid integer: " + args[i]);
                }

            } else if (arg.equals("--edges") || arg.equals("-m")) {
                if (i + 1 >= args.length) {
                    throw new InvalidAlgorithmParameterException("Missing argument: -m/--edges requires a value.");
                }
                try {
                    edges = Long.parseLong(args[++i]);
                } catch (NumberFormatException e) {
                    throw new InvalidAlgorithmParameterException("Edges should be a valid integer: " + args[i]);
                }

            } else if (arg.equals("--density") || arg.equals("-d")) {
                if (i + 1 >= args.length) {
                    throw new InvalidAlgorithmParameterException("Missing argument: -d/--density requires a value.");
                }
                try {
                    density = Double.parseDouble(args[++i]);
                } catch (NumberFormatException e) {
                    throw new InvalidAlgorithmParameterException("Density should be a valid decimal: " + args[i]);
                }

            } else if (arg.equals("--seed") || arg.equals("-s")) {
                if (i + 1 >= args.length) {
                    throw new InvalidAlgorithmParameterException("Missing argument: -s/--seed requires a value.");
                }
                try {
                    seed = Long.parseLong(args[++i]);
                } catch (NumberFormatException e) {
                    throw new InvalidAlgorithmParameterException("Seed should be a valid integer: " + args[i]);
                }

            } else if (arg.equals("--target") || arg.equals("-t")) {
                if (i + 1 >= args.length) {
                    throw new InvalidAlgorithmParameterException("Missing argument: -t/--target requires a value.");
                }
                try {
                    target = Integer.parseInt(args[++i]);
                } catch (NumberFormatException e) {
                    throw new InvalidAlgorithmParameterException("Target should be a valid integer: " + args[i]);
                }

            } else if (arg.equals("--directed")) {
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

            } else if (arg.equals("--forward-star")) {
                representation = "Forward Star";

            } else if (arg.equals("--incidence-matrix")) {
                throw new InvalidAlgorithmParameterException("Invalid argument: Incidence Matrix not implemented yet.");

            } else if (arg.equals("--adjacency-matrix")) {
                throw new InvalidAlgorithmParameterException("Invalid argument: Adjacency Matrix not implemented yet.");

            } else if (arg.equals("--adjacency-list")) {
                throw new InvalidAlgorithmParameterException("Invalid argument: Adjacency List not implemented yet.");

            } else {
                throw new InvalidAlgorithmParameterException("Unknown argument: " + arg);
            }
        }

        switch (representation) {
            default:
                builder = new ForwardStarGraphBuilder(isDirected);
                break;
        }

        if (isRead && target == -1) {
            throw new InvalidAlgorithmParameterException("Missing argument: -t/--target is required for read mode.");
        }

        if (isCreate) {
            if (graphPath == null) {
                throw new InvalidAlgorithmParameterException(
                        "Missing argument: -f/--file is required for create mode.");
            }

            if (vertices <= 0) {
                throw new InvalidAlgorithmParameterException("Missing or invalid argument: -n/--vertices must be > 0.");
            }

            if (edges >= 0 && density >= 0) {
                throw new InvalidAlgorithmParameterException("Cannot specify both -m/--edges and -d/--density.");
            }

            if (edges < 0 && density < 0) {
                throw new InvalidAlgorithmParameterException("Must specify either -m/--edges or -d/--density.");
            }

            if (density >= 0 && (density < 0.0 || density > 1.0)) {
                throw new InvalidAlgorithmParameterException("Density must be between 0.0 and 1.0.");
            }

            long maxEdges = isDirected ? (long) vertices * (vertices - 1) : (long) vertices * (vertices - 1) / 2;
            if (edges >= 0 && edges > maxEdges) {
                throw new InvalidAlgorithmParameterException("Edges must be between 0 and " + maxEdges + ".");
            }
        }
    }

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
                            .seed(seed != null ? seed : null)
                            .build();
                } else {
                    config = GraphGenerator.builder()
                            .directed(isDirected)
                            .connectivity(connectivity)
                            .vertices(vertices)
                            .density(density)
                            .outputPath(graphPath)
                            .seed(seed != null ? seed : null)
                            .build();
                }

                long maxEdges = config.maxEdges();
                System.out.printf("  Vertices: %,d\n", vertices);
                if (density >= 0) {
                    System.out.printf("  Density: %.2f\n", density);
                }
                if (seed != null) {
                    System.out.printf("  Seed: %d\n", seed);
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
package graph;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import graph.api.Graph;
import graph.cli.GraphGenerator;
import graph.cli.GraphLogger;
import graph.cli.GraphReader;
import graph.cli.GraphGenerator.ConnectivityType;
import graph.representations.GraphBuilder;
import graph.representations.adjacencylist.AdjacencyListGraphBuilder;
import graph.util.Usage;
import graph.representations.adjacencymatrix.AdjacencyMatrixGraphBuilder;
import graph.representations.forwardstar.ForwardStarGraphBuilder;

public class GraphCLI {

    // CLI details
    static String subcommand = null;
    static String graphPath = null;
    static String outputPath = null;

    // Graph details
    static boolean isDirected = true;
    static Graph graph = null;
    static String representation = "Forward Star";
    static GraphBuilder builder;

    // Generator details
    static Integer target = null;
    static int vertices = -1;
    static double density = -1.0;
    static long edges = -1;
    static ConnectivityType connectivity = ConnectivityType.CONNECTED;
    static Long seed = null;

    // Reader details
    static Set<String> algorithms = new HashSet<>();
    static final Set<String> VALID_ALGORITHMS = Set.of("--dfs", "--kosaraju", "--fleury", "--naive-bridges");
    static final Map<String, String> ALGORITHM_NAMES = Map.of(
            "--dfs", "DFS",
            "--kosaraju", "Kosaraju",
            "--fleury", "Fleury",
            "--naive-bridges", "Bridges");

    static boolean isFlagValue(String arg) {
        return !arg.startsWith("-") && !arg.equals("create") && !arg.equals("read") && !arg.equals("help");
    }

    static void processArguments(String[] args) throws InvalidAlgorithmParameterException {
        if (args.length == 0) {
            Usage.printGeneral();
            return;
        }

        String firstArg = args[0];

        if (firstArg.equals("help")) {
            if (args.length == 1) {
                Usage.printGeneral();

            } else if (args[1].equals("create")) {
                Usage.printCreate();

            } else if (args[1].equals("read")) {
                Usage.printRead();

            } else {
                throw new InvalidAlgorithmParameterException("Unknown subcommand: " + args[1]);
            }
            return;
        }

        if (!firstArg.equals("create") && !firstArg.equals("read")) {
            throw new InvalidAlgorithmParameterException(
                    "Unknown subcommand: " + firstArg + ". Use 'create', 'read', or 'help'.");
        }

        subcommand = firstArg;

        if (args.length < 2) {
            throw new InvalidAlgorithmParameterException("Missing path for " + subcommand + " subcommand.");
        }

        if (isFlagValue(args[1])) {
            graphPath = args[1];
        } else {
            throw new InvalidAlgorithmParameterException("Invalid path: " + args[1]);
        }

        for (int i = 2; i < args.length; i++) {
            String arg = args[i];

            if (arg.equals("--directed")) {
                isDirected = true;

            } else if (arg.equals("--undirected") || arg.equals("-u")) {
                isDirected = false;

            } else if (VALID_ALGORITHMS.contains(arg)) {
                algorithms.add(arg);

            } else if (arg.equals("-t") || arg.equals("--target")) {
                if (i + 1 >= args.length || !isFlagValue(args[i + 1])) {
                    throw new InvalidAlgorithmParameterException("Missing value for -t/--target.");
                }

                try {
                    target = Integer.parseInt(args[++i]);

                } catch (NumberFormatException e) {
                    throw new InvalidAlgorithmParameterException("Invalid target: " + args[i]);
                }
            } else if (arg.equals("-o") || arg.equals("--output")) {
                if (i + 1 >= args.length || !isFlagValue(args[i + 1])) {
                    throw new InvalidAlgorithmParameterException("Missing value for -o/--output.");
                }

                outputPath = args[++i];
            } else if (arg.equals("-n") || arg.equals("--vertices")) {
                if (subcommand.equals("read")) {
                    throw new InvalidAlgorithmParameterException("Invalid flag for read: " + arg);
                }

                if (i + 1 >= args.length || !isFlagValue(args[i + 1])) {
                    throw new InvalidAlgorithmParameterException("Missing value for -n/--vertices.");
                }

                try {
                    vertices = Integer.parseInt(args[++i]);

                } catch (NumberFormatException e) {
                    throw new InvalidAlgorithmParameterException("Invalid vertices: " + args[i]);
                }
            } else if (arg.equals("-m") || arg.equals("--edges")) {
                if (subcommand.equals("read")) {
                    throw new InvalidAlgorithmParameterException("Invalid flag for read: " + arg);
                }

                if (i + 1 >= args.length || !isFlagValue(args[i + 1])) {
                    throw new InvalidAlgorithmParameterException("Missing value for -m/--edges.");
                }

                try {
                    edges = Long.parseLong(args[++i]);

                } catch (NumberFormatException e) {
                    throw new InvalidAlgorithmParameterException("Invalid edges: " + args[i]);
                }
            } else if (arg.equals("-d") || arg.equals("--density")) {
                if (subcommand.equals("read")) {
                    throw new InvalidAlgorithmParameterException("Invalid flag for read: " + arg);
                }

                if (i + 1 >= args.length || !isFlagValue(args[i + 1])) {
                    throw new InvalidAlgorithmParameterException("Missing value for -d/--density.");
                }

                try {
                    density = Double.parseDouble(args[++i]);

                } catch (NumberFormatException e) {
                    throw new InvalidAlgorithmParameterException("Invalid density: " + args[i]);
                }
            } else if (arg.equals("-s") || arg.equals("--seed")) {
                if (subcommand.equals("read")) {
                    throw new InvalidAlgorithmParameterException("Invalid flag for read: " + arg);
                }

                if (i + 1 >= args.length || !isFlagValue(args[i + 1])) {
                    throw new InvalidAlgorithmParameterException("Missing value for -s/--seed.");
                }

                try {
                    seed = Long.parseLong(args[++i]);

                } catch (NumberFormatException e) {
                    throw new InvalidAlgorithmParameterException("Invalid seed: " + args[i]);
                }
            } else if (arg.equals("--connected")) {
                if (subcommand.equals("read")) {
                    throw new InvalidAlgorithmParameterException("Invalid flag for read: " + arg);
                }

                connectivity = ConnectivityType.CONNECTED;
            } else if (arg.equals("--disconnected")) {
                if (subcommand.equals("read")) {
                    throw new InvalidAlgorithmParameterException("Invalid flag for read: " + arg);
                }

                connectivity = ConnectivityType.DISCONNECTED;
            } else if (arg.equals("--eulerian")) {
                if (subcommand.equals("read")) {
                    throw new InvalidAlgorithmParameterException("Invalid flag for read: " + arg);
                }

                connectivity = ConnectivityType.EULERIAN;
            } else if (arg.equals("--semi-eulerian")) {
                if (subcommand.equals("read")) {
                    throw new InvalidAlgorithmParameterException("Invalid flag for read: " + arg);
                }

                connectivity = ConnectivityType.SEMI_EULERIAN;
            } else if (arg.equals("--forward-star")) {
                representation = "Forward Star";

            } else if (arg.equals("--adjacency-matrix")) {
                representation = "Adjacency Matrix";

            } else if (arg.equals("--adjacency-list")) {
                representation = "Adjacency List";

            } else if (arg.equals("--incidence-matrix")) {
                throw new InvalidAlgorithmParameterException("Not implemented: " + arg);

            } else {
                throw new InvalidAlgorithmParameterException("Unknown argument: " + arg);
            }
        }

        switch (representation) {
            case "Adjacency Matrix" -> builder = new AdjacencyMatrixGraphBuilder(isDirected);
            case "Adjacency List" -> builder = new AdjacencyListGraphBuilder(isDirected);
            case "Forward Star" -> builder = new ForwardStarGraphBuilder(isDirected);
            default -> builder = new ForwardStarGraphBuilder(isDirected);
        }

        if (subcommand.equals("read")) {
            if (algorithms.isEmpty()) {
                throw new InvalidAlgorithmParameterException("No algorithm specified.");
            }

            if (algorithms.contains("--dfs")) {
                if (target == null) {
                    throw new InvalidAlgorithmParameterException("--dfs requires -t/--target.");
                }
            }
        }

        if (subcommand.equals("create")) {
            if (graphPath == null) {
                throw new InvalidAlgorithmParameterException("Missing graph file path.");
            }

            if (vertices <= 0) {
                throw new InvalidAlgorithmParameterException("-n/--vertices must be > 0.");
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
        try {
            processArguments(args);

            if (subcommand == null) {
                throw new InvalidAlgorithmParameterException("[Error] No subcommand was provided.");
            }

            System.out.printf("\nGraph Configuration:\n");
            System.out.printf("  Subcommand: %s\n", subcommand);
            System.out.printf("  Graph File Path: %s\n", graphPath);
            System.out.printf("  Direction Type: %s\n", isDirected ? "Directed" : "Undirected");

            if (subcommand.equals("create")) {
                GraphGenerator generator = new GraphGenerator(vertices, isDirected, graphPath);
                generator.setConnectivity(connectivity);
                generator.enableLog();

                if (edges >= 0) {
                    generator.setEdges(edges);
                } else {
                    generator.setDensity(density);
                }

                if (seed != null) {
                    generator.setSeed(seed);
                    System.out.printf("  Seed: %d\n", seed);
                }

                System.out.printf("  Connectivity: %s\n", connectivity.toString().toLowerCase());
                System.out.printf("  Vertices: %,d\n", vertices);
                System.out.printf("  Edges: %,d\n", generator.getEdges());
                System.out.printf("  Density: %.2f\n", generator.getDensity());
                System.out.printf("  Max Edges: %,d\n", generator.maxEdges());

                System.out.printf("\nGenerating Graph...\n");
                generator.create();
                System.out.println("\nGraph generation complete.");
            }

            if (subcommand.equals("read")) {
                System.out.printf("  Representation: %s\n", representation);
                System.out.printf("  Algorithms: %s\n", algorithms);

                if (target != null) {
                    System.out.printf("  Target Vertex: %d\n", target);
                }

                if (outputPath != null) {
                    System.out.printf("  Output Path: %s\n", outputPath);
                }

                int totalSteps = 2 + algorithms.size();
                int step = 0;

                System.out.printf("\n[%d/%d] Reading File...", ++step, totalSteps);
                GraphLogger.logTime(() -> {
                    try {
                        GraphReader.readFile(graphPath, builder);

                    } catch (Exception e) {
                        System.err.println(e.getMessage());
                    }
                });

                System.out.printf("[%d/%d] Building Graph...", ++step, totalSteps);
                GraphLogger.logTime(() -> graph = builder.build());

                StringBuilder allResults = new StringBuilder();

                for (String algorithm : algorithms) {
                    String algorithmName = ALGORITHM_NAMES.get(algorithm);

                    System.out.printf("[%d/%d] Running %s...", ++step, totalSteps, algorithmName);
                    GraphLogger.logTime(() -> {
                        try {
                            String result;

                            switch (algorithm) {
                                case "--dfs" -> result = GraphLogger.runDFS(graph, target, outputPath);
                                case "--kosaraju" -> result = GraphLogger.runKosaraju(graph, outputPath);
                                case "--fleury" -> result = GraphLogger.runFleury(graph, outputPath);
                                case "--naive-bridges" -> result = GraphLogger.runNaiveBridges(graph, outputPath);
                                default -> throw new RuntimeException("Unknown algorithm: " + algorithm);
                            }

                            allResults.append(result);

                        } catch (RuntimeException e) {
                            System.err.printf("[Error] " + e.getMessage());
                        } catch (Exception e) {
                            System.err.println(e.getMessage());
                        }
                    });
                }

                if (outputPath == null) {
                    System.out.print(allResults.toString());
                } else {
                    try {
                        GraphLogger.writeToFile(outputPath, allResults.toString());

                    } catch (IOException e) {
                        System.err.printf("[Error] Fail to write output: " + e.getMessage());
                    }
                }
            }
        } catch (InvalidAlgorithmParameterException e) {
            System.err.println(e.getMessage());
            System.out.println();
            if (subcommand != null && subcommand.equals("create")) {
                Usage.printCreate();
            } else if (subcommand != null && subcommand.equals("read")) {
                Usage.printRead();
            } else {
                Usage.printGeneral();
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
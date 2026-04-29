package graph.cli;

import graph.api.ConnectivityType;
import graph.cli.create.CreateConfig;
import graph.cli.read.AlgorithmRequest;
import graph.cli.read.ReadConfig;
import graph.util.Usage;

import java.security.InvalidAlgorithmParameterException;
import java.util.ArrayList;
import java.util.List;

public class CliParser {

    public static CliCommand parse(String[] args) throws InvalidAlgorithmParameterException {
        if (args.length == 0) {
            Usage.printGeneral();
            return null;
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
            return null;
        }

        if (!firstArg.equals("create") && !firstArg.equals("read")) {
            throw new InvalidAlgorithmParameterException(
                    "Unknown subcommand: " + firstArg + ". Use 'create', 'read', or 'help'.");
        }

        String subcommand = firstArg;

        if (args.length < 2) {
            throw new InvalidAlgorithmParameterException("Missing path for " + subcommand + " subcommand.");
        }

        String graphPath = args[1];
        if (graphPath.startsWith("-")) {
            throw new InvalidAlgorithmParameterException("Invalid path: " + graphPath);
        }

        if (subcommand.equals("create")) {
            return parseCreate(args, graphPath);
        } else {
            return parseRead(args, graphPath);
        }
    }

    private static CreateCommand parseCreate(String[] args, String graphPath)
            throws InvalidAlgorithmParameterException {
        int vertices = -1;
        double density = -1.0;
        long edges = -1;
        ConnectivityType connectivity = ConnectivityType.CONNECTED;
        boolean isDirected = true;
        Long seed = null;

        for (int i = 2; i < args.length; i++) {
            String arg = args[i];

            if (arg.equals("--directed")) {
                isDirected = true;
            } else if (arg.equals("--undirected") || arg.equals("-u")) {
                isDirected = false;
            } else if (arg.equals("-n") || arg.equals("--vertices")) {
                if (i + 1 >= args.length || args[i + 1].startsWith("-")) {
                    throw new InvalidAlgorithmParameterException("Missing value for -n/--vertices.");
                }
                try {
                    vertices = Integer.parseInt(args[++i]);
                } catch (NumberFormatException e) {
                    throw new InvalidAlgorithmParameterException("Invalid vertices: " + args[i]);
                }
            } else if (arg.equals("-m") || arg.equals("--edges")) {
                if (i + 1 >= args.length || args[i + 1].startsWith("-")) {
                    throw new InvalidAlgorithmParameterException("Missing value for -m/--edges.");
                }
                try {
                    edges = Long.parseLong(args[++i]);
                } catch (NumberFormatException e) {
                    throw new InvalidAlgorithmParameterException("Invalid edges: " + args[i]);
                }
            } else if (arg.equals("-d") || arg.equals("--density")) {
                if (i + 1 >= args.length || args[i + 1].startsWith("-")) {
                    throw new InvalidAlgorithmParameterException("Missing value for -d/--density.");
                }
                try {
                    density = Double.parseDouble(args[++i]);
                } catch (NumberFormatException e) {
                    throw new InvalidAlgorithmParameterException("Invalid density: " + args[i]);
                }
            } else if (arg.equals("-s") || arg.equals("--seed")) {
                if (i + 1 >= args.length || args[i + 1].startsWith("-")) {
                    throw new InvalidAlgorithmParameterException("Missing value for -s/--seed.");
                }
                try {
                    seed = Long.parseLong(args[++i]);
                } catch (NumberFormatException e) {
                    throw new InvalidAlgorithmParameterException("Invalid seed: " + args[i]);
                }
            } else if (arg.equals("--connected")) {
                connectivity = ConnectivityType.CONNECTED;
            } else if (arg.equals("--disconnected")) {
                connectivity = ConnectivityType.DISCONNECTED;
            } else if (arg.equals("--eulerian")) {
                connectivity = ConnectivityType.EULERIAN;
            } else if (arg.equals("--semi-eulerian")) {
                connectivity = ConnectivityType.SEMI_EULERIAN;
            } else {
                throw new InvalidAlgorithmParameterException("Unknown argument: " + arg);
            }
        }

        // Validate
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
        if (connectivity == ConnectivityType.EULERIAN && edges < vertices) {
            throw new IllegalArgumentException(
                    "Eulerian graph with " + vertices + " vertices requires at least " + vertices + " edges");
        }

        CreateConfig config = new CreateConfig(graphPath, vertices, edges, density, seed, connectivity, isDirected);
        return new CreateCommand(config);
    }

    private static ReadCommand parseRead(String[] args, String graphPath) throws InvalidAlgorithmParameterException {
        boolean isDirected = true;
        String representation = "Forward Star";
        Integer target = null;
        String outputPath = null;
        List<AlgorithmRequest> algorithms = new ArrayList<>();

        for (int i = 2; i < args.length; i++) {
            String arg = args[i];

            if (arg.equals("--directed")) {
                isDirected = true;
            } else if (arg.equals("--undirected") || arg.equals("-u")) {
                isDirected = false;
            } else if (arg.equals("--dfs")) {
                algorithms.add(AlgorithmRequest.dfs(0)); // target set later
            } else if (arg.equals("--kosaraju")) {
                algorithms.add(AlgorithmRequest.kosaraju());
            } else if (arg.equals("--fleury")) {
                algorithms.add(AlgorithmRequest.fleury(null));
            } else if (arg.equals("--tarjan")) {
                algorithms.add(AlgorithmRequest.tarjan());
            } else if (arg.equals("--naive-local")) {
                algorithms.add(AlgorithmRequest.naiveLocalBridges());
            } else if (arg.equals("--naive-global")) {
                algorithms.add(AlgorithmRequest.naiveBridges());
            } else if (arg.equals("-t") || arg.equals("--target")) {
                if (i + 1 >= args.length || args[i + 1].startsWith("-")) {
                    throw new InvalidAlgorithmParameterException("Missing value for -t/--target.");
                }
                try {
                    target = Integer.parseInt(args[++i]);
                } catch (NumberFormatException e) {
                    throw new InvalidAlgorithmParameterException("Invalid target: " + args[i]);
                }
            } else if (arg.equals("-o") || arg.equals("--output")) {
                if (i + 1 >= args.length || args[i + 1].startsWith("-")) {
                    throw new InvalidAlgorithmParameterException("Missing value for -o/--output.");
                }
                outputPath = args[++i];
            } else if (arg.equals("--forward-star")) {
                representation = "Forward Star";
            } else if (arg.equals("--adjacency-matrix")) {
                representation = "Adjacency Matrix";
            } else if (arg.equals("--adjacency-list")) {
                representation = "Adjacency List";
            } else {
                throw new InvalidAlgorithmParameterException("Unknown argument: " + arg);
            }
        }

        // Validate
        if (algorithms.isEmpty()) {
            throw new InvalidAlgorithmParameterException("No algorithm specified.");
        }

        // Set target for --dfs
        if (algorithms.stream().anyMatch(r -> r.name().equals("--dfs"))) {
            if (target == null) {
                throw new InvalidAlgorithmParameterException("--dfs requires -t/--target.");
            }
            for (int j = 0; j < algorithms.size(); j++) {
                if (algorithms.get(j).name().equals("--dfs")) {
                    algorithms.set(j, AlgorithmRequest.dfs(target));
                }
            }
        }

        // Resolve bridge finder for --fleury
        boolean hasTarjan = algorithms.stream().anyMatch(r -> r.name().equals("--tarjan"));
        boolean hasNaiveGlobalAlgo = algorithms.stream().anyMatch(r -> r.name().equals("--naive-global"));
        for (int j = 0; j < algorithms.size(); j++) {
            if (algorithms.get(j).name().equals("--fleury")) {
                if (hasTarjan) {
                    algorithms.set(j, AlgorithmRequest.fleury(graph.algorithms.Fleury.BridgeFinder.TARJAN));
                } else if (hasNaiveGlobalAlgo) {
                    algorithms.set(j, AlgorithmRequest.fleury(graph.algorithms.Fleury.BridgeFinder.NAIVE_GLOBAL));
                } else {
                    algorithms.set(j, AlgorithmRequest.fleury(graph.algorithms.Fleury.BridgeFinder.NAIVE_LOCAL));
                }
            }
        }

        ReadConfig config = new ReadConfig(graphPath, representation, isDirected, algorithms, outputPath);
        return new ReadCommand(config);
    }
}

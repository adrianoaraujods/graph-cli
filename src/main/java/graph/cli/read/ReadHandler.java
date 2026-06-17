package graph.cli.read;

import graph.api.Graph;
import graph.cli.read.result.AlgorithmResult;

import java.util.ArrayList;
import java.util.List;

public class ReadHandler {

    public static List<AlgorithmOutput> run(ReadConfig config) throws Exception {
        System.out.printf("\nGraph Configuration:\n");
        System.out.printf("  Subcommand: read\n");
        System.out.printf("  Graph File Path: %s\n", config.graphPath());
        System.out.printf("  Direction Type: %s\n", config.isDirected() ? "Directed" : "Undirected");
        System.out.printf("  Representation: %s\n", config.representation());

        // Check for algorithm combinations
        boolean hasFleury = config.algorithms().stream().anyMatch(r -> r.name().equals("--fleury"));
        boolean hasTarjan = config.algorithms().stream().anyMatch(r -> r.name().equals("--tarjan"));
        boolean hasNaiveLocal = config.algorithms().stream().anyMatch(r -> r.name().equals("--naive-local"));
        boolean hasNaiveGlobal = config.algorithms().stream().anyMatch(r -> r.name().equals("--naive-global"));

        boolean readFlag = false;
        for (AlgorithmRequest req : config.algorithms()) {
            if (req.name().equals("--gonzalez") || req.name().equals("--fastmap") ||
                    req.name().equals("--wva-ig") || req.name().equals("--exact")) {
                if ((int) req.params().get("k") == 0) {
                    readFlag = true;
                    break;
                }
            }
        }
        // Display algorithms with bridge finder info
        System.out.printf("  Algorithms: %s\n",
                config.algorithms().stream()
                        .map(r -> {
                            String displayName = getAlgorithmDisplayName(r.name());
                            if (r.name().equals("--fleury")) {
                                if (hasTarjan) {
                                    displayName += " (Tarjan)";
                                } else if (hasNaiveGlobal) {
                                    displayName += " (Naive Global)";
                                } else {
                                    displayName += " (Naive Local)";
                                }
                            }
                            return displayName;
                        })
                        .toList());

        if (config.algorithms().stream().anyMatch(r -> r.name().equals("--dfs"))) {
            AlgorithmRequest dfsReq = config.algorithms().stream()
                    .filter(r -> r.name().equals("--dfs"))
                    .findFirst().get();
            System.out.printf("  Target Vertex: %d\n", dfsReq.params().get("target"));
        }

        if (config.outputPath() != null) {
            System.out.printf("  Output Path: %s\n", config.outputPath());
        }

        int totalSteps = 2 + config.algorithms().size();

        if (hasFleury && (hasTarjan || hasNaiveGlobal || hasNaiveLocal)) {
            totalSteps--; // bridge finder counts as one step
        }

        int step = 0;
        System.out.printf("\n[%d/%d] Building graph from the file...", ++step, totalSteps);
        GraphLoaderReturn loader = GraphLoader.load(
                config.graphPath(),
                config.representation(),
                config.isDirected(),
                config.isWeighted(),
                config.hasCapacity(),
                readFlag);

        Graph graph = loader.graph();
        int kFromFile = loader.flag();

        StringBuilder allResults = new StringBuilder();
        List<AlgorithmOutput> outputs = new ArrayList<>();

        // Run algorithms one at a time, printing "Running..." before each
        for (AlgorithmRequest request : config.algorithms()) {
            String algorithmName = getAlgorithmDisplayName(request.name());

            // Inject the 'k' value from the file if this is a k-center algorithm and
            // --k-centers was used
            if (readFlag && (request.name().equals("--gonzalez") || request.name().equals("--fastmap") ||
                    request.name().equals("--wva-ig") || request.name().equals("--exact"))) {
                // Rebuild the request with the file's k value
                request = new AlgorithmRequest(request.name(), java.util.Map.of("k", kFromFile));
            }

            // Skip bridge finder if fleury is present (it's shown as part of Fleury)
            if (request.name().equals("--tarjan") && hasFleury) {
                continue;
            }
            if ((request.name().equals("--naive-local") || request.name().equals("--naive-global")) && hasFleury) {
                continue;
            }

            System.out.printf("\n[%d/%d] Running %s...", ++step, totalSteps, algorithmName);
            AlgorithmResult result = AlgorithmRunner.runSingle(graph, request, hasTarjan, hasNaiveGlobal, hasFleury);
            outputs.add(new AlgorithmOutput(request.name(), result));
            allResults.append(result.toString());
        }

        if (config.outputPath() == null) {
            System.out.print(allResults.toString());
        } else {
            try (java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileWriter(config.outputPath()))) {
                writer.print(allResults.toString());
            } catch (java.io.IOException e) {
                System.err.printf("[Error] Fail to write output: " + e.getMessage());
            }
        }

        return outputs;
    }

    private static String getAlgorithmDisplayName(String algorithm) {
        return switch (algorithm) {
            case "--dfs" -> "DFS";
            case "--kosaraju" -> "Kosaraju";
            case "--fleury" -> "Fleury";
            case "--tarjan" -> "Tarjan";
            case "--naive-local" -> "Naive Bridges (Local)";
            case "--naive-global" -> "Naive Bridges (Global)";
            case "--disjoint-paths" -> "Disjoint Paths";
            case "--dinic" -> "Dinic";
            case "--floyd-warshall" -> "Floyd-Warshall";
            case "--gonzalez" -> "González's Farthest-First Traversal (2-Approximation)";
            case "--fastmap" -> "Spectral / Embedding-Based Initialization";
            case "--wva-ig" -> "Worst-Vertex-Anchored Iterated Greedy";
            case "--exact" -> "KCenter - Brute Force (Exact)";
            default -> algorithm;
        };
    }
}

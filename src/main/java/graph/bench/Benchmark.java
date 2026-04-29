package graph.bench;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import graph.algorithms.Fleury;
import graph.algorithms.Fleury.BridgeFinder;
import graph.algorithms.Fleury.EulerianPath;
import graph.api.ConnectivityType;
import graph.api.UndirectedGraph;
import graph.cli.create.GraphGenerator;
import graph.cli.GraphReader;
import graph.representations.GraphBuilder;
import graph.representations.adjacencylist.AdjacencyListGraphBuilder;
import graph.representations.forwardstar.ForwardStarGraphBuilder;

public class Benchmark {

    private static final int MAX_RETRIES = 3;

    private static int attempts = 10;
    private static boolean parallel = false;
    private static int maxThreads = 24;
    private static String outputPath = "results.csv";

    private static final int[][] GRAPHS = {
            { 100, 250 },
            { 1000, 2500 },
            { 10000, 25000 },
            { 100000, 250000 }
    };
    private static final ConnectivityType[] CONNECTIVITIES = {
            ConnectivityType.EULERIAN,
            ConnectivityType.SEMI_EULERIAN,
            ConnectivityType.CONNECTED
    };
    private static final String[] REPRESENTATIONS = { "Adjacency List", "Forward Star" };
    private static final String[] ALGORITHMS = { "Tarjan", "Naive Local", "Naive Global" };

    private static final ReentrantLock fileLock = new ReentrantLock();

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down, cleaning up temp files...");
            cleanupTempFiles();
        }));

        parseArguments(args);
        printConfig();
        writeHeader();

        if (parallel) {
            runParallel();
        } else {
            runSequential();
        }

        System.out.println("Benchmark complete!");
    }

    private static void cleanupTempFiles() {
        try {
            var tempDir = java.nio.file.Paths.get("").toFile();
            var files = tempDir.listFiles((dir, name) -> name.startsWith("temp_benchmark_") && name.endsWith(".txt"));
            if (files != null) {
                for (var file : files) {
                    if (file.delete()) {
                        System.out.println("Deleted: " + file.getName());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error cleaning up temp files: " + e.getMessage());
        }
    }

    private static void parseArguments(String[] args) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            switch (arg) {
                case "--attempts" -> attempts = Integer.parseInt(args[++i]);
                case "--parallel" -> parallel = true;
                case "--max-threads" -> maxThreads = Integer.parseInt(args[++i]);
                case "--output" -> outputPath = args[++i];
                default -> throw new IllegalArgumentException("Unknown argument: " + arg);
            }
        }
    }

    private static void printConfig() {
        System.out.println("=== Benchmark Configuration ===");
        System.out.println("Attempts: " + attempts);
        System.out.println("Parallel: " + parallel);
        System.out.println("Max Threads: " + maxThreads);
        System.out.println("Output: " + outputPath);
        System.out.println("==================================");
    }

    private static void writeHeader() {
        try {
            boolean fileExists = java.nio.file.Paths.get(outputPath).toFile().exists();
            try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(outputPath, fileExists)))) {
                if (!fileExists) {
                    writer.println(
                            "attempt,vertices,edges,connectivity,representation,generation_time_ms,fleury_time_ms,path_length,eulerian_type,algorithm");
                }
            }
        } catch (IOException e) {
            System.err.println("Error writing header: " + e.getMessage());
        }
    }

    private static void runSequential() {
        int totalRuns = getTotalRuns();
        AtomicInteger completed = new AtomicInteger(0);

        for (String algo : ALGORITHMS) {
            for (int[] graph : GRAPHS) {
                int n = graph[0];
                int m = graph[1];
                for (String rep : REPRESENTATIONS) {
                    for (ConnectivityType conn : CONNECTIVITIES) {
                        for (int attempt = 1; attempt <= attempts; attempt++) {
                            int current = completed.incrementAndGet();
                            System.out.printf("[%d/%d] Running: n=%d, m=%d, conn=%s, rep=%s, algo=%s, attempt=%d%n",
                                    current, totalRuns, n, m, conn, rep, algo, attempt);

                            runSingle(n, m, conn, rep, algo, attempt);
                        }
                    }
                }
            }
        }
    }

    private static void runParallel() {
        List<Runnable> tasks = new ArrayList<>();
        int totalRuns = getTotalRuns();
        AtomicInteger completed = new AtomicInteger(0);

        for (String algo : ALGORITHMS) {
            for (int[] graph : GRAPHS) {
                int n = graph[0];
                int m = graph[1];
                for (String rep : REPRESENTATIONS) {
                    for (ConnectivityType conn : CONNECTIVITIES) {
                        for (int attempt = 1; attempt <= attempts; attempt++) {
                            int finalN = n;
                            int finalM = m;
                            ConnectivityType finalConn = conn;
                            String finalRep = rep;
                            String finalAlgo = algo;
                            int finalAttempt = attempt;

                            tasks.add(() -> {
                                int current = completed.incrementAndGet();
                                System.out.printf("[%d/%d] Running: n=%d, m=%d, conn=%s, rep=%s, algo=%s, attempt=%d%n",
                                        current, totalRuns, finalN, finalM, finalConn, finalRep, finalAlgo,
                                        finalAttempt);
                                try {
                                    runSingle(finalN, finalM, finalConn, finalRep, finalAlgo, finalAttempt);
                                } catch (Throwable t) {
                                    System.err.printf("CRASHED: n=%d, m=%d, conn=%s, rep=%s, algo=%s, attempt=%d: %s%n",
                                            finalN, finalM, finalConn, finalRep, finalAlgo, finalAttempt,
                                            t.getMessage());
                                    writeResult(finalAttempt, finalN, finalM, finalConn.name(), finalRep, -1, -1, -1,
                                            "CRASHED", finalAlgo);
                                }
                            });
                        }
                    }
                }
            }
        }

        System.out.println("Total tasks: " + tasks.size());

        ExecutorService executor = Executors.newFixedThreadPool(maxThreads);
        for (Runnable task : tasks) {
            executor.submit(task);
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static int getTotalRuns() {
        return GRAPHS.length * CONNECTIVITIES.length * REPRESENTATIONS.length * ALGORITHMS.length * attempts;
    }

    private static void runSingle(int n, int m, ConnectivityType conn, String rep, String algo, int attempt) {
        long generationTime = -1;
        long fleuryTime = -1;
        int pathLength = -1;
        String eulerianType = "FAILED";
        String tempFile = null;
        int createdEdges = m;

        for (int retry = 0; retry < MAX_RETRIES; retry++) {
            try {
                long seed = (long) (Math.random() * Long.MAX_VALUE);
                tempFile = "temp_benchmark_" + seed + ".txt";

                GraphGenerator generator = new GraphGenerator(n, false, tempFile);
                generator.setConnectivity(conn);
                generator.setEdges(m);
                generator.setSeed(seed);

                long startGenerate = System.currentTimeMillis();
                createdEdges = (int) generator.create();
                generationTime = System.currentTimeMillis() - startGenerate;

                GraphBuilder builder = switch (rep) {
                    case "Adjacency List" -> new AdjacencyListGraphBuilder(false);
                    case "Forward Star" -> new ForwardStarGraphBuilder(false);
                    default -> throw new IllegalArgumentException("Unknown representation: " + rep);
                };

                GraphReader.readFile(tempFile, builder);
                UndirectedGraph graph = (UndirectedGraph) builder.build();

                long startFleury = System.currentTimeMillis();
                EulerianPath eulerianPath;
                switch (algo) {
                    case "Tarjan" ->
                        eulerianPath = Fleury.findEulerianPath(graph, BridgeFinder.TARJAN, false);
                    case "Naive Global" ->
                        eulerianPath = Fleury.findEulerianPath(graph, BridgeFinder.NAIVE_GLOBAL, false);
                    case "Naive Local" ->
                        eulerianPath = Fleury.findEulerianPath(graph, BridgeFinder.NAIVE_LOCAL, false);
                    default -> throw new IllegalArgumentException("Unknown algorithm: " + algo);
                }

                fleuryTime = System.currentTimeMillis() - startFleury;

                pathLength = eulerianPath.path().length;
                eulerianType = eulerianPath.type().name();

                break;
            } catch (Exception e) {
                if (retry < MAX_RETRIES - 1) {
                    System.err.printf("Retry %d/%d for n=%d, m=%d, conn=%s, rep=%s, algo=%s: %s%n",
                            retry + 1, MAX_RETRIES, n, createdEdges, conn, rep, algo, e.getMessage());
                } else {
                    System.err.printf("FAILED after %d retries for n=%d, m=%d, conn=%s, rep=%s, algo=%s: %s%n",
                            MAX_RETRIES, n, createdEdges, conn, rep, algo, e.getMessage());
                    eulerianType = "RETRY_FAILED";
                }
            } finally {
                if (tempFile != null) {
                    try {
                        Files.deleteIfExists(Paths.get(tempFile));
                    } catch (IOException e) {
                        System.err.println("Failed to delete temp file: " + tempFile);
                    }
                }
            }
        }

        writeResult(attempt, n, createdEdges, conn.name(), rep, generationTime, fleuryTime, pathLength, eulerianType,
                algo);
    }

    private static void writeResult(int attempt, int vertices, int edges, String connectivity,
            String representation, long generationTime, long fleuryTime, int pathLength,
            String eulerianType, String algorithm) {
        fileLock.lock();
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(outputPath, true)))) {
            writer.printf("%d,%d,%d,%s,%s,%d,%d,%d,%s,%s%n",
                    attempt, vertices, edges, connectivity, representation, generationTime, fleuryTime, pathLength,
                    eulerianType, algorithm);
        } catch (IOException e) {
            System.err.println("Error writing result: " + e.getMessage());
        } finally {
            fileLock.unlock();
        }
    }
}
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

import graph.algorithms.Dijkstra;
import graph.api.ConnectivityType;
import graph.api.WeightedGraph;
import graph.cli.create.GraphGenerator;
import graph.cli.GraphReader;
import graph.representations.forwardstar.ForwardStarGraphBuilder;
import graph.cli.read.result.ShortestPathResult;

public class DijkstraBenchmark {

    private static final int MAX_RETRIES = 3;

    private static int attempts = 10;
    private static boolean parallel = false;
    private static int maxThreads = 24;
    private static String outputPath = "results.csv";

    private static final int[][] GRAPHS = {
            { 1_000, 100_000 },
            { 10_000, 1_000_000 },
            { 100_000, 10_000_000 },
            { 1_000_000, 100_000_000 }
    };
    private static final ConnectivityType[] CONNECTIVITIES = {
            ConnectivityType.STRONGLY_CONNECTED,
            ConnectivityType.CONNECTED,
    };
    private static final String[] REPRESENTATIONS = { "Forward Star" };

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
            var tempDir = Paths.get("").toFile();
            var files = tempDir
                    .listFiles((dir, name) -> name.startsWith("temp_dijkstra_benchmark_") && name.endsWith(".txt"));
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
        System.out.println("=== Dijkstra Benchmark Configuration ===");
        System.out.println("Attempts: " + attempts);
        System.out.println("Parallel: " + parallel);
        System.out.println("Max Threads: " + maxThreads);
        System.out.println("Output: " + outputPath);
        System.out.println("==================================");
    }

    private static void writeHeader() {
        try {
            boolean fileExists = Paths.get(outputPath).toFile().exists();
            try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(outputPath, fileExists)))) {
                if (!fileExists) {
                    writer.println(
                            "attempt,vertices,edges,connectivity,representation,generation_time_ms,dijkstra_time_ms,distance_to_target,path_length");
                }
            }
        } catch (IOException e) {
            System.err.println("Error writing header: " + e.getMessage());
        }
    }

    private static void runSequential() {
        int totalRuns = getTotalRuns();
        AtomicInteger completed = new AtomicInteger(0);

        for (String rep : REPRESENTATIONS) {
            for (int[] graph : GRAPHS) {
                int n = graph[0];
                int m = graph[1];
                for (ConnectivityType conn : CONNECTIVITIES) {
                    for (int attempt = 1; attempt <= attempts; attempt++) {
                        int current = completed.incrementAndGet();
                        System.out.printf("[%d/%d] Running: n=%d, m=%d, conn=%s, rep=%s, attempt=%d%n",
                                current, totalRuns, n, m, conn, rep, attempt);

                        runSingle(n, m, conn, rep, attempt);
                    }
                }
            }
        }
    }

    private static void runParallel() {
        List<Runnable> tasks = new ArrayList<>();
        int totalRuns = getTotalRuns();
        AtomicInteger completed = new AtomicInteger(0);

        for (String rep : REPRESENTATIONS) {
            for (int[] graph : GRAPHS) {
                int n = graph[0];
                int m = graph[1];
                for (ConnectivityType conn : CONNECTIVITIES) {
                    for (int attempt = 1; attempt <= attempts; attempt++) {
                        int finalN = n;
                        int finalM = m;
                        ConnectivityType finalConn = conn;
                        String finalRep = rep;
                        int finalAttempt = attempt;

                        tasks.add(() -> {
                            int current = completed.incrementAndGet();
                            System.out.printf("[%d/%d] Running: n=%d, m=%d, conn=%s, rep=%s, attempt=%d%n",
                                    current, totalRuns, finalN, finalM, finalConn, finalRep,
                                    finalAttempt);
                            try {
                                runSingle(finalN, finalM, finalConn, finalRep, finalAttempt);
                            } catch (Throwable t) {
                                System.err.printf("CRASHED: n=%d, m=%d, conn=%s, rep=%s, attempt=%d: %s%n",
                                        finalN, finalM, finalConn, finalRep, finalAttempt,
                                        t.getMessage());
                                writeResult(finalAttempt, finalN, finalM, finalConn.name(), finalRep, -1, -1, -1, -1);
                            }
                        });
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
        return GRAPHS.length * CONNECTIVITIES.length * REPRESENTATIONS.length * attempts;
    }

    private static void runSingle(int n, int m, ConnectivityType conn, String rep, int attempt) {
        long generationTime = -1;
        long dijkstraTime = -1;
        long distanceToTarget = -1;
        int pathLength = -1;
        String tempFile = null;
        long createdEdges = m;

        for (int retry = 0; retry < MAX_RETRIES; retry++) {
            try {
                long seed = (long) (Math.random() * Long.MAX_VALUE);
                tempFile = "temp_dijkstra_benchmark_" + seed + ".txt";

                GraphGenerator generator = new GraphGenerator(n, true, tempFile);
                generator.setConnectivity(conn);
                generator.setEdges(m);
                generator.setWeights(1, 100);
                generator.setSeed(seed);
                generator.setSkipConfirmation(true);

                long startGenerate = System.currentTimeMillis();
                createdEdges = generator.create();
                generationTime = System.currentTimeMillis() - startGenerate;

                ForwardStarGraphBuilder builder = new ForwardStarGraphBuilder(true);
                GraphReader.readFile(tempFile, builder, true);
                WeightedGraph graph = (WeightedGraph) builder.build();

                int source = 1;
                int target = 1 + (int) (Math.random() * n);
                long startDijkstra = System.currentTimeMillis();
                ShortestPathResult result = Dijkstra.compute(graph, source, target, true);
                dijkstraTime = System.currentTimeMillis() - startDijkstra;

                distanceToTarget = result.distances()[target - 1];
                if (distanceToTarget == Integer.MAX_VALUE) {
                    distanceToTarget = -1;
                    pathLength = -1;
                } else {
                    pathLength = 0;
                    if (source == target) {
                        pathLength = 0;
                    } else {
                        int current = target - 1;
                        while (current != -1 && current != (source - 1)) {
                            pathLength++;
                            current = result.parents()[current];
                        }
                        if (current == -1) {
                            pathLength = -1;
                        }
                    }
                }

                break;
            } catch (Exception e) {
                if (retry < MAX_RETRIES - 1) {
                    System.err.printf("Retry %d/%d for n=%d, m=%d, conn=%s, rep=%s, attempt=%d: %s%n",
                            retry + 1, MAX_RETRIES, n, createdEdges, conn, rep, attempt, e.getMessage());
                } else {
                    System.err.printf("FAILED after %d retries for n=%d, m=%d, conn=%s, rep=%s, attempt=%d: %s%n",
                            MAX_RETRIES, n, createdEdges, conn, rep, attempt, e.getMessage());
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

        writeResult(attempt, n, (int) createdEdges, conn.name(), rep, generationTime, dijkstraTime, distanceToTarget,
                pathLength);
    }

    private static void writeResult(int attempt, int vertices, int edges, String connectivity,
            String representation, long generationTime, long dijkstraTime,
            long distanceToTarget, int pathLength) {
        fileLock.lock();
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(outputPath, true)))) {
            writer.printf("%d,%d,%d,%s,%s,%d,%d,%d,%d%n",
                    attempt, vertices, edges, connectivity, representation, generationTime, dijkstraTime,
                    distanceToTarget, pathLength);
        } catch (IOException e) {
            System.err.println("Error writing result: " + e.getMessage());
        } finally {
            fileLock.unlock();
        }
    }
}

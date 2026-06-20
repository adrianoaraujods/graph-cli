package graph.bench;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import graph.algorithms.FloydWarshall;
import graph.algorithms.KCenter;
import graph.algorithms.KCenterExactSolver;
import graph.api.WeightedGraph;
import graph.cli.GraphReader;
import graph.cli.read.result.AllPairsShortestPathResult;
import graph.representations.forwardstar.ForwardStarGraphBuilder;
import graph.util.KCenterUtils;

public class KCenterBenchmark {

    private static int attempts = 5;
    private static boolean parallel = false;
    private static int maxThreads = 24;
    private static String outputPath = "results.csv";
    private static String pmedDir = "examples/pmed";
    private static int maxNForExact = 300;
    private static int[] instanceFilter = null;
    private static Set<String> algorithmFilter = null;

    private static final int[] INSTANCES = {
            1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
            11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
            21, 22, 23, 24, 25, 26, 27, 28, 29, 30,
            31, 32, 33, 34, 35, 36, 37, 38, 39, 40
    };

    private static final int[] OPTIMAL_RADII = {
            0,
            127, 98, 93, 74, 48,
            84, 64, 55, 37, 20,
            59, 51, 35, 26, 18,
            47, 39, 28, 18, 13,
            40, 38, 22, 15, 11,
            38, 32, 18, 13, 9,
            30, 29, 15, 11, 30,
            27, 15, 29, 23, 13
    };

    private static final String[] ALGORITHMS = { "Gonzalez", "FastMap", "WVA-IG", "Exact" };

    private static final ReentrantLock fileLock = new ReentrantLock();

    public static void main(String[] args) {
        parseArguments(args);
        printConfig();
        writeHeader();

        int[] activeInstances = instanceFilter != null ? instanceFilter : INSTANCES;

        if (parallel) {
            runParallel(activeInstances);
        } else {
            runSequential(activeInstances);
        }

        System.out.println("Benchmark complete!");
    }

    private static void parseArguments(String[] args) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            switch (arg) {
                case "--attempts" -> attempts = Integer.parseInt(args[++i]);
                case "--parallel" -> parallel = true;
                case "--max-threads" -> maxThreads = Integer.parseInt(args[++i]);
                case "--output" -> outputPath = args[++i];
                case "--pmed-dir" -> pmedDir = args[++i];
                case "--max-n-exact" -> maxNForExact = Integer.parseInt(args[++i]);
                case "--instances" -> {
                    String[] parts = args[++i].split(",");
                    instanceFilter = new int[parts.length];
                    for (int j = 0; j < parts.length; j++) {
                        instanceFilter[j] = Integer.parseInt(parts[j].trim());
                    }
                }
                case "--algorithms" -> algorithmFilter = Set.of(args[++i].split(","));
                default -> throw new IllegalArgumentException("Unknown argument: " + arg);
            }
        }
    }

    private static void printConfig() {
        System.out.println("=== K-Center Benchmark Configuration ===");
        System.out.println("Attempts: " + attempts);
        System.out.println("Parallel: " + parallel);
        System.out.println("Max Threads: " + maxThreads);
        System.out.println("Output: " + outputPath);
        System.out.println("PMED Directory: " + pmedDir);
        System.out.println("Max N for Exact: " + maxNForExact);
        int[] active = instanceFilter != null ? instanceFilter : INSTANCES;
        System.out.println("Instances: " + active.length);
        String[] activeAlgos = algorithmFilter != null
                ? algorithmFilter.toArray(new String[0])
                : ALGORITHMS;
        System.out.println("Algorithms:");
        for (String algo : activeAlgos) {
            System.out.println("  - " + algo);
        }
        System.out.println("========================================");
    }

    private static void writeHeader() {
        try {
            boolean fileExists = Paths.get(outputPath).toFile().exists();
            try (PrintWriter w = new PrintWriter(new BufferedWriter(new FileWriter(outputPath, fileExists)))) {
                if (!fileExists) {
                    w.println("instance,n,k,algorithm,attempt,floyd_ms,algo_ms,radius,optimal_radius,gap_pct");
                }
            }
        } catch (IOException e) {
            System.err.println("Error writing header: " + e.getMessage());
        }
    }

    private static void runSequential(int[] instances) {
        int total = instances.length;
        for (int idx = 0; idx < total; idx++) {
            int inst = instances[idx];
            System.out.printf("[%d/%d] Processing pmed%d...%n", idx + 1, total, inst);
            try {
                processInstance(inst);
            } catch (Throwable t) {
                System.err.printf("CRASHED on pmed%d: %s%n", inst, t.getMessage());
            }
        }
    }

    private static void runParallel(int[] instances) {
        List<Runnable> tasks = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger(0);
        int total = instances.length;

        for (int inst : instances) {
            tasks.add(() -> {
                int current = counter.incrementAndGet();
                System.out.printf("[%d/%d] Processing pmed%d...%n", current, total, inst);
                try {
                    processInstance(inst);
                } catch (Throwable t) {
                    System.err.printf("CRASHED on pmed%d: %s%n", inst, t.getMessage());
                }
            });
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

    private static void processInstance(int instanceNum) throws Exception {
        String path = pmedDir + "/pmed" + instanceNum + ".txt";

        long floydStart = System.currentTimeMillis();

        ForwardStarGraphBuilder builder = new ForwardStarGraphBuilder(false);
        int k = GraphReader.readFile(path, builder, true, false, true);
        WeightedGraph graph = (WeightedGraph) builder.build();
        int n = graph.getVerticesCount();

        AllPairsShortestPathResult apsp = FloydWarshall.compute(graph);
        int[][] dist = apsp.distances();

        long floydTime = System.currentTimeMillis() - floydStart;

        int optimalRadius = (instanceNum >= 1 && instanceNum <= 40) ? OPTIMAL_RADII[instanceNum] : -1;

        String[] activeAlgorithms = algorithmFilter != null
                ? algorithmFilter.toArray(new String[0])
                : ALGORITHMS;

        for (String algorithm : activeAlgorithms) {
            if (algorithm.equals("Exact") && n > maxNForExact) {
                System.out.printf("  Skipping Exact for pmed%d (n=%d > %d)%n", instanceNum, n, maxNForExact);
                for (int a = 1; a <= attempts; a++) {
                    writeResult(instanceNum, n, k, algorithm, a, floydTime, -1, -1, optimalRadius, -1);
                }
                continue;
            }

            for (int attempt = 1; attempt <= attempts; attempt++) {
                long seed = (long) instanceNum * 1000000 + (long) algorithm.hashCode() * 1000 + attempt;

                long algoStart = System.nanoTime();
                int[] centers = switch (algorithm) {
                    case "Gonzalez" -> KCenter.solveGonzalez(dist, n, k);
                    case "FastMap" -> KCenter.solveFastMapKMeans(dist, n, k, seed);
                    case "WVA-IG" -> KCenter.solveWvaIg(dist, n, k, seed);
                    case "Exact" -> KCenterExactSolver.solveExact(dist, n, k);
                    default -> throw new IllegalArgumentException("Unknown algorithm: " + algorithm);
                };
                long algoTimeNs = System.nanoTime() - algoStart;

                int radius = KCenterUtils.evaluateRadius(dist, n, k, centers);

                double gapPct = (optimalRadius > 0)
                        ? ((double) (radius - optimalRadius) / optimalRadius) * 100.0
                        : -1;

                long algoMs = algoTimeNs / 1_000_000;
                writeResult(instanceNum, n, k, algorithm, attempt, floydTime, algoMs,
                        radius, optimalRadius, gapPct);

                System.out.printf("  pmed%d %s #%d: radius=%d (opt=%d, gap=%.2f%%) time=%d ms%n",
                        instanceNum, algorithm, attempt, radius, optimalRadius, gapPct, algoMs);
            }
        }
    }

    private static void writeResult(int instance, int n, int k, String algorithm, int attempt,
            long floydMs, long algoMs, int radius, int optimalRadius, double gapPct) {
        fileLock.lock();
        try (PrintWriter w = new PrintWriter(new BufferedWriter(new FileWriter(outputPath, true)))) {
            w.printf("pmed%d,%d,%d,%s,%d,%d,%d,%d,%d,%.2f%n",
                    instance, n, k, algorithm, attempt, floydMs, algoMs,
                    radius, optimalRadius, gapPct);
        } catch (IOException e) {
            System.err.println("Error writing result: " + e.getMessage());
        } finally {
            fileLock.unlock();
        }
    }
}

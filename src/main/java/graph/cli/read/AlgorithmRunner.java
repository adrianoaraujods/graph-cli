package graph.cli.read;

import graph.algorithms.DFS;
import graph.algorithms.Dijkstra;
import graph.algorithms.Dinic;
import graph.algorithms.DisjointPaths;
import graph.algorithms.Fleury;
import graph.algorithms.FloydWarshall;
import graph.algorithms.KCenter;
import graph.algorithms.KCenterExactSolver;
import graph.algorithms.Kosaraju;
import graph.util.KCenterUtils;
import graph.algorithms.NaiveBridges;
import graph.algorithms.Tarjan;
import graph.api.DirectedGraph;
import graph.api.Graph;
import graph.api.GraphBase;
import graph.api.UndirectedGraph;
import graph.api.WeightedGraph;
import graph.cli.read.result.AlgorithmResult;
import graph.cli.read.result.AllPairsShortestPathResult;
import graph.cli.read.result.BridgeResult;
import graph.cli.read.result.DFSResult;
import graph.cli.read.result.EulerianResult;
import graph.cli.read.result.MaximumFlowResult;
import graph.cli.read.result.SCCResult;
import graph.cli.read.result.ShortestPathResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AlgorithmRunner {

    public static List<AlgorithmOutput> run(Graph graph, List<AlgorithmRequest> requests) {
        List<AlgorithmOutput> outputs = new ArrayList<>();
        boolean hasTarjan = requests.stream().anyMatch(r -> r.name().equals("--tarjan"));
        boolean hasNaiveGlobal = requests.stream().anyMatch(r -> r.name().equals("--naive-global"));
        boolean hasFleury = requests.stream().anyMatch(r -> r.name().equals("--fleury"));

        for (AlgorithmRequest request : requests) {
            AlgorithmResult result = switch (request.name()) {
                case "--dfs" -> runDFS(graph, request);
                case "--kosaraju" -> runKosaraju(graph);
                case "--fleury" -> runFleury(graph, request, hasTarjan, hasNaiveGlobal, hasFleury);
                case "--tarjan" -> runTarjan(graph);
                case "--naive-global" -> runNaiveBridges(graph);
                case "--naive-local" -> runNaiveLocalBridges(graph);
                case "--dijkstra" -> runDijkstra(graph, request);
                case "--disjoint-paths" -> runDisjointPaths(graph, request);
                case "--dinic" -> runDinic(graph, request);
                case "--floyd-warshall" -> runFloydWarshall(graph);
                case "--gonzalez" -> runKCenter(graph, request, "Gonzalez");
                case "--fastmap" -> runKCenter(graph, request, "FastMap");
                case "--wva-ig" -> runKCenter(graph, request, "WVA-IG");
                case "--exact" -> runKCenter(graph, request, "Exact");
                default -> throw new RuntimeException("Unknown algorithm: " + request.name());
            };

            outputs.add(new AlgorithmOutput(request.name(), result));
        }

        return outputs;
    }

    public static AlgorithmResult runSingle(Graph graph, AlgorithmRequest request,
            boolean hasTarjan, boolean hasNaiveGlobal, boolean hasFleury) {
        return switch (request.name()) {
            case "--dfs" -> runDFS(graph, request);
            case "--kosaraju" -> runKosaraju(graph);
            case "--fleury" -> runFleury(graph, request, hasTarjan, hasNaiveGlobal, hasFleury);
            case "--tarjan" -> runTarjan(graph);
            case "--naive-global" -> runNaiveBridges(graph);
            case "--naive-local" -> runNaiveLocalBridges(graph);
            case "--dijkstra" -> runDijkstra(graph, request);
            case "--disjoint-paths" -> runDisjointPaths(graph, request);
            case "--dinic" -> runDinic(graph, request);
            case "--floyd-warshall" -> runFloydWarshall(graph);
            case "--gonzalez" -> runKCenter(graph, request, "Gonzalez");
            case "--fastmap" -> runKCenter(graph, request, "FastMap");
            case "--wva-ig" -> runKCenter(graph, request, "WVA-IG");
            case "--exact" -> runKCenter(graph, request, "Exact");
            default -> throw new RuntimeException("Unknown algorithm: " + request.name());
        };
    }

    private static MaximumFlowResult runDinic(Graph graph, AlgorithmRequest request) {
        int source = (int) request.params().get("source");
        int target = (int) request.params().get("target");

        return Dinic.compute(graph, source, target);
    }

    private static MaximumFlowResult runDisjointPaths(Graph graph, AlgorithmRequest request) {
        int source = (int) request.params().get("source");
        int target = (int) request.params().get("target");

        return DisjointPaths.compute((DirectedGraph) graph, source, target);
    }

    private static DFSResult runDFS(Graph graph, AlgorithmRequest request) {
        int target = (int) request.params().get("target");

        if (target < 1 || target > graph.getVerticesCount()) {
            throw new IllegalArgumentException("Invalid target: must be between 1 and " + graph.getVerticesCount());
        }

        DFS.DFSResult dfsResult = DFS.search(graph);
        DFS.ClassifiedDFSEdges classifiedEdges = DFS.classifyVertexDFSEdges(graph, target, dfsResult);
        long[] treeEdges = DFS.getDFSTreeEdges(graph, dfsResult.parents());

        int outDegree, inDegree;
        int[] successors, predecessors;

        if (graph.isDirected) {
            outDegree = ((DirectedGraph) graph).getOutDegree(target);
            inDegree = ((DirectedGraph) graph).getInDegree(target);
            successors = ((DirectedGraph) graph).getSuccessors(target);
            predecessors = ((DirectedGraph) graph).getPredecessors(target);
        } else {
            outDegree = ((UndirectedGraph) graph).getDegree(target);
            inDegree = -1;
            successors = ((UndirectedGraph) graph).getNeighbors(target);
            predecessors = new int[0];
        }

        return new DFSResult(target, outDegree, inDegree, successors, predecessors, treeEdges, classifiedEdges);
    }

    private static SCCResult runKosaraju(Graph graph) {
        if (!graph.isDirected) {
            return new SCCResult(new DirectedGraph[0]);
        }

        DFS.DFSResult dfsResult = DFS.search(graph);
        DirectedGraph[] components = Kosaraju.findSCCs((DirectedGraph) graph, dfsResult.finishTimes());
        return new SCCResult(components);
    }

    private static EulerianResult runFleury(Graph graph, AlgorithmRequest request,
            boolean hasTarjan, boolean hasNaiveGlobal, boolean hasFleury) {
        Fleury.BridgeFinder bridgeFinder = (Fleury.BridgeFinder) request.params().get("bridgeFinder");

        if (bridgeFinder == null) {
            if (hasTarjan) {
                bridgeFinder = Fleury.BridgeFinder.TARJAN;
            } else if (hasNaiveGlobal) {
                bridgeFinder = Fleury.BridgeFinder.NAIVE_GLOBAL;
            } else {
                bridgeFinder = Fleury.BridgeFinder.NAIVE_LOCAL;
            }
        }

        Fleury.EulerianPath path = graph.isDirected
                ? Fleury.findEulerianPath((DirectedGraph) graph)
                : Fleury.findEulerianPath((UndirectedGraph) graph, bridgeFinder, true);

        String bridgeFinderName = switch (bridgeFinder) {
            case TARJAN -> "Tarjan";
            case NAIVE_GLOBAL -> "Naive Bridges (Global)";
            case NAIVE_LOCAL -> "Naive Bridges (Local)";
        };

        return new EulerianResult(path, bridgeFinderName);
    }

    private static BridgeResult runTarjan(Graph graph) {
        Set<Long> bridges = Tarjan.findAll(graph);
        return new BridgeResult("--tarjan", bridges);
    }

    private static BridgeResult runNaiveBridges(Graph graph) {
        Set<Long> bridges = NaiveBridges.findAll(graph);
        return new BridgeResult("--naive-global", bridges);
    }

    private static BridgeResult runNaiveLocalBridges(Graph graph) {
        Set<Long> bridges = NaiveBridges.findAll(graph);
        return new BridgeResult("--naive-local", bridges);
    }

    private static AllPairsShortestPathResult runFloydWarshall(GraphBase graph) {
        return FloydWarshall.compute((WeightedGraph) graph);
    }

    private static ShortestPathResult runDijkstra(GraphBase graph, AlgorithmRequest request) {
        int source = (int) request.params().get("source");
        Integer target = (Integer) request.params().get("target");
        boolean findPath = (boolean) request.params().get("findPath");

        ShortestPathResult result = Dijkstra.compute((WeightedGraph) graph, source, target, findPath);
        return new ShortestPathResult(result.distances(), result.parents(), result.source(), target, findPath);
    }

    private static graph.cli.read.result.KCenterResult runKCenter(GraphBase graph, AlgorithmRequest request,
            String mode) {
        if (!(graph instanceof WeightedGraph)) {
            throw new IllegalArgumentException("k-Center requires a weighted graph. Use --weighted.");
        }

        int k = (int) request.params().get("k");
        int n = graph.getVerticesCount();

        AllPairsShortestPathResult apsp = FloydWarshall.compute((WeightedGraph) graph);
        int[][] dist = apsp.distances();

        int[] centers = switch (mode) {
            case "Gonzalez" -> KCenter.solveGonzalez(dist, n, k);
            case "FastMap" -> KCenter.solveFastMapKMeans(dist, n, k, System.currentTimeMillis());
            case "WVA-IG" -> KCenter.solveWvaIg(dist, n, k, System.currentTimeMillis());
            case "Exact"    -> KCenterExactSolver.solveExact(dist, n, k, true);
            default -> throw new IllegalArgumentException("Unknown k-Center mode: " + mode);
        };

        int radius = KCenterUtils.evaluateRadius(dist, n, k, centers);
        return new graph.cli.read.result.KCenterResult(mode, k, radius, centers);
    }
}

package graph.cli.read;

import graph.algorithms.DFS;
import graph.algorithms.Fleury;
import graph.algorithms.Kosaraju;
import graph.algorithms.NaiveBridges;
import graph.algorithms.Tarjan;
import graph.api.DirectedGraph;
import graph.api.Graph;
import graph.api.UndirectedGraph;
import graph.cli.read.result.AlgorithmResult;
import graph.cli.read.result.BridgeResult;
import graph.cli.read.result.DFSResult;
import graph.cli.read.result.EulerianResult;
import graph.cli.read.result.SCCResult;

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
            default -> throw new RuntimeException("Unknown algorithm: " + request.name());
        };
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
}

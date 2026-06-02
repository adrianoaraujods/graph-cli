package graph.cli.read;

import graph.algorithms.Fleury.BridgeFinder;

import java.util.HashMap;
import java.util.Map;

public record AlgorithmRequest(String name, Map<String, Object> params) {

    public static AlgorithmRequest dfs(int target) {
        return new AlgorithmRequest("--dfs", Map.of("target", target));
    }

    public static AlgorithmRequest kosaraju() {
        return new AlgorithmRequest("--kosaraju", Map.of());
    }

    public static AlgorithmRequest fleury(BridgeFinder bridgeFinder) {
        if (bridgeFinder == null) {
            return new AlgorithmRequest("--fleury", Map.of());
        }
        return new AlgorithmRequest("--fleury", Map.of("bridgeFinder", bridgeFinder));
    }

    public static AlgorithmRequest tarjan() {
        return new AlgorithmRequest("--tarjan", Map.of());
    }

    public static AlgorithmRequest naiveBridges() {
        return new AlgorithmRequest("--naive-global", Map.of());
    }

    public static AlgorithmRequest naiveLocalBridges() {
        return new AlgorithmRequest("--naive-local", Map.of());
    }

    public static AlgorithmRequest dijkstra(int source, Integer target, boolean findPath) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("source", source);
        params.put("target", target);
        params.put("findPath", findPath);
        return new AlgorithmRequest("--dijkstra", params);
    }

    public static AlgorithmRequest disjointPaths(int source, int target) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("source", source);
        params.put("target", target);
        return new AlgorithmRequest("--disjoint-paths", params);
    }

    public static AlgorithmRequest dinic(int source, int target) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("source", source);
        params.put("target", target);
        return new AlgorithmRequest("--dinic", params);
    }
}

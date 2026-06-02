package graph.cli.read.result;

public sealed interface AlgorithmResult
    permits DFSResult, SCCResult, EulerianResult, BridgeResult, ShortestPathResult, MaximumFlowResult {
}

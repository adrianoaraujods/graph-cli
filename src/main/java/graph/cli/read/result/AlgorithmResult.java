package graph.cli.read.result;

public sealed interface AlgorithmResult
    permits AllPairsShortestPathResult, DFSResult, SCCResult, EulerianResult, BridgeResult, ShortestPathResult, MaximumFlowResult {
}

package graph.util;

import java.util.function.Supplier;
import graph.api.Graph;
import graph.representations.GraphBuilder;

public class TestGraphs {

    public static Graph singleEdge(Supplier<GraphBuilder> builderSupplier) {
        return GraphTestHelper.build(builderSupplier, 2,
                new int[][] { { 1, 2 } });
    }

    public static Graph linearChain(Supplier<GraphBuilder> builderSupplier) {
        return GraphTestHelper.build(builderSupplier, 5,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 4 }, { 4, 5 } });
    }

    public static Graph simpleCycle(Supplier<GraphBuilder> builderSupplier) {
        return GraphTestHelper.build(builderSupplier, 3,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 } });
    }

    public static Graph graphWithOneBridge(Supplier<GraphBuilder> builderSupplier) {
        return GraphTestHelper.build(builderSupplier, 6,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 4 }, { 2, 5 }, { 5, 6 } });
    }

    public static Graph multipleBridges(Supplier<GraphBuilder> builderSupplier) {
        return GraphTestHelper.build(builderSupplier, 7,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 4 }, { 4, 5 }, { 3, 6 }, { 6, 7 } });
    }

    public static Graph disconnectedGraph(Supplier<GraphBuilder> builderSupplier) {
        return GraphTestHelper.build(builderSupplier, 6,
                new int[][] { { 1, 2 }, { 3, 4 }, { 5, 6 } });
    }

    public static Graph tree(Supplier<GraphBuilder> builderSupplier) {
        return GraphTestHelper.build(builderSupplier, 5,
                new int[][] { { 1, 2 }, { 1, 3 }, { 2, 4 }, { 2, 5 } });
    }

    public static Graph graphWithTwoCycles(Supplier<GraphBuilder> builderSupplier) {
        return GraphTestHelper.build(builderSupplier, 5,
                new int[][] { { 1, 2 }, { 2, 3 }, { 3, 1 }, { 3, 4 }, { 4, 5 }, { 5, 3 } });
    }

    public static Graph bridgeInMiddle(Supplier<GraphBuilder> builderSupplier) {
        return GraphTestHelper.build(builderSupplier, 6,
                new int[][] {
                        { 1, 2 }, { 2, 3 }, { 1, 3 },
                        { 3, 4 },
                        { 4, 5 }, { 5, 6 }, { 6, 4 }
                });
    }
}

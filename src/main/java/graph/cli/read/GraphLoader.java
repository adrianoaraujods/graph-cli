package graph.cli.read;

import graph.api.Graph;
import graph.cli.GraphReader;
import graph.representations.GraphBuilder;
import graph.representations.adjacencylist.AdjacencyListGraphBuilder;
import graph.representations.adjacencymatrix.AdjacencyMatrixGraphBuilder;
import graph.representations.forwardstar.ForwardStarGraphBuilder;

import java.io.IOException;

public class GraphLoader {

    public static Graph load(String graphPath, String representation, boolean isDirected, boolean isWeighted)
            throws IOException, Exception {
        GraphBuilder builder;
        switch (representation) {
            case "Adjacency Matrix" -> builder = new AdjacencyMatrixGraphBuilder(isDirected);
            case "Adjacency List" -> builder = new AdjacencyListGraphBuilder(isDirected);
            case "Forward Star" -> builder = new ForwardStarGraphBuilder(isDirected);
            default -> builder = new ForwardStarGraphBuilder(isDirected);
        }

        GraphReader.readFile(graphPath, builder, isWeighted);
        return builder.build();
    }

    public static Graph load(String graphPath, String representation, boolean isDirected)
            throws IOException, Exception {
        return load(graphPath, representation, isDirected, false);
    }
}

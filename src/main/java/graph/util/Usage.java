package graph.util;

public class Usage {

    public static void printGeneral() {
        System.out.println("Usage:");
        printCreateUsage();
        printReadUsage();
        System.out.println();
        System.out.println("Subcommands:");
        System.out.println("  create     Generate a new graph file");
        System.out.println("  read       Read and analyze an existing graph file");
        System.out.println();
        System.out.println("Create Options:");
        printCreateOptions();
        System.out.println();
        System.out.println("Read Options:");
        printReadOptions();
        System.out.println();
        System.out.println("Help:");
        System.out.println("  graph-cli help            Show this help");
        System.out.println("  graph-cli help create     Show create help");
        System.out.println("  graph-cli help read       Show read help");
        System.out.println();
        System.out.println("Examples:");
        printCreateExamples();
        printReadExamples();
    }

    public static void printCreate() {
        System.out.println("Usage:");
        printCreateUsage();
        System.out.println();
        System.out.println("Options:");
        printCreateOptions();
        System.out.println();
        System.out.println("Examples:");
        printCreateExamples();
    }

    public static void printRead() {
        System.out.println("Usage:");
        printReadOptions();
        System.out.println();
        System.out.println("Options:");
        printReadOptions();
        System.out.println();
        System.out.println("Examples:");
        printReadExamples();
    }

    // Create Methods

    public static void printCreateUsage() {
        System.out.println("  graph-cli create <file> -n <vertices> -d <density>");
        System.out.println("  graph-cli create <file> -n <vertices> -m <edges>");
        System.out.println("  graph-cli create <file> -n <vertices> -d <density> --weighted");
    }

    public static void printCreateOptions() {
        System.out.println("  -n, --vertices <n>      Number of vertices (required)");
        System.out.println("  -d, --density <val>     Edge density 0.0-1.0 (alternative to edges)");
        System.out.println("  -m, --edges <m>         Number of edges (alternative to density)");
        System.out.println("  -s, --seed <n>          Random seed for reproducible graphs");
        System.out.println("  --directed              Treat graph as directed (default)");
        System.out.println("  --undirected, -u        Treat graph as undirected");
        System.out.println("  --connected             Graph is weakly connected (default for directed)");
        System.out.println("  --strongly              Graph is strongly connected (directed only)");
        System.out.println("  --disconnected          Graph may be disconnected");
        System.out.println("  --eulerian              Graph has all vertices with equal in/out-degree (directed) or even degree (undirected)");
        System.out.println("  --semi-eulerian         Graph has exactly two vertices with imbalanced in/out-degree (directed) or odd degree (undirected)");
        System.out.println("  --weighted              Generate weighted graph (format: u v w)");
    }

    public static void printCreateExamples() {
        System.out.println("  graph-cli create graph.txt -n 1000 -d 0.5");
        System.out.println("  graph-cli create graph.txt -n 1000 -m 500");
        System.out.println("  graph-cli create graph.txt -n 1000 -d 0.5 -s 42");
        System.out.println("  graph-cli create weighted.txt -n 1000 -d 0.5 --weighted");
    }

    // Read Methods

    public static void printReadUsage() {
        System.out.println("  graph-cli read <file> --<algorithm> [-t <target>] [-o <path>] [--weighted]");
    }

    public static void printReadOptions() {
        System.out.println("  Algorithms (at least one required):");
        System.out.println("    --dfs                 Run DFS with edge classification");
        System.out.println("    --kosaraju            Run Kosaraju to find SCCs (only directed)");
        System.out.println("    --fleury              Run Fleury to find an Eulerian path or cycle");
        System.out.println("    --tarjan              Run Tarjan to find all bridges");
        System.out.println("    --naive-local         Run Naive Bridges (Local) to find all bridge edges");
        System.out.println("    --naive-global        Run Naive Bridges (Global) to find all bridge edges");
        System.out.println(
                "    --dijkstra            Run Dijkstra shortest path (requires --weighted, --source, --target)");
        System.out.println("  --source <n>            Source vertex (required for --dijkstra)");
        System.out.println("  --target <n>, -t        Target vertex (required for --dfs, --dijkstra)");
        System.out.println("  --path                  Show path vertices (used on fleury, dijkstra)");
        System.out.println("  --output <path>, -o     Output file (default: terminal)");
        System.out.println("  --forward-star          Use Forward Star representation (default)");
        System.out.println("  --adjacency-matrix      Use Adjacency Matrix representation");
        System.out.println("  --directed              Treat graph as directed (default)");
        System.out.println("  --undirected, -u        Treat graph as undirected");
        System.out.println("  --weighted              Read weighted graph file (format: u v w)");
    }

    public static void printReadExamples() {
        System.out.println("  graph-cli read graph.txt --dfs -t 5");
        System.out.println("  graph-cli read graph.txt --dfs -t 5 -o output.log");
        System.out.println("  graph-cli read graph.txt --kosaraju --fleury");
        System.out.println("  graph-cli read graph.txt --weighted --dijkstra --source 1 --target 5");
    }
}

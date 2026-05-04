# Graph CLI

High-performance Java toolkit for generating, parsing, and analyzing large-scale graphs.

Prioritizes cache-friendly data structures and zero-allocation processing loops for massive datasets.

## Key Features

- **Forward Star (CSR) Representation:** Compressed Sparse Row memory model with primitive arrays (`targets`, `pointers`). Minimal memory overhead, zero GC pressure, maximum CPU cache locality.
- **Adjacency List Representation:** Hash-based structure supporting weighted edges with O(1) edge lookup.
- **Weighted Graph Support:** Full support for weighted graphs. Algorithms ignore weights during traversal while preserving them for analysis.
- **Direct NIO Parsing:** Bypasses standard Java I/O bottlenecks. Uses `FileChannel` and direct `ByteBuffer` for native disk speeds.
- **Memoryless Graph Generation:** O(m) geometric jump algorithm for uniform random graphs without holding the graph in memory.
- **Architectural Purity:** Builder pattern (immutable graphs), Visitor pattern (decoupled traversal logic).

## File Formats

### Unweighted Graphs

**Header:** First line contains `n` (vertices) and `m` (edges).
**Body:** Next `m` lines with `u v` (source and target vertices, 1-indexed).

```
6 9
1 6
1 2
2 4
3 5
3 1
4 6
5 6
5 2
6 2
```

### Weighted Graphs

**Header:** First line contains `n` (vertices) and `m` (edges).
**Body:** Next `m` lines with `u v w` (source, target, and weight).

```
4 3
1 2 5
2 3 10
3 1 15
```

> **Note:** Use `--weighted` flag when reading weighted graph files.

## Usage

### Prerequisites

- Java Development Kit (JDK) 26 or higher

### Build

```bash
# Run tests
mvn test

# Package as JAR
mvn package

# Package as JAR skipping tests
mvn package -DskipTests
```

### Run

```bash
# Using packaged JAR
java -jar target/graph-cli-0.1.0.jar <subcommand> [options]

# Subcommands
create <file>     # Generate a new graph file
read <file>       # Read and analyze an existing graph file
help              # Show help (use help create or help read for subcommand-specific help)
```

### Without Maven

```bash
# Compile
javac -d target/classes --release 25 $(find src/main/java -name "*.java")

# Run
java -cp target/classes graph.GraphCLI --help
```

### Create Examples

```bash
# Create a graph with 1k vertices and 50% density
java -jar target/graph-cli-0.1.0.jar create graph.txt -n 1000 -d 0.5

# Create a graph with 1k vertices and 500 edges
java -jar target/graph-cli-0.1.0.jar create graph.txt -n 1000 -m 500

# Create a reproducible graph with a seed
java -jar target/graph-cli-0.1.0.jar create graph.txt -n 1000 -d 0.5 -s 42

# Create an undirected graph
java -jar target/graph-cli-0.1.0.jar create graph.txt -n 1000 -d 0.5 --undirected

# Create an Eulerian graph (all vertices have even degree)
java -jar target/graph-cli-0.1.0.jar create graph.txt -n 1000 -d 0.5 --eulerian

# Create a weighted graph (generates weighted output)
java -jar target/graph-cli-0.1.0.jar create weighted.txt -n 1000 -d 0.5 --weighted
```

### Read Examples

```bash
# Read and run DFS algorithm on target vertex 5
java -jar target/graph-cli-0.1.0.jar read graph.txt --dfs -t 5

# Read and run multiple algorithms, save output to file
java -jar target/graph-cli-0.1.0.jar read graph.txt --kosaraju --fleury -o output.log

# Read using undirected graph and run bridges algorithm (local)
java -jar target/graph-cli-0.1.0.jar read graph.txt --naive-local --undirected

# Read using undirected graph and run bridges algorithm (global)
java -jar target/graph-cli-0.1.0.jar read graph.txt --naive-global --undirected

# Read a weighted graph (ignores weights during algorithms, preserves them for analysis)
java -jar target/graph-cli-0.1.0.jar read weighted.txt --weighted --tarjan

# Read a weighted graph and run DFS
java -jar target/graph-cli-0.1.0.jar read weighted.txt --weighted --dfs -t 5

# Read a weighted undirected graph and find Eulerian path
java -jar target/graph-cli-0.1.0.jar read weighted.txt --weighted --undirected --fleury

# Show help
java -jar target/graph-cli-0.1.0.jar help

# Show create help
java -jar target/graph-cli-0.1.0.jar help create

# Show read help
java -jar target/graph-cli-0.1.0.jar help read
```

### Supported Algorithms

- **DFS (`--dfs`):** Depth-First Search with edge classification (tree, back, cross, forward edges)
- **Kosaraju (`--kosaraju`):** Find Strongly Connected Components (directed graphs only)
- **Fleury (`--fleury`):** Find Eulerian paths/cycles
- **Tarjan (`--tarjan`):** Find all bridges in undirected graphs
- **Naive Bridges Local (`--naive-local`):** Find bridges using local connectivity check
- **Naive Bridges Global (`--naive-global`):** Find bridges using global connectivity check (parallelized)

> **Note:** All algorithms work on both weighted and unweighted graphs. Weighted graphs preserve edge weights but algorithms ignore them during traversal.

## Architecture & Core Components

### 1. The Core Engine (`Graph.java` & `GraphBuilder.java`)

Abstract foundation of the project. `GraphBuilder` handles raw data ingestion, while `Graph` contains universal, read-only algorithms like DFS. DFS uses a Callback/Visitor pattern (`DFSVisitor`), automatically working for any graph representation.

### 2. Storage Implementations

- **Forward Star (`ForwardStarGraph.java`):** Immutable CSR implementation. Builder sorts edges and calculates prefix-sum array (`pointers`). Supports weighted edges via parallel `weights` array.
- **Adjacency List (`AdjacencyListGraph.java`):** Hash-based implementation with O(1) edge lookup. Supports weighted edges via `WeightedGraph` interface.
- **Adjacency Matrix (`AdjacencyMatrixGraph.java`):** Boolean matrix representation. Does NOT support weighted edges (unweighted only).

### 3. The Parser (`GraphReader.java`)

Custom I/O handler. Reads data in 64 KB blocks, manually parsing ASCII bytes into integers. Feeds data blindly into `GraphBuilder` interface, keeping I/O logic 100% decoupled from storage.

### 4. The Array Engine (`Sort.java`)

Custom recursive Quicksort for parallel array synchronization. Uses `SortVisitor` to keep the core pivot algorithm pure while handling single or multiple synchronized arrays.

## Weighted Graph Support

### Interface

```java
public interface WeightedGraph extends GraphBase {
    int getEdgeWeight(int v, int w);
    int[] getWeightsSet();
    record WeightedEdges(long[] edges, int[] weights) {}
    WeightedEdges getWeightedEdgesSet();
}
```

### Implementations

- `ForwardStarGraph` - Implements `WeightedGraph`
- `AdjacencyListGraph` - Implements `WeightedGraph`
- `AdjacencyMatrixGraph` - Does NOT implement `WeightedGraph` (unweighted only)

### Behavior

- Algorithms (DFS, Kosaraju, Fleury, Tarjan, NaiveBridges) ignore weights during traversal
- Weights are preserved in graph operations (`getReversed()`, `getInducedSubgraph()`)
- `getEdgesSet()` returns unweighted edge set
- `getWeightedEdgesSet()` returns aligned edges and weights

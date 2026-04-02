# High-Performance Java Graph Processing

A highly optimized, memory-efficient Java toolkit for generating, parsing, and analyzing large-scale directed graphs.

This project prioritizes cache-friendly data structures and zero-allocation processing loops to handle massive datasets.

## Key Features

- **Forward Star (CSR) Representation:** Implements the Compressed Sparse Row memory model using immutable primitive integer arrays (`targets` and `pointers`). This guarantees minimal memory overhead, zero Garbage Collection (GC) pressure during traversals, and maximum CPU cache locality.
- **Direct NIO Parsing:** Bypasses standard Java `Scanner` or `BufferedReader` bottlenecks. Uses `FileChannel` and direct `ByteBuffer` manipulation to parse multi-gigabyte text files at native disk speeds.
- **Memoryless Graph Generation:** Uses a highly advanced $O(m)$ geometric jump algorithm to generate uniform random graphs. It mathematically calculates probability jumps to skip non-edges, allowing it to generate billions of edges without ever holding the graph in memory.
- **Architectural Purity:** Heavily utilizes robust software design patterns:
- **Builder Pattern:** Ensures the final `Graph` objects are strictly immutable and thread-safe.
- **Visitor Pattern:** Decouples the traversal logic (like Depth-First Search or Quicksort) from the underlying data mutations or classifications.

## File Format

The tools expect and generate plain text files representing a directed graph.

- **Header:** The first line contains two integers: `n` (number of vertices) and `m` (number of edges).
- **Body:** The next `m` lines contain two integers each: `u` (source vertex) and `v` (target vertex).
- Vertices are strictly **1-indexed** (from 1 to `n`).

**Example (`graph.txt`):**

```text
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

> _Note: The white spaces don't matter, the `GraphReader` ignores them automatically._

## Usage

### Prerequisites

- Java Development Kit (JDK) 25 or higher

### Build

```bash
# Run tests
mvn test

# Package as JAR
mvn package
```

### Run

```bash
# Using packaged JAR
java -jar target/graph-cli-0.1.0.jar [options] <arguments>

# Commands (use --help for full list)
--create, -c          Generate a new graph file
--read, -r            Read and analyze an existing graph file
```

### Without Maven

If you don't have Maven installed, compile and run directly:

```bash
# Compile
javac -d target/classes --release 25 $(find src/main/java -name "*.java")

# Run
java -cp target/classes graph.GraphCLI --help
```

### Examples

```bash
# Generate a graph with 1k vertices with 20% desity
java -jar target/graph-cli-0.1.0.jar -create graph.txt 1000 0.2

# Read and analyze a graph
java -jar target/graph-cli-0.1.0.jar -read graph.txt 1

# Show help
java -jar target/graph-cli-0.1.0.jar --help
```

## Architecture & Core Components

### 1. The Core Engine (`Graph.java` & `GraphBuilder.java`)

The abstract foundation of the project. `GraphBuilder` dictates how raw data is ingested, while `Graph` contains universal, read-only algorithms like Depth First Search (DFS Forest). Because DFS is implemented here using a Callback/Visitor pattern (`DFSVisitor`), it automatically works for any future graph representation you might build.

### 2. The Storage (`ForwardStarGraph.java` & `ForwardStarGraphBuilder.java`)

The concrete implementation of the engine. During construction, the builder sorts the raw edges and calculates a prefix-sum array (`pointers`). Once built, the graph transfers ownership of the arrays and becomes entirely immutable, ensuring algorithms cannot corrupt its topology.

### 3. The Parser (`GraphReader.java`)

A custom I/O director. It reads data chunks in 64 KB blocks, manually parsing ASCII bytes into integers. It feeds this data blindly into the `GraphBuilder` interface, keeping the I/O logic 100% decoupled from the graph storage logic.

### 4. The Array Engine (`Sort.java`)

A custom recursive Quicksort utility designed for parallel array synchronization. By using a `SortVisitor`, the core pivoting algorithm remains pure while seamlessly handling single arrays or synchronized dual arrays (e.g., swapping `sources` and `targets` simultaneously).

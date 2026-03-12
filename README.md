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

## Usage Instructions

### Prerequisites

- Java Development Kit (JDK) 8 or higher.

### Compilation

Compile all Java files in the directory:

```bash
javac *.java
```

### 1. Generating a Random Graph

Use the `GraphGenerator` to create a random, simple directed graph (no self-loops). The geometric jump algorithm ensures uniform distribution.

**Syntax:**

```bash
java GraphGenerator <output_path> <number_of_vertices> <number_of_edges>
```

**Example:** (Generate a graph with 1 million vertices and 50 million edges)

```bash
java GraphGenerator my_huge_graph.txt 1000000 50000000
```

### 2. Reading and Analyzing a Graph

Use the `GraphReader` to load a graph into the Forward Star memory structure and analyze a specific target vertex. It will output the vertex's degree details, its immediate neighbors, the full edge list, and the transposed (reversed) edge list.

**Syntax:**

```bash
java GraphReader <file_path> <target_vertex_id>
```

**Example:** (Read the graph and analyze vertex ID 3)

```bash
java GraphReader my_huge_graph.txt 3
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

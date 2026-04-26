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

- Java Development Kit (JDK) 26 or higher

### Build

```bash
# Run tests
mvn test

# Package as JAR
mvn package

# Package as JAR skiping tests
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

If you don't have Maven installed, compile and run directly:

```bash
# Compile
javac -d target/classes --release 25 $(find src/main/java -name "*.java")

# Run
java -cp target/classes graph.GraphCLI --help
```

### Examples

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

# Read and run DFS algorithm on target vertex 5
java -jar target/graph-cli-0.1.0.jar read graph.txt --dfs -t 5

# Read and run multiple algorithms, save output to file
java -jar target/graph-cli-0.1.0.jar read graph.txt --kosaraju --fleury -o output.log

# Read using undirected graph and run bridges algorithm (local)
java -jar target/graph-cli-0.1.0.jar read graph.txt --naive-local --undirected

# Read using undirected graph and run bridges algorithm (global)
java -jar target/graph-cli-0.1.0.jar read graph.txt --naive-global --undirected

# Show help
java -jar target/graph-cli-0.1.0.jar help

# Show create help
java -jar target/graph-cli-0.1.0.jar help create

# Show read help
java -jar target/graph-cli-0.1.0.jar help read
```

### Benchmark

Run Fleury algorithm performance benchmarks on generated graphs:

```bash
mvn compile exec:java -Dexec.mainClass="graph.bench.Benchmark" -Dexec.args="[options]"
```

**Options:**

| Argument          | Description                                  | Default     |
| ----------------- | -------------------------------------------- | ----------- |
| `--attempts N`    | Number of attempts per parameter combination | 10          |
| `--parallel`      | Enable parallel execution                    | false       |
| `--max-threads N` | Maximum parallel threads                     | 24          |
| `--output FILE`   | Output CSV file path                         | results.csv |

**Example:**

```bash
# Run 10 attempts for each combination sequentially
mvn compile exec:java -Dexec.mainClass="graph.bench.Benchmark" -Dexec.args="--attempts 10"

# Run with parallel execution (uses all 24 cores)
mvn compile exec:java -Dexec.mainClass="graph.bench.Benchmark" -Dexec.args="--attempts 10 --parallel"

# Custom output file
mvn compile exec:java -Dexec.mainClass="graph.bench.Benchmark" -Dexec.args="--attempts 10 --output my_results.csv"
```

**CSV Output Format:**

```csv
attempt,vertices,density,connectivity,representation,create_time_ms,read_build_time_ms,fleury_time_ms,eulerian_type
1,100,0.05,EULERIAN,ForwardStar,7,1,138,EULERIAN
2,100,0.05,EULERIAN,ForwardStar,4,0,87,EULERIAN
...
```

**Protections:**

The benchmark includes several safeguards to prevent crashes and ensure reliable execution:

1. **JVM Flags**: The following flags are automatically applied:
   - `-Xmx24g` - Limits heap to 24GB (leaves 8GB for system)
   - `-XX:+UseG1GC` - Uses G1 garbage collector (better for large heaps)
   - `-XX:+ExitOnOutOfMemoryError` - Exits cleanly on OOM instead of hanging
   - `-XX:+CrashOnOutOfMemoryError` - Crashes immediately on OOM for debugging

2. **Thread-Level Exception Handling**: In parallel mode, each task runs in its own thread with try-catch protection. If a task crashes (Exception or Error), the benchmark logs the error and continues with the next task.

3. **Crash Recovery**: Failed tasks write "CRASHED" to the CSV and the benchmark continues to the next combination.

4. **Temp File Cleanup**: On normal completion or unexpected termination (Ctrl+C, crash), temp files are automatically cleaned up.

**CSV Output Codes:**

| Code            | Description                                                    |
| --------------- | -------------------------------------------------------------- |
| `EULERIAN`      | Graph has an Eulerian path (exactly 0 odd-degree vertices)     |
| `SEMI_EULERIAN` | Graph has a semi-Eulerian path (exactly 2 odd-degree vertices) |
| `NON_EULERIAN`  | Graph is not Eulerian or semi-Eulerian                         |
| `RETRY_FAILED`  | All 3 retry attempts failed                                    |
| `CRASHED`       | Task crashed with uncaught error                               |

**Tested Parameters:**

- Vertices: 100, 10,000, 100,000
- Density: 0.01, 0.05, 0.10
- Connectivity: EULERIAN, SEMI_EULERIAN, CONNECTED
- Representation: ForwardStar, AdjacencyMatrix

> **Note:** AdjacencyMatrix is only tested with n ≤ 10,000 due to memory constraints (n=100,000 requires ~10GB for the boolean matrix).

## Architecture & Core Components

### 1. The Core Engine (`Graph.java` & `GraphBuilder.java`)

The abstract foundation of the project. `GraphBuilder` dictates how raw data is ingested, while `Graph` contains universal, read-only algorithms like Depth First Search (DFS Forest). Because DFS is implemented here using a Callback/Visitor pattern (`DFSVisitor`), it automatically works for any future graph representation you might build.

### 2. The Storage (`ForwardStarGraph.java` & `ForwardStarGraphBuilder.java`)

The concrete implementation of the engine. During construction, the builder sorts the raw edges and calculates a prefix-sum array (`pointers`). Once built, the graph transfers ownership of the arrays and becomes entirely immutable, ensuring algorithms cannot corrupt its topology.

### 3. The Parser (`GraphReader.java`)

A custom I/O director. It reads data chunks in 64 KB blocks, manually parsing ASCII bytes into integers. It feeds this data blindly into the `GraphBuilder` interface, keeping the I/O logic 100% decoupled from the graph storage logic.

### 4. The Array Engine (`Sort.java`)

A custom recursive Quicksort utility designed for parallel array synchronization. By using a `SortVisitor`, the core pivoting algorithm remains pure while seamlessly handling single arrays or synchronized dual arrays (e.g., swapping `sources` and `targets` simultaneously).

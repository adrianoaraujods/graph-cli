# Benchmark Methodology

## Overview

This benchmark evaluates Fleury's algorithm for finding Eulerian paths across different graph configurations, bridge-finding algorithms, and graph representations.

## Parameters

| Parameter       | Value                                          |
| --------------- | ---------------------------------------------- |
| Repetitions     | 10                                             |
| Vertices (n)    | 100, 1,000, 10,000, 100,000                    |
| Edges (m)       | 500, 5,000, 50,000, 500,000                    |
| Connectivity    | Eulerian, Semi-Eulerian, Non-Eulerian          |
| Algorithms      | Fleury + Naive Bridges, Fleury + Tarjan        |
| Representations | Adjacency List, Adjacency Matrix, Forward Star |

## Algorithm Variants

### Bridge Detection

- **Naive Bridges**: O(n\*m) algorithm that checks each edge by removal
- **Tarjan**: O(n+m) algorithm using DFS-based bridge finding

### Representations

- **Adjacency List**: Linked list per vertex storing incident edges
- **Forward Star**: Two arrays (head/to) with edge ordering
- **Adjacency Matrix**: Only for n ≤ 1000 vertices

## Execution Flow

For each repetition:

1. Generate graph with specified (n, m, connectivity)
2. Build graph representation (Adjacency List, Adjacency Matrix, Forward Star)
3. Run Fleury's algorithm (with specified bridge finder)
4. Record timing and results

## Output CSV Columns

| Column             | Description                                        |
| ------------------ | -------------------------------------------------- |
| attempt            | Repetition number (1-10)                           |
| vertices           | Number of vertices (n)                             |
| edges              | Number of edges (m)                                |
| connectivity       | Eulerian, Semi-Eulerian, Non-Eulerian              |
| representation     | AdjacencyList or ForwardStar                       |
| generation_time_ms | Graph generation time in milliseconds              |
| fleury_time_ms     | Fleury algorithm execution time in milliseconds    |
| path_length        | Length of Eulerian path found (m+1)                |
| eulerian_type      | Result type: EULERIAN, SEMI_EULERIAN, NON_EULERIAN |
| algorithm          | Naive or Trajan (bridge detection method)          |

## Validation Rules

- Skip AdjacencyMatrix for n > 1000
- Non-Eulerian connectivity produces disconnected graphs (included for comparison)
- Timeout: 60 minutes max, may return incomplete path

## Total Runs

- 4 graph sizes × 3 connectivity types × 2 algorithms × 3 representations × 10 attempts - (Adjacency Matrix fails) = 600 runs

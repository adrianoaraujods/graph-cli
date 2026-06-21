# graph-cli

Ferramenta de linha de comando para geração e análise de grafos. Projeto acadêmico da disciplina Teoria dos Grafos Computacional (TGC) da PUC Minas.

Grafos sintéticos podem ser gerados com conectividade controlada, e algoritmos clássicos podem ser executados para exploração didática e comparação de desempenho.

## Funcionalidades

- **Geração de grafos** — direcionados ou não-direcionados, com conectividade controlada:
  - Conexo / Fortemente Conexo / Desconexo / Euleriano / Semi-Euleriano
  - Arestas com peso opcional
  - Semente aleatória para reprodutibilidade
- **3 representações internas**:
  - **Forward Star** (_default_) — compacta, cache-friendly, alta performance
  - **Lista de Adjacência** — dinâmica, boa para mutações
  - **Matriz de Adjacência** — simples, mas consome mais memória
- **12 algoritmos**: DFS, Kosaraju, Fleury, Tarjan, Pontes Ingênuas, Dijkstra, Dinic (Fluxo Máximo / Caminhos Disjuntos), Floyd-Warshall, González (k-Center), FastMap (k-Center), WVA-IG (k-Center), Exact (k-Center)
- **Benchmarks automatizados** para Dijkstra, Fleury, Caminhos Disjuntos e k-Center com suporte a execução paralela

## Requisitos

- **Java 26**
- **Maven 3.x** (opcional)

## Compilação e Execução

```bash
# Compilar usando o Maven
mvn package

# Compilar sem o Maven
javac -d target/classes --release 25 $(find src/main/java -name "*.java")

# Executar usando o Maven
java -jar target/graph-cli-0.1.0.jar <subcomando> [options]

# Executar sem o Maven
java -cp target/classes graph.GraphCLI
```

## Uso e Exemplos

Dois subcomandos principais: `create` (gerar) e `read` (analisar).

Use `help` para a referência completa de flags:

```bash
java -jar target/graph-cli-0.1.0.jar help
java -jar target/graph-cli-0.1.0.jar help create
java -jar target/graph-cli-0.1.0.jar help read
```

### Create

Gera um arquivo de grafo sintético.

```bash
# Grafo direcionado com 1000 vértices e densidade 0,5
java -jar target/graph-cli-0.1.0.jar create graph.txt -n 1000 -d 0.5

# Grafo não-direcionado Euleriano com 100 vértices e 5000 arestas
java -jar target/graph-cli-0.1.0.jar create euler.txt -n 100 -m 5000 --eulerian -u

# Grafo direcionado fortemente conexo com pesos
java -jar target/graph-cli-0.1.0.jar create weighted.txt -n 1000 -d 0.5 --strongly --min-weight 1 --max-weight 100

# Com semente para reprodutibilidade
java -jar target/graph-cli-0.1.0.jar create graph.txt -n 1000 -m 500 -s 42
```

| Flag                           | Descrição                              |
| ------------------------------ | -------------------------------------- |
| `--vertices`, `-n`             | Número de vértices (obrigatório)       |
| `--edges`, `-m`                | Número de arestas                      |
| `--density`, `-d`              | Densidade 0.0–1.0 (alternativa a `-m`) |
| `--seed`, `-s`                 | Semente aleatória                      |
| `--directed`                   | Grafo direcionado (_default_)          |
| `--undirected`, `-u`           | Grafo não-direcionado                  |
| `--connected`                  | Conexo (_default_ para direcionado)    |
| `--strongly`                   | Fortemente conexo (direcionado apenas) |
| `--disconnected`               | Pode ser desconexo                     |
| `--eulerian`                   | Euleriano                              |
| `--semi-eulerian`              | Semi-Euleriano                         |
| `--min-weight`, `--max-weight` | Intervalo de pesos                     |

### Read

Lê um arquivo de grafo e executa algoritmos.

```bash
# DFS a partir do vértice 5
java -jar target/graph-cli-0.1.0.jar read graph.txt --dfs -t 5

# Especificar representação
java -jar target/graph-cli-0.1.0.jar read graph.txt -u --adjacency-list --fleury --tarjan

# Salvar saída em arquivo
java -jar target/graph-cli-0.1.0.jar read graph.txt --dfs -t 5 -o result.log

# Kosaraju + Fleury
java -jar target/graph-cli-0.1.0.jar read graph.txt --kosaraju --fleury

# Pontes (Tarjan)
java -jar target/graph-cli-0.1.0.jar read graph.txt --tarjan

# Dijkstra em grafo com peso
java -jar target/graph-cli-0.1.0.jar read ./examples/weighted-8v16e.txt --weighted --dijkstra --source 5

# Dijkstra em grafo com peso mostrando o caminho
java -jar target/graph-cli-0.1.0.jar read ./examples/weighted-8v16e.txt --weighted --dijkstra --source 5 --path

# Caminhos disjuntos em arestas (Dinic)
java -jar target/graph-cli-0.1.0.jar read ./examples/flow-11v17e.txt --capacities --disjoint-paths --source 1 --target 11

# Fluxo máximo (Dinic) com capacidades
java -jar target/graph-cli-0.1.0.jar read ./examples/flow-11v17e.txt --capacities --dinic --source 1 --target 11

# k-Center: González 2-approximation
java -jar target/graph-cli-0.1.0.jar read examples/pmed/pmed1.txt --weighted --gonzalez -k 5

# k-Center: González lendo k do cabeçalho do arquivo (formato PMED: n m k)
java -jar target/graph-cli-0.1.0.jar read examples/pmed/pmed1.txt --weighted --gonzalez --k-centers

# k-Center: FastMap + K-Means++
java -jar target/graph-cli-0.1.0.jar read examples/pmed/pmed1.txt --weighted --fastmap --k-centers

# k-Center: WVA-IG Heuristic
java -jar target/graph-cli-0.1.0.jar read examples/pmed/pmed1.txt --weighted --wva-ig --k-centers

# k-Center: Exact bitmask B&B (max ~128 vértices)
java -jar target/graph-cli-0.1.0.jar read examples/pmed/pmed1.txt --weighted --exact --k-centers

# Floyd-Warshall all-pairs shortest path
java -jar target/graph-cli-0.1.0.jar read examples/weighted-8v16e.txt --weighted --floyd-warshall
```

| Flag                    | Descrição                                                                                          |
| ----------------------- | -------------------------------------------------------------------------------------------------- |
| `--dfs`                 | Busca em profundidade (requer `-t`)                                                                |
| `--kosaraju`            | Componentes fortemente conexos                                                                     |
| `--fleury`              | Caminho/Ciclo Euleriano                                                                            |
| `--tarjan`              | Pontes (Tarjan)                                                                                    |
| `--naive-local`         | Pontes Ingênuas (Local)                                                                            |
| `--naive-global`        | Pontes Ingênuas (Global)                                                                           |
| `--dijkstra`            | Caminho mínimo (requer `--weighted`, `--source`, `--target`)                                       |
| `--disjoint-paths`      | Caminhos disjuntos em arestas via fluxo máximo (Dinic, requer `--source`, `--target`, direcionado) |
| `--dinic`               | Fluxo máximo (Dinic, requer `--capacities`, `--source`, `--target`, direcionado)                   |
| `--floyd-warshall`      | Floyd-Warshall all-pairs shortest path (requer `--weighted`)                                       |
| `--gonzalez`            | González 2-approximation para k-Center (requer `--weighted`, `-k` ou `--k-centers`)                |
| `--fastmap`             | FastMap embedding + K-Means++ para k-Center (requer `--weighted`, `-k` ou `--k-centers`)           |
| `--wva-ig`              | WVA-IG Heuristic para k-Center (requer `--weighted`, `-k` ou `--k-centers`)                        |
| `--exact`               | Exact Bitmask B&B para k-Center (requer `--weighted`, `-k` ou `--k-centers`, max 128 vértices)     |
| `--capacities`          | Capacidades nas arestas (formato: `u v capacity`, mutuamente exclusivo com `--weighted`)           |
| `--source`              | Vértice de origem (obrigatório para `--dijkstra`, `--disjoint-paths`, `--dinic`)                   |
| `-t`, `--target`        | Vértice de destino (obrigatório para `--dfs`, `--dijkstra`, `--disjoint-paths`, `--dinic`)         |
| `--path`                | Exibir caminho                                                                                     |
| `-k`, `--centers`       | Número de centros (obrigatório para algoritmos k-Center)                                           |
| `--k-centers`           | Ler k do cabeçalho do arquivo (formato: `n m k`, alternativa a `-k`)                               |
| `-o`, `--output`        | Arquivo de saída                                                                                   |
| `--forward-star`        | Forward Star (_default_)                                                                           |
| `--adjacency-matrix`    | Matriz de Adjacência                                                                               |
| `--adjacency-list`      | Lista de Adjacência                                                                                |
| `--weighted`            | Grafo com peso (formato: `u v w`)                                                                  |

## Algoritmos

| Algoritmo                  | Flag               | Descrição                                                     | Direcionado | Peso |
| -------------------------- | ------------------ | ------------------------------------------------------------- | :---------: | :--: |
| DFS                        | `--dfs`            | Busca em profundidade com classificação de arestas            |     Sim     | Não  |
| Kosaraju                   | `--kosaraju`       | Componentes fortemente conexos (2-passagens DFS)              |     Sim     | Não  |
| Fleury                     | `--fleury`         | Caminho ou ciclo Euleriano (usa Tarjan ou Pontes Ingênuas)    |     Sim     | Não  |
| Tarjan                     | `--tarjan`         | Encontra todas as pontes (DFS + low-link, O(V+E))             |     Não     | Não  |
| Pontes Ingênuas (Global)   | `--naive-global`   | Encontra pontes testando cada aresta (Union-Find, paralelo)   |     Não     | Não  |
| Pontes Ingênuas (Local)    | `--naive-local`    | Verificação sob demanda durante Fleury (short-circuit)        |     Não     | Não  |
| Dijkstra                   | `--dijkstra`       | Caminho mínimo (pesos positivos, desempate por menos arestas) |     Sim     | Sim  |
| Floyd-Warshall             | `--floyd-warshall` | Caminhos mínimos entre todos os pares (programação dinâmica)  |     Sim     | Sim  |
| Dinic / Caminhos Disjuntos | `--disjoint-paths` | Caminhos disjuntos em arestas via fluxo máximo                |     Sim     | Não  |
| Dinic (Fluxo Máximo)       | `--dinic`          | Fluxo máximo em redes com capacidades nas arestas             |     Sim     | Não  |
| González (k-Center)        | `--gonzalez`       | 2-aproximação via farthest-first traversal, O(kn)             |     Sim     | Sim  |
| FastMap (k-Center)         | `--fastmap`        | Embedding 2D espectral + K-Means++                            |     Sim     | Sim  |
| WVA-IG (k-Center)          | `--wva-ig`         | Worst-Vertex-Anchored Iterated Greedy com busca local         |     Sim     | Sim  |
| Exact (k-Center)           | `--exact`          | Bitmask B&B exato (busca binária no raio), O(2^n)             |     Sim     | Sim  |

## Formato do Arquivo de Grafo

Arquivo texto simples. A primeira linha contém o número de vértices e arestas. As linhas seguintes listam as arestas.

```
<V> <E>
<u> <v>
<u> <v>
```

Para grafos com peso nas arestas (utilizando `-weighted`), adiciona-se um terceiro campo:

```
<V> <E>
<u> <v> <peso>
<u> <v> <peso>
```

Para grafos com capacidade nas arestas (utilizando `--capacities`), o formato segue o mesmo padrão de 3 campos:

```
<V> <E>
<u> <v> <capacidade>
<u> <v> <capacidade>
```

Para o formato PMED utilizado pelos algoritmos k-Center com `--k-centers`, a primeira linha contém três números:

```
<V> <E> <k>
<u> <v> <peso>
<u> <v> <peso>
```

As 40 instâncias PMED da OR-Library estão em [`examples/pmed/`](examples/pmed/).

Arquivos de exemplo adicionais disponíveis em [`examples/`](examples/).

## Estrutura do Projeto

```
graph-cli/
├── src/
│   └── main/java/graph/
│       ├── GraphCLI.java        # Ponto de entrada
│       ├── api/                 # Interfaces e contratos
│       ├── algorithms/          # Implementações dos algoritmos
│       ├── representations/     # Forward Star, Lista, Matriz
│       ├── cli/                 # Parser, handlers, leitura/escrita
│       ├── bench/               # Benchmarks (Dijkstra, Fleury, DisjointPaths, KCenter)
│       └── util/                # Utilitários (Timer, Sort, Usage)
├── examples/                    # Grafos de exemplo
└── articles/                    # Artigos acadêmicos (LaTeX + PDF + resultados)
```

## Benchmarks

O projeto inclui quatro harnesses de benchmark para executar experimentos de desempenho:

### Dijkstra Benchmark

Testa o algoritmo de Dijkstra em grafos de diferentes tamanhos e conectividades.

```bash
# Sequencial
java -cp target/classes graph.bench.DijkstraBenchmark

# Paralelo com 8 threads
java -cp target/classes graph.bench.DijkstraBenchmark --parallel --max-threads 8

# Configurar número de tentativas e saída
java -cp target/classes graph.bench.DijkstraBenchmark --attempts 5 --output resultados.csv
```

### Fleury Benchmark

Testa o algoritmo de Fleury com diferentes estratégias de detecção de pontes (Tarjan vs. Naive Local) e representações.

```bash
# Sequencial
java -cp target/classes graph.bench.FleuryBenchmark

# Paralelo
java -cp target/classes graph.bench.FleuryBenchmark --parallel --max-threads 8

# Saída personalizada
java -cp target/classes graph.bench.FleuryBenchmark --attempts 4 --output resultados.csv
```

### Disjoint Paths Benchmark

Testa o algoritmo de Caminhos Disjuntos (via fluxo máximo/Dinic) em grafos direcionados de diferentes tamanhos e conectividades.

```bash
# Sequencial
java -cp target/classes graph.bench.DisjointPathsBenchmark

# Paralelo com 8 threads
java -cp target/classes graph.bench.DisjointPathsBenchmark --parallel --max-threads 8

# Configurar número de tentativas e saída
java -cp target/classes graph.bench.DisjointPathsBenchmark --attempts 3 --output resultados.csv
```

### K-Center Benchmark

Testa os 4 algoritmos de k-Center (González, FastMap, WVA-IG, Exact) nas 40 instâncias PMED da OR-Library.

```bash
# Sequencial (todas as 40 instâncias, 5 tentativas cada)
java -cp target/classes graph.bench.KCenterBenchmark

# Paralelo com 8 threads
java -cp target/classes graph.bench.KCenterBenchmark --parallel --max-threads 8

# Filtrar instâncias e algoritmos específicos
java -cp target/classes graph.bench.KCenterBenchmark --instances 1,2,3 --algorithms Gonzalez,WVA-IG

# Customizar diretório PMED, tentativas, e limite para Exact
java -cp target/classes graph.bench.KCenterBenchmark --pmed-dir examples/pmed --attempts 3 --max-n-exact 100 --output kcenter.csv
```

**Flags específicas do K-Center Benchmark:**

| Flag | Descrição | Default |
| ---- | --------- | :-----: |
| `--attempts` | Tentativas por configuração | 5 |
| `--parallel` | Execução paralela | off |
| `--max-threads` | Número máximo de threads | 24 |
| `--output` | Arquivo CSV de saída | results.csv |
| `--pmed-dir` | Diretório das instâncias PMED | examples/pmed |
| `--max-n-exact` | Máx. vértices para rodar Exact | 300 |
| `--instances` | Filtrar por índices (ex: `1,2,3`) | todas |
| `--algorithms` | Filtrar por algoritmo (ex: `Gonzalez,FastMap`) | todos |

Colunas do CSV: `instance,n,k,algorithm,attempt,floyd_ms,algo_ms,radius,optimal_radius,gap_pct`.

**Flags comuns aos benchmarks:**

| Flag            | Descrição                             |          Default           |
| --------------- | ------------------------------------- | :------------------------: |
| `--attempts`    | Número de tentativas por configuração | 10 (Dijkstra) / 4 (Fleury) / 4 (DisjointPaths) / 5 (k-Center) |
| `--parallel`    | Execução paralela                     |            off             |
| `--max-threads` | Número máximo de threads              |             24             |
| `--output`      | Arquivo CSV de saída                  |        results.csv         |

Os resultados são exportados em formato CSV para análise em planilhas ou scripts.

## Notas de Desempenho

Para grafos grandes, recomenda-se configurar a JVM com mais memória:

```bash
java -cp target/classes -Xmx16g -XX:+UseG1GC -jar target/graph-cli-0.1.0.jar
```

Grafos com mais de **50 milhões de arestas** exibem uma confirmação antes de prosseguir (pode ser desabilitada em benchmarks via `setSkipConfirmation(true)`).

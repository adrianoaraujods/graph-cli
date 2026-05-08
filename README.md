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
- **6 algoritmos**: DFS, Kosaraju, Fleury, Tarjan, Pontes Ingênuas, Dijkstra
- **Benchmarks automatizados** para Dijkstra e Fleury com suporte a execução paralela

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

# Kosaraju + Fleury
java -jar target/graph-cli-0.1.0.jar read graph.txt --kosaraju --fleury

# Pontes (Tarjan)
java -jar target/graph-cli-0.1.0.jar read graph.txt --tarjan

# Dijkstra em grafo com peso
java -jar target/graph-cli-0.1.0.jar read weighted.txt --weighted --dijkstra --source 1 --target 5 --path

# Especificar representação
java -jar target/graph-cli-0.1.0.jar read graph.txt -u --adjacency-list --fleury --tarjan

# Salvar saída em arquivo
java -jar target/graph-cli-0.1.0.jar read graph.txt --dfs -t 5 -o resultado.log
```

| Flag                 | Descrição                                                    |
| -------------------- | ------------------------------------------------------------ |
| `--dfs`              | Busca em profundidade (requer `-t`)                          |
| `--kosaraju`         | Componentes fortemente conexos                               |
| `--fleury`           | Caminho/Ciclo Euleriano                                      |
| `--tarjan`           | Pontes (Tarjan)                                              |
| `--naive-local`      | Pontes Ingênuas (Local)                                      |
| `--naive-global`     | Pontes Ingênuas (Global)                                     |
| `--dijkstra`         | Caminho mínimo (requer `--weighted`, `--source`, `--target`) |
| `--source`           | Vértice de origem                                            |
| `-t`, `--target`     | Vértice de destino                                           |
| `--path`             | Exibir caminho                                               |
| `-o`, `--output`     | Arquivo de saída                                             |
| `--forward-star`     | Forward Star (_default_)                                     |
| `--adjacency-matrix` | Matriz de Adjacência                                         |
| `--adjacency-list`   | Lista de Adjacência                                          |
| `--weighted`         | Grafo com peso (formato: `u v w`)                            |

## Algoritmos

| Algoritmo                | Flag             | Descrição                                                     | Direcionado | Peso |
| ------------------------ | ---------------- | ------------------------------------------------------------- | :---------: | :--: |
| DFS                      | `--dfs`          | Busca em profundidade com classificação de arestas            |     Sim     | Não  |
| Kosaraju                 | `--kosaraju`     | Componentes fortemente conexos (2-passagens DFS)              |     Sim     | Não  |
| Fleury                   | `--fleury`       | Caminho ou ciclo Euleriano (usa Tarjan ou Pontes Ingênuas)    |     Sim     | Não  |
| Tarjan                   | `--tarjan`       | Encontra todas as pontes (DFS + low-link, O(V+E))             |     Não     | Não  |
| Pontes Ingênuas (Global) | `--naive-global` | Encontra pontes testando cada aresta (Union-Find, paralelo)   |     Não     | Não  |
| Pontes Ingênuas (Local)  | `--naive-local`  | Verificação sob demanda durante Fleury (short-circuit)        |     Não     | Não  |
| Dijkstra                 | `--dijkstra`     | Caminho mínimo (pesos positivos, desempate por menos arestas) |     Sim     | Sim  |

## Formato do Arquivo de Grafo

Arquivo texto simples. A primeira linha contém o número de vértices e arestas. As linhas seguintes listam as arestas.

```
<V> <E>
<u> <v>
<u> <v>
```

Para grafos com peso, adiciona-se um terceiro campo:

```
<V> <E>
<u> <v> <peso>
<u> <v> <peso>
```

Arquivos de exemplo disponíveis em [`examples/`](examples/).

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
│       ├── bench/               # Benchmarks (Dijkstra, Fleury)
│       └── util/                # Utilitários (Timer, Sort, Usage)
├── examples/                    # Grafos de exemplo
└── articles/                    # Artigos acadêmicos (LaTeX + PDF + resultados)
```

## Benchmarks

O projeto inclui dois harnesses de benchmark para executar experimentos de desempenho:

### Dijkstra Benchmark

Testa o algoritmo de Dijkstra em grafos de diferentes tamanhos e conectividades.

```bash
# Sequencial
java graph.bench.DijkstraBenchmark

# Paralelo com 8 threads
java graph.bench.DijkstraBenchmark --parallel --max-threads 8

# Configurar número de tentativas e saída
java graph.bench.DijkstraBenchmark --attempts 5 --output resultados.csv
```

### Fleury Benchmark

Testa o algoritmo de Fleury com diferentes estratégias de detecção de pontes (Tarjan vs. Naive Local) e representações.

```bash
# Sequencial
java graph.bench.FleuryBenchmark

# Paralelo
java graph.bench.FleuryBenchmark --parallel --max-threads 8

# Saída personalizada
java graph.bench.FleuryBenchmark --attempts 4 --output resultados.csv
```

**Flags comuns:**

| Flag            | Descrição                             |          Default           |
| --------------- | ------------------------------------- | :------------------------: |
| `--attempts`    | Número de tentativas por configuração | 10 (Dijkstra) / 4 (Fleury) |
| `--parallel`    | Execução paralela                     |            off             |
| `--max-threads` | Número máximo de threads              |             24             |
| `--output`      | Arquivo CSV de saída                  |        results.csv         |

Os resultados são exportados em formato CSV para análise em planilhas ou scripts.

## Notas de Desempenho

Para grafos grandes, recomenda-se configurar a JVM com mais memória:

```bash
java -Xmx16g -XX:+UseG1GC -jar target/graph-cli-0.1.0.jar ...
```

Grafos com mais de **50 milhões de arestas** exibem uma confirmação antes de prosseguir (pode ser desabilitada em benchmarks via `setSkipConfirmation(true)`).

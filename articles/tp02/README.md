# graph-cli

Ferramenta de linha de comando para análise de grafos para resolução do
**Problema dos k-Centros (Vertex k-Center)**. Projeto acadêmico da disciplina
Teoria dos Grafos Computacional (TGC) da PUC Minas.

## Algoritmos

- **Exact Solver** — Algoritmo exato paralelo utilizando heurística de _Set
  Covering_, _Domination Pruning_ e _Bitmask Branch & Bound_.
- **Gonzalez** — Algoritmo de 2-aproximação.
- **FastMap + K-Means++** — Heurística rápida de mapeamento e clusterização.
- **WVA-IG** — Metaheurística combinando busca local com trocas sob o resultado
  do **Gonzalez**.

## Requisitos

- **Java 21**
- **Maven 3.x**

## Compilação

Compilar o projeto com o Maven (configurado no `pom.xml`):

```bash
mvn clean package
```

Para compilar sem o Maven (assume-se que esta no linux):

```bash
javac -d target/classes --release 21 $(find src/main/java -name "*.java")
```

## Uso e Exemplos

O subcomando principal para a resolução do problema é o `read`, que permite ler
um arquivo de texto contendo a descrição de um grafo. Em seguida deve-se
fornecer o algoritimo a ser utilizado.

| Flag           | Descrição                                                               |
| -------------- | ----------------------------------------------------------------------- |
| `--k-centers`  | Habilita a suíte de resolução do problema dos k-Centros                 |
| `--exact`      | Executa o resolvedor Exato (Branch & Bound)                             |
| `--gonzalez`   | Executa a 2-Aproximação de Gonzalez                                     |
| `--fastmap`    | Executa a heurística FastMap + K-Means++                                |
| `--wva-ig`     | Executa a heurística WVA-IG                                             |
| `--undirected` | Trata o grafo como não-direcionado (_necessário para os arquivos pmed_) |
| `--weighted`   | Informa que o arquivo possui pesos nas arestas                          |

Ler uma instância da OR-Library e rodar o algoritmo Exato:

```bash
java -jar target/graph-cli-0.1.0.jar read ./examples/pmed/pmed1.txt --undirected --weighted --k-centers --exact
```

Rodar a aproximação de Gonzalez:

```bash
java -jar target/graph-cli-0.1.0.jar read ./examples/pmed/pmed40.txt --undirected --weighted --k-centers --gonzalez
```

## Formato do Arquivo de Grafo (pmed)

O parser espera o formato padrão de instâncias OR-Library para o problema
$k$-centros. A primeira linha contém o número de vértices (`n`), o número de
arestas (`m`) e o número de centros (`k`). As linhas seguintes listam as arestas
no formato `u v peso`.

Arquivo texto simples:

```
<n> <m> <k>
<u> <v> <peso>
<u> <v> <peso>
```

### K-Center Benchmark

Testa os 4 algoritmos de k-Center (González, FastMap, WVA-IG, Exact) nas 40
instâncias PMED da OR-Library.

bash

```bash
# Sequencial (todas as 40 instâncias, 5 tentativas cada)
java -cp target/classes graph.bench.KCenterBenchmark
```

```bash
# Paralelo com 8 threads
java -cp target/classes graph.bench.KCenterBenchmark --parallel --max-threads 8
```

```bash
# Filtrar instâncias e algoritmos específicos
java -cp target/classes graph.bench.KCenterBenchmark --instances 1,2,3 --algorithms Gonzalez,WVA-IG
```

```bash
# Customizar diretório PMED, tentativas, e limite para Exact
java -cp target/classes graph.bench.KCenterBenchmark --pmed-dir examples/pmed --attempts 3 --max-n-exact 100 --output kcenter.csv
```

**Flags disponíveis K-Center Benchmark:**

| Flag            | Descrição                                      |    Default    |
| --------------- | ---------------------------------------------- | :-----------: |
| `--attempts`    | Tentativas por configuração                    |       5       |
| `--parallel`    | Execução paralela                              |      off      |
| `--max-threads` | Número máximo de threads                       |      24       |
| `--output`      | Arquivo CSV de saída                           |  results.csv  |
| `--pmed-dir`    | Diretório das instâncias PMED                  | examples/pmed |
| `--max-n-exact` | Máx. vértices para rodar Exact                 |      300      |
| `--instances`   | Filtrar por índices (ex: `1,2,3`)              |     todas     |
| `--algorithms`  | Filtrar por algoritmo (ex: `Gonzalez,FastMap`) |     todos     |

Colunas do CSV:
`instance,n,k,algorithm,attempt,floyd_ms,algo_ms,radius,optimal_radius,gap_pct`.

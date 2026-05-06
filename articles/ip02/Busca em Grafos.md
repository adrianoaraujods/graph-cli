# Implementação N.02 - Busca em Grafos

## Descrição da tarefa

Nessa tarefa iremos explorar a implementação de busca em grafos e seu uso para classificação de arestas.

Portanto, você deve implementar (na linguagem de C, C++ ou Java) um programa que receba duas informações do usuário:

1. O nome do arquivo contendo as informações/dados sobre um grafo direcionado;
2. O número de um dos vértices do grafo descrito no arquivo.

Seu programa deverá realizar uma busca em profundidade (em ordem lexicográfica) no grafo direcionado fornecido, listando todas as arestas de árvore encontradas. Além disso, sua implementação deve classificar todas as arestas divergentes (que saem) do vértice escolhido pelo usuário.

> OBS.:
> É necessário produzir a classificação de todas as arestas apenas para o vértice informado.

Para testar seu programa você pode utilizar os arquivos abaixo:

- [graph-test-100.txt](../data/graph-test-100.txt)
- [graph-test-50000.txt](../data/graph-test-50000.txt)

## Formato do arquivo contendo os dados do grafo

Seu programa deverá ler as informações sobre o grafo a partir de um arquivo texto. A primeira linha desse arquivo contém o número n de vértices seguido do número m de arestas. Você deve considerar que os vértices são numerados (rotulados) de 1 a $n$. Depois disso, o arquivo contém uma lista com as m arestas (sendo uma aresta por linha) em que cada aresta é representada pelos seus vértices de origem e de destino.

Abaixo, você pode observa um esquema que representa a estrutura que deve ser esperada do arquivo:

| $n$                          | $m$                           |
| ---------------------------- | ----------------------------- |
| $\text{Origem da Aresta } 1$ | $\text{Destino da Aresta } 1$ |
| $\text{Origem da Aresta } 2$ | $\text{Destino da Aresta } 2$ |
| $\dots$                      | $\dots$                       |
| $\text{Origem da Aresta } m$ | $\text{Destino da Aresta } m$ |

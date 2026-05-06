# Implementação N.01 - Representação em Grafos

## Descrição da tarefa

Nessa tarefa iremos explorar a implementação de estruturas de dados para representação de grafos e seu uso para obtenção de informações sobre grau e vizinhança dos vértices.

Portanto, você deve implementar (na linguagem de C, C++ ou Java) um programa que receba duas informações do usuário:

1. Nome do arquivo contendo as informações/dados sobre um grafo direcionado;
2. Número de um dos vértices do grafo descrito no arquivo.

Seu programa deverá ler o conteúdo do arquivo e representar o grafo direcionado em memória utilizando uma das estruturas discutidas em nossas aulas. Depois disso, sua implementação deve utilizar a estrutura escolhida para produzir as seguintes informações para o vértice informado pelo usuário:

1. Grau de saída;
2. grau de entrada;
3. Conjunto de sucessores;
4. Conjunto de predecessores

> OBS.:
> É necessário produzir tais informações apenas para o vértice informado.

A escolha da estrutura faz parte da tarefa e deverá ser feita levando-se em conta o tipo do grafo (que é direcionado), o tamanho do grafo (que poderá ter centenas de milhares de vértices e dezenas de milhões de arestas) e as operações necessárias que serão realizadas sobre o mesmo (para se obter/calcular tanto o grau quanto a vizinhança de um vértice).

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

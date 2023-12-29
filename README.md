# [Strategic oscillation tabu search for improved hierarchical graph drawing](https://doi.org/10.1016/j.eswa.2023.122668)

## Abstract
In the last years, many areas in science, business, and engineering have experienced an enormous growth in the amount of data that they are required to analyze. In many cases, this analysis relies intimately on data visualization and, as a result, graph drawing has emerged as a new field of research. This paper addresses the challenge of drawing hierarchical graphs, which is one of the most widely used drawing standards. We introduce a new mathematical model to automatically represent a graph based on the alignment of long arcs, which we combine with the classic arc crossing minimization objective in hierarchical drawings. We complement our proposal with a heuristic algorithm that can obtain high-quality results in the short computational time required by graph drawing systems. Our algorithm joins two methodologies, tabu search and strategic oscillation (SOS), to perform a fast and effective exploration of the search space. We conduct extensive experimentation that integrates our new mathematical programming formulation and the SOS tabu search that targets large instances. Our statistical analysis confirms the effectiveness of this proposal.

## Authors
Authors involved in this work:
- Sergio Cavero [sergio.cavero@urjc.es](mailto:sergio.cavero@urjc.es)
- Eduardo G. Pardo [eduardo.pardo@urjc.es](mailto:eduardo.pardo@urjc.es)
- Fred Glover [fred@entanglement.com](mailto:fred@entanglement.com)
- Rafael Martí [rafael.marti@uv.es](mailto:rafael.marti@uv.es)

## Datasets

Instances are categorized in different datasets inside the ['instances' folder](https://github.com/scaverod/SOS-TS-GraphDrawing/tree/main/instances).

## Cite

Consider citing our paper if used in your own work:

### DOI
[https://doi.org/10.1016/j.eswa.2023.122668](https://doi.org/10.1016/j.eswa.2023.122668)

### Bibtex
```bibtex
@article{cavero2024strategic,
  title={Strategic oscillation tabu search for improved hierarchical graph drawing},
  author={Cavero, Sergio and Pardo, Eduardo G and Glover, Fred and Mart{\'\i}, Rafael},
  journal={Expert Systems with Applications},
  volume={243},
  pages={122668},
  year={2024},
  publisher={Elsevier}
}
```

### Other citing forms:
- **MLA**: Cavero, Sergio, et al. "Strategic oscillation tabu search for improved hierarchical graph drawing." Expert Systems with Applications 243 (2024): 122668.
- **APA**: Cavero, S., Pardo, E. G., Glover, F., & Martí, R. (2024). Strategic oscillation tabu search for improved hierarchical graph drawing. Expert Systems with Applications, 243, 122668.
- **IEEE**: S. Cavero, E. G. Pardo, F. Glover, and R. Martí, ‘Strategic oscillation tabu search for improved hierarchical graph drawing’, Expert Systems with Applications, vol. 243, p. 122668, 2024.

## Made with MORK (Metaheuristic Optimization framewoRK)
| ![mork logo](https://user-images.githubusercontent.com/55482385/233611563-4f5c91f2-af36-4437-a4b5-572b6655487a.svg) | Mork is a framework for developing approaches for NP-Hard problems using the JVM. It is currently under heavy development. TLDR: Automatically generate a project using [https://generator.mork-optimization.com/](https://generator.mork-optimization.com/), import in your favourite IDE and start working! See Getting started page in the [Official Documentation for more details](https://docs.mork-optimization.com/en/latest/). |
|--|--|



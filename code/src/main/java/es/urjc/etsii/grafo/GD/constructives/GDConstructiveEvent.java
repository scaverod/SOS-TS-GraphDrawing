package es.urjc.etsii.grafo.GD.constructives;

import es.urjc.etsii.grafo.GD.excel.EvolutionEvent;
import es.urjc.etsii.grafo.GD.model.GDInstance;
import es.urjc.etsii.grafo.GD.model.GDSolution;
import es.urjc.etsii.grafo.create.Constructive;
import es.urjc.etsii.grafo.solver.services.events.EventPublisher;
import es.urjc.etsii.grafo.util.CollectionUtil;

import java.util.*;
import java.util.stream.Collectors;

public class GDConstructiveEvent extends Constructive<GDSolution, GDInstance> {


    /**
     * Min value of w1 weight
     */
    final double SE_w1;

    /**
     * Max value of w1 weight
     */
    final double SE_w2;

    /**
     * Min value of w1 weight
     */
    final double LE_w1;

    /**
     * Max value of w1 weight
     */
    final double LE_w2;

    /**
     * Maximum number of iterations
     */
    final int iterations;

    /**
     * The tabu size is a percentage of vertices of a layer
     * Size of the Tabu Memory = Percentage *  number of vertices of a layer
     */
    final double tabuSize;


    /**
     * Criteria to determine next input vertex to
     */
    final InputVertexSelectionCriteria selectionCriteria;
    /**
     * Criteria to determine where an input vertex should be placed
     */
    final HostVertexCriteria hostVertexCriteria;
    final LongEdgeSelectionCriteria longEdgeSelectionCriteria;
    final LongEdgeLocationCriteria longEdgeLocationCriteria;

    public GDConstructiveEvent(double SE_w1, double SE_w2, double LE_w1, double LE_w2, int iterations, double tabuSize, InputVertexSelectionCriteria selectionCriteria, HostVertexCriteria hostVertexCriteria, LongEdgeSelectionCriteria longEdgeSelectionCriteria, LongEdgeLocationCriteria longEdgeLocationCriteria) {
        this.LE_w1 = LE_w1;
        this.LE_w2 = LE_w2;
        this.SE_w1 = SE_w1;
        this.SE_w2 = SE_w2;
        this.iterations = iterations;
        this.tabuSize = tabuSize;
        this.selectionCriteria = selectionCriteria;
        this.hostVertexCriteria = hostVertexCriteria;
        this.longEdgeSelectionCriteria = longEdgeSelectionCriteria;
        this.longEdgeLocationCriteria = longEdgeLocationCriteria;
        if (SE_w1 + SE_w2 != 1) {
            throw new IllegalArgumentException("Constructive procedure error: se_w1 + se_w2 != 1");
        }
        if (LE_w1 + LE_w2 != 1) {
            throw new IllegalArgumentException("Constructive procedure error: le_w1 + le_w2 != 1");
        }
        if (iterations < 0) {
            throw new IllegalArgumentException("Constructive procedure error: iterations < 0");
        }
    }

    @Override
    public GDSolution construct(GDSolution solution) {
        ArrayList<Integer> evolution = new ArrayList<>(this.iterations);
        int sizeOfTabuMemory = (int) (tabuSize * solution.getInstance().NumVerticesInLayer());
        var tabuMemory = new TabuMemory(sizeOfTabuMemory, solution.getInstance());
        GDSolution bestSolution = solution.cloneSolution();
        bestSolution.setScore(Integer.MAX_VALUE / 2);
        for (int i = 0; i < iterations; i++) {
            GDSolution s = solution.cloneSolution();
            if (constructSolution(s, tabuMemory)) {
                if (s.isBetterThan(bestSolution)) {
                    bestSolution = s;
                }
                evolution.add((int) bestSolution.getScore());
            }

        }
        bestSolution.updateLastModifiedTime();
        var event = new EvolutionEvent(this.getClass().getSimpleName(), solution.getInstance().getId(), iterations, evolution);
        EventPublisher.getInstance().publishEvent(event);
        return bestSolution;
    }

    /**
     * Construct a solution using GDGRASP constructive procedure
     *
     * @param solution   soliton to be constructed
     * @param tabuMemory tabu memory
     * @return true if a new solution is constructed, false otherwise
     */
    private boolean constructSolution(GDSolution solution, TabuMemory tabuMemory) {
        GDInstance ins = solution.getInstance();
        HashSet<Integer> addedVertices = new HashSet<>(solution.getInstance().TotalNumVertices());
        HashMap<Integer, ArrayList<GDInstance.Edge>> addedEdges = new HashMap<>();
        for (int layer = 0; layer < solution.getInstance().NumberOfLayers(); layer++) {
            addedEdges.put(layer, new ArrayList<>());
        }
        boolean[][] used = new boolean[ins.NumberOfLayers()][ins.NumVerticesInLayer()];
        if (!addLongEdges(solution, ins, used, tabuMemory, addedVertices, addedEdges)) return false;
        return addRemainingVertices(solution, ins, used, addedVertices, addedEdges);
    }

    /**
     * Add long edges to the solution
     *
     * @param solution      solution to be constructed
     * @param ins           instance
     * @param used          used positions of the solution
     * @param tabuMemory    tabu memory
     * @param addedVertices vertices added to the solution
     * @param addedEdges
     * @return true if all long edges are added, false otherwise
     */
    private boolean addLongEdges(GDSolution solution, GDInstance ins, boolean[][] used, TabuMemory tabuMemory, HashSet<Integer> addedVertices, HashMap<Integer, ArrayList<GDInstance.Edge>> addedEdges) {
        var listOfLongEdges = new ArrayList<>(solution.getInstance().getListOfLongEdges());
        HashMap<GDInstance.Edge, Double> mcallister = new HashMap<>(listOfLongEdges.size());
        sortListOfLongEdges(listOfLongEdges, ins, mcallister);
        while (!listOfLongEdges.isEmpty()) {
            var longEdge = listOfLongEdges.remove(0);
            ArrayList<Integer> availablePositions = getLongEdgePositions(longEdge, used, tabuMemory, ins, addedVertices, solution);
            if (availablePositions.size() == 0) return false;
            int pos = CollectionUtil.pickRandom(availablePositions);
            addLongEdge(solution, used, pos, longEdge, addedVertices, addedEdges);
            if (this.longEdgeSelectionCriteria == LongEdgeSelectionCriteria.MCALLISTER) {
                updateListByMcallister(listOfLongEdges, longEdge, mcallister, ins);
            }
            if (this.longEdgeLocationCriteria == LongEdgeLocationCriteria.RANDOM_WITH_TABU || this.longEdgeLocationCriteria == LongEdgeLocationCriteria.MEDIAN_WITH_TABU) {
                tabuMemory.updateTabuList(longEdge, pos);
            }
        }
        return true;
    }

    /**
     * Sort long edges by criteria to the solution
     *
     * @param listOfLongEdges list of long edges
     * @param ins             instance
     */
    private void sortListOfLongEdges(ArrayList<GDInstance.Edge> listOfLongEdges, GDInstance ins, HashMap<GDInstance.Edge, Double> mcallister) {
        if (listOfLongEdges.size() > 1) {
            if (this.longEdgeSelectionCriteria == LongEdgeSelectionCriteria.RANDOM) {
                CollectionUtil.shuffle(listOfLongEdges);
            } else if (this.longEdgeSelectionCriteria == LongEdgeSelectionCriteria.LONGEST_FIRST) {
                listOfLongEdges.sort(Comparator.comparingInt(o -> ins.getListOfShortEdgesOfLongEdge((GDInstance.Edge) o).size()).reversed());
            } else if (this.longEdgeSelectionCriteria == LongEdgeSelectionCriteria.SHORTEST_FIRST) {
                listOfLongEdges.sort(Comparator.comparingInt(o -> ins.getListOfShortEdgesOfLongEdge(o).size()));
            } else if (this.longEdgeSelectionCriteria == LongEdgeSelectionCriteria.MCALLISTER) {
                initMcallister(listOfLongEdges, ins, mcallister);
                listOfLongEdges.sort(Comparator.comparingDouble(mcallister::get).reversed());
            }
        }
    }

    /**
     * Initialize mcallister map
     *
     * @param listOfLongEdges list of long edges
     * @param ins             instance
     * @param mcallister      mcallister map
     */
    private void initMcallister(ArrayList<GDInstance.Edge> listOfLongEdges, GDInstance ins, HashMap<GDInstance.Edge, Double> mcallister) {
        for (GDInstance.Edge longEdge : listOfLongEdges) {
            mcallister.put(longEdge, ins.getSetOfAdjacentVerticesWithLongEdgesOfLongEdge(longEdge).size() * -this.LE_w2);
        }
    }

    /**
     * Update mcallister map
     *
     * @param listOfLongEdges list of long edges
     * @param longEdge        long edge
     * @param mcallister      mcallister map
     * @param ins             instance
     */
    private void updateListByMcallister(ArrayList<GDInstance.Edge> listOfLongEdges, GDInstance.Edge longEdge, HashMap<GDInstance.Edge, Double> mcallister, GDInstance ins) {
        mcallister.remove(longEdge);
        for (Map.Entry<GDInstance.Edge, Double> edgeDoubleEntry : mcallister.entrySet()) {
            if (ins.getSetOfAdjacentVerticesWithLongEdgesOfLongEdge(edgeDoubleEntry.getKey()).contains(longEdge.source())) {
                edgeDoubleEntry.setValue(edgeDoubleEntry.getValue() + 1);
            }
            if (ins.getSetOfAdjacentVerticesWithLongEdgesOfLongEdge(edgeDoubleEntry.getKey()).contains(longEdge.target())) {
                edgeDoubleEntry.setValue(edgeDoubleEntry.getValue() + 1);
            }
        }
        listOfLongEdges.sort(Comparator.comparingDouble(mcallister::get).reversed());
    }

    /**
     * Given a long edge, return the positions of the host graph where it can be inserted
     *
     * @param longEdge      long edge
     * @param used          boolean matrix of used host veritces
     * @param tabuMemory    tabu memory
     * @param ins           instance
     * @param addedVertices vertices added to the host graph
     * @param solution      solution
     * @return positions of the host graph where the long edge can be inserted
     */
    private ArrayList<Integer> getLongEdgePositions(GDInstance.Edge longEdge, boolean[][] used, TabuMemory tabuMemory, GDInstance ins, HashSet<Integer> addedVertices, GDSolution solution) {
        if (this.longEdgeLocationCriteria == LongEdgeLocationCriteria.RANDOM) {
            return this.getLongEdgePositionRANDOM(longEdge, used, ins);
        } else if (this.longEdgeLocationCriteria == LongEdgeLocationCriteria.RANDOM_WITH_TABU) {
            return this.getLongEdgePositionRANDOM_WITH_TABU(longEdge, used, tabuMemory, ins);
        } else if (this.longEdgeLocationCriteria == LongEdgeLocationCriteria.MEDIAN) {
            return this.getLongEdgePositionMEDIAN(longEdge, used, ins, addedVertices, solution);
        } else if (this.longEdgeLocationCriteria == LongEdgeLocationCriteria.MEDIAN_WITH_TABU) {
            return this.getLongEdgePositionMEDIAN_WITH_TABU(longEdge, used, tabuMemory, ins, addedVertices, solution);
        } else {
            throw new IllegalArgumentException("Constructive Heuristic: Long Edge Location Criteria not implemented");
        }
    }

    /**
     * Given a long edge, return the positions of the host graph where it can be inserted
     *
     * @param longEdge long edge
     * @param layers   boolean matrix of used host veritces
     * @param ins      instance
     * @return positions of the host graph where the long edge can be inserted
     * TODO: generate an initial list of available positions and update it
     */
    private ArrayList<Integer> getLongEdgePositionRANDOM(GDInstance.Edge longEdge, boolean[][] layers, GDInstance ins) {
        ArrayList<Integer> availablePositions = new ArrayList<>();
        int layerOfLongEdge = ins.getLayerOfVertex(longEdge.source());
        for (int pos = 0; pos < layers[layerOfLongEdge].length; pos++) {
            int size = ins.getListOfShortEdgesOfLongEdge(longEdge).size() + 1;
            if (isValidPosition(layers, layerOfLongEdge, pos, size)) {
                availablePositions.add(pos);
            }
        }
        return availablePositions;
    }

    // METHODS TO WORK WITH MCALLISTER

    /**
     * Given a long edge, return the positions of the host graph where it can be inserted. Uses tabu memory: if a position is already in the tabu memory, it is not considered
     *
     * @param longEdge   long edge
     * @param layers     boolean matrix of used host vertices
     * @param tabuMemory tabu memory
     * @param ins        instance
     * @return positions of the host graph where the long edge can be inserted
     * TODO: generate an initial list of available positions and update it
     */
    private ArrayList<Integer> getLongEdgePositionRANDOM_WITH_TABU(GDInstance.Edge longEdge, boolean[][] layers, TabuMemory tabuMemory, GDInstance ins) {
        ArrayList<Integer> availablePositions = new ArrayList<>();
        int layerOfLongEdge = ins.getLayerOfVertex(longEdge.source());
        for (int pos = 0; pos < layers[layerOfLongEdge].length; pos++) {
            if (!tabuMemory.isInTabuList(longEdge, pos)) {
                int size = ins.getListOfShortEdgesOfLongEdge(longEdge).size() + 1;
                if (isValidPosition(layers, layerOfLongEdge, pos, size)) {
                    availablePositions.add(pos);
                }
            }
        }
        return availablePositions;
    }

    /**
     * Given a long edge, return the positions of the host graph where it can be inserted.
     * The positions for a long edge are those that are located as close as possible to the median position of the adjacent vertices of the long edge
     *
     * @param longEdge      long edge
     * @param layers        boolean matrix of used host vertices
     * @param ins           instance
     * @param addedVertices set of added vertices
     * @param solution      solution
     * @return list of available positions
     */
    private ArrayList<Integer> getLongEdgePositionMEDIAN(GDInstance.Edge longEdge, boolean[][] layers, GDInstance ins, HashSet<Integer> addedVertices, GDSolution solution) {
        ArrayList<Integer> availablePositions = new ArrayList<>();
        int layerOfLongEdge = ins.getLayerOfVertex(longEdge.source());
        int size = ins.getListOfShortEdgesOfLongEdge(longEdge).size() + 1;
        ArrayList<Integer> positionsOfAdjacentVertices = new ArrayList<>();
        for (Integer v : ins.getSetOfAdjacentVerticesWithLongEdgesOfLongEdge(longEdge)) {
            if (addedVertices.contains(v)) {
                positionsOfAdjacentVertices.add(solution.getPositionOfInputVertex(v));
            }
        }
        if (positionsOfAdjacentVertices.size() == 0) {
            return getLongEdgePositionRANDOM(longEdge, layers, ins);
        }
        Collections.sort(positionsOfAdjacentVertices);
        int median = (int) Math.floor(positionsOfAdjacentVertices.size() / 2.0);
        int medianPosition = positionsOfAdjacentVertices.get(median);
        if (positionsOfAdjacentVertices.size() % 2 != 0) {
            if (isValidPosition(layers, layerOfLongEdge, medianPosition, size)) {
                availablePositions.add(medianPosition);
                return availablePositions;
            }
        } else {
            medianPosition = (medianPosition + positionsOfAdjacentVertices.get(median - 1)) / 2;
        }
        for (int pos = 0; pos < layers[layerOfLongEdge].length; pos++) {
            int a = medianPosition - pos - 1;
            int b = medianPosition + pos;
            if (a >= 0 && a < layers[layerOfLongEdge].length) {
                if (isValidPosition(layers, layerOfLongEdge, a, size)) {
                    availablePositions.add(a);
                }
            }
            if (b >= 0 && b < layers[layerOfLongEdge].length) {
                if (isValidPosition(layers, layerOfLongEdge, b, size)) {
                    availablePositions.add(b);
                }
            }
            if (availablePositions.size() > 0) {
                return availablePositions;
            }
        }
        return availablePositions;
    }

    /**
     * Given a long edge, return the positions of the host graph where it can be inserted.
     * The positions for a long edge are those that are located as close as possible to the median position of the adjacent vertices of the long edge
     * Uses tabu memory: if a position is already in the tabu memory, it is not considered
     *
     * @param longEdge      long edge
     * @param layers        boolean matrix of used host vertices
     * @param tabuMemory    tabu memory
     * @param ins           instance
     * @param addedVertices set of added vertices
     * @param solution      solution
     * @return list of available positions
     */
    private ArrayList<Integer> getLongEdgePositionMEDIAN_WITH_TABU(GDInstance.Edge longEdge, boolean[][] layers, TabuMemory tabuMemory, GDInstance ins, HashSet<Integer> addedVertices, GDSolution solution) {
        ArrayList<Integer> availablePositions = new ArrayList<>();
        int layerOfLongEdge = ins.getLayerOfVertex(longEdge.source());
        int size = ins.getListOfShortEdgesOfLongEdge(longEdge).size() + 1;
        ArrayList<Integer> positionsOfAdjacentVertices = new ArrayList<>();
        for (Integer v : ins.getSetOfAdjacentVerticesWithLongEdgesOfLongEdge(longEdge)) {
            if (addedVertices.contains(v)) {
                positionsOfAdjacentVertices.add(solution.getPositionOfInputVertex(v));
            }
        }
        if (positionsOfAdjacentVertices.size() == 0) {
            return getLongEdgePositionRANDOM(longEdge, layers, ins);
        }
        Collections.sort(positionsOfAdjacentVertices);
        int median = (int) Math.floor(positionsOfAdjacentVertices.size() / 2.0);
        int medianPosition = positionsOfAdjacentVertices.get(median);
        if (positionsOfAdjacentVertices.size() % 2 != 0) {
            if (isValidPosition(layers, layerOfLongEdge, medianPosition, size)) {
                availablePositions.add(medianPosition);
                return availablePositions;
            }
        } else {
            medianPosition = (medianPosition + positionsOfAdjacentVertices.get(median - 1)) / 2;
        }
        for (int pos = 0; pos < layers[layerOfLongEdge].length; pos++) {
            int a = medianPosition - pos - 1;
            int b = medianPosition + pos;
            if (!tabuMemory.isInTabuList(longEdge, a)) {
                if (a >= 0 && a < layers[layerOfLongEdge].length) {
                    if (isValidPosition(layers, layerOfLongEdge, a, size)) {
                        availablePositions.add(a);
                    }
                }
            }
            if (!tabuMemory.isInTabuList(longEdge, b)) {
                if (b >= 0 && b < layers[layerOfLongEdge].length) {
                    if (isValidPosition(layers, layerOfLongEdge, b, size)) {
                        availablePositions.add(b);
                    }
                }
            }
            if (availablePositions.size() > 0) {
                return availablePositions;
            }
        }
        return availablePositions;
    }

    private void addVertex(GDSolution s, boolean[][] used, int pos, int v, HashSet<Integer> addedVertices) {
        s.setVertexPosition(v, s.getLayerOfInputVertex(v), pos);
        used[s.getLayerOfInputVertex(v)][pos] = true;
        addedVertices.add(v);
    }

    /**
     * Add a long edge to the solution
     *
     * @param s             solution
     * @param used          matrix of used positions
     * @param pos           position to add a long edge
     * @param longEdge      long edge to add
     * @param addedVertices list of added vertices
     * @param addedEdges
     */
    private void addLongEdge(GDSolution s, boolean[][] used, int pos, GDInstance.Edge longEdge, HashSet<Integer> addedVertices, HashMap<Integer, ArrayList<GDInstance.Edge>> addedEdges) {
        var u = longEdge.source();
        s.setVertexPosition(u, s.getLayerOfInputVertex(u), pos);
        used[s.getLayerOfInputVertex(u)][pos] = true;
        addedVertices.add(u);
        var shortEdges = s.getInstance().getListOfShortEdgesOfLongEdge(longEdge);
        for (GDInstance.Edge edge : shortEdges) {
            var v = edge.target();
            s.setVertexPosition(v, s.getLayerOfInputVertex(v), pos);
            used[s.getLayerOfInputVertex(v)][pos] = true;
            addedVertices.add(v);
            addedEdges.get(s.getLayerOfInputVertex(edge.source())).add(edge);
        }
    }

    /**
     * Add long edges to the solution
     *
     * @param solution   solution to be constructed
     * @param ins        instance
     * @param used       used vertices
     * @param addedEdges
     * @return true if a new solution is constructed, false otherwise
     */
    private boolean addRemainingVertices(GDSolution solution, GDInstance ins, boolean[][] used, HashSet<Integer> addedVertices, HashMap<Integer, ArrayList<GDInstance.Edge>> addedEdges) {
        var listOfReamingVertices = ins.getListOfRealVertices().stream().filter(vertex -> !addedVertices.contains(vertex)).collect(Collectors.toCollection(() -> new ArrayList<>(ins.TotalNumVertices())));
        HashMap<Integer, Double> mcallister = new HashMap<>(listOfReamingVertices.size());
        sortListOfRemainingVertices(listOfReamingVertices, ins, mcallister, addedVertices);
        while (!listOfReamingVertices.isEmpty()) {
            int v = listOfReamingVertices.remove(0);
            ArrayList<Integer> availablePositions = getBestPositionOfVertex(solution, used, v, ins, addedVertices, addedEdges);
            if (availablePositions.isEmpty()) {
                return false;
            } else {
                int pos = CollectionUtil.pickRandom(availablePositions);
                addVertex(solution, used, pos, v, addedVertices);
                if (this.selectionCriteria == InputVertexSelectionCriteria.MCALLISTER) {
                    updateListByMcallister(listOfReamingVertices, v, mcallister, ins);
                }
            }
        }
        return true;
    }

    private void sortListOfRemainingVertices(ArrayList<Integer> listOfReamingVertices, GDInstance ins, HashMap<Integer, Double> mcallister, HashSet<Integer> addedVertices) {
        if (listOfReamingVertices.size() > 1) {
            if (this.selectionCriteria == InputVertexSelectionCriteria.RANDOM) {
                CollectionUtil.shuffle(listOfReamingVertices);
            } else if (this.selectionCriteria == InputVertexSelectionCriteria.MCALLISTER) {
                initMcallister(listOfReamingVertices, ins, mcallister, addedVertices);
                listOfReamingVertices.sort(Comparator.comparingDouble(mcallister::get).reversed());
            } else {
                throw new IllegalArgumentException("Unknown input vertex selection criteria");
            }
        }
    }

    /**
     * Initialize mcallister map
     *
     * @param listOfVertices list of reaming vertices
     * @param ins            instance
     * @param mcallister     mcallister map
     */
    private void initMcallister(ArrayList<Integer> listOfVertices, GDInstance ins, HashMap<Integer, Double> mcallister, HashSet<Integer> addedVertices) {
        for (Integer vertex : listOfVertices) {
            double value = 0;
            for (Integer adjacent : ins.getAdjacentShortVertexListOfVertex(vertex, GDInstance.TYPE_OF_ADJACENT_VERTEX.ALL)) {
                if (addedVertices.contains(adjacent)) {
                    value += SE_w1;
                } else value -= SE_w2;
            }
            mcallister.put(vertex, value);
        }
    }

    /**
     * Update mcallister map
     *
     * @param listOfVertices list of reaming vertices
     * @param previousVertex previous vertex
     * @param mcallister     mcallister map
     * @param ins            instance
     */
    private void updateListByMcallister(ArrayList<Integer> listOfVertices, int previousVertex, HashMap<Integer, Double> mcallister, GDInstance ins) {
        mcallister.remove(previousVertex);
        for (Map.Entry<Integer, Double> vertexValue : mcallister.entrySet()) {
            if (ins.getAdjacentShortVertexListOfVertex(vertexValue.getKey(), GDInstance.TYPE_OF_ADJACENT_VERTEX.ALL).contains(previousVertex)) {
                vertexValue.setValue(vertexValue.getValue() + 1);
            }
        }
        listOfVertices.sort(Comparator.comparingDouble(mcallister::get).reversed());
    }

    private ArrayList<Integer> getBestPositionOfVertex(GDSolution solution, boolean[][] used, int v, GDInstance ins, HashSet<Integer> addedVertices, HashMap<Integer, ArrayList<GDInstance.Edge>> addedEdges) {
        if (this.hostVertexCriteria == HostVertexCriteria.RANDOM) {
            return getPositionsRandom(used, v, ins);
        } else if (this.hostVertexCriteria == HostVertexCriteria.CROSSINGS) {
            return getPositionsCrossings(solution, used, v, ins, addedVertices, addedEdges);
        } else if (this.hostVertexCriteria == HostVertexCriteria.MEDIAN) {
            return getPositionsMedian(solution, used, v, ins, addedVertices);
        } else if (this.hostVertexCriteria == HostVertexCriteria.CROSSINGS_MEDIAN) {
            return getPositionsCrossingsMedian(solution, used, v, ins, addedVertices, addedEdges);
        } else {
            throw new IllegalArgumentException("Unknown host vertex selection criteria");
        }
    }

    private ArrayList<Integer> getPositionsCrossingsMedian(GDSolution solution, boolean[][] used, int v, GDInstance ins, HashSet<Integer> addedVertices, HashMap<Integer, ArrayList<GDInstance.Edge>> addedEdges) {
        HashSet<Integer> positions = new HashSet<>(this.getPositionsCrossings(solution, used, v, ins, addedVertices, addedEdges));
        return this.getPositionsMedian(solution, used, v, ins, addedVertices, positions);
    }

    private ArrayList<Integer> getPositionsMedian(GDSolution solution, boolean[][] layers, int v, GDInstance ins, HashSet<Integer> addedVertices, HashSet<Integer> positionsCrossings) {
        ArrayList<Integer> positions = new ArrayList<>();
        int layer = ins.getLayerOfVertex(v);
        ArrayList<Integer> positionsOfAdjacentVertices = new ArrayList<>();
        for (Integer u : ins.getAdjacentShortVertexListOfVertex(v, GDInstance.TYPE_OF_ADJACENT_VERTEX.ALL)) {
            if (addedVertices.contains(u)) {
                positionsOfAdjacentVertices.add(solution.getPositionOfInputVertex(u));
            }
        }
        if (positionsOfAdjacentVertices.size() == 0) {
            return getPositionsRandom(layers, v, ins);
        }
        Collections.sort(positionsOfAdjacentVertices);
        int median = (int) Math.floor(positionsOfAdjacentVertices.size() / 2.0);
        int medianPosition = positionsOfAdjacentVertices.get(median);
        if (positionsOfAdjacentVertices.size() % 2 != 0) {
            if (positionsCrossings.contains(medianPosition)) {
                positions.add(medianPosition);
                return positions;
            }
        } else {
            medianPosition = (medianPosition + positionsOfAdjacentVertices.get(median - 1)) / 2;
        }
        for (int pos = 0; pos < layers[layer].length; pos++) {
            int a = medianPosition - pos - 1;
            int b = medianPosition + pos;
            if (positionsCrossings.contains(a)) {
                positions.add(a);
            }
            if (positionsCrossings.contains(b)) {
                positions.add(b);
            }
            if (positions.size() > 0) {
                return positions;
            }
        }
        return positions;
    }

    private ArrayList<Integer> getPositionsMedian(GDSolution solution, boolean[][] layers, int v, GDInstance ins, HashSet<Integer> addedVertices) {
        ArrayList<Integer> positions = new ArrayList<>();
        int layer = ins.getLayerOfVertex(v);
        ArrayList<Integer> positionsOfAdjacentVertices = new ArrayList<>();
        for (Integer u : ins.getAdjacentShortVertexListOfVertex(v, GDInstance.TYPE_OF_ADJACENT_VERTEX.ALL)) {
            if (addedVertices.contains(u)) {
                positionsOfAdjacentVertices.add(solution.getPositionOfInputVertex(u));
            }
        }
        if (positionsOfAdjacentVertices.size() == 0) {
            return getPositionsRandom(layers, v, ins);
        }
        Collections.sort(positionsOfAdjacentVertices);
        int median = (int) Math.floor(positionsOfAdjacentVertices.size() / 2.0);
        int medianPosition = positionsOfAdjacentVertices.get(median);
        if (positionsOfAdjacentVertices.size() % 2 != 0) {
            if (!layers[layer][medianPosition]) {
                positions.add(medianPosition);
                return positions;
            }
        } else {
            medianPosition = (medianPosition + positionsOfAdjacentVertices.get(median - 1)) / 2;
        }
        for (int pos = 0; pos < layers[layer].length; pos++) {
            int a = medianPosition - pos - 1;
            int b = medianPosition + pos;
            if (a >= 0 && a < layers[layer].length) {
                if (!layers[layer][a]) {
                    positions.add(a);
                }
            }
            if (b >= 0 && b < layers[layer].length) {
                if (!layers[layer][b]) {
                    positions.add(b);
                }
            }
            if (positions.size() > 0) {
                return positions;
            }
        }
        return positions;
    }

    private ArrayList<Integer> getPositionsCrossings(GDSolution solution, boolean[][] used, int v, GDInstance ins, HashSet<Integer> addedVertices, HashMap<Integer, ArrayList<GDInstance.Edge>> addedEdges) {
        ArrayList<Integer> positions = new ArrayList<>();
        ArrayList<GDInstance.Edge> edgesToAdd = new ArrayList<>();
        int MinCuts = Integer.MAX_VALUE;
        for (Integer adjV : ins.getAdjacentShortVertexListOfVertex(v, GDInstance.TYPE_OF_ADJACENT_VERTEX.SOURCE)) {
            if (addedVertices.contains(adjV)) {
                edgesToAdd.add(new GDInstance.Edge(v, adjV));
            }
        }
        for (Integer adjV : ins.getAdjacentShortVertexListOfVertex(v, GDInstance.TYPE_OF_ADJACENT_VERTEX.TARGET)) {
            if (addedVertices.contains(adjV)) {
                edgesToAdd.add(new GDInstance.Edge(adjV, v));
            }
        }
        var layer = ins.getLayerOfVertex(v);
        var edgesToCalculateCrossings = addedEdges.get(layer);
        if (layer - 1 >= 0) {
            edgesToCalculateCrossings.addAll(addedEdges.get(layer - 1));
        }
        for (int i = 0; i < used[layer].length; i++) {
            if (!used[layer][i]) {
                int cuts = calculateCrossings(solution, v, i, edgesToAdd, edgesToCalculateCrossings);
                if (cuts < MinCuts) {
                    MinCuts = cuts;
                    positions = new ArrayList<>();
                    positions.add(i);
                } else if (cuts == MinCuts) {
                    positions.add(i);
                }
            }
        }
        for (GDInstance.Edge edge : edgesToAdd) {
            var layerOfVertex = ins.getLayerOfVertex(edge.source());
            addedEdges.get(layerOfVertex).add(edge);
        }
        return positions;
    }

    private int calculateCrossings(GDSolution solution, int v, int posV, ArrayList<GDInstance.Edge> edgesToAdd, ArrayList<GDInstance.Edge> edgesToCalculateCrossings) {
        int cuts = 0;
        for (GDInstance.Edge edge : edgesToAdd) {
            for (GDInstance.Edge otherEdge : edgesToCalculateCrossings) {
                if (solution.getInstance().getLayerOfVertex(edge.source()) == solution.getInstance().getLayerOfVertex(otherEdge.source())) {
                    if (isCut(edge, otherEdge, v, posV, solution)) {
                        cuts++;
                    }
                }
            }
        }
        return cuts;
    }

    /**
     * Check if there is a cut between two edges (u,v) and (w,z)
     *
     * @param edge      (u,v)
     * @param otherEdge (w,z)
     * @return true if there is a cut between edges (u,v) and (w,z)
     */
    private boolean isCut(GDInstance.Edge edge, GDInstance.Edge otherEdge, int vertex, int possiblePos, GDSolution solution) {
        var posU = (edge.source() == vertex) ? possiblePos : solution.getPositionOfInputVertex(edge.source());
        var posV = (edge.target() == vertex) ? possiblePos : solution.getPositionOfInputVertex(edge.target());
        var posW = solution.getPositionOfInputVertex(otherEdge.source());
        var posZ = solution.getPositionOfInputVertex(otherEdge.target());
        // check if u and w are the same and check if v and z are the same
        // if so, there is no cut
        if (posU == posW) return false;
        if (posV == posZ) return false;
        // if posU < posW and posV < posZ, then there is not a cut
        // or if posU > posW and posV > posZ, then there is not a cut
        // or if posU < posW and posV > posZ, then there is a cut
        // or if posU > posW and posV < posZ, then there is a cut
        if (posU < posW && posV < posZ) {
            return false;
        } else return posU <= posW || posV <= posZ;
    }

    private ArrayList<Integer> getPositionsRandom(boolean[][] used, int v, GDInstance ins) {
        ArrayList<Integer> positions = new ArrayList<>();
        var layer = ins.getLayerOfVertex(v);
        for (int i = 0; i < used[layer].length; i++) {
            if (!used[layer][i]) {
                positions.add(i);
            }
        }
        return positions;
    }

    /**
     * Checks if the position is valid to add a long edge
     *
     * @param used  boolean matrix of host vertices
     * @param layer starting layer of the long edge
     * @param pos   position to be checked
     * @param size  size of the long edge
     * @return true if the position is valid
     */
    private boolean isValidPosition(boolean[][] used, int layer, int pos, int size) {
        if (size == 0) return true;
        else if (used[layer][pos]) return false;
        else return isValidPosition(used, layer + 1, pos, size - 1);
    }

    @Override
    public String toString() {
        return "C{" + "SE_w1=" + SE_w1 + "-SE_w2=" + SE_w2 + "-LE_w1=" + LE_w1 + "-LE_w2=" + LE_w2 + "-it=" + iterations + "-tS=" + tabuSize + "-lES=" + longEdgeSelectionCriteria + "-lEL=" + longEdgeLocationCriteria + "-vS=" + selectionCriteria + "-vL=" + hostVertexCriteria + '}';
    }

    public enum InputVertexSelectionCriteria {
        RANDOM, // Select a random long edge
        MCALLISTER // Select the long edge with more adjacent long edges added
    }

    public enum HostVertexCriteria {
        RANDOM, // RANDOM
        CROSSINGS, // Minimize the number of crossings of the edges of the vertex to be added
        MEDIAN, // Locate a vertex in the median of its adjacent vertices
        CROSSINGS_MEDIAN, // Locate a vertex in the position that minimize the number of crossings. If there is more than one available position, it is set in the median. in the median of its adjacent vertices
    }


    public enum LongEdgeSelectionCriteria {
        RANDOM, // Select a random long edge
        LONGEST_FIRST, // Select the longest long edge
        SHORTEST_FIRST, // Select the shortest long edge
        MCALLISTER // Select the long edge with more adjacent long edges added
    }

    /**
     * Determine where to place a long edge
     */
    public enum LongEdgeLocationCriteria {
        RANDOM, // RANDOM
        MEDIAN, // Locate a vertex in the median of its adjacent vertices
        RANDOM_WITH_TABU, // RANDOM + TABU
        MEDIAN_WITH_TABU // DISTANCE + TABU
    }

    /**
     * Tabu memory
     */
    private static class TabuMemory {

        /**
         * Size of the tabu list
         */
        final int size;
        /**
         * The tabu list.
         */
        private final HashMap<GDInstance.Edge, LinkedList<Integer>> tabuList;
        /**
         * The tabu set
         */
        private final HashMap<GDInstance.Edge, HashSet<Integer>> inTabuList;

        public TabuMemory(int size, GDInstance instance) {
            this.size = size;
            tabuList = new HashMap<>(instance.NumRealLongEdges());
            inTabuList = new HashMap<>(instance.NumRealLongEdges());
            initializeTabuList(instance);
        }

        /**
         * Initializes the tabu list with the edges of the instance.
         */
        private void initializeTabuList(GDInstance instance) {
            for (GDInstance.Edge edge : instance.getListOfLongEdges()) {
                tabuList.put(edge, new LinkedList<>());
                inTabuList.put(edge, new HashSet<>(this.size));
            }
        }

        /**
         * Adds an edge to the tabu list.
         */
        public void addToTabuList(GDInstance.Edge edge, int pos) {
            tabuList.get(edge).add(pos);
            inTabuList.get(edge).add(pos);
        }


        /**
         * Checks whether an edge is in the tabu list.
         */
        public boolean isInTabuList(GDInstance.Edge edge, int pos) {
            return inTabuList.get(edge).contains(pos);
        }

        /**
         * Removes an edge from the tabu list.
         */
        public void removeFromTabuList(GDInstance.Edge edge) {
            if (inTabuList.get(edge).size() > size) {
                var pos = tabuList.get(edge).remove();
                inTabuList.get(edge).remove(pos);
            }
        }

        /**
         * Updates the tabu list.
         *
         * @param edge the edge to update
         * @param pos  the position to update
         */
        public void updateTabuList(GDInstance.Edge edge, int pos) {
            addToTabuList(edge, pos);
            removeFromTabuList(edge);
        }

    }

}

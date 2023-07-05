package es.urjc.etsii.grafo.GD.constructives;

import es.urjc.etsii.grafo.GD.model.GDInstance;
import es.urjc.etsii.grafo.GD.model.GDSolution;
import es.urjc.etsii.grafo.create.Constructive;
import es.urjc.etsii.grafo.util.CollectionUtil;

import java.util.ArrayList;

public class GDRandomConstructive extends Constructive<GDSolution, GDInstance> {

    @Override
    public GDSolution construct(GDSolution solution) {
        var unfeasible = true;
        var aux = new GDSolution(solution);
        while (unfeasible) {
            solution = aux.cloneSolution();
            unfeasible = constructSolution(solution);
        }
        solution.getScore();
        solution.updateLastModifiedTime();
        return solution;
    }

    private boolean constructSolution(GDSolution solution) {
        GDInstance ins = solution.getInstance();
        boolean[][] used = new boolean[ins.NumberOfLayers()][ins.NumVerticesInLayer()];
        if (addLongEdges(solution, ins, used)) return true;
        return addShortEdges(solution, solution, ins, used);
    }

    private boolean addShortEdges(GDSolution s, GDSolution aux, GDInstance ins, boolean[][] used) {
        var realVerticesSet = s.getInstance().getListOfRealVertices();
        ArrayList<Integer> randomListOfRealVertices = new ArrayList<>(realVerticesSet);
        CollectionUtil.shuffle(randomListOfRealVertices);
        for (int v : randomListOfRealVertices) {
            if (!s.getInstance().hasLongEdge(v)) {
                var layer = s.getLayerOfInputVertex(v);
                var availablePosition = getAvailablePosition(used, layer);
                if (availablePosition != -1) {
                    addShortEdge(aux, used, availablePosition, v);
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    private void addShortEdge(GDSolution s, boolean[][] used, int pos, int v) {
        s.setVertexPosition(v, s.getLayerOfInputVertex(v), pos);
        used[s.getLayerOfInputVertex(v)][pos] = true;
    }

    private int getAvailablePosition(boolean[][] used, int layer) {
        for (int i = 0; i < used[layer].length; i++) {
            if (!used[layer][i]) return i;
        }
        return -1;
    }

    private boolean addLongEdges(GDSolution solution, GDInstance ins, boolean[][] used) {
        var listOfLongEdges = solution.getInstance().getListOfLongEdges();
        for (GDInstance.Edge longEdge : listOfLongEdges) {
            int u = longEdge.source();
            int layerU = ins.getLayerOfVertex(u);
            int size = ins.getListOfShortEdgesOfLongEdge(longEdge).size() + 1;
            ArrayList<Integer> availablePositions = getAvailablePositions(used, layerU, size);
            if (availablePositions.size() == 0) return true;
            int pos = CollectionUtil.pickRandom(availablePositions);
            addLongEdge(solution, used, pos, longEdge);
        }
        return false;
    }

    private void addLongEdge(GDSolution s, boolean[][] used, int pos, GDInstance.Edge longEdge) {
        var u = longEdge.source();
        s.setVertexPosition(u, s.getLayerOfInputVertex(u), pos);
        used[s.getLayerOfInputVertex(u)][pos] = true;
        var shortEdges = s.getInstance().getListOfShortEdgesOfLongEdge(longEdge);
        for (GDInstance.Edge edge : shortEdges) {
            var v = edge.target();
            s.setVertexPosition(v, s.getLayerOfInputVertex(v), pos);
            used[s.getLayerOfInputVertex(v)][pos] = true;
        }
    }

    private ArrayList<Integer> getAvailablePositions(boolean[][] layers, int layerU, int size) {
        ArrayList<Integer> availablePositions = new ArrayList<>();
        for (int pos = 0; pos < layers[layerU].length; pos++) {
            if (isValidPosition(layers, layerU, pos, size)) {
                availablePositions.add(pos);
            }
        }
        return availablePositions;
    }

    private boolean isValidPosition(boolean[][] layers, int layer, int pos, int size) {
        if (size == 0) return true;
        else if (layers[layer][pos]) return false;
        else return isValidPosition(layers, layer + 1, pos, size - 1);
    }

    @Override
    public String toString() {
        return "Random";
    }
}

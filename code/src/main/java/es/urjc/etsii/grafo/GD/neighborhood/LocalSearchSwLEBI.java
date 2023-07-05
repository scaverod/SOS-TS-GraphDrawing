package es.urjc.etsii.grafo.GD.neighborhood;

import es.urjc.etsii.grafo.GD.model.GDInstance;
import es.urjc.etsii.grafo.GD.model.GDSolution;
import es.urjc.etsii.grafo.improve.Improver;

import java.util.ArrayList;

public class LocalSearchSwLEBI extends Improver<GDSolution, GDInstance> {
    @Override
    protected GDSolution _improve(GDSolution solution) {
        var improved = true;
        var bestSolution = solution.cloneSolution();
        while (improved) {
            improved = false;
            var ins = solution.getInstance();
            var bestSolutionTemp = bestSolution.cloneSolution();
            for (GDInstance.Edge longEdge : ins.getListOfLongEdges()) {
                var listOfAvailablePositions = getAvailablePositions(bestSolution, longEdge);
                for (Integer position : listOfAvailablePositions) {
                    solution = bestSolution.cloneSolution();
                    solution.swapLongEdge(longEdge, position);
                    if (solution.isBetterThan(bestSolution)) {
                        bestSolutionTemp = solution;
                        improved = true;
                    }
                }
            }
            if (improved) {
                bestSolution = bestSolutionTemp;
            }
        }
        return bestSolution;
    }

    private ArrayList<Integer> getAvailablePositions(GDSolution solution, GDInstance.Edge longEdge) {
        ArrayList<Integer> availablePositions = new ArrayList<>();
        var ins = solution.getInstance();
        var size = ins.getListOfShortEdgesOfLongEdge(longEdge).size();
        var layer = ins.getLayerOfVertex(longEdge.source());
        var numVerticesInLayer = ins.NumVerticesInLayer();
        for (int i = 0; i < numVerticesInLayer; i++) {
            if (i != solution.getPositionOfInputVertex(longEdge.source())) {
                if (isValidPosition(solution, layer, size, i)) {
                    availablePositions.add(i);
                }
            }
        }
        return availablePositions;
    }

    private boolean isValidPosition(GDSolution solution, int layer, int size, int position) {
        var ins = solution.getInstance();
        if (size == -1) return true;
        var vertex = solution.getInputVertexAssignedTo(layer + size, position);
        if (ins.isDummy(vertex) || ins.hasLongEdge(vertex)) return false;
        else return isValidPosition(solution, layer, size - 1, position);
    }
}

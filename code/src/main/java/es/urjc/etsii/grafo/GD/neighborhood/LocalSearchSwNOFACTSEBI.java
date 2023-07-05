package es.urjc.etsii.grafo.GD.neighborhood;

import es.urjc.etsii.grafo.GD.model.GDInstance;
import es.urjc.etsii.grafo.GD.model.GDSolution;
import es.urjc.etsii.grafo.improve.Improver;

public class LocalSearchSwNOFACTSEBI extends Improver<GDSolution, GDInstance> {
    @Override
    protected GDSolution _improve(GDSolution solution) {
        var improved = true;
        var bestSolution = solution.cloneSolution();
        while (improved) {
            improved = false;
            var ins = solution.getInstance();
            var bestSolutionTemp = bestSolution.cloneSolution();
            for (int h = 0; h < ins.NumberOfLayers(); h++) {
                var listOfVerticesInLayer = ins.getListOfVerticesInLayer(h);
                for (int i = 0; i < listOfVerticesInLayer.size(); i++) {
                    var u = listOfVerticesInLayer.get(i);
                    if (!ins.isDummy(u) && !ins.hasLongEdge(u)) {
                        for (int j = i + 1; j < listOfVerticesInLayer.size(); j++) {
                            var v = listOfVerticesInLayer.get(j);
                            if (!ins.isDummy(v) && !ins.hasLongEdge(v)) {
                                solution = bestSolution.cloneSolution();
                                solution.swap(u, v);
                                if (solution.isBetterThan(bestSolutionTemp)) {
                                    bestSolutionTemp = solution.cloneSolution();
                                    improved = true;
                                }
                            }
                        }
                    }
                }
            }
            if (improved) {
                bestSolution = bestSolutionTemp;
            }
        }
        return bestSolution;
    }
}

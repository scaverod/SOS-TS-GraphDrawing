package es.urjc.etsii.grafo.GD.neighborhood;

import es.urjc.etsii.grafo.GD.model.GDInstance;
import es.urjc.etsii.grafo.GD.model.GDSolution;
import es.urjc.etsii.grafo.improve.Improver;

public class LocalSearchSwSEBI extends Improver<GDSolution, GDInstance> {
    @Override
    protected GDSolution _improve(GDSolution solution) {
        var improved = true;
        while (improved) {
            improved = false;
            var ins = solution.getInstance();
            var bestU = -1;
            var bestV = -1;
            var bestVariation = 0;
            for (int h = 0; h < ins.NumberOfLayers(); h++) {
                var listOfVerticesInLayer = ins.getListOfVerticesInLayer(h);
                for (int i = 0; i < listOfVerticesInLayer.size(); i++) {
                    var u = listOfVerticesInLayer.get(i);
                    if (!ins.isDummy(u) && !ins.hasLongEdge(u)) {
                        for (int j = i + 1; j < listOfVerticesInLayer.size(); j++) {
                            var v = listOfVerticesInLayer.get(j);
                            if (!ins.isDummy(v) && !ins.hasLongEdge(v)) {
                                var variation = solution.getVariation(u, v);
                                if (variation < bestVariation) {
                                    bestU = u;
                                    bestV = v;
                                    bestVariation = variation;
                                    improved = true;
                                }
                            }
                        }
                    }
                }
            }
            if (improved) {
                solution.swap(bestU, bestV);
            }
        }
        return solution;
    }
}

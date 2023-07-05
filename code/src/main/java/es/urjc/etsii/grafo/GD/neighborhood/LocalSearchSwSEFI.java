package es.urjc.etsii.grafo.GD.neighborhood;

import es.urjc.etsii.grafo.GD.model.GDInstance;
import es.urjc.etsii.grafo.GD.model.GDSolution;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.util.random.RandomManager;

public class LocalSearchSwSEFI extends Improver<GDSolution, GDInstance> {
    @Override
    protected GDSolution _improve(GDSolution solution) {
        var improved = true;
        while (improved) {
            improved = false;
            var ins = solution.getInstance();
            var bestU = -1;
            var bestV = -1;
            var bestVariation = 0;
            int h = 0;
            int initial = RandomManager.getRandom().nextInt(ins.NumberOfLayers());
            while (h < ins.NumberOfLayers() && !improved) {
                var l = (h + initial) % ins.NumberOfLayers();
                var listOfVerticesInLayer = ins.getListOfVerticesInLayer(l);
                int i = 0;
                while (i < listOfVerticesInLayer.size() && !improved) {
                    var u = listOfVerticesInLayer.get(i);
                    int j = i + 1;
                    if (!ins.isDummy(u) && !ins.hasLongEdge(u)) {
                        while (j < listOfVerticesInLayer.size() && !improved) {
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
                            j++;
                        }
                    }
                    i++;
                }
                h++;
            }
            if (improved) {
                solution.fastSwap(bestU, bestV);
            }
        }
        return solution;
    }
}

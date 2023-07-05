package es.urjc.etsii.grafo.GD.experiments;

import es.urjc.etsii.grafo.GD.constructives.GDConstructive;
import es.urjc.etsii.grafo.GD.model.GDInstance;
import es.urjc.etsii.grafo.GD.model.GDSolution;
import es.urjc.etsii.grafo.GD.neighborhood.LocalSearchSwSEFI;
import es.urjc.etsii.grafo.GD.neighborhood.LocalSearchSwLEBI;
import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.algorithms.SimpleAlgorithm;
import es.urjc.etsii.grafo.solver.SolverConfig;
import es.urjc.etsii.grafo.solver.services.AbstractExperiment;

import java.util.ArrayList;
import java.util.List;

public class LocalSearchExperiment extends AbstractExperiment<GDSolution, GDInstance> {

    /**
     * Initialize common fields for all experiments
     *
     * @param solverConfig solver configuration, see the application.yml file for more details
     */
    protected LocalSearchExperiment(SolverConfig solverConfig) {
        super(solverConfig);
    }

    @Override
    public List<Algorithm<GDSolution, GDInstance>> getAlgorithms() {
        var algorithms = new ArrayList<Algorithm<GDSolution, GDInstance>>();

        var LE_w1 = 0.76;
        var SE_w1 = 0.64;
        var iterations = 1000;
        var tabuSize = 0.5;
        var selectionCriteria = GDConstructive.InputVertexSelectionCriteria.MCALLISTER;
        var hostVertexCriteria = GDConstructive.HostVertexCriteria.CROSSINGS_MEDIAN;
        var longEdgeSelectionCriteria = GDConstructive.LongEdgeSelectionCriteria.MCALLISTER;
        var longEdgeLocationCriteria = GDConstructive.LongEdgeLocationCriteria.MEDIAN_WITH_TABU;

        var constructive = new GDConstructive(SE_w1, 1 - SE_w1, LE_w1, 1 - LE_w1, iterations, tabuSize, selectionCriteria, hostVertexCriteria, longEdgeSelectionCriteria, longEdgeLocationCriteria);
        var ls = new LocalSearchSwLEBI();
        var ls2 = new LocalSearchSwSEFI();
        algorithms.add(new SimpleAlgorithm<>(constructive, ls, ls2, ls, ls2));

        return algorithms;
    }
}

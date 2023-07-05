package es.urjc.etsii.grafo.GD.experiments;

import es.urjc.etsii.grafo.GD.constructives.GDConstructive;
import es.urjc.etsii.grafo.GD.constructives.GDConstructiveEvent;
import es.urjc.etsii.grafo.GD.constructives.GDRandomConstructive;
import es.urjc.etsii.grafo.GD.model.GDInstance;
import es.urjc.etsii.grafo.GD.model.GDSolution;
import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.algorithms.SimpleAlgorithm;
import es.urjc.etsii.grafo.solver.SolverConfig;
import es.urjc.etsii.grafo.solver.services.AbstractExperiment;

import java.util.ArrayList;
import java.util.List;

public class ConstructiveExperiment extends AbstractExperiment<GDSolution, GDInstance> {

    public ConstructiveExperiment(SolverConfig solverConfig) {
        super(solverConfig);
    }

    @Override
    public List<Algorithm<GDSolution, GDInstance>> getAlgorithms() {
        // In this experiment we will compare a random constructive with several GRASP constructive configurations

        var algorithms = new ArrayList<Algorithm<GDSolution, GDInstance>>();


        var LE_w1 = 0.78;
        var SE_w1 = 0.43;
        var iterations = 1817;
        var tabuSize = 0.21;
        var selectionCriteria = GDConstructive.InputVertexSelectionCriteria.MCALLISTER;
        var hostVertexCriteria = GDConstructive.HostVertexCriteria.MEDIAN;
        var longEdgeSelectionCriteria = GDConstructive.LongEdgeSelectionCriteria.MCALLISTER;
        var longEdgeLocationCriteriaMedian = GDConstructive.LongEdgeLocationCriteria.MEDIAN;
        var longEdgeLocationCriteriaMedianT = GDConstructive.LongEdgeLocationCriteria.MEDIAN_WITH_TABU;

        var constructive = new GDConstructive(SE_w1, 1 - SE_w1, LE_w1, 1 - LE_w1, iterations, tabuSize, selectionCriteria, hostVertexCriteria, longEdgeSelectionCriteria, longEdgeLocationCriteriaMedian);
        var constructive2 = new GDConstructive(SE_w1, 1 - SE_w1, LE_w1, 1 - LE_w1, iterations, tabuSize, selectionCriteria, hostVertexCriteria, longEdgeSelectionCriteria, longEdgeLocationCriteriaMedianT);


        algorithms.add(new SimpleAlgorithm<>("normal", constructive));
        algorithms.add(new SimpleAlgorithm<>("TS", constructive2));

        return algorithms;
    }
}

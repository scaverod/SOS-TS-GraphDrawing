package es.urjc.etsii.grafo.GD.experiments;

import es.urjc.etsii.grafo.GD.constructives.GDConstructive;
import es.urjc.etsii.grafo.GD.model.GDInstance;
import es.urjc.etsii.grafo.GD.model.GDSolution;
import es.urjc.etsii.grafo.GD.neighborhood.LocalSearchSwLEFI;
import es.urjc.etsii.grafo.GD.neighborhood.LocalSearchSwSEBI;
import es.urjc.etsii.grafo.GD.neighborhood.LocalSearchSwSEFI;
import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.algorithms.SimpleAlgorithm;
import es.urjc.etsii.grafo.algorithms.multistart.MultiStartAlgorithm;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.improve.VND;
import es.urjc.etsii.grafo.solver.SolverConfig;
import es.urjc.etsii.grafo.solver.services.AbstractExperiment;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MultistartVNDExperiment extends AbstractExperiment<GDSolution, GDInstance> {

    /**
     * Initialize common fields for all experiments
     *
     * @param solverConfig solver configuration, see the application.yml file for more details
     */
    protected MultistartVNDExperiment(SolverConfig solverConfig) {
        super(solverConfig);
    }

    @Override
    public List<Algorithm<GDSolution, GDInstance>> getAlgorithms() {
        var algorithms = new ArrayList<Algorithm<GDSolution, GDInstance>>();

        var LE_w1 = 0.78;
        var SE_w1 = 0.43;
        var iterations = 1817;
        var tabuSize = 0.21;
        var selectionCriteria = GDConstructive.InputVertexSelectionCriteria.MCALLISTER;
        var hostVertexCriteria = GDConstructive.HostVertexCriteria.MEDIAN;
        var longEdgeSelectionCriteria = GDConstructive.LongEdgeSelectionCriteria.MCALLISTER;
        var longEdgeLocationCriteria = GDConstructive.LongEdgeLocationCriteria.MEDIAN_WITH_TABU;

        var constructive = new GDConstructive(SE_w1, 1 - SE_w1, LE_w1, 1 - LE_w1, iterations, tabuSize, selectionCriteria, hostVertexCriteria, longEdgeSelectionCriteria, longEdgeLocationCriteria);
        var lsLE = new LocalSearchSwLEFI();
        var lsSE = new LocalSearchSwSEFI();
        ArrayList<Improver<GDSolution, GDInstance>> list = new ArrayList<>(2);
        list.add(lsLE);
        list.add(lsSE);
        var vnd = new VND<>(list, false);
        SimpleAlgorithm<GDSolution, GDInstance> simpleAlg = new SimpleAlgorithm<>(constructive, vnd);
        MultiStartAlgorithm<GDSolution, GDInstance> multistart = MultiStartAlgorithm.<GDSolution, GDInstance>builder().withTime(60, TimeUnit.SECONDS).withMaxIterationsWithoutImproving(100).build(simpleAlg);

        algorithms.add(multistart);


        return algorithms;
    }
}

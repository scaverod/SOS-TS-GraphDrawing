package es.urjc.etsii.grafo.GD.experiments;

import es.urjc.etsii.grafo.GD.constructives.GDConstructive;
import es.urjc.etsii.grafo.GD.constructives.GDRandomConstructive;
import es.urjc.etsii.grafo.GD.model.GDInstance;
import es.urjc.etsii.grafo.GD.model.GDSolution;
import es.urjc.etsii.grafo.GD.neighborhood.LocalSearchSwLEBI;
import es.urjc.etsii.grafo.GD.neighborhood.LocalSearchSwNOFACTSEBI;
import es.urjc.etsii.grafo.GD.neighborhood.LocalSearchSwSEBI;
import es.urjc.etsii.grafo.GD.neighborhood.LocalSearchSwSEFI;
import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.algorithms.SimpleAlgorithm;
import es.urjc.etsii.grafo.solver.SolverConfig;
import es.urjc.etsii.grafo.solver.services.AbstractExperiment;

import java.util.ArrayList;
import java.util.List;

public class LocalSearchFactExperiment extends AbstractExperiment<GDSolution, GDInstance> {

    /**
     * Initialize common fields for all experiments
     *
     * @param solverConfig solver configuration, see the application.yml file for more details
     */
    protected LocalSearchFactExperiment(SolverConfig solverConfig) {
        super(solverConfig);
    }

    @Override
    public List<Algorithm<GDSolution, GDInstance>> getAlgorithms() {
        var algorithms = new ArrayList<Algorithm<GDSolution, GDInstance>>();


        var constructive = new GDRandomConstructive();
        var lsNOFACT = new LocalSearchSwNOFACTSEBI();
        var lsFACT = new LocalSearchSwSEBI();
        algorithms.add(new SimpleAlgorithm<>("NOFACT", constructive, lsNOFACT));
        algorithms.add(new SimpleAlgorithm<>("FACT", constructive, lsFACT));

        return algorithms;
    }
}

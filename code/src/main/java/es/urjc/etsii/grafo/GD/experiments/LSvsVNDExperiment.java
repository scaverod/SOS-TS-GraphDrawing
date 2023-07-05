package es.urjc.etsii.grafo.GD.experiments;

import es.urjc.etsii.grafo.GD.constructives.GDConstructive;
import es.urjc.etsii.grafo.GD.constructives.GDRandomConstructive;
import es.urjc.etsii.grafo.GD.model.GDInstance;
import es.urjc.etsii.grafo.GD.model.GDSolution;
import es.urjc.etsii.grafo.GD.neighborhood.LocalSearchSwLEFI;
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

public class LSvsVNDExperiment extends AbstractExperiment<GDSolution, GDInstance> {

    /**
     * Initialize common fields for all experiments
     *
     * @param solverConfig solver configuration, see the application.yml file for more details
     */
    protected LSvsVNDExperiment(SolverConfig solverConfig) {
        super(solverConfig);
    }

    @Override
    public List<Algorithm<GDSolution, GDInstance>> getAlgorithms() {
        var algorithms = new ArrayList<Algorithm<GDSolution, GDInstance>>();

        var constructive = new GDRandomConstructive();

        var lsLE = new LocalSearchSwLEFI();
        var lsSE = new LocalSearchSwSEFI();
        ArrayList<Improver<GDSolution, GDInstance>> list = new ArrayList<>(2);
        list.add(lsLE);
        list.add(lsSE);
        var vnd = new VND<>(list, false);
        MultiStartAlgorithm<GDSolution, GDInstance> multistartLSLE = MultiStartAlgorithm.<GDSolution, GDInstance>builder().withTime(60, TimeUnit.SECONDS).withAlgorithmName("LSLE").build(new SimpleAlgorithm<>("LSLE", constructive, lsLE));
        MultiStartAlgorithm<GDSolution, GDInstance> multistartLSSE = MultiStartAlgorithm.<GDSolution, GDInstance>builder().withTime(60, TimeUnit.SECONDS).withAlgorithmName("LSSE").build(new SimpleAlgorithm<>("LSSE", constructive, lsSE));
        MultiStartAlgorithm<GDSolution, GDInstance> multistartVND = MultiStartAlgorithm.<GDSolution, GDInstance>builder().withTime(60, TimeUnit.SECONDS).withAlgorithmName("MS").build(new SimpleAlgorithm<>(constructive, vnd));

        algorithms.add(multistartLSLE);
        algorithms.add(multistartLSSE);
        algorithms.add(multistartVND);


        return algorithms;
    }
}

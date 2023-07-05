package es.urjc.etsii.grafo.GD.experiments;

import es.urjc.etsii.grafo.GD.gurobi.GDGurobiCrossings;
import es.urjc.etsii.grafo.GD.gurobi.GDGurobiRafa;
import es.urjc.etsii.grafo.GD.model.GDInstance;
import es.urjc.etsii.grafo.GD.model.GDSolution;
import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.solver.SolverConfig;
import es.urjc.etsii.grafo.solver.services.AbstractExperiment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/*
export LD_LIBRARY_PATH=/home/sergiocavero/opt/gurobi951/linux64/lib//home/sergiocavero/opt/gurobi951/linux64/lib/
java -Xmx16G -Xms8G -cp target/GD-0.10.jar -Djava.library.path=/home/sergiocavero/opt/gurobi951/linux64/lib/ -Dloader.path=/home/sergiocavero/opt/gurobi951/linux64/lib org.springframework.boot.loader.PropertiesLauncher

 */

public class GurobiExperiment extends AbstractExperiment<GDSolution, GDInstance> {


    /**
     * Initialize common fields for all experiments
     *
     * @param solverConfig solver configuration, see the application.yml file for more details
     * @param seconds      seconds to run the experiment
     *
     *                     How to use ?
     *                     java -jar --solver.gurobi.timelimit=60
     *                     [8:10] Raúl Martín Santamaría
     */
    protected GurobiExperiment(SolverConfig solverConfig, @Value("${solver.gurobi.timelimit:3600}") int seconds) {
        super(solverConfig);
        this.seconds = seconds;
    }

    static Logger logger = LoggerFactory.getLogger(GurobiExperiment.class);

    final int seconds;

    @Override
    public List<Algorithm<GDSolution, GDInstance>> getAlgorithms() {

        logger.info("CutOffTimeGurobi: " + seconds);

        var algorithms = new ArrayList<Algorithm<GDSolution, GDInstance>>();
        algorithms.add(new GDGurobiRafa(seconds, TimeUnit.SECONDS));
        algorithms.add(new GDGurobiCrossings(seconds, TimeUnit.SECONDS));

        return algorithms;
    }
}

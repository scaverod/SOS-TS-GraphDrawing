package es.urjc.etsii.grafo.GD.experiments;

import es.urjc.etsii.grafo.GD.constructives.GDConstructive;
import es.urjc.etsii.grafo.GD.model.GDInstance;
import es.urjc.etsii.grafo.GD.model.GDSolution;
import es.urjc.etsii.grafo.GD.neighborhood.LocalSearchSwLEBI;
import es.urjc.etsii.grafo.GD.neighborhood.LocalSearchSwLEFI;
import es.urjc.etsii.grafo.GD.neighborhood.LocalSearchSwSEBI;
import es.urjc.etsii.grafo.GD.neighborhood.LocalSearchSwSEFI;
import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.algorithms.SimpleAlgorithm;
import es.urjc.etsii.grafo.algorithms.multistart.MultiStartAlgorithm;
import es.urjc.etsii.grafo.create.Constructive;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.improve.VND;
import es.urjc.etsii.grafo.solver.irace.IraceAlgorithmGenerator;
import es.urjc.etsii.grafo.solver.irace.IraceRuntimeConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class IraceExperiment extends IraceAlgorithmGenerator<GDSolution, GDInstance> {



    @Override
    public Algorithm<GDSolution, GDInstance> buildAlgorithm(IraceRuntimeConfiguration config) {

        // CONSTRUCTIVE
        int iterations = Integer.parseInt(config.getValue("iterations").orElse("1000"));

        double LE_w = Double.parseDouble(config.getValue("LE_w").orElse("0.5"));
        double SE_W = Double.parseDouble(config.getValue("SE_w").orElse("0.5"));

        var LE_S = config.getValue("LongEdgeSelectionCriteria").orElseThrow();
        var LE_P = config.getValue("LongEdgeLocationCriteria").orElseThrow();
        var SE_S = config.getValue("InputVertexSelectionCriteria").orElseThrow();
        var SE_P = config.getValue("HostVertexCriteria").orElseThrow();

        double ts = Double.parseDouble(config.getValue("ts").orElse("0.5"));

        var constructive = buildConstructive(iterations, LE_w, SE_W, LE_S, LE_P, SE_S, SE_P, ts);

        // LOCAL SEARCH
        var LS_LE = config.getValue("LS_LE").orElseThrow();

        var localSearchLE = buildLocalSearchLE(LS_LE);

        var LS_SE = config.getValue("LS_SE").orElseThrow();

        var localSearchSE = buildLocalSearchSE(LS_SE);

        var order = config.getValue("order").orElseThrow();

        var vnd = buildVND(localSearchLE, localSearchSE, order);

        var algorithm = new SimpleAlgorithm<>(constructive, vnd);

        return MultiStartAlgorithm.<GDSolution, GDInstance>builder().withTime(60, TimeUnit.SECONDS).withMaxIterationsWithoutImproving(100).build(algorithm);
    }


    private Constructive<GDSolution, GDInstance> buildConstructive(int iterations, double LE_w, double SE_W, String LE_S, String LE_P, String SE_S, String SE_L, double ts) {
        return new GDConstructive(SE_W, 1 - SE_W, LE_w, 1 - LE_w, iterations, ts, getSE_S(SE_S), getSE_L(SE_L), getLE_S(LE_S), getLE_P(LE_P));
    }

    private GDConstructive.LongEdgeSelectionCriteria getLE_S(String LE_S) {
        return switch (LE_S) {
            case "RANDOM" -> GDConstructive.LongEdgeSelectionCriteria.RANDOM;
            case "SHORTEST_FIRST" -> GDConstructive.LongEdgeSelectionCriteria.SHORTEST_FIRST;
            case "LONGEST_FIRST" -> GDConstructive.LongEdgeSelectionCriteria.LONGEST_FIRST;
            case "MCALLISTER" -> GDConstructive.LongEdgeSelectionCriteria.MCALLISTER;
            default -> throw new IllegalArgumentException("LongEdgeSelectionCriteria: " + LE_S);
        };
    }

    private GDConstructive.LongEdgeLocationCriteria getLE_P(String LE_P) {
        return switch (LE_P) {
            case "RANDOM" -> GDConstructive.LongEdgeLocationCriteria.RANDOM;
            case "RANDOM_WITH_TABU" -> GDConstructive.LongEdgeLocationCriteria.RANDOM_WITH_TABU;
            case "MEDIAN" -> GDConstructive.LongEdgeLocationCriteria.MEDIAN;
            case "MEDIAN_WITH_TABU" -> GDConstructive.LongEdgeLocationCriteria.MEDIAN_WITH_TABU;
            default -> throw new IllegalArgumentException("LongEdgeLocationCriteria: " + LE_P);
        };
    }

    private GDConstructive.InputVertexSelectionCriteria getSE_S(String SE_S) {
        return switch (SE_S) {
            case "RANDOM" -> GDConstructive.InputVertexSelectionCriteria.RANDOM;
            case "MCALLISTER" -> GDConstructive.InputVertexSelectionCriteria.MCALLISTER;
            default -> throw new IllegalArgumentException("InputVertexSelectionCriteria: " + SE_S);
        };
    }

    private GDConstructive.HostVertexCriteria getSE_L(String SE_L) {
        return switch (SE_L) {
            case "RANDOM" -> GDConstructive.HostVertexCriteria.RANDOM;
            case "CROSSINGS" -> GDConstructive.HostVertexCriteria.CROSSINGS;
            case "MEDIAN" -> GDConstructive.HostVertexCriteria.MEDIAN;
            case "CROSSINGS_MEDIAN" -> GDConstructive.HostVertexCriteria.CROSSINGS_MEDIAN;
            default -> throw new IllegalArgumentException("HostVertexCriteria: " + SE_L);
        };
    }


    private Improver<GDSolution, GDInstance> buildLocalSearchLE(String LS_LE) {
        return switch (LS_LE) {
            case "FI" -> new LocalSearchSwLEFI();
            case "BI" -> new LocalSearchSwLEBI();
            default -> throw new IllegalArgumentException("LS_LE: " + LS_LE);
        };
    }


    private Improver<GDSolution, GDInstance> buildLocalSearchSE(String LS_SE) {
        return switch (LS_SE) {
            case "FI" -> new LocalSearchSwSEFI();
            case "BI" -> new LocalSearchSwSEBI();
            default -> throw new IllegalArgumentException("LS_SE: " + LS_SE);
        };
    }


    private VND<GDSolution, GDInstance> buildVND(Improver<GDSolution, GDInstance> localSearchLE, Improver<GDSolution, GDInstance> localSearchSE, String order) {
        return switch (order) {
            case "LE_SE" -> new VND<>(new ArrayList<>(Arrays.asList(localSearchLE, localSearchSE)), false);
            case "SE_LE" -> new VND<>(new ArrayList<>(Arrays.asList(localSearchSE, localSearchLE)), false);
            default -> throw new IllegalArgumentException("order: " + order);
        };
    }
}

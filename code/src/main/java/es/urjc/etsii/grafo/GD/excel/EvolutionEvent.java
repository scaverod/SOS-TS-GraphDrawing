package es.urjc.etsii.grafo.GD.excel;

import es.urjc.etsii.grafo.solver.services.events.types.MorkEvent;

import java.util.ArrayList;

public class EvolutionEvent extends MorkEvent {

    /**
     * Name of the algorithm
     */
    private final String algorithm;

    /**
     * Name of the instance
     **/
    private final String instance;

    /**
     * Evolution of the quality of the solution
     */
    private final ArrayList<Integer> evolution;

    private final int maxIter;

    /**
     * Constructor of the class
     *
     * @param algorithm Name of the algorithm
     * @param instance  Name of the instance
     * @param maxIter
     * @param evolution Evolution of the quality of the solution
     **/
    public EvolutionEvent(String algorithm, String instance, int maxIter, ArrayList<Integer> evolution) {
        this.algorithm = algorithm;
        this.instance = instance;
        this.maxIter = maxIter;
        this.evolution = evolution;
    }

    /**
     * Getter of the algorithm
     *
     * @return Name of the algorithm
     **/
    public String getAlgorithm() {
        return algorithm;
    }

    /**
     * Getter of the instance
     *
     * @return Name of the instance
     **/
    public String getInstance() {
        return instance;
    }

    /**
     * Getter of the maxIter
     *
     * @return maxIter
     **/
    public int getMaxIter() {
        return maxIter;
    }

    /**
     * Getter of the evolution
     *
     * @return Evolution of the quality of the solution
     **/
    public ArrayList<Integer> getEvolution() {
        return evolution;
    }

}

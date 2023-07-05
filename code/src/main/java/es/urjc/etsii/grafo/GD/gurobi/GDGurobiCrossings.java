package es.urjc.etsii.grafo.GD.gurobi;

import es.urjc.etsii.grafo.GD.model.GDInstance;
import es.urjc.etsii.grafo.GD.model.GDSolution;
import es.urjc.etsii.grafo.algorithms.Algorithm;
import gurobi.*;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class GDGurobiCrossings extends Algorithm<GDSolution, GDInstance> {

    private static final Logger log = LoggerFactory.getLogger(GDGurobiCrossings.class);



    public static final int NUMBER_OF_THREATS = 1;

    /**
     * Gurobi environment
     */
    protected final GRBEnv env;

    public GDGurobiCrossings() {
        try {
            this.env = initializeEnvironment(-1);
        } catch (GRBException e) {
            throw new RuntimeException(e);
        }
    }

    public GDGurobiCrossings(int time, TimeUnit timeUnit) {
        try {
            this.env = initializeEnvironment(timeUnit.toSeconds(time));
        } catch (GRBException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public GDSolution algorithm(GDInstance instance) {
        return execute(instance);
    }

    protected RuntimeException onModelFail(GRBModel model, int status) {
        log.error("Gurobi fail: {}", status);
        return new RuntimeException("Failed gurobi solve");
    }


    protected GRBEnv initializeEnvironment(long timeSeconds) throws GRBException {
        var env = new GRBEnv("gurobi_log.txt");
        if (timeSeconds > 0) {
            env.set(GRB.DoubleParam.TimeLimit, timeSeconds);
        }
        env.set(GRB.IntParam.LogToConsole, 0);
        env.set(GRB.IntParam.OutputFlag, 0);
        env.set(GRB.IntParam.Threads, GDGurobiCrossings.NUMBER_OF_THREATS);
        env.start();
        return env;
    }

    public GDSolution execute(GDInstance instance) {
        try {
            var model = new GRBModel(this.env);
            var dataGurobi = initializeModel(model, instance);
            model.optimize();

            int status = model.get(GRB.IntAttr.Status);
            if (status != GRB.INFEASIBLE) {
                System.out.println(status);
                System.out.println(model.get(GRB.DoubleAttr.ObjVal));
                return onModelSolved(instance, model, dataGurobi);
            }
            throw onModelFail(model, status);
        } catch (GRBException e) {
            throw new RuntimeException(e);
        } finally {
            if (env != null) {
                try {
                    env.dispose();
                } catch (GRBException ignored) {
                }
            }
        }
    }

    private GDSolution onModelSolved(GDInstance instance, GRBModel model, DataGurobi data) throws GRBException {
        for (int h = 0; h < data.x.length; h++) {
            for (int i = 0; i < data.x[h].length; i++) {
                for (int j = 0; j < data.x[h][i].length; j++) {
                    if (i != j) {
                        System.out.println(h + " " + (data.dictionaryHP_V.get(h).get(i)+1) + " " + (1+data.dictionaryHP_V.get(h).get(j)) + "->" + Math.rint(data.x[h][i][j].get(GRB.DoubleAttr.X)));
                    }
                }
            }
        }
        for (int i = 0; i < data.c.length; i++) {
            for (int j = 0; j < data.c.length; j++) {
                for (int k = 0; k < data.c.length; k++) {
                    for (int l = 0; l < data.c.length; l++) {
                        if (data.c[i][j][k][l] != null) {
                            System.out.println("c[" + (i+1) + "]" + "[" + (j+1) + "]" + "[" + (k+1) + "]" + "[" + (l+1) + "]-> " + data.c[i][j][k][l].get(GRB.DoubleAttr.X));
                        }
                    }
                }
            }
        }
        var s = new GDSolution(instance);
        for (int h = 0; h < data.x.length; h++) {
            for (int i = 0; i < data.x[h].length; i++) {
                int pos = instance.NumVerticesInLayer()-1;
                int vertex =  data.dictionaryHP_V.get(h).get(i);
                for (int j = 0; j < data.x[h][i].length; j++) {
                    if (i != j) {
                        pos = pos - (int)Math.rint(data.x[h][i][j].get(GRB.DoubleAttr.X));
                    }
                }
                s.setVertexPosition(vertex, h, pos);
            }
        }
        s.updateLastModifiedTime();
        s.getScore();
        return s;
    }

    public record DataGurobi(GRBVar[][][] x, GRBVar[][][][] c, HashMap<Integer, Pair<Integer, Integer>> dictionaryV_HP,
                             HashMap<Integer, HashMap<Integer, Integer>> dictionaryHP_V) {
    }

    protected DataGurobi initializeModel(GRBModel model, GDInstance instance) throws GRBException {

        int totalNumVertices = instance.TotalNumVertices(); // NUMBER OF VERTICES OF THE GRAPH
        int numLayers = instance.NumberOfLayers();  // NUMBER OF LAYERS
        int numVerticesInLayer = instance.NumVerticesInLayer(); // NUMBER OF VERTICES IN LAYERS

        // Vértice --> H, posición
        HashMap<Integer, Pair<Integer, Integer>> dictionaryV_HP = new HashMap<>(totalNumVertices);
        // H, posición --> Vertice
        HashMap<Integer, HashMap<Integer, Integer>> dictionaryHP_V = new HashMap<>(instance.NumberOfLayers());

        generateDictionaries(instance, dictionaryV_HP, dictionaryHP_V);

        // CREATE VARIABLES
        // x^h_ij
        GRBVar[][][] x = new GRBVar[numLayers][numVerticesInLayer][numVerticesInLayer];
        for (int h = 0; h < numLayers; h++) {
            for (int i = 0; i < numVerticesInLayer; i++) {
                for (int j = 0; j < numVerticesInLayer; j++) {
                    var name = "x[" + h + "][" + i + "]" + "[" + j + "]";
                    if (i != j) {
                        // EN LA CAPA H, I ESTÁ ANTES QUE J 0/1
                        x[h][i][j] = model.addVar(0.0, 1.0, 0, GRB.BINARY, name);
                    }
                }
            }
        }
        // Defino las variables c_ijkl

        // Aprovecho para definir la funcion objetivo
        GRBLinExpr objective = new GRBLinExpr();

        GRBVar[][][][] c = new GRBVar[totalNumVertices][totalNumVertices][totalNumVertices][totalNumVertices];

        for (int e1 = 0; e1 < instance.getListOfShortEdges().size(); e1++) {
            for (int e2 = e1 + 1; e2 < instance.getListOfShortEdges().size(); e2++) {
                var edge1 = instance.getListOfShortEdges().get(e1);
                var edge2 = instance.getListOfShortEdges().get(e2);
                var i = edge1.source();
                var j = edge1.target();
                var k = edge2.source();
                var l = edge2.target();
                if (dictionaryV_HP.get(i).getValue0().intValue() == dictionaryV_HP.get(k).getValue0().intValue()) {
                    var name = "c[" + i + "]" + "[" + j + "]" + "[" + k + "]" + "[" + l + "]";
                    c[i][j][k][l] = model.addVar(0.0, 1.0, 1.0, GRB.BINARY, name);
                    objective.addTerm(1.0, c[i][j][k][l]); // DEFINE OBJECTIVE FUNCTION
                }
            }
        }
        model.setObjective(objective, GRB.MINIMIZE);


        // RESTRICCIONES 1 Y 2.
        // SI I ESTÁ DELANTE DE K Y L DELANTE DE J, Y HAY UNA ARISTA (I,J), (K,L), ESTÁNDO I Y K EN H Y J Y L EN H+1, ENTONCES HAY UN CORTE


        for (int e1 = 0; e1 < instance.getListOfShortEdges().size(); e1++) {
            for (int e2 = e1; e2 < instance.getListOfShortEdges().size(); e2++) {
                var edge1 = instance.getListOfShortEdges().get(e1); // (i,j)
                var edge2 = instance.getListOfShortEdges().get(e2); // (k,l)
                var i = edge1.source();
                var j = edge1.target();
                var k = edge2.source();
                var l = edge2.target();
                var xi = dictionaryV_HP.get(i);
                var xj = dictionaryV_HP.get(j);
                var xk = dictionaryV_HP.get(k);
                var xl = dictionaryV_HP.get(l);
                if (xi.getValue0().intValue() == xk.getValue0().intValue()) {
                   // if (xi.getValue1() < xk.getValue1() && xj.getValue1().intValue() != xl.getValue1().intValue()) {
                    if (xi.getValue1().intValue() != xk.getValue1().intValue() && xj.getValue1().intValue() != xl.getValue1().intValue()) {
                        GRBLinExpr r1 = new GRBLinExpr();
                        r1.addTerm(1, x[xi.getValue0()][xi.getValue1()][xk.getValue1()]); // Xh_ik
                        r1.addTerm(1, x[xj.getValue0()][xl.getValue1()][xj.getValue1()]); // Xh+1_lj
                        r1.addTerm(-1, c[i][j][k][l]); // cijkl
                        model.addConstr(r1, GRB.LESS_EQUAL, 1.0, "r1_" + xi.getValue0() + "_" + xi.getValue1() + "_" + xj.getValue1() + "_" + xk.getValue1() + "_" + xl.getValue1());
                        GRBLinExpr r2 = new GRBLinExpr();
                        r2.addTerm(1, x[xi.getValue0()][xk.getValue1()][xi.getValue1()]); // Xh_ki
                        r2.addTerm(1, x[xj.getValue0()][xj.getValue1()][xl.getValue1()]); // Xh+1_jl
                        r2.addTerm(-1, c[i][j][k][l]); // cijkl
                        model.addConstr(r2, GRB.LESS_EQUAL, 1.0, "r2_" + xi.getValue0() + "_" + xi.getValue1() + "_" + xj.getValue1() + "_" + xk.getValue1() + "_" + xl.getValue1());
                    }
                }
            }
        }


        // RESTRICCIÓN 3: SI I ESTÁ DELANTE DE J, J ESTÁ DELANTE DE K, ENTONCES K NO PUEDE ESTÁR DELANTE DE I
        for (int h = 0; h < numLayers; h++) {
            for (int i = 0; i < numVerticesInLayer; i++) {
                for (int j = i + 1; j < numVerticesInLayer; j++) {
                    for (int k = j + 1; k < numVerticesInLayer; k++) {
                        GRBLinExpr r3 = new GRBLinExpr();
                        r3.addTerm(1.0, x[h][i][j]);
                        r3.addTerm(1.0, x[h][j][k]);
                        r3.addTerm(1.0, x[h][k][i]);
                        model.addConstr(r3, GRB.LESS_EQUAL, 2.0, "r3_" + h + "_" + i + "_" + j + "_" + k);
                        GRBLinExpr r3i = new GRBLinExpr();
                        r3i.addTerm(1.0, x[h][j][i]);
                        r3i.addTerm(1.0, x[h][k][j]);
                        r3i.addTerm(1.0, x[h][i][k]);
                        model.addConstr(r3i, GRB.LESS_EQUAL, 2.0, "r3i_" + h + "_" + i + "_" + j + "_" + k);
                    }
                }
            }
        }


        // RESTRICCIÓN 4: SI I ESTÁ DELANTE DE J, J NO PUEDE ESTÁR DELANTE DE I
        for (int h = 0; h < numLayers; h++) {
            for (int i = 0; i < instance.NumVerticesInLayer(); i++) {
                for (int j = i + 1; j < instance.NumVerticesInLayer(); j++) {
                    GRBLinExpr r4 = new GRBLinExpr();
                    var name = "x[" + h + "][" + i + "]" + "[" + j + "]";
                    r4.addTerm(1.0, x[h][i][j]);
                    r4.addTerm(1.0, x[h][j][i]);
                    model.addConstr(r4, GRB.EQUAL, 1.0, "r4_" + h + "_" + i + "_" + j + "_");
                }
            }
        }
        model.update();

        return new DataGurobi(x, c,dictionaryV_HP, dictionaryHP_V);

    }

    private void generateDictionaries(GDInstance instance, HashMap<Integer, Pair<Integer, Integer>> dictionaryV_hp, HashMap<Integer, HashMap<Integer, Integer>> dictionaryHP_v) {
        for (int h = 0; h < instance.NumberOfLayers(); h++) {
            int pos = 0;
            dictionaryHP_v.put(h, new HashMap<>(instance.NumVerticesInLayer()));
            for (Integer v : instance.getListOfVerticesInLayer(h)) {
                dictionaryV_hp.put(v, new Pair<>(h, pos));
                dictionaryHP_v.get(h).put(pos, v);
                pos++;
            }
        }

    }

    @Override
    public String getShortName() {
        return "GurobiCross";
    }

}




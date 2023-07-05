package es.urjc.etsii.grafo.GD.model;

import es.urjc.etsii.grafo.solution.Solution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class GDSolution extends Solution<GDSolution, GDInstance> {

    /*
     * In this implementation, the grid has been defined as an array:
     *
     *                  Grid 7 x 7 (n x n)
     *
     *     i/j  | 0 | 1 | 2 | 3 | 4 | 5 | 6 |                i/j  | 0 | 1 | 2 | 3 | 4 | 5 | 6 |
     *      0   |0,0|0,1|0,2|0,3|0,4|0,5|0,6|                 0   | 0 | 1 | 2 | 3 | 4 | 5 | 6 |
     *      1   |1,0|1,1|1,2|1,3|1,4|1,5|1,6|                 1   | 7 | 8 | 9 |10 |11 |12 |13 |
     *      2   |2,0|2,1|2,2|2,3|2,4|2,5|2,6|                 2   |14 |15 |16 |17 |18 |19 |20 |
     *      3   |3,0|3,1|3,2|3,3|3,4|3,5|3,6|                 3   |21 |22 |23 |24 |25 |26 |27 |
     *      4   |4,0|4,1|4,2|4,3|4,4|4,5|4,6|                 4   |28 |29 |30 |31 |32 |33 |34 |
     *      5   |5,0|5,1|5,2|5,3|5,4|5,5|5,6|                 5   |35 |36 |37 |38 |39 |40 |41 |
     *      6   |6,0|6,1|6,2|6,3|6,4|6,5|6,6|                 6   |42 |43 |44 |45 |46 |47 |48 |
     *
     *
     *     |0,0|0,1|0,2|0,3|0,4|0,5|0,6|1,0|1,1|1,2|1,3|1,4|1,5|1,6|2,0|2,1|2,2|2,3|2,4|2,5|2,6|3,0|3,1|3,2|...
     *     | 0 | 1 | 2 | 3 | 4 | 5 | 5 | 6 | 7 | 8 | 9 | 10| 11|12 |13 |14 |15 |16 |17 |18 |19 |10 |21 |22 |...
     *
     *
     * Therefore, in order to calculate the relative position:
     *
     *    Position of the array (p): 13
     *    Position of the matrix:  i= p/n = 13/7 = 1 /// j = i= p%n = 13%7 = 6 --> (i,j) = (1,6)
     *    Position of the array (p): 22
     *    Position of the matrix:  i= p/n = 22/7 = 3 /// j = i= p%n = 22%7 = 1 --> (i,j) = (3,1)
     *
     */

    /**
     * This array represents a solution in such a way that the indexes are de vertices of the host graph and the
     * content of each component of the array is a vertex of the input graph.
     * <p>
     * index: vertices of the host graph     [0][1][2][3][4][5][6][7][8][...]
     * content: vertices of the input graph  [5][1][4][7][8][6][0][2][3][...]
     * <p>
     * For example, in the vertex "2" of the host graph is embedded the vertex "4" of the input graph
     */
    private final int[] hostGraph;
    /**
     * This array represents a solution in such a way that the indexes are de vertices of the input graph and the
     * content of each component of the array is a vertex of the host graph.
     * <p>
     * index: vertices of the input graph  [0][1][2][3][4][5][6][7][8][...]
     * content: vertices of the host graph [6][1][7][8][2][0][5][3][4][...]
     * <p>
     * For example, the vertex "4" of the input graph is embedded in the vertex "2" of the  host graph
     */
    private final int[] inputGraph;

    /**
     * Objective function of the solution
     */
    private int objectiveFunction;


//    /**
//     * Cuts associated to each edge
//     */
//    private HashMap<GDInstance.Edge, Integer> cutsOfEdge;


    private int[][] matrixCuts;

    private static final Map<String, Function<GDSolution, Integer>> customProperties = Map.of("alignment",  GDSolution::getAlignmentSt);


    /**
     * Initialize solution from instance
     *
     * @param ins instance of the problem
     */
    public GDSolution(GDInstance ins) {
        super(ins);
        this.objectiveFunction = Integer.MAX_VALUE;
        this.hostGraph = new int[ins.TotalNumVertices()];
        this.inputGraph = new int[ins.TotalNumVertices()];
        this.matrixCuts = new int[ins.TotalNumVertices()][ins.TotalNumVertices()];
    }

    /**
     * Clone constructor
     *
     * @param s Solution to clone
     */
    public GDSolution(GDSolution s) {
        super(s);
        this.objectiveFunction = s.objectiveFunction;
        this.hostGraph = s.hostGraph.clone();
        this.inputGraph = s.inputGraph.clone();
        this.matrixCuts = Arrays.stream(s.matrixCuts).map(int[]::clone).toArray(int[][]::new);
    }


    /**
     * Clone a solution in a new one
     *
     * @return a new solution with the same content
     */
    @Override
    public GDSolution cloneSolution() {
        return new GDSolution(this);
    }


    ////////////////////////////////////////
    //    Comparing solutions methods     //
    ////////////////////////////////////////

    /**
     * Compare current solution against another. Return true if "this" solution is
     * better than the one passed as a function parameter. False if solution is equal or worse than the other.
     *
     * @param other other solution
     * @return true if "this" solution is better than the one passed as a function parameter. False if solution is equal or worse than the other.
     */
    @Override
    protected boolean _isBetterThan(GDSolution other) {
        return this.getScore() < other.getScore();
    }


    //////////////////////////////////////////////
    //  Objective function calculation methods  //
    //////////////////////////////////////////////

    /**
     * Get the current solution score.
     * The difference between this method and recalculateScore is that
     * this result can be a property of the solution, or cached,
     * it does not have to be calculated each time this method is called
     *
     * @return current solution score as double
     */
    @Override
    public double getScore() {
        if (this.objectiveFunction == Integer.MAX_VALUE) {
            this.objectiveFunction = __recalculateScore(matrixCuts);
        }
        return this.objectiveFunction;
    }

    /**
     * Recalculate solution score and validate current solution state
     * You must check that no constraints are broken, and that all costs are valid
     * The difference between this method and getScore is that we must recalculate the score from scratch,
     * without using any cache/shortcuts.
     * DO NOT UPDATE CACHES / MAKE SURE THIS METHOD DOES NOT HAVE SIDE EFFECTS
     *
     * @return current solution score as double
     */
    @Override
    public double recalculateScore() {
        var ins = this.getInstance();
        this.matrixCuts = new int[ins.TotalNumVertices()][ins.TotalNumVertices()];
        return __recalculateScore(matrixCuts);
    }


    /**
     * Get the current solution score.
     * The difference between this method and recalculateScore is that
     * this result can be a property of the solution, or cached,
     * it does not have to be calculated each time this method is called
     *
     * @param matrixCuts
     * @return current solution score as double, but will be cached in the solution objectiveFunction field
     */
    private int __recalculateScore(int[][] matrixCuts) {
        int score = 0;
        var instance = this.getInstance();
        for (int h = 0; h < instance.NumberOfLayers(); h++) {
            ArrayList<GDInstance.Edge> shortEdgesOfLayer = instance.shortEdgesOfLayer(h);
            for (int i = 0, shortEdgesOfLayerSize = shortEdgesOfLayer.size(); i < shortEdgesOfLayerSize; i++) {
                GDInstance.Edge edge = shortEdgesOfLayer.get(i);
                for (int j = i + 1; j < shortEdgesOfLayerSize; j++) {
                    GDInstance.Edge otherEdge = shortEdgesOfLayer.get(j);
                    if (isCut(edge, otherEdge)) {
                        score += 1;
                        addMatrixCut(edge, otherEdge);
                    } else {
                        addMatrixNonCut(edge, otherEdge);
                    }
                }
            }
        }
        return score;
    }


    /**
     * Recalculate the value of the objective function. V1 must be lower than V2
     *
     * @param v1 first vertex
     * @param v2 second vertex
     */
    private void fastSwapRecalculateScore(int v1, int v2) {
        if (v1 > v2) {
            int temp = v1;
            v1 = v2;
            v2 = temp;
        }
        this.objectiveFunction = this.objectiveFunction + this.matrixCuts[v2][v1] - this.matrixCuts[v1][v2];
        var instance = this.getInstance();
        this.matrixCuts[v1][v2] = 0;
        this.matrixCuts[v2][v1] = 0;
        var downAdjV1 = instance.getAdjacentShortVertexListOfVertex(v1, GDInstance.TYPE_OF_ADJACENT_VERTEX.SOURCE);
        var downAdjV2 = instance.getAdjacentShortVertexListOfVertex(v2, GDInstance.TYPE_OF_ADJACENT_VERTEX.SOURCE);
        updateMatrix(v1, v2, downAdjV1, downAdjV2);
        var upAdjV1 = instance.getAdjacentShortVertexListOfVertex(v1, GDInstance.TYPE_OF_ADJACENT_VERTEX.TARGET);
        var upAdjV2 = instance.getAdjacentShortVertexListOfVertex(v2, GDInstance.TYPE_OF_ADJACENT_VERTEX.TARGET);
        updateMatrix(v1, v2, upAdjV1, upAdjV2);
    }

    private void updateMatrix(int v1, int v2, ArrayList<Integer> listAdjV1, ArrayList<Integer> listAdjV2) {
        for (int v1Adj : listAdjV1) {
            var posV1Adj = this.getPositionOfInputVertex(v1Adj);
            for (int v2Adj : listAdjV2) {
                var posV2Adj = this.getPositionOfInputVertex(v2Adj);
                GDInstance.Edge edge = new GDInstance.Edge(v1, v1Adj);
                GDInstance.Edge otherEdge = new GDInstance.Edge(v2, v2Adj);
                if (isCut(edge, otherEdge)) {
                    addMatrixCut(edge, otherEdge);
                } else {
                    addMatrixNonCut(edge, otherEdge);
                }
                if (isCutByPos(this.getPositionOfInputVertex(v2), posV1Adj, this.getPositionOfInputVertex(v1), posV2Adj)) {
                    removeMatrixCut(edge, otherEdge);
                } else {
                    removeMatrixNonCut(edge, otherEdge);
                }
            }
        }
    }

    private void removeMatrixNonCut(GDInstance.Edge e1, GDInstance.Edge e2) {
        var u = e1.source();
        var v = e2.source();
        var w = e1.target();
        var x = e2.target();
        if (u != v && w != x) {
            if (w < x) {
                this.matrixCuts[x][w]--;
            } else {
                this.matrixCuts[w][x]--;
            }
        }
    }

    private void removeMatrixCut(GDInstance.Edge e1, GDInstance.Edge e2) {
        var w = e1.target();
        var x = e2.target();
        if (w < x) {
            this.matrixCuts[w][x]--;
        } else {
            this.matrixCuts[x][w]--;
        }
    }

    /**
     * Check if there is a cut between two edges (u,v) and (w,z)
     *
     * @param edge      (u,v)
     * @param otherEdge (w,z)
     * @return true if there is a cut between edges (u,v) and (w,z)
     */
    private boolean isCut(GDInstance.Edge edge, GDInstance.Edge otherEdge) {
        var posU = this.getPositionOfInputVertex(edge.source());
        var posV = this.getPositionOfInputVertex(edge.target());
        var posW = this.getPositionOfInputVertex(otherEdge.source());
        var posZ = this.getPositionOfInputVertex(otherEdge.target());
        return isCutByPos(posU, posV, posW, posZ);
    }

    /**
     * Check if there is a cut between two edges (u,v) and (w,z)
     *
     * @param posU position of vertex u
     * @param posV position of vertex v
     * @param posW position of vertex w
     * @param posZ position of vertex z
     * @return true if there is a cut between edges (u,v) and (w,z)
     */
    private boolean isCutByPos(int posU, int posV, int posW, int posZ) {
        // check if u and w are the same and check if v and z are the same
        // if so, there is no cut
        if (posU == posW) return false;
        if (posV == posZ) return false;
        // if posU < posW and posV < posZ, then there is not a cut
        // or if posU > posW and posV > posZ, then there is not a cut
        // or if posU < posW and posV > posZ, then there is a cut
        // or if posU > posW and posV < posZ, then there is a cut
        if (posU < posW && posV < posZ) {
            return false;
        } else return posU <= posW || posV <= posZ;
    }


    ////////////////////////////////////////
    //  Methods for defining an embedding //
    ////////////////////////////////////////

    /**
     * Get the position in the layout of a specific vertex (i.e. the vertex of the host graph)
     *
     * @param inputVertex input vertex
     * @return the position in the layout, the vertex of the host graph in which the vertex is embedded.
     */
    public int getHostVertexAssignedTo(int inputVertex) {
        return this.inputGraph[inputVertex];
    }


    /**
     * Get the layer of a specific input vertex (i.e. the layer of the host graph)
     *
     * @param inputVertex input vertex
     * @return the layer of the vertex in the layout, the layer of the host graph in which the vertex is embedded.
     */
    public int getLayerOfInputVertex(int inputVertex) {
        return this.getInstance().getLayerOfVertex(inputVertex);
    }


    /**
     * Get the position of a specific input vertex (i.e. the position in layer of the host graph)
     *
     * @param inputVertex input vertex
     * @return the position of the vertex in the layout, the position of the host graph in which the vertex is embedded.
     */
    public int getPositionOfInputVertex(int inputVertex) {
        int layer = this.getLayerOfInputVertex(inputVertex);
        return this.getHostVertexAssignedTo(inputVertex) - layer * this.getInstance().NumVerticesInLayer();
    }

    /**
     * Get the input vertex assigned to a specific position in the layout
     *
     * @param layer    layer
     * @param position position in the layer
     * @return the vertex of the input graph embedded in the host graph
     */
    public int getInputVertexAssignedTo(int layer, int position) {
        return this.hostGraph[this.getHostVertex(layer, position)];
    }

    /**
     * Given a layer and a position, get the host vertex
     *
     * @param layer    layer
     * @param position position
     * @return host vertex
     */
    private int getHostVertex(int layer, int position) {
        return getInstance().NumVerticesInLayer() * layer + position;
    }


    /**
     * Set the host vertex in which a vertex of the input graph is going to be embedded.
     *
     * @param inputVertex vertex is going to be embedded in the host graph
     * @param layer       layer in which the vertex is going to be embedded in the host graph
     * @param position    host vertex in which the vertex is going to be embedded in the host graph
     */
    public void setVertexPosition(int inputVertex, int layer, int position) {
        int hostVertex = this.getHostVertex(layer, position);
        this.inputGraph[inputVertex] = hostVertex;
        this.hostGraph[hostVertex] = inputVertex;
    }


    /**
     * Generate a string representation of this solution. Used when printing progress to console,
     * show as minimal info as possible
     *
     * @return Small string representing the current solution (Example: id + score)
     */
    @Override
    public String toString() {
        StringBuilder resul = new StringBuilder("OF: " + this.objectiveFunction + "\n");
        for (int l = 0; l < this.getInstance().NumberOfLayers(); l++) {
            for (int p = 0; p < this.getInstance().NumVerticesInLayer(); p++) {
                resul.append(this.getInputVertexAssignedTo(l, p)).append(" ");
            }
            resul.append("\n");
        }
        // append the alignment
        resul.append("Alignment: ").append(this.getAlignment()).append("\n");
        return resul.toString();
    }

    private void addMatrixCut(GDInstance.Edge e1, GDInstance.Edge e2) {
        var u = e1.source();
        var v = e2.source();
        var w = e1.target();
        var x = e2.target();
        if (u < v) {
            this.matrixCuts[u][v]++;
        } else {
            this.matrixCuts[v][u]++;
        }
        if (w < x) {
            this.matrixCuts[w][x]++;
        } else {
            this.matrixCuts[x][w]++;
        }
    }

    private void addMatrixNonCut(GDInstance.Edge e1, GDInstance.Edge e2) {
        var u = e1.source();
        var v = e2.source();
        var w = e1.target();
        var x = e2.target();
        if (u != v && w != x) {
            if (u < v) {
                this.matrixCuts[v][u]++;
            } else {
                this.matrixCuts[u][v]++;
            }
            if (w < x) {
                this.matrixCuts[x][w]++;
            } else {
                this.matrixCuts[w][x]++;
            }
        }
    }

    public void setScore(int v) {
        this.objectiveFunction = v;
    }

    /**
     * Swap two vertices in the same layer
     *
     * @param v1 first vertex
     * @param v2 second vertex
     */
    public void swap(int v1, int v2) {
        assert this.getLayerOfInputVertex(v1) == this.getLayerOfInputVertex(v2);
        int temp = this.inputGraph[v1];
        this.inputGraph[v1] = this.inputGraph[v2];
        this.inputGraph[v2] = temp;
        temp = this.hostGraph[this.inputGraph[v1]];
        this.hostGraph[this.inputGraph[v1]] = this.hostGraph[this.inputGraph[v2]];
        this.hostGraph[this.inputGraph[v2]] = temp;
        this.objectiveFunction = (int) this.recalculateScore();
    }

    /**
     * Fast swap of two vertices in the same layer
     */
    public void fastSwap(int v1, int v2) {
        assert this.getLayerOfInputVertex(v1) == this.getLayerOfInputVertex(v2);
        int temp = this.inputGraph[v1];
        this.inputGraph[v1] = this.inputGraph[v2];
        this.inputGraph[v2] = temp;
        temp = this.hostGraph[this.inputGraph[v1]];
        this.hostGraph[this.inputGraph[v1]] = this.hostGraph[this.inputGraph[v2]];
        this.hostGraph[this.inputGraph[v2]] = temp;
        var posV1 = this.getPositionOfInputVertex(v1);
        var posV2 = this.getPositionOfInputVertex(v2);
        this.fastSwapRecalculateScore(v1, v2);
        if (posV2 < posV1) {
            var tem = posV1;
            posV1 = posV2;
            posV2 = tem;
        }
        for (int i = posV1 + 1; i < posV2; i++) {
            var v3 = this.getInputVertexAssignedTo(this.getLayerOfInputVertex(v1), i);
            this.fastSwapRecalculateScore(v1, v3);
            this.fastSwapRecalculateScore(v2, v3);
        }
    }

    /**
     * Get the variation in the objetive function if the swap is performed
     *
     * @param v1 first vertex
     * @param v2 second vertex
     * @return the variation in the objective function
     */
    public int getVariation(int v1, int v2) {
        var variation = this.getVariationOfSingleSwap(v1, v2);
        var posV1 = this.getPositionOfInputVertex(v1);
        var posV2 = this.getPositionOfInputVertex(v2);
        if (posV2 < posV1) {
            var tem = posV1;
            posV1 = posV2;
            posV2 = tem;
        }
        for (int i = posV1 + 1; i < posV2; i++) {
            var v3 = this.getInputVertexAssignedTo(this.getLayerOfInputVertex(v1), i);
            variation += getVariationOfSingleSwap(v1, v3);
            variation += getVariationOfSingleSwap(v2, v3);
        }
        return variation;
    }

    private int getVariationOfSingleSwap(int v1, int v2) {
        if (v1 > v2) {
            int temp = v1;
            v1 = v2;
            v2 = temp;
        }
        return this.matrixCuts[v2][v1] - this.matrixCuts[v1][v2];
    }


    public void swapLongEdge(GDInstance.Edge longEdge, Integer position) {
        var ins = this.getInstance();
        var listOfShortEdges = ins.getListOfShortEdgesOfLongEdge(longEdge);
        this.fastSwap(longEdge.source(), this.getInputVertexAssignedTo(ins.getLayerOfVertex(longEdge.source()), position));
        for (GDInstance.Edge shortEdge : listOfShortEdges) {
            this.fastSwap(shortEdge.target(), this.getInputVertexAssignedTo(ins.getLayerOfVertex(shortEdge.target()), position));
        }
    }

    /**
     * Get the alignment of a long edge
     * The aligment is the sum of the distance between each dummy vertex and to the first vertex of the long edge
     *
     * @param longEdge the long edge
     * @return the alignment of the long edge
     */
    public int getAlignmentOfLongEdge(GDInstance.Edge longEdge) {
        var ins = this.getInstance();
        var listOfShortEdges = ins.getListOfShortEdgesOfLongEdge(longEdge);
        var alignment = 0;
        var firstVertex = longEdge.source();
        var firstVertexPosition = this.getPositionOfInputVertex(firstVertex);
        for (GDInstance.Edge shortEdge : listOfShortEdges) {
            var vertex = shortEdge.target();
            var vertexPosition = this.getPositionOfInputVertex(vertex);
            alignment += Math.abs(vertexPosition - firstVertexPosition);
        }
        return alignment;
    }

    /**
     * Get the alignment of the solution
     * The alignment is the sum of the distance between each dummy vertex and to the first vertex of the long edge
     *
     * @return the alignment of the solution, i.e., the sum of the alignment of each long edge
     */
    public int getAlignment() {
        var ins = this.getInstance();
        var alignment = 0;
        for (GDInstance.Edge longEdge : ins.getListOfLongEdges()) {
            alignment += this.getAlignmentOfLongEdge(longEdge);
        }
        return alignment;
    }

    /**
     * Get the alignment of the solution
     * The alignment is the sum of the distance between each dummy vertex and to the first vertex of the long edge
     *
     * @return the alignment of the solution, i.e., the sum of the alignment of each long edge
     */
    public static int getAlignmentSt(GDSolution solution) {
        var ins = solution.getInstance();
        var alignment = 0;
        for (GDInstance.Edge longEdge : ins.getListOfLongEdges()) {
            alignment += solution.getAlignmentOfLongEdge(longEdge);
        }
        return alignment;
    }

}

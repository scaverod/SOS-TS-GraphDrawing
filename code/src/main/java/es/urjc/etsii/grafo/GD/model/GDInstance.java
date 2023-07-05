package es.urjc.etsii.grafo.GD.model;

import es.urjc.etsii.grafo.io.Instance;
import org.javatuples.Triplet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;


/**
 * This class represents a proper hierarchy graph
 * A proper hierarchy graph is defined as PH = (V U V', A', nl, L)
 * - V: vertices of the original graph
 * - V': dummy vertices
 * - A': short edges of the original graph and dummy edges. A' = A / AL U AL'
 * - nl: number of layers
 * - L: function that assigns a vertex to a layer
 */
public class GDInstance extends Instance {

    ////////////////////////////////////////
    //  Attributes related with vertices  //
    ////////////////////////////////////////

    /**
     * |V U V'|
     */
    private final int totalNumVertices;

    /**
     * |V|
     */
    private final int numRealVertices;

    /**
     * |V'|
     */
    private final int numDummyVertices;

    /**
     * List of all vertices V U V'
     */
    private final HashSet<Integer> totalListOfVertices;

    /**
     * List of real vertices V
     */
    private final HashSet<Integer> listOfRealVertices;

    /**
     * List of real vertices V with short edges
     */
    private final HashSet<Integer> listOfRealVerticesWithShortEdges;

    /**
     * List of real vertices V with long edges
     */
    private final HashSet<Integer> listOfRealVerticesWithLongEdges;


    /**
     * Set of adjacent vertices of a long edge
     */
    private final HashMap<Edge, HashSet<Integer>> adjacentVerticesWithLongEdgesOfLongEdge;

    /**
     * List of dummy vertices V'
     */
    private final HashSet<Integer> listOfDummyVertices;


    ////////////////////////////////////////
    //    Attributes related with edges   //
    ////////////////////////////////////////

    /**
     * Number of edges |A'|
     */
    private final int numEdges;

    /**
     * Number of real edges |A|
     */
    private final int numRealEdges;

    /**
     * Number of long edges |AL|
     */
    private final int numRealLongEdges;

    /**
     * Number of dummy edges |AL'|
     */
    private final int numDummyEdges;

    /**
     * List of short edges A'
     */
    private final ArrayList<Edge> listOfShortEdges;

    /**
     * List of long edges AL
     */
    private final ArrayList<Edge> listOfLongEdges;

    /**
     * List of vertices adjacent to a given vertex, connected by a short edge (not long arcs)
     * List of vertices are classified into:
     * - Source vertices
     * - Target vertices
     * - All vertices
     */
    private final HashMap<Integer, Triplet<ArrayList<Integer>, ArrayList<Integer>, ArrayList<Integer>>> adjacentListOfShortEdgesOfVertex;

    /**
     * Set of vertices adjacent to a given vertex, connected by a short edge (not long arcs)
     * Set of vertices are classified into:
     * - Source vertices
     * - Target vertices
     * - All vertices
     */
    private final HashMap<Integer, Triplet<HashSet<Integer>, HashSet<Integer>, HashSet<Integer>>> adjacentSetOfShortEdgesOfVertex;


    /**
     * List of dummy vertices  adjacent to a given vertex
     * List of vertices are classified into:
     * - Source vertices
     * - Target vertices
     * - All vertices
     */
    private final HashMap<Integer, Triplet<ArrayList<Integer>, ArrayList<Integer>, ArrayList<Integer>>> adjacentListOfDummyVerticesOfVertex;


    /**
     * List of short edges assigned to each long edge AL'
     */
    private final HashMap<Edge, ArrayList<Edge>> listOfShortEdgesOfLongEdge;


    /**
     * List of vertices of each layer L
     */
    private final HashMap<Integer, ArrayList<Integer>> listOfVerticesInLayer;


    /**
     * List of short edges of each layer L
     */
    private final HashMap<Integer, ArrayList<Edge>> listOfShortEdgesInLayer;


    ////////////////////////////////////////
    //     Attributes with the layers     //
    ////////////////////////////////////////

    /**
     * nl
     */
    private final int numberOfLayers;

    /**
     * Number of vertices in a layer
     */
    private final int numVerticesInLayer;

    /**
     * Array that store for each vertex V U V' the layer where they are located
     */
    private final int[] layerOfVertex;


    public GDInstance(String name, int totalNumVertices, int numRealVertices, int numDummyVertices, int numEdges, int numRealEdges, int numRealLongEdges, int numDummyEdges, int numberOfLayers, HashSet<Integer> totalListOfVertices, HashSet<Integer> listOfRealVertices, HashSet<Integer> listOfDummyVertices, ArrayList<Edge> listOfShortEdges, ArrayList<Edge> listOfLongEdges, HashMap<Integer, Triplet<ArrayList<Integer>, ArrayList<Integer>, ArrayList<Integer>>> adjacentListOfShortEdgesOfVertex, HashMap<Edge, ArrayList<Edge>> listOfShortEdgesOfLongEdge, HashMap<Integer, ArrayList<Integer>> listOfVerticesInLayer, int[] layerOfVertex) {
        super(name);
        this.totalNumVertices = totalNumVertices;
        this.numRealVertices = numRealVertices;
        this.numDummyVertices = numDummyVertices;
        this.numEdges = numEdges;
        this.numRealEdges = numRealEdges;
        this.numRealLongEdges = numRealLongEdges;
        this.numDummyEdges = numDummyEdges;
        this.numberOfLayers = numberOfLayers;
        this.totalListOfVertices = totalListOfVertices;
        this.listOfRealVertices = listOfRealVertices;
        this.listOfDummyVertices = listOfDummyVertices;
        this.listOfShortEdges = listOfShortEdges;
        this.listOfLongEdges = listOfLongEdges;
        this.adjacentListOfShortEdgesOfVertex = adjacentListOfShortEdgesOfVertex;
        this.listOfShortEdgesOfLongEdge = listOfShortEdgesOfLongEdge;
        this.listOfVerticesInLayer = listOfVerticesInLayer;
        this.layerOfVertex = layerOfVertex;
        this.numVerticesInLayer = this.listOfVerticesInLayer.get(0).size();
        this.adjacentSetOfShortEdgesOfVertex = generateAdjacentSet(this.adjacentListOfShortEdgesOfVertex);
        this.adjacentListOfDummyVerticesOfVertex = generateListOfAdjacentDummyVertices();
        this.listOfShortEdgesInLayer = generateListOfShortEdgesInLayer();
        this.listOfRealVerticesWithLongEdges = generateListOfRealVerticesWithLongEdges();
        this.listOfRealVerticesWithShortEdges = generateListOfRealVerticesWithShortEdges();
        this.adjacentVerticesWithLongEdgesOfLongEdge = generateAdjacentVerticesOfLongEdge();
        this.setProperty("numV", totalNumVertices);
        this.setProperty("numE", numEdges);
        this.setProperty("numL", numberOfLayers);
        this.setProperty("numLE", numRealLongEdges);
    }

    private HashMap<Edge, HashSet<Integer>> generateAdjacentVerticesOfLongEdge() {
        HashMap<Edge, HashSet<Integer>> adjacentVerticesOfLongEdge = new HashMap<>();
        for (Edge longEdge : this.listOfLongEdges) {
            HashSet<Integer> adjacentVertices = new HashSet<>();
            var source = longEdge.source();
            var target = longEdge.target();
            for (Integer vertex : getAdjacentShortVertexListOfVertex(source, TYPE_OF_ADJACENT_VERTEX.ALL)) {
                if(this.hasLongEdge(vertex)){
                    adjacentVertices.add(vertex);
                }
            }
            for (Integer vertex : getAdjacentShortVertexListOfVertex(target, TYPE_OF_ADJACENT_VERTEX.ALL)) {
                if(this.hasLongEdge(vertex)){
                    adjacentVertices.add(vertex);
                }
            }
            adjacentVerticesOfLongEdge.put(longEdge, adjacentVertices);
        }
        return adjacentVerticesOfLongEdge;
    }

    private HashSet<Integer> generateListOfRealVerticesWithLongEdges() {
        HashSet<Integer> listOfRealVerticesWithLongEdges = new HashSet<>();
        for (Edge longEdge : this.listOfLongEdges) {
            listOfRealVerticesWithLongEdges.add(longEdge.source);
            listOfRealVerticesWithLongEdges.add(longEdge.target);
        }
        return listOfRealVerticesWithLongEdges;
    }

    private HashSet<Integer> generateListOfRealVerticesWithShortEdges() {
        HashSet<Integer> listOfRealVerticesWithShortEdges = new HashSet<>();
        for (Edge longEdge : this.getListOfShortEdges()) {
            listOfRealVerticesWithShortEdges.add(longEdge.source);
            listOfRealVerticesWithShortEdges.add(longEdge.target);
        }
        return listOfRealVerticesWithShortEdges;
    }

    private HashMap<Integer, ArrayList<Edge>> generateListOfShortEdgesInLayer() {
        HashMap<Integer, ArrayList<Edge>> listOfShortEdgesInLayer = new HashMap<>();
        for (int h = 0; h < this.numberOfLayers; h++) {
            listOfShortEdgesInLayer.put(h, new ArrayList<>());
        }
        for (Edge e : this.listOfShortEdges) {
            listOfShortEdgesInLayer.get(this.layerOfVertex[e.source()]).add(e);
        }
        return listOfShortEdgesInLayer;
    }

    private HashMap<Integer, Triplet<ArrayList<Integer>, ArrayList<Integer>, ArrayList<Integer>>> generateListOfAdjacentDummyVertices() {
        HashMap<Integer, Triplet<ArrayList<Integer>, ArrayList<Integer>, ArrayList<Integer>>> hashMap = new HashMap<>(this.numRealLongEdges);
        for (Map.Entry<Edge, ArrayList<Edge>> entry : listOfShortEdgesOfLongEdge.entrySet()) {
            var longEdge = entry.getKey();
            var listOfEdges = entry.getValue();
            hashMap.putIfAbsent(longEdge.source(), new Triplet<>(new ArrayList<Integer>(this.numVerticesInLayer), new ArrayList<Integer>(this.numVerticesInLayer), new ArrayList<Integer>(this.numVerticesInLayer)));
            var sourceEntry = hashMap.get(longEdge.source());
            sourceEntry.getValue0().add(listOfEdges.get(0).target());
            sourceEntry.getValue2().add(listOfEdges.get(0).target());
            hashMap.putIfAbsent(longEdge.target(), new Triplet<>(new ArrayList<Integer>(this.numVerticesInLayer), new ArrayList<Integer>(this.numVerticesInLayer), new ArrayList<Integer>(this.numVerticesInLayer)));
            var targetEntry = hashMap.get(longEdge.target());
            targetEntry.getValue1().add(listOfEdges.get(listOfEdges.size() - 1).source());
            targetEntry.getValue2().add(listOfEdges.get(listOfEdges.size() - 1).source());
        }
        return hashMap;
    }

    private HashMap<Integer, Triplet<HashSet<Integer>, HashSet<Integer>, HashSet<Integer>>> generateAdjacentSet(HashMap<Integer, Triplet<ArrayList<Integer>, ArrayList<Integer>, ArrayList<Integer>>> adjacentShortVertexListOfVertex) {
        HashMap<Integer, Triplet<HashSet<Integer>, HashSet<Integer>, HashSet<Integer>>> as = new HashMap<>(adjacentShortVertexListOfVertex.size());
        for (Map.Entry<Integer, Triplet<ArrayList<Integer>, ArrayList<Integer>, ArrayList<Integer>>> tripletEntry : adjacentShortVertexListOfVertex.entrySet()) {
            var listSource = tripletEntry.getValue().getValue0();
            var listTarget = tripletEntry.getValue().getValue1();
            var listAll = tripletEntry.getValue().getValue2();
            as.put(tripletEntry.getKey(), new Triplet<>(new HashSet<>(listSource), new HashSet<>(listTarget), new HashSet<>(listAll)));
        }
        return as;
    }

    /**
     * Get total number of vertices of the graph
     *
     * @return the total number of vertices of the graph
     */
    public int TotalNumVertices() {
        return totalNumVertices;
    }

    /**
     * Get the number of real vertices
     *
     * @return number of real vertices
     */
    public int NumRealVertices() {
        return numRealVertices;
    }

    /**
     * Get the number of dummy vertices
     *
     * @return the number of dummy vertices
     */
    public int NumDummyVertices() {
        return numDummyVertices;
    }


    /**
     * Get the number of edges
     *
     * @return the number of edges
     */
    public int NumEdges() {
        return numEdges;
    }

    /**
     * Get the number of real edges
     *
     * @return the number of real edges
     */
    public int NumRealEdges() {
        return numRealEdges;
    }

    /**
     * Get the number of real long edges
     *
     * @return the number of real long edges
     */
    public int NumRealLongEdges() {
        return numRealLongEdges;
    }

    /**
     * Get the number of dummy edges
     *
     * @return the number of dummy edges
     */
    public int NumDummyEdges() {
        return numDummyEdges;
    }

    /**
     * Get the number of layers
     *
     * @return the number of layers
     */
    public int NumberOfLayers() {
        return numberOfLayers;
    }

    /**
     * Get the list of all vertices of the graph
     *
     * @return the list of vertices of the graph
     */
    public HashSet<Integer> getTotalListOfVertices() {
        return totalListOfVertices;
    }

    /**
     * Get the list of real vertices of the graph
     *
     * @return the list of real vertices
     */
    public HashSet<Integer> getListOfRealVertices() {
        return listOfRealVertices;
    }

    /**
     * Get the list of dummy vertices
     *
     * @return the list of dummy vertices
     */
    public HashSet<Integer> getListOfDummyVertices() {
        return listOfDummyVertices;
    }

    /**
     * Get list of short edges
     *
     * @return list of short edges
     */
    public ArrayList<Edge> getListOfShortEdges() {
        return listOfShortEdges;
    }


    /**
     * Get list of long edges
     *
     * @return list of long edges
     */
    public ArrayList<Edge> getListOfLongEdges() {
        return listOfLongEdges;
    }

    /**
     * Get list of short edges given a long edge
     *
     * @return list of short edges
     */
    public ArrayList<Edge> getListOfShortEdgesOfLongEdge(Edge longEdge) {
        return listOfShortEdgesOfLongEdge.get(longEdge);
    }

    /**
     * Get set of all adjacent vertices to a long edge, i.e. the vertices adjacent to the source and target of the long edge
     *
     * @return set of all adjacent vertices to a long edge
     */
    public HashSet<Integer> getSetOfAdjacentVerticesWithLongEdgesOfLongEdge(Edge longEdge) {
        return this.adjacentVerticesWithLongEdgesOfLongEdge.get(longEdge);
    }

    /**
     * Return the list of vertices in a given layer
     *
     * @return list of vertices
     */
    public ArrayList<Integer> getListOfVerticesInLayer(int layer) {
        return listOfVerticesInLayer.get(layer);
    }

    /**
     * Get the layer of a vertex
     *
     * @param vertex vertex
     * @return layer
     */
    public int getLayerOfVertex(int vertex) {
        return layerOfVertex[vertex];
    }


    /**
     * Get the list of short edges that emerge from a given a layer
     *
     * @param  layer layer
     * @return list of short edges of a layer
     */
    public ArrayList<Edge> shortEdgesOfLayer(int layer) {
        return this.listOfShortEdgesInLayer.get(layer);
    }

    /**
     * Check if a vertex is dummy
     * @param vertex vertex
     * @return true if the vertex is dummy
     */
    public boolean isDummy(int vertex) {
        return this.listOfDummyVertices.contains(vertex);
    }

    /**
     * Enumerate use to query a specific set of vertices
     *  A -> B -> C
     *  Source of B will return C
     *  Target of B will return A
     *
     */
    public enum TYPE_OF_ADJACENT_VERTEX {
        SOURCE, TARGET, ALL
    }

    /**
     * Get list of adjacent vertices given a vertex and a type
     *
     * @param vertex vertex
     * @param type   source, target or all
     * @return list of vertices
     */
    public ArrayList<Integer> getAdjacentShortVertexListOfVertex(int vertex, TYPE_OF_ADJACENT_VERTEX type) {
        return switch (type) {
            case SOURCE -> adjacentListOfShortEdgesOfVertex.get(vertex).getValue0();
            case TARGET -> adjacentListOfShortEdgesOfVertex.get(vertex).getValue1();
            case ALL -> adjacentListOfShortEdgesOfVertex.get(vertex).getValue2();
        };
    }


    /**
     * Get list of adjacent vertices given a vertex and a type
     *
     * @param vertex vertex
     * @param type   source, target or all
     * @return list of vertices
     */
    public ArrayList<Integer> getAdjacentDummyVerticesOfLongEdgeOfVertex(int vertex, TYPE_OF_ADJACENT_VERTEX type) {
        return switch (type) {
            case SOURCE -> adjacentListOfDummyVerticesOfVertex.get(vertex).getValue0();
            case TARGET -> adjacentListOfDummyVerticesOfVertex.get(vertex).getValue1();
            case ALL -> adjacentListOfDummyVerticesOfVertex.get(vertex).getValue2();
        };
    }

    public int NumVerticesInLayer() {
        return numVerticesInLayer;
    }


    /**
     * Check if two vertices are adjacent
     *
     * @param u vertex
     * @param v vertex
     * @return true if are adjacent, false in other case
     */
    public boolean areAdjacent(int u, int v, TYPE_OF_ADJACENT_VERTEX type) {
        return switch (type) {
            case SOURCE -> this.adjacentSetOfShortEdgesOfVertex.get(u).getValue0().contains(v);
            case TARGET -> this.adjacentSetOfShortEdgesOfVertex.get(u).getValue1().contains(v);
            case ALL -> this.adjacentSetOfShortEdgesOfVertex.get(u).getValue2().contains(v);
        };
    }

    /**
     * Check if two vertices are adjacent
     *
     * @param u vertex
     * @param v vertex
     * @return true if are adjacent, false in other case
     */
    public boolean areAdjacent(int u, int v) {
        return this.adjacentSetOfShortEdgesOfVertex.get(u).getValue2().contains(v);
    }


    public HashSet<Integer> getListOfRealVerticesWithShortEdges() {
        return this.listOfRealVerticesWithShortEdges;
    }

    /**
     * Check if two vertices are in the same layer
     *
     * @param u vertex
     * @param v vertex
     * @return true if both vertices are in the same layer, false in other case
     */
    public boolean areInSameLayer(int u, int v) {
        return this.getLayerOfVertex(u) == this.getLayerOfVertex(v);
    }

    /**
     * Check if a vertex has a long edge
     * @param vertex vertex
     * @return true if the vertex has a long edge, false in other case
     */
    public boolean hasLongEdge (int vertex) {
        return this.listOfRealVerticesWithLongEdges.contains(vertex);
    }

    /**
     * This class represent a directed edge
     */
    public record Edge(int source, int target) {

        /**
         * Constructor of an edge.
         *
         * @param source vertex
         * @param target vertex
         */
        public Edge(int source, int target) {
            this.source = source;
            this.target = target;
        }

        /**
         * Given a vertex of an edge, it returns the other one
         *
         * @param vertex vertex
         * @return the other vertex
         */
        public int getOther(int vertex) {
            return this.source == vertex ? this.target : this.source;
        }


        /**
         * String representation of an edge
         *
         * @return string
         */
        @Override
        public String toString() {
            return "(" + source + "," + target + ')';
        }


        public String toEdgeDot() {
            return source + "->{" + target + '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Edge edge = (Edge) o;

            if (source != edge.source) return false;
            return target == edge.target;
        }

        @Override
        public int hashCode() {
            int result = source;
            result = 31 * result + target;
            return result;
        }
    }

}

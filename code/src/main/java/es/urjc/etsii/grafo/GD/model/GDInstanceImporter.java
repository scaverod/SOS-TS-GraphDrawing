package es.urjc.etsii.grafo.GD.model;

import es.urjc.etsii.grafo.io.InstanceImporter;
import org.javatuples.Triplet;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class GDInstanceImporter extends InstanceImporter<GDInstance> {

    @Override
    public GDInstance importInstance(BufferedReader reader, String filename) throws IOException {

        int totalNumVertices;
        int numRealVertices;
        int numDummyVertices;
        int numEdges;
        int numRealEdges;
        int numRealLongEdges;
        int numDummyEdges;
        int numberOfLayers;
        HashSet<Integer> totalListOfVertices;
        HashSet<Integer> listOfRealVertices;
        HashSet<Integer> listOfDummyVertices;
        ArrayList<GDInstance.Edge> listOfShortEdges;
        ArrayList<GDInstance.Edge> listOfLongEdges;
        HashMap<Integer, Triplet<ArrayList<Integer>, ArrayList<Integer>, ArrayList<Integer>>> adjacentShortVertexListOfVertex;
        HashMap<GDInstance.Edge, ArrayList<GDInstance.Edge>> listOfShortEdgesOfLongEdge;
        HashMap<Integer, ArrayList<Integer>> listOfVerticesInLayer;
        int[] layerOfVertex;


        String line = reader.readLine();
        String[] splitLine = line.split(" ");
        totalNumVertices = Integer.parseInt(splitLine[0]);
        numEdges = Integer.parseInt(splitLine[1]);
        numberOfLayers = Integer.parseInt(splitLine[2]);
        numRealLongEdges = Integer.parseInt(splitLine[3]); // Number of long edges


        totalListOfVertices = new HashSet<>(totalNumVertices);
        listOfRealVertices = new HashSet<>(totalNumVertices);
        listOfShortEdges = new ArrayList<>(numEdges);
        adjacentShortVertexListOfVertex = new HashMap<>(numEdges);
        for (int e = 0; e < numEdges; e++) {
            line = reader.readLine();
            splitLine = line.split(" ");
            GDInstance.Edge edge = new GDInstance.Edge(Integer.parseInt(splitLine[0]) - 1, Integer.parseInt(splitLine[1]) - 1);
            addEdgeToSet(totalListOfVertices, edge);
            addEdgeToSet(listOfRealVertices,edge);
            listOfShortEdges.add(edge);
            updateAdjacentListOfVertex(adjacentShortVertexListOfVertex, edge);
        }



        listOfVerticesInLayer = new HashMap<>(numberOfLayers);
        layerOfVertex = new int[totalNumVertices];
        for (int layer = 0; layer < numberOfLayers; layer++) {
            line = reader.readLine();
            splitLine = line.split(" ");
            for (String v : splitLine) {
                int vertex = Integer.parseInt(v) - 1;
                updateLayers(listOfVerticesInLayer, layerOfVertex, layer, vertex);
            }
        }


        listOfDummyVertices = new HashSet<>();
        listOfLongEdges = new ArrayList<>(numRealLongEdges);
        listOfShortEdgesOfLongEdge = new HashMap<>(numRealLongEdges);
        numRealEdges = numEdges;
        numDummyEdges = 0;
        for (int e = 0; e < numRealLongEdges; e++) {
            line = reader.readLine();
            splitLine = line.split(" ");
            GDInstance.Edge edge = new GDInstance.Edge(Integer.parseInt(splitLine[0]) - 1, Integer.parseInt(splitLine[splitLine.length - 1]) - 1);
            numRealEdges++;
            listOfLongEdges.add(edge);
            listOfShortEdgesOfLongEdge.put(edge, new ArrayList<>());
            for (int v = 0; v < splitLine.length - 1; v++) {
                GDInstance.Edge dEdge = new GDInstance.Edge(Integer.parseInt(splitLine[v]) - 1, Integer.parseInt(splitLine[v + 1]) - 1);
                listOfDummyVertices.add(dEdge.target());
                listOfShortEdgesOfLongEdge.get(edge).add(dEdge);
                numDummyEdges++;
            }
            listOfDummyVertices.remove(edge.target());
        }
        listOfRealVertices.removeAll(listOfDummyVertices);

        numRealVertices = listOfRealVertices.size();
        numDummyVertices = listOfDummyVertices.size();
        numRealEdges = numRealEdges - numDummyEdges;

//        var info = (totalNumVertices + " " + numEdges + " " + numberOfLayers + " " + numRealLongEdges);
//        System.out.println(info);

        return new GDInstance(filename, totalNumVertices, numRealVertices, numDummyVertices, numEdges, numRealEdges, numRealLongEdges, numDummyEdges, numberOfLayers,
                totalListOfVertices, listOfRealVertices, listOfDummyVertices, listOfShortEdges, listOfLongEdges, adjacentShortVertexListOfVertex, listOfShortEdgesOfLongEdge,
                listOfVerticesInLayer, layerOfVertex);
    }

    private void addEdgeToSet(HashSet<Integer> totalListOfVertices, GDInstance.Edge edge) {
        totalListOfVertices.add(edge.source());
        totalListOfVertices.add(edge.target());
    }

    private void updateAdjacentListOfVertex(HashMap<Integer, Triplet<ArrayList<Integer>, ArrayList<Integer>, ArrayList<Integer>>> adjacentListOfVertex, GDInstance.Edge edge) {
        int source = edge.source();
        int target = edge.target();
        adjacentListOfVertex.computeIfAbsent(source, k -> new Triplet<>(new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
        adjacentListOfVertex.get(source).getValue0().add(target);
        adjacentListOfVertex.get(source).getValue2().add(target);
        adjacentListOfVertex.computeIfAbsent(target, k -> new Triplet<>(new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
        adjacentListOfVertex.get(target).getValue1().add(source);
        adjacentListOfVertex.get(target).getValue2().add(source);
    }

    private Triplet<ArrayList<GDInstance.Edge>, ArrayList<GDInstance.Edge>, ArrayList<GDInstance.Edge>> getListOfEdges(ArrayList<GDInstance.Edge> edgeList, ArrayList<GDInstance.Edge> longEdgeList) {
        HashSet<GDInstance.Edge> setOfLongEdges = new HashSet<>(longEdgeList);
        ArrayList<GDInstance.Edge> shortEdgeList = new ArrayList<>(edgeList.size() - longEdgeList.size());
        for (GDInstance.Edge edge : edgeList) {
            if (!setOfLongEdges.contains(edge)) {
                shortEdgeList.add(edge);
            }
        }
        return new Triplet<>(shortEdgeList, longEdgeList, edgeList);
    }

    private void updateLayers(HashMap<Integer, ArrayList<Integer>> vertexInLayer, int[] layerOfVertex, int layer, int vertex) {
        vertexInLayer.computeIfAbsent(layer, k -> new ArrayList<>());
        vertexInLayer.get(layer).add(vertex);
        layerOfVertex[vertex] = layer;
    }



}

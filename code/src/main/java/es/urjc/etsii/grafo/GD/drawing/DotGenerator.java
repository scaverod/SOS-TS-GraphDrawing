package es.urjc.etsii.grafo.GD.drawing;

import es.urjc.etsii.grafo.GD.model.GDInstance;
import es.urjc.etsii.grafo.GD.model.GDSolution;
import es.urjc.etsii.grafo.solver.services.events.MemoryEventStorage;
import es.urjc.etsii.grafo.solver.services.events.types.SolutionGeneratedEvent;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;

/**
 * This class generates a DOT file that can display both the
 * candidate graph and the candidate graph embedded in a cycle.
 */
@RestController
public class DotGenerator {

    static String COLOR_DUMMY_EDGE = "#E6AA68";
    static String COLOR_DUMMY_NODE = "#ECE4B7";
    static String COLOR_EDGE = "#2c497f";
    static String COLOR_NODE = "#808a9f";
    static boolean SHOW_DUMMY_EDGES = false;

    /**
     * Common features for all files.
     * Layout is set as "neato", other layout as "dot", "fdp", "sfdp" "twopi" or "circo" can be selected.
     * More information: https://www.graphviz.org
     */
    private static final String header = """
            digraph G {
              layout="neato"
              node[style=filled fillcolor="white", fixedsize=true, shape=circle]""";

    /**
     * Common feature for all files.
     */
    private static final String footer = "\n}";

    /**
     * Generate a dot file of a candidate graph embedded in a cycle with an specific layout.
     *
     * @param solution candidate graph to be embedded
     * @param path     path where the generated dot file will be located
     */
    public static void generateDot(GDSolution solution, String path) {
        Writer.write(path, solution.getInstance().getId(),
                "-ordered.dot",
                Arrays.asList(header, generateDotGrid(solution), generateDotEdges(solution), footer));

    }

    protected static String generateDotDiagram(GDSolution solution) {
        return String.join("\n", header, generateDotGrid(solution), generateDotEdges(solution), footer);
    }

    /**
     * Generate edges in DOT language
     *
     * @param solution candidate graph to be embedded
     * @return string of the edges to be added to the dot file
     */
    private static String generateDotEdges(GDSolution solution) {
        var instance = solution.getInstance();
        StringBuilder edges = new StringBuilder();
        for (GDInstance.Edge edge : instance.getListOfShortEdges()) {
            edges.append(edge.source()+1)
                    .append("->{")
                    .append(edge.target()+1)
                    .append('}')
                    .append(instance.isDummy(edge.target()) || instance.isDummy(edge.source()) ? "[penwidth=3 color=\""+COLOR_DUMMY_EDGE+"\"]" : "[penwidth=3 color=\""+COLOR_EDGE+"\"]")
                    .append("\n");
        }
        return edges.toString();
    }


    private static String generateDotGrid(GDSolution solution) {
        StringBuilder grid = new StringBuilder();
        int scale = solution.getInstance().NumVerticesInLayer() / 4;
        scale = scale == 0 ? 1 : scale;
        for (int i = 1; i < solution.getInstance().TotalNumVertices(); i++) {
            grid.append((i + 1))
                    .append("[label=\"")
                    .append(i + 1)
                    .append("\", pos=\"")
                    .append(solution.getPositionOfInputVertex(i) * scale)
                    .append(",")
                    .append(scale * solution.getInstance().NumberOfLayers() - solution.getLayerOfInputVertex(i) * scale)
                    .append("!\", shape = \"circle\",")
                    .append(solution.getInstance().isDummy(i) ? "fillcolor=\""+COLOR_DUMMY_NODE+"\"" : "fillcolor=\""+COLOR_NODE+"\",")
                    .append(solution.getInstance().isDummy(i) ? "color=\""+COLOR_DUMMY_EDGE+"\"" : "color=\""+COLOR_EDGE+"\"")
                    .append("];\n");
        }
        int i = 0;
        grid.append((i + 1))
                .append("[label=\"")
                .append(i + 1)
                .append("\", pos=\"")
                .append(solution.getPositionOfInputVertex(i) * scale)
                .append(",")
                .append(scale * solution.getInstance().NumberOfLayers() - solution.getLayerOfInputVertex(i) * scale)
                .append("!\", shape = \"circle\",")
                .append(solution.getInstance().isDummy(i) ? "fillcolor=\""+COLOR_DUMMY_NODE+"\"" : "fillcolor=\""+COLOR_NODE+"\",")
                .append(solution.getInstance().isDummy(i) ? "color=\""+COLOR_DUMMY_EDGE+"\"" : "color=\""+COLOR_EDGE+"\"")
                .append("];\n");
        return grid.toString();
    }

    @Autowired
    private MemoryEventStorage eventStorage;

    @GetMapping("/api/generategraph/{eventId}")
    public String getSolutionAsDotString(@PathVariable int eventId) throws IOException {
        var event = (SolutionGeneratedEvent) eventStorage.getEvents(eventId, eventId + 1).get(0);
        var solution = (GDSolution) event.getSolution().get();
        var viz = Graphviz.fromString(generateDotDiagram(solution));
        BufferedImage image = viz.render(Format.PNG).toImage();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        byte[] bytes = baos.toByteArray();
        return new String(Base64.getEncoder().encode(bytes));
    }

}

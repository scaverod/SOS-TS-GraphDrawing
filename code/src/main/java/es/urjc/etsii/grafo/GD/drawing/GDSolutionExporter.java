package es.urjc.etsii.grafo.GD.drawing;

import es.urjc.etsii.grafo.GD.model.GDInstance;
import es.urjc.etsii.grafo.GD.model.GDSolution;
import es.urjc.etsii.grafo.io.serializers.SolutionSerializer;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class GDSolutionExporter extends SolutionSerializer<GDSolution, GDInstance> {
    /**
     * Create a new solution serializer with the given config
     *
     * @param config configuration
     */
    public GDSolutionExporter(GDSerializerConfig config) {
        super(config);
    }

    @Override
    public void export(File f, GDSolution solution) {
        f = new File(f.getAbsolutePath() + "_FO_" + solution.getScore() + ".png");
        var viz = Graphviz.fromString(DotGenerator.generateDotDiagram(solution));
        BufferedImage image = viz.render(Format.PNG).toImage();
        try (var os = new FileOutputStream(f)) {
            ImageIO.write(image, "png", os);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void export(BufferedWriter writer, GDSolution cMinSaSolution) throws IOException {

    }

}

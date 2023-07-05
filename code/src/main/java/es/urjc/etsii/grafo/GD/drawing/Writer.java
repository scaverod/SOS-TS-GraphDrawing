package es.urjc.etsii.grafo.GD.drawing;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * This class creates a text file in a certain location.
 */
public class Writer {

    /**
     * It creates a file with a specific name, an extension, a set of lines to be written and the path of the location of the file.
     *
     * @param path      is the location of the file
     * @param name      of the file
     * @param extension of the file
     * @param lines     to be written in the file
     */
    public static void write(String path, String name, String extension, List<String> lines) {
        Path file = Paths.get(path + name + extension);
        try {
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

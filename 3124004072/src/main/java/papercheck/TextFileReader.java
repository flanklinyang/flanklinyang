package papercheck;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Reads only the two input files passed by the command line.
 */
final class TextFileReader {

    private TextFileReader() {
    }

    static String read(Path path) throws IOException {
        Path normalizedPath = path.toAbsolutePath().normalize();
        if (Files.isDirectory(normalizedPath)) {
            throw new IllegalArgumentException("input path is a directory: " + normalizedPath);
        }
        if (!Files.isRegularFile(normalizedPath)) {
            throw new IOException("input file does not exist: " + normalizedPath);
        }
        String content = new String(Files.readAllBytes(normalizedPath), StandardCharsets.UTF_8);
        if (!content.isEmpty() && content.charAt(0) == '\uFEFF') {
            content = content.substring(1);
        }
        return content.replace("\r\n", "\n").trim();
    }
}

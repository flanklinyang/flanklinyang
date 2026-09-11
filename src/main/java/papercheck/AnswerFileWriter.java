package papercheck;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;

/**
 * Writes the similarity score with exactly two decimal places.
 */
final class AnswerFileWriter {

    private AnswerFileWriter() {
    }

    static void write(Path path, double score) throws IOException {
        Path outputPath = path.toAbsolutePath().normalize();
        Path parent = outputPath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        String answer = String.format(Locale.ROOT, "%.2f", score);
        Files.write(
                outputPath,
                answer.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
        );
    }
}

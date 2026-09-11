package papercheck;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Handles argument validation, file I/O and user-facing error messages.
 */
public final class PlagiarismCheckerApp {

    public int run(String[] args) {
        if (args == null || args.length != 3) {
            printUsage();
            return 2;
        }

        try {
            Path originalPath = Paths.get(args[0]);
            Path copiedPath = Paths.get(args[1]);
            Path answerPath = Paths.get(args[2]);

            String originalText = TextFileReader.read(originalPath);
            String copiedText = TextFileReader.read(copiedPath);
            double score = new PaperSimilarityScorer().score(originalText, copiedText);

            AnswerFileWriter.write(answerPath, score);
            return 0;
        } catch (InvalidPathException e) {
            printError("invalid path: " + e.getMessage());
            return 1;
        } catch (IllegalArgumentException e) {
            printError(e.getMessage());
            return 1;
        } catch (IOException e) {
            printError("cannot read or write file: " + e.getMessage());
            return 1;
        }
    }

    private void printUsage() {
        System.err.println("Usage: java -jar main.jar <originalFile> <copiedFile> <answerFile>");
    }

    private void printError(String message) {
        System.err.println("Error: " + message);
    }
}

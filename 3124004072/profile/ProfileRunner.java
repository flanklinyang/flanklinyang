import com.jprofiler.api.controller.Controller;
import papercheck.PaperSimilarityScorer;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class ProfileRunner {

    private static final int ITERATIONS = 30;

    private ProfileRunner() {
    }

    public static void main(String[] args) throws Exception {
        String original = new String(Files.readAllBytes(Paths.get(args[0])), StandardCharsets.UTF_8);
        String copied = new String(Files.readAllBytes(Paths.get(args[1])), StandardCharsets.UTF_8);
        PaperSimilarityScorer scorer = new PaperSimilarityScorer();

        Controller.startCPURecording(true);
        for (int i = 0; i < ITERATIONS; i++) {
            scorer.score(original, copied);
        }
        Controller.stopCPURecording();
        Controller.saveSnapshot(new File("jprofiler-cpu.jps"));
    }
}

package papercheck;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;

public class CommandLineRunnerTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private final PlagiarismCheckerApp app = new PlagiarismCheckerApp();

    @Test
    public void writesAnswerWithTwoDecimalPlaces() throws IOException {
        Path original = temporaryFolder.newFile("original.txt").toPath();
        Path copied = temporaryFolder.newFile("copied.txt").toPath();
        Path answer = temporaryFolder.getRoot().toPath().resolve("nested").resolve("answer.txt");
        Files.write(original, "同一个原文文件".getBytes(StandardCharsets.UTF_8));
        Files.write(copied, "同一个原文文件".getBytes(StandardCharsets.UTF_8));

        int exitCode = app.run(new String[]{
                original.toString(),
                copied.toString(),
                answer.toString()
        });

        assertEquals(0, exitCode);
        assertEquals("1.00", new String(Files.readAllBytes(answer), StandardCharsets.UTF_8));
    }

    @Test
    public void tooFewArgumentsReturnUsageExitCode() {
        assertEquals(2, app.run(new String[]{"only-one.txt"}));
    }

    @Test
    public void tooManyArgumentsReturnUsageExitCode() {
        assertEquals(2, app.run(new String[]{"a.txt", "b.txt", "c.txt", "d.txt"}));
    }

    @Test
    public void nullArgumentsReturnUsageExitCode() {
        assertEquals(2, app.run(null));
    }

    @Test
    public void invalidPathReturnsNonZeroExitCode() {
        int exitCode = app.run(new String[]{
                "bad\0path.txt",
                "copied.txt",
                "answer.txt"
        });

        assertEquals(1, exitCode);
    }

    @Test
    public void missingInputFileReturnsNonZeroExitCode() throws IOException {
        Path copied = temporaryFolder.newFile("copied.txt").toPath();
        Path answer = temporaryFolder.newFile("answer.txt").toPath();
        Files.write(copied, "文本".getBytes(StandardCharsets.UTF_8));

        Path missing = temporaryFolder.getRoot().toPath().resolve("missing.txt");
        int exitCode = app.run(new String[]{
                missing.toString(),
                copied.toString(),
                answer.toString()
        });

        assertEquals(1, exitCode);
    }

    @Test
    public void directoryAsInputReturnsNonZeroExitCode() throws IOException {
        Path directory = temporaryFolder.newFolder("not-a-file").toPath();
        Path copied = temporaryFolder.newFile("copied.txt").toPath();
        Path answer = temporaryFolder.newFile("answer.txt").toPath();
        Files.write(copied, "文本".getBytes(StandardCharsets.UTF_8));

        int exitCode = app.run(new String[]{
                directory.toString(),
                copied.toString(),
                answer.toString()
        });

        assertEquals(1, exitCode);
    }

    @Test
    public void outputPathAsDirectoryReturnsNonZeroExitCode() throws IOException {
        Path original = temporaryFolder.newFile("original.txt").toPath();
        Path copied = temporaryFolder.newFile("copied.txt").toPath();
        Path answerDirectory = temporaryFolder.newFolder("answer-dir").toPath();
        Files.write(original, "原文".getBytes(StandardCharsets.UTF_8));
        Files.write(copied, "抄袭文".getBytes(StandardCharsets.UTF_8));

        int exitCode = app.run(new String[]{
                original.toString(),
                copied.toString(),
                answerDirectory.toString()
        });

        assertEquals(1, exitCode);
    }
}

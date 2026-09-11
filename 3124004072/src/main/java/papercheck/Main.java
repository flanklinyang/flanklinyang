package papercheck;

/**
 * Command line entry point required by the assignment:
 * java -jar main.jar original.txt copied.txt answer.txt
 */
public final class Main {


    public static void main(String[] args) {
        int exitCode = new PlagiarismCheckerApp().run(args);
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }
}

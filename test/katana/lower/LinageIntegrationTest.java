package katana.lower;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import katana.oracle.Runs;
import katana.oracle.nist.Cobc;
import katana.oracle.nist.Runner;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * LINAGE through both lanes (J-149, J-151): LINTRIAL's counters, END-OF-PAGE and overflows, IBM's
 * readings in every step, and LINFORM's file, cobc's print-line form for a LINAGE file (the lane's
 * profile); each program's output and every file it writes the same, byte for byte, under cobc
 * -std=ibm and through the Java lane.
 */
@Tag("integration")
class LinageIntegrationTest {
    private static final Path ROOT = Path.of(System.getProperty("katana.root", "."));
    private static final Map<String, List<String>> FILES = Map.of("LINTRIAL", List.of("PAGED", "VARIED"), "LINFORM", List.of("FORMED"));

    @Test
    void bothLanesCountAndWriteThePagesAlike() throws Exception {
        Path work = Files.createDirectories(ROOT.resolve("build/linage"));
        for (var program : FILES.entrySet()) {
            Path cobc = Files.createTempDirectory(work, "cobc"), java = Files.createTempDirectory(work, "java");
            String a = ran(new Cobc(Duration.ofSeconds(30), "ibm"), cobc, program.getKey()), b = ran(new JavaRunner(60), java, program.getKey());
            assertEquals(a, b, program.getKey() + "'s output");
            for (String file : program.getValue()) assertEquals(Files.readString(cobc.resolve(file)), Files.readString(java.resolve(file)), program.getKey() + "'s " + file);
            Runs.passed(cobc, java);
        }
    }

    /** Compiles and runs a program of test/katana/lower/linage in a folder under a runner; what it printed. */
    private static String ran(Runner runner, Path dir, String program) throws Exception {
        Files.copy(ROOT.resolve("test/katana/lower/linage/" + program + ".cbl"), dir.resolve(program + ".cbl"));
        assertEquals(0, runner.program(dir, program + ".cbl").exit(), program + " compiles");
        Runner.Result result = runner.run(dir, program, Map.of(), new byte[0]);
        assertEquals(0, result.exit(), result.output());
        return result.output().lines().filter(l -> !l.contains("_FORTIFY_SOURCE") && !l.contains("previous definition")).reduce("", (x, y) -> x + y + "\n");
    }
}

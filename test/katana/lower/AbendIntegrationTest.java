package katana.lower;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import katana.oracle.nist.Cobc;
import katana.oracle.nist.ModuleRun;
import katana.oracle.nist.Runner;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * LANE-08 with the files: ABENDW writes two records and then opens an absent input file, which ends the
 * run unit. Both lanes must end it for the same reason and leave the same bytes in the file written, the
 * records reaching it because the run unit's end closes open files (PG 6.4 p. 179, TRAP(ON)); a lane
 * that did not close it would agree on the reason and leave the file short.
 */
@Tag("integration")
class AbendIntegrationTest {
    private static final Path ROOT = Path.of(System.getProperty("katana.root", "."));

    @Test
    void bothLanesEndForTheSameReasonAndLeaveTheSameFile() throws Exception {
        Path work = Files.createDirectories(ROOT.resolve("build/abend"));
        Path cobc = ran(new Cobc(Duration.ofSeconds(30), "ibm"), Files.createTempDirectory(work, "cobc"));
        Path java = ran(new JavaRunner(60), Files.createTempDirectory(work, "java"));
        assertArrayEquals("RECORD ONERECORD TWO".getBytes(), Files.readAllBytes(cobc.resolve("ABOUT")));
        assertArrayEquals(Files.readAllBytes(cobc.resolve("ABOUT")), Files.readAllBytes(java.resolve("ABOUT")));
    }

    /** Compiles and runs ABENDW in a folder under a runner: it ends abnormally, for status 35 on ABSENT; the folder. */
    private static Path ran(Runner runner, Path dir) throws Exception {
        Files.copy(ROOT.resolve("test/katana/lower/abend/ABENDW.cbl"), dir.resolve("ABENDW.cbl"));
        assertEquals(0, runner.program(dir, "ABENDW.cbl").exit(), "ABENDW compiles");
        Runner.Result result = runner.run(dir, "ABENDW", Map.of(), new byte[0]);
        assertNotEquals(0, result.exit(), result.output());
        assertEquals("OPEN IN-FILE status 35 on ABSENT", ModuleRun.reason(result.output()), result.output());
        return dir;
    }
}

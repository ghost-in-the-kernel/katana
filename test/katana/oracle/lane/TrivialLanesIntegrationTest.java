package katana.oracle.lane;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import katana.oracle.compare.Compare;
import katana.oracle.compare.Layout;
import katana.oracle.fixture.Fixture;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Phase 1.8's check: a hand-written Java program that copies its input to its output (CopyRecords)
 * passes the trivial fixture through both lanes with an empty diff, the cobc lane's two runs first
 * through the gate. The java lane's stub refuses what waits for rt/.
 */
@Tag("integration")
class TrivialLanesIntegrationTest {
    private static final Path ROOT = LaneRuns.ROOT, TRIVIAL = ROOT.resolve("oracle/fixture/trivial");
    private static final String OUT = "KATANA.TRIVIAL.OUT";

    private static CobcLane.Setup setup(String folder, Map<String, List<String>> java) {
        return new CobcLane.Setup(ROOT, Map.of("repo", ROOT), TRIVIAL.resolve("cbl"), List.of(TRIVIAL.resolve("cpy")),
                ROOT.resolve("build/" + folder), ROOT.resolve("build/lane-cache"), Duration.ofSeconds(60), (j, d) -> null, null, java, false);
    }

    private static final Map<String, List<String>> JAVA = Map.of("COPYREC",
            List.of(Path.of(System.getProperty("java.home"), "bin", "java").toString(), "-cp", System.getProperty("java.class.path"), CopyRecords.class.getName()));

    @Test
    void aJavaCopyAndACobolCopyPassTheTrivialFixtureWithAnEmptyDiff() throws Exception {
        Fixture fixture = Fixture.read(Files.readString(TRIVIAL.resolve("COPY.fixture")));
        Report a = CobcLane.run(fixture, setup("trivial-cobc-a", null)), b = CobcLane.run(fixture, setup("trivial-cobc-b", null));
        Report java = CobcLane.run(fixture, setup("trivial-java", JAVA));
        assertEquals(List.of(), a.findings(), a.text());
        assertEquals(List.of(), java.findings(), java.text());
        Gate.Result gate = Gate.compare(fixture, a, ROOT.resolve("build/trivial-cobc-a"), b, ROOT.resolve("build/trivial-cobc-b"));
        Layout layout = Layout.read(Files.readString(TRIVIAL.resolve("cpy/COPYREC.cpy")));
        Compare.Result diff = Compare.compare(gate,
                Compare.Side.fixed(OUT, Files.readAllBytes(ROOT.resolve("build/trivial-cobc-a/unload/" + OUT)), 20, "ascii"),
                Compare.Side.fixed(OUT, Files.readAllBytes(ROOT.resolve("build/trivial-java/unload/" + OUT)), 20, "ascii"), layout, List.of());
        assertEquals(List.of(), diff.findings());
        assertTrue(diff.same());
        assertEquals("COPIED 00005 RECORDS\n", Files.readString(ROOT.resolve("build/trivial-cobc-a/sysout/COPY.stdout")));
        assertEquals(Files.readString(ROOT.resolve("build/trivial-cobc-a/sysout/COPY.stdout")), Files.readString(ROOT.resolve("build/trivial-java/sysout/COPY.stdout")));
        assertEquals("COPYREC rc=0", java.steps().get("COPY"));
    }

    @Test
    void theStubRefusesWhatWaitsForTheRuntime() throws Exception {
        Fixture posttran = Fixture.read(Files.readString(ROOT.resolve("oracle/fixture/carddemo/POSTTRAN.fixture")));
        Report report = CobcLane.run(posttran, setup("trivial-java-posttran", JAVA));
        assertEquals(List.of("the java lane's stub has no stub CEE3ABD yet (rt/, phase 3)"), report.findings());
    }
}

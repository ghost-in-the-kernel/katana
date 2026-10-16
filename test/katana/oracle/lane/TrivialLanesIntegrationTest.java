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
                Compare.Side.fixed(OUT, Files.readAllBytes(ROOT.resolve("build/trivial-java/unload/" + OUT)), 20, "ascii"), layout, List.of(), Compare.Numproc.NOPFD);
        assertEquals(List.of(), diff.findings());
        assertTrue(diff.same());
        assertEquals("COPIED 00005 RECORDS\n", Files.readString(ROOT.resolve("build/trivial-cobc-a/sysout/COPY.stdout")));
        assertEquals(Files.readString(ROOT.resolve("build/trivial-cobc-a/sysout/COPY.stdout")), Files.readString(ROOT.resolve("build/trivial-java/sysout/COPY.stdout")));
        assertEquals("COPYREC rc=0", java.steps().get("COPY"));
    }

    /** The java lane refuses what it does not run yet (LANE-05): a called module, and an indexed dataset with no `rt/Ksds` command (LANE-16). */
    @Test
    void theJavaLaneRefusesWhatWaitsForTheRuntime() throws Exception {
        Fixture posttran = Fixture.read(Files.readString(ROOT.resolve("oracle/fixture/carddemo/POSTTRAN.fixture")));
        Report report = CobcLane.run(posttran, setup("trivial-java-posttran", JAVA));
        assertEquals(List.of("the java lane's setup has no *KSDS command for the indexed dataset AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS (LANE-16)"), report.findings());
        Fixture creastmt = Fixture.read(Files.readString(ROOT.resolve("oracle/fixture/carddemo/CREASTMT.fixture")));
        assertEquals(List.of("the java lane has no called module CBSTM03B yet (CALL, phase 4.3)"), CobcLane.run(creastmt, setup("trivial-java-creastmt", JAVA)).findings());
    }

    /** No COB_VARSEQ_FORMAT is IBM's RDW (J-062), so a RECFM=V DD stops both lanes before a step runs (LANE-02). */
    @Test
    void aVariableDatasetStopsBothLanes() throws Exception {
        String text = Files.readString(TRIVIAL.resolve("COPY.fixture")).replace("dd OUTFILE dsn=KATANA.TRIVIAL.OUT disp=NEW,CATLG,DELETE recfm=F", "dd OUTFILE dsn=KATANA.TRIVIAL.OUT disp=NEW,CATLG,DELETE recfm=VB");
        for (Map<String, List<String>> java : java.util.Arrays.asList(null, JAVA)) {
            Report report = CobcLane.run(Fixture.read(text), setup(java == null ? "trivial-v-cobc" : "trivial-v-java", java));
            assertEquals(List.of("dd OUTFILE in COPY is RECFM=VB, not in the lanes yet (LANE-02)"), report.findings(), report.text());
            assertTrue(report.steps().values().stream().noneMatch(o -> o.contains("rc=0")), report.text());
        }
    }
}

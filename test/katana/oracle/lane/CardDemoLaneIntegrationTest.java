package katana.oracle.lane;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import katana.oracle.fixture.Fixture;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Phase 1.4's check (R-048): the CardDemo fixtures through the cobc lane. POSTTRAN reproduces the
 * study's frozen TRANFILE unload (F-02); INTCALC runs through the PARM driver; TRANREPT-STUDY and
 * CREASTMT run every step before their SORT. TRANREPT as the JCL writes it stops earlier, at the
 * REPROCT member's comment in column 1 (J-035). Every report is held by
 * oracle/lane/carddemo.golden (`-Dkatana.record=true` rewrites it). Needs cobc and `corpus/fetch.sh carddemo`.
 */
@Tag("integration")
class CardDemoLaneIntegrationTest {
    private static final Path ROOT = Path.of(System.getProperty("katana.root", "."));
    private static final List<String> JOBS = List.of("POSTTRAN", "TRANREPT", "TRANREPT-STUDY", "INTCALC", "CREASTMT");
    private final Map<String, Report> reports = new HashMap<>();

    /** A CardDemo fixture through the lane in build/lane/JOB, once per test instance. */
    private Report run(String job) {
        Report done = reports.get(job);
        if (done != null) return done;
        try {
            Fixture fixture = Fixture.read(Files.readString(ROOT.resolve("oracle/fixture/carddemo/" + job + ".fixture")));
            Path carddemo = ROOT.resolve("corpus/carddemo");
            CobcLane.Setup setup = new CobcLane.Setup(ROOT, Map.of("carddemo", carddemo, "repo", ROOT), carddemo.resolve("app/cbl"),
                    List.of(carddemo.resolve("app/cpy")), ROOT.resolve("build/lane/" + job), ROOT.resolve("build/lane-cache"),
                    Duration.ofSeconds(60), this::unloaded);
            Report report = CobcLane.run(fixture, setup);
            reports.put(job, report);
            return report;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** A dataset as another fixture's job leaves it: that job run through the lane, its unload read. */
    private byte[] unloaded(String job, String dsn) {
        run(job);
        try {
            Path file = ROOT.resolve("build/lane/" + job + "/unload/" + dsn);
            return Files.exists(file) ? Files.readAllBytes(file) : null;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Test
    void theCardDemoJobsRunAsTheGoldenSays() throws Exception {
        StringBuilder all = new StringBuilder();
        for (String job : JOBS) all.append("# oracle/fixture/carddemo/").append(job).append(".fixture\n").append(run(job).text()).append('\n');
        System.out.print(all);
        Path golden = ROOT.resolve("oracle/lane/carddemo.golden");
        if (Boolean.getBoolean("katana.record")) Files.writeString(golden, all.toString());
        assertEquals(Files.readString(golden), all.toString());
    }

    @Test
    void posttranReproducesTheFrozenTranfileUnload() {
        Report posttran = run("POSTTRAN");
        assertEquals(List.of(), posttran.findings());
        assertEquals("CBTRN02C rc=4", posttran.steps().get("STEP15"));
        assertEquals("sha256:aed617444207a7ebd2684e589c8569f766fb5d1d8323a25a658f8c4c03020b48",
                posttran.datasets().get("AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS"));
    }

    @Test
    void intcalcRunsThroughTheParmDriver() {
        Report intcalc = run("INTCALC");
        assertEquals(List.of(), intcalc.findings());
        assertTrue(intcalc.steps().get("STEP15").matches("CBACT04C rc=\\d+"), intcalc.text());
    }

    @Test
    void theJobsWithASortRunUpToItAndStopThere() {
        Map<String, String> sorts = Map.of("TRANREPT-STUDY", "STEP05R", "CREASTMT", "STEP010");
        sorts.forEach((job, sort) -> {
            Report report = run(job);
            assertEquals(List.of("step " + sort + ": SORT is phase 1.5 (R-048); the job stops here"), report.findings(), report.text());
            report.steps().forEach((step, outcome) -> {
                if (!step.equals(sort) && !outcome.contains("not run")) assertTrue(outcome.endsWith("rc=0"), job + " " + step + " " + outcome);
            });
        });
    }

    @Test
    void tranreptAsWrittenStopsAtReproctsColumnOneComment() {
        Report tranrept = run("TRANREPT");
        assertEquals(1, tranrept.findings().size(), tranrept.text());
        assertTrue(tranrept.findings().get(0).startsWith("step STEP05R.PRC001: IDCAMS: card 1 opens a comment in column 1"), tranrept.text());
    }
}

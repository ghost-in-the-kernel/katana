package katana.oracle.lane;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.util.List;
import katana.oracle.fixture.Fixture;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Phase 1.6's check: with the clock pinned to hundredths and zone every CardDemo fixture runs the same
 * twice and the gate passes; with POSTTRAN's clock pinned to the second (set outside the fixture,
 * R-009) the gate fails, naming TRANFILE and nothing but its hundredths, columns 325 and 326 (F-02).
 */
@Tag("integration")
class GateIntegrationTest {
    private static final List<String> JOBS = List.of("POSTTRAN", "TRANREPT", "TRANREPT-STUDY", "INTCALC", "CREASTMT", "PRTCATBL", "PRTCATBL-INLINE", "COMBTRAN", "SORTTEST");

    private static Gate.Result twice(String job, LaneRuns first, LaneRuns second) throws Exception {
        Fixture fixture = Fixture.read(Files.readString(LaneRuns.ROOT.resolve("oracle/fixture/carddemo/" + job + ".fixture")));
        return Gate.compare(fixture, first.run(job), first.work(job), second.run(job), second.work(job));
    }

    @Test
    void everyCardDemoFixtureRunsTheSameTwice() throws Exception {
        LaneRuns a = new LaneRuns("gate-a"), b = new LaneRuns("gate-b");
        for (String job : JOBS) {
            Gate.Result result = twice(job, a, b);
            assertEquals(List.of(), result.differences(), job);
            assertTrue(result.identical(), job);
        }
    }

    /**
     * The hundredths of a clock pinned only to the second are live, and land in TRANFILE columns 325 and
     * 326, the same value on every record of a run: two runs differ in 325, in 326 or in both, and about
     * one pair in a hundred agrees by chance, so up to three pairs are run (J-055).
     */
    @Test
    void aClockPinnedToTheSecondFailsTheGateInTranfilesHundredths() throws Exception {
        String seconds = "2024-03-01 12:00:00";
        Gate.Result result = null;
        for (int pair = 1; pair <= 3 && (result == null || result.identical()); pair++) {
            result = twice("POSTTRAN", new LaneRuns("gate-seconds-a" + pair, seconds), new LaneRuns("gate-seconds-b" + pair, seconds));
        }
        System.out.println("gate POSTTRAN, clock to the second: " + result.differences());
        assertFalse(result.identical());
        assertEquals(1, result.differences().size(), result.differences().toString());
        assertTrue(result.differences().get(0).matches("dataset AWS\\.M2\\.CARDDEMO\\.TRANSACT\\.VSAM\\.KSDS \\(DD TRANFILE in STEP15\\): \\d+ of 257 records differ, in (column 325|column 326|columns 325-326)"),
                result.differences().get(0));
    }
}

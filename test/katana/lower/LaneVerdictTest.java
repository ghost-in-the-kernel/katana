package katana.lower;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import katana.oracle.nist.ModuleRun;
import katana.oracle.nist.Report;
import org.junit.jupiter.api.Test;

/**
 * The Java lane's verdict on one run (`NistJavaLaneIntegrationTest.same`), the killer mutation for its
 * floors (R-056, tithe 3a.7): a run whose report differs from cobc's, or that ended in an exception, is
 * never counted the same, so a lowering that broke every program counts 0 and falls below each floor.
 */
class LaneVerdictTest {
    private static ModuleRun.Outcome run(String error, int total, int pass, int fail, int exit, String reason) {
        return new ModuleRun.Outcome("NC999A", error, "", Optional.of(new Report.Counts(total, pass, fail, 0, 0)), exit, reason);
    }

    private static boolean same(ModuleRun.Outcome o, String golden) {
        return NistJavaLaneIntegrationTest.same(o, NistJavaLaneIntegrationTest.rendered(o), golden);
    }

    @Test
    void aRunIsTheSameOnlyWhenItsReportIs() {
        assertTrue(same(run(null, 10, 10, 0, 0, null), "1 ok 10 10 0 0 0"));
        assertFalse(same(run(null, 10, 9, 1, 0, null), "1 ok 10 10 0 0 0"), "one test failing more");
        assertFalse(same(run("lowering: java.lang.IllegalStateException", 10, 10, 0, 0, null), "1 ok 10 10 0 0 0"), "a run that ended in an exception");
    }

    @Test
    void anAbnormalEndingIsTheSameOnlyForTheSameReason() {
        String golden = "1 ok 0 0 0 0 0 exit=1 reason=OPEN_TFIL_status_35_on_TAPE1";
        assertTrue(same(run(null, 0, 0, 0, 1, "OPEN TFIL status 35 on TAPE1"), golden));
        assertFalse(same(run(null, 0, 0, 0, 1, "OPEN TFIL status 37 on TAPE1"), golden), "another status");
        assertFalse(same(run(null, 0, 0, 0, 0, null), golden), "a run that ended normally");
    }
}

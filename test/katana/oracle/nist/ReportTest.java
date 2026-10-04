package katana.oracle.nist;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * The reports under reports/ are excerpts of real runs under cobc in the image (1.1d), and their
 * counts are the study's for the same programs (oracle/prototypes/nist/<module>/report.txt).
 */
class ReportTest {
    private static String report(String name) throws Exception {
        return Files.readString(Path.of(System.getProperty("katana.root", "."), "test/katana/oracle/nist/reports", name + ".txt"));
    }

    @Test
    void allPassed() throws Exception {
        assertEquals(Optional.of(new Report.Counts(93, 93, 0, 0, 0)), Report.read(report("NC101A")));
    }

    @Test
    void someDeleted() throws Exception {
        assertEquals(Optional.of(new Report.Counts(132, 129, 0, 3, 0)), Report.read(report("NC105A")));
    }

    @Test
    void someNeedInspection() throws Exception {
        assertEquals(Optional.of(new Report.Counts(23, 12, 0, 0, 11)), Report.read(report("SQ201M")));
    }

    @Test
    void someFailed() throws Exception {
        // No program failed a test in the study's run; this footer is NC101A's with two failures written in.
        String failed = report("NC101A").replace("093 OF 093", "091 OF 093").replace("NO  TEST(S) FAILED", "002 TEST(S) FAILED");
        assertEquals(Optional.of(new Report.Counts(93, 91, 2, 0, 0)), Report.read(failed));
    }

    @Test
    void aReportWithoutItsWholeFooterHasNoCounts() throws Exception {
        String whole = report("NC101A");
        assertEquals(Optional.empty(), Report.read(whole.substring(0, whole.indexOf("NO  TEST(S) DELETED"))));
        assertEquals(Optional.empty(), Report.read(""));
    }

    @Test
    void formFeedsBetweenPagesDoNotMatter() throws Exception {
        assertEquals(Report.read(report("NC101A")), Report.read(report("NC101A").replace("\n...\n", "\n\f...\n")));
    }
}

package katana.oracle.prove;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import katana.findings.Divergence;
import katana.findings.Finding;
import org.junit.jupiter.api.Test;

/** 8A.6's report on entries small enough to read (research §53): a possible entry, an unattributed one, the findings apart, and a proof whose fixture is missing refused. */
class ReportTest {
    private static final Report.Header HEADER = new Report.Header("P", "oracle/fixture/foreign/ACCRUE.fixture", "0".repeat(64), "java Port", 3);

    @Test
    void possibleAndUnattributedEntriesAndTheFindingsApart() {
        List<Report.Entry> entries = List.of(
                new Report.Entry("proven", "RES", "OUT", List.of(2), "fixture nowhere/X.fixture sha256:" + "1".repeat(64) + "; input IN record 2 sha256:" + "2".repeat(64), "COMPUTE #1 IN CALC",
                        "P.cbl:9", "unaligned", null, 0, 3, null, "MAIN > CALC", "-"),
                new Report.Entry("possible", "TOTAL", null, List.of(), null, "ADD #2 IN CALC", "P.cbl:10", "Port.java:4 (unconfirmed, name TOTAL)", Divergence.Kind.DV_01, 0, 0, null,
                        "not taken across the boundary by this fixture: no TRUNCATED at its paragraph", "// P.cbl:10 CALC ADD"));
        String text = Report.write(HEADER, entries, List.of(new Finding(Finding.Kind.NE_04, "P.cbl:12:12", "DIVIDE A INTO B")), "never covers");
        assertTrue(text.contains("1 divergences proven, 1 possible."), text);
        assertTrue(text.contains("- Cause: unattributed: no probe reproduces these cells"), text);
        assertTrue(text.contains("## 2. possible: TOTAL") && text.contains("- Cause: DV-01 high-order truncation. The rule:"), text);
        assertTrue(text.contains("## No port can be exact here\n\nNE-04 division without ON SIZE ERROR: DIVIDE A INTO B (P.cbl:12:12)"), text);
        assertTrue(text.endsWith("## What it never covers (docs/PROOF.md, verbatim)\n\nnever covers\n"), text);
    }

    @Test
    void aProofWhoseFixtureIsMissingIsRefused() throws Exception {
        String report = "- Proven by: fixture nowhere/X.fixture sha256:" + "1".repeat(64) + "; input IN record 1\n";
        assertEquals(List.of("a proven entry cites nowhere/X.fixture, which does not exist"), Report.refusals(report, Path.of(System.getProperty("katana.root", "."))));
    }
}

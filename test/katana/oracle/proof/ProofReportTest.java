package katana.oracle.proof;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import katana.oracle.fixture.Fixture;
import katana.oracle.lane.Gate;
import katana.oracle.lane.Report;
import org.junit.jupiter.api.Test;

/** PROOF-01 and PROOF-03 (docs/PROOF.md): the report quotes "What it never covers" verbatim, and a failed gate compares nothing. */
class ProofReportTest {
    private static String proof() throws Exception {
        return Files.readString(Path.of(System.getProperty("katana.root", "."), "docs/PROOF.md"));
    }

    private static final Fixture FIXTURE = Fixture.read("job T\n");

    @Test
    void theReportQuotesWhatItNeverCoversVerbatim() throws Exception {
        String section = ProofReport.neverCovers(proof());
        assertTrue(section.startsWith("- Environment configuration:"), section);
        String report = ProofReport.write(FIXTURE, "t.fixture", new Report(), new Gate.Result(true, List.of()), "nothing", List.of(), new Report(), proof(), List.of());
        assertTrue(report.endsWith("## What it never covers (docs/PROOF.md, verbatim)\n\n" + section + "\n"), report);
    }

    @Test
    void aFailedGateComparesNothing() throws Exception {
        String report = ProofReport.write(FIXTURE, "t.fixture", new Report(), new Gate.Result(false, List.of("dataset X differs")), "nothing", List.of(), new Report(), proof(), List.of());
        assertTrue(report.contains("Failed: the cobc lane's two runs differ, so nothing is compared (PROOF-03).\n- dataset X differs"), report);
        assertEquals(-1, report.indexOf("## Compared"));
    }

    /** R-049's mechanism: a program valid only under a hypothesis is said apart from what is not exact. */
    @Test
    void aHypothesisIsSaidApartFromTheFindings() throws Exception {
        var hy = new katana.findings.Finding(katana.findings.Finding.Kind.HY_01, "P.cbl:9:12", "P: A goes to Z, out of A THRU B");
        var ne = new katana.findings.Finding(katana.findings.Finding.Kind.NE_04, "P.cbl:7:12", "P: DIVIDE without ON SIZE ERROR");
        String report = ProofReport.write(FIXTURE, "t.fixture", new Report(), new Gate.Result(true, List.of()), "nothing", List.of(), new Report(), proof(), List.of(hy, ne));
        String findings = report.substring(report.indexOf("## Findings"), report.indexOf("## Valid under hypothesis")), valid = report.substring(report.indexOf("## Valid under hypothesis"));
        org.junit.jupiter.api.Assertions.assertTrue(findings.contains("NE-04") && !findings.contains("HY-01"), findings);
        org.junit.jupiter.api.Assertions.assertTrue(valid.contains("HY-01"), valid);
        org.junit.jupiter.api.Assertions.assertTrue(report.contains("not proven (R-057)"), report);
    }

    /** v1 (research §27): a dataset only the java lane leaves is said, not compared; coverage names where it comes from. */
    @Test
    void v1SaysWhatItCouldNotCompareAndWhereCoverageCameFrom() throws Exception {
        var coverage = new java.util.TreeMap<String, Report.Coverage>(java.util.Map.of("P", new Report.Coverage(1, 2, List.of("ABEND"))));
        String report = ProofReport.v1(FIXTURE, "t.fixture", new Report(), new Gate.Result(true, List.of()), List.of(), List.of("OUT.PS"), coverage,
                java.util.Map.of("P", "the java lane's trace"), proof(), List.of(), List.of("a note"));
        org.junit.jupiter.api.Assertions.assertTrue(report.contains("Not compared, the cobc lane leaving none of them (a step it cannot run, R-027): OUT.PS."), report);
        org.junit.jupiter.api.Assertions.assertTrue(report.contains("| P | 1 of 2 | ABEND | the java lane's trace |") && report.contains("- a note"), report);
        org.junit.jupiter.api.Assertions.assertTrue(report.startsWith("# Proof report v1:"), report);
    }

    /** CardDemo's coverage: a paragraph one fixture entered is entered; only one no fixture entered is listed. */
    @Test
    void coverageTakesWhatAnyFixtureEntered() {
        java.util.Map<String, Report.Coverage> one = java.util.Map.of("P", new Report.Coverage(2, 4, List.of("C", "D"))), two = java.util.Map.of("P", new Report.Coverage(3, 4, List.of("D")));
        var byJob = new java.util.LinkedHashMap<String, java.util.Map<String, Report.Coverage>>();
        byJob.put("ONE", one);
        byJob.put("TWO", two);
        String summary = ProofReport.coverage(byJob);
        org.junit.jupiter.api.Assertions.assertTrue(summary.contains("| P | 3 of 4 | ONE, TWO | D |") && summary.contains(": 3 of 4."), summary);
    }
}

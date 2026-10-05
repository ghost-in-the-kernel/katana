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
        String report = ProofReport.write(FIXTURE, "t.fixture", new Report(), new Gate.Result(true, List.of()), "nothing", List.of(), new Report(), proof());
        assertTrue(report.endsWith("## What it never covers (docs/PROOF.md, verbatim)\n\n" + section + "\n"), report);
    }

    @Test
    void aFailedGateComparesNothing() throws Exception {
        String report = ProofReport.write(FIXTURE, "t.fixture", new Report(), new Gate.Result(false, List.of("dataset X differs")), "nothing", List.of(), new Report(), proof());
        assertTrue(report.contains("Failed: the cobc lane's two runs differ, so nothing is compared (PROOF-03).\n- dataset X differs"), report);
        assertEquals(-1, report.indexOf("## Compared"));
    }
}

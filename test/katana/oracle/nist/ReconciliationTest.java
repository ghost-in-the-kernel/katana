package katana.oracle.nist;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Holds oracle/nist/RECONCILIATION.md (R-047): applied to Katana's golden, its lines rebuild every
 * published row exactly, so every difference from the published summary is explained. No cobc needed.
 */
class ReconciliationTest {
    private static String read(String name) throws Exception {
        return Files.readString(Path.of(System.getProperty("katana.root", "."), "oracle/nist", name));
    }

    @Test
    void everyDifferenceFromThePublishedSummaryIsExplained() throws Exception {
        assertEquals(List.of(), Reconciliation.check(Reconciliation.golden(read("programs.golden")),
                Reconciliation.lines(read("RECONCILIATION.md")), read("published.summary")));
    }

    private static final String GOLDEN = """
            NC NC107A 1 ok 177 172 0 0 5
            NC NC110M 1 ok 0 0 0 0 0
            IF IF119A 1 compile 0 0 0 0 0
            DB DB103M 1 ok 14 14 0 0 0
            DB DB103M 2 ok 14 14 0 0 0
            """;
    private static final String PUBLISHED = """
            NC 2 2 0 0 178 0 0 0 178
            IF 1 1 0 0 23 0 0 0 23
            DB 2 2 0 0 42 0 0 0 42
            """;
    private static final String TABLE = """
            | Program | Run | Reason | Published | Why |
            | --- | --- | --- | --- | --- |
            | NC107A | 1 | `inspection-as-pass` | - | five tests need a person |
            | NC110M | 1 | `none-as-one` | - | displays one PASS, no footer |
            | IF119A | 1 | `cobc-rejects` | `ok 23 23 0 0 0` | the ALL subscript |
            | DB103M | 2 | `runs-summed` | - | the second run's count includes the first's |
            """;

    @Test
    void aSmallReconciliationRebuildsItsRows() {
        assertEquals(List.of(), Reconciliation.check(Reconciliation.golden(GOLDEN), Reconciliation.lines(TABLE), PUBLISHED));
    }

    @Test
    void aMissingLineIsAnUnexplainedDifference() {
        String table = TABLE.replace("| NC110M | 1 | `none-as-one` | - | displays one PASS, no footer |\n", "");
        assertFalse(Reconciliation.check(Reconciliation.golden(GOLDEN), Reconciliation.lines(table), PUBLISHED).isEmpty());
    }

    @Test
    void anUnknownReasonOrAMissingStatedOutcomeIsRefused() {
        assertFalse(Reconciliation.check(Reconciliation.golden(GOLDEN), Reconciliation.lines(TABLE.replace("`none-as-one`", "`close-enough`")), PUBLISHED).isEmpty());
        assertFalse(Reconciliation.check(Reconciliation.golden(GOLDEN), Reconciliation.lines(TABLE.replace("`ok 23 23 0 0 0`", "-")), PUBLISHED).isEmpty());
    }
}

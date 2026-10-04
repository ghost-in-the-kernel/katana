package katana.oracle.nist;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Holds oracle/nist/IBM-CHANGES.md (1.2): every change between the cobol85 and ibm goldens, each with a cause that holds. */
class DialectChangesTest {
    private static String read(String name) throws Exception {
        return Files.readString(Path.of(System.getProperty("katana.root", "."), "oracle/nist", name));
    }

    @Test
    void everyChangeUnderTheIbmDialectHasACauseThatHolds() throws Exception {
        assertEquals(List.of(), DialectChanges.check(Reconciliation.golden(read("programs.golden")),
                Reconciliation.golden(read("programs-ibm.golden")), DialectChanges.read(read("IBM-CHANGES.md"))));
    }

    private static final String FROM = "NC NC106A 1 ok 126 126 0 0 0\nST ST115A 1 ok 0 0 0 0 0\nST ST117A 1 ok 1 1 0 0 0\n";
    private static final String TO = "NC NC106A 1 ok 126 84 42 0 0\nST ST115A 1 compile 0 0 0 0 0\nST ST117A 1 ok 1 0 1 0 0\n";
    private static final String TABLE = """
            | Program | Run | Causes | Follows | Why |
            | --- | --- | --- | --- | --- |
            | NC106A | 1 | `arithmetic-osvs=39, binary-truncation=3` | - | x |
            | ST115A | 1 | `reserved-word` | - | x |
            | ST117A | 1 | `follows` | ST115A | x |
            """;

    @Test
    void aSmallRecordHolds() {
        assertEquals(List.of(), DialectChanges.check(Reconciliation.golden(FROM), Reconciliation.golden(TO), DialectChanges.read(TABLE)));
    }

    @Test
    void anUnrecordedChangeOrACountThatDoesNotAddUpFails() {
        assertFalse(DialectChanges.check(Reconciliation.golden(FROM), Reconciliation.golden(TO),
                DialectChanges.read(TABLE.replace("| ST117A | 1 | `follows` | ST115A | x |\n", ""))).isEmpty());
        assertFalse(DialectChanges.check(Reconciliation.golden(FROM), Reconciliation.golden(TO),
                DialectChanges.read(TABLE.replace("binary-truncation=3", "binary-truncation=2"))).isEmpty());
        assertFalse(DialectChanges.check(Reconciliation.golden(FROM), Reconciliation.golden(TO),
                DialectChanges.read(TABLE.replace("`reserved-word`", "`dialect-feeling`"))).isEmpty());
    }
}

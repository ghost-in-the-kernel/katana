package katana.intent;

import katana.support.Small;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import katana.sir.types.Program;
import org.junit.jupiter.api.Test;

/** T-M2 on a program small enough to read (research §33, §34): an amount moved to a field edited with commas holds; the same field as text refutes. */
class MoneyEditedElsewhereTest {
    private static Verdict verdict(String picture) {
        Program p = Small.program("P", "01 TOTAL PIC S9(7)V99.\n01 OUT-TOTAL PIC " + picture + ".\n", "MAIN.\n    MOVE TOTAL TO OUT-TOTAL.\n    STOP RUN.\n");
        return MoneyEditedElsewhere.verdict(p, Small.item(p, "TOTAL"), Corpus.of(Map.of()));
    }

    @Test
    void anEditedFieldHoldsAndTextRefutes() {
        assertEquals(Verdict.Outcome.HOLDS, verdict("-Z,ZZZ,ZZ9.99").outcome(), verdict("-Z,ZZZ,ZZ9.99").line());
        assertEquals(Verdict.Outcome.REFUTES, verdict("X(15)").outcome(), verdict("X(15)").line());
    }
}

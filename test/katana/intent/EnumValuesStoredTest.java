package katana.intent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import katana.sir.types.Program;
import org.junit.jupiter.api.Test;

/** T-E1 on programs small enough to read (research §33, §34): a value stored that no condition-name names refutes; one set outside the program cannot be told; a CardDemo callee is read as the program. */
class EnumValuesStoredTest {
    private static final String X = "01 X PIC 99.\n    88 X-A VALUE 0.\n    88 X-B VALUE 16.\n";

    private static Verdict verdict(Program p, Corpus corpus) {
        return EnumValuesStored.verdict(p, Small.item(p, "X"), corpus);
    }

    @Test
    void aValueNoConditionNameNamesRefutes() {
        Verdict v = verdict(Small.program("P", X, "MAIN.\n    SET X-A TO TRUE.\n    MOVE 12 TO X.\n    STOP RUN.\n"), Corpus.of(Map.of()));
        assertEquals(Verdict.Outcome.REFUTES, v.outcome(), v.line());
        assertTrue(v.why().contains("12 (in MAIN)"), v.line());
        assertEquals(Verdict.Outcome.REFUTES, verdict(Small.program("P", X, "MAIN.\n    IF X = 12 DISPLAY 'T' END-IF.\n    STOP RUN.\n"), Corpus.of(Map.of())).outcome(), "a literal compared counts");
    }

    @Test
    void onlyNamedValuesHold() {
        Verdict v = verdict(Small.program("P", X, "MAIN.\n    SET X-A TO TRUE.\n    MOVE 16 TO X.\n    IF X = 0 DISPLAY 'Z' END-IF.\n    STOP RUN.\n"), Corpus.of(Map.of()));
        assertEquals(Verdict.Outcome.HOLDS, v.outcome(), v.line());
    }

    @Test
    void anItemHandedOutsideCannotBeToldAndACorpusCalleeIsRead() {
        Program caller = Small.program("P", "01 G.\n" + X.replace("01 X", "  05 X"), "MAIN.\n    SET X-A TO TRUE.\n    CALL 'Q' USING G.\n    STOP RUN.\n");
        assertEquals(Verdict.Outcome.CANNOT_TELL, verdict(caller, Corpus.of(Map.of())).outcome(), verdict(caller, Corpus.of(Map.of())).line());
        Program good = Small.program("Q", "", "01 L.\n  05 Y PIC 99.\n", "L", "MAIN.\n    MOVE 16 TO Y.\n    GOBACK.\n");
        Program bad = Small.program("Q", "", "01 L.\n  05 Y PIC 99.\n", "L", "MAIN.\n    MOVE 12 TO Y.\n    GOBACK.\n");
        assertEquals(Verdict.Outcome.HOLDS, verdict(caller, Corpus.of(Map.of("Q", good))).outcome(), verdict(caller, Corpus.of(Map.of("Q", good))).line());
        Verdict v = verdict(caller, Corpus.of(Map.of("Q", bad)));
        assertEquals(Verdict.Outcome.REFUTES, v.outcome(), v.line());
        assertTrue(v.why().contains("12 (in Q MAIN)"), v.line());
    }
}

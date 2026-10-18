package katana.intent;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import katana.sir.types.Program;
import org.junit.jupiter.api.Test;

/** T-E2 on programs small enough to read (research §33, §34): an item only set refutes; a test of it, here or in the CardDemo program a CALL hands it to, holds; a callee outside cannot be told. */
class EnumIsReadTest {
    private static final String L = "01 L PIC X(20).\n    88 L-A VALUE 'a'.\n    88 L-B VALUE 'b'.\n";

    private static Verdict verdict(Program p, Corpus corpus) {
        return EnumIsRead.verdict(p, Small.item(p, "L"), corpus);
    }

    @Test
    void anItemOnlySetRefutesAndATestOfItHolds() {
        Verdict set = verdict(Small.program("P", L, "MAIN.\n    SET L-A TO TRUE.\n    DISPLAY L.\n    STOP RUN.\n"), Corpus.of(Map.of()));
        assertEquals(Verdict.Outcome.REFUTES, set.outcome(), set.line());
        Verdict tested = verdict(Small.program("P", L, "MAIN.\n    SET L-A TO TRUE.\n    IF L-A DISPLAY L END-IF.\n    STOP RUN.\n"), Corpus.of(Map.of()));
        assertEquals(Verdict.Outcome.HOLDS, tested.outcome(), tested.line());
    }

    @Test
    void aCalleeIsFollowedWhereItIsCardDemosAndCannotBeToldWhereNot() {
        Program caller = Small.program("P", L, "MAIN.\n    SET L-A TO TRUE.\n    CALL 'Q' USING L.\n    STOP RUN.\n");
        assertEquals(Verdict.Outcome.CANNOT_TELL, verdict(caller, Corpus.of(Map.of())).outcome());
        Program tests = Small.program("Q", "", "01 K PIC X(20).\n    88 K-A VALUE 'a'.\n", "K", "MAIN.\n    IF K-A DISPLAY 'A' END-IF.\n    GOBACK.\n");
        Program not = Small.program("Q", "", "01 K PIC X(20).\n", "K", "MAIN.\n    DISPLAY K.\n    GOBACK.\n");
        assertEquals(Verdict.Outcome.HOLDS, verdict(caller, Corpus.of(Map.of("Q", tests))).outcome(), verdict(caller, Corpus.of(Map.of("Q", tests))).line());
        assertEquals(Verdict.Outcome.REFUTES, verdict(caller, Corpus.of(Map.of("Q", not))).outcome(), verdict(caller, Corpus.of(Map.of("Q", not))).line());
    }
}

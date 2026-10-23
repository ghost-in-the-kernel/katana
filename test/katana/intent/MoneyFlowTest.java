package katana.intent;

import katana.support.Small;
import static org.junit.jupiter.api.Assertions.assertEquals;

import katana.sir.types.Program;
import org.junit.jupiter.api.Test;

/** T-M1 on programs small enough to read (research §33, §34): an amount added to a count or a rate refutes; one multiplied by a rate, added to an amount, or printed holds. */
class MoneyFlowTest {
    private static final String DATA = "01 TOTAL PIC S9(7)V99.\n01 AMT PIC 9(5)V99.\n01 CNT PIC 9(5).\n01 RATE PIC 9V9999.\n01 OUT-TOTAL PIC -Z,ZZZ,ZZ9.99.\n";

    private static Verdict verdict(String procedure) {
        Program p = Small.program("P", DATA, "MAIN.\n" + procedure + "    STOP RUN.\n");
        return MoneyFlow.verdict(p, Small.item(p, "TOTAL"));
    }

    @Test
    void aCountOrARateAddedToAnAmountRefutes() {
        assertEquals(Verdict.Outcome.REFUTES, verdict("    ADD CNT TO TOTAL.\n").outcome(), verdict("    ADD CNT TO TOTAL.\n").line());
        assertEquals(Verdict.Outcome.REFUTES, verdict("    ADD RATE TO TOTAL.\n").outcome());
        assertEquals(Verdict.Outcome.REFUTES, verdict("    COMPUTE TOTAL = TOTAL + CNT.\n").outcome());
        assertEquals(Verdict.Outcome.REFUTES, verdict("    IF TOTAL > CNT DISPLAY 'X' END-IF.\n").outcome());
    }

    @Test
    void aProductAnAmountAndAPrintedFieldHold() {
        Verdict v = verdict("    COMPUTE TOTAL = TOTAL * RATE.\n    ADD AMT TO TOTAL.\n    MOVE TOTAL TO OUT-TOTAL.\n    IF TOTAL > 0 MOVE ZERO TO TOTAL END-IF.\n");
        assertEquals(Verdict.Outcome.HOLDS, v.outcome(), v.line());
    }
}

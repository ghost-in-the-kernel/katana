package katana.lower;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import katana.sir.types.Program;
import katana.support.Small;
import org.junit.jupiter.api.Test;

/** EXIT PERFORM [CYCLE] lowered (R-090, J-273): the meaning is held by the lowering differential's EXITS program (LowerDifferentialIntegrationTest); here the shape and the finding. */
class ExitsTest {
    private static final String DATA = "01 I PIC 9.\n01 K PIC 9.\n";

    private static Lower.Result lowered(String procedure) {
        Program p = Small.program(DATA, procedure);
        return Lower.java(p, "P");
    }

    @Test
    void aJavaLoopIsLeftByBreakAndCycledByContinue() {
        Lower.Result r = lowered("MAIN.\n    PERFORM 3 TIMES\n        ADD 1 TO K\n        IF K = 1 EXIT PERFORM CYCLE END-IF\n        IF K = 2 EXIT PERFORM END-IF\n    END-PERFORM.\n    STOP RUN.\n");
        assertTrue(r.findings().isEmpty(), r.findings().toString());
        assertTrue(r.java().contains("if (true) continue;") && r.java().contains("if (true) break;"), r.java());
    }

    @Test
    void anUntilTestedAfterCyclesToItsTest() {
        Lower.Result r = lowered("MAIN.\n    PERFORM WITH TEST AFTER UNTIL I > 2\n        ADD 1 TO I\n        EXIT PERFORM CYCLE\n    END-PERFORM.\n    STOP RUN.\n");
        assertTrue(r.findings().isEmpty(), r.findings().toString());
        assertTrue(r.java().contains("do {") && r.java().contains("if (true) continue;") && r.java().contains("while (!("), r.java());
    }

    @Test
    void aLoopWhoseBodyPerformsAProcedureIsLeftByJumpingToItsLabels() {
        Lower.Result r = lowered("MAIN.\n    PERFORM VARYING I FROM 1 BY 1 UNTIL I > 3\n        PERFORM SHOW\n        IF I = 2 EXIT PERFORM CYCLE END-IF\n        IF I = 3 EXIT PERFORM END-IF\n    END-PERFORM.\n    STOP RUN.\nSHOW.\n    DISPLAY I.\n");
        assertTrue(r.findings().isEmpty(), r.findings().toString());
        assertFalse(r.java().contains("continue;") || r.java().contains("break;"), "stretches have no Java loop to break: " + r.java());
        assertTrue(r.java().contains("if (true) return "), r.java());
    }

    @Test
    void aLoopWithoutAnExitIsLoweredAsBefore() {
        Lower.Result r = lowered("MAIN.\n    PERFORM 3 TIMES\n        ADD 1 TO K\n    END-PERFORM.\n    PERFORM\n        ADD 1 TO K\n    END-PERFORM.\n    STOP RUN.\n");
        assertTrue(r.findings().isEmpty(), r.findings().toString());
        assertFalse(r.java().contains("continue;") || r.java().contains("break;") || r.java().contains("while (false)"), r.java());
    }

    @Test
    void outsideAnInlinePerformItIsAFindingNeverASilentNothing() {
        Lower.Result r = lowered("MAIN.\n    EXIT PERFORM.\n    STOP RUN.\n");
        assertTrue(r.findings().stream().anyMatch(f -> f.toString().contains("EXIT PERFORM outside an inline PERFORM")), r.findings().toString());
        r = lowered("MAIN.\n    PERFORM SHOW.\n    STOP RUN.\nSHOW.\n    EXIT PERFORM.\n");
        assertTrue(r.findings().stream().anyMatch(f -> f.toString().contains("EXIT PERFORM outside an inline PERFORM")), "a performed paragraph is no inline PERFORM: " + r.findings());
    }
}

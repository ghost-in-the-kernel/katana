package katana.passes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import katana.intent.rules.Rules;
import katana.lower.Lower;
import katana.sir.types.Program;
import org.junit.jupiter.api.Test;

/** 6.6's dataflow and stateless subprograms (research §44) on programs small enough to read: each rule kept, and each broken. */
class DataflowTest {
    private static Program program(String data, String procedure) {
        return katana.support.Small.program(data, procedure);
    }

    private static List<String> refusals(String data, String procedure) {
        return Stateless.refusals(program(data, procedure));
    }

    @Test
    void writtenBeforeReadOnEveryPathIsStateless() {
        assertEquals(List.of(), refusals("01 A PIC 9.\n", "MAIN.\n    MOVE 1 TO A.\n    DISPLAY A.\n    GOBACK.\n"));
        assertEquals(List.of(), refusals("01 K PIC 9 VALUE 5.\n", "MAIN.\n    DISPLAY K.\n    GOBACK.\n"), "an item no statement writes holds its VALUE on every call");
        assertEquals(List.of(), refusals("01 A PIC 9.\n", "MAIN.\n    PERFORM SETA.\n    DISPLAY A.\n    GOBACK.\nSETA.\n    MOVE 1 TO A.\n"), "a range PERFORMed once writes for certain");
        assertEquals(List.of(), refusals("01 G.\n   05 A PIC 9.\n   05 B PIC 9 VALUE 2.\n", "MAIN.\n    MOVE 1 TO A.\n    DISPLAY G.\n    GOBACK.\n"), "a group's bytes no statement writes hold their VALUE");
        assertEquals(List.of(), refusals("01 I PIC 9.\n", "MAIN.\n    PERFORM VARYING I FROM 1 BY 1 UNTIL I > 3\n        DISPLAY I\n    END-PERFORM.\n    GOBACK.\n"), "FROM writes a VARYING identifier before UNTIL reads it");
        assertEquals(List.of(), refusals("01 G.\n   05 A PIC 9.\n   05 FILLER PIC X VALUE '-'.\n   05 B PIC X.\n", "MAIN.\n    INITIALIZE G.\n    DISPLAY G.\n    MOVE 1 TO A.\n    GOBACK.\n"), "INITIALIZE of a group writes its receivers, its FILLER keeping its VALUE");
        Program p = program("01 A PIC 9.\n", "MAIN.\n    MOVE 1 TO A.\n    DISPLAY A.\n    GOBACK.\n");
        String java = Lower.java(p, "P", Structuring.plan(p, Rules.infer(p)).plan()).java();
        assertTrue(java.contains("public boolean initial() { return true; }") && java.contains("// Stateless (passes/Stateless)"), java);
    }

    @Test
    void eachWayAReadCanSeeAnEarlierCallKeepsTheState() {
        assertRefused("01 C PIC 9 VALUE 0.\n", "MAIN.\n    ADD 1 TO C.\n    DISPLAY C.\n    GOBACK.\n", "a counter of calls");
        assertRefused("01 X PIC 9.\n01 A PIC 9.\n", "MAIN.\n    MOVE 1 TO X.\n    IF X = 1 MOVE 1 TO A END-IF.\n    DISPLAY A.\n    GOBACK.\n", "written on one branch only");
        assertRefused("01 X PIC 9.\n01 A PIC 9.\n", "MAIN.\n    MOVE 0 TO X.\n    PERFORM SETA UNTIL X = 0.\n    DISPLAY A.\n    GOBACK.\nSETA.\n    MOVE 1 TO A.\n    MOVE 0 TO X.\n", "a loop that may run no times");
        assertRefused("01 A PIC 9.\n", "MAIN.\n    GO TO L2.\nL1.\n    MOVE 1 TO A.\nL2.\n    DISPLAY A.\n    GOBACK.\n", "a GO TO past the write");
        assertRefused("01 G.\n   05 A PIC 9.\n   05 B PIC 9.\n", "MAIN.\n    MOVE 1 TO A.\n    DISPLAY G.\n    MOVE 2 TO B.\n    GOBACK.\n", "a group read before all its written bytes are");
        assertRefused("01 C PIC 9 VALUE 0.\n", "MAIN.\n    STOP 'PAUSE'.\n    ADD 1 TO C.\n    GOBACK.\n", "STOP with a literal goes on");
        assertRefused("01 G.\n   05 A PIC 9.\n   05 B PIC X.\n", "MAIN.\n    INITIALIZE G REPLACING NUMERIC DATA BY 0.\n    DISPLAY G.\n    MOVE 'X' TO B.\n    GOBACK.\n", "INITIALIZE REPLACING writes only the categories it names");
        assertRefused("01 A PIC 9.\n", "MAIN.\n    MOVE 1 TO A (1:1).\n    DISPLAY A.\n    GOBACK.\n", "a reference-modified write is no write for certain");
    }

    @Test
    void whatElseCarriesStateKeepsAProgramOut() {
        assertTrue(refusals("01 G EXTERNAL.\n   05 A PIC 9.\n", "MAIN.\n    MOVE 1 TO A.\n    GOBACK.\n").stream().anyMatch(w -> w.contains("EXTERNAL")));
        assertTrue(refusals("01 A PIC 9.\n", "MAIN.\n    MOVE 1 TO A.\n    GO TO M2.\nM2.\n    ALTER M3 TO PROCEED TO M2.\nM3.\n    GO TO M2.\n").stream().anyMatch(w -> w.contains("ALTER")));
    }

    private static void assertRefused(String data, String procedure, String what) {
        List<String> why = refusals(data, procedure);
        assertFalse(why.isEmpty(), what + ": should keep its state");
        assertTrue(why.stream().anyMatch(w -> w.contains("reads bytes no path need have written")), what + ": " + why);
    }
}

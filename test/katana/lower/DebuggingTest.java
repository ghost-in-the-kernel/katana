package katana.lower;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;
import katana.front.Copybooks;
import katana.front.Parse;
import katana.sir.build.Build;
import katana.sir.types.Program;
import org.junit.jupiter.api.Test;

/** USE FOR DEBUGGING on procedures, lowered (research §17, J-176). */
class DebuggingTest {
    private static final String HASH = "sha256:cc4bbf084793b6f5280da870d1aa7b6fb58d535dcfaf091d8436a36a66c4dae1";

    private static String java(boolean mode) {
        Program p = Build.program(Parse.text(java.util.Arrays.stream(new String[] {
            "IDENTIFICATION DIVISION.", "PROGRAM-ID. DBG.", "ENVIRONMENT DIVISION.", "CONFIGURATION SECTION.",
            "SOURCE-COMPUTER. IBM-Z" + (mode ? " WITH DEBUGGING MODE." : "."), "DATA DIVISION.", "WORKING-STORAGE SECTION.", "01 HOLD PIC X(30).",
            "PROCEDURE DIVISION.", "DECLARATIVES.", "WATCH SECTION.", "    USE FOR DEBUGGING ON TARGET.", "W1.", "    MOVE DEBUG-NAME TO HOLD.",
            "END DECLARATIVES.", "MAIN SECTION.", "M1.", "    PERFORM TARGET.", "    STOP RUN.", "TARGET.", "    DISPLAY HOLD."})
            .map(l -> "       " + l).collect(Collectors.joining("\n", "", "\n")), new Copybooks(List.of(), n -> 8)), HASH).program();
        return Lower.java(p, "Dbg").java();
    }

    @Test
    void aWatchedParagraphRunsItsDebuggingSectionBeforeEachEntry() {
        String java = java(true);
        assertTrue(java.contains("this.dbg = new Debug(cp, true);"), java);
        assertTrue(java.contains("if (dbg.on()) { dbg.fill(\"TARGET\"); use("), "Table 74: before each execution of the named procedure");
        assertTrue(java.contains("dbg.perform(d.perform("), "the PERFORM tells its cause, PERFORM LOOP");
        assertTrue(java.contains("Move.move(dbg.name, "), "DEBUG-NAME is DEBUG-ITEM's field");
    }

    @Test
    void withoutDebuggingModeNothingIsWatched() {
        assertFalse(java(false).contains("dbg."), "LR p. 760: the debugging sections are comments");
    }
}

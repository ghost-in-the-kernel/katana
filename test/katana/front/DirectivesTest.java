package katana.front;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/** The `>>` directives (research 58, R-081): conditional text and `>>D`; any other directive stays an error. */
class DirectivesTest {
    private static final Copybooks NONE = new Copybooks(List.of(), name -> 8);

    private static String program(String... body) {
        StringBuilder text = new StringBuilder("       IDENTIFICATION DIVISION.\n       PROGRAM-ID. P.\n       PROCEDURE DIVISION.\n");
        for (String line : body) text.append(line.startsWith(">") ? "       " + line : "           " + line).append('\n');
        return text.toString();
    }

    @Test
    void anUndefinedNameTakesTheElseBranchAndKeepsEveryLine() {
        String text = program(">>IF ACTIVATE DEFINED", "DISPLAY 'NO'.", ">>ELSE", "DISPLAY 'YES'.", ">>END-IF", "STOP RUN.");
        String applied = Directives.apply(text);
        assertEquals(text.length(), applied.length());
        assertEquals(text.split("\n", -1).length, applied.split("\n", -1).length);
        assertFalse(applied.contains("NO'"));
        assertTrue(applied.contains("'YES'"));
        assertEquals(List.of(), Parse.text(text, NONE).errors());
    }

    @Test
    void elifTakesTheFirstTrueBranchAndNestingHoldsOuterFalse() {
        String text = program(">>DEFINE B AS 1", ">>IF A DEFINED", "DISPLAY 'A'.", ">>ELIF B DEFINED", ">>IF A NOT DEFINED",
                "DISPLAY 'B'.", ">>ELSE", "DISPLAY 'C'.", ">>END-IF", ">>ELSE", "DISPLAY 'D'.", ">>END-IF", "STOP RUN.");
        String applied = Directives.apply(text);
        assertTrue(applied.contains("'B'") && !applied.contains("'A'") && !applied.contains("'C'") && !applied.contains("'D'"));
    }

    @Test
    void aValueIsComparedAsText() {
        String text = program(">>DEFINE CS AS 'EBCDIC'", ">>IF CS = 'EBCDIC'", "DISPLAY 'E'.", ">>END-IF", ">>IF CS <> 'EBCDIC'", "DISPLAY 'X'.", ">>END-IF",
                ">>IF OTHER = 'EBCDIC'", "DISPLAY 'U'.", ">>END-IF", "STOP RUN.");
        String applied = Directives.apply(text);
        assertTrue(applied.contains("'E'") && !applied.contains("'X'") && !applied.contains("'U'"));
    }

    @Test
    void aDebuggingDirectiveLineIsADebuggingLine() {
        String text = program("DISPLAY 'OK'.", "STOP RUN.").replace("           STOP RUN.", "      >>D    DISPLAY 'KO'.\n           STOP RUN.");
        assertEquals(List.of(), Parse.text(text, NONE).errors());
        assertEquals("D", Directives.apply(text).split("\n")[4].substring(6, 7));
        assertFalse(Lines.normalize(text).isBlank());
    }

    @Test
    void aDirectiveItDoesNotReadLeavesTheSourceAsItWasSoTheParserStops() {
        for (String directive : List.of(">>SET CONSTANT X 'A'", ">>SOURCE FORMAT FREE", ">>TURN EC-BOUND CHECKING ON", ">>DEFINE X AS PARAMETER", ">>IF X IS FOO")) {
            String text = program(directive, "STOP RUN.");
            assertEquals(text, Directives.apply(text), directive);
            assertFalse(Parse.text(text, NONE).clean(), directive);
        }
        String unbalanced = program(">>END-IF", "STOP RUN.");
        assertEquals(unbalanced, Directives.apply(unbalanced));
        String open = program(">>IF X DEFINED", "STOP RUN.");
        assertEquals(open, Directives.apply(open));
    }
}

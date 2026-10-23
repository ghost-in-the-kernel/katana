package katana.front;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** The `>>` directives (research 58, R-081): conditional text and `>>D`; any other directive stays an error. */
class DirectivesTest {
    private static final Copybooks NONE = new Copybooks(List.of(), name -> 8);

    /** The census's reading and the unit tests': no definitions from the job, the GnuCOBOL extensions on. */
    private static final Settings NO_DEFINES = Settings.declared(Map.of(), true);

    private static String applied(String text) {
        return Directives.apply(text, "", NO_DEFINES).text();
    }

    private static String program(String... body) {
        StringBuilder text = new StringBuilder("       IDENTIFICATION DIVISION.\n       PROGRAM-ID. P.\n       PROCEDURE DIVISION.\n");
        for (String line : body) text.append(line.startsWith(">") ? "       " + line : "           " + line).append('\n');
        return text.toString();
    }

    @Test
    void anUndefinedNameTakesTheElseBranchAndKeepsEveryLine() {
        String text = program(">>IF ACTIVATE DEFINED", "DISPLAY 'NO'.", ">>ELSE", "DISPLAY 'YES'.", ">>END-IF", "STOP RUN.");
        String applied = applied(text);
        assertEquals(text.length(), applied.length());
        assertEquals(text.split("\n", -1).length, applied.split("\n", -1).length);
        assertFalse(applied.contains("NO'"));
        assertTrue(applied.contains("'YES'"));
        assertEquals(List.of(), Parse.text(text, "", NONE, NO_DEFINES).errors());
    }

    @Test
    void elifTakesTheFirstTrueBranchAndNestingHoldsOuterFalse() {
        String text = program(">>DEFINE B AS 1", ">>IF A DEFINED", "DISPLAY 'A'.", ">>ELIF B DEFINED", ">>IF A NOT DEFINED",
                "DISPLAY 'B'.", ">>ELSE", "DISPLAY 'C'.", ">>END-IF", ">>ELSE", "DISPLAY 'D'.", ">>END-IF", "STOP RUN.");
        String applied = applied(text);
        assertTrue(applied.contains("'B'") && !applied.contains("'A'") && !applied.contains("'C'") && !applied.contains("'D'"));
    }

    @Test
    void aValueIsComparedAsText() {
        String text = program(">>DEFINE CS AS 'EBCDIC'", ">>IF CS = 'EBCDIC'", "DISPLAY 'E'.", ">>END-IF", ">>IF CS <> 'EBCDIC'", "DISPLAY 'X'.", ">>END-IF",
                ">>IF OTHER = 'EBCDIC'", "DISPLAY 'U'.", ">>END-IF", "STOP RUN.");
        String applied = applied(text);
        assertTrue(applied.contains("'E'") && !applied.contains("'X'") && !applied.contains("'U'"));
    }

    @Test
    void aDebuggingDirectiveLineIsADebuggingLine() {
        String text = program("DISPLAY 'OK'.", "STOP RUN.").replace("           STOP RUN.", "      >>D    DISPLAY 'KO'.\n           STOP RUN.");
        assertEquals(List.of(), Parse.text(text, "", NONE, NO_DEFINES).errors());
        assertEquals("D", applied(text).split("\n")[4].substring(6, 7));
        assertFalse(Lines.normalize(text, "", NO_DEFINES, new java.util.ArrayList<>(), new java.util.ArrayList<>()).toString().isBlank());
    }

    @Test
    void aDirectiveItDoesNotReadLeavesTheSourceAsItWasSoTheParserStops() {
        for (String directive : List.of(">>SET CONSTANT X 'A'", ">>SOURCE FORMAT FREE", ">>TURN EC-BOUND CHECKING ON", ">>DEFINE X AS PARAMETER", ">>IF X IS FOO")) {
            String text = program(directive, "STOP RUN.");
            assertEquals(text, applied(text), directive);
            assertFalse(Parse.text(text, "", NONE, NO_DEFINES).clean(), directive);
        }
        String unbalanced = program(">>END-IF", "STOP RUN.");
        assertEquals(unbalanced, applied(unbalanced));
        String open = program(">>IF X DEFINED", "STOP RUN.");
        assertEquals(open, applied(open));
    }

    @Test
    void aNameTheSourceNeverDefinesIsAnErrorUnlessTheJobDeclaresItsDefinitions() {
        String text = program(">>IF ACTIVATE DEFINED", "DISPLAY 'NO'.", ">>ELSE", "DISPLAY 'YES'.", ">>END-IF", "STOP RUN.");
        Parse.Result undeclared = Parse.text(text, NONE);
        assertFalse(undeclared.clean());
        assertTrue(undeclared.errors().get(0).contains("ACTIVATE") && undeclared.errors().get(0).startsWith("line 4:"), undeclared.errors().get(0));
        assertEquals(text, Directives.apply(text, "", Settings.UNDECLARED).text());
        String equals = program(">>IF CS = 'EBCDIC'", "DISPLAY 'E'.", ">>END-IF", "STOP RUN.");
        assertFalse(Parse.text(equals, NONE).clean());
        // A name the source defines first needs no declaration.
        assertTrue(Parse.text(program(">>DEFINE A AS 1", ">>IF A DEFINED", "DISPLAY 'A'.", ">>END-IF", "STOP RUN."), NONE).clean());
        // Declared: the job's names are read, and any other name is undefined by the job's word.
        Settings job = Settings.declared(Map.of("activate", "1"), false);
        String yes = Directives.apply(text, "", job).text();
        assertTrue(yes.contains("'NO'") && !yes.contains("'YES'"));
        assertTrue(Parse.text(text, "", NONE, job).clean());
        assertTrue(Parse.text(text, "", NONE, Settings.declared(Map.of(), false)).clean());
        assertTrue(Directives.apply(equals, "", Settings.declared(Map.of("CS", "EBCDIC"), false)).text().contains("'E'"));
    }

    @Test
    void aSourceDefineThatContradictsTheJobsIsAnError() {
        String text = program(">>DEFINE A AS 2", "STOP RUN.");
        Directives.Result r = Directives.apply(text, "", Settings.declared(Map.of("A", "1"), false));
        assertEquals(text, r.text());
        assertTrue(r.errors().get(0).contains("contradicts"));
        assertEquals(List.of(), Directives.apply(text, "", Settings.declared(Map.of("A", "2"), false)).errors());
    }

    @Test
    void elifAndDebuggingDirectivesAreReadOnlyWhereTheDialectAllowsThem() {
        Settings ibm = Settings.declared(Map.of(), false);
        for (String text : List.of(program(">>IF A DEFINED", ">>ELIF B DEFINED", ">>END-IF", "STOP RUN."), program(">>D DISPLAY 'X'.", "STOP RUN."))) {
            assertEquals(text, Directives.apply(text, "", ibm).text());
            assertFalse(Parse.text(text, "", NONE, ibm).clean());
            assertFalse(Directives.apply(text, "", NO_DEFINES).text().equals(text));
        }
    }

    @Test
    void eachRangeAFalseConditionExcludesIsRecordedWithItsDirective() {
        String text = program(">>IF A DEFINED", "DISPLAY 'A'.", "DISPLAY 'B'.", ">>ELIF B DEFINED", "DISPLAY 'C'.", ">>ELSE", "DISPLAY 'D'.", ">>END-IF",
                ">>IF C DEFINED", ">>END-IF", "STOP RUN.");
        Directives.Result r = Directives.apply(text, "p.cbl", NO_DEFINES);
        // Lines 4 to 11 hold the directives and branches: A is false (lines 5-6), B is false (line 8), the ELSE is live; the empty IF excludes nothing.
        assertEquals(List.of(new Excluded("p.cbl", 5, 6, ">>IF A DEFINED"), new Excluded("p.cbl", 8, 8, ">>ELIF B DEFINED")), r.excluded());
        String live = program(">>IF A DEFINED", "DISPLAY 'A'.", ">>ELSE", "DISPLAY 'D'.", ">>END-IF", "STOP RUN.");
        assertEquals(List.of(new Excluded("", 7, 7, ">>ELSE")), Directives.apply(live, "", Settings.declared(Map.of("A", "1"), false)).excluded());
        // An excluded range nested in another is part of the outer one.
        String nested = program(">>IF A DEFINED", ">>IF B DEFINED", "DISPLAY 'X'.", ">>END-IF", ">>END-IF", "STOP RUN.");
        assertEquals(List.of(new Excluded("", 5, 7, ">>IF A DEFINED")), Directives.apply(nested, "", NO_DEFINES).excluded());
    }
}

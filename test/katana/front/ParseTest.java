package katana.front;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** The front end on small programs: reference format, COPY and REPLACE, and the gaps 2.1 closes. */
class ParseTest {
    /** A program in reference format: each line of the body indented to area B (column 12) unless it starts with `A:`. */
    private static String program(String... body) {
        StringBuilder text = new StringBuilder("""
                       IDENTIFICATION DIVISION.
                       PROGRAM-ID. P.
                       DATA DIVISION.
                       WORKING-STORAGE SECTION.
                       01  X PIC X(10).
                       01  Y PIC X(10).
                       01  N PIC 9(4).
                       PROCEDURE DIVISION.
                """);
        for (String line : body) text.append(line.startsWith("A:") ? "       " + line.substring(2) : "           " + line).append('\n');
        return text.toString();
    }

    private static Parse.Result parse(String text, Copybooks books) {
        return Parse.text(text, books);
    }

    private static final Copybooks NONE = new Copybooks(List.of(), name -> 8);

    @Test
    void aSmallProgramIsClean() {
        Parse.Result r = parse(program("MOVE 'A' TO X.", "STOP RUN."), NONE);
        assertEquals(List.of(), r.errors());
    }

    @Test
    void aSyntaxErrorIsAnError() {
        assertFalse(parse(program("MOVE 'A' TO.", "STOP RUN."), NONE).clean());
        assertFalse(parse(program("IF X = Y DISPLAY X.", "END-IF.", "STOP RUN."), NONE).clean());
    }

    /** Research 61 (J-268): ROUNDED MODE is not confirmed as IBM's and its modes are not built in `rt/`, so it stays an error (R-089, P-03). */
    @Test
    void roundedModeIsAnErrorUntilIbmsTextIsRead() {
        assertTrue(parse(program("COMPUTE N ROUNDED = 7 / 2.", "STOP RUN."), NONE).clean());
        assertFalse(parse(program("COMPUTE N ROUNDED MODE NEAREST-EVEN = 7 / 2.", "STOP RUN."), NONE).clean());
        assertFalse(parse(program("COMPUTE N ROUNDED MODE IS TRUNCATION = 7 / 2.", "STOP RUN."), NONE).clean());
    }

    /** R-090 (J-273): EXIT PERFORM [CYCLE], EXIT PARAGRAPH and EXIT SECTION are IBM's; PARAGRAPH and CYCLE stay unreserved, usable as a name. */
    @Test
    void exitPerformParagraphAndSectionParseAndTheirWordsStayNames() {
        assertTrue(parse(program("PERFORM 2 TIMES", "    EXIT PERFORM CYCLE", "    EXIT PERFORM", "END-PERFORM", "EXIT PARAGRAPH", "EXIT SECTION", "EXIT PROGRAM", "STOP RUN."), NONE).clean());
        assertTrue(parse(program("PERFORM", "    IF N = 1 EXIT PERFORM END-IF", "    DISPLAY X", "END-PERFORM", "EXIT.", "STOP RUN."), NONE).clean());
        String named = program("MOVE 1 TO CYCLE.", "MOVE 2 TO PARAGRAPH.", "EXIT.", "STOP RUN.").replace("01  N PIC 9(4).", "01  N PIC 9(4).\n       01  CYCLE PIC 9.\n       01  PARAGRAPH PIC 9.");
        assertEquals(List.of(), parse(named, NONE).errors());
        assertFalse(parse(program("EXIT PERFORM STOP.", "STOP RUN."), NONE).clean(), "EXIT PERFORM takes CYCLE or nothing");
    }

    /** R-090 (J-273): a `$` in column 7 is another dialect's directive line (`$SET`), not an IBM indicator: an error naming the line, IBM's set and the recollection. */
    @Test
    void aDollarInColumnSevenIsAnError() {
        String text = program("MOVE 'A' TO X.", "STOP RUN.").replace("       PROCEDURE DIVISION.\n", "      $SET SSRANGE\n       PROCEDURE DIVISION.\n");
        Parse.Result r = parse(text, NONE);
        assertFalse(r.clean());
        assertEquals(1, r.errors().size(), r.errors().toString());
        String error = r.errors().get(0);
        assertTrue(error.contains("line 8:") && error.contains("`$` in column 7") && error.contains("`*`, `/`, `-`, `D` and a blank") && error.endsWith("(recollection, R-082)"), error);
        assertTrue(parse(text.replace("      $SET SSRANGE\n", "      *SET SSRANGE\n"), NONE).clean(), "the same line as a comment is clean");
        assertTrue(parse(program("MOVE '$' TO X.", "STOP RUN."), NONE).clean(), "a $ elsewhere in a line is no indicator");
    }

    @Test
    void commentsContinuationsAndSequenceNumbersAreReferenceFormat() {
        String text = program("STOP RUN.").replace("       PROCEDURE DIVISION.\n", """
                000100 PROCEDURE DIVISION.
                      *
                      * a comment
                           MOVE 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789ABCDEFGHIJKLMNOPQRS
                      -         'TUVWXYZ' TO X.
                           DISPLAY X                                                    IGNORED
                """);
        assertEquals(List.of(), parse(text, NONE).errors());
        String normalized = Lines.normalize(text);
        assertTrue(normalized.contains("ABCDEFGHIJKLMNOPQRSTUVWXYZ' TO X."), normalized);
        assertFalse(normalized.contains("IGNORED"));
        assertEquals(text.split("\n").length, normalized.split("\n").length);
    }

    /** The text a REPLACE leaves, comments dropped and blanks folded. */
    private static String replaced(String... lines) {
        String text = java.util.Arrays.stream(lines).map(l -> l.startsWith("*") || l.startsWith("D") ? "      " + l : "       " + l).collect(java.util.stream.Collectors.joining("\n", "", "\n"));
        return Preprocessor.run(text, NONE).text().toString().replaceAll("\\*>[^\\n]*", " ").replaceAll("\\s+", " ").trim();
    }

    /**
     * IBM's comparison rules for REPLACE and COPY REPLACING (LR 6.4 pp. 693, 701-702; J-173): a replacement is
     * never matched again; comment lines between words are ignored, a debugging line's words take part, a
     * separator comma or semicolon is a space; the replaceables are tried in the order written.
     */
    @Test
    void replacingFollowsIbmsComparisonRules() {
        assertEquals("MOVE 5 TO X.", replaced("REPLACE ==1== BY ==5== ==5== BY ==7==.", "MOVE 1 TO X.", "REPLACE OFF."), "SM206A: 1 BY 5, 5 BY 7 gives 5");
        assertEquals("MOVE \"PASS\" TO Y.", replaced("REPLACE ==MOVE \"FAIL\" TO== BY ==MOVE \"PASS\" TO==.", "MOVE", "* A COMMENT", "\"FAIL\"", "TO Y.", "REPLACE OFF."));
        assertEquals("MOVE \"PASS\" TO Y.", replaced("REPLACE ==MOVE; \"FAIL\" , TO== BY ==MOVE \"PASS\" TO==.", "MOVE , \"FAIL\"; TO Y.", "REPLACE OFF."), "SM208A: separators");
        assertEquals("PASS.", replaced("REPLACE ==FAIL. GARBAGE. NEXT.== BY ==PASS.==.", "FAIL.", "D   GARBAGE.", "NEXT.", "REPLACE OFF."), "SM206A: a debugging line's words match");
        assertEquals("MOVE B TO X.", replaced("REPLACE ==A== BY ==B== ==A TO== BY ==C==.", "MOVE A TO X.", "REPLACE OFF."), "the first written that matches, not the longest");
    }

    @Test
    void copyFindsItsBookQuotedOrNotInItsLibraryAndReplaces(@TempDir Path dir) throws Exception {
        Files.writeString(dir.resolve("REC.cpy"), "       01  :P:-A PIC X.\n");
        Files.createDirectories(dir.resolve("LIB2"));
        Files.writeString(dir.resolve("LIB2/REC.CPY"), "       01  LIBREC PIC X.\n");
        Copybooks books = new Copybooks(List.of(dir), name -> 8);
        String text = program("MOVE Q-A TO X.", "STOP RUN.").replace("       01  N PIC 9(4).\n",
                "           COPY REC REPLACING ==:P:== BY ==Q==.\n           COPY 'REC' REPLACING ==:P:== BY ==R==.\n           COPY REC OF LIB2.\n");
        Parse.Result r = parse(text, books);
        assertEquals(List.of(), r.errors());
        String pre = Preprocessor.run(text, books).text();
        assertTrue(pre.contains("01  Q-A PIC X.") && pre.contains("01  R-A PIC X.") && pre.contains("01  LIBREC PIC X."), pre);
        assertEquals(List.of("copybook NOPE not found (line 7)"), Preprocessor.run(program("STOP RUN.").replace("       01  N PIC 9(4).\n", "           COPY NOPE.\n"), books).errors());
    }

    @Test
    void replaceGovernsTheTextAfterIt() {
        String text = program("REPLACE ==WHO== BY ==X==.", "MOVE 'A' TO WHO.", "REPLACE OFF.", "STOP RUN.");
        assertEquals(List.of(), parse(text, NONE).errors());
        assertTrue(Preprocessor.run(text, NONE).text().contains("MOVE 'A' TO X."));
    }

    @Test
    void execBlocksBecomeTaggedLines() {
        String text = program("EXEC CICS RETURN", "END-EXEC.", "EXEC SQL SELECT A INTO :X FROM T END-EXEC.", "EXEC SQL", "", "COMMIT END-EXEC.", "STOP RUN.");
        assertEquals(List.of(), parse(text, NONE).errors());
        Mapped mapped = Preprocessor.run(text, "P.cbl", NONE).mapped();
        for (int i = 0; i < mapped.length(); i++) assertEquals("P.cbl", mapped.origin(i).file(), "character " + i);
        assertTrue(Preprocessor.run(text, NONE).text().contains("*>EXECCICS END-EXEC }."));
    }

    @Test
    void commentEntriesAreNotCode() {
        String text = program("STOP RUN.").replace("       PROGRAM-ID. P.\n", "       PROGRAM-ID. P.\n       AUTHOR. SOMEONE, WHO WROTE IT.\n           AND THIS LINE TOO.\n");
        assertEquals(List.of(), parse(text, NONE).errors());
    }

    @Test
    void theGapsClosed() {
        assertEquals(List.of(), parse(program("DISPLAY FUNCTION TRIM(X, TRAILING) FUNCTION TRIM(Y LEADING).", "STOP RUN."), NONE).errors(), "keyword arguments");
        assertEquals(List.of(), parse(program("IF X NOT= Y DISPLAY X END-IF.", "STOP RUN."), NONE).errors(), "NOT= as one token");
        assertEquals(List.of(), parse(program("MOVE X(1:LENGTH OF Y) TO Y.", "STOP RUN."), NONE).errors(), "LENGTH OF in a reference modification");
        assertEquals(List.of(), parse(program("STOP RUN.").replace("PROGRAM-ID. P.", "PROGRAM-ID. P"), NONE).errors(), "PROGRAM-ID without a period");
        String alternate = program("STOP RUN.").replace("       DATA DIVISION.\n", """
                       ENVIRONMENT DIVISION.
                       INPUT-OUTPUT SECTION.
                       FILE-CONTROL.
                           SELECT F ASSIGN TO D ORGANIZATION IS INDEXED
                               RECORD KEY IS K ALTERNATE KEY IS A WITH DUPLICATES.
                       DATA DIVISION.
                       FILE SECTION.
                       FD  F.
                       01  R.
                           05  K PIC X.
                           05  A PIC X.
                """);
        assertEquals(List.of(), parse(alternate, NONE).errors(), "ALTERNATE KEY without RECORD");
        String tab = program("STOP RUN.").replace("           STOP RUN.", "\tSTOP RUN.");
        assertEquals(List.of(), parse(new String(Tabs.expand(tab.getBytes(), 8)), NONE).errors(), "a tab in the indicator area");
    }

    @Test
    void everyCharacterKnowsWhereItCameFrom(@TempDir Path dir) throws Exception {
        Files.writeString(dir.resolve("REC.cpy"), "       01  :P:-A PIC X.\n");
        Path program = dir.resolve("P.cbl");
        Files.writeString(program, program("MOVE 'ABC' TO Q-A.", "STOP RUN.").replace("       01  N PIC 9(4).\n", "           COPY REC REPLACING ==:P:== BY ==Q==.\n"));
        Parse.Result r = Parse.file(program, new Copybooks(List.of(dir), name -> 8), 8);
        assertEquals(List.of(), r.errors());
        assertEquals(List.of(), new RoundTrip(name -> 8).check(r));
        String text = r.text().toString();
        int q = text.indexOf("Q-A PIC");
        assertEquals(new Mapped.Origin(dir.resolve("REC.cpy").toString(), 1, 12, Mapped.Kind.REPLACED), r.text().origin(q));
        assertEquals(new Mapped.Origin(dir.resolve("REC.cpy").toString(), 1, 15, Mapped.Kind.SOURCE), r.text().origin(q + 1));
        int move = text.indexOf("MOVE");
        assertEquals(new Mapped.Origin(program.toString(), 9, 12, Mapped.Kind.SOURCE), r.text().origin(move));
    }
}

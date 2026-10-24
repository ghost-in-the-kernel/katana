package katana.oracle.autotest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * The Autotest reader on a sample written here from the format the Autoconf manual describes (no
 * GnuCOBOL file is read, R-045): quoting undone one level deep (inner brackets kept), quadrigraphs, a check's
 * defaults, a skipped case, and the malformed files it refuses with their line.
 */
class AutotestsTest {
    private static final String SAMPLE = String.join("\n",
            "## a comment, and a quoted [AT_SETUP] that is not a call",
            "AT_SETUP([DISPLAY a literal])",
            "AT_KEYWORDS([display literal])",
            "",
            "AT_DATA([prog.cob], [",
            "       IDENTIFICATION DIVISION.",
            "       PROGRAM-ID. prog.",
            "       PROCEDURE DIVISION.",
            "           DISPLAY \"A, (B)\" @<:@1@:>@",
            "           STOP RUN.",
            "])",
            "AT_CHECK([$COMPILE prog.cob], [0], [], [])",
            "AT_CHECK([$COBCRUN_DIRECT ./prog], [0],",
            "[A, (B) [[1]]",
            "])",
            "AT_CLEANUP",
            "",
            "dnl a dnl line",
            "AT_SETUP([a skipped one])",
            "AT_SKIP_IF([true])",
            "AT_DATA([in.txt], [x@S|@y@%:@z@&t@])",
            "AT_CHECK([cat in.txt],, [x$y#z])",
            "AT_CHECK([false], [ignore])",
            "AT_CLEANUP",
            "");

    @Test
    void eachCaseItsFilesAndChecks() {
        List<Autotests.Case> cases = Autotests.read("sample.at", SAMPLE);
        assertEquals(2, cases.size());
        Autotests.Case first = cases.get(0);
        assertEquals("DISPLAY a literal", first.title());
        assertEquals(2, first.line());
        assertEquals(List.of("display", "literal"), first.keywords());
        assertTrue(first.files().get("prog.cob").contains("DISPLAY \"A, (B)\" [1]"), first.files().get("prog.cob"));
        assertEquals(2, first.checks().size());
        assertEquals(new Autotests.Check("$COBCRUN_DIRECT ./prog", "0", "A, (B) [[1]]\n", "", 13), first.checks().get(1));
        assertFalse(first.conditional());
        Autotests.Case second = cases.get(1);
        assertTrue(second.conditional());
        assertEquals(Map.of("in.txt", "x$y#z"), second.files());
        assertEquals("0", second.checks().get(0).status());
        assertEquals("x$y#z", second.checks().get(0).stdout());
        assertEquals("ignore", second.checks().get(1).status());
    }

    @Test
    void aWholeDoubleQuotedArgumentLosesItsSecondLevelAndAQuadrigraphBracketKeepsItsBrackets() {
        String at = String.join("\n", "AT_SETUP([q])",
                "AT_DATA([prog.cob], [[", "       PROGRAM-ID. p.", "]])",
                "AT_DATA([one.txt], [", "       PROGRAM-ID. p.", "])",
                "AT_DATA([quad.txt], [@<:@x@:>@])",
                "AT_DATA([inner.txt], [a [b] c])",
                "AT_CHECK([[test -f x]], [0], [[out]], [[err", "]])",
                "AT_CLEANUP", "");
        Autotests.Case c = Autotests.read("q.at", at).get(0);
        assertEquals("\n       PROGRAM-ID. p.\n", c.files().get("prog.cob"));
        assertEquals("\n       PROGRAM-ID. p.\n", c.files().get("one.txt"));
        assertEquals("[x]", c.files().get("quad.txt"));
        assertEquals("a [b] c", c.files().get("inner.txt"));
        assertEquals("[test -f x]", c.checks().get(0).command());
        assertEquals("out", c.checks().get(0).stdout());
        assertEquals("err\n", c.checks().get(0).stderr());
        assertEquals("a", Autotests.unquoteWhole("[a]"));
        assertEquals("[a][b]", Autotests.unquoteWhole("[a][b]"));
        assertEquals("[a", Autotests.unquoteWhole("[a"));
    }

    @Test
    void aMalformedFileIsRefusedWithItsLine() {
        assertEquals("bad.at:1: AT_SETUP with no AT_CLEANUP", assertThrows(Autotests.Malformed.class, () -> Autotests.read("bad.at", "AT_SETUP([x])\n")).getMessage());
        assertEquals("bad.at:2: a quote that does not close", assertThrows(Autotests.Malformed.class,
                () -> Autotests.read("bad.at", "AT_SETUP([x])\nAT_DATA([f], [unclosed)\nAT_CLEANUP\n")).getMessage());
        assertEquals("bad.at:1: AT_CLEANUP with no AT_SETUP", assertThrows(Autotests.Malformed.class, () -> Autotests.read("bad.at", "AT_CLEANUP\n")).getMessage());
    }

    @Test
    void eachCaseIsWrittenToAFolderOfItsOwn() throws Exception {
        Path out = Files.createTempDirectory("autotests");
        Autotests.write(Autotests.read("sample.at", SAMPLE), out);
        assertTrue(Files.readString(out.resolve("sample-2/prog.cob")).contains("PROGRAM-ID. prog."));
        assertEquals("A, (B) [[1]]\n", Files.readString(out.resolve("sample-2/check-1.stdout")));
        assertEquals("$COBCRUN_DIRECT ./prog\n", Files.readString(out.resolve("sample-2/check-1.sh")));
        assertTrue(Files.readString(out.resolve("sample-19/case.txt")).startsWith("title a skipped one\n"));
        String nested = "AT_SETUP([n])\nAT_DATA([SUB/copy.inc], [x])\nAT_CLEANUP\n";
        Autotests.write(Autotests.read("nested.at", nested), out);
        assertEquals("x", Files.readString(out.resolve("nested-1/SUB/copy.inc")));
        assertThrows(Autotests.Malformed.class, () -> Autotests.write(Autotests.read("escape.at", nested.replace("SUB/", "../../")), out));
    }

    @Test
    void aCasesCompileFlagsAreExtractedByCode() {
        String text = String.join("\n",
                "AT_SETUP([flags])",
                "AT_DATA([prog.cob], [x])",
                "AT_CHECK([$COMPILE -o ascii ascii.c], [0], [], [])",
                "AT_CHECK([ENVX=1 $COMPILE -free -D ONE -DTWO=2 -std=ibm -o prog \\",
                "  prog.cob], [0], [], [])",
                "AT_CHECK([$COBCRUN_DIRECT ./prog], [0], [out",
                "])",
                "AT_CHECK([$COMPILE -fother prog.cob], [0], [], [])",
                "AT_CLEANUP",
                "AT_SETUP([none])",
                "AT_CHECK([$COMPILE_ONLY prog.cob], [0], [], [])",
                "AT_CLEANUP",
                "");
        List<Autotests.Case> cases = Autotests.read("f.at", text);
        assertEquals(List.of("-free", "-DONE", "-DTWO=2", "-std=ibm", "-o"), cases.get(0).flags(), "the compile before the run that expects output");
        assertEquals(List.of(), cases.get(1).flags());
    }

    @Test
    void theFlagsAreWrittenBesideTheCase() throws Exception {
        Path out = Files.createTempDirectory("autotests");
        Autotests.write(Autotests.read("f.at", "AT_SETUP([x])\nAT_CHECK([$COMPILE -free prog.cob])\nAT_CLEANUP\n"), out);
        assertEquals("-free\n", Files.readString(out.resolve("f-1/flags.txt")));
    }
}

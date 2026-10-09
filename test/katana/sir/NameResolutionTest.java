package katana.sir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;
import katana.front.Copybooks;
import katana.front.Parse;
import katana.sir.build.Build;
import katana.sir.types.Item;
import katana.sir.types.Program;
import katana.sir.types.Statement;
import katana.sir.types.Value;
import org.junit.jupiter.api.Test;

/**
 * J-148's sweep: wherever sir/build turns a name into an item, `X OF Y` reaches it as two words, and a
 * name that resolves to nothing is a build finding, never a silent 0 or the first item of that name.
 * Each case here resolved wrongly, or silently, before the sweep.
 */
class NameResolutionTest {
    private static final String HASH = "sha256:25d21d9dc3d4db6554821c53cb0927a5bcc95f750d04631a50e43bb636866296";

    private static Build.Result build(String... lines) {
        String source = java.util.Arrays.stream(lines).map(l -> (l.startsWith("*") ? "      " : "       ") + l).collect(Collectors.joining("\n", "", "\n"));
        return Build.program(Parse.text(source, new Copybooks(List.of(), n -> 8)), HASH);
    }

    private static int id(Program p, String name, String parent) {
        return p.items().stream().filter(i -> i.name().equals(name) && (parent == null || p.items().get(i.parent() - 1).name().equals(parent))).findFirst().orElseThrow().id();
    }

    private static final String[] DATA = {
        "IDENTIFICATION DIVISION.", "PROGRAM-ID. NAMES.", "DATA DIVISION.", "WORKING-STORAGE SECTION.",
        "01 G1.", "   05 N PIC 9 VALUE 1.", "   05 K PIC 9.", "01 G2.", "   05 N PIC 9 VALUE 2.", "   05 K PIC 9.",
        "01 T1.", "   05 E1 OCCURS 1 TO 5 DEPENDING ON N OF G2.", "      10 K PIC 9.", "      10 V PIC X.",
        "01 T2.", "   05 E2 OCCURS 3 ASCENDING KEY IS K OF E2 INDEXED BY X2.", "      10 K PIC 9.",
        "01 R.", "   05 S1.", "      10 A PIC X.", "      10 B PIC X.", "   05 S2.", "      10 A PIC X.", "      10 B PIC X.",
        "66 RS RENAMES A OF S2 THRU B OF S2.",
    };

    @Test
    void qualifiersReachTheDataMap() {
        Build.Result r = build(concat(DATA, "PROCEDURE DIVISION.", "MAIN-PARA.", "    STOP RUN."));
        Program p = r.program();
        Item e1 = p.items().get(id(p, "E1", "T1") - 1), e2 = p.items().get(id(p, "E2", "T2") - 1), rs = p.items().get(id(p, "RS", null) - 1);
        assertEquals(id(p, "N", "G2"), e1.occurs().dependingOn(), "DEPENDING ON N OF G2, not G1's N");
        assertEquals(id(p, "K", "E2"), e2.occurs().keys().get(0).item(), "KEY IS K OF E2, not G1's K");
        assertEquals(p.items().get(id(p, "A", "S2") - 1).offset(), rs.offset(), "RENAMES A OF S2, not S1's A");
        assertEquals(List.of(), r.findings());
    }

    @Test
    void anAmbiguousOrUnresolvedNameIsAFinding() {
        String[] data = java.util.Arrays.copyOf(DATA, DATA.length);
        data[11] = "   05 E1 OCCURS 1 TO 5 DEPENDING ON N.";
        data[16] = "   05 E2 OCCURS 3 ASCENDING KEY IS NOPE INDEXED BY X2.";
        Build.Result r = build(concat(data, "PROCEDURE DIVISION.", "MAIN-PARA.", "    STOP RUN."));
        String findings = String.join("\n", r.findings());
        assertTrue(findings.contains("N is ambiguous: 2 items"), findings);
        assertTrue(findings.contains("NOPE names no item"), findings);
    }

    @Test
    void anUnresolvedUsingIsAFinding() {
        Build.Result r = build("IDENTIFICATION DIVISION.", "PROGRAM-ID. CALLED.", "DATA DIVISION.", "LINKAGE SECTION.", "01 L PIC X.",
                "PROCEDURE DIVISION USING NOPE.", "MAIN-PARA.", "    GOBACK.");
        assertTrue(String.join("\n", r.findings()).contains("PROCEDURE DIVISION USING NOPE at "), r.findings().toString());
    }

    @Test
    void linageCounterOfAFileNamesTheFile() {
        Build.Result r = build("IDENTIFICATION DIVISION.", "PROGRAM-ID. LINES.", "ENVIRONMENT DIVISION.", "INPUT-OUTPUT SECTION.", "FILE-CONTROL.",
                "    SELECT LISTING ASSIGN TO \"LST\".", "DATA DIVISION.", "FILE SECTION.", "FD LISTING LINAGE IS 10 LINES.", "01 PR PIC X(10).",
                "WORKING-STORAGE SECTION.", "01 W PIC 99.", "PROCEDURE DIVISION.", "MAIN-PARA.", "    MOVE LINAGE-COUNTER OF LISTING TO W", "    STOP RUN.");
        Statement move = r.program().procedure().paragraphs().stream().filter(g -> g.name().equals("MAIN-PARA")).findFirst().orElseThrow().statements().get(0);
        assertTrue(move.operands().stream().anyMatch(o -> o.value().equals(new Value.Expression("LINAGE-COUNTER OF", List.of(new Value.File("LISTING"))))), move.toString());
    }

    /** LINAGE's data-names can be qualified, as any name but in REDEFINES (LR 6.4 p. 67; J-160). */
    @Test
    void linageNamesAreQualified() {
        Build.Result r = build("IDENTIFICATION DIVISION.", "PROGRAM-ID. PAGES.", "ENVIRONMENT DIVISION.", "INPUT-OUTPUT SECTION.", "FILE-CONTROL.",
                "    SELECT LISTING ASSIGN TO \"LST\".", "DATA DIVISION.", "FILE SECTION.", "FD LISTING LINAGE IS L OF G2 LINES WITH FOOTING AT L OF G1.",
                "01 PR PIC X(10).", "WORKING-STORAGE SECTION.", "01 G1.", "   05 L PIC 99 VALUE 8.", "01 G2.", "   05 L PIC 99 VALUE 10.",
                "PROCEDURE DIVISION.", "MAIN-PARA.", "    STOP RUN.");
        Program p = r.program();
        var linage = p.files().get(0).fd().linage();
        assertEquals(new Value.Field(id(p, "L", "G2"), List.of(), null, null), linage.body(), "L OF G2, not G1's L");
        assertEquals(new Value.Field(id(p, "L", "G1"), List.of(), null, null), linage.footing());
        assertEquals(List.of(), r.findings());
    }

    /** A comment line between a clause's words is not part of the clause: SQ401M's ASSIGN is TAPE2 (J-151). */
    @Test
    void aCommentInsideAClauseIsNotPartOfIt() {
        Build.Result r = build("IDENTIFICATION DIVISION.", "PROGRAM-ID. ASSIGNS.", "ENVIRONMENT DIVISION.", "INPUT-OUTPUT SECTION.", "FILE-CONTROL.",
                "    SELECT TFIL ASSIGN", "*Message expected for above statement: NON-CONFORMING STANDARD", "    TAPE2.", "DATA DIVISION.", "FILE SECTION.",
                "FD TFIL.", "01 TREC PIC X.", "PROCEDURE DIVISION.", "MAIN-PARA.", "    STOP RUN.");
        assertEquals("TAPE2", r.program().files().get(0).assign());
    }

    /**
     * The rule the sweep leaves (J-148): in sir/build a name becomes an item through `Names.resolve`,
     * which records a finding when it names nothing; `Names.find`, which does not, is for `Values` alone,
     * which tries what else a name can be and ends in a finding itself.
     */
    @Test
    void onlyValuesFindsWithoutAFinding() throws Exception {
        java.nio.file.Path build = java.nio.file.Path.of(System.getProperty("katana.root", ".")).resolve("sir/build");
        List<String> callers = new java.util.ArrayList<>();
        try (var files = java.nio.file.Files.list(build)) {
            for (var f : files.sorted().toList()) {
                String name = f.getFileName().toString();
                if (name.equals("Values.java") || name.equals("Names.java")) continue;
                if (java.util.regex.Pattern.compile("\\.find\\(").matcher(java.nio.file.Files.readString(f)).find()) callers.add(name);
            }
        }
        assertEquals(List.of(), callers, "a name looked up with Names.find outside Values can become a silent 0; use Names.resolve");
    }

    private static String[] concat(String[] a, String... b) {
        String[] out = java.util.Arrays.copyOf(a, a.length + b.length);
        System.arraycopy(b, 0, out, a.length, b.length);
        return out;
    }
}

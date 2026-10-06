package katana.sir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import katana.findings.Finding;
import katana.front.Copybooks;
import katana.front.Parse;
import katana.profile.Profile;
import katana.sir.build.Build;
import katana.sir.check.NonExact;
import katana.sir.types.Program;
import org.junit.jupiter.api.Test;

/** 2.7's check: TRUNC(OPT) and an overlapping PERFORM range are findings from the IR, before any lowering; and the other kinds. */
class NonExactTest {
    private static final Path ROOT = Path.of(System.getProperty("katana.root", "."));

    private static Profile profile(String trunc) throws Exception {
        return Profile.read(Files.readString(ROOT.resolve("profile/ibm.profile")).replace("ibm.trunc STD", "ibm.trunc " + trunc));
    }

    private static List<Finding.Kind> kinds(String procedure, String data, Profile profile) {
        String text = String.join("\n",
                "       IDENTIFICATION DIVISION.", "       PROGRAM-ID. P.", "       DATA DIVISION.", "       WORKING-STORAGE SECTION.",
                "       01  N        PIC 9(4).", data, "       PROCEDURE DIVISION.", procedure, "");
        Program p = Build.program(Parse.text(text, new Copybooks(List.of(), n -> 8)), profile.hash()).program();
        return NonExact.findings(p, profile).stream().map(Finding::kind).toList();
    }

    private static final String PARAGRAPHS = String.join("\n", "       P1.", "           DISPLAY 1.", "       P2.", "           DISPLAY 2.",
            "       P3.", "           DISPLAY 3.", "       P4.", "           STOP RUN.");

    @Test
    void anOverlappingPerformRangeIsAFinding() throws Exception {
        Profile ibm = profile("STD");
        String overlap = "       MAIN.\n           PERFORM P1 THRU P3\n           PERFORM P2 THRU P4\n           STOP RUN.\n" + PARAGRAPHS;
        assertEquals(List.of(Finding.Kind.NE_02), kinds(overlap, "", ibm));
        String nested = "       MAIN.\n           PERFORM P1 THRU P4\n           PERFORM P2 THRU P3\n           STOP RUN.\n" + PARAGRAPHS;
        assertEquals(List.of(), kinds(nested, "", ibm));
    }

    @Test
    void aGoToOutOfAPerformRangeIsAFinding() throws Exception {
        Profile ibm = profile("STD");
        String out = "       MAIN.\n           PERFORM P1 THRU P2\n           STOP RUN.\n" + PARAGRAPHS.replace("DISPLAY 1.", "GO TO P4.");
        assertEquals(List.of(Finding.Kind.HY_01), kinds(out, "", ibm));
        String inside = "       MAIN.\n           PERFORM P1 THRU P3\n           STOP RUN.\n" + PARAGRAPHS.replace("DISPLAY 1.", "GO TO P3.");
        assertEquals(List.of(), kinds(inside, "", ibm));
    }

    @Test
    void truncOptOverABinaryItemIsAFinding() throws Exception {
        String data = "       01  B        PIC S9(4) COMP.";
        assertEquals(List.of(Finding.Kind.NE_01), kinds("           STOP RUN.", data, profile("OPT")));
        assertEquals(List.of(), kinds("           STOP RUN.", data, profile("STD")));
    }

    @Test
    void theOtherKinds() throws Exception {
        Profile ibm = profile("STD");
        assertEquals(List.of(Finding.Kind.NE_03), kinds("       MAIN.\n           PERFORM MAIN.\n           STOP RUN.", "", ibm));
        assertEquals(List.of(Finding.Kind.NE_04), kinds("           DIVIDE 2 INTO N.\n           DIVIDE 2 INTO N ON SIZE ERROR DISPLAY 0 END-DIVIDE.\n           STOP RUN.", "", ibm));
        assertEquals(List.of(Finding.Kind.NE_04), kinds("           COMPUTE N = N / 2.\n           STOP RUN.", "", ibm));
        assertEquals(List.of(Finding.Kind.NE_05, Finding.Kind.NE_06),
                kinds("           STOP RUN.", "       01  T.\n           05  E PIC X OCCURS 1 TO 9 DEPENDING ON N.\n       01  F        COMP-2.", ibm));
    }

    @Test
    void theProfileMustBeTheOneTheIrWasBuiltUnder() throws Exception {
        Profile std = profile("STD"), opt = profile("OPT");
        Program p = Build.program(Parse.text("       IDENTIFICATION DIVISION.\n       PROGRAM-ID. P.\n       PROCEDURE DIVISION.\n           STOP RUN.\n",
                new Copybooks(List.of(), n -> 8)), std.hash()).program();
        assertThrows(IllegalArgumentException.class, () -> NonExact.findings(p, opt));
    }
}

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

/** The 85 Standard's obsolete elements as IBM keeps them, lowered (research §18, J-177). */
class ObsoleteTest {
    private static final String HASH = "sha256:25d21d9dc3d4db6554821c53cb0927a5bcc95f750d04631a50e43bb636866296";

    private static String java(String... procedure) {
        List<String> lines = new java.util.ArrayList<>(List.of("IDENTIFICATION DIVISION.", "PROGRAM-ID. OBS.", "PROCEDURE DIVISION."));
        lines.addAll(List.of(procedure));
        Program p = Build.program(Parse.text(lines.stream().map(l -> "       " + l).collect(Collectors.joining("\n", "", "\n")), new Copybooks(List.of(), n -> 8)), HASH).program();
        return Lower.java(p, "Obs").java();
    }

    @Test
    void stopLiteralTellsTheOperatorAndGoesOn() {
        String java = java("MAIN SECTION.", "M1.", "    STOP \"MOUNT TAPE\".", "    DISPLAY \"AFTER\".", "    STOP RUN.");
        assertTrue(java.contains("err.display(true, Literal.bytes(Literal.text(\"MOUNT TAPE\", cp)));"), java);
        assertTrue(java.indexOf("MOUNT TAPE") < java.indexOf("\"AFTER\""), "LR p. 457: execution continues at the next executable statement");
    }

    @Test
    void aGoToAlteredInASectionIsReadFromTheTableWhereAnotherSectionHasItsName() {
        String java = java("ONE SECTION.", "A1.", "    ALTER P IN TWO TO PROCEED TO Q.", "    GO TO P IN TWO.", "P.", "    GO TO Q.",
                "Q.", "    STOP RUN.", "TWO SECTION.", "P.", "    GO TO Q IN ONE.", "R.", "    STOP RUN.");
        assertTrue(java.contains("d.goTo(4, "), "TWO's P (paragraph 4) reads its ALTERed target (NIST OBNC2M's PARA-6B): " + java);
        assertFalse(java.contains("d.goTo(2, "), "ONE's P is altered by nothing");
    }
}

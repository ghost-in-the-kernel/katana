package katana.lower;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;
import katana.front.Copybooks;
import katana.front.Parse;
import katana.sir.build.Build;
import katana.sir.types.Procedure;
import katana.sir.types.Program;
import org.junit.jupiter.api.Test;

/** Sections' priority-numbers and the altered GO TO, from the source to the lowered Java (NIST SG, J-175). */
class SegmentsTest {
    private static final String HASH = "sha256:cc4bbf084793b6f5280da870d1aa7b6fb58d535dcfaf091d8436a36a66c4dae1";

    private static final Program P = Build.program(Parse.text(java.util.Arrays.stream(new String[] {
        "IDENTIFICATION DIVISION.", "PROGRAM-ID. SEGS.", "PROCEDURE DIVISION.",
        "MAIN SECTION.", "M1.", "    ALTER SWITCH TO PROCEED TO DONE.", "    GO TO IND.",
        "IND SECTION 60.", "SWITCH.", "    GO TO.", "FALLEN.", "    DISPLAY \"FELL\".",
        "DONE.", "    STOP RUN."}).map(l -> "       " + l).collect(Collectors.joining("\n", "", "\n")), new Copybooks(List.of(), n -> 8)), HASH).program();

    @Test
    void aSectionCarriesItsPriorityNumber() {
        assertEquals(List.of(-1, 60), P.procedure().sections().stream().map(Procedure.Section::priority).toList());
    }

    @Test
    void anAlteredGoToIsAGoToNamingNoProcedure() {
        Procedure.Paragraph sw = P.procedure().paragraphs().stream().filter(g -> g.name().equals("SWITCH")).findFirst().orElseThrow();
        assertEquals(1, sw.statements().size(), "LR p. 347: `SWITCH. GO TO.` is a statement, not an empty paragraph");
        assertEquals("GO TO", sw.statements().get(0).verb());
        assertTrue(sw.statements().get(0).operands().isEmpty());
    }

    @Test
    void aSegmentedProgramEntersItsParagraphsThroughTheDispatcher() {
        String java = Lower.java(P, "Segs").java();
        assertTrue(java.contains("new Dispatcher(new int[] {-1, -1, 60, 60, 60})"), java);
        assertTrue(java.contains("d.enter(2);"), "each paragraph's start is an entry");
        assertTrue(java.contains("int g = d.goTo(2, -1);"), "before any ALTER the GO TO continues");
    }
}

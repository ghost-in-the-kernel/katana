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
 * SIR-12 (research §24): a nested program names its container's GLOBAL records as its own, copied after
 * its items; a name it defines itself is its own (LR 6.4 p. 197); its PROGRAM-ID's COMMON and INITIAL.
 */
class GlobalsTest {
    private static final String HASH = "sha256:cc4bbf084793b6f5280da870d1aa7b6fb58d535dcfaf091d8436a36a66c4dae1";

    @Test
    void aNestedProgramSeesItsContainersGlobalRecordsUnlessItDefinesTheName() {
        List<String> lines = List.of("IDENTIFICATION DIVISION.", "PROGRAM-ID. OUTERP.", "DATA DIVISION.", "WORKING-STORAGE SECTION.",
                "01 G GLOBAL.", "   02 A PIC X.", "   02 B PIC 9.", "01 H PIC X.", "01 B2 GLOBAL PIC X.", "PROCEDURE DIVISION.", "P.", "    STOP RUN.",
                "IDENTIFICATION DIVISION.", "PROGRAM-ID. INNERP IS COMMON.", "DATA DIVISION.", "WORKING-STORAGE SECTION.", "01 B2 PIC 9.",
                "PROCEDURE DIVISION.", "Q.", "    MOVE \"X\" TO A. MOVE 1 TO B2.", "END PROGRAM INNERP.", "END PROGRAM OUTERP.");
        Parse.Result parse = Parse.text(lines.stream().map(l -> "       " + l).collect(Collectors.joining("\n", "", "\n")), new Copybooks(List.of(), n -> 8));
        assertEquals(List.of(), parse.errors());
        Build.Result built = Build.programs(parse, HASH).get(0);
        assertEquals(List.of(), built.findings());
        Program outer = built.program(), inner = outer.nested().get(0);
        assertTrue(inner.header().common() && !inner.header().initial() && !outer.header().common());
        assertEquals(List.of("B2", "G", "A", "B", "B2"), inner.items().stream().map(Item::name).toList(), "its own item, then G's record and B2's, copied");
        assertEquals(List.of(new Program.Global(2, 1, 1), new Program.Global(5, 1, 5)), inner.globals());
        List<Statement> moves = inner.procedure().paragraphs().stream().flatMap(g -> g.statements().stream()).toList();
        assertEquals(3, ((Value.Field) moves.get(0).operands().stream().filter(o -> o.value() instanceof Value.Field).findFirst().orElseThrow().value()).item(), "A, the copy of G's");
        assertEquals(1, ((Value.Field) moves.get(1).operands().stream().filter(o -> o.value() instanceof Value.Field).findFirst().orElseThrow().value()).item(), "B2, its own");
    }
}

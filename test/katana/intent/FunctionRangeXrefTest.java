package katana.intent;

import katana.support.Small;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import katana.sir.types.Program;
import org.junit.jupiter.api.Test;

/** T-F2 on a listing small enough to read, in cobc 3.1.2's `-Xref` form (research §33, §34): a range only PERFORMed holds; a GO TO into it from outside refutes it. */
class FunctionRangeXrefTest {
    private static final String MAIN = "MAIN.\n    PERFORM A THRU A-EXIT.\n    STOP RUN.\nA.\n    IF I = 1 GO TO A-EXIT.\n    DISPLAY 'A'.\nA-EXIT.\n    EXIT.\n";

    /** The listing cobc writes for MAIN, then `more`'s lines, (a paragraph Z holding a GO TO A-EXIT), its labels and their references. */
    private static String listing(String more) {
        String[] src = ("IDENTIFICATION DIVISION.\nPROGRAM-ID. P.\nDATA DIVISION.\nWORKING-STORAGE SECTION.\n01 I PIC 9.\nPROCEDURE DIVISION.\n" + MAIN + more).split("\n");
        StringBuilder out = new StringBuilder("LINE    PG/LN  A...B............................................................\n\n");
        for (int i = 0; i < src.length; i++) out.append(String.format("%06d         ", i + 1)).append(src[i]).append('\n');
        out.append("\nLABEL                          DEFINED                REFERENCES\n\nE P                            6\nP MAIN                         7\n");
        out.append("P A                            10       8                              x1\n");
        out.append(more.isEmpty() ? "P A-EXIT                       13       8       11                      x2\n" : "P A-EXIT                       13       8       11      16              x3\nP Z                            15\n");
        return out.toString();
    }

    private static List<String> refused(String goToFrom) {
        Program p = Small.program("P", "01 I PIC 9.\n", MAIN + goToFrom);
        return FunctionRangeXref.refusals(FunctionRangeXref.parse(listing(goToFrom)), p, FunctionRangeStructure.range(p, "A THRU A-EXIT"));
    }

    @Test
    void aRangeOnlyPerformedHoldsAndAGoToIntoItRefutes() {
        assertEquals(List.of(), refused(""));
        List<String> why = refused("Z.\n    GO TO A-EXIT.\n");
        assertTrue(why.stream().anyMatch(w -> w.contains("outside it, names A-EXIT")), why.toString());
    }

    @Test
    void theReferencesCobcCountsAreRead() {
        FunctionRangeXref.Listing l = FunctionRangeXref.parse(listing(""));
        assertEquals(3, l.counted());
        assertEquals(FunctionRangeXref.Kind.PERFORM, FunctionRangeXref.kind(l, "A", 8));
        assertEquals(FunctionRangeXref.Kind.THRU, FunctionRangeXref.kind(l, "A-EXIT", 8));
        assertEquals(FunctionRangeXref.Kind.GO_TO, FunctionRangeXref.kind(l, "A-EXIT", 11));
    }
}

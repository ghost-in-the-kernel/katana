package katana.lower;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import katana.front.Copybooks;
import katana.front.Parse;
import katana.sir.build.Build;
import katana.sir.types.Program;
import katana.sir.types.Value;
import org.junit.jupiter.api.Test;

/** `Places`, one case per operand kind (tithe, J-170): what has a PICTURE, and each kind that has none. */
class PlacesTest {
    private static final String HASH = "sha256:cc4bbf084793b6f5280da870d1aa7b6fb58d535dcfaf091d8436a36a66c4dae1";

    private static final Program P = Build.program(Parse.text(java.util.Arrays.stream(new String[] {
        "IDENTIFICATION DIVISION.", "PROGRAM-ID. PLACES.", "ENVIRONMENT DIVISION.", "INPUT-OUTPUT SECTION.", "FILE-CONTROL.",
        "    SELECT PAGED ASSIGN TO \"P1\".", "    SELECT NAMED ASSIGN TO \"P2\".", "DATA DIVISION.", "FILE SECTION.",
        "FD PAGED LINAGE IS 60 LINES.", "01 PR PIC X(10).", "FD NAMED LINAGE IS LN LINES.", "01 NR PIC X(10).",
        "WORKING-STORAGE SECTION.", "01 LN PIC 9(3) VALUE 66.", "01 AMOUNT PIC S9(5)V99.", "01 EDITED PIC ZZZ9.9.", "01 SCALED PIC 9(3)PP.",
        "01 T.", "   05 E PIC X OCCURS 9 TIMES INDEXED BY IX.", "01 PTR USAGE POINTER.", "01 FL USAGE COMP-2.", "01 NAME PIC X(5).",
        "PROCEDURE DIVISION.", "MAIN-PARA.", "    STOP RUN."}).map(l -> "       " + l).collect(Collectors.joining("\n", "", "\n")), new Copybooks(List.of(), n -> 8)), HASH).program();
    private static final Exprs X = new Exprs(P, new ArrayList<>());

    private static Places.Size of(Value v) {
        return Places.of(v, X);
    }

    private static Value field(String name) {
        return new Value.Field(P.items().stream().filter(i -> i.name().equals(name)).findFirst().orElseThrow().id(), List.of(), null, null);
    }

    private static int places(Value v) {
        Places.Size s = of(v);
        assertNull(s.refused(), v + ": " + s.refused());
        return s.places();
    }

    @Test
    void anItemWithAPictureHasItsIntegerDigits() {
        assertEquals(5, places(field("AMOUNT")));
        assertEquals(5, places(field("SCALED")), "9(3)PP holds up to 99900: its Ps are integer places");
        assertEquals(4, places(field("EDITED")), "a numeric-edited quotient sizes DIVIDE's REMAINDER (NC203A)");
    }

    @Test
    void anIndexHasTheFullwordsNine() {
        assertEquals(9, places(field("IX")), "J-167: zero places wrapped VARYING IX at 100");
    }

    @Test
    void linageCounterHasItsIntegersDigitsOrItsDataNames() {
        assertEquals(2, places(new Value.Expression("LINAGE-COUNTER OF", List.of(new Value.File("PAGED")))));
        assertEquals(3, places(new Value.Expression("LINAGE-COUNTER OF", List.of(new Value.File("NAMED")))));
    }

    @Test
    void lengthOfAndTheNumericRegistersHaveIbmsImplicitPictures() {
        assertEquals(9, places(new Value.Expression("LENGTH OF", List.of(field("NAME")))));
        assertEquals(4, places(new Value.Name("register", "RETURN-CODE")));
        assertEquals(5, places(new Value.Name("register", "TALLY")));
    }

    @Test
    void literalsAndZeroHaveTheirDigits() {
        assertEquals(2, places(new Value.Literal("-12.3", Value.Literal.Kind.NUMERIC, "-123", 1)));
        assertEquals(1, places(new Value.Figurative("ZERO", null)));
    }

    @Test
    void aPointerAFloatAnAddressAndAFunctionAreRefusedWithWhy() {
        for (Value v : List.of(field("PTR"), field("FL"), new Value.Expression("ADDRESS OF", List.of(field("NAME"))),
                new Value.Expression("FUNCTION RANDOM", List.of()), field("NAME"))) {
            assertNotNull(of(v).refused(), v + " has places");
        }
    }
}

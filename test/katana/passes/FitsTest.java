package katana.passes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import katana.intent.rules.Rules;
import katana.lower.Lower;
import katana.sir.types.Program;
import org.junit.jupiter.api.Test;

/** 6.6b's PICTURE rule on programs small enough to read (research §45): a store that always fits assigned, and each obligation broken keeping the store. */
class FitsTest {
    private static final String DATA = "01 A PIC 9(3).\n01 B PIC 9(3).\n01 C PIC 9(4).\n01 S PIC S9(4).\n01 V PIC 9(4)V9.\n";

    private static Program program(String procedure) {
        return katana.support.Small.program(DATA, "MAIN.\n    MOVE 5 TO A.\n    MOVE 7 TO B.\n" + procedure + "    DISPLAY A B C S V.\n    STOP RUN.\n");
    }

    private static Structuring.Result plan(Program p) {
        return Structuring.plan(p, Rules.infer(p));
    }

    @Test
    void aValueThatAlwaysFitsIsAssigned() {
        Program p = program("    COMPUTE C = A + B.\n    COMPUTE S = A - B.\n    ADD A B GIVING V.\n");
        Structuring.Result r = plan(p);
        assertEquals(3, r.plan().fits().size(), r.refused().toString());
        String java = Lower.java(p, "P", r.plan()).java();
        assertTrue(java.contains("c = (a + b);") && java.contains("s = (a - b);"), java);
        assertTrue(java.contains("v = java.math.BigDecimal.valueOf(a).add(java.math.BigDecimal.valueOf(b)).setScale(1);"), java);
    }

    @Test
    void eachObligationBrokenKeepsTheStore() {
        assertKept("    ADD A TO C.\n", "integer places", "an accumulator: its sum has a place more than the receiver");
        assertKept("    COMPUTE C = A * B.\n", "integer places", "a product of six places into four");
        assertKept("    COMPUTE C = A - B.\n", "unsigned", "an unsigned receiver of a difference");
        assertKept("    COMPUTE C = S + 1.\n", "unsigned", "an unsigned receiver of a signed operand");
        assertKept("    COMPUTE C = A + 0.5.\n", "decimal places", "a decimal place the receiver drops");
        assertKept("    COMPUTE C V = A + B.\n", "one promoted receiver", "two receivers");
    }

    private static void assertKept(String procedure, String why, String what) {
        Program p = program(procedure);
        Structuring.Result r = plan(p);
        assertEquals(1, r.plan().exact().size(), what + ": exact first, " + r.refused());
        assertTrue(r.plan().fits().isEmpty() && r.refused().entrySet().stream().anyMatch(e -> e.getKey().endsWith(" fits") && e.getValue().stream().anyMatch(w -> w.contains(why))), what + ": " + r.refused());
        assertTrue(Lower.java(p, "P", r.plan()).java().contains("Scalars.store("), what + ": the store kept");
    }
}

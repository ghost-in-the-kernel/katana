package katana.passes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import katana.intent.rules.Rules;
import katana.lower.Lower;
import katana.sir.types.Program;
import org.junit.jupiter.api.Test;

/** 6.4 on programs small enough to read (research §41): the statements written as exact arithmetic, the Java each becomes, and each obligation refused. */
class ExactTest {
    private static Program program(String data, String procedure) {
        return katana.support.Small.program(data, "MAIN.\n" + procedure + "    DISPLAY C T.\n    STOP RUN.\n");
    }

    private static final String DATA = "01 C PIC 9(5).\n01 T PIC S9(5)V99.\n01 R.\n   05 F PIC 9(3).\n01 N5 PIC 9(4) COMP-5.\n";

    @Test
    void anIntegerSumIsLongArithmeticAndADecimalOneBigDecimals() {
        Program p = program(DATA, "    ADD 1 TO C.\n    COMPUTE T ROUNDED = C * 2 + 1.5.\n    SUBTRACT 3 FROM C GIVING C.\n");
        Passes.Result r = Passes.plan(p, Rules.infer(p));
        assertEquals(3, r.plan().exact().size(), r.refused().toString());
        String java = Lower.java(p, "P", r.plan()).java();
        assertTrue(java.contains("Scalars.store(Decimal.exact((c + 1)), 5, 0, false, false, false,"), java);
        assertTrue(java.contains("Decimal.exact(java.math.BigDecimal.valueOf(c).multiply(new java.math.BigDecimal(\"2\")).add(new java.math.BigDecimal(\"1.5\")))"), "a decimal operand makes the expression BigDecimal's");
        assertTrue(java.contains("Decimal.exact((c - 3))"), "SUBTRACT ... GIVING is the minuend less the subtrahends");
    }

    /** 6.4b (R-069): a record field is read under the guard, the exact form when its bytes are a valid number within its PICTURE, else the step-by-step one. */
    @Test
    void aRecordFieldIsReadExactlyUnderTheGuard() {
        Program p = program(DATA, "    ADD F TO C.\n    MOVE SPACES TO R.\n");
        Passes.Result r = Passes.plan(p, Rules.infer(p));
        assertEquals(1, r.plan().exact().size(), r.refused().toString());
        String java = Lower.java(p, "P", r.plan()).java();
        // R is a record class (6.7): F is its member `r.f`, read in the exact form through its getter.
        assertTrue(java.contains("if (valid(r.f)) {") && java.contains("Decimal.exact((c + r.f()))") && java.contains("else {"), java);
        assertTrue(java.contains("private static boolean valid(Field... operands) {") && java.contains("if (!Classes.exact(f)) return false;"), "one guard per program");
    }

    @Test
    void eachObligationBrokenKeepsTheStepWiseForm() {
        assertRefused("    ADD N5 TO C.\n", "native binary");
        assertRefused("    ADD F (1:2) TO C.\n", "reference-modified");
        assertRefused("    COMPUTE C = C / 2.\n", "a /");
        assertRefused("    DIVIDE 2 INTO C.\n", "a division");
        assertRefused("    ADD 1 TO C T.\n", "several receivers");
        assertRefused("    COMPUTE T = C.\n", "no operation");
        assertRefused("    ADD 1 TO F.\n    MOVE R TO R.\n    COMPUTE C = C * C * C * C * C * C * C.\n", "past the cap");
    }

    private static void assertRefused(String procedure, String why) {
        Program p = program(DATA, procedure);
        Passes.Result r = Passes.plan(p, Rules.infer(p));
        assertTrue(r.refused().entrySet().stream().anyMatch(e -> !e.getKey().startsWith("item ") && e.getValue().stream().anyMatch(w -> w.contains(why))), why + ": " + r.refused());
    }
}

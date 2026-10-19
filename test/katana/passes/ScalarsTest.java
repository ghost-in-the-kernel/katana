package katana.passes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import katana.front.Copybooks;
import katana.front.Parse;
import katana.intent.rules.Rules;
import katana.lower.Lower;
import katana.sir.build.Build;
import katana.sir.types.Item;
import katana.sir.types.Program;
import org.junit.jupiter.api.Test;

/** 6.3 on programs small enough to read (research §39, R-068): an item each obligation keeps, an item each one refuses, and the Java a promoted one becomes. */
class ScalarsTest {
    private static Program program(String data, String procedure) {
        String source = ("IDENTIFICATION DIVISION.\nPROGRAM-ID. P.\nDATA DIVISION.\nWORKING-STORAGE SECTION.\n" + data + "PROCEDURE DIVISION.\nMAIN.\n" + procedure + "    STOP RUN.\n")
                .lines().map(l -> "       " + l).collect(Collectors.joining("\n", "", "\n"));
        return Build.program(Parse.text(source, new Copybooks(List.of(), n -> 8)), "sha256:" + "0".repeat(64)).program();
    }

    private static int id(Program p, String name) {
        return p.items().stream().filter(i -> i.name().equals(name)).mapToInt(Item::id).findFirst().orElseThrow();
    }

    @Test
    void aCounterAndATotalBecomeVariablesEveryStoreExact() {
        Program p = program("01 WS-COUNT PIC 9(3) VALUE 5.\n01 WS-TOTAL PIC S9(5)V99 COMP-3.\n",
                "    ADD 1 TO WS-COUNT.\n    COMPUTE WS-TOTAL = WS-COUNT / 3.\n    MOVE 7 TO WS-COUNT.\n    MOVE 1000 TO WS-COUNT.\n    IF WS-COUNT > 2 DISPLAY WS-COUNT WS-TOTAL.\n");
        Structuring.Result r = Structuring.plan(p, Rules.infer(p));
        int count = id(p, "WS-COUNT"), total = id(p, "WS-TOTAL");
        assertEquals(Set.of(count, total), r.plan().scalars(), r.refused().toString());
        String java = Lower.java(p, "P", r.plan()).java();
        assertTrue(java.contains("private long wsCount;") && java.contains("private java.math.BigDecimal wsTotal"), java);
        assertFalse(java.contains("private Field f" + count + ";") || java.contains("private Field f" + total + ";"), "the promoted items' fields are gone");
        assertTrue(java.contains("wsCount = 5;") && java.contains("wsCount = 7;"), "a literal the item holds as written is assigned");
        assertTrue(java.contains("wsCount = Scalars.longOf(Scalars.received(A" + count + ", cp, scratch -> Move.move(Literal.number(\"1000\""), "a literal it would truncate goes through MOVE's own truncation");
        assertTrue(java.contains("sz |= Scalars.store(") && java.contains("stored -> wsCount = Scalars.longOf(stored)"), "an arithmetic store keeps Decimal.store's truncation and size error (R-068)");
        assertTrue(java.contains("Scalars.field(A" + count + ", cp, Scalars.of(wsCount, 0))"), "a DISPLAY reads a field the value is encoded into");
    }

    /** Each obligation broken on purpose: the item keeps its bytes, and the pass says why. */
    @Test
    void eachObligationBrokenKeepsTheItemsBytes() {
        assertRefused("01 G.\n   05 N PIC 9(3).\n01 R REDEFINES G PIC X(3).\n", "    ADD 1 TO N.\n", "redefines");
        assertRefused("01 G.\n   05 N PIC 9(3) OCCURS 3.\n", "    ADD 1 TO N (1).\n", "occurs");
        assertRefused("01 G.\n   05 N PIC 9(3).\n01 X PIC X(3).\n", "    ADD 1 TO N.\n    MOVE G TO X.\n", "a group holding it");
        assertRefused("01 G VALUE '123'.\n   05 N PIC 9(3).\n", "    ADD 1 TO N.\n", "has a VALUE");
        assertRefused("01 N PIC 9(3).\n", "    ADD 1 TO N.\n    CALL 'SUB' USING N.\n", "CALL");
        assertRefused("01 N PIC 9(3).\n", "    PERFORM VARYING N FROM 1 BY 1 UNTIL N > 3\n        DISPLAY N\n    END-PERFORM.\n", "varied by a PERFORM");
        assertRefused("01 N PIC 9(3).\n   88 DONE VALUE 9.\n", "    SET DONE TO TRUE.\n", "SET");
        assertRefused("01 N PIC 9(3).\n", "    ADD 1 TO N.\n    DISPLAY N (1:1).\n", "reference-modified");
        assertRefused("01 N PIC 9(3)PP.\n", "    ADD 1 TO N.\n", "P");
        assertRefused("01 N PIC 9(19).\n", "    ADD 1 TO N.\n", "18");
        assertRefused("01 N PIC 9(3) COMP-5.\n", "    ADD 1 TO N.\n", "usage");
        assertRefused("01 N PIC 9(3).\n", "    DISPLAY 'X'.\n", "never referenced");
        assertRefused("01 N PIC 9(3).\n", "    INITIALIZE N.\n", "INITIALIZE");
        assertRefused("01 G EXTERNAL.\n   05 N PIC 9(3).\n", "    ADD 1 TO N.\n", "EXTERNAL");
        assertRefused("01 G GLOBAL.\n   05 N PIC 9(3).\n", "    ADD 1 TO N.\n", "GLOBAL");
    }

    private static void assertRefused(String data, String procedure, String why) {
        Program p = program(data, procedure);
        Map<String, List<String>> refused = new LinkedHashMap<>();
        Set<Integer> promoted = Scalars.promote(p, refused);
        assertFalse(promoted.contains(id(p, "N")), data + procedure);
        assertTrue(refused.getOrDefault("item N", List.of()).stream().anyMatch(w -> w.contains(why)), why + ": " + refused);
    }
}
